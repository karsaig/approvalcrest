package com.github.karsaig.approvalcrest.matcher;


import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import com.github.karsaig.approvalcrest.util.InMemoryFsInfo;
import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;
import com.github.karsaig.approvalcrest.util.PreBuilt;

import com.google.common.collect.ImmutableList;


/**
 * Abstract class for common methods used by the JsonMatcher tests.
 *
 * @author Andras_Gyuro
 */
public abstract class AbstractFileMatcherTest {

    private static final Pattern NOT_APPROVED_PATTERN = Pattern.compile("-not", Pattern.LITERAL);

    protected BeanWithPrimitives getBeanWithPrimitives() {
        return PreBuilt.getBeanWithPrimitives();
    }

    protected String getBeanAsJsonString() {
        return "{ beanLong: 5, beanString: \"dummyString\", beanInt: 10  }";
    }

    protected String getBeanWithPrimitivesAsJsonString() {
        return PreBuilt.getBeanWithPrimitivesAsJsonString();
    }

    protected void inMemoryFsWithDummyTestInfo(Object input, String expectedFileContent, boolean result) {
        inMemoryUnixFs(imfsi -> {
            try {
                Path jsonDir = imfsi.getTestPath().resolve("4ac405");
                Path testFile = Files.createDirectories(jsonDir).resolve("11b2ef-approved.json");
                Files.write(testFile, ImmutableList.of(expectedFileContent), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            JsonMatcher<Object> jsonMatcher = new JsonMatcher<>(new DummyInformation(imfsi.getTestPath(), imfsi.getResourcePath()), getDefaultFileMatcherConfig());
            MatcherAssert.assertThat(jsonMatcher.matches(input), Matchers.is(result));
        });
    }

    protected void inMemoryFsWithDummyTestInfo(Object input, String expectedFileContent, String expectedExceptionMessage) {
        inMemoryUnixFs(imfsi -> {
            try {
                Path jsonDir = imfsi.getTestPath().resolve("4ac405");
                Path testFile = Files.createDirectories(jsonDir).resolve("11b2ef-approved.json");
                Files.write(testFile, ImmutableList.of(expectedFileContent), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            JsonMatcher<Object> jsonMatcher = new JsonMatcher<>(new DummyInformation(imfsi.getTestPath(), imfsi.getResourcePath()), getDefaultFileMatcherConfig());
            if (expectedExceptionMessage == null) {
                MatcherAssert.assertThat(jsonMatcher.matches(input), Matchers.is(true));
                assertInMemoryFsWithDummyTestInfo(imfsi, expectedFileContent);
            } else {
                AssertionError actualError = assertThrows(AssertionError.class,
                        () -> MatcherAssert.assertThat(input, jsonMatcher));

                Assertions.assertEquals(expectedExceptionMessage, actualError.getMessage());
                assertInMemoryFsWithDummyTestInfo(imfsi, expectedFileContent);
            }
        });
    }

    private void assertInMemoryFsWithDummyTestInfo(InMemoryFsInfo imfsi, String expectedFileContent) {
        List<InMemoryFiles> actualFiles = getFiles(imfsi);
        List<InMemoryFiles> expected = singletonList(new InMemoryFiles("4ac405/11b2ef-approved.json", expectedFileContent + System.lineSeparator()));

        assertIterableEquals(expected, actualFiles);
    }

    protected void inMemoryUnixFs(Consumer<InMemoryFsInfo> test) {
        InMemoryFsUtil.inMemoryUnixFs(test);
    }

    protected void inMemoryWindowsFs(Consumer<InMemoryFsInfo> test) {
        InMemoryFsUtil.inMemoryWindowsFs(test);
    }

    protected class DummyInformation implements TestMetaInformation {

        private final Path path;
        private final String testClassName;
        private final String testMethodName;
        private final Path approvedDirectory;

        public DummyInformation(Path path, Path approvedDirectory) {
            this(path, "dummyTestClassName", "dummyTestMethodName", approvedDirectory);
        }

        public DummyInformation(Path path, String testClassName, String testMethodName, Path approvedDirectory) {
            this.path = path;
            this.testClassName = testClassName;
            this.testMethodName = testMethodName;
            this.approvedDirectory = approvedDirectory;
        }

        @Override
        public Path getTestClassPath() {
            return path;
        }

        @Override
        public String testClassName() {
            return testClassName;
        }

        @Override
        public String testMethodName() {
            return testMethodName;
        }

        @Override
        public Path getApprovedDirectory() {
            return approvedDirectory;
        }
    }


    protected DummyInformation dummyInformation(InMemoryFsInfo imfsi) {
        return new DummyInformation(imfsi.getTestPath(), imfsi.getResourcePath());
    }

    protected DummyInformation dummyInformation(InMemoryFsInfo imfsi, String testClassName, String testMethodName) {
        return new DummyInformation(imfsi.getTestPath(), testClassName, testMethodName, imfsi.getResourcePath());
    }

    protected List<InMemoryFiles> getFiles(InMemoryFsInfo imfsi) {
        return InMemoryFsUtil.getFiles(imfsi);
    }

    protected List<InMemoryFiles> getFiles(FileSystem fs) {
        return InMemoryFsUtil.getFiles(fs);
    }

    protected String readFile(Path path) {
        return InMemoryFsUtil.readFile(path);
    }

    protected String getNotApprovedCreationMessage(String classHash, String createdFile, String renameTo) {
        return getNotApprovedCreationMessage(classHash + File.separator + createdFile, renameTo);
    }

    protected String getNotApprovedCreationMessage(String createdFile, String renameTo) {
        StringBuilder builder = new StringBuilder();
        builder.append("Not approved file created: '");
        builder.append(createdFile);
        builder.append("';\n please verify its contents and rename it to '");
        builder.append(renameTo);
        builder.append("'.");
        return builder.toString();
    }

    protected void writeFile(Path path, String content) {
        InMemoryFsUtil.writeFile(path, content);
    }


    protected Path approveFile(Path from) {
        try {
            Path to = from.getParent().resolve(NOT_APPROVED_PATTERN.matcher(from.getFileName().toString()).replaceAll(Matcher.quoteReplacement("")));
            return Files.move(from, to);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileMatcherConfig getDefaultFileMatcherConfig() {
        return new FileMatcherConfig(false, false, false, false);
    }

    public static FileMatcherConfig enableInPlaceOverwrite() {
        return new FileMatcherConfig(true, false, false, false);
    }

    public static FileMatcherConfig enablePassOnCreate() {
        return new FileMatcherConfig(false, true, false, false);
    }

    public static FileMatcherConfig enableInPlaceOverwriteAndPassOnCreate() {
        return new FileMatcherConfig(true, true, false, false);
    }
}
