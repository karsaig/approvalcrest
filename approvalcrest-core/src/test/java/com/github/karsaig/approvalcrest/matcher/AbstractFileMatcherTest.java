package com.github.karsaig.approvalcrest.matcher;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import com.github.karsaig.approvalcrest.util.InMemoryFsUtil;

import com.google.common.collect.ImmutableList;


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

    protected void inMemoryFsWithDummyTestInfo(Object input, String expected, boolean result) {
        inMemoryUnixFs((fs, path) -> {
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

    protected void inMemoryUnixFs(BiConsumer<FileSystem, Path> test) {
        InMemoryFsUtil.inMemoryUnixFs(test);
    }

    protected void inMemoryWindowsFs(BiConsumer<FileSystem, Path> test) {
        InMemoryFsUtil.inMemoryWindowsFs(test);
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
}
