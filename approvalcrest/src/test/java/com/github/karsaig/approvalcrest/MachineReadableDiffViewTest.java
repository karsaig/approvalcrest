package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.ComparisonFailure;
import org.junit.Test;

import com.github.karsaig.approvalcrest.testdata.Bean;

/**
 * Verifies that the IDE diff view still works in both normal and machine-readable modes for JUnit 4.
 * <p>
 * {@link ComparisonFailure#getExpected()} and {@link ComparisonFailure#getActual()} must always be
 * populated so IDEs can show the diff panel.
 * <p>
 * In machine-readable mode a {@link CleanMessageComparisonFailure} is thrown instead, which returns
 * the original structured message from {@link ComparisonFailure#getMessage()} without the
 * {@code ComparisonCompactor} suffix (e.g. {@code expected:<[...]> but was:<[...]>}).
 */
public class MachineReadableDiffViewTest {

    @Test
    public void normalModePreservesExpectedAndActualForIdeDiffView() {
        Bean expected = bean().string("value1").integer(1).build();
        Bean actual = bean().string("value2").integer(2).build();

        ComparisonFailure error = null;
        try {
            assertThat(actual, sameBeanAs(expected));
            fail("Expected ComparisonFailure");
        } catch (ComparisonFailure e) {
            error = e;
        }

        assertNotNull("getExpected() must be non-null for IDE diff view", error.getExpected());
        assertNotNull("getActual() must be non-null for IDE diff view", error.getActual());
    }

    @Test
    public void machineReadableModePreservesExpectedAndActualAndCleanMessage() {
        Bean expected = bean().string("value1").integer(1).build();
        Bean actual = bean().string("value2").integer(2).build();

        ComparisonFailure error = null;
        try {
            assertThat(actual, sameBeanAs(expected).withMachineReadableOutput());
            fail("Expected ComparisonFailure");
        } catch (ComparisonFailure e) {
            error = e;
        }

        assertNotNull("getExpected() must be non-null for IDE diff view", error.getExpected());
        assertNotNull("getActual() must be non-null for IDE diff view", error.getActual());
        assertTrue("getMessage() should contain machine-readable expected block",
                error.getMessage().contains("=== EXPECTED (full) ==="));
        assertFalse("getMessage() should not be polluted by ComparisonCompactor",
                error.getMessage().contains("expected:<"));
    }
}
