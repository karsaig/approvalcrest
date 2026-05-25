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

public class JunitJupiterTestMeta extends Junit5TestMetaBase {

    private static final String CANNOT_DETERMINE_TEST_METHOD_ERROR = "Cannot determine test method for JunitJupiterTestMeta, do either of the following to solve it:\n1. Pass org.junit.jupiter.api.TestInfo in as constructor parameter to matcher, if you add it as a parameter to the test method, junit will provide it\n2. Provide a custom implementation of TestMetaInformation, this is rarely needed.";

    public JunitJupiterTestMeta() {
        this(Objects.requireNonNull(getTestStackTraceElement(Thread.currentThread().getStackTrace()), CANNOT_DETERMINE_TEST_METHOD_ERROR));
    }

    protected JunitJupiterTestMeta(String sourceRoutePathString) {
        this(sourceRoutePathString, Objects.requireNonNull(getTestStackTraceElement(Thread.currentThread().getStackTrace()), CANNOT_DETERMINE_TEST_METHOD_ERROR));
    }

    private JunitJupiterTestMeta(String sourceRoutePathString, StackTraceElement testStackTraceElement) {
        super(testStackTraceElement.getClassName(), testStackTraceElement.getMethodName(), sourceRoutePathString);
    }

    private JunitJupiterTestMeta(StackTraceElement testStackTraceElement) {
        super(testStackTraceElement.getClassName(), testStackTraceElement.getMethodName());
    }

    public JunitJupiterTestMeta(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory);
    }


    static StackTraceElement getTestStackTraceElement(StackTraceElement[] stackTrace) {
        return findTestStackTraceElement(stackTrace, JunitJupiterTestMeta::isTestMethod);
    }

    private static boolean isTestMethod(StackTraceElement element) {
        try {
            Class<?> clazz = Class.forName(element.getClassName());
            Method method = findMethod(clazz, element.getMethodName());
            return method != null && hasTestMethodAnnotation(method);
        } catch (Throwable e) {
            return false;
        }
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
}
