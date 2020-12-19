package com.github.karsaig.approvalcrest.jupiter;

import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class Junit5TestMetaBase implements TestMetaInformation {

    private static final String SRC_TEST_JAVA_PATH = "src" + File.separator + "test" + File.separator + "java"
            + File.separator;
    private static final Pattern DOT_LITERAL_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private static final Path APPROVED_DIRECTORY = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "approvalcrest");

    private final Path testClassPath;
    private final String testClassName;
    private final String testMethodName;
    private final Path approvedDirectory;

    protected Junit5TestMetaBase(String testClassName, String testMethodName) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.testClassPath = buildClassPath();
        this.approvedDirectory = APPROVED_DIRECTORY;
    }

    protected Path buildClassPath() {
        return Paths.get(getSourceRoutePathString() + DOT_LITERAL_PATTERN.matcher(testClassName).replaceAll(Matcher.quoteReplacement(File.separator))).getParent();
    }

    protected String getSourceRoutePathString() {
        return SRC_TEST_JAVA_PATH;
    }

    protected Junit5TestMetaBase(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        this.testClassPath = testClassPath;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.approvedDirectory = approvedDirectory;
    }

    @Override
    public Path getTestClassPath() { return testClassPath; }

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