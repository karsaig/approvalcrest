package com.github.karsaig.approvalcrest.matcher;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.util.InMemoryFiles;

public class ContentMatcherOverwriteTest extends AbstractFileMatcherTest {

    @Test
    public void shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist() {
        String actual = "Test input data...";
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherOverwriteTest", "shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo);
            underTest.setOverwriteInPlaceEnabled(true);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("c716ab", "b1fd39-not-approved.content", "b1fd39-approved.content"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("c716ab/b1fd39-not-approved.content", "/*ContentMatcherOverwriteTest.shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist*/\n" +
                    "Test input data...");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows() {
        String actual = "Test input data...";
        inMemoryWindowsFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherOverwriteTest", "shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo);
            underTest.setOverwriteInPlaceEnabled(true);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("c716ab", "d5edaa-not-approved.content", "d5edaa-approved.content"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("c716ab\\d5edaa-not-approved.content", "/*ContentMatcherOverwriteTest.shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows*/\n" +
                    "Test input data...");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists() {
        String actual = "Test input data...";
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherOverwriteTest", "shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo);
            underTest.setOverwriteInPlaceEnabled(true);

            writeFile(path.resolve("c716ab").resolve("24db15-approved.content"), "dummyContent");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("c716ab/24db15-approved.content", "/*ContentMatcherOverwriteTest.shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists*/\n" +
                    "Test input data...");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse() {
        String actual = "Test input data...";
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherOverwriteTest", "shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo);
            underTest.setOverwriteInPlaceEnabled(false);

            writeFile(path.resolve("c716ab").resolve("ccb1cc-approved.content"), "differentContent");

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            MatcherAssert.assertThat(actualError.getMessage(), Matchers.containsString("Expected: differentContent\n" +
                    "     but: Expected file c716ab\\ccb1cc-approved.content\n" +
                    "Content does not match!"));

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("c716ab/ccb1cc-approved.content", "differentContent");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }
}
