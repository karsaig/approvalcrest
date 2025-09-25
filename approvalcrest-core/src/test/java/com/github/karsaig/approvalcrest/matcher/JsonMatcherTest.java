package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.StringUtil;
import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.testdata.BeanWithGenericIterable;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.github.karsaig.approvalcrest.util.InMemoryPermissions;
import com.google.common.collect.Sets;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.*;
import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generatePerson;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for the {@link JsonMatcher}.
 * Verifies creation of not approved files.
 *
 * @author Andras_Gyuro
 */
public class JsonMatcherTest extends AbstractFileMatcherTest {

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExists() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExists");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "183d71-not-approved.json", "183d71-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/183d71-not-approved.json", "/*JsonMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExists*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void testRunShouldCreateNotApprovedFileWithCorrectPermissionsWhenNotExists() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        InMemoryFsUtil.inMemoryUnixFsWithFileAttributeSupport(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "testRunShouldCreateNotApprovedFileWithCorrectPermissionsWhenNotExists");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());


            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "4b34e9-not-approved.json", "4b34e9-approved.json"), actualError.getMessage());

            List<InMemoryPermissions> actualFiles = InMemoryFsUtil.getPermissons(imfsi);
            List<InMemoryPermissions> expected = new ArrayList<>();
            expected.add(new InMemoryPermissions("8c5498", DIRECTORY_CREATE_PERMISSONS));
            expected.add(new InMemoryPermissions("8c5498/4b34e9-not-approved.json", FILE_CREATE_PERMISSONS));
            assertIterableEquals(expected, actualFiles);
        });
    }

    @Test
    public void shouldOverwriteNotApprovedFileWithCorrectPermissionsWhenPassOnCreateIsEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        InMemoryFsUtil.inMemoryUnixFsWithFileAttributeSupport(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "shouldOverwriteNotApprovedFileWithCorrectPermissionsWhenPassOnCreateIsEnabled");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enablePassOnCreate());

            writeFile(imfsi.getTestPath().resolve("8c5498").resolve("f587c6-not-approved.json"), "dummyContent");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryPermissions> actualFiles = InMemoryFsUtil.getPermissons(imfsi);
            List<InMemoryPermissions> expected = new ArrayList<>();
            expected.add(new InMemoryPermissions("8c5498", DEFAULT_JIMFS_PERMISSIONS));
            expected.add(new InMemoryPermissions("8c5498/f587c6-not-approved.json", FILE_CREATE_PERMISSONS));
            assertIterableEquals(expected, actualFiles);
        });
    }

    @Test
    public void testRunShouldCreateNotApprovedFileAndPassWhenNotExistsAnsPassOnCreateEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExists");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enablePassOnCreate());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/183d71-not-approved.json", "/*JsonMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExists*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteApprovedFileWhenDiffersFromActualAndBothInPlaceOverwriteAndContinueOnCreateIsEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "shouldOverwriteApprovedFileWhenDiffersFromActualAndBothInPlaceOverwriteAndContinueOnCreateIsEnabled");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwriteAndPassOnCreate());

            writeFile(imfsi.getTestPath().resolve("8c5498").resolve("0821e3-approved.json"), "{ dummyProperty: dummyContent }");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/0821e3-approved.json", "/*JsonMatcherTest.shouldOverwriteApprovedFileWhenDiffersFromActualAndBothInPlaceOverwriteAndContinueOnCreateIsEnabled*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldFailWhenApprovedFileDiffersAndPassOnCreateIsEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "shouldFailWhenApprovedFileDiffersAndPassOnCreateIsEnabled");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enablePassOnCreate());

            writeFile(imfsi.getTestPath().resolve("8c5498").resolve("247f32-approved.json"), "{ dummyProperty: dummyContent }");

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));


            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"dummyProperty\": \"dummyContent\"\n" +
                    "}\n" +
                    "     but: Expected file 8c5498/247f32-approved.json\n" +
                    "\n" +
                    "Expected: dummyProperty\n" +
                    "     but none found\n" +
                    " ; \n" +
                    "Unexpected: beanBoolean\n" +
                    " ; \n" +
                    "Unexpected: beanByte\n" +
                    " ; \n" +
                    "Unexpected: beanChar\n" +
                    " ; \n" +
                    "Unexpected: beanDouble\n" +
                    " ; \n" +
                    "Unexpected: beanFloat\n" +
                    " ; \n" +
                    "Unexpected: beanInteger\n" +
                    " ; \n" +
                    "Unexpected: beanLong\n" +
                    " ; \n" +
                    "Unexpected: beanShort\n", StringUtil.normalizeNewLines(actualError.getMessage()));


            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/247f32-approved.json", "{ dummyProperty: dummyContent }");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteNotApprovedFileWhenPassOnCreateIsEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "shouldOverwriteNotApprovedFileWhenPassOnCreateIsEnabled");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enablePassOnCreate());

            writeFile(imfsi.getTestPath().resolve("8c5498").resolve("901d38-not-approved.json"), "dummyContent");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/901d38-not-approved.json", "/*JsonMatcherTest.shouldOverwriteNotApprovedFileWhenPassOnCreateIsEnabled*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldOverwriteNotApprovedFileWhenBothPassOnCreateAndInPlaceOverwriteAreEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "shouldOverwriteNotApprovedFileWhenBothPassOnCreateAndInPlaceOverwriteAreEnabled");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, enableInPlaceOverwriteAndPassOnCreate());

            writeFile(imfsi.getTestPath().resolve("8c5498").resolve("df20a0-not-approved.json"), "dummyContent");

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/df20a0-not-approved.json", "/*JsonMatcherTest.shouldOverwriteNotApprovedFileWhenBothPassOnCreateAndInPlaceOverwriteAreEnabled*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString() {
        String actual = getBeanAsJsonString();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString");
            JsonMatcher<String> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "675159-not-approved.json", "675159-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/675159-not-approved.json", "/*JsonMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString*/\n" +
                    "{\n" +
                    "  \"beanInt\": 10,\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"dummyString\"\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryWindowsFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "e6fb22-not-approved.json", "e6fb22-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f\\e6fb22-not-approved.json", "/*ContentMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApproved() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApproved");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("39c4dd-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/39c4dd-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentDiffersFromApprovedContent() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String approvedFileContent = "{ beanLong: 5, beanString: \"Modified content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentDiffersFromApprovedContent");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("0d08c2-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanInt\": 10,\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Modified content\"\n" +
                    "}\n" +
                    "     but: Expected file 87668f/0d08c2-approved.json\n" +
                    "\n" +
                    "Expected: beanInt\n" +
                    "     but none found\n" +
                    " ; beanLong\n" +
                    "Expected: 5\n" +
                    "     got: 6\n" +
                    " ; \n" +
                    "Expected: beanString\n" +
                    "     but none found\n" +
                    " ; \n" +
                    "Unexpected: beanBoolean\n" +
                    " ; \n" +
                    "Unexpected: beanByte\n" +
                    " ; \n" +
                    "Unexpected: beanChar\n" +
                    " ; \n" +
                    "Unexpected: beanDouble\n" +
                    " ; \n" +
                    "Unexpected: beanFloat\n" +
                    " ; \n" +
                    "Unexpected: beanInteger\n" +
                    " ; \n" +
                    "Unexpected: beanShort\n", StringUtil.normalizeNewLines(actualError.getMessage()));

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/0d08c2-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId("idTest");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("3f0945-idTest-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/3f0945-idTest-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueIdAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String approvedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueIdAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId("idTest");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("39e1a0-idTest-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanInt\": 10,\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\"\n" +
                    "}\n" +
                    "     but: Expected file 87668f/39e1a0-idTest-approved.json\n" +
                    "\n" +
                    "Expected: beanInt\n" +
                    "     but none found\n" +
                    " ; beanLong\n" +
                    "Expected: 5\n" +
                    "     got: 6\n" +
                    " ; \n" +
                    "Expected: beanString\n" +
                    "     but none found\n" +
                    " ; \n" +
                    "Unexpected: beanBoolean\n" +
                    " ; \n" +
                    "Unexpected: beanByte\n" +
                    " ; \n" +
                    "Unexpected: beanChar\n" +
                    " ; \n" +
                    "Unexpected: beanDouble\n" +
                    " ; \n" +
                    "Unexpected: beanFloat\n" +
                    " ; \n" +
                    "Unexpected: beanInteger\n" +
                    " ; \n" +
                    "Unexpected: beanShort\n", StringUtil.normalizeNewLines(actualError.getMessage()));

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/39e1a0-idTest-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withFileName("single-line");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("single-line-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/single-line-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String approvedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withFileName("single-line");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("single-line-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanInt\": 10,\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\"\n" +
                    "}\n" +
                    "     but: Expected file 87668f/single-line-approved.json\n" +
                    "\n" +
                    "Expected: beanInt\n" +
                    "     but none found\n" +
                    " ; beanLong\n" +
                    "Expected: 5\n" +
                    "     got: 6\n" +
                    " ; \n" +
                    "Expected: beanString\n" +
                    "     but none found\n" +
                    " ; \n" +
                    "Unexpected: beanBoolean\n" +
                    " ; \n" +
                    "Unexpected: beanByte\n" +
                    " ; \n" +
                    "Unexpected: beanChar\n" +
                    " ; \n" +
                    "Unexpected: beanDouble\n" +
                    " ; \n" +
                    "Unexpected: beanFloat\n" +
                    " ; \n" +
                    "Unexpected: beanInteger\n" +
                    " ; \n" +
                    "Unexpected: beanShort\n", StringUtil.normalizeNewLines(actualError.getMessage()));

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/single-line-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().resolve("src/test/contents").resolve("single-line-2-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("src/test/contents/single-line-2-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String approvedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().resolve("src/test/contents").resolve("single-line-2-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            String basePath = imfsi.getTestPath().toString();
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanInt\": 10,\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\"\n" +
                    "}\n" +
                    "     but: Expected file " + basePath + "/src/test/contents/single-line-2\n" +
                    "\n" +
                    "Expected: beanInt\n" +
                    "     but none found\n" +
                    " ; beanLong\n" +
                    "Expected: 5\n" +
                    "     got: 6\n" +
                    " ; \n" +
                    "Expected: beanString\n" +
                    "     but none found\n" +
                    " ; \n" +
                    "Unexpected: beanBoolean\n" +
                    " ; \n" +
                    "Unexpected: beanByte\n" +
                    " ; \n" +
                    "Unexpected: beanChar\n" +
                    " ; \n" +
                    "Unexpected: beanDouble\n" +
                    " ; \n" +
                    "Unexpected: beanFloat\n" +
                    " ; \n" +
                    "Unexpected: beanInteger\n" +
                    " ; \n" +
                    "Unexpected: beanShort\n", StringUtil.normalizeNewLines(actualError.getMessage()));

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("src/test/contents/single-line-2-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathName");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("/src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().resolve("/src/test/contents").resolve("single-line-2-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("/src/test/contents/single-line-2-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathNameAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String approvedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("/src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().resolve("/src/test/contents").resolve("single-line-2-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanInt\": 10,\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\"\n" +
                    "}\n" +
                    "     but: Expected file /src/test/contents/single-line-2\n" +
                    "\n" +
                    "Expected: beanInt\n" +
                    "     but none found\n" +
                    " ; beanLong\n" +
                    "Expected: 5\n" +
                    "     got: 6\n" +
                    " ; \n" +
                    "Expected: beanString\n" +
                    "     but none found\n" +
                    " ; \n" +
                    "Unexpected: beanBoolean\n" +
                    " ; \n" +
                    "Unexpected: beanByte\n" +
                    " ; \n" +
                    "Unexpected: beanChar\n" +
                    " ; \n" +
                    "Unexpected: beanDouble\n" +
                    " ; \n" +
                    "Unexpected: beanFloat\n" +
                    " ; \n" +
                    "Unexpected: beanInteger\n" +
                    " ; \n" +
                    "Unexpected: beanShort\n", StringUtil.normalizeNewLines(actualError.getMessage()));

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("/src/test/contents/single-line-2-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenJsonWithWindowsNewLineExpectedContentSameContentAsApproved() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String approvedFileContent = "{\r\n" +
                "  \"beanInteger\": 4,\r\n" +
                "  \"beanByte\": 2,\r\n" +
                "  \"beanChar\": \"c\",\r\n" +
                "  \"beanShort\": 1,\r\n" +
                "  \"beanLong\": 6,\r\n" +
                "  \"beanFloat\": 3.0,\r\n" +
                "  \"beanDouble\": 5.0,\r\n" +
                "  \"beanBoolean\": true\r\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenJsonWithWindowsNewLineExpectedContentSameContentAsApproved");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("78a94f-approved.json"), approvedFileContent);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/78a94f-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void contentMatcherWorkflowVerifierTest() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "contentMatcherWorkflowVerifierTest");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "e4bc67-not-approved.json", "e4bc67-approved.json"), actualError.getMessage());

            approveFile(imfsi.getTestPath().resolve("87668f").resolve("e4bc67-not-approved.json"));

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/e4bc67-approved.json", "/*ContentMatcherTest.contentMatcherWorkflowVerifierTest*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void contentMatcherWorkflowWithNonAsciiCharacters() {
        BeanWithGeneric<String> actual = BeanWithGeneric.of("dummyValue", "Árvízűtűrőtükörfúrógép\\nL’apostrophe 用的名字☺\\nд1@00000☺☹❤\\naA@AA1A猫很可爱");
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "contentMatcherWorkflowWithNonAsciiCharacters");
            JsonMatcher<BeanWithGeneric<String>> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "c5f725-not-approved.json", "c5f725-approved.json"), actualError.getMessage());

            approveFile(imfsi.getTestPath().resolve("87668f").resolve("c5f725-not-approved.json"));

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/c5f725-approved.json", "/*ContentMatcherTest.contentMatcherWorkflowWithNonAsciiCharacters*/\n" +
                    "{\n" +
                    "  \"dummyString\": \"dummyValue\",\n" +
                    "  \"genericValue\": \"Árvízűtűrőtükörfúrógép\\\\nL’apostrophe 用的名字☺\\\\nд1@00000☺☹❤\\\\naA@AA1A猫很可爱\"\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void runningFromCommandLineFailedWithCharacterProblemsWithThis() {
        BeanWithGeneric<List<String>> actual = BeanWithGeneric.of("dummyValue", Collections.singletonList("5 三月 1984 07:15:17"));
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "runningFromCommandLineFailedWithCharacterProblemsWithThis");
            JsonMatcher<BeanWithGeneric<List<String>>> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "dc100a-not-approved.json", "dc100a-approved.json"), actualError.getMessage());

            approveFile(imfsi.getTestPath().resolve("87668f").resolve("dc100a-not-approved.json"));

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/dc100a-approved.json", "/*ContentMatcherTest.runningFromCommandLineFailedWithCharacterProblemsWithThis*/\n" +
                    "{\n" +
                    "  \"dummyString\": \"dummyValue\",\n" +
                    "  \"genericValue\": [\n" +
                    "    \"5 三月 1984 07:15:17\"\n" +
                    "  ]\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    public static Object[][] expectedFileSortingTestcases() {
        return new Object[][]{
                {"Object input", BeanWithGenericIterable.Builder.bean().dummyString("String1")
                        .set(Sets.newHashSet(generatePerson(1L), generatePerson(2L), generatePerson(3L)))
                        .hashMap(new HashMap<Object, Object>() {{
                            put("p1", generatePerson(4L));
                            put("p2", generatePerson(5L));
                        }})
                        .array(new Object[]{generatePerson(6L), generatePerson(7L), generatePerson(8L)})
                        .build()},
                {"Json string input", "{\n" +
                        "  \"array\": [\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"HUNGARY\",\n" +
                        "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName6\",\n" +
                        "        \"country\": \"HUNGARY\",\n" +
                        "        \"postCode\": \"PostCode69\",\n" +
                        "        \"since\": \"2017-04-07\",\n" +
                        "        \"streetName\": \"StreetName65\",\n" +
                        "        \"streetNumber\": 48\n" +
                        "      },\n" +
                        "      \"email\": \"e6@e.mail\",\n" +
                        "      \"firstName\": \"FirstName6\",\n" +
                        "      \"lastName\": \"LastName6\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName16\",\n" +
                        "          \"country\": \"CANADA\",\n" +
                        "          \"postCode\": \"PostCode79\",\n" +
                        "          \"since\": \"2017-04-17\",\n" +
                        "          \"streetName\": \"StreetName75\",\n" +
                        "          \"streetNumber\": 58\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"AUSTRIA\",\n" +
                        "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName7\",\n" +
                        "        \"country\": \"AUSTRIA\",\n" +
                        "        \"postCode\": \"PostCode70\",\n" +
                        "        \"since\": \"2017-04-08\",\n" +
                        "        \"streetName\": \"StreetName66\",\n" +
                        "        \"streetNumber\": 49\n" +
                        "      },\n" +
                        "      \"email\": \"e7@e.mail\",\n" +
                        "      \"firstName\": \"FirstName7\",\n" +
                        "      \"lastName\": \"LastName7\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName17\",\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"postCode\": \"PostCode80\",\n" +
                        "          \"since\": \"2017-04-18\",\n" +
                        "          \"streetName\": \"StreetName76\",\n" +
                        "          \"streetNumber\": 59\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName18\",\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"postCode\": \"PostCode81\",\n" +
                        "          \"since\": \"2017-04-19\",\n" +
                        "          \"streetName\": \"StreetName77\",\n" +
                        "          \"streetNumber\": 60\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"BELGIUM\",\n" +
                        "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName8\",\n" +
                        "        \"country\": \"BELGIUM\",\n" +
                        "        \"postCode\": \"PostCode71\",\n" +
                        "        \"since\": \"2017-04-09\",\n" +
                        "        \"streetName\": \"StreetName67\",\n" +
                        "        \"streetNumber\": 50\n" +
                        "      },\n" +
                        "      \"email\": \"e8@e.mail\",\n" +
                        "      \"firstName\": \"FirstName8\",\n" +
                        "      \"lastName\": \"LastName8\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName18\",\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"postCode\": \"PostCode81\",\n" +
                        "          \"since\": \"2017-04-19\",\n" +
                        "          \"streetName\": \"StreetName77\",\n" +
                        "          \"streetNumber\": 60\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName19\",\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"postCode\": \"PostCode82\",\n" +
                        "          \"since\": \"2017-04-20\",\n" +
                        "          \"streetName\": \"StreetName78\",\n" +
                        "          \"streetNumber\": 61\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName20\",\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"postCode\": \"PostCode83\",\n" +
                        "          \"since\": \"2017-04-21\",\n" +
                        "          \"streetName\": \"StreetName79\",\n" +
                        "          \"streetNumber\": 62\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"dummyString\": \"String1\",\n" +
                        "  \"hashMap\": [\n" +
                        "    {\n" +
                        "      \"p1\": {\n" +
                        "        \"birthCountry\": \"EGYPT\",\n" +
                        "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"city\": \"CityName4\",\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"postCode\": \"PostCode67\",\n" +
                        "          \"since\": \"2017-04-05\",\n" +
                        "          \"streetName\": \"StreetName63\",\n" +
                        "          \"streetNumber\": 46\n" +
                        "        },\n" +
                        "        \"email\": \"e4@e.mail\",\n" +
                        "        \"firstName\": \"FirstName4\",\n" +
                        "        \"lastName\": \"LastName4\",\n" +
                        "        \"previousAddresses\": [\n" +
                        "          {\n" +
                        "            \"city\": \"CityName14\",\n" +
                        "            \"country\": \"AUSTRIA\",\n" +
                        "            \"postCode\": \"PostCode77\",\n" +
                        "            \"since\": \"2017-04-15\",\n" +
                        "            \"streetName\": \"StreetName73\",\n" +
                        "            \"streetNumber\": 56\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"city\": \"CityName15\",\n" +
                        "            \"country\": \"BELGIUM\",\n" +
                        "            \"postCode\": \"PostCode78\",\n" +
                        "            \"since\": \"2017-04-16\",\n" +
                        "            \"streetName\": \"StreetName74\",\n" +
                        "            \"streetNumber\": 57\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"city\": \"CityName16\",\n" +
                        "            \"country\": \"CANADA\",\n" +
                        "            \"postCode\": \"PostCode79\",\n" +
                        "            \"since\": \"2017-04-17\",\n" +
                        "            \"streetName\": \"StreetName75\",\n" +
                        "            \"streetNumber\": 58\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"city\": \"CityName17\",\n" +
                        "            \"country\": \"DENMARK\",\n" +
                        "            \"postCode\": \"PostCode80\",\n" +
                        "            \"since\": \"2017-04-18\",\n" +
                        "            \"streetName\": \"StreetName76\",\n" +
                        "            \"streetNumber\": 59\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"p2\": {\n" +
                        "        \"birthCountry\": \"FRANCE\",\n" +
                        "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"city\": \"CityName5\",\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"postCode\": \"PostCode68\",\n" +
                        "          \"since\": \"2017-04-06\",\n" +
                        "          \"streetName\": \"StreetName64\",\n" +
                        "          \"streetNumber\": 47\n" +
                        "        },\n" +
                        "        \"email\": \"e5@e.mail\",\n" +
                        "        \"firstName\": \"FirstName5\",\n" +
                        "        \"lastName\": \"LastName5\",\n" +
                        "        \"previousAddresses\": []\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"set\": [\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"BELGIUM\",\n" +
                        "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName1\",\n" +
                        "        \"country\": \"BELGIUM\",\n" +
                        "        \"postCode\": \"PostCode64\",\n" +
                        "        \"since\": \"2017-04-02\",\n" +
                        "        \"streetName\": \"StreetName60\",\n" +
                        "        \"streetNumber\": 43\n" +
                        "      },\n" +
                        "      \"email\": \"e1@e.mail\",\n" +
                        "      \"firstName\": \"FirstName1\",\n" +
                        "      \"lastName\": \"LastName1\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName11\",\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"postCode\": \"PostCode74\",\n" +
                        "          \"since\": \"2017-04-12\",\n" +
                        "          \"streetName\": \"StreetName70\",\n" +
                        "          \"streetNumber\": 53\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"CANADA\",\n" +
                        "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName2\",\n" +
                        "        \"country\": \"CANADA\",\n" +
                        "        \"postCode\": \"PostCode65\",\n" +
                        "        \"since\": \"2017-04-03\",\n" +
                        "        \"streetName\": \"StreetName61\",\n" +
                        "        \"streetNumber\": 44\n" +
                        "      },\n" +
                        "      \"email\": \"e2@e.mail\",\n" +
                        "      \"firstName\": \"FirstName2\",\n" +
                        "      \"lastName\": \"LastName2\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName12\",\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"postCode\": \"PostCode75\",\n" +
                        "          \"since\": \"2017-04-13\",\n" +
                        "          \"streetName\": \"StreetName71\",\n" +
                        "          \"streetNumber\": 54\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName13\",\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"postCode\": \"PostCode76\",\n" +
                        "          \"since\": \"2017-04-14\",\n" +
                        "          \"streetName\": \"StreetName72\",\n" +
                        "          \"streetNumber\": 55\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"DENMARK\",\n" +
                        "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName3\",\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"postCode\": \"PostCode66\",\n" +
                        "        \"since\": \"2017-04-04\",\n" +
                        "        \"streetName\": \"StreetName62\",\n" +
                        "        \"streetNumber\": 45\n" +
                        "      },\n" +
                        "      \"email\": \"e3@e.mail\",\n" +
                        "      \"firstName\": \"FirstName3\",\n" +
                        "      \"lastName\": \"LastName3\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName13\",\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"postCode\": \"PostCode76\",\n" +
                        "          \"since\": \"2017-04-14\",\n" +
                        "          \"streetName\": \"StreetName72\",\n" +
                        "          \"streetNumber\": 55\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName14\",\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"postCode\": \"PostCode77\",\n" +
                        "          \"since\": \"2017-04-15\",\n" +
                        "          \"streetName\": \"StreetName73\",\n" +
                        "          \"streetNumber\": 56\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName15\",\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"postCode\": \"PostCode78\",\n" +
                        "          \"since\": \"2017-04-16\",\n" +
                        "          \"streetName\": \"StreetName74\",\n" +
                        "          \"streetNumber\": 57\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitSortingByPathOfExpectedFile(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"array\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"AUSTRIA\",\n" +
                "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName7\",\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"postCode\": \"PostCode70\",\n" +
                "        \"since\": \"2017-04-08\",\n" +
                "        \"streetName\": \"StreetName66\",\n" +
                "        \"streetNumber\": 49\n" +
                "      },\n" +
                "      \"email\": \"e7@e.mail\",\n" +
                "      \"firstName\": \"FirstName7\",\n" +
                "      \"lastName\": \"LastName7\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName17\",\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"postCode\": \"PostCode80\",\n" +
                "          \"since\": \"2017-04-18\",\n" +
                "          \"streetName\": \"StreetName76\",\n" +
                "          \"streetNumber\": 59\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"streetNumber\": 60\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"HUNGARY\",\n" +
                "      \"lastName\": \"LastName6\",\n" +
                "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName6\",\n" +
                "        \"since\": \"2017-04-07\",\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"postCode\": \"PostCode69\",\n" +
                "        \"streetName\": \"StreetName65\",\n" +
                "        \"streetNumber\": 48\n" +
                "      },\n" +
                "      \"email\": \"e6@e.mail\",\n" +
                "      \"firstName\": \"FirstName6\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName16\",\n" +
                "          \"postCode\": \"PostCode79\",\n" +
                "          \"since\": \"2017-04-17\",\n" +
                "          \"streetName\": \"StreetName75\",\n" +
                "          \"streetNumber\": 58\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                "      \"firstName\": \"FirstName8\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName8\",\n" +
                "        \"postCode\": \"PostCode71\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"since\": \"2017-04-09\",\n" +
                "        \"streetName\": \"StreetName67\",\n" +
                "        \"streetNumber\": 50\n" +
                "      },\n" +
                "      \"email\": \"e8@e.mail\",\n" +
                "      \"lastName\": \"LastName8\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName19\",\n" +
                "          \"since\": \"2017-04-20\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode82\",\n" +
                "          \"streetName\": \"StreetName78\",\n" +
                "          \"streetNumber\": 61\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetNumber\": 60\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName20\",\n" +
                "          \"postCode\": \"PostCode83\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-21\",\n" +
                "          \"streetName\": \"StreetName79\",\n" +
                "          \"streetNumber\": 62\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"hashMap\": [\n" +
                "    {\n" +
                "      \"p2\": {\n" +
                "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                "        \"birthCountry\": \"FRANCE\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName5\",\n" +
                "          \"streetName\": \"StreetName64\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode68\",\n" +
                "          \"since\": \"2017-04-06\",\n" +
                "          \"streetNumber\": 47\n" +
                "        },\n" +
                "        \"firstName\": \"FirstName5\",\n" +
                "        \"email\": \"e5@e.mail\",\n" +
                "        \"lastName\": \"LastName5\",\n" +
                "        \"previousAddresses\": []\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"p1\": {\n" +
                "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                "        \"birthCountry\": \"EGYPT\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName4\",\n" +
                "          \"since\": \"2017-04-05\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode67\",\n" +
                "          \"streetName\": \"StreetName63\",\n" +
                "          \"streetNumber\": 46\n" +
                "        },\n" +
                "        \"email\": \"e4@e.mail\",\n" +
                "        \"lastName\": \"LastName4\",\n" +
                "        \"firstName\": \"FirstName4\",\n" +
                "        \"previousAddresses\": [\n" +
                "          {\n" +
                "            \"city\": \"CityName14\",\n" +
                "            \"postCode\": \"PostCode77\",\n" +
                "            \"country\": \"AUSTRIA\",\n" +
                "            \"since\": \"2017-04-15\",\n" +
                "            \"streetName\": \"StreetName73\",\n" +
                "            \"streetNumber\": 56\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName16\",\n" +
                "            \"since\": \"2017-04-17\",\n" +
                "            \"country\": \"CANADA\",\n" +
                "            \"postCode\": \"PostCode79\",\n" +
                "            \"streetName\": \"StreetName75\",\n" +
                "            \"streetNumber\": 58\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName15\",\n" +
                "            \"country\": \"BELGIUM\",\n" +
                "            \"streetName\": \"StreetName74\",\n" +
                "            \"postCode\": \"PostCode78\",\n" +
                "            \"since\": \"2017-04-16\",\n" +
                "            \"streetNumber\": 57\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName17\",\n" +
                "            \"country\": \"DENMARK\",\n" +
                "            \"postCode\": \"PostCode80\",\n" +
                "            \"streetName\": \"StreetName76\",\n" +
                "            \"since\": \"2017-04-18\",\n" +
                "            \"streetNumber\": 59\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\",\n" +
                "        \"streetNumber\": 44\n" +
                "      },\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"since\": \"2017-04-13\",\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"since\": \"2017-04-02\",\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43\n" +
                "      },\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"since\": \"2017-04-12\",\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\",\n" +
                "        \"streetNumber\": 45\n" +
                "      },\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\",\n" +
                "          \"streetNumber\": 56\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"since\": \"2017-04-16\",\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(), jsonMatcher -> jsonMatcher.sortField("array", "array.previousAddresses", "set.previousAddresses", "hashMap.p1.previousAddresses", "hashMap", "set"), null, null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.sortField("array", "array.previousAddresses", "set.previousAddresses", "hashMap.p1.previousAddresses", "hashMap", "set"), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("array[1].birthCountry\n" +
                    "Expected: HUNGARY\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].birthDate\n" +
                    "Expected: 2011-04-01T13:42:11\n" +
                    "     got: 2009-04-01T13:42:11\n" +
                    " ; array[1].currentAddress.city\n" +
                    "Expected: CityName6\n" +
                    "     got: CityName8\n" +
                    " ; array[1].currentAddress.country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].currentAddress.postCode\n" +
                    "Expected: PostCode69\n" +
                    "     got: PostCode71\n" +
                    " ; array[1].currentAddress.since\n" +
                    "Expected: 2017-04-07\n" +
                    "     got: 2017-04-09\n" +
                    " ; array[1].currentAddress.streetName\n" +
                    "Expected: StreetName65\n" +
                    "     got: StreetName67\n" +
                    " ; array[1].currentAddress.streetNumber\n" +
                    "Expected: 48\n" +
                    "     got: 50\n" +
                    " ; array[1].email\n" +
                    "Expected: e6@e.mail\n" +
                    "     got: e8@e.mail\n" +
                    " ; array[1].firstName\n" +
                    "Expected: FirstName6\n" +
                    "     got: FirstName8\n" +
                    " ; array[1].lastName\n" +
                    "Expected: LastName6\n" +
                    "     got: LastName8\n" +
                    " ; array[1].previousAddresses[]: Expected 1 values but got 3 ; array[2].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].birthDate\n" +
                    "Expected: 2009-04-01T13:42:11\n" +
                    "     got: 2011-04-01T13:42:11\n" +
                    " ; array[2].currentAddress.city\n" +
                    "Expected: CityName8\n" +
                    "     got: CityName6\n" +
                    " ; array[2].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].currentAddress.postCode\n" +
                    "Expected: PostCode71\n" +
                    "     got: PostCode69\n" +
                    " ; array[2].currentAddress.since\n" +
                    "Expected: 2017-04-09\n" +
                    "     got: 2017-04-07\n" +
                    " ; array[2].currentAddress.streetName\n" +
                    "Expected: StreetName67\n" +
                    "     got: StreetName65\n" +
                    " ; array[2].currentAddress.streetNumber\n" +
                    "Expected: 50\n" +
                    "     got: 48\n" +
                    " ; array[2].email\n" +
                    "Expected: e8@e.mail\n" +
                    "     got: e6@e.mail\n" +
                    " ; array[2].firstName\n" +
                    "Expected: FirstName8\n" +
                    "     got: FirstName6\n" +
                    " ; array[2].lastName\n" +
                    "Expected: LastName8\n" +
                    "     got: LastName6\n" +
                    " ; array[2].previousAddresses[]: Expected 3 values but got 1 ; hashMap[0]\n" +
                    "Expected: p2\n" +
                    "     but none found\n" +
                    " ; hashMap[0]\n" +
                    "Unexpected: p1\n" +
                    " ; hashMap[1]\n" +
                    "Expected: p1\n" +
                    "     but none found\n" +
                    " ; hashMap[1]\n" +
                    "Unexpected: p2\n" +
                    " ; set[0].birthCountry\n" +
                    "Expected: CANADA\n" +
                    "     got: BELGIUM\n" +
                    " ; set[0].birthDate\n" +
                    "Expected: 2015-04-01T13:42:11\n" +
                    "     got: 2016-04-01T13:42:11\n" +
                    " ; set[0].currentAddress.city\n" +
                    "Expected: CityName2\n" +
                    "     got: CityName1\n" +
                    " ; set[0].currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: BELGIUM\n" +
                    " ; set[0].currentAddress.postCode\n" +
                    "Expected: PostCode65\n" +
                    "     got: PostCode64\n" +
                    " ; set[0].currentAddress.since\n" +
                    "Expected: 2017-04-03\n" +
                    "     got: 2017-04-02\n" +
                    " ; set[0].currentAddress.streetName\n" +
                    "Expected: StreetName61\n" +
                    "     got: StreetName60\n" +
                    " ; set[0].currentAddress.streetNumber\n" +
                    "Expected: 44\n" +
                    "     got: 43\n" +
                    " ; set[0].email\n" +
                    "Expected: e2@e.mail\n" +
                    "     got: e1@e.mail\n" +
                    " ; set[0].firstName\n" +
                    "Expected: FirstName2\n" +
                    "     got: FirstName1\n" +
                    " ; set[0].lastName\n" +
                    "Expected: LastName2\n" +
                    "     got: LastName1\n" +
                    " ; set[0].previousAddresses[]: Expected 2 values but got 1 ; set[1].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: CANADA\n" +
                    " ; set[1].birthDate\n" +
                    "Expected: 2016-04-01T13:42:11\n" +
                    "     got: 2015-04-01T13:42:11\n" +
                    " ; set[1].currentAddress.city\n" +
                    "Expected: CityName1\n" +
                    "     got: CityName2\n" +
                    " ; set[1].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: CANADA\n" +
                    " ; set[1].currentAddress.postCode\n" +
                    "Expected: PostCode64\n" +
                    "     got: PostCode65\n" +
                    " ; set[1].currentAddress.since\n" +
                    "Expected: 2017-04-02\n" +
                    "     got: 2017-04-03\n" +
                    " ; set[1].currentAddress.streetName\n" +
                    "Expected: StreetName60\n" +
                    "     got: StreetName61\n" +
                    " ; set[1].currentAddress.streetNumber\n" +
                    "Expected: 43\n" +
                    "     got: 44\n" +
                    " ; set[1].email\n" +
                    "Expected: e1@e.mail\n" +
                    "     got: e2@e.mail\n" +
                    " ; set[1].firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: FirstName2\n" +
                    " ; set[1].lastName\n" +
                    "Expected: LastName1\n" +
                    "     got: LastName2\n" +
                    " ; set[1].previousAddresses[]: Expected 1 values but got 2 ; set[2].previousAddresses[0].city\n" +
                    "Expected: CityName14\n" +
                    "     got: CityName13\n" +
                    " ; set[2].previousAddresses[0].country\n" +
                    "Expected: AUSTRIA\n" +
                    "     got: HUNGARY\n" +
                    " ; set[2].previousAddresses[0].postCode\n" +
                    "Expected: PostCode77\n" +
                    "     got: PostCode76\n" +
                    " ; set[2].previousAddresses[0].since\n" +
                    "Expected: 2017-04-15\n" +
                    "     got: 2017-04-14\n" +
                    " ; set[2].previousAddresses[0].streetName\n" +
                    "Expected: StreetName73\n" +
                    "     got: StreetName72\n" +
                    " ; set[2].previousAddresses[0].streetNumber\n" +
                    "Expected: 56\n" +
                    "     got: 55\n" +
                    " ; set[2].previousAddresses[1].city\n" +
                    "Expected: CityName13\n" +
                    "     got: CityName14\n" +
                    " ; set[2].previousAddresses[1].country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: AUSTRIA\n" +
                    " ; set[2].previousAddresses[1].postCode\n" +
                    "Expected: PostCode76\n" +
                    "     got: PostCode77\n" +
                    " ; set[2].previousAddresses[1].since\n" +
                    "Expected: 2017-04-14\n" +
                    "     got: 2017-04-15\n" +
                    " ; set[2].previousAddresses[1].streetName\n" +
                    "Expected: StreetName72\n" +
                    "     got: StreetName73\n" +
                    " ; set[2].previousAddresses[1].streetNumber\n" +
                    "Expected: 55\n" +
                    "     got: 56\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "no explicit sorting applied");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "no explicit sorting applied");
        }, AssertionFailedError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitSortingByMatcherOfExpectedFile(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"array\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"AUSTRIA\",\n" +
                "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName7\",\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"postCode\": \"PostCode70\",\n" +
                "        \"since\": \"2017-04-08\",\n" +
                "        \"streetName\": \"StreetName66\",\n" +
                "        \"streetNumber\": 49\n" +
                "      },\n" +
                "      \"email\": \"e7@e.mail\",\n" +
                "      \"firstName\": \"FirstName7\",\n" +
                "      \"lastName\": \"LastName7\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName17\",\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"postCode\": \"PostCode80\",\n" +
                "          \"since\": \"2017-04-18\",\n" +
                "          \"streetName\": \"StreetName76\",\n" +
                "          \"streetNumber\": 59\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"streetNumber\": 60\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"HUNGARY\",\n" +
                "      \"lastName\": \"LastName6\",\n" +
                "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName6\",\n" +
                "        \"since\": \"2017-04-07\",\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"postCode\": \"PostCode69\",\n" +
                "        \"streetName\": \"StreetName65\",\n" +
                "        \"streetNumber\": 48\n" +
                "      },\n" +
                "      \"email\": \"e6@e.mail\",\n" +
                "      \"firstName\": \"FirstName6\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName16\",\n" +
                "          \"postCode\": \"PostCode79\",\n" +
                "          \"since\": \"2017-04-17\",\n" +
                "          \"streetName\": \"StreetName75\",\n" +
                "          \"streetNumber\": 58\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                "      \"firstName\": \"FirstName8\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName8\",\n" +
                "        \"postCode\": \"PostCode71\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"since\": \"2017-04-09\",\n" +
                "        \"streetName\": \"StreetName67\",\n" +
                "        \"streetNumber\": 50\n" +
                "      },\n" +
                "      \"email\": \"e8@e.mail\",\n" +
                "      \"lastName\": \"LastName8\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName19\",\n" +
                "          \"since\": \"2017-04-20\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode82\",\n" +
                "          \"streetName\": \"StreetName78\",\n" +
                "          \"streetNumber\": 61\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetNumber\": 60\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName20\",\n" +
                "          \"postCode\": \"PostCode83\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-21\",\n" +
                "          \"streetName\": \"StreetName79\",\n" +
                "          \"streetNumber\": 62\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"hashMap\": [\n" +
                "    {\n" +
                "      \"p2\": {\n" +
                "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                "        \"birthCountry\": \"FRANCE\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName5\",\n" +
                "          \"streetName\": \"StreetName64\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode68\",\n" +
                "          \"since\": \"2017-04-06\",\n" +
                "          \"streetNumber\": 47\n" +
                "        },\n" +
                "        \"firstName\": \"FirstName5\",\n" +
                "        \"email\": \"e5@e.mail\",\n" +
                "        \"lastName\": \"LastName5\",\n" +
                "        \"previousAddresses\": []\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"p1\": {\n" +
                "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                "        \"birthCountry\": \"EGYPT\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName4\",\n" +
                "          \"since\": \"2017-04-05\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode67\",\n" +
                "          \"streetName\": \"StreetName63\",\n" +
                "          \"streetNumber\": 46\n" +
                "        },\n" +
                "        \"email\": \"e4@e.mail\",\n" +
                "        \"lastName\": \"LastName4\",\n" +
                "        \"firstName\": \"FirstName4\",\n" +
                "        \"previousAddresses\": [\n" +
                "          {\n" +
                "            \"city\": \"CityName14\",\n" +
                "            \"postCode\": \"PostCode77\",\n" +
                "            \"country\": \"AUSTRIA\",\n" +
                "            \"since\": \"2017-04-15\",\n" +
                "            \"streetName\": \"StreetName73\",\n" +
                "            \"streetNumber\": 56\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName16\",\n" +
                "            \"since\": \"2017-04-17\",\n" +
                "            \"country\": \"CANADA\",\n" +
                "            \"postCode\": \"PostCode79\",\n" +
                "            \"streetName\": \"StreetName75\",\n" +
                "            \"streetNumber\": 58\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName15\",\n" +
                "            \"country\": \"BELGIUM\",\n" +
                "            \"streetName\": \"StreetName74\",\n" +
                "            \"postCode\": \"PostCode78\",\n" +
                "            \"since\": \"2017-04-16\",\n" +
                "            \"streetNumber\": 57\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName17\",\n" +
                "            \"country\": \"DENMARK\",\n" +
                "            \"postCode\": \"PostCode80\",\n" +
                "            \"streetName\": \"StreetName76\",\n" +
                "            \"since\": \"2017-04-18\",\n" +
                "            \"streetNumber\": 59\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\",\n" +
                "        \"streetNumber\": 44\n" +
                "      },\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"since\": \"2017-04-13\",\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"since\": \"2017-04-02\",\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43\n" +
                "      },\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"since\": \"2017-04-12\",\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\",\n" +
                "        \"streetNumber\": 45\n" +
                "      },\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\",\n" +
                "          \"streetNumber\": 56\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"since\": \"2017-04-16\",\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(), jsonMatcher -> jsonMatcher.sortField(is("array"), is("previousAddresses"), is("hashMap"), is("set")), null, null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.sortField(is("array"), is("previousAddresses"), is("hashMap"), is("set")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("array[1].birthCountry\n" +
                    "Expected: HUNGARY\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].birthDate\n" +
                    "Expected: 2011-04-01T13:42:11\n" +
                    "     got: 2009-04-01T13:42:11\n" +
                    " ; array[1].currentAddress.city\n" +
                    "Expected: CityName6\n" +
                    "     got: CityName8\n" +
                    " ; array[1].currentAddress.country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].currentAddress.postCode\n" +
                    "Expected: PostCode69\n" +
                    "     got: PostCode71\n" +
                    " ; array[1].currentAddress.since\n" +
                    "Expected: 2017-04-07\n" +
                    "     got: 2017-04-09\n" +
                    " ; array[1].currentAddress.streetName\n" +
                    "Expected: StreetName65\n" +
                    "     got: StreetName67\n" +
                    " ; array[1].currentAddress.streetNumber\n" +
                    "Expected: 48\n" +
                    "     got: 50\n" +
                    " ; array[1].email\n" +
                    "Expected: e6@e.mail\n" +
                    "     got: e8@e.mail\n" +
                    " ; array[1].firstName\n" +
                    "Expected: FirstName6\n" +
                    "     got: FirstName8\n" +
                    " ; array[1].lastName\n" +
                    "Expected: LastName6\n" +
                    "     got: LastName8\n" +
                    " ; array[1].previousAddresses[]: Expected 1 values but got 3 ; array[2].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].birthDate\n" +
                    "Expected: 2009-04-01T13:42:11\n" +
                    "     got: 2011-04-01T13:42:11\n" +
                    " ; array[2].currentAddress.city\n" +
                    "Expected: CityName8\n" +
                    "     got: CityName6\n" +
                    " ; array[2].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].currentAddress.postCode\n" +
                    "Expected: PostCode71\n" +
                    "     got: PostCode69\n" +
                    " ; array[2].currentAddress.since\n" +
                    "Expected: 2017-04-09\n" +
                    "     got: 2017-04-07\n" +
                    " ; array[2].currentAddress.streetName\n" +
                    "Expected: StreetName67\n" +
                    "     got: StreetName65\n" +
                    " ; array[2].currentAddress.streetNumber\n" +
                    "Expected: 50\n" +
                    "     got: 48\n" +
                    " ; array[2].email\n" +
                    "Expected: e8@e.mail\n" +
                    "     got: e6@e.mail\n" +
                    " ; array[2].firstName\n" +
                    "Expected: FirstName8\n" +
                    "     got: FirstName6\n" +
                    " ; array[2].lastName\n" +
                    "Expected: LastName8\n" +
                    "     got: LastName6\n" +
                    " ; array[2].previousAddresses[]: Expected 3 values but got 1 ; hashMap[0]\n" +
                    "Expected: p2\n" +
                    "     but none found\n" +
                    " ; hashMap[0]\n" +
                    "Unexpected: p1\n" +
                    " ; hashMap[1]\n" +
                    "Expected: p1\n" +
                    "     but none found\n" +
                    " ; hashMap[1]\n" +
                    "Unexpected: p2\n" +
                    " ; set[0].birthCountry\n" +
                    "Expected: CANADA\n" +
                    "     got: BELGIUM\n" +
                    " ; set[0].birthDate\n" +
                    "Expected: 2015-04-01T13:42:11\n" +
                    "     got: 2016-04-01T13:42:11\n" +
                    " ; set[0].currentAddress.city\n" +
                    "Expected: CityName2\n" +
                    "     got: CityName1\n" +
                    " ; set[0].currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: BELGIUM\n" +
                    " ; set[0].currentAddress.postCode\n" +
                    "Expected: PostCode65\n" +
                    "     got: PostCode64\n" +
                    " ; set[0].currentAddress.since\n" +
                    "Expected: 2017-04-03\n" +
                    "     got: 2017-04-02\n" +
                    " ; set[0].currentAddress.streetName\n" +
                    "Expected: StreetName61\n" +
                    "     got: StreetName60\n" +
                    " ; set[0].currentAddress.streetNumber\n" +
                    "Expected: 44\n" +
                    "     got: 43\n" +
                    " ; set[0].email\n" +
                    "Expected: e2@e.mail\n" +
                    "     got: e1@e.mail\n" +
                    " ; set[0].firstName\n" +
                    "Expected: FirstName2\n" +
                    "     got: FirstName1\n" +
                    " ; set[0].lastName\n" +
                    "Expected: LastName2\n" +
                    "     got: LastName1\n" +
                    " ; set[0].previousAddresses[]: Expected 2 values but got 1 ; set[1].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: CANADA\n" +
                    " ; set[1].birthDate\n" +
                    "Expected: 2016-04-01T13:42:11\n" +
                    "     got: 2015-04-01T13:42:11\n" +
                    " ; set[1].currentAddress.city\n" +
                    "Expected: CityName1\n" +
                    "     got: CityName2\n" +
                    " ; set[1].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: CANADA\n" +
                    " ; set[1].currentAddress.postCode\n" +
                    "Expected: PostCode64\n" +
                    "     got: PostCode65\n" +
                    " ; set[1].currentAddress.since\n" +
                    "Expected: 2017-04-02\n" +
                    "     got: 2017-04-03\n" +
                    " ; set[1].currentAddress.streetName\n" +
                    "Expected: StreetName60\n" +
                    "     got: StreetName61\n" +
                    " ; set[1].currentAddress.streetNumber\n" +
                    "Expected: 43\n" +
                    "     got: 44\n" +
                    " ; set[1].email\n" +
                    "Expected: e1@e.mail\n" +
                    "     got: e2@e.mail\n" +
                    " ; set[1].firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: FirstName2\n" +
                    " ; set[1].lastName\n" +
                    "Expected: LastName1\n" +
                    "     got: LastName2\n" +
                    " ; set[1].previousAddresses[]: Expected 1 values but got 2 ; set[2].previousAddresses[0].city\n" +
                    "Expected: CityName14\n" +
                    "     got: CityName13\n" +
                    " ; set[2].previousAddresses[0].country\n" +
                    "Expected: AUSTRIA\n" +
                    "     got: HUNGARY\n" +
                    " ; set[2].previousAddresses[0].postCode\n" +
                    "Expected: PostCode77\n" +
                    "     got: PostCode76\n" +
                    " ; set[2].previousAddresses[0].since\n" +
                    "Expected: 2017-04-15\n" +
                    "     got: 2017-04-14\n" +
                    " ; set[2].previousAddresses[0].streetName\n" +
                    "Expected: StreetName73\n" +
                    "     got: StreetName72\n" +
                    " ; set[2].previousAddresses[0].streetNumber\n" +
                    "Expected: 56\n" +
                    "     got: 55\n" +
                    " ; set[2].previousAddresses[1].city\n" +
                    "Expected: CityName13\n" +
                    "     got: CityName14\n" +
                    " ; set[2].previousAddresses[1].country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: AUSTRIA\n" +
                    " ; set[2].previousAddresses[1].postCode\n" +
                    "Expected: PostCode76\n" +
                    "     got: PostCode77\n" +
                    " ; set[2].previousAddresses[1].since\n" +
                    "Expected: 2017-04-14\n" +
                    "     got: 2017-04-15\n" +
                    " ; set[2].previousAddresses[1].streetName\n" +
                    "Expected: StreetName72\n" +
                    "     got: StreetName73\n" +
                    " ; set[2].previousAddresses[1].streetNumber\n" +
                    "Expected: 55\n" +
                    "     got: 56\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "no explicit sorting applied");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "no explicit sorting applied");
        }, AssertionFailedError.class);
    }


    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameConfig() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withRelativePathName("notHash").withFileName("single-line-2");

            writeFile(imfsi.getTestPath().resolve("notHash").resolve("single-line-2-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("notHash/single-line-2-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameConfigAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String approvedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.<BeanWithPrimitives>jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig()).withRelativePathName("notHash").withFileName("single-line-2");

            writeFile(imfsi.getTestPath().resolve("notHash").resolve("single-line-2-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            String basePath = imfsi.getTestPath().toString();
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanInt\": 10,\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\"\n" +
                    "}\n" +
                    "     but: Expected file " + basePath + "/notHash/single-line-2\n" +
                    "\n" +
                    "Expected: beanInt\n" +
                    "     but none found\n" +
                    " ; beanLong\n" +
                    "Expected: 5\n" +
                    "     got: 6\n" +
                    " ; \n" +
                    "Expected: beanString\n" +
                    "     but none found\n" +
                    " ; \n" +
                    "Unexpected: beanBoolean\n" +
                    " ; \n" +
                    "Unexpected: beanByte\n" +
                    " ; \n" +
                    "Unexpected: beanChar\n" +
                    " ; \n" +
                    "Unexpected: beanDouble\n" +
                    " ; \n" +
                    "Unexpected: beanFloat\n" +
                    " ; \n" +
                    "Unexpected: beanInteger\n" +
                    " ; \n" +
                    "Unexpected: beanShort\n", StringUtil.normalizeNewLines(actualError.getMessage()));

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("notHash/single-line-2-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitSortingByPathOfExpectedFileShouldThrowWhenExpectedFileDiffersAndInStrictMode(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"array\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"HUNGARY\",\n" +
                "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName6\",\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"postCode\": \"PostCode69\",\n" +
                "        \"since\": \"2017-04-07\",\n" +
                "        \"streetName\": \"StreetName65\",\n" +
                "        \"streetNumber\": 48\n" +
                "      },\n" +
                "      \"email\": \"e6@e.mail\",\n" +
                "      \"firstName\": \"FirstName6\",\n" +
                "      \"lastName\": \"LastName6\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName16\",\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"postCode\": \"PostCode79\",\n" +
                "          \"since\": \"2017-04-17\",\n" +
                "          \"streetName\": \"StreetName75\",\n" +
                "          \"streetNumber\": 58\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"AUSTRIA\",\n" +
                "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName7\",\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"postCode\": \"PostCode70\",\n" +
                "        \"since\": \"2017-04-08\",\n" +
                "        \"streetName\": \"StreetName66\",\n" +
                "        \"streetNumber\": 49\n" +
                "      },\n" +
                "      \"email\": \"e7@e.mail\",\n" +
                "      \"firstName\": \"FirstName7\",\n" +
                "      \"lastName\": \"LastName7\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName17\",\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"postCode\": \"PostCode80\",\n" +
                "          \"since\": \"2017-04-18\",\n" +
                "          \"streetName\": \"StreetName76\",\n" +
                "          \"streetNumber\": 59\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"streetNumber\": 60\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName8\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"postCode\": \"PostCode71\",\n" +
                "        \"since\": \"2017-04-09\",\n" +
                "        \"streetName\": \"StreetName67\",\n" +
                "        \"streetNumber\": 50\n" +
                "      },\n" +
                "      \"email\": \"e8@e.mail\",\n" +
                "      \"firstName\": \"FirstName8\",\n" +
                "      \"lastName\": \"LastName8\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"streetNumber\": 60\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName19\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode82\",\n" +
                "          \"since\": \"2017-04-20\",\n" +
                "          \"streetName\": \"StreetName78\",\n" +
                "          \"streetNumber\": 61\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName20\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"postCode\": \"PostCode83\",\n" +
                "          \"since\": \"2017-04-21\",\n" +
                "          \"streetName\": \"StreetName79\",\n" +
                "          \"streetNumber\": 62\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"hashMap\": [\n" +
                "    {\n" +
                "      \"p1\": {\n" +
                "        \"birthCountry\": \"EGYPT\",\n" +
                "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName4\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode67\",\n" +
                "          \"since\": \"2017-04-05\",\n" +
                "          \"streetName\": \"StreetName63\",\n" +
                "          \"streetNumber\": 46\n" +
                "        },\n" +
                "        \"email\": \"e4@e.mail\",\n" +
                "        \"firstName\": \"FirstName4\",\n" +
                "        \"lastName\": \"LastName4\",\n" +
                "        \"previousAddresses\": [\n" +
                "          {\n" +
                "            \"city\": \"CityName14\",\n" +
                "            \"country\": \"AUSTRIA\",\n" +
                "            \"postCode\": \"PostCode77\",\n" +
                "            \"since\": \"2017-04-15\",\n" +
                "            \"streetName\": \"StreetName73\",\n" +
                "            \"streetNumber\": 56\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName15\",\n" +
                "            \"country\": \"BELGIUM\",\n" +
                "            \"postCode\": \"PostCode78\",\n" +
                "            \"since\": \"2017-04-16\",\n" +
                "            \"streetName\": \"StreetName74\",\n" +
                "            \"streetNumber\": 57\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName16\",\n" +
                "            \"country\": \"CANADA\",\n" +
                "            \"postCode\": \"PostCode79\",\n" +
                "            \"since\": \"2017-04-17\",\n" +
                "            \"streetName\": \"StreetName75\",\n" +
                "            \"streetNumber\": 58\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName17\",\n" +
                "            \"country\": \"DENMARK\",\n" +
                "            \"postCode\": \"PostCode80\",\n" +
                "            \"since\": \"2017-04-18\",\n" +
                "            \"streetName\": \"StreetName76\",\n" +
                "            \"streetNumber\": 59\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"p2\": {\n" +
                "        \"birthCountry\": \"FRANCE\",\n" +
                "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName5\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode68\",\n" +
                "          \"since\": \"2017-04-06\",\n" +
                "          \"streetName\": \"StreetName64\",\n" +
                "          \"streetNumber\": 47\n" +
                "        },\n" +
                "        \"email\": \"e5@e.mail\",\n" +
                "        \"firstName\": \"FirstName5\",\n" +
                "        \"lastName\": \"LastName5\",\n" +
                "        \"previousAddresses\": []\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"since\": \"2017-04-02\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43\n" +
                "      },\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"since\": \"2017-04-12\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"streetNumber\": 44\n" +
                "      },\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"since\": \"2017-04-13\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"streetNumber\": 45\n" +
                "      },\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"streetNumber\": 56\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"since\": \"2017-04-16\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator = jsonMatcher -> jsonMatcher.sortField("array", "array.previousAddresses", "set.previousAddresses", "hashMap.p1.previousAddresses", "hashMap", "set");
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(), configurator, null, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSorting(), configurator, thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("array[0].birthCountry\n" +
                    "Expected: HUNGARY\n" +
                    "     got: AUSTRIA\n" +
                    " ; array[0].birthDate\n" +
                    "Expected: 2011-04-01T13:42:11\n" +
                    "     got: 2010-04-01T13:42:11\n" +
                    " ; array[0].currentAddress.city\n" +
                    "Expected: CityName6\n" +
                    "     got: CityName7\n" +
                    " ; array[0].currentAddress.country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: AUSTRIA\n" +
                    " ; array[0].currentAddress.postCode\n" +
                    "Expected: PostCode69\n" +
                    "     got: PostCode70\n" +
                    " ; array[0].currentAddress.since\n" +
                    "Expected: 2017-04-07\n" +
                    "     got: 2017-04-08\n" +
                    " ; array[0].currentAddress.streetName\n" +
                    "Expected: StreetName65\n" +
                    "     got: StreetName66\n" +
                    " ; array[0].currentAddress.streetNumber\n" +
                    "Expected: 48\n" +
                    "     got: 49\n" +
                    " ; array[0].email\n" +
                    "Expected: e6@e.mail\n" +
                    "     got: e7@e.mail\n" +
                    " ; array[0].firstName\n" +
                    "Expected: FirstName6\n" +
                    "     got: FirstName7\n" +
                    " ; array[0].lastName\n" +
                    "Expected: LastName6\n" +
                    "     got: LastName7\n" +
                    " ; array[0].previousAddresses[]: Expected 1 values but got 2 ; array[1].birthCountry\n" +
                    "Expected: AUSTRIA\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].birthDate\n" +
                    "Expected: 2010-04-01T13:42:11\n" +
                    "     got: 2009-04-01T13:42:11\n" +
                    " ; array[1].currentAddress.city\n" +
                    "Expected: CityName7\n" +
                    "     got: CityName8\n" +
                    " ; array[1].currentAddress.country\n" +
                    "Expected: AUSTRIA\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].currentAddress.postCode\n" +
                    "Expected: PostCode70\n" +
                    "     got: PostCode71\n" +
                    " ; array[1].currentAddress.since\n" +
                    "Expected: 2017-04-08\n" +
                    "     got: 2017-04-09\n" +
                    " ; array[1].currentAddress.streetName\n" +
                    "Expected: StreetName66\n" +
                    "     got: StreetName67\n" +
                    " ; array[1].currentAddress.streetNumber\n" +
                    "Expected: 49\n" +
                    "     got: 50\n" +
                    " ; array[1].email\n" +
                    "Expected: e7@e.mail\n" +
                    "     got: e8@e.mail\n" +
                    " ; array[1].firstName\n" +
                    "Expected: FirstName7\n" +
                    "     got: FirstName8\n" +
                    " ; array[1].lastName\n" +
                    "Expected: LastName7\n" +
                    "     got: LastName8\n" +
                    " ; array[1].previousAddresses[]: Expected 2 values but got 3 ; array[2].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].birthDate\n" +
                    "Expected: 2009-04-01T13:42:11\n" +
                    "     got: 2011-04-01T13:42:11\n" +
                    " ; array[2].currentAddress.city\n" +
                    "Expected: CityName8\n" +
                    "     got: CityName6\n" +
                    " ; array[2].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].currentAddress.postCode\n" +
                    "Expected: PostCode71\n" +
                    "     got: PostCode69\n" +
                    " ; array[2].currentAddress.since\n" +
                    "Expected: 2017-04-09\n" +
                    "     got: 2017-04-07\n" +
                    " ; array[2].currentAddress.streetName\n" +
                    "Expected: StreetName67\n" +
                    "     got: StreetName65\n" +
                    " ; array[2].currentAddress.streetNumber\n" +
                    "Expected: 50\n" +
                    "     got: 48\n" +
                    " ; array[2].email\n" +
                    "Expected: e8@e.mail\n" +
                    "     got: e6@e.mail\n" +
                    " ; array[2].firstName\n" +
                    "Expected: FirstName8\n" +
                    "     got: FirstName6\n" +
                    " ; array[2].lastName\n" +
                    "Expected: LastName8\n" +
                    "     got: LastName6\n" +
                    " ; array[2].previousAddresses[]: Expected 3 values but got 1"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "no explicit sorting applied");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "no explicit sorting applied");
        }, AssertionFailedError.class);
    }
}
