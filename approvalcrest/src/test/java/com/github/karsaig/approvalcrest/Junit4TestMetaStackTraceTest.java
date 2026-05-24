package com.github.karsaig.approvalcrest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Junit4TestMetaStackTraceTest {

    @Test
    public void returnsNullForEmptyStackTrace() {
        assertNull(Junit4TestMeta.getTestStackTraceElement(new StackTraceElement[0]));
    }

    @Test
    public void returnsNullWhenNoFrameHasTestAnnotation() {
        StackTraceElement[] stackTrace = {
            new StackTraceElement("com.example.SomeHelper", "helperMethod", "SomeHelper.java", 10),
            new StackTraceElement("com.example.AnotherClass", "anotherMethod", "AnotherClass.java", 20)
        };
        assertNull(Junit4TestMeta.getTestStackTraceElement(stackTrace));
    }

    @Test
    public void returnsNullForNonExistentClass() {
        StackTraceElement[] stackTrace = {
            new StackTraceElement("com.example.DoesNotExist", "someMethod", "DoesNotExist.java", 1)
        };
        assertNull(Junit4TestMeta.getTestStackTraceElement(stackTrace));
    }

    @Test
    public void returnsTestFrameWhenTestAnnotationPresent() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement result = Junit4TestMeta.getTestStackTraceElement(stackTrace);
        assertNotNull("Expected a test frame to be found", result);
        assertEquals("returnsTestFrameWhenTestAnnotationPresent", result.getMethodName());
    }
}
