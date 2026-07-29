package com.github.karsaig.approvalcrest;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * {@code fileMatcherSourceRoot} has to reach both JUnit 4 routes identically, so that adopting
 * {@link Junit4DesciptionWatcher} does not change where approved files live.
 */
public class SourceRootPerRouteTest {

    private static final String PACKAGE_PATH = "com/github/karsaig/approvalcrest";

    @Rule
    public Junit4DesciptionWatcher watcher = new Junit4DesciptionWatcher();

    @Before
    @After
    public void clearProperties() {
        System.clearProperty("fileMatcherSourceRoot");
        System.clearProperty("fmSourceRoot");
    }

    private static Path expected(String sourceRoot) {
        return Paths.get(sourceRoot).resolve(PACKAGE_PATH);
    }

    @Test
    public void bothRoutesDefaultToSrcTestJava() {
        assertEquals(expected("src/test/java"), new Junit4TestMeta().getTestClassPath());
        assertEquals(expected("src/test/java"),
                new Junit4DescriptionBasedTestMeta(watcher.getDescription()).getTestClassPath());
    }

    @Test
    public void bothRoutesHonourTheCanonicalProperty() {
        System.setProperty("fileMatcherSourceRoot", "src/it/java");

        assertEquals("stack route must honour the property",
                expected("src/it/java"), new Junit4TestMeta().getTestClassPath());
        assertEquals("Description route must honour it identically",
                expected("src/it/java"),
                new Junit4DescriptionBasedTestMeta(watcher.getDescription()).getTestClassPath());
    }

    @Test
    public void bothRoutesHonourTheAlias() {
        System.setProperty("fmSourceRoot", "src/it/java");

        assertEquals(expected("src/it/java"), new Junit4TestMeta().getTestClassPath());
        assertEquals(expected("src/it/java"),
                new Junit4DescriptionBasedTestMeta(watcher.getDescription()).getTestClassPath());
    }
}
