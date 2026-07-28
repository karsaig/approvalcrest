package com.github.karsaig.approvalcrest.dedup;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;
import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.Test;

/**
 * Reproducing tests for the data-loss findings in the dedup module (review findings 0.1 - 0.3).
 *
 * <p>Each test documents the CURRENT behaviour. Where the current behaviour is the bug, the
 * assertion asserts the buggy outcome and the javadoc states what the correct outcome would be,
 * so the test flips to red once the bug is fixed.
 */
public class DedupDataLossReproTest {

    private static final String SHARED_DIR = "src/test/java/shared-approvals";
    private static final int BUCKET_DEPTH = 2;

    private FileSystem newFs() {
        return Jimfs.newFileSystem(InMemoryFsUtil.JIMFS_UNIX_CONFIG);
    }

    private Path writeApproved(Path workDir, String relPath, String content) throws IOException {
        Path file = workDir.resolve(relPath);
        Files.createDirectories(file.getParent());
        Files.write(file, ("/*test*/\n" + content).getBytes(UTF_8));
        return file;
    }

    private String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), UTF_8);
    }

    private List<Path> listCanonicals(Path workDir) throws IOException {
        Path shared = workDir.resolve(SHARED_DIR);
        if (!Files.exists(shared)) {
            return new ArrayList<>();
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> s = Files.walk(shared)) {
            s.filter(Files::isRegularFile).forEach(result::add);
        }
        return result;
    }

    /**
     * Finding 0.1 - the GC pass deletes canonicals that are still referenced by pointer files
     * living outside the scan directory.
     *
     * <p>Correct behaviour: narrowing --dir must never delete a canonical that some pointer
     * elsewhere still needs, or must refuse to run the GC at all.
     */
    @Test
    public void gcDeletesCanonicalsStillReferencedOutsideTheScanDir() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");

            // moduleA has a duplicated pair, moduleB has a different duplicated pair.
            writeApproved(workDir, "src/test/java/moduleA/a1-approved.json", "{\"v\":\"A\"}");
            writeApproved(workDir, "src/test/java/moduleA/a2-approved.json", "{\"v\":\"A\"}");
            Path b1 = writeApproved(workDir, "src/test/java/moduleB/b1-approved.json", "{\"v\":\"B\"}");
            writeApproved(workDir, "src/test/java/moduleB/b2-approved.json", "{\"v\":\"B\"}");

            // First run covers the whole tree: two canonicals, four pointers.
            new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false)
                    .deduplicate();
            assertEquals(2, listCanonicals(workDir).size(), "setup: expected one canonical per group");

            String b1PointerBefore = read(b1);
            assertTrue(b1PointerBefore.contains("/*pointer:"),
                    "setup: moduleB file should now be a pointer, was: " + b1PointerBefore);

            // Second run is scoped to moduleA only - moduleB's pointers are never scanned.
            ApprovalDeduplicator.DeduplicatorResult result =
                    new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java/moduleA"), SHARED_DIR,
                            BUCKET_DEPTH, false).deduplicate();

            // BUG: moduleB's canonical is reported as an orphan and deleted.
            assertEquals(1, result.getOrphansRemoved(),
                    "BUG 0.1: moduleB's still-referenced canonical was garbage collected");
            assertEquals(1, listCanonicals(workDir).size(),
                    "BUG 0.1: only moduleA's canonical survives");

            // moduleB's pointer file still exists but now points at nothing: content is gone.
            assertEquals(b1PointerBefore, read(b1), "moduleB pointer was left untouched and now dangles");
            String target = b1PointerBefore.substring(b1PointerBefore.indexOf("/*pointer:")
                    + "/*pointer:".length(), b1PointerBefore.lastIndexOf("*/")).trim();
            assertFalse(Files.exists(workDir.resolve(target)),
                    "BUG 0.1: pointer target " + target + " no longer exists - approved content is unrecoverable");

            // The user-visible impact: moduleB's tests can no longer read their approved content.
            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                    new FileMatcherConfig(false, false, false, false, true, SHARED_DIR, true, BUCKET_DEPTH));
            assertThrows(NoSuchFileException.class, () -> utils.readFile(b1, workDir),
                    "BUG 0.1: every test behind moduleB's pointer now fails to read its approved file");
        }
    }

    /**
     * Finding 0.2 - DedupCli parses --dry-run but never passes it to the reinstate path, so
     * "--reinstate --dry-run" performs a full destructive run.
     *
     * <p>Correct behaviour: --dry-run must leave the filesystem byte-for-byte unchanged.
     */
    @Test
    public void reinstateIgnoresDryRunAndWritesAnyway() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            Path a1 = writeApproved(workDir, "src/test/java/moduleA/a1-approved.json", "{\"v\":\"A\"}");
            writeApproved(workDir, "src/test/java/moduleA/a2-approved.json", "{\"v\":\"A\"}");

            new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false)
                    .deduplicate();

            String pointerContent = read(a1);
            assertTrue(pointerContent.contains("/*pointer:"), "setup: expected a pointer file");
            assertEquals(1, listCanonicals(workDir).size(), "setup: expected one canonical");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DedupCli.run(new String[]{"--reinstate", "--dry-run",
                    "--dir", "src/test/java", "--shared-dir", SHARED_DIR}, workDir, new PrintStream(out));

            // BUG: despite --dry-run the pointer was rewritten and the canonical deleted.
            assertFalse(read(a1).contains("/*pointer:"),
                    "BUG 0.2: --dry-run still rewrote the pointer back to standalone content");
            assertEquals(0, listCanonicals(workDir).size(),
                    "BUG 0.2: --dry-run still deleted every canonical");
        }
    }

    /**
     * Finding 0.3 - reinstate deletes every approved file under the shared dir unconditionally.
     * When --shared-dir is pointed at the scan dir (easy to do: the DEFAULT shared dir already
     * lives inside the DEFAULT scan dir) every golden master in the project is deleted, and the
     * exclude filter means nothing is reinstated first.
     *
     * <p>Correct behaviour: refuse to run when sharedDir overlaps scanDir.
     */
    @Test
    public void reinstateWithSharedDirEqualToScanDirDeletesEveryApprovedFile() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            Path a1 = writeApproved(workDir, "src/test/java/moduleA/a1-approved.json", "{\"v\":\"A\"}");
            Path b1 = writeApproved(workDir, "src/test/java/moduleB/b1-approved.json", "{\"v\":\"B\"}");
            assertTrue(Files.exists(a1) && Files.exists(b1), "setup");

            ApprovalReinstate.ReinstateResult result =
                    new ApprovalReinstate(workDir, workDir.resolve("src/test/java"), "src/test/java").reinstate();

            // BUG: nothing reinstated, but both plain approved files were deleted.
            assertEquals(0, result.getPointersReinstated(), "nothing was reinstated");
            assertEquals(2, result.getCanonicalsDeleted(), "BUG 0.3: both approved files treated as canonicals");
            assertFalse(Files.exists(a1), "BUG 0.3: moduleA approved file deleted");
            assertFalse(Files.exists(b1), "BUG 0.3: moduleB approved file deleted");
        }
    }
}
