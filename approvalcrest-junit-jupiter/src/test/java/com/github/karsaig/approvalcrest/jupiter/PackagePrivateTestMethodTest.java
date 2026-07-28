package com.github.karsaig.approvalcrest.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * JUnit Jupiter permits package-private test methods, and its own user guide demonstrates them, so
 * approvalcrest has to be able to identify the running test from such a frame. The stack walk used
 * to look the method up with {@code Class#getMethods()}, which returns public members only, so a
 * package-private test could not resolve its approved-file path at all.
 */
public class PackagePrivateTestMethodTest {

    @Test
    public void publicTestMethodIsFound() {
        StackTraceElement result =
                JunitJupiterTestMeta.getTestStackTraceElement(Thread.currentThread().getStackTrace());

        assertNotNull(result, "a public @Test frame must be located");
        assertEquals("publicTestMethodIsFound", result.getMethodName());
    }

    @Test
    void packagePrivateTestMethodIsFound() {
        StackTraceElement result =
                JunitJupiterTestMeta.getTestStackTraceElement(Thread.currentThread().getStackTrace());

        assertNotNull(result, "a package-private @Test frame must be located");
        assertEquals("packagePrivateTestMethodIsFound", result.getMethodName());
    }

    @Test
    void packagePrivateTestMethodCanConstructTestMeta() {
        JunitJupiterTestMeta meta = new JunitJupiterTestMeta();

        assertEquals("packagePrivateTestMethodCanConstructTestMeta", meta.testMethodName());
    }

    @Test
    public void publicTestMethodCanConstructTestMeta() {
        JunitJupiterTestMeta meta = new JunitJupiterTestMeta();

        assertEquals("publicTestMethodCanConstructTestMeta", meta.testMethodName());
    }

    /**
     * A test method sharing its name with a non-test overload must still be identified: the
     * reflection order of same-named methods is unspecified, so picking the first match was a
     * coin flip between the real test and the overload.
     */
    @Test
    void overloadedTestMethodIsFound() {
        StackTraceElement result =
                JunitJupiterTestMeta.getTestStackTraceElement(Thread.currentThread().getStackTrace());

        assertNotNull(result, "the annotated overload must be located");
        assertEquals("overloadedTestMethodIsFound", result.getMethodName());
    }

    /** Deliberate same-named, unannotated overload of the test method above. */
    @SuppressWarnings("unused")
    void overloadedTestMethodIsFound(int notATest) {
        throw new UnsupportedOperationException("never invoked");
    }
}
