package com.github.karsaig.approvalcrest.testng;

import com.github.karsaig.approvalcrest.matcher.AbstractTestMetaBase;

import java.lang.reflect.Method;
import java.nio.file.Path;

public class TestNgMethodBasedTestMeta extends AbstractTestMetaBase {

    public TestNgMethodBasedTestMeta(Method testMethod) {
        super(testMethod.getDeclaringClass().getName(), testMethod.getName());
    }

    public TestNgMethodBasedTestMeta(Path testClassPath, String testClassName, String testMethodName, Path approvedDirectory) {
        super(testClassPath, testClassName, testMethodName, approvedDirectory);
    }
}
