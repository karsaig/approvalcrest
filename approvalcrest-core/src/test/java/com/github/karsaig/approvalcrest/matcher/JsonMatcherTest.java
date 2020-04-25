package com.github.karsaig.approvalcrest.matcher;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;

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
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "JsonMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExists");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "183d71-not-approved.json", "183d71-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("8c5498/183d71-not-approved.json", "/*JsonMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExists*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString() {
        String actual = getBeanAsJsonString();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "JsonMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString");
            JsonMatcher<String> underTest = new JsonMatcher<>(dummyTestInfo);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "675159-not-approved.json", "675159-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(fs);
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
        inMemoryWindowsFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "e6fb22-not-approved.json", "e6fb22-approved.json"), actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f\\e6fb22-not-approved.json", "/*ContentMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExistsOnWindows*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApproved() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApproved");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo);

            writeFile(path.resolve("87668f").resolve("39c4dd-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f/39c4dd-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentDiffersFromApprovedContent() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Modified content\", beanInt: 10  }";
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentDiffersFromApprovedContent");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo);

            writeFile(path.resolve("87668f").resolve("0d08c2-approved.json"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Modified content\",\n" +
                    "  \"beanInt\": 10\n" +
                    "}\n" +
                    "     but: Expected file 87668f\\0d08c2-approved.json\n" +
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

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f/0d08c2-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueId");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo).withUniqueId("idTest");

            writeFile(path.resolve("87668f").resolve("3f0945-idTest-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f/3f0945-idTest-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueIdAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithUniqueIdAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo).withUniqueId("idTest");

            writeFile(path.resolve("87668f").resolve("39e1a0-idTest-approved.json"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\",\n" +
                    "  \"beanInt\": 10\n" +
                    "}\n" +
                    "     but: Expected file 87668f\\39e1a0-idTest-approved.json\n" +
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

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f/39e1a0-idTest-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileName");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo).withFileName("single-line");

            writeFile(path.resolve("87668f").resolve("single-line-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f/single-line-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo).withFileName("single-line");

            writeFile(path.resolve("87668f").resolve("single-line-approved.json"), apprivedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"Different content\",\n" +
                    "  \"beanInt\": 10\n" +
                    "}\n" +
                    "     but: Expected file 87668f\\single-line-approved.json\n" +
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

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f/single-line-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathName");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo).withPath(path.resolve("src/test/contents")).withFileName("single-line-2");

            writeFile(path.getRoot().resolve("/work/test/path/src/test/contents").resolve("single-line-2-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("src/test/contents/single-line-2-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndRelativePathNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo).withPath(path.resolve("src/test/contents")).withFileName("single-line-2");

            writeFile(path.getRoot().resolve("/work/test/path/src/test/contents").resolve("single-line-2-approved.json"), apprivedFileContent);

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

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("src/test/contents/single-line-2-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathName() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathName");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo).withPath(path.resolve("/src/test/contents")).withFileName("single-line-2");

            writeFile(path.getRoot().resolve("/src/test/contents").resolve("single-line-2-approved.json"), getBeanWithPrimitivesAsJsonString());

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("/src/test/contents/single-line-2-approved.json", getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathNameAndContentDiffers() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        String apprivedFileContent = "{ beanLong: 5, beanString: \"Different content\", beanInt: 10  }";
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldThrowAssertionErrorWhenContentIsSameContentAsApprovedWithFileNameAndAbsolutePathNameAndContentDiffers");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<BeanWithPrimitives>(dummyTestInfo).withPath(path.resolve("/src/test/contents")).withFileName("single-line-2");

            writeFile(path.getRoot().resolve("/src/test/contents").resolve("single-line-2-approved.json"), apprivedFileContent);

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

            List<InMemoryFiles> actualFiles = getFiles(fs);
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
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "shouldNotThrowAssertionErrorWhenJsonWithWindowsNewLineExpectedContentSameContentAsApproved");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo);

            writeFile(path.resolve("87668f").resolve("78a94f-approved.json"), apprivedFileContent);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f/78a94f-approved.json", apprivedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void contentMatcherWorkflowVerifierTest() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "contentMatcherWorkflowVerifierTest");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "e4bc67-not-approved.json", "e4bc67-approved.json"), actualError.getMessage());

            approveFile(path.resolve("87668f").resolve("e4bc67-not-approved.json"));

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("87668f/e4bc67-approved.json", "/*ContentMatcherTest.contentMatcherWorkflowVerifierTest*/\n" +
                    getBeanWithPrimitivesAsJsonString());

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void contentMatcherWorkflowWithNonAsciiCharacters() {
        BeanWithGeneric<String> actual = BeanWithGeneric.of("dummyValue", "Árvízűtűrőtükörfúrógép\\nL’apostrophe 用的名字☺\\nд1@00000☺☹❤\\naA@AA1A猫很可爱");
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "contentMatcherWorkflowWithNonAsciiCharacters");
            JsonMatcher<BeanWithGeneric<String>> underTest = new JsonMatcher<>(dummyTestInfo);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "c5f725-not-approved.json", "c5f725-approved.json"), actualError.getMessage());

            approveFile(path.resolve("87668f").resolve("c5f725-not-approved.json"));

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
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
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "ContentMatcherTest", "runningFromCommandLineFailedWithCharacterProblemsWithThis");
            JsonMatcher<BeanWithGeneric<List<String>>> underTest = new JsonMatcher<>(dummyTestInfo);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("87668f", "dc100a-not-approved.json", "dc100a-approved.json"), actualError.getMessage());

            approveFile(path.resolve("87668f").resolve("dc100a-not-approved.json"));

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(fs);
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
}
