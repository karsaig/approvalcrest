package com.github.karsaig.approvalcrest.jupiter;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;

import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;

public class JunitJupiterTestMeta  implements TestMetaInformation {

    private static final String SRC_TEST_JAVA_PATH = "src" + File.separator + "test" + File.separator + "java"
            + File.separator;
    private static final Pattern DOT_LITERAL_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private static final Path APPROVED_DIRECTORY = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "approvalcrest");

    private final Path testClassPath;
    private final String testClassName;
    private final String testMethodName;
    private final Path approvedDirectory;

    public JunitJupiterTestMeta(TestInfo testInfo) {
        this(getRequiredClassName(testInfo), getTestMethodName(testInfo));
    }


    public JunitJupiterTestMeta() {
        this(Objects.requireNonNull(getTestStackTraceElement(Thread.currentThread().getStackTrace()), "Cannot determine test method for JunitJupiterTestMeta, custom implementation of TestMetaInformation required!"));
    }

    private JunitJupiterTestMeta(StackTraceElement testStackTraceElement) {
        this(testStackTraceElement.getClassName(), testStackTraceElement.getMethodName());
    }

    protected JunitJupiterTestMeta(String testClassName, String testMethodName) {
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

    public JunitJupiterTestMeta(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
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


    private static String getTestMethodName(TestInfo testInfo) {
        return testInfo.getTestMethod().map(Method::getName).orElseGet(testInfo::getDisplayName);
    }

    private static String getRequiredClassName(TestInfo testInfo) {
        return testInfo.getTestClass().orElseThrow(() -> new IllegalStateException("Cannot get class from Testinfo, custom implementation of TestMetaInformation required!")).getName();
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
