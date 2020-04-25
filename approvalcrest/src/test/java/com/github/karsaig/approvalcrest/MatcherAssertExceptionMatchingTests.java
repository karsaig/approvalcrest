package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import junit.framework.AssertionFailedError;

public class MatcherAssertExceptionMatchingTests {

    @SuppressWarnings("deprecation")
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
        expectedException.expect(ComparisonFailure.class);
        expectedException.expectMessage("0x1.detailMessage\n" +
                "Expected: Different excpetion message...\n" +
                "     got: Test excpetion message...");
        //WHEN - THEN
        MatcherAssert.assertThrows(sameBeanAs(expected), () -> {
            throw new IllegalStateException("Test excpetion message...");
        });
    }

    @Test
    public void assertThrowsWithSameBeanAsShouldFailWhenNoExceptionThrown() {
        //GIVEN
        IllegalStateException expected = new IllegalStateException("Different excpetion message...");
        expectedException.expect(AssertionFailedError.class);
        expectedException.expectMessage("Expected exception but no exception was thrown!");
        //WHEN - THEN
        MatcherAssert.assertThrows(sameBeanAs(expected), () -> {
        });
    }

    @Test
    public void assertThrowsWithSameJsonAsShouldPassWhenExpectationIsMet() {
        //GIVEN
        //WHEN - THEN
        MatcherAssert.assertThrows(sameJsonAsApproved(), () -> {
            throw new IllegalStateException("Test excpetion message...");
        });
    }

    @Test
    public void assertThrowsWithSameJsonAsShouldFailWhenMessageIsDifferent() {
        //GIVEN
        expectedException.expect(ComparisonFailure.class);
        expectedException.expectMessage("0x1.detailMessage\n" +
                "Expected: Different excpetion message...\n" +
                "     got: Test excpetion message...");
        //WHEN - THEN
        MatcherAssert.assertThrows(sameJsonAsApproved(), () -> {
            throw new IllegalStateException("Test excpetion message...");
        });
    }

    @Test
    public void assertThrowsWithSameJsonAsShouldFailWhenNoExceptionThrown() {
        //GIVEN
        expectedException.expect(AssertionFailedError.class);
        expectedException.expectMessage("Expected exception but no exception was thrown!");
        //WHEN - THEN
        MatcherAssert.assertThrows(sameJsonAsApproved(), () -> {
        });
    }
}
