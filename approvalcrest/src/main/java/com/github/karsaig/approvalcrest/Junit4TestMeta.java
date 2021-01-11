package com.github.karsaig.approvalcrest;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.Test;

public class Junit4TestMeta extends Junit4TestMetaBase {

    public Junit4TestMeta() {
        this(Objects.requireNonNull(getTestStackTraceElement(Thread.currentThread().getStackTrace()), "Cannot determine test method for Junit4TestMeta, custom implementation of TestMetaInformation required!"));
    }

    private Junit4TestMeta(StackTraceElement testStackTraceElement) {
        super(testStackTraceElement.getClassName(), testStackTraceElement.getMethodName());
    }

    public Junit4TestMeta(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory);
    }

    private static StackTraceElement getTestStackTraceElement(StackTraceElement[] stackTrace) {
        StackTraceElement result = null;
        for (StackTraceElement s : stackTrace) {
            if (isTestMethod(s)) {
                result = s;
                break;
            }
        }
        return result;
    }

    private static boolean isTestMethod(StackTraceElement element) {
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

    private static Method findMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}
