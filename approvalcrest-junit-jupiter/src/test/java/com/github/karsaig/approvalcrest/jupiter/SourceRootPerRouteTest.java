package com.github.karsaig.approvalcrest.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * {@code fileMatcherSourceRoot} has to reach both routes identically. If only one honoured it,
 * a project that moved its tests would get approved files in two different places depending on
 * whether a given test happened to take a {@code TestInfo} parameter.
 *
 * <p>{@code SourceRootConfigTest} in approvalcrest-core covers the property parsing itself; this
 * covers that the JUnit 5 routes are actually wired to it.
 */
public class SourceRootPerRouteTest {

    private static final String PACKAGE_PATH = "com/github/karsaig/approvalcrest/jupiter";

    @BeforeEach
    @AfterEach
    void clearProperties() {
        System.clearProperty("fileMatcherSourceRoot");
        System.clearProperty("fmSourceRoot");
    }

    private static Path expected(String sourceRoot) {
        return Paths.get(sourceRoot).resolve(PACKAGE_PATH);
    }

    @Test
    void bothRoutesDefaultToSrcTestJava(TestInfo testInfo) {
        assertEquals(expected("src/test/java"), new JunitJupiterTestMeta().getTestClassPath());
        assertEquals(expected("src/test/java"), new Junit5InfoBasedTestMeta(testInfo).getTestClassPath());
    }

    @Test
    void bothRoutesHonourTheCanonicalProperty(TestInfo testInfo) {
        System.setProperty("fileMatcherSourceRoot", "src/it/java");

        assertEquals(expected("src/it/java"), new JunitJupiterTestMeta().getTestClassPath(),
                "stack route must honour the property");
        assertEquals(expected("src/it/java"), new Junit5InfoBasedTestMeta(testInfo).getTestClassPath(),
                "TestInfo route must honour it identically");
    }

    @Test
    void bothRoutesHonourTheAlias(TestInfo testInfo) {
        System.setProperty("fmSourceRoot", "src/it/java");

        assertEquals(expected("src/it/java"), new JunitJupiterTestMeta().getTestClassPath());
        assertEquals(expected("src/it/java"), new Junit5InfoBasedTestMeta(testInfo).getTestClassPath());
    }
}
