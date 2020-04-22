package com.github.karsaig.approvalcrest.matcher;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;


/**
 * Abstract class for common methods used by the JsonMatcher tests.
 *
 * @author Andras_Gyuro
 */
public abstract class AbstractFileMatcherTest {

    protected BeanWithPrimitives getBeanWithPrimitives() {
        short beanShort = 1;
        boolean beanBoolean = true;
        byte beanByte = 2;
        char beanChar = 'c';
        float beanFloat = 3f;
        int beanInt = 4;
        double beanDouble = 5d;
        long beanLong = 6L;

        BeanWithPrimitives bean = BeanWithPrimitives.Builder.beanWithPrimitives()
                .beanShort(beanShort)
                .beanBoolean(beanBoolean)
                .beanByte(beanByte)
                .beanChar(beanChar)
                .beanFloat(beanFloat)
                .beanInt(beanInt)
                .beanDouble(beanDouble)
                .beanLong(beanLong)
                .build();

        return bean;
    }

    protected String getBeanAsJsonString() {
        return "{ beanLong: 5, beanString: \"dummyString\", beanInt: 10  }";
    }

    protected void inMemoryFsWithDummyTestInfo(Object input, String expected, boolean result) throws IOException {
        inMemoryFs((fs, path) -> {
            try {
                Path jsonDir = path.resolve("4ac405");
                Path testFile = Files.createDirectories(jsonDir).resolve("11b2ef-approved.json");
                Files.write(testFile, ImmutableList.of(expected), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            MatcherAssert.assertThat(new JsonMatcher<>(new DummyInformation(path)).matches(input), Matchers.is(result));
        });
    }

    protected void inMemoryFs(BiConsumer<FileSystem, Path> test) throws IOException {
        Configuration config = Configuration.unix()
                .toBuilder()
                .setAttributeViews("basic", "owner", "posix", "unix")
                .build();
        try (FileSystem fs = Jimfs.newFileSystem(config)) {
            Path testPath = fs.getPath("test", "path");
            Path pathWithDirs = Files.createDirectories(testPath);
            test.accept(fs, pathWithDirs);
        }
    }

    protected class DummyInformation implements TestMetaInformation {

        private final Path path;
        private final String testClassName;
        private final String testMethodName;

        public DummyInformation(Path path) {
            this(path, "dummyTestClassName", "dummyTestMethodName");
        }

        public DummyInformation(Path path, String testClassName, String testMethodName) {
            this.path = path;
            this.testClassName = testClassName;
            this.testMethodName = testMethodName;
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
    }

    protected enum InMemoryFsTypes {
        FILE, DIR, SYMLINK, OTHER
    }

    protected class InMemoryFiles {
        private final String path;
        private final String content;

        public InMemoryFiles(String path, String content) {
            this.path = path;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public String getPath() {
            return path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InMemoryFiles that = (InMemoryFiles) o;
            return Objects.equals(path, that.path) &&
                    Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, content);
        }
    }


    protected List<InMemoryFiles> getFiles(FileSystem fs) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap(rp -> {
                    try {
                        return Files.find(rp, Integer.MAX_VALUE, (p, fa) -> Files.isRegularFile(p));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(p -> {
                    String fullPath = p.toString();
                    if (fullPath.startsWith("/work/test/path/")) {
                        fullPath = fullPath.substring(16);
                    }
                    return new InMemoryFiles(fullPath, readFile(p));
                })
                .collect(Collectors.toList());
    }


    protected String readFile(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getNotApprovedCreationMessage(String createdFile, String renameTo) {
        StringBuilder builder = new StringBuilder();
        builder.append("Not approved file created: '");
        builder.append(createdFile);
        builder.append("'; please verify its contents and rename it to '");
        builder.append(renameTo);
        builder.append("'.");
        return builder.toString();
    }
}
