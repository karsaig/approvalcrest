package com.github.karsaig.approvalcrest.testng;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

public class TestNgTestMetaStackTraceTest {

    @Test
    public void returnsNullForEmptyStackTrace() {
        assertNull(TestNgTestMeta.getTestStackTraceElement(new StackTraceElement[0]));
    }

    @Test
    public void returnsNullWhenNoFrameHasTestAnnotation() {
        StackTraceElement[] stackTrace = {
            new StackTraceElement("com.example.SomeHelper", "helperMethod", "SomeHelper.java", 10),
            new StackTraceElement("com.example.AnotherClass", "anotherMethod", "AnotherClass.java", 20)
        };
        assertNull(TestNgTestMeta.getTestStackTraceElement(stackTrace));
    }

    @Test
    public void returnsNullForNonExistentClass() {
        StackTraceElement[] stackTrace = {
            new StackTraceElement("com.example.DoesNotExist", "someMethod", "DoesNotExist.java", 1)
        };
        assertNull(TestNgTestMeta.getTestStackTraceElement(stackTrace));
    }

    @Test
    public void returnsTestFrameWhenTestNgAnnotationPresent() {
        StackTraceElement[] stackTrace = {
            new StackTraceElement("com.example.SomeHelper", "helperMethod", "SomeHelper.java", 10),
            new StackTraceElement(TestNgAnnotatedTestHelper.class.getName(), "annotatedTestMethod", "TestNgAnnotatedTestHelper.java", 5)
        };
        StackTraceElement result = TestNgTestMeta.getTestStackTraceElement(stackTrace);
        assertNotNull(result, "Expected a test frame to be found");
        assertEquals(result.getMethodName(), "annotatedTestMethod");
    }

    @Test
    public void ignoresMethodsWithoutTestNgAnnotation() {
        StackTraceElement[] stackTrace = {
            new StackTraceElement(TestNgAnnotatedTestHelper.class.getName(), "notAnnotatedMethod", "TestNgAnnotatedTestHelper.java", 10)
        };
        assertNull(TestNgTestMeta.getTestStackTraceElement(stackTrace));
    }

    @Test
    public void findsCurrentTestMethodFromLiveStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement result = TestNgTestMeta.getTestStackTraceElement(stackTrace);
        assertNotNull(result, "Expected current test method to be found");
        assertEquals(result.getMethodName(), "findsCurrentTestMethodFromLiveStackTrace");
    }
}
