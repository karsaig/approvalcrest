package com.github.karsaig.approvalcrest.testng;

import com.github.karsaig.approvalcrest.matcher.AbstractTestMetaBase;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Objects;

import org.testng.annotations.Test;

public class TestNgTestMeta extends AbstractTestMetaBase {

    private static final String CANNOT_DETERMINE_TEST_METHOD_ERROR = "Cannot determine test method for TestNgTestMeta, do either of the following to solve it:\n1. Pass java.lang.reflect.Method as constructor parameter to matcher (TestNG injects it into test methods)\n2. Provide a custom implementation of TestMetaInformation, this is rarely needed.";

    public TestNgTestMeta() {
        this(Objects.requireNonNull(getTestStackTraceElement(Thread.currentThread().getStackTrace()), CANNOT_DETERMINE_TEST_METHOD_ERROR));
    }

    private TestNgTestMeta(StackTraceElement testStackTraceElement) {
        super(testStackTraceElement.getClassName(), testStackTraceElement.getMethodName());
    }

    public TestNgTestMeta(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory);
    }

    static StackTraceElement getTestStackTraceElement(StackTraceElement[] stackTrace) {
        return findTestStackTraceElement(stackTrace, TestNgTestMeta::isTestMethod);
    }

    private static boolean isTestMethod(StackTraceElement element) {
        try {
            Class<?> clazz = Class.forName(element.getClassName());
            Method method = findMethod(clazz, element.getMethodName());
            return method != null && method.isAnnotationPresent(Test.class);
        } catch (Throwable e) {
            return false;
        }
    }
}
