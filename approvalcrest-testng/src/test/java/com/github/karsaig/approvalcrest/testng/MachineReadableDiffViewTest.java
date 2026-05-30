package com.github.karsaig.approvalcrest.testng;

import static com.github.karsaig.approvalcrest.testng.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.opentest4j.AssertionFailedError;
import org.testng.annotations.Test;

import com.github.karsaig.approvalcrest.testdata.Bean;

/**
 * Verifies that the IDE diff view still works when machine-readable output is active for TestNG.
 * {@link AssertionFailedError#isExpectedDefined()} and {@link AssertionFailedError#isActualDefined()}
 * must be true so that IDEs can show the diff panel, while {@code getMessage()} carries the
 * clean machine-readable text only (no ComparisonCompactor pollution).
 */
public class MachineReadableDiffViewTest {

    @Test
    public void machineReadableModePreservesExpectedAndActualForIdeDiffView() {
        Bean expected = bean().string("value1").integer(1).build();
        Bean actual = bean().string("value2").integer(2).build();

        AssertionFailedError error = null;
        try {
            assertThat(actual, sameBeanAs(expected).withMachineReadableOutput());
            fail("Expected AssertionFailedError");
        } catch (AssertionFailedError e) {
            error = e;
        }

        assertTrue(error.isExpectedDefined(), "Expected must be defined for IDE diff view");
        assertTrue(error.isActualDefined(), "Actual must be defined for IDE diff view");
        assertNotNull(error.getExpected().getValue(), "Expected value must be non-null");
        assertNotNull(error.getActual().getValue(), "Actual value must be non-null");
        assertTrue(error.getMessage().contains("=== EXPECTED (full) ==="),
                "getMessage() should contain machine-readable expected block");
        assertFalse(error.getMessage().contains("expected:<"),
                "getMessage() should not be polluted by ComparisonCompactor");
    }

    @Test
    public void normalModePreservesExpectedAndActualForIdeDiffView() {
        Bean expected = bean().string("value1").integer(1).build();
        Bean actual = bean().string("value2").integer(2).build();

        AssertionFailedError error = null;
        try {
            assertThat(actual, sameBeanAs(expected));
            fail("Expected AssertionFailedError");
        } catch (AssertionFailedError e) {
            error = e;
        }

        assertTrue(error.isExpectedDefined(), "Expected must be defined for IDE diff view");
        assertTrue(error.isActualDefined(), "Actual must be defined for IDE diff view");
        assertNotNull(error.getExpected().getValue(), "Expected value must be non-null");
        assertNotNull(error.getActual().getValue(), "Actual value must be non-null");
    }
}
