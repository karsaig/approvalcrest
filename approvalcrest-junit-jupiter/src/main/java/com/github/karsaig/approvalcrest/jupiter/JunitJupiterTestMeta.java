package com.github.karsaig.approvalcrest.jupiter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

public class JunitJupiterTestMeta  extends Junit5TestMetaBase {


    public JunitJupiterTestMeta() {
        this(Objects.requireNonNull(getTestStackTraceElement(Thread.currentThread().getStackTrace()), "Cannot determine test method for JunitJupiterTestMeta, custom implementation of TestMetaInformation required!"));
    }

    private JunitJupiterTestMeta(StackTraceElement testStackTraceElement) {
        super(testStackTraceElement.getClassName(), testStackTraceElement.getMethodName());
    }

    public JunitJupiterTestMeta(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
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
            isTest = method != null && hasTestMethodAnnotation(method);
        } catch (Throwable e) {
            isTest = false;
        }

        return isTest;
    }

    private static boolean hasTestMethodAnnotation(Method method) {
        Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
        return Arrays.stream(declaredAnnotations).anyMatch(JunitJupiterTestMeta::isTestAnnotation);
    }

    private static boolean isTestAnnotation(Annotation annotation) {
        Set<Class<? extends Annotation>> annotationClasses = collectAnnotationClasses(annotation);

        return annotationClasses.contains(Test.class) || annotationClasses.contains(TestTemplate.class);
    }

    private static Set<Class<? extends Annotation>> collectAnnotationClasses(Annotation annotation) {
        Set<Class<? extends Annotation>> annotationClasses = new HashSet<>();

        collectAnnotationClasses(annotationClasses, annotation);

        return annotationClasses;
    }

    private static void collectAnnotationClasses(Set<Class<? extends Annotation>> annotationClasses,
                                          Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();

        if (!annotationClasses.contains(annotationClass)) {
            annotationClasses.add(annotationClass);

            Arrays.stream(annotationClass.getDeclaredAnnotations()).forEach(a -> collectAnnotationClasses(annotationClasses, a));
        }
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
