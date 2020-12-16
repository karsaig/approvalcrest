package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.DEFAULT_JIMFS_PERMISSIONS;
import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.DIRECTORY_CREATE_PERMISSONS;
import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.FILE_CREATE_PERMISSONS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.testdata.ChildBean;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.github.karsaig.approvalcrest.util.InMemoryPermissions;

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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());


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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, enablePassOnCreate());

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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, enablePassOnCreate());

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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, enableInPlaceOverwriteAndPassOnCreate());

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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, enablePassOnCreate());

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
                    "Unexpected: beanShort\n", actualError.getMessage());


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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, enablePassOnCreate());

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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, enableInPlaceOverwriteAndPassOnCreate());

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
            JsonMatcher<String> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "675159-not-approved.json", "675159-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/675159-not-approved.json", "/*JsonMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString*/\n" +
                    "{\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"dummyString\",\n" +
                    "  \"beanInt\": 10\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryWindowsFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

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
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Modified content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentDiffersFromApprovedContent");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("0d08c2-approved.json"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Modified content\",\n" +
                    "  \"beanInt\": 10\n" +
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
                    "Unexpected: beanShort\n", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/0d08c2-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId("idTest");

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
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueIdAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId("idTest");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("39e1a0-idTest-approved.json"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\",\n" +
                    "  \"beanInt\": 10\n" +
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
                    "Unexpected: beanShort\n", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/39e1a0-idTest-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo, getDefaultFileMatcherConfig()).withFileName("single-line");

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
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo, getDefaultFileMatcherConfig()).withFileName("single-line");

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("single-line-approved.json"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\",\n" +
                    "  \"beanInt\": 10\n" +
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
                    "Unexpected: beanShort\n", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/single-line-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().getRoot().resolve("/work/test/path/src/test/contents").resolve("single-line-2-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("src/test/contents/single-line-2-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().getRoot().resolve("/work/test/path/src/test/contents").resolve("single-line-2-approved.json"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\",\n" +
                    "  \"beanInt\": 10\n" +
                    "}\n" +
                    "     but: Expected file /work/test/path/src/test/contents/single-line-2\n" +
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
                    "Unexpected: beanShort\n", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("src/test/contents/single-line-2-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathName");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("/src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().getRoot().resolve("/src/test/contents").resolve("single-line-2-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("/src/test/contents/single-line-2-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathNameAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo, getDefaultFileMatcherConfig()).withPath(imfsi.getTestPath().resolve("/src/test/contents")).withFileName("single-line-2");

            writeFile(imfsi.getTestPath().getRoot().resolve("/src/test/contents").resolve("single-line-2-approved.json"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\",\n" +
                    "  \"beanInt\": 10\n" +
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
                    "Unexpected: beanShort\n", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("/src/test/contents/single-line-2-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenJsonWithWindowsNewLineExpectedContentSameContentAsApproved() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String apprivedFileContent = "{\r\n" +
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
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("78a94f-approved.json"), apprivedFileContent);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/78a94f-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void contentMatcherWorkflowVerifierTest() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "contentMatcherWorkflowVerifierTest");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

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
            JsonMatcher<BeanWithGeneric<String>> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

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
            JsonMatcher<BeanWithGeneric<List<String>>> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

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
    
    // 262575
    @Test
    public void testStabilizeUUIDs() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        ChildBean c1 = ChildBean.Builder.child()
                .childString(uuid1)
                .build();
        ChildBean c2 = ChildBean.Builder.child()
                .childString(uuid2)
                .build();
        ParentBean actual = ParentBean.Builder.parent()
                .parentString(uuid1)
                .addToChildBeanList(c1)
                .addToChildBeanList(c2)
                .build();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherTest", "testStabilizeUUIDs");
            JsonMatcher<ParentBean> underTest = new JsonMatcher<ParentBean>(dummyTestInfo, getDefaultFileMatcherConfig())
                    .stabilizeUUIDs();
            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "262575-not-approved.json", "262575-approved.json"), actualError.getMessage());
            approveFile(imfsi.getTestPath().resolve("8c5498").resolve("262575-not-approved.json"));
            MatcherAssert.assertThat(actual, underTest);
            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("8c5498/262575-approved.json", "/*JsonMatcherTest.testStabilizeUUIDs*/\n" +
                    "{\n" +
                    "  \"parentString\": \"cfcd2084-95d5-35ef-a6e7-dff9f98764da\",\n" +
                    "  \"childBeanList\": [\n" +
                    "    {\n" +
                    "      \"childString\": \"cfcd2084-95d5-35ef-a6e7-dff9f98764da\",\n" +
                    "      \"childInteger\": 0\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"childString\": \"c4ca4238-a0b9-3382-8dcc-509a6f75849b\",\n" +
                    "      \"childInteger\": 0\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }
    
    // TODO: MFA - Arrays of primitives, Maps with UUID keys, things which are nearly UUIDs 
    
}
