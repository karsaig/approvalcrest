package com.github.karsaig.approvalcrest.testng;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

/**
 * Both TestNG routes name the class the test method is <em>declared</em> in - the stack frame does,
 * and so does {@code Method#getDeclaringClass()}. For a test inherited from an abstract base that is
 * never the class actually running, so every subclass would resolve to the same approved file.
 * Both routes refuse rather than guess.
 */
public class InheritedTestMethodTest {

    public abstract static class AbstractContractTest {

        @Test
        public void inheritedTest(Method testMethod) {
            assertEquals(testMethod.getDeclaringClass().getName(), AbstractContractTest.class.getName(),
                    "the injected Method reports the declaring class, not the subclass");

            assertRefuses(TestNgTestMeta::new, "stack-trace route");
            assertRefuses(() -> new TestNgMethodBasedTestMeta(testMethod), "Method route");
        }

        private void assertRefuses(Runnable construction, String route) {
            try {
                construction.run();
                fail(route + " must refuse to resolve an inherited test method");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("abstract class " + AbstractContractTest.class.getName()),
                        route + " error must name the offending class, was: " + e.getMessage());
            }
        }
    }

    public static class FirstImplementationTest extends AbstractContractTest {
    }

    public static class SecondImplementationTest extends AbstractContractTest {
    }

    /** A concrete test class is unaffected by the guard. */
    @Test
    public void concreteTestClassIsUnaffected(Method testMethod) {
        assertEquals(new TestNgTestMeta().testClassName(), InheritedTestMethodTest.class.getName());
        assertEquals(new TestNgMethodBasedTestMeta(testMethod).testClassName(),
                InheritedTestMethodTest.class.getName());
    }
}
