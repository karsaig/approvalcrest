package com.github.karsaig.approvalcrest;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;

public class JunitJupiterTestMeta implements TestMetaInformation {

    private static final String SRC_TEST_JAVA_PATH = "src" + File.separator + "test" + File.separator + "java"
            + File.separator;
    private static final Pattern DOT_LITERAL_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private final StackTraceElement testStackTraceElement;

    public JunitJupiterTestMeta() {
        testStackTraceElement = getTestStackTraceElement(Thread.currentThread().getStackTrace());
    }

    @Override
    public Path getTestClassPath() {
        String fileName = testStackTraceElement.getFileName().substring(0, testStackTraceElement.getFileName().lastIndexOf("."));
        return Paths.get(SRC_TEST_JAVA_PATH
                + DOT_LITERAL_PATTERN.matcher(testStackTraceElement.getClassName()).replaceAll(Matcher.quoteReplacement(File.separator)).replace(fileName, ""));
    }

    @Override
    public String testClassName() {
        return testStackTraceElement != null ? testStackTraceElement.getClassName() : null;
    }

    @Override
    public String testMethodName() {
        return testStackTraceElement != null ? testStackTraceElement.getMethodName() : null;
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
            isTest = method != null && hasTestMethodAnnotation(method);
        } catch (Throwable e) {
            isTest = false;
        }

        return isTest;
    }

    private boolean hasTestMethodAnnotation(Method method) {
        Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
        return Arrays.stream(declaredAnnotations).anyMatch(this::isTestAnnotation);
    }

    private boolean isTestAnnotation(Annotation annotation) {
        Set<Class<? extends Annotation>> annotationClasses = collectAnnotationClasses(annotation);

        return annotationClasses.contains(Test.class) || annotationClasses.contains(TestTemplate.class);
    }

    private Set<Class<? extends Annotation>> collectAnnotationClasses(Annotation annotation) {
        Set<Class<? extends Annotation>> annotationClasses = new HashSet<>();

        collectAnnotationClasses(annotationClasses, annotation);

        return annotationClasses;
    }

    private void collectAnnotationClasses(Set<Class<? extends Annotation>> annotationClasses,
                                          Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();

        if (!annotationClasses.contains(annotationClass)) {
            annotationClasses.add(annotationClass);

            Arrays.stream(annotationClass.getDeclaredAnnotations()).forEach(a -> collectAnnotationClasses(annotationClasses, a));
        }
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
