package com.github.karsaig.approvalcrest;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;

public class Junit4TestMeta implements TestMetaInformation {

    private static final String SRC_TEST_JAVA_PATH = "src" + File.separator + "test" + File.separator + "java"
            + File.separator;
    private static final Pattern DOT_LITERAL_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private final Path testClassPath;
    private final String testClassName;
    private final String testMethodName;
    private final Path approvedDirectory;

    public Junit4TestMeta() {
        StackTraceElement testStackTraceElement = Objects.requireNonNull(getTestStackTraceElement(Thread.currentThread().getStackTrace()), "Cannot determine test method for Junit4TestMeta, custom implementation of TestMetaInformation required!");
        String fileName = testStackTraceElement.getFileName().substring(0, testStackTraceElement.getFileName().lastIndexOf("."));
        testClassPath = Paths.get(SRC_TEST_JAVA_PATH
                + DOT_LITERAL_PATTERN.matcher(testStackTraceElement.getClassName()).replaceAll(Matcher.quoteReplacement(File.separator)).replace(fileName, ""));
        testClassName = testStackTraceElement.getClassName();
        testMethodName = testStackTraceElement.getMethodName();
        approvedDirectory = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "approvalcrest");
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

    private StackTraceElement getTestStackTraceElement(StackTraceElement[] stackTrace) {
        StackTraceElement result = null;
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement s = stackTrace[i];
            if (isTestMethod(s)) {
                result = s;
                break;
            }
        }
        return result;
    }

    private boolean isTestMethod(StackTraceElement element) {
        boolean isTest;

        String fullClassName = element.getClassName();
        Class<?> clazz;
        try {
            clazz = Class.forName(fullClassName);
            Method method = findMethod(clazz, element.getMethodName());
            isTest = method != null && method.isAnnotationPresent(Test.class);
        } catch (Throwable e) {
            isTest = false;
        }

        return isTest;
    }

    private Method findMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}
