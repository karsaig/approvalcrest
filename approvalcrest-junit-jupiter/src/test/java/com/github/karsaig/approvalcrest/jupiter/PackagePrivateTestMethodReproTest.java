package com.github.karsaig.approvalcrest.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Reproducing test for review finding 2.3.
 *
 * <p>{@code AbstractTestMetaBase.findMethod} uses {@code Class#getMethods()}, which returns only
 * public members. JUnit Jupiter permits - and its own user guide demonstrates - package-private
 * test methods, so those frames are invisible to the stack walk and approvalcrest cannot work out
 * which test it is running.
 *
 * <p>Correct behaviour: both methods below should locate their own frame.
 */
public class PackagePrivateTestMethodReproTest {

    /** Baseline: a public @Test method is found, so the mechanism works in general. */
    @Test
    public void publicTestMethodIsFound() {
        StackTraceElement result =
                JunitJupiterTestMeta.getTestStackTraceElement(Thread.currentThread().getStackTrace());

        assertNotNull(result, "a public @Test frame is located");
        assertEquals("publicTestMethodIsFound", result.getMethodName());
    }

    /**
     * A package-private @Test method - perfectly legal in Jupiter, and this very method is being
     * executed by the Jupiter engine right now.
     */
    @Test
    void packagePrivateTestMethodIsNotFound() {
        StackTraceElement result =
                JunitJupiterTestMeta.getTestStackTraceElement(Thread.currentThread().getStackTrace());

        // BUG: getMethods() cannot see this method, so no frame matches.
        assertNull(result,
                "BUG 2.3: package-private @Test frame not located - getMethods() only returns public members");
    }

    /**
     * The user-visible consequence: constructing the test meta from a package-private test method
     * blows up instead of resolving the approved-file path.
     */
    @Test
    void packagePrivateTestMethodCannotConstructTestMeta() {
        NullPointerException e = assertThrows(NullPointerException.class, JunitJupiterTestMeta::new,
                "BUG 2.3: package-private @Test method cannot construct JunitJupiterTestMeta");

        assertTrue(e.getMessage() != null && e.getMessage().contains("Cannot determine test method"),
                "fails with the 'Cannot determine test method' guidance, message was: " + e.getMessage());
    }

    /** Same construction from a public test method succeeds, isolating visibility as the cause. */
    @Test
    public void publicTestMethodCanConstructTestMeta() {
        JunitJupiterTestMeta meta = new JunitJupiterTestMeta();

        assertEquals("publicTestMethodCanConstructTestMeta", meta.testMethodName());
    }
}
