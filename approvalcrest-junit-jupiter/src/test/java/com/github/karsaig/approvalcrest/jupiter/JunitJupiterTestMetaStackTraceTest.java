package com.github.karsaig.approvalcrest.jupiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JunitJupiterTestMetaStackTraceTest {

    @Test
    public void returnsNullForEmptyStackTrace() {
        assertNull(JunitJupiterTestMeta.getTestStackTraceElement(new StackTraceElement[0]));
    }

    @Test
    public void returnsNullWhenNoFrameHasTestAnnotation() {
        StackTraceElement[] stackTrace = {
            new StackTraceElement("com.example.SomeHelper", "helperMethod", "SomeHelper.java", 10),
            new StackTraceElement("com.example.AnotherClass", "anotherMethod", "AnotherClass.java", 20)
        };
        assertNull(JunitJupiterTestMeta.getTestStackTraceElement(stackTrace));
    }

    @Test
    public void returnsNullForNonExistentClass() {
        StackTraceElement[] stackTrace = {
            new StackTraceElement("com.example.DoesNotExist", "someMethod", "DoesNotExist.java", 1)
        };
        assertNull(JunitJupiterTestMeta.getTestStackTraceElement(stackTrace));
    }

    @Test
    public void returnsTestFrameWhenTestAnnotationPresent() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement result = JunitJupiterTestMeta.getTestStackTraceElement(stackTrace);
        assertNotNull(result, "Expected a test frame to be found");
        assertEquals("returnsTestFrameWhenTestAnnotationPresent", result.getMethodName());
    }
}
