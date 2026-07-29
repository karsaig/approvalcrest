package com.github.karsaig.approvalcrest.testng;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * {@code fileMatcherSourceRoot} has to reach both TestNG routes identically, so that whether a test
 * happens to accept a {@link Method} parameter does not change where its approved file lives.
 */
public class SourceRootPerRouteTest {

    private static final String PACKAGE_PATH = "com/github/karsaig/approvalcrest/testng";

    @BeforeMethod
    @AfterMethod
    public void clearProperties() {
        System.clearProperty("fileMatcherSourceRoot");
        System.clearProperty("fmSourceRoot");
    }

    private static Path expected(String sourceRoot) {
        return Paths.get(sourceRoot).resolve(PACKAGE_PATH);
    }

    @Test
    public void bothRoutesDefaultToSrcTestJava(Method testMethod) {
        assertEquals(new TestNgTestMeta().getTestClassPath(), expected("src/test/java"));
        assertEquals(new TestNgMethodBasedTestMeta(testMethod).getTestClassPath(), expected("src/test/java"));
    }

    @Test
    public void bothRoutesHonourTheCanonicalProperty(Method testMethod) {
        System.setProperty("fileMatcherSourceRoot", "src/it/java");

        assertEquals(new TestNgTestMeta().getTestClassPath(), expected("src/it/java"),
                "stack route must honour the property");
        assertEquals(new TestNgMethodBasedTestMeta(testMethod).getTestClassPath(), expected("src/it/java"),
                "Method route must honour it identically");
    }

    @Test
    public void bothRoutesHonourTheAlias(Method testMethod) {
        System.setProperty("fmSourceRoot", "src/it/java");

        assertEquals(new TestNgTestMeta().getTestClassPath(), expected("src/it/java"));
        assertEquals(new TestNgMethodBasedTestMeta(testMethod).getTestClassPath(), expected("src/it/java"));
    }
}
