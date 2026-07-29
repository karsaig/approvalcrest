package com.github.karsaig.approvalcrest.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * A test method inherited from an abstract base is reported by the stack frame as belonging to the
 * base, not to the subclass running it. Every subclass would therefore resolve to the same approved
 * file and overwrite each other, so the stack route refuses to guess. The TestInfo route knows the
 * concrete class and keeps working.
 */
public class InheritedTestMethodTest {

    /**
     * Shared contract test - the pattern that triggers this: one test method, several subclasses.
     */
    public abstract static class AbstractContractTest {

        @Test
        void inheritedTest(TestInfo testInfo) {
            // The frame names the abstract declaring class, which cannot be the running test class.
            StackTraceElement frame =
                    JunitJupiterTestMeta.getTestStackTraceElement(Thread.currentThread().getStackTrace());
            assertEquals(AbstractContractTest.class.getName(), frame.getClassName(),
                    "the stack frame reports the declaring class, not the subclass");

            IllegalStateException e = assertThrows(IllegalStateException.class, JunitJupiterTestMeta::new,
                    "the stack route must refuse rather than resolve to the base class");
            assertTrue(e.getMessage().contains("abstract class " + AbstractContractTest.class.getName()),
                    "the error names the offending class, was: " + e.getMessage());
            assertTrue(e.getMessage().contains("TestInfo"),
                    "the error says how to fix it, was: " + e.getMessage());

            // The injected route knows which subclass is running, so it stays usable.
            Junit5InfoBasedTestMeta meta = new Junit5InfoBasedTestMeta(testInfo);
            assertEquals(getClass().getName(), meta.testClassName(),
                    "TestInfo resolves the concrete subclass");
            assertEquals("inheritedTest", meta.testMethodName());
        }
    }

    /** Two subclasses: under the old behaviour both resolved to the same approved file. */
    public static class FirstImplementationTest extends AbstractContractTest {
    }

    public static class SecondImplementationTest extends AbstractContractTest {
    }

    /** A concrete test class is unaffected - the guard must not fire for ordinary tests. */
    @Test
    void concreteTestClassIsUnaffected() {
        JunitJupiterTestMeta meta = new JunitJupiterTestMeta();

        assertEquals(InheritedTestMethodTest.class.getName(), meta.testClassName());
        assertEquals("concreteTestClassIsUnaffected", meta.testMethodName());
    }
}
