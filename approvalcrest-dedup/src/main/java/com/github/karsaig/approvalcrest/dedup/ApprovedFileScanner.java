package com.github.karsaig.approvalcrest.dedup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;

/**
 * Locates approved files on disk for the dedup tooling.
 *
 * <p>Both {@link ApprovalDeduplicator} and {@link ApprovalReinstate} need the same two scans, and
 * they need to agree about them exactly: a canonical file may only be deleted when nothing points
 * at it any more, so the scan that finds pointers must not be narrower than the scan that finds
 * canonicals.
 */
class ApprovedFileScanner {

    /**
     * Directories never worth walking when looking for pointer references. Without this the
     * project-wide reference scan would descend into build output and version control metadata.
     */
    private static final Set<String> PRUNED_DIRECTORIES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    ".git", ".hg", ".svn", "target", "build", "out", "node_modules", ".gradle", ".idea")));

    private final FileStoreMatcherUtils jsonUtils;
    private final FileStoreMatcherUtils contentUtils;

    ApprovedFileScanner(String sharedApprovalDir, int bucketDepth) {
        FileMatcherConfig config =
                new FileMatcherConfig(false, false, false, false, true, sharedApprovalDir, true, bucketDepth);
        this.jsonUtils = new FileStoreMatcherUtils("json", config);
        this.contentUtils = new FileStoreMatcherUtils("content", config);
    }

    /**
     * Returns every approved file under {@code dir}, optionally excluding a subtree.
     */
    List<Path> collectApprovedFiles(Path dir, Path excludeDir) throws IOException {
        if (!Files.exists(dir)) {
            return Collections.emptyList();
        }
        List<Path> result = new ArrayList<>();
        Path normalizedExclude = excludeDir == null ? null : excludeDir.normalize();
        try (Stream<Path> stream = Files.walk(dir)) {
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

    /**
     * Returns every approved file in the project, skipping build output and VCS metadata. Used to
     * find the pointers that reference canonicals, which is deliberately independent of the scan
     * directory: narrowing the scan must never make a live canonical look unreferenced.
     */
    List<Path> collectApprovedFilesForReferenceLookup(Path workingDirectory) throws IOException {
        if (!Files.exists(workingDirectory)) {
            return Collections.emptyList();
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(workingDirectory)) {
            stream.filter(p -> !isPruned(workingDirectory, p))
                    .filter(Files::isRegularFile)
                    .filter(p -> isApprovedFileName(p.getFileName().toString()))
                    .forEach(result::add);
        }
        return result;
    }

    private boolean isPruned(Path root, Path candidate) {
        Path relative = root.relativize(candidate);
        for (Path segment : relative) {
            if (PRUNED_DIRECTORIES.contains(segment.toString())) {
                return true;
            }
        }
        return false;
    }

    boolean isApprovedFileName(String name) {
        return (name.endsWith("-approved.json") || name.endsWith("-approved.content"))
                && !name.contains("-not-approved.");
    }

    /**
     * Returns the approved file's extension, or null if it is neither json nor content.
     */
    String getExtension(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".json")) {
            return "json";
        } else if (name.endsWith(".content")) {
            return "content";
        }
        return null;
    }

    FileStoreMatcherUtils utilsFor(String extension) {
        if ("content".equals(extension)) {
            return contentUtils;
        }
        return jsonUtils;
    }
}
