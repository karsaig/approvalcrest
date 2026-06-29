package com.github.karsaig.approvalcrest.dedup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;

/**
 * Replaces all pointer files in a directory with standalone approved files containing the
 * canonical content, then removes all canonical files from the shared directory.
 *
 * <p>After reinstate, the repository is in the same state as before any deduplication was run.
 */
public class ApprovalReinstate {

    private final Path workingDirectory;
    private final Path scanDir;
    private final String sharedApprovalDir;
    private final FileStoreMatcherUtils jsonUtils;
    private final FileStoreMatcherUtils contentUtils;

    public ApprovalReinstate(Path workingDirectory, Path scanDir, String sharedApprovalDir) {
        this.workingDirectory = workingDirectory;
        this.scanDir = scanDir;
        this.sharedApprovalDir = sharedApprovalDir;
        FileMatcherConfig config = new FileMatcherConfig(false, false, false, false, true, sharedApprovalDir, true, 2);
        this.jsonUtils = new FileStoreMatcherUtils("json", config);
        this.contentUtils = new FileStoreMatcherUtils("content", config);
    }

    public ReinstateResult reinstate() throws IOException {
        Path sharedDirPath = workingDirectory.resolve(sharedApprovalDir).normalize();

        // Collect approved files in the scan dir, excluding the shared dir (canonicals are not pointers)
        List<Path> allApprovedFiles = collectApprovedFiles(scanDir, sharedDirPath);

        int pointersReinstated = 0;
        for (Path file : allApprovedFiles) {
            String ext = getExtension(file);
            if (ext == null) {
                continue;
            }
            FileStoreMatcherUtils utils = utilsFor(ext);
            if (utils.isPointerFile(file)) {
                String comment = utils.readComment(file);
                // readFile follows pointer chains and returns the final canonical content
                String content = utils.readFile(file, workingDirectory);
                utils.writeStandaloneFile(file, content, comment);
                pointersReinstated++;
            }
        }

        // Delete all canonical files from the shared directory, then clean up empty dirs
        int canonicalsDeleted = 0;
        if (Files.exists(sharedDirPath)) {
            List<Path> canonicals = collectApprovedFiles(sharedDirPath, null);
            for (Path canonical : canonicals) {
                Files.delete(canonical);
                canonicalsDeleted++;
            }
            deleteEmptyDirs(sharedDirPath);
        }

        return new ReinstateResult(pointersReinstated, canonicalsDeleted);
    }

    private void deleteEmptyDirs(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return;
        }
        List<Path> children = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.forEach(children::add);
        }
        for (Path child : children) {
            if (Files.isDirectory(child)) {
                deleteEmptyDirs(child);
            }
        }
        List<Path> remaining = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.forEach(remaining::add);
        }
        if (remaining.isEmpty()) {
            Files.delete(dir);
        }
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

    public static class ReinstateResult {
        private final int pointersReinstated;
        private final int canonicalsDeleted;

        public ReinstateResult(int pointersReinstated, int canonicalsDeleted) {
            this.pointersReinstated = pointersReinstated;
            this.canonicalsDeleted = canonicalsDeleted;
        }

        public int getPointersReinstated() {
            return pointersReinstated;
        }

        public int getCanonicalsDeleted() {
            return canonicalsDeleted;
        }

        @Override
        public String toString() {
            return "Reinstate complete: " + pointersReinstated + " pointer(s) replaced with standalone content, "
                    + canonicalsDeleted + " canonical(s) deleted.";
        }
    }
}
