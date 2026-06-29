package com.github.karsaig.approvalcrest.dedup;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.Test;

public class ApprovalReinstateTest {

    private static final String SHARED_DIR = "src/test/java/shared-approvals";

    private FileSystem newFs() {
        return Jimfs.newFileSystem(Configuration.unix());
    }

    private Path workingDir(FileSystem fs) throws IOException {
        Path work = fs.getPath("work");
        Files.createDirectories(work);
        return work;
    }

    private String readFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), UTF_8);
    }

    private String writeCanonicalAndGetRelative(Path workDir, String content, String extension) throws IOException {
        FileStoreMatcherUtils utils = new FileStoreMatcherUtils(extension,
                new FileMatcherConfig(false, false, false, false, true, SHARED_DIR, true, 2));
        return utils.writeCanonical(content, "shared", workDir, SHARED_DIR, 2);
    }

    private Path writePointerFile(Path workDir, String relPath, String comment, String canonicalRelative) throws IOException {
        Path file = workDir.resolve(relPath);
        Files.createDirectories(file.getParent());
        Files.write(file, ("/*" + comment + "*/\n/*pointer:" + canonicalRelative + "*/").getBytes(UTF_8));
        return file;
    }

    @Test
    public void pointerFilesAreReplacedWithStandaloneContent() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"reinstated\":true}";

            String canonicalRelative = writeCanonicalAndGetRelative(workDir, content, "json");
            Path pointer = writePointerFile(workDir, "src/test/java/a/test-approved.json", "a.test", canonicalRelative);

            ApprovalReinstate reinstate = new ApprovalReinstate(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR);
            ApprovalReinstate.ReinstateResult result = reinstate.reinstate();

            assertEquals(1, result.getPointersReinstated());
            String fileContent = readFile(pointer);
            assertFalse(fileContent.contains("/*pointer:"), "Reinstated file must not contain pointer");
            assertTrue(fileContent.contains(content), "Reinstated file must contain the canonical content");
            assertTrue(fileContent.startsWith("/*a.test*/"), "Reinstated file must preserve original comment");
        }
    }

    @Test
    public void canonicalsAreDeletedAfterReinstate() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"canonical\":true}";

            String canonicalRelative = writeCanonicalAndGetRelative(workDir, content, "json");
            Path canonical = workDir.resolve(canonicalRelative);
            assertTrue(Files.exists(canonical), "Canonical must exist before reinstate");

            writePointerFile(workDir, "src/test/java/a/test-approved.json", "a.test", canonicalRelative);

            ApprovalReinstate reinstate = new ApprovalReinstate(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR);
            ApprovalReinstate.ReinstateResult result = reinstate.reinstate();

            assertEquals(1, result.getCanonicalsDeleted());
            assertFalse(Files.exists(canonical), "Canonical must be deleted after reinstate");
        }
    }

    @Test
    public void sharedDirectoryIsRemovedWhenEmptyAfterReinstate() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"cleanup\":true}";

            String canonicalRelative = writeCanonicalAndGetRelative(workDir, content, "json");
            writePointerFile(workDir, "src/test/java/a/test-approved.json", "a.test", canonicalRelative);

            Path sharedDirPath = workDir.resolve(SHARED_DIR);
            assertTrue(Files.exists(sharedDirPath));

            ApprovalReinstate reinstate = new ApprovalReinstate(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR);
            reinstate.reinstate();

            assertFalse(Files.exists(sharedDirPath), "Shared dir should be deleted when empty after reinstate");
        }
    }

    @Test
    public void multiplePointerFilesAllReinstated() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content1 = "{\"a\":1}";
            String content2 = "{\"b\":2}";

            String canonical1 = writeCanonicalAndGetRelative(workDir, content1, "json");
            String canonical2 = writeCanonicalAndGetRelative(workDir, content2, "json");

            Path ptr1 = writePointerFile(workDir, "src/test/java/a/test1-approved.json", "a.test1", canonical1);
            Path ptr2 = writePointerFile(workDir, "src/test/java/b/test2-approved.json", "b.test2", canonical1);
            Path ptr3 = writePointerFile(workDir, "src/test/java/c/test3-approved.json", "c.test3", canonical2);

            ApprovalReinstate reinstate = new ApprovalReinstate(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR);
            ApprovalReinstate.ReinstateResult result = reinstate.reinstate();

            assertEquals(3, result.getPointersReinstated());
            assertEquals(2, result.getCanonicalsDeleted());

            assertTrue(readFile(ptr1).contains(content1));
            assertTrue(readFile(ptr2).contains(content1));
            assertTrue(readFile(ptr3).contains(content2));
        }
    }

    @Test
    public void nonPointerApprovedFilesAreNotModifiedDuringReinstate() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String standaloneContent = "{\"standalone\":true}";
            String pointerContent = "{\"pointed\":true}";

            String canonicalRelative = writeCanonicalAndGetRelative(workDir, pointerContent, "json");

            Path standalone = workDir.resolve("src/test/java/a/standalone-approved.json");
            Files.createDirectories(standalone.getParent());
            Files.write(standalone, ("/*a.standalone*/\n" + standaloneContent).getBytes(UTF_8));
            String originalStandalone = readFile(standalone);

            writePointerFile(workDir, "src/test/java/b/pointed-approved.json", "b.pointed", canonicalRelative);

            ApprovalReinstate reinstate = new ApprovalReinstate(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR);
            ApprovalReinstate.ReinstateResult result = reinstate.reinstate();

            assertEquals(1, result.getPointersReinstated(), "Only pointer files should be counted");
            assertEquals(originalStandalone, readFile(standalone), "Standalone file must be unchanged");
        }
    }

    @Test
    public void reinstateRoundtripMatchesOriginalContent() throws IOException {
        try (FileSystem fs = newFs()) {
            Path workDir = workingDir(fs);
            String content = "{\"key\":\"value\",\"nested\":{\"a\":1}}";

            // Simulate: original file → dedup creates canonical + pointer → reinstate
            Path originalFile = workDir.resolve("src/test/java/x/original-approved.json");
            Files.createDirectories(originalFile.getParent());
            Files.write(originalFile, ("/*x.original*/\n" + content).getBytes(UTF_8));

            // Dedup
            Path anotherFile = workDir.resolve("src/test/java/y/another-approved.json");
            Files.createDirectories(anotherFile.getParent());
            Files.write(anotherFile, ("/*y.another*/\n" + content).getBytes(UTF_8));
            ApprovalDeduplicator dedup = new ApprovalDeduplicator(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR, 2, false);
            dedup.deduplicate();

            assertTrue(readFile(originalFile).contains("/*pointer:"), "Should be pointer after dedup");

            // Reinstate
            ApprovalReinstate reinstate = new ApprovalReinstate(
                    workDir, workDir.resolve("src/test/java"), SHARED_DIR);
            reinstate.reinstate();

            String restoredContent = readFile(originalFile);
            assertFalse(restoredContent.contains("/*pointer:"), "Should not be pointer after reinstate");
            assertTrue(restoredContent.contains(content), "Content must match original after reinstate");
        }
    }
}
