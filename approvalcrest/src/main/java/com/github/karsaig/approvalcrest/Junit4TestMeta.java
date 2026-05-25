package com.github.karsaig.approvalcrest;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.Test;

public class Junit4TestMeta extends Junit4TestMetaBase {

    public Junit4TestMeta() {
        this(Objects.requireNonNull(getTestStackTraceElement(Thread.currentThread().getStackTrace()), "Cannot determine test method for Junit4TestMeta do either of the following to solve it:\n1. Use Junit4DesciptionWatcher as a @Rule and the matcher constructor with Description parameter!\n2. Provide a custom implementation of TestMetaInformation, this is rarely needed."));
    }

    private Junit4TestMeta(StackTraceElement testStackTraceElement) {
        super(testStackTraceElement.getClassName(), testStackTraceElement.getMethodName());
    }

    public Junit4TestMeta(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory);
    }

    static StackTraceElement getTestStackTraceElement(StackTraceElement[] stackTrace) {
        return findTestStackTraceElement(stackTrace, Junit4TestMeta::isTestMethod);
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
