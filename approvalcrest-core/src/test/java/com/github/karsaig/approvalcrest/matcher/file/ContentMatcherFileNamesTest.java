package com.github.karsaig.approvalcrest.matcher.file;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.ContentMatcher;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import org.hamcrest.MatcherAssert;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ContentMatcherFileNamesTest extends AbstractFileMatcherTest {

    public static Object[][] uniqueIndexIgnoredCases() {
        return new Object[][]{
                {null},
                {""},
                {" "},
                {"    "},
                {"-"}
        };
    }

    @ParameterizedTest
    @MethodSource("uniqueIndexIgnoredCases")
    public void uniqueIdShouldBeIgnored(String input) {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId(input);

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/cd3006-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    public static Object[][] uniqueIdShouldBeAppliedCases() {
        return new Object[][]{
                {"a","a"},
                {"A","A"},
                {"asd","asd"},
                {"ASD","ASD"},
                {"aaaa","aaaa"},
                {"-a","a"},
                {"a-","a"},
                {"-a-","a"},
                {"ASD-","ASD"},
        };
    }

    @ParameterizedTest
    @MethodSource("uniqueIdShouldBeAppliedCases")
    public void uniqueIdShouldBeApplied(String input,String fileNamePart) {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId(input);

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("cd3006-"+fileNamePart+"-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/cd3006-"+fileNamePart+"-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    public static Object[][] customFileNameCases() {
        return new Object[][]{
                {null,null},
                {" ",null},
                {"    ",null},
                {"a","a"},
                {"A","A"},
                {"a-","a"},
                {"A-","A"},
                {"asd","asd"},
                {"asd-","asd"},
                {"ASD-","ASD"},
        };
    }

    @ParameterizedTest
    @MethodSource("customFileNameCases")
    public void customFileNameTest(String input, String fileName) {
        String actual = "Content";
        String actualFileName = fileName == null ? "cd3006-approved.content" : fileName + "-approved.content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withFileName(input);

            writeFile(imfsi.getTestPath().resolve("87668f").resolve(actualFileName), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("87668f/"+actualFileName, "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    public static Object[][] customPathCases() {
        return new Object[][]{
                {null,null},
                {" ",null},
                {"    ",null},
                {"a","a"},
                {"A","A"},
                {"a-","a-"},
                {"A-","A-"},
                {"asd","asd"},
                {"asd-","asd-"},
                {"ASD-","ASD-"},
                {"a/b/c","a/b/c"},
        };
    }

    @ParameterizedTest
    @MethodSource("customPathCases")
    public void relativeCustomPathTest(String input, String path) {
        String actual = "Content";
        String actualPath = path == null ? "87668f" : path;
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withPathName(input);

            writeFile(imfsi.getTestPath().resolve(actualPath).resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(actualPath + "/cd3006-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }


    public static Object[][] absoluteCustomPathCases() {
        return new Object[][]{
                {"/","/"},
                {"/a","/a/"},
                {"/A","/A/"},
                {"/a-","/a-/"},
                {"/A-","/A-/"},
                {"/asd","/asd/"},
                {"/asd-","/asd-/"},
                {"/ASD-","/ASD-/"},
                {"/a/b/c","/a/b/c/"},
        };
    }

    @ParameterizedTest
    @MethodSource("absoluteCustomPathCases")
    public void absoluteCustomPathTest(String input, String path) {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withPathName(input);

            writeFile(imfsi.getInMemoryFileSystem().getPath("/").resolve(input).resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(path + "cd3006-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    public static Object[][] relativePathCases() {
        return new Object[][]{
                {"a","a"},
                {"A","A"},
                {"a-","a-"},
                {"A-","A-"},
                {"asd","asd"},
                {"asd-","asd-"},
                {"ASD-","ASD-"},
                {"a/b/c","a/b/c"},
        };
    }

    @ParameterizedTest
    @MethodSource("relativePathCases")
    public void relativePathTest(String input, String path) {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withRelativePathName(input);

            writeFile(imfsi.getWorkingDirectory().resolve(path).resolve("87668f").resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(imfsi.getWorkingDirectory().resolve(path).toAbsolutePath() + "/87668f/cd3006-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    public static Object[][] absoluteRelativePathCases() {
        return new Object[][]{
                {"/a","/a"},
                {"/A","/A"},
                {"/a-","/a-"},
                {"/A-","/A-"},
                {"/asd","/asd"},
                {"/asd-","/asd-"},
                {"/ASD-","/ASD-"},
                {"/a/b/c","/a/b/c"},
        };
    }

    @ParameterizedTest
    @MethodSource("absoluteRelativePathCases")
    public void absoluteRelativePath(String input, String path) {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withRelativePathName(input);

            writeFile(imfsi.getInMemoryFileSystem().getPath(path).resolve("87668f").resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(imfsi.getInMemoryFileSystem().getPath(path).toAbsolutePath() + "/87668f/cd3006-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }
}
