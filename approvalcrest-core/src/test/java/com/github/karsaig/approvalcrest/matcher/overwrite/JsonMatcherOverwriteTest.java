package com.github.karsaig.approvalcrest.matcher.overwrite;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.github.karsaig.approvalcrest.util.InMemoryPermissions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.github.karsaig.approvalcrest.StringUtil.normalizeNewLines;
import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.DEFAULT_JIMFS_PERMISSIONS;
import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.FILE_CREATE_PERMISSONS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonMatcherOverwriteTest extends AbstractFileMatcherTest {

    @Test
    public void shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwrite());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("11ee79", "b1fd39-not-approved.json", "b1fd39-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79/b1fd39-not-approved.json", "/*JsonMatcherOverwriteTest.shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExist*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanByte\": 2,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanInteger\": 4,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryWindowsFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwrite());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("11ee79", "d5edaa-not-approved.json", "d5edaa-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79\\d5edaa-not-approved.json", "/*JsonMatcherOverwriteTest.shouldThrowExceptionWhenOverwriteInPlaceEnabledAndApprovedFileDoesNotExistOnWindows*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanByte\": 2,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanInteger\": 4,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwrite());

            writeFile(imfsi.getTestPath().resolve("11ee79").resolve("24db15-approved.json"), "dummyContent");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79/24db15-approved.json", "/*JsonMatcherOverwriteTest.shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanByte\": 2,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanInteger\": 4,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExistsButContainsNewLineOnly() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwrite());

            writeFile(imfsi.getTestPath().resolve("11ee79").resolve("24db15-approved.json"), "");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79/24db15-approved.json", "/*JsonMatcherOverwriteTest.shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanByte\": 2,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanInteger\": 4,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExistsButEmpty() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwrite());

            writeFile(imfsi.getTestPath().resolve("11ee79").resolve("24db15-approved.json"), null);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79/24db15-approved.json", "/*JsonMatcherOverwriteTest.shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanByte\": 2,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanInteger\": 4,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExistsButEmptyWithIgnoringFields() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            JsonMatcher<Object> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwrite()).ignoring("beanByte", "beanInteger");

            writeFile(imfsi.getTestPath().resolve("11ee79").resolve("24db15-approved.json"), null);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79/24db15-approved.json", "/*JsonMatcherOverwriteTest.shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWithCorrectPermissionsWhenOverwriteInPlaceEnabledAndApprovedFileExists() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        InMemoryFsUtil.inMemoryUnixFsWithFileAttributeSupport(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldOverwriteApprovedFileWithCorrectPermissionsWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwrite());

            writeFile(imfsi.getTestPath().resolve("11ee79").resolve("00068b-approved.json"), "dummyContent");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryPermissions> actualFiles = InMemoryFsUtil.getPermissons(imfsi);
            List<InMemoryPermissions> expected = new ArrayList<>();
            expected.add(new InMemoryPermissions("11ee79", DEFAULT_JIMFS_PERMISSIONS));
            expected.add(new InMemoryPermissions("11ee79/00068b-approved.json", FILE_CREATE_PERMISSONS));
            assertIterableEquals(expected, actualFiles);
        });
    }

    @Test
    public void shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("11ee79").resolve("ccb1cc-approved.json"), "differentContent");

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            MatcherAssert.assertThat(normalizeNewLines(actualError.getMessage()), Matchers.containsString("Expected: \"differentContent\"\n" +
                    "     but: Expected file 11ee79/ccb1cc-approved.json\n" +
                    "\n"));

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79/ccb1cc-approved.json", "differentContent");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldLeaveApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExistsButContainingIgnoredFieldsAndStrictMatchingIsOff() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            JsonMatcher<Object> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwriteNonStrict()).ignoring("beanByte", "beanInteger");

            String contentWithIgnoredFieldsInIt = "/*JsonMatcherOverwriteTest.shouldLeaveApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExistsButContainingIgnoredFieldsAndStrictUpdateIsOff*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanByte\": 2,\n" +
                    "  \"beanInteger\": 4,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}";
            writeFile(imfsi.getTestPath().resolve("11ee79").resolve("24db15-approved.json"), contentWithIgnoredFieldsInIt);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79/24db15-approved.json", contentWithIgnoredFieldsInIt);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExistsButContainingIgnoredFieldsAndStrictMatchingIsOn() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherOverwriteTest", "shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists");
            JsonMatcher<Object> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwrite()).ignoring("beanByte", "beanInteger");

            String contentWithIgnoredFieldsInIt = "/*JsonMatcherOverwriteTest.shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanByte\": 2,\n" +
                    "  \"beanInteger\": 4,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}";
            writeFile(imfsi.getTestPath().resolve("11ee79").resolve("24db15-approved.json"), contentWithIgnoredFieldsInIt);

            MatcherAssert.assertThat(actual, underTest);


            String contentIgnoredFieldsRemoved = "/*JsonMatcherOverwriteTest.shouldOverwriteApprovedFileWhenOverwriteInPlaceEnabledAndApprovedFileExists*/\n" +
                    "{\n" +
                    "  \"beanBoolean\": true,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanShort\": 1\n" +
                    "}";
            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("11ee79/24db15-approved.json", contentIgnoredFieldsRemoved);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }
}
