package com.github.karsaig.approvalcrest.jupiter;

import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

/**
 * {@code assertThrows} catches {@link Throwable}, so anything the executable throws is treated as
 * the exception under test. Two categories can never be that, and matching them hides what really
 * happened: a skip signal, and a JVM-level error.
 */
public class AssertThrowsRethrowTest {

    /**
     * An assumption failure means "skip this test". Matching it against the expected exception
     * turns a skip into a pass or a confusing failure.
     */
    @Test
    public void assumptionFailureIsRethrownRatherThanMatched() {
        TestAbortedException aborted = new TestAbortedException("assumption not met");

        TestAbortedException thrown = assertThrows(TestAbortedException.class,
                () -> MatcherAssert.assertThrows(sameBeanAs(new IllegalStateException("expected")), () -> {
                    throw aborted;
                }));

        assertSame(aborted, thrown, "the skip signal must pass through untouched");
    }

    /** A VM-level error is rethrown as-is, not wrapped, so nothing is allocated to report it. */
    @Test
    public void virtualMachineErrorIsRethrownUnwrapped() {
        StackOverflowError error = new StackOverflowError();

        StackOverflowError thrown = assertThrows(StackOverflowError.class,
                () -> MatcherAssert.assertThrows(sameBeanAs(new IllegalStateException("expected")), () -> {
                    throw error;
                }));

        assertSame(error, thrown, "the error must pass through unwrapped");
    }

    /**
     * The constraint that shapes the fix: asserting that a matcher fails is a supported use, and a
     * matcher failure is an AssertionError, so those must still be matched rather than rethrown.
     */
    @Test
    public void assertionErrorIsStillMatched() {
        AssertionError expected = new AssertionError("boom");

        Throwable returned = MatcherAssert.assertThrows(sameBeanAs(new AssertionError("boom")),
                () -> {
                    throw expected;
                });

        assertSame(expected, returned, "an AssertionError is a legitimate exception under test");
    }

    /** Ordinary exceptions are unaffected. */
    @Test
    public void ordinaryExceptionIsStillMatchedAndReturned() {
        IllegalStateException expected = new IllegalStateException("boom");

        Throwable returned = MatcherAssert.assertThrows(sameBeanAs(new IllegalStateException("boom")),
                () -> {
                    throw expected;
                });

        assertSame(expected, returned);
        assertEquals("boom", returned.getMessage());
    }
}
