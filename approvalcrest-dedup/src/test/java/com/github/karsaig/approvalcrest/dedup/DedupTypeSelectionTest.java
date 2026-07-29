package com.github.karsaig.approvalcrest.dedup;

import com.github.karsaig.approvalcrest.ApprovedFileType;

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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.Test;

/**
 * Dedup and reinstate can be limited to one approved file type. Both types share the same bucket
 * directories in the shared directory, so the interesting cases are about what a single-type run
 * must leave alone.
 */
public class DedupTypeSelectionTest {

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

    private List<Path> canonicalsWithSuffix(Path workDir, String suffix) throws IOException {
        Path shared = workDir.resolve(SHARED_DIR);
        if (!Files.exists(shared)) {
            return new ArrayList<>();
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> s = Files.walk(shared)) {
            s.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(suffix))
                    .forEach(result::add);
        }
        return result;
    }

    /** A duplicated pair of each type, none of them yet deduplicated. */
    private void writeDuplicatePairOfEachType(Path workDir) throws IOException {
        writeApproved(workDir, "src/test/java/m/j1-approved.json", "{\"v\":\"J\"}");
        writeApproved(workDir, "src/test/java/m/j2-approved.json", "{\"v\":\"J\"}");
        writeApproved(workDir, "src/test/java/m/c1-approved.content", "shared text");
        writeApproved(workDir, "src/test/java/m/c2-approved.content", "shared text");
    }

    private ApprovalDeduplicator dedup(Path workDir, String types) {
        return new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR,
                BUCKET_DEPTH, false, ApprovedFileType.parse(types));
    }

    @Test
    public void jsonOnlyConvertsJsonAndLeavesContentAlone() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            writeDuplicatePairOfEachType(workDir);

            ApprovalDeduplicator.DeduplicatorResult result = dedup(workDir, "json").deduplicate();

            assertEquals(1, result.getCanonicalsCreated(), "only the json group is canonicalised");
            assertEquals(2, result.getPointersWritten());
            assertEquals(1, canonicalsWithSuffix(workDir, ".json").size());
            assertEquals(0, canonicalsWithSuffix(workDir, ".content").size(), "no content canonical");

            assertTrue(read(workDir.resolve("src/test/java/m/j1-approved.json")).contains("/*pointer:"));
            assertFalse(read(workDir.resolve("src/test/java/m/c1-approved.content")).contains("/*pointer:"),
                    "content files must be untouched");
        }
    }

    @Test
    public void contentOnlyConvertsContentAndLeavesJsonAlone() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            writeDuplicatePairOfEachType(workDir);

            ApprovalDeduplicator.DeduplicatorResult result = dedup(workDir, "content").deduplicate();

            assertEquals(1, result.getCanonicalsCreated(), "only the content group is canonicalised");
            assertEquals(1, canonicalsWithSuffix(workDir, ".content").size());
            assertEquals(0, canonicalsWithSuffix(workDir, ".json").size(), "no json canonical");

            assertTrue(read(workDir.resolve("src/test/java/m/c1-approved.content")).contains("/*pointer:"));
            assertFalse(read(workDir.resolve("src/test/java/m/j1-approved.json")).contains("/*pointer:"),
                    "json files must be untouched");
        }
    }

    /** No selection keeps the previous behaviour: both types are processed. */
    @Test
    public void defaultProcessesBothTypes() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            writeDuplicatePairOfEachType(workDir);

            ApprovalDeduplicator.DeduplicatorResult result =
                    new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR,
                            BUCKET_DEPTH, false).deduplicate();

            assertEquals(2, result.getCanonicalsCreated(), "one canonical per type");
            assertEquals(4, result.getPointersWritten());
        }
    }

    /**
     * The case this feature could most easily get wrong. Both types live in the same bucket
     * directories, so if the type filter reached the reference lookup, a json-only run would not see
     * the content pointers, would judge their canonicals unreferenced, and would delete them.
     */
    @Test
    public void jsonOnlyRunDoesNotCollectContentCanonicals() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            writeDuplicatePairOfEachType(workDir);

            // First deduplicate everything, so content pointers and their canonical exist.
            new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false)
                    .deduplicate();
            assertEquals(1, canonicalsWithSuffix(workDir, ".content").size(), "setup");
            Path c1 = workDir.resolve("src/test/java/m/c1-approved.content");
            assertTrue(read(c1).contains("/*pointer:"), "setup: content is a pointer");

            // Now run json-only. The content canonical must survive and its pointer must resolve.
            ApprovalDeduplicator.DeduplicatorResult result = dedup(workDir, "json").deduplicate();

            assertEquals(0, result.getOrphansRemoved(), "nothing may be collected");
            assertEquals(1, canonicalsWithSuffix(workDir, ".content").size(),
                    "the content canonical must survive a json-only run");
            assertEquals("shared text",
                    new ApprovedFileScanner(SHARED_DIR, BUCKET_DEPTH, ApprovedFileType.all())
                            .utilsFor(ApprovedFileType.CONTENT).readFile(c1, workDir),
                    "the content pointer must still resolve");
        }
    }

    /** Reinstate is symmetric: a json-only reinstate leaves content pointers and canonicals alone. */
    @Test
    public void jsonOnlyReinstateLeavesContentPointersInPlace() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            writeDuplicatePairOfEachType(workDir);
            new ApprovalDeduplicator(workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false)
                    .deduplicate();
            Path c1 = workDir.resolve("src/test/java/m/c1-approved.content");
            Path j1 = workDir.resolve("src/test/java/m/j1-approved.json");

            ApprovalReinstate.ReinstateResult result =
                    new ApprovalReinstate(workDir, workDir.resolve("src/test/java"), SHARED_DIR, false,
                            ApprovedFileType.parse("json")).reinstate();

            assertEquals(2, result.getPointersReinstated(), "only the two json pointers");
            assertEquals(1, result.getCanonicalsDeleted(), "only the json canonical");
            assertFalse(read(j1).contains("/*pointer:"), "json is reinstated");
            assertTrue(read(c1).contains("/*pointer:"), "content stays a pointer");
            assertEquals(1, canonicalsWithSuffix(workDir, ".content").size(),
                    "the content canonical must survive");
        }
    }

    @Test
    public void cliAcceptsTypesFlag() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = fs.getPath("/work");
            writeDuplicatePairOfEachType(workDir);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DedupCli.run(new String[]{"--types", "content", "--dir", "src/test/java",
                    "--shared-dir", SHARED_DIR}, workDir, new PrintStream(out));

            assertEquals(1, canonicalsWithSuffix(workDir, ".content").size());
            assertEquals(0, canonicalsWithSuffix(workDir, ".json").size(), "json untouched via CLI");
        }
    }

    @Test
    public void unknownTypeIsRejectedWithTheValidValues() {
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> ApprovedFileType.parse("xml"));

        assertTrue(e.getMessage().contains("xml"), "names the offending value: " + e.getMessage());
        assertTrue(e.getMessage().contains("json") && e.getMessage().contains("content"),
                "lists the valid values: " + e.getMessage());
    }

    @Test
    public void parseAcceptsListsAndAll() {
        assertEquals(ApprovedFileType.all(), ApprovedFileType.parse(null));
        assertEquals(ApprovedFileType.all(), ApprovedFileType.parse("  "));
        assertEquals(ApprovedFileType.all(), ApprovedFileType.parse("all"));
        assertEquals(ApprovedFileType.all(), ApprovedFileType.parse("json, content"));
        assertEquals(1, ApprovedFileType.parse("JSON").size(), "matching is case-insensitive");
    }
}
