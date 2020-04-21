package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class MatcherAssertExceptionMatchingTests {

    @Test
    public void assertThrowsWithSameBeanAsShouldPassWhenExpectationIsMet() {
        //GIVEN
        IllegalStateException expected = new IllegalStateException("Test excpetion message...");
        //WHEN - THEN
        MatcherAssert.assertThrows(sameBeanAs(expected), () -> {
            throw new IllegalStateException("Test excpetion message...");
        });
    }

    @Test
    public void assertThrowsWithSameBeanAsShouldFailWhenMessageIsDifferent() {
        //GIVEN
        IllegalStateException expected = new IllegalStateException("Different excpetion message...");
        //WHEN - THEN
        Assertions.assertThrows(AssertionFailedError.class, () -> {
            MatcherAssert.assertThrows(sameBeanAs(expected), () -> {
                throw new IllegalStateException("Test excpetion message...");
            });
        }, "0x1.detailMessage\n" +
                "Expected: Different excpetion message...\n" +
                "     got: Test excpetion message...");
    }

    @Test
    public void assertThrowsWithSameBeanAsShouldFailWhenNoExceptionThrown() {
        //GIVEN
        IllegalStateException expected = new IllegalStateException("Different excpetion message...");
        //WHEN - THEN
        Assertions.assertThrows(AssertionFailedError.class, () -> {
            MatcherAssert.assertThrows(sameBeanAs(expected), () -> {
            });
        }, "Expected exception but no exception was thrown!");
    }

    @Test
    public void assertThrowsWithSameJsonAsShouldPassWhenExpectationIsMet() {
        MatcherAssert.assertThrows(sameJsonAsApproved(), () -> {
            throw new IllegalStateException("Test excpetion message...");
        });
    }

    @Test
    public void assertThrowsWithSameJsonAsShouldFailWhenMessageIsDifferent() {
        Assertions.assertThrows(AssertionFailedError.class, () -> {
            MatcherAssert.assertThrows(sameJsonAsApproved(), () -> {
                throw new IllegalStateException("Test excpetion message...");
            });
        }, "0x1.detailMessage\n" +
                "Expected: Different excpetion message...\n" +
                "     got: Test excpetion message...");
    }

    @Test
    public void assertThrowsWithSameJsonAsShouldFailWhenNoExceptionThrown() {
        Assertions.assertThrows(AssertionFailedError.class, () -> {
            MatcherAssert.assertThrows(sameJsonAsApproved(), () -> {
            });
        }, "Expected exception but no exception was thrown!");
    }
}
