package com.github.karsaig.approvalcrest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.TestInfo;

import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;

public class Junit5InfoBasedTestMeta implements TestMetaInformation {

    private static final String SRC_TEST_JAVA_PATH = "src" + File.separator + "test" + File.separator + "java"
            + File.separator;
    private static final Pattern DOT_LITERAL_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private final Path testClassPath;
    private final String testClassName;
    private final String testMethodName;

    public Junit5InfoBasedTestMeta(TestInfo testInfo) {
        Class<?> clazz = testInfo.getTestClass().orElseThrow(() -> new IllegalStateException("Cannot get class from Testinfo, custom implementation of TestMetaInformation required!"));
        this.testClassPath = Paths.get(SRC_TEST_JAVA_PATH +
                DOT_LITERAL_PATTERN.matcher(clazz.getName()).replaceAll(Matcher.quoteReplacement(File.separator))).getParent();
        this.testClassName = clazz.getName();
        this.testMethodName = testInfo.getTestMethod().map(m -> m.getName()).orElseGet(() -> testInfo.getDisplayName());
    }

    public Junit5InfoBasedTestMeta(Path testClassPath, String testClassName, String testMethodName) {
        this.testClassPath = testClassPath;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
    }

    @Override
    public Path getTestClassPath() {
        return testClassPath;
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
