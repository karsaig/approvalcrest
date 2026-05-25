package com.github.karsaig.approvalcrest.matcher;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTestMetaBase implements TestMetaInformation {

    protected static final String SRC_TEST_JAVA_PATH = "src" + File.separator + "test" + File.separator + "java" + File.separator;
    protected static final Pattern DOT_LITERAL_PATTERN = Pattern.compile(".", Pattern.LITERAL);
    protected static final Path APPROVED_DIRECTORY = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "approvalcrest");

    private final Path testClassPath;
    private final String testClassName;
    private final String testMethodName;
    private final Path approvedDirectory;
    private final Path workingDirectory;

    protected AbstractTestMetaBase(String testClassName, String testMethodName) {
        this(buildClassPath(testClassName), testClassName, testMethodName, APPROVED_DIRECTORY);
    }

    protected AbstractTestMetaBase(String testClassName, String testMethodName, String sourceRoutePathString) {
        this(buildClassPath(testClassName, sourceRoutePathString), testClassName, testMethodName, APPROVED_DIRECTORY);
    }

    protected AbstractTestMetaBase(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        this(testClassPath, testClassName, testMethodName, approvedDirectory, detectWorkingDirectory());
    }

    protected AbstractTestMetaBase(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory, Path workingDirectory) {
        this.testClassPath = testClassPath;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.approvedDirectory = approvedDirectory;
        this.workingDirectory = workingDirectory;
    }

    protected static Path buildClassPath(String testClassName) {
        return buildClassPath(testClassName, getSourceRoutePathString());
    }

    protected static Path buildClassPath(String testClassName, String sourceRoutePathString) {
        return Paths.get(sourceRoutePathString + DOT_LITERAL_PATTERN.matcher(testClassName).replaceAll(Matcher.quoteReplacement(File.separator))).getParent();
    }

    protected static Path detectWorkingDirectory() {
        return Paths.get("").toAbsolutePath();
    }

    protected static String getSourceRoutePathString() {
        return SRC_TEST_JAVA_PATH;
    }

    protected static StackTraceElement findTestStackTraceElement(StackTraceElement[] stackTrace, Predicate<StackTraceElement> isTestMethod) {
        for (StackTraceElement s : stackTrace) {
            if (isTestMethod.test(s)) {
                return s;
            }
        }
        return null;
    }

    protected static Method findMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
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

    @Override
    public Path getApprovedDirectory() {
        return approvedDirectory;
    }

    @Override
    public Path workingDirectory() {
        return workingDirectory;
    }

    @Override
    public String toString() {
        return "TestMeta[cn=" + testClassName +
                ",mn=" + testMethodName +
                ",cp=" + testClassPath +
                ",ad=" + approvedDirectory +
                ",wd=" + workingDirectory + "]";
    }
}
