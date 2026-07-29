package com.github.karsaig.approvalcrest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;

/**
 * A test method inherited from an abstract base is reported by the stack frame as belonging to the
 * base, so every subclass would resolve to the same approved file. The stack route refuses rather
 * than guess; the Description route, which knows the concrete class, keeps working.
 */
public class InheritedTestMethodTest {

    public abstract static class AbstractContractTest {

        @Rule
        public Junit4DesciptionWatcher watcher = new Junit4DesciptionWatcher();

        @Test
        public void inheritedTest() {
            try {
                new Junit4TestMeta();
                fail("the stack route must refuse to resolve an inherited test method");
            } catch (IllegalStateException e) {
                assertTrue("the error must name the offending class, was: " + e.getMessage(),
                        e.getMessage().contains("abstract class " + AbstractContractTest.class.getName()));
                assertTrue("the error must say how to fix it, was: " + e.getMessage(),
                        e.getMessage().contains("Junit4DesciptionWatcher"));
            }

            Junit4DescriptionBasedTestMeta meta = new Junit4DescriptionBasedTestMeta(watcher.getDescription());
            assertEquals("Description resolves the concrete subclass", getClass().getName(), meta.testClassName());
            assertEquals("inheritedTest", meta.testMethodName());
        }
    }

    public static class FirstImplementationTest extends AbstractContractTest {
    }

    public static class SecondImplementationTest extends AbstractContractTest {
    }

    /** A concrete test class is unaffected by the guard. */
    @Test
    public void concreteTestClassIsUnaffected() {
        Junit4TestMeta meta = new Junit4TestMeta();

        assertEquals(InheritedTestMethodTest.class.getName(), meta.testClassName());
        assertEquals("concreteTestClassIsUnaffected", meta.testMethodName());
    }
}
