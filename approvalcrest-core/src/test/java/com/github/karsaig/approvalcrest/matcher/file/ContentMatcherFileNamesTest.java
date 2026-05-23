package com.github.karsaig.approvalcrest.matcher.file;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.ContentMatcher;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import org.hamcrest.MatcherAssert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.google.common.jimfs.Configuration;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;


import static com.github.karsaig.approvalcrest.util.InMemoryFsUtil.TESTED_OS_CONFIGS;
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


    public static Stream<Arguments> uniqueIndexIgnoredCasesForEveryOs(){
        return supportedOsPermutations(uniqueIndexIgnoredCases());
    }


    @ParameterizedTest
    @MethodSource("uniqueIndexIgnoredCasesForEveryOs")
    public void uniqueIdShouldBeIgnored(Configuration osConfig,String input) {
        String actual = "Content";
        inMemoryFs(osConfig, imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId(input);

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(imfsi.getInMemoryFileSystem().getPath("87668f").resolve("cd3006-approved.content"), "Content");

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

    public static Stream<Arguments> uniqueIdShouldBeAppliedCasesForEveryOs(){
        return supportedOsPermutations(uniqueIdShouldBeAppliedCases());
    }

    @ParameterizedTest
    @MethodSource("uniqueIdShouldBeAppliedCasesForEveryOs")
    public void uniqueIdShouldBeApplied(Configuration osConfig,String input,String fileNamePart) {
        String actual = "Content";
        inMemoryFs(osConfig,imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withUniqueId(input);

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("cd3006-"+fileNamePart+"-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(imfsi.getInMemoryFileSystem().getPath("87668f").resolve("cd3006-"+fileNamePart+"-approved.content"), "Content");

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

    public static Stream<Arguments> customFileNameCasesForEveryOs(){
        return supportedOsPermutations(customFileNameCases());
    }

    @ParameterizedTest
    @MethodSource("customFileNameCasesForEveryOs")
    public void customFileNameTest(Configuration osConfig, String input, String fileName) {
        String actual = "Content";
        String actualFileName = fileName == null ? "cd3006-approved.content" : fileName + "-approved.content";
        inMemoryFs(osConfig,imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withFileName(input);

            writeFile(imfsi.getTestPath().resolve("87668f").resolve(actualFileName), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(imfsi.getInMemoryFileSystem().getPath("87668f").resolve(actualFileName), "Content");

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

    public static Stream<Arguments> customPathCasesForEveryOs(){
        return supportedOsPermutations(customPathCases());
    }

    @ParameterizedTest
    @MethodSource("customPathCasesForEveryOs")
    public void relativeCustomPathTest(Configuration osConfig, String input, String path) {
        String actual = "Content";
        String actualPath = path == null ? "87668f" : path;
        inMemoryFs(osConfig, imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withPathName(input);

            writeFile(imfsi.getTestPath().resolve(actualPath).resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(imfsi.getInMemoryFileSystem().getPath(actualPath).resolve("cd3006-approved.content"), "Content");

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

    public static Stream<Arguments> relativePathCasesForEveryOs(){
        return supportedOsPermutations(relativePathCases());
    }

    @ParameterizedTest
    @MethodSource("relativePathCasesForEveryOs")
    public void relativePathTest(Configuration osConfig,String input, String path) {
        String actual = "Content";
        inMemoryFs(osConfig, imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withRelativePathName(input);

            writeFile(imfsi.getWorkingDirectory().resolve(path).resolve("87668f").resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(imfsi.getWorkingDirectory().resolve(path).resolve("87668f").resolve("cd3006-approved.content").toAbsolutePath(), "Content");

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


    public static Object[][] customRelativePathAndCustomPathCases() {
        return new Object[][]{
                {"a","b","a/b/"},
                {"a/b","c/d","a/b/c/d"},
        };
    }

    public static Stream<Arguments> customRelativePathAndCustomPathCasesForEveryOs(){
        return supportedOsPermutations(customRelativePathAndCustomPathCases());
    }

    @ParameterizedTest
    @MethodSource("customRelativePathAndCustomPathCasesForEveryOs")
    public void customRelativePathAndCustomPath(Configuration osConfig, String relativePath,String customPath, String actualPath) {
        String actual = "Content";
        inMemoryFs(osConfig, imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig()).withRelativePathName(relativePath).withPathName(customPath);

            writeFile(imfsi.getWorkingDirectory().resolve(relativePath).resolve(customPath).resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(imfsi.getWorkingDirectory().resolve(relativePath).resolve(customPath).resolve("cd3006-approved.content").toAbsolutePath(), "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    // f1-abs-rel-combo: absolute withRelativePathName + relative withPathName (Unix only,
    // because the production code uses Paths.get(relativePathName) to check isAbsolute(), which
    // relies on the host OS default filesystem; on Linux, strings starting with "/" are absolute).
    public static Stream<Arguments> absoluteRelativePathWithCustomPathCases() {
        return Stream.of(
            Arguments.of("/abs", "sub", "/abs/sub/"),
            Arguments.of("/abs/deep", "sub/dir", "/abs/deep/sub/dir/")
        );
    }

    @ParameterizedTest
    @MethodSource("absoluteRelativePathWithCustomPathCases")
    public void absoluteRelativePathWithCustomPath(String relativePath, String customPath, String expectedDirPath) {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig())
                    .withRelativePathName(relativePath).withPathName(customPath);

            // absolute relativePathName causes workingDirectory.resolve(relPath) → relPath itself
            writeFile(imfsi.getInMemoryFileSystem().getPath(relativePath).resolve(customPath).resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(expectedDirPath + "cd3006-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    // f1-path-overload: withPath(null) behaves like no path was set (uses class-hash directory).
    public static Stream<Arguments> allOsPermutations() {
        return TESTED_OS_CONFIGS.stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("allOsPermutations")
    public void withPathObjectNullBehavesLikeDefaultTest(Configuration osConfig) {
        String actual = "Content";
        inMemoryFs(osConfig, imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig())
                    .withPath(null);

            writeFile(imfsi.getTestPath().resolve("87668f").resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(
                    imfsi.getInMemoryFileSystem().getPath("87668f").resolve("cd3006-approved.content"), "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    // f1-path-overload: withPath(Path) with a relative filesystem-native Path mirrors withPathName.
    @ParameterizedTest
    @MethodSource("allOsPermutations")
    public void withPathObjectRelativeTest(Configuration osConfig) {
        String actual = "Content";
        inMemoryFs(osConfig, imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            Path relPath = imfsi.getInMemoryFileSystem().getPath("myPath");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig())
                    .withPath(relPath);

            writeFile(imfsi.getTestPath().resolve("myPath").resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(
                    imfsi.getInMemoryFileSystem().getPath("myPath").resolve("cd3006-approved.content"), "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    // f1-path-overload + f1-windows-abs: withPath(absolutePath) with a Unix jimfs absolute Path.
    @Test
    public void withPathObjectAbsoluteUnixTest() {
        String actual = "Content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            Path absPath = imfsi.getInMemoryFileSystem().getPath("/a");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig())
                    .withPath(absPath);

            writeFile(absPath.resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("/a/cd3006-approved.content", "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    // f1-windows-abs: withPath(absolutePath) with a Windows jimfs absolute Path (e.g. C:\a).
    // withPathName(String) cannot cover this on a Linux host because Paths.get("C:\\a") creates a
    // relative Linux path; withPath(Path) accepts the native filesystem Path directly.
    @Test
    public void withPathObjectAbsoluteWindowsTest() {
        String actual = "Content";
        inMemoryWindowsFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "ContentMatcherTest", "uniqueIdTest");
            FileSystem fs = imfsi.getInMemoryFileSystem();
            // Build a Windows absolute path: root (C:\) + "a" → C:\a
            Path absWindowsPath = fs.getRootDirectories().iterator().next().resolve("a");
            ContentMatcher<String> underTest = new ContentMatcher<String>(dummyTestInfo, getDefaultFileMatcherConfig())
                    .withPath(absWindowsPath);

            writeFile(absWindowsPath.resolve("cd3006-approved.content"), actual);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles(
                    absWindowsPath.resolve("cd3006-approved.content"), "Content");

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }
}
