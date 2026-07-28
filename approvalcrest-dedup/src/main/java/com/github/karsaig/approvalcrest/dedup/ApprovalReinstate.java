package com.github.karsaig.approvalcrest.dedup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;

/**
 * Replaces pointer files in a directory with standalone approved files containing the canonical
 * content, then removes the canonical files that nothing references any more.
 *
 * <p>After reinstating the whole project, the repository is in the same state as before any
 * deduplication was run.
 */
public class ApprovalReinstate {

    private final Path workingDirectory;
    private final Path scanDir;
    private final String sharedApprovalDir;
    private final boolean dryRun;
    private final ApprovedFileScanner scanner;

    public ApprovalReinstate(Path workingDirectory, Path scanDir, String sharedApprovalDir) {
        this(workingDirectory, scanDir, sharedApprovalDir, false);
    }

    public ApprovalReinstate(Path workingDirectory, Path scanDir, String sharedApprovalDir, boolean dryRun) {
        DedupPaths.requireSharedDirDoesNotContainScanDir(workingDirectory, scanDir, sharedApprovalDir);
        this.workingDirectory = workingDirectory;
        this.scanDir = scanDir;
        this.sharedApprovalDir = sharedApprovalDir;
        this.dryRun = dryRun;
        this.scanner = new ApprovedFileScanner(sharedApprovalDir, 2);
    }

    /**
     * Replaces pointers under the scan directory with their content, then deletes the canonicals
     * that are no longer referenced.
     *
     * <p>Deletion is reference-checked rather than unconditional. Reinstating a narrowed scan
     * directory leaves pointers elsewhere in the project untouched, and those still need their
     * canonicals; removing them would leave the pointers resolving to nothing.
     */
    public ReinstateResult reinstate() throws IOException {
        Path sharedDirPath = workingDirectory.resolve(sharedApprovalDir).normalize();

        List<Path> allApprovedFiles = scanner.collectApprovedFiles(scanDir, sharedDirPath);

        int pointersReinstated = 0;
        for (Path file : allApprovedFiles) {
            String ext = scanner.getExtension(file);
            if (ext == null) {
                continue;
            }
            FileStoreMatcherUtils utils = scanner.utilsFor(ext);
            if (utils.isPointerFile(file)) {
                String comment = utils.readComment(file);
                // readFile follows pointer chains and returns the final canonical content
                String content = utils.readFile(file, workingDirectory);
                if (!dryRun) {
                    utils.writeStandaloneFile(file, content, comment);
                }
                pointersReinstated++;
            }
        }

        int canonicalsDeleted = deleteUnreferencedCanonicals(sharedDirPath);

        return new ReinstateResult(pointersReinstated, canonicalsDeleted);
    }

    private int deleteUnreferencedCanonicals(Path sharedDirPath) throws IOException {
        if (!Files.exists(sharedDirPath)) {
            return 0;
        }

        // In dry-run nothing was rewritten, so the pointers this run would have reinstated still
        // look live on disk. Exclude the scan directory from the reference lookup to describe the
        // state a real run would leave behind.
        Set<String> referenced = DedupPaths.collectReferencedCanonicals(scanner, workingDirectory, sharedDirPath);
        if (dryRun) {
            referenced.removeAll(DedupPaths.collectReferencedCanonicalsUnder(scanner, workingDirectory, scanDir, sharedDirPath));
        }

        int canonicalsDeleted = 0;
        for (Path canonical : scanner.collectApprovedFiles(sharedDirPath, null)) {
            if (!referenced.contains(DedupPaths.relativize(workingDirectory, canonical))) {
                if (!dryRun) {
                    Files.delete(canonical);
                }
                canonicalsDeleted++;
            }
        }
        if (!dryRun) {
            deleteEmptyDirs(sharedDirPath);
        }
        return canonicalsDeleted;
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
