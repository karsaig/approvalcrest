package com.github.karsaig.approvalcrest.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the test source root used to locate approved files is configurable via the
 * {@code fileMatcherSourceRoot} property (alias {@code fmSourceRoot}) and defaults to
 * {@code src/test/java}.
 */
public class SourceRootConfigTest {

    private static final String CLASS_NAME = "com.foo.BarTest";

    @BeforeEach
    @AfterEach
    void clearProperties() {
        System.clearProperty("fileMatcherSourceRoot");
        System.clearProperty("fmSourceRoot");
    }

    private static Path expectedPackagePath(String sourceRoot) {
        return Paths.get(sourceRoot).resolve("com").resolve("foo");
    }

    @Test
    public void defaultsToSrcTestJava() {
        assertEquals(expectedPackagePath("src/test/java"), AbstractTestMetaBase.buildClassPath(CLASS_NAME));
    }

    @Test
    public void canonicalPropertyConfiguresSourceRoot() {
        System.setProperty("fileMatcherSourceRoot", "src" + File.separator + "it" + File.separator + "java");
        assertEquals(expectedPackagePath("src/it/java"), AbstractTestMetaBase.buildClassPath(CLASS_NAME));
    }

    @Test
    public void aliasConfiguresSourceRoot() {
        System.setProperty("fmSourceRoot", "src" + File.separator + "it" + File.separator + "java");
        assertEquals(expectedPackagePath("src/it/java"), AbstractTestMetaBase.buildClassPath(CLASS_NAME));
    }

    @Test
    public void conflictingCanonicalAndAliasThrows() {
        System.setProperty("fileMatcherSourceRoot", "src" + File.separator + "it" + File.separator + "java");
        System.setProperty("fmSourceRoot", "src" + File.separator + "test" + File.separator + "kotlin");
        assertThrows(IllegalStateException.class, () -> AbstractTestMetaBase.buildClassPath(CLASS_NAME));
    }

    @Test
    public void forwardSlashesAndMissingTrailingSeparatorAreNormalized() {
        System.setProperty("fileMatcherSourceRoot", "src/test/kotlin");
        assertEquals(expectedPackagePath("src/test/kotlin"), AbstractTestMetaBase.buildClassPath(CLASS_NAME));
    }
}
