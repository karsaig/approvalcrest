package com.github.karsaig.approvalcrest.matcher.machinereadable;

import com.github.karsaig.approvalcrest.matcher.AbstractTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMatcherFactory;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives.Builder.beanWithPrimitives;
import static org.junit.jupiter.api.Assertions.*;

public class BeanMatcherMachineReadableTest extends AbstractTest {

    private static final TestMatcherFactory MATCHER_FACTORY = new TestMatcherFactory();

    @Test
    public void shouldOutputMachineReadableMessageOnMismatchWhenFluentApiEnabled() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).beanInt(99).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).beanInt(42).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        assertAll(
                () -> assertTrue(msg.contains("=== EXPECTED (full) ==="), "Should contain EXPECTED block start"),
                () -> assertTrue(msg.contains("=== END EXPECTED ==="), "Should contain EXPECTED block end"),
                () -> assertTrue(msg.contains("=== ACTUAL (full) ==="), "Should contain ACTUAL block start"),
                () -> assertTrue(msg.contains("=== END ACTUAL ==="), "Should contain ACTUAL block end"),
                () -> assertFalse(msg.contains("Approved file (expected):"), "Should NOT contain approved file path label for bean matchers"),
                () -> assertTrue(error.isExpectedDefined(), "Expected must be defined for IDE diff view"),
                () -> assertTrue(error.isActualDefined(), "Actual must be defined for IDE diff view"),
                () -> assertNotNull(error.getExpected().getValue(), "Expected value must be non-null for IDE diff view"),
                () -> assertNotNull(error.getActual().getValue(), "Actual value must be non-null for IDE diff view")
        );
    }

    @Test
    public void shouldOutputDiffOnMismatchWhenMachineReadableDisabled() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected);

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        assertAll(
                () -> assertFalse(msg.contains("=== EXPECTED (full) ==="), "Should NOT contain machine-readable EXPECTED block"),
                () -> assertFalse(msg.contains("=== ACTUAL (full) ==="), "Should NOT contain machine-readable ACTUAL block")
        );
    }
}
