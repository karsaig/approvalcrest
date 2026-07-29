package com.github.karsaig.approvalcrest.dedup;

import com.github.karsaig.approvalcrest.ApprovedFileType;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.Test;

/**
 * The dedup tooling rewrites and deletes approved files, so these cover the ways it could destroy
 * content that cannot be recovered from disk.
 */
public class DedupSafetyTest {

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

    private Path setUpTwoModulesWithPointers(Path workDir) throws IOException {
        writeApproved(workDir, "src/test/java/moduleA/a1-approved.json", "{\"v\":\"A\"}");
        writeApproved(workDir, "src/test/java/moduleA/a2-approved.json", "{\"v\":\"A\"}");
        Path b1 = writeApproved(workDir, "src/test/java/moduleB/b1-approved.json", "{\"v\":\"B\"}");
        writeApproved(workDir, "src/test/java/moduleB/b2-approved.json", "{\"v\":\"B\"}");

        new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false)
                .deduplicate();
        return b1;
    }

    /**
     * Narrowing the scan directory must not delete canonicals that pointers elsewhere still need.
     * The scan directory says what to convert; it does not say what is still referenced.
     */
    @Test
    public void narrowedScanKeepsCanonicalsReferencedElsewhere() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            Path b1 = setUpTwoModulesWithPointers(workDir);
            assertEquals(2, listCanonicals(workDir).size(), "setup: one canonical per group");
            String b1PointerBefore = read(b1);

            ApprovalDeduplicator.DeduplicatorResult result =
                    new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java/moduleA"), SHARED_DIR,
                            BUCKET_DEPTH, false).deduplicate();

            assertEquals(0, result.getOrphansRemoved(), "moduleB's canonical is still referenced");
            assertEquals(2, listCanonicals(workDir).size(), "both canonicals must survive");

            assertEquals(b1PointerBefore, read(b1), "moduleB's pointer is untouched");
            assertEquals("{\"v\":\"B\"}", scannerUtils(workDir).readFile(b1, workDir),
                    "moduleB's approved content is still resolvable");
        }
    }

    private com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils scannerUtils(Path workDir) {
        return new ApprovedFileScanner(SHARED_DIR, BUCKET_DEPTH, ApprovedFileType.all()).utilsFor(ApprovedFileType.JSON);
    }

    /** A canonical nothing points at any more is still collected. */
    @Test
    public void trulyUnreferencedCanonicalIsStillRemoved() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            setUpTwoModulesWithPointers(workDir);

            // Delete moduleB's pointers outright, so its canonical becomes genuinely unreferenced.
            Files.delete(workDir.resolve("src/test/java/moduleB/b1-approved.json"));
            Files.delete(workDir.resolve("src/test/java/moduleB/b2-approved.json"));

            ApprovalDeduplicator.DeduplicatorResult result =
                    new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR,
                            BUCKET_DEPTH, false).deduplicate();

            assertEquals(1, result.getOrphansRemoved(), "the unreferenced canonical is collected");
            assertEquals(1, listCanonicals(workDir).size(), "only moduleA's canonical remains");
        }
    }

    /** --dry-run must leave the filesystem untouched on the reinstate path too. */
    @Test
    public void reinstateDryRunChangesNothing() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            Path a1 = writeApproved(workDir, "src/test/java/moduleA/a1-approved.json", "{\"v\":\"A\"}");
            writeApproved(workDir, "src/test/java/moduleA/a2-approved.json", "{\"v\":\"A\"}");
            new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false)
                    .deduplicate();

            String pointerBefore = read(a1);
            assertTrue(pointerBefore.contains("/*pointer:"), "setup: expected a pointer file");
            assertEquals(1, listCanonicals(workDir).size(), "setup: expected one canonical");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DedupCli.run(new String[]{"--reinstate", "--dry-run",
                    "--dir", "src/test/java", "--shared-dir", SHARED_DIR}, workDir, new PrintStream(out));

            assertEquals(pointerBefore, read(a1), "--dry-run must not rewrite the pointer");
            assertEquals(1, listCanonicals(workDir).size(), "--dry-run must not delete canonicals");
            assertTrue(out.toString().contains("2 pointer(s) replaced"),
                    "the preview still reports the two pointers it would rewrite, was: " + out);
            assertTrue(out.toString().contains("1 canonical(s) deleted"),
                    "the preview still reports the canonical it would delete, was: " + out);
        }
    }

    /** Without --dry-run the same command does perform the work. */
    @Test
    public void reinstateWithoutDryRunRewritesAndDeletes() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            Path a1 = writeApproved(workDir, "src/test/java/moduleA/a1-approved.json", "{\"v\":\"A\"}");
            writeApproved(workDir, "src/test/java/moduleA/a2-approved.json", "{\"v\":\"A\"}");
            new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false)
                    .deduplicate();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DedupCli.run(new String[]{"--reinstate",
                    "--dir", "src/test/java", "--shared-dir", SHARED_DIR}, workDir, new PrintStream(out));

            assertEquals("/*test*/\n{\"v\":\"A\"}", read(a1), "the pointer is replaced by its content");
            assertEquals(0, listCanonicals(workDir).size(), "canonicals are cleared");
        }
    }

    /**
     * A shared directory that contains the scan directory would make every approved file look like
     * a canonical. It is reachable by a single mistyped flag, because the default shared directory
     * already sits inside the default scan directory, so it has to be refused outright.
     */
    @Test
    public void refusesWhenSharedDirContainsScanDir() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            Path a1 = writeApproved(workDir, "src/test/java/moduleA/a1-approved.json", "{\"v\":\"A\"}");
            Path b1 = writeApproved(workDir, "src/test/java/moduleB/b1-approved.json", "{\"v\":\"B\"}");

            IllegalArgumentException reinstateError = assertThrows(IllegalArgumentException.class,
                    () -> new ApprovalReinstate(workDir, workDir.resolve("src/test/java"), "src/test/java"));
            assertTrue(reinstateError.getMessage().contains("contains the scan directory"),
                    "the error must explain the overlap, was: " + reinstateError.getMessage());

            assertThrows(IllegalArgumentException.class,
                    () -> new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), "src/test/java",
                            BUCKET_DEPTH, false));

            assertTrue(Files.exists(a1), "nothing may be deleted");
            assertTrue(Files.exists(b1), "nothing may be deleted");
        }
    }

    /** Reinstating a narrowed scan must not strip canonicals that pointers elsewhere still use. */
    @Test
    public void narrowedReinstateKeepsCanonicalsReferencedElsewhere() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            Path b1 = setUpTwoModulesWithPointers(workDir);

            ApprovalReinstate.ReinstateResult result =
                    new ApprovalReinstate(workDir, workDir.resolve("src/test/java/moduleA"), SHARED_DIR).reinstate();

            assertEquals(2, result.getPointersReinstated(), "only moduleA's pointers are reinstated");
            assertEquals(1, result.getCanonicalsDeleted(), "only moduleA's canonical is deleted");
            assertEquals("{\"v\":\"B\"}", scannerUtils(workDir).readFile(b1, workDir),
                    "moduleB's pointer must still resolve");
        }
    }
}
