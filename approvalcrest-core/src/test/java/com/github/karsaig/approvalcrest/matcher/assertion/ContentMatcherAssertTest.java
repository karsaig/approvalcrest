package com.github.karsaig.approvalcrest.matcher.assertion;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.PreBuilt;

public class ContentMatcherAssertTest extends AbstractFileMatcherTest {

    @Test
    void assertThatShouldThrowWhenContentMatcherForNullMatchesWithoutReason() {
        String testInput = null;
        String expected = "";

        runContentMatcherTestWithDummyTestInfo(expected, testInfo -> {
            IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> TEST_ASSERT_IMPl.assertThat(null, testInput, MATCHER_FACTORY.contentMatcher(testInfo, getDefaultFileMatcherConfig()),
                            (s, cd) -> {
                                throw new NotImplementedException("Shouldn't throw this");
                            }));

            Assertions.assertEquals("Only String content matcher is supported!", thrown.getMessage());
        });
    }

    @Test
    void assertThatShouldThrowWhenContentMatcherForBeanWithoutReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitives();
        String expected = "";

        runContentMatcherTestWithDummyTestInfo(expected, testInfo -> {
            IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> TEST_ASSERT_IMPl.assertThat(null, testInput, MATCHER_FACTORY.contentMatcher(testInfo, getDefaultFileMatcherConfig()),
                            (s, cd) -> {
                                throw new NotImplementedException("Shouldn't throw this");
                            }));

            Assertions.assertEquals("Only String content matcher is supported!", thrown.getMessage());
        });
    }

    @Test
    void assertThatShouldDoNothingWhenContentMatcherForStringMatchesWithReason() {
        String testInput = "Test input";

        runContentMatcherTestWithDummyTestInfo(testInput, testInfo -> {
            TEST_ASSERT_IMPl.assertThat("This is the reason", testInput, MATCHER_FACTORY.contentMatcher(testInfo, getDefaultFileMatcherConfig()),
                    (s, cd) -> {
                        throw new NotImplementedException("Shouldn't throw this");
                    });
        });
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenContentMatcherForStringDoesNotMatchWithoutReason() {
        String testInput = "Test input";
        String expected = "Expected differs";

        runContentMatcherTestWithDummyTestInfo(expected, testInfo -> {
            AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                    () -> TEST_ASSERT_IMPl.assertThat(null, testInput, MATCHER_FACTORY.contentMatcher(testInfo, getDefaultFileMatcherConfig()),
                            comparisonDescriptionHandler()));

            Assertions.assertEquals("Expected file 4ac405/11b2ef-approved.content\n" +
                    "Content does not match!", thrown.getMessage());


            Assertions.assertEquals(testInput, thrown.getActual().getStringRepresentation());
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation());
        });
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenContentMatcherForBeanDoesNotMatchWithReason() {
        String testInput = "Test input";
        String expected = "Expected differs";

        runContentMatcherTestWithDummyTestInfo(expected, testInfo -> {
            AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                    () -> TEST_ASSERT_IMPl.assertThat("This is a reason", testInput, MATCHER_FACTORY.contentMatcher(testInfo, getDefaultFileMatcherConfig()),
                            comparisonDescriptionHandler()));

            Assertions.assertEquals("This is a reason\n" +
                    "Expected file 4ac405/11b2ef-approved.content\n" +
                    "Content does not match!", thrown.getMessage());


            Assertions.assertEquals(testInput, thrown.getActual().getStringRepresentation());
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation());
        });
    }
}
