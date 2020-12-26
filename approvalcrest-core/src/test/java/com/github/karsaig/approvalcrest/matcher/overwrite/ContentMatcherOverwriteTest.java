package com.github.karsaig.approvalcrest.matcher.overwrite;

import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.DEFAULT_JIMFS_PERMISSIONS;
import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.FILE_CREATE_PERMISSONS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.ContentMatcher;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.github.karsaig.approvalcrest.util.InMemoryPermissions;

public class ContentMatcherOverwriteTest extends AbstractFileMatcherTest {

    @Test
    public void shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist() {
        String actual = "Test input data...";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherOverwriteTest", "shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, enableInPlaceOverwrite());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("c716ab", "b1fd39-not-approved.content", "b1fd39-approved.content"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("c716ab/b1fd39-not-approved.content", "/*ContentMatcherOverwriteTest.shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist*/\n" +
                    "Test input data...");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows() {
        String actual = "Test input data...";
        inMemoryWindowsFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherOverwriteTest", "shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, enableInPlaceOverwrite());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("c716ab", "d5edaa-not-approved.content", "d5edaa-approved.content"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("c716ab\\d5edaa-not-approved.content", "/*ContentMatcherOverwriteTest.shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows*/\n" +
                    "Test input data...");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists() {
        String actual = "Test input data...";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherOverwriteTest", "shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, enableInPlaceOverwrite());

            writeFile(imfsi.getTestPath().resolve("c716ab").resolve("24db15-approved.content"), "dummyContent");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("c716ab/24db15-approved.content", "/*ContentMatcherOverwriteTest.shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists*/\n" +
                    "Test input data...");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWithCorrectPermissionsWhenOverwriteInPlaceEnabledAndApprovedFileExists() {
        String actual = "Test input data...";
        InMemoryFsUtil.inMemoryUnixFsWithFileAttributeSupport(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherOverwriteTest", "shouldOverwriteApprovedFileWithCorrectPermissionsWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, enableInPlaceOverwrite());

            writeFile(imfsi.getTestPath().resolve("c716ab").resolve("00068b-approved.content"), "dummyContent");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryPermissions> actualFiles = InMemoryFsUtil.getPermissons(imfsi);
            List<InMemoryPermissions> expected = new ArrayList<>();
            expected.add(new InMemoryPermissions("c716ab", DEFAULT_JIMFS_PERMISSIONS));
            expected.add(new InMemoryPermissions("c716ab/00068b-approved.content", FILE_CREATE_PERMISSONS));
            assertIterableEquals(expected, actualFiles);
        });
    }

    @Test
    public void shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse() {
        String actual = "Test input data...";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherOverwriteTest", "shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse");
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("c716ab").resolve("ccb1cc-approved.content"), "differentContent");

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            MatcherAssert.assertThat(actualError.getMessage(), Matchers.containsString("Expected: differentContent\n" +
                    "     but: Expected file c716ab/ccb1cc-approved.content\n" +
                    "Content does not match!"));

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("c716ab/ccb1cc-approved.content", "differentContent");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }
}
