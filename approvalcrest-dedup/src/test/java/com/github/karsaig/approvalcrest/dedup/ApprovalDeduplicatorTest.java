package com.github.karsaig.approvalcrest.dedup;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.Test;

public class ApprovalDeduplicatorTest {

    private static final String SHARED_DIR = "src/test/java/shared-approvals";
    private static final int BUCKET_DEPTH = 2;

    private FileSystem newFs() {
        return Jimfs.newFileSystem(Configuration.unix());
    }

    private Path workingDir(FileSystem fs) throws IOException {
        Path work = fs.getPath("work");
        Files.createDirectories(work);
        return work;
    }

    private Path writeApprovedFile(Path workDir, String relPath, String comment, String content) throws IOException {
        Path file = workDir.resolve(relPath);
        Files.createDirectories(file.getParent());
        String fileContent = "/*" + comment + "*/\n" + content;
        Files.write(file, fileContent.getBytes(UTF_8));
        return file;
    }

    private String readFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), UTF_8);
    }

    private String computeKey(String content) {
        FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                new FileMatcherConfig(false, false, false, false, true, SHARED_DIR, true, BUCKET_DEPTH));
        return utils.computeContentKey(content);
    }

    private Path expectedCanonicalPath(Path workDir, String content) {
        String key = computeKey(content);
        String bucket = key.substring(0, BUCKET_DEPTH);
        return workDir.resolve(SHARED_DIR).resolve(bucket).resolve(key + "-approved.json");
    }

    @Test
    public void twoFilesWithIdenticalContentProduceSingleCanonicalAndTwoPointers() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"foo\":\"bar\"}";

            Path file1 = writeApprovedFile(workDir, "src/test/java/a/b/test1-approved.json", "a.b.test1", content);
            Path file2 = writeApprovedFile(workDir, "src/test/java/c/d/test2-approved.json", "c.d.test2", content);

            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            assertEquals(2, result.getPointersWritten(), "Both files should become pointers");
            assertEquals(1, result.getCanonicalsCreated(), "One canonical should be created");
            assertEquals(0, result.getOrphansRemoved());

            Path canonical = expectedCanonicalPath(workDir, content);
            assertTrue(Files.exists(canonical), "Canonical file must exist");
            assertTrue(readFile(canonical).contains(content), "Canonical must contain the content");

            String canonicalRelative = workDir.relativize(canonical).toString();
            String file1Content = readFile(file1);
            String file2Content = readFile(file2);
            assertTrue(file1Content.contains("/*pointer:"), "file1 should be a pointer");
            assertTrue(file1Content.contains(canonicalRelative), "file1 pointer should reference canonical");
            assertTrue(file2Content.contains("/*pointer:"), "file2 should be a pointer");
            assertTrue(file2Content.contains(canonicalRelative), "file2 pointer should reference canonical");
        }
    }

    @Test
    public void singleUniqueFileIsNotDeduplicated() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"unique\":true}";

            Path file = writeApprovedFile(workDir, "src/test/java/a/test-approved.json", "a.test", content);
            String originalContent = readFile(file);

            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            assertEquals(0, result.getPointersWritten(), "Unique file should not become a pointer");
            assertEquals(0, result.getCanonicalsCreated(), "No canonical should be created for unique file");
            assertEquals(originalContent, readFile(file), "File content should be unchanged");
        }
    }

    @Test
    public void threeFilesWithTwoIdenticalAndOneUniqueDedupsTwoOnly() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String sharedContent = "{\"shared\":true}";
            String uniqueContent = "{\"unique\":true}";

            Path file1 = writeApprovedFile(workDir, "src/test/java/a/shared1-approved.json", "a.shared1", sharedContent);
            Path file2 = writeApprovedFile(workDir, "src/test/java/b/shared2-approved.json", "b.shared2", sharedContent);
            Path file3 = writeApprovedFile(workDir, "src/test/java/c/unique-approved.json", "c.unique", uniqueContent);
            String originalUnique = readFile(file3);

            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            assertEquals(2, result.getPointersWritten());
            assertEquals(1, result.getCanonicalsCreated());
            assertTrue(readFile(file1).contains("/*pointer:"), "file1 should be a pointer");
            assertTrue(readFile(file2).contains("/*pointer:"), "file2 should be a pointer");
            assertEquals(originalUnique, readFile(file3), "Unique file should be unchanged");
        }
    }

    @Test
    public void newFileDuplicatingExistingCanonicalBecomesPointer() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"existing\":true}";

            // Pre-create canonical (simulates a previous dedup run)
            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                    new FileMatcherConfig(false, false, false, false, true, SHARED_DIR, true, BUCKET_DEPTH));
            utils.writeCanonical(content, "shared", workDir, SHARED_DIR, BUCKET_DEPTH);

            // New file with the same content
            Path newFile = writeApprovedFile(workDir, "src/test/java/a/new-approved.json", "a.new", content);

            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            assertEquals(1, result.getPointersWritten(), "New file matching existing canonical should become pointer");
            assertEquals(0, result.getCanonicalsCreated(), "No new canonical needed");
            assertTrue(readFile(newFile).contains("/*pointer:"), "New file should be a pointer");
        }
    }

    @Test
    public void orphanedCanonicalIsRemovedByGarbageCollection() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"orphan\":true}";

            // Create a canonical with no pointer referencing it
            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                    new FileMatcherConfig(false, false, false, false, true, SHARED_DIR, true, BUCKET_DEPTH));
            utils.writeCanonical(content, "shared", workDir, SHARED_DIR, BUCKET_DEPTH);
            Path canonical = expectedCanonicalPath(workDir, content);
            assertTrue(Files.exists(canonical), "Canonical must exist before GC");

            // No approved files reference it
            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            assertEquals(1, result.getOrphansRemoved(), "Orphaned canonical should be removed");
            assertFalse(Files.exists(canonical), "Canonical must not exist after GC");
        }
    }

    @Test
    public void existingPointerFileIsPreservedAndItsCanonicalIsNotOrphaned() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"pointed\":true}";

            // Create canonical
            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                    new FileMatcherConfig(false, false, false, false, true, SHARED_DIR, true, BUCKET_DEPTH));
            String canonicalRelative = utils.writeCanonical(content, "shared", workDir, SHARED_DIR, BUCKET_DEPTH);

            // Create a pointer file referencing the canonical
            Path pointer = workDir.resolve("src/test/java/a/ptr-approved.json");
            Files.createDirectories(pointer.getParent());
            Files.write(pointer, ("/*a.ptr*/\n/*pointer:" + canonicalRelative + "*/").getBytes(UTF_8));

            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            assertEquals(0, result.getOrphansRemoved(), "Referenced canonical must not be GC'd");
            Path canonical = expectedCanonicalPath(workDir, content);
            assertTrue(Files.exists(canonical), "Referenced canonical must still exist");
        }
    }

    @Test
    public void dryRunDoesNotModifyAnyFiles() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"dryrun\":true}";

            Path file1 = writeApprovedFile(workDir, "src/test/java/a/dry1-approved.json", "a.dry1", content);
            Path file2 = writeApprovedFile(workDir, "src/test/java/b/dry2-approved.json", "b.dry2", content);
            String original1 = readFile(file1);
            String original2 = readFile(file2);

            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, true);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            assertEquals(2, result.getPointersWritten(), "Dry-run should still report what would happen");
            assertEquals(1, result.getCanonicalsCreated());
            assertEquals(original1, readFile(file1), "file1 must not be modified in dry-run");
            assertEquals(original2, readFile(file2), "file2 must not be modified in dry-run");
            assertFalse(Files.exists(expectedCanonicalPath(workDir, content)), "No canonical written in dry-run");
        }
    }

    @Test
    public void contentFilesAreAlsoDeduplicated() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "plain text content";

            Path file1 = workDir.resolve("src/test/java/a/text1-approved.content");
            Path file2 = workDir.resolve("src/test/java/b/text2-approved.content");
            Files.createDirectories(file1.getParent());
            Files.createDirectories(file2.getParent());
            Files.write(file1, ("/*a.text1*/\n" + content).getBytes(UTF_8));
            Files.write(file2, ("/*b.text2*/\n" + content).getBytes(UTF_8));

            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            assertEquals(2, result.getPointersWritten());
            assertEquals(1, result.getCanonicalsCreated());

            // Canonical should have .content extension
            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("content",
                    new FileMatcherConfig(false, false, false, false, true, SHARED_DIR, true, BUCKET_DEPTH));
            String key = utils.computeContentKey(content);
            String bucket = key.substring(0, BUCKET_DEPTH);
            Path canonical = workDir.resolve(SHARED_DIR).resolve(bucket).resolve(key + "-approved.content");
            assertTrue(Files.exists(canonical), "Content canonical must have .content extension");
        }
    }

    @Test
    public void mixedJsonAndContentFilesWithSameContentAreGroupedSeparately() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String sameText = "same content";

            Path jsonFile1 = workDir.resolve("src/test/java/a/j1-approved.json");
            Path jsonFile2 = workDir.resolve("src/test/java/b/j2-approved.json");
            Path contentFile1 = workDir.resolve("src/test/java/c/c1-approved.content");
            Path contentFile2 = workDir.resolve("src/test/java/d/c2-approved.content");
            for (Path p : Arrays.asList(jsonFile1, jsonFile2, contentFile1, contentFile2)) {
                Files.createDirectories(p.getParent());
                Files.write(p, ("/*comment*/\n" + sameText).getBytes(UTF_8));
            }

            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            ApprovalDeduplicator.DeduplicatorResult result = dedup.deduplicate();

            // 2 JSON files → 1 JSON canonical; 2 content files → 1 content canonical
            assertEquals(4, result.getPointersWritten());
            assertEquals(2, result.getCanonicalsCreated());
            assertCanonicalCount(workDir, 2);
        }
    }

    @Test
    public void deduplicatedFileRoundtripMatchesLibraryWrittenContent() throws IOException {
        // Proves the seam: dedup reads files written by the library's writer, and the
        // library's findMatchingCanonical finds the canonical created by the dedup tool.
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"seam\":\"verified\"}";

            FileStoreMatcherUtils utils = new FileStoreMatcherUtils("json",
                    new FileMatcherConfig(false, false, false, false, true, SHARED_DIR, true, BUCKET_DEPTH));

            // Write two approved files using the library's own writer — same bytes the library produces at test time
            Path approvedFile1 = workDir.resolve("src/test/java/a/seam1-approved.json");
            Path approvedFile2 = workDir.resolve("src/test/java/b/seam2-approved.json");
            Files.createDirectories(approvedFile1.getParent());
            Files.createDirectories(approvedFile2.getParent());
            utils.writeStandaloneFile(approvedFile1, content, "a.seam1");
            utils.writeStandaloneFile(approvedFile2, content, "b.seam2");

            // Run dedup — it reads the library-written files
            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, BUCKET_DEPTH, false);
            dedup.deduplicate();

            // The library must find the canonical using the same key it would compute at runtime
            Optional<String> canonical = utils.findMatchingCanonical(content, workDir, SHARED_DIR, BUCKET_DEPTH);
            assertTrue(canonical.isPresent(), "Library's findMatchingCanonical must locate the dedup-created canonical");

            // Reading the pointer through the library must return the exact original content
            String readBack = utils.readFile(approvedFile1, workDir);
            assertEquals(content, readBack, "Reading pointer file through library must return original content");
        }
    }

    private void assertCanonicalCount(Path workDir, int expectedCount) throws IOException {
        Path sharedDirPath = workDir.resolve(SHARED_DIR);
        if (!Files.exists(sharedDirPath)) {
            assertEquals(0, expectedCount);
            return;
        }
        int count = 0;
        try (Stream<Path> stream = Files.walk(sharedDirPath)) {
            List<Path> canonicals = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return (name.endsWith("-approved.json") || name.endsWith("-approved.content"))
                                && !name.contains("-not-approved.");
                    })
                    .collect(Collectors.toList());
            count = canonicals.size();
        }
        assertEquals(expectedCount, count, "Canonical file count mismatch");
    }
}
