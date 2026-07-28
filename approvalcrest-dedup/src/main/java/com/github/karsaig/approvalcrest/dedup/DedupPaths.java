package com.github.karsaig.approvalcrest.dedup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;

/**
 * Path rules shared by the dedup and reinstate tools.
 *
 * <p>Both tools delete files, so both need the same answers to "is this configuration safe?" and
 * "what is still referenced?". Keeping those answers in one place stops the two from drifting.
 */
final class DedupPaths {

    private DedupPaths() {
    }

    /**
     * Renders a path as the project-relative, forward-slash form used inside pointer files.
     */
    static String relativize(Path workingDirectory, Path file) {
        return workingDirectory.relativize(file).toString().replace('\\', '/');
    }

    /**
     * Rejects a configuration where the shared directory contains the scan directory.
     *
     * <p>In that arrangement every approved file in the scan tree is also treated as a canonical,
     * so a run would delete the entire golden-master corpus while reinstating nothing. It is easy
     * to hit by accident because the default shared directory already sits inside the default scan
     * directory, so a single mistyped {@code --shared-dir} is enough.
     */
    static void requireSharedDirDoesNotContainScanDir(Path workingDirectory, Path scanDir, String sharedApprovalDir) {
        Path sharedDirPath = workingDirectory.resolve(sharedApprovalDir).normalize();
        Path normalizedScanDir = scanDir.normalize();
        if (normalizedScanDir.startsWith(sharedDirPath)) {
            throw new IllegalArgumentException(
                    "Refusing to run: the shared approvals directory '" + sharedDirPath
                            + "' contains the scan directory '" + normalizedScanDir
                            + "'. Every approved file in the scan directory would be treated as a canonical"
                            + " and deleted. Point --shared-dir at a directory that does not contain --dir.");
        }
    }

    /**
     * Returns the canonicals referenced by pointer files anywhere in the project.
     *
     * <p>Deliberately independent of the scan directory: a pointer outside the scanned subtree
     * keeps its canonical alive just as much as one inside it, and deleting on the narrower view
     * would leave those pointers resolving to a file that no longer exists.
     */
    static Set<String> collectReferencedCanonicals(ApprovedFileScanner scanner, Path workingDirectory, Path sharedDirPath)
            throws IOException {
        return collectReferencesIn(scanner, workingDirectory,
                scanner.collectApprovedFilesForReferenceLookup(workingDirectory), sharedDirPath);
    }

    /**
     * Returns the canonicals referenced by pointer files under a single directory.
     *
     * <p>Used by reinstate's dry run: the pointers it would rewrite are still on disk, so they have
     * to be discounted to describe the state a real run would leave behind.
     */
    static Set<String> collectReferencedCanonicalsUnder(ApprovedFileScanner scanner, Path workingDirectory,
                                                        Path dir, Path sharedDirPath) throws IOException {
        return collectReferencesIn(scanner, workingDirectory,
                scanner.collectApprovedFiles(dir, sharedDirPath), sharedDirPath);
    }

    private static Set<String> collectReferencesIn(ApprovedFileScanner scanner, Path workingDirectory,
                                                   Iterable<Path> files, Path sharedDirPath) throws IOException {
        Set<String> referenced = new HashSet<>();
        for (Path file : files) {
            if (file.normalize().startsWith(sharedDirPath)) {
                continue;
            }
            String ext = scanner.getExtension(file);
            if (ext == null) {
                continue;
            }
            FileStoreMatcherUtils utils = scanner.utilsFor(ext);
            if (!utils.isPointerFile(file)) {
                continue;
            }
            Optional<String> target = utils.readPointerTarget(file);
            target.ifPresent(t -> referenced.add(t.replace('\\', '/')));
        }
        return referenced;
    }
}
