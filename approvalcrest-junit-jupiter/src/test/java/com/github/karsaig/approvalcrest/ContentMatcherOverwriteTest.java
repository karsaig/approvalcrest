package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.assertContains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.karsaig.approvalcrest.matcher.Matchers;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ContentMatcherOverwriteTest {
    private static final String OVERWRITE_FLAG_NAME = "jsonMatcherUpdateInPlace";

    @TempDir
    public File testFolder;

    @AfterAll
    public static void tearDown() {
        disableOverwrite();
    }

    @Test
    public void shouldThrowExceptionWhenSystemPropertyIsSetAndApprovedFileDoesNotExist() {
        // GIVEN
        enableOverwrite();
        String input = "Test input data...";

        // WHEN
        AssertionError exception = assertThrows(AssertionError.class, () -> {
            MatcherAssert.assertThat(input, Matchers.sameContentAsApproved()
                    .withPathName(testFolder.getAbsolutePath()).withFileName("notExistingApprovedFile"));
        });

        // THEN
        assertEquals("Not approved file created: 'notExistingApprovedFile-not-approved.content';\n"
                + " please verify its contents and rename it to 'notExistingApprovedFile-approved.content'.", exception.getMessage());
    }

    @Test
    public void shouldOverwriteApprovedFileWhenSystemPropertyIsSetAndApprovedFileExists() throws IOException {
        // GIVEN
        File tmp = new File(testFolder.getAbsolutePath() + "/overwriteTestInput-approved.content");
        enableOverwrite();

        String input = "Overwritten content...";
        Files.copy(new File("src/test/overwriteTestInputToCopy.content"), tmp);
        // WHEN
        MatcherAssert.assertThat(input, Matchers.sameContentAsApproved()
                .withPathName(testFolder.getAbsolutePath()).withFileName("overwriteTestInput"));
        // THEN
        String actual = Files.toString(tmp, Charsets.UTF_8);
        String expected = "/*com.github.karsaig.approvalcrest.ContentMatcherOverwriteTest.shouldOverwriteApprovedFileWhenSystemPropertyIsSetAndApprovedFileExists*/\nOverwritten content...";
        assertThat(actual, is(expected));

    }

    @Test
    public void shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse() throws IOException {
        // GIVEN
        File tmp = new File(testFolder.getAbsolutePath() + "/overwriteTestInput2-approved.content");
        disableOverwrite();
        String input = "Overwritten content...";
        Files.copy(new File("src/test/overwriteTestInputToCopy.content"), tmp);

        // WHEN
        AssertionError exception = assertThrows(AssertionError.class, () -> {
            MatcherAssert.assertThat(input, Matchers.sameContentAsApproved()
                    .withPathName(testFolder.getAbsolutePath()).withFileName("overwriteTestInput2"));
        });

        // THEN
        assertContains("Content does not match! expected:<[Original content!]> but was:<[Overwritten content...]>", exception.getMessage());
    }

    private static void enableOverwrite() {
        System.setProperty(OVERWRITE_FLAG_NAME, "true");
    }

    private static void disableOverwrite() {
        System.clearProperty(OVERWRITE_FLAG_NAME);
    }
}
