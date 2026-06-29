package com.github.karsaig.approvalcrest.dedup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;

/**
 * Scans a directory for duplicate approved files and replaces them with pointer files referencing
 * a single canonical file in a shared directory.
 *
 * <p>Files that are already pointer files are not modified; their existing canonical references
 * are tracked to prevent garbage-collection of still-referenced canonicals.
 *
 * <p>After deduplication, any canonical in the shared directory not referenced by any pointer
 * file is removed (garbage collection).
 */
public class ApprovalDeduplicator {

    private final Path workingDirectory;
    private final Path scanDir;
    private final String sharedApprovalDir;
    private final int bucketDepth;
    private final boolean dryRun;
    private final FileStoreMatcherUtils jsonUtils;
    private final FileStoreMatcherUtils contentUtils;

    public ApprovalDeduplicator(Path workingDirectory, Path scanDir, String sharedApprovalDir, int bucketDepth, boolean dryRun) {
        this.workingDirectory = workingDirectory;
        this.scanDir = scanDir;
        this.sharedApprovalDir = sharedApprovalDir;
        this.bucketDepth = bucketDepth;
        this.dryRun = dryRun;
        FileMatcherConfig config = new FileMatcherConfig(false, false, false, false, true, sharedApprovalDir, true, bucketDepth);
        this.jsonUtils = new FileStoreMatcherUtils("json", config);
        this.contentUtils = new FileStoreMatcherUtils("content", config);
    }

    public DeduplicatorResult deduplicate() throws IOException {
        Path sharedDirPath = workingDirectory.resolve(sharedApprovalDir).normalize();

        // Collect all approved files from the scan dir, excluding the shared dir
        List<Path> allApprovedFiles = collectApprovedFiles(scanDir, sharedDirPath);

        // Separate pointer files (track their references) from content files (group for dedup)
        Set<String> referencedCanonicals = new HashSet<>();
        Map<String, List<ApprovedFileEntry>> contentGroups = new LinkedHashMap<>();

        for (Path file : allApprovedFiles) {
            String ext = getExtension(file);
            if (ext == null) {
                continue;
            }
            FileStoreMatcherUtils utils = utilsFor(ext);
            if (utils.isPointerFile(file)) {
                Optional<String> target = utils.readPointerTarget(file);
                if (target.isPresent()) {
                    referencedCanonicals.add(target.get().replace('\\', '/'));
                }
            } else {
                String content = utils.readFile(file, workingDirectory);
                String key = utils.computeContentKey(content);
                String comment = utils.readComment(file);
                String groupKey = key + "." + ext;
                List<ApprovedFileEntry> group = contentGroups.get(groupKey);
                if (group == null) {
                    group = new ArrayList<>();
                    contentGroups.put(groupKey, group);
                }
                group.add(new ApprovedFileEntry(file, key, content, comment, ext));
            }
        }

        // For each group: point to existing canonical, or create a new one for groups of size >= 2
        int pointersWritten = 0;
        int canonicalsCreated = 0;

        for (Map.Entry<String, List<ApprovedFileEntry>> entry : contentGroups.entrySet()) {
            List<ApprovedFileEntry> group = entry.getValue();
            ApprovedFileEntry first = group.get(0);
            FileStoreMatcherUtils utils = utilsFor(first.extension);

            Optional<String> existingCanonical = utils.findMatchingCanonical(
                    first.content, workingDirectory, sharedApprovalDir, bucketDepth);

            String canonicalRelative;
            if (existingCanonical.isPresent()) {
                canonicalRelative = existingCanonical.get();
            } else if (group.size() >= 2) {
                if (!dryRun) {
                    canonicalRelative = utils.writeCanonical(
                            first.content, "shared", workingDirectory, sharedApprovalDir, bucketDepth);
                } else {
                    String key = utils.computeContentKey(first.content);
                    String bucket = key.substring(0, bucketDepth);
                    canonicalRelative = sharedApprovalDir + "/" + bucket + "/" + key + "-approved." + first.extension;
                }
                canonicalsCreated++;
            } else {
                // Single unique file with no existing canonical — nothing to do
                continue;
            }

            referencedCanonicals.add(canonicalRelative);

            for (ApprovedFileEntry fileEntry : group) {
                if (!dryRun) {
                    utilsFor(fileEntry.extension).writePointerFile(fileEntry.path, fileEntry.comment, canonicalRelative);
                }
                pointersWritten++;
            }
        }

        // GC: remove any canonical in the shared dir that no pointer references
        int orphansRemoved = 0;
        if (Files.exists(sharedDirPath)) {
            List<Path> allCanonicals = collectApprovedFiles(sharedDirPath, null);
            for (Path canonical : allCanonicals) {
                String relPath = workingDirectory.relativize(canonical).toString().replace('\\', '/');
                if (!referencedCanonicals.contains(relPath)) {
                    if (!dryRun) {
                        Files.delete(canonical);
                    }
                    orphansRemoved++;
                }
            }
        }

        return new DeduplicatorResult(pointersWritten, canonicalsCreated, orphansRemoved);
    }

    private List<Path> collectApprovedFiles(Path dir, Path excludeDir) throws IOException {
        if (!Files.exists(dir)) {
            return Collections.emptyList();
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(dir)) {
            final Path normalizedExclude = excludeDir == null ? null : excludeDir.normalize();
            stream.filter(p -> {
                if (!Files.isRegularFile(p)) {
                    return false;
                }
                if (normalizedExclude != null && p.normalize().startsWith(normalizedExclude)) {
                    return false;
                }
                return isApprovedFileName(p.getFileName().toString());
            }).forEach(result::add);
        }
        return result;
    }

    private boolean isApprovedFileName(String name) {
        return (name.endsWith("-approved.json") || name.endsWith("-approved.content"))
                && !name.contains("-not-approved.");
    }

    private String getExtension(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".json")) {
            return "json";
        } else if (name.endsWith(".content")) {
            return "content";
        }
        return null;
    }

    private FileStoreMatcherUtils utilsFor(String extension) {
        if ("content".equals(extension)) {
            return contentUtils;
        }
        return jsonUtils;
    }

    static class ApprovedFileEntry {
        final Path path;
        final String key;
        final String content;
        final String comment;
        final String extension;

        ApprovedFileEntry(Path path, String key, String content, String comment, String extension) {
            this.path = path;
            this.key = key;
            this.content = content;
            this.comment = comment;
            this.extension = extension;
        }
    }

    public static class DeduplicatorResult {
        private final int pointersWritten;
        private final int canonicalsCreated;
        private final int orphansRemoved;

        public DeduplicatorResult(int pointersWritten, int canonicalsCreated, int orphansRemoved) {
            this.pointersWritten = pointersWritten;
            this.canonicalsCreated = canonicalsCreated;
            this.orphansRemoved = orphansRemoved;
        }

        public int getPointersWritten() {
            return pointersWritten;
        }

        public int getCanonicalsCreated() {
            return canonicalsCreated;
        }

        public int getOrphansRemoved() {
            return orphansRemoved;
        }

        @Override
        public String toString() {
            return "Deduplication complete: " + canonicalsCreated + " canonical(s) created, "
                    + pointersWritten + " pointer(s) written, "
                    + orphansRemoved + " orphaned canonical(s) removed.";
        }
    }
}
