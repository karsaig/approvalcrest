package com.github.karsaig.approvalcrest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that each property alias for {@link FileMatcherConfig} is resolved
 * identically to its canonical system property name, and that setting both to
 * conflicting values throws an {@link IllegalStateException}.
 */
public class FileMatcherConfigPropertyAliasTest {

    @BeforeEach
    void clearAllProperties() {
        System.clearProperty("jsonMatcherUpdateInPlace");
        System.clearProperty("fileMatcherUpdateInPlace");
        System.clearProperty("fMUInPlace");
        System.clearProperty("fileMatcherPassOnCreate");
        System.clearProperty("fMPOnCreate");
        System.clearProperty("buildFileIndex");
        System.clearProperty("bFIndex");
        System.clearProperty("useApprovedDirectory");
        System.clearProperty("uADirectory");
        System.clearProperty("sortInputFile");
        System.clearProperty("sIFile");
        System.clearProperty("fileMatcherStrictFileMatching");
        System.clearProperty("fMStrictMatching");
    }

    @AfterEach
    void restoreAllProperties() {
        clearAllProperties();
    }

    @Test
    public void fMUInPlaceAliasEnablesOverwriteInPlace() {
        System.setProperty("fMUInPlace", "true");
        assertTrue(new FileMatcherConfig().isOverwriteInPlaceEnabled());
    }

    @Test
    public void fMPOnCreateAliasEnablesPassOnCreate() {
        System.setProperty("fMPOnCreate", "true");
        assertTrue(new FileMatcherConfig().isPassOnCreateEnabled());
    }

    @Test
    public void bFIndexAliasEnablesBuildIndex() {
        System.setProperty("bFIndex", "true");
        assertTrue(new FileMatcherConfig().isBuildIndex());
    }

    @Test
    public void uADirectoryAliasEnablesApprovedDirectory() {
        System.setProperty("uADirectory", "true");
        assertTrue(new FileMatcherConfig().isApprovedDirectory());
    }

    @Test
    public void sIFileAliasEnablesSortInputFile() {
        System.setProperty("sIFile", "true");
        assertTrue(new FileMatcherConfig().isSortInputFile());
    }

    @Test
    public void fMStrictMatchingAliasConfiguresStrictFileMatching() {
        System.setProperty("fMStrictMatching", "false");
        assertFalse(new FileMatcherConfig().isStrictFileMatching());
    }

    @Test
    public void conflictingCanonicalAndAliasThrowsIllegalStateException() {
        System.setProperty("fileMatcherUpdateInPlace", "true");
        System.setProperty("fMUInPlace", "false");
        assertThrows(IllegalStateException.class, FileMatcherConfig::new);
    }

    @Test
    public void canonicalAndAliasAgreementDoesNotThrow() {
        System.setProperty("fileMatcherUpdateInPlace", "true");
        System.setProperty("fMUInPlace", "true");
        assertTrue(new FileMatcherConfig().isOverwriteInPlaceEnabled());
    }
}
