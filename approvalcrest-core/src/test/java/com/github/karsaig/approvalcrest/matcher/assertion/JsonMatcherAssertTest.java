package com.github.karsaig.approvalcrest.matcher.assertion;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.PreBuilt;

public class JsonMatcherAssertTest extends AbstractFileMatcherTest {

    @Test
    void assertThatShouldDoNothingWhenJsonMatcherForBeanMatchesWithoutReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitives();
        String expected = PreBuilt.getBeanWithPrimitivesAsJsonString();

        runJsonMatcherTestWithDummyTestInfo(expected, testInfo -> {
            TEST_ASSERT_IMPl.assertThat(null, testInput, MATCHER_FACTORY.jsonMatcher(testInfo, getDefaultFileMatcherConfig()),
                    (s, cd) -> {
                        throw new NotImplementedException("Shouldn't throw this");
                    });
        });
    }

    @Test
    void assertThatShouldDoNothingWhenJsonMatcherForBeanMatchesWithReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitives();
        String expected = PreBuilt.getBeanWithPrimitivesAsJsonString();

        runJsonMatcherTestWithDummyTestInfo(expected, testInfo -> {
            TEST_ASSERT_IMPl.assertThat("This is the reason", testInput, MATCHER_FACTORY.jsonMatcher(testInfo, getDefaultFileMatcherConfig()),
                    (s, cd) -> {
                        throw new NotImplementedException("Shouldn't throw this");
                    });
        });
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenJsonMatcherForBeanDoesNotMatchWithoutReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitivesBuilder().build();
        String expected = "{\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanShort\": 1,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanBoolean\": true\n" +
                "}";

        runJsonMatcherTestWithDummyTestInfo(expected, testInfo -> {
            AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                    () -> TEST_ASSERT_IMPl.assertThat(null, testInput, MATCHER_FACTORY.jsonMatcher(testInfo, getDefaultFileMatcherConfig()),
                            comparisonDescriptionHandler()));

            Assertions.assertEquals("Expected file 4ac405/11b2ef-approved.json\n" +
                    "beanLong\n" +
                    "Expected: 13\n" +
                    "     got: 6\n", thrown.getMessage());


            Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation());
        });
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenJsonMatcherForBeanMultipleDifferenceDoesNotMatchWithoutReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitivesBuilder().build();
        String expected = "{\n" +
                "  \"beanBoolean\": true,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanInteger\": 42,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanShort\": 1\n" +
                "}";

        runJsonMatcherTestWithDummyTestInfo(expected, testInfo -> {
            AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                    () -> TEST_ASSERT_IMPl.assertThat(null, testInput, MATCHER_FACTORY.jsonMatcher(testInfo, getDefaultFileMatcherConfig()),
                            comparisonDescriptionHandler()));

            Assertions.assertEquals("Expected file 4ac405/11b2ef-approved.json\n" +
                    "beanInteger\n" +
                    "Expected: 42\n" +
                    "     got: 4\n" +
                    " ; beanLong\n" +
                    "Expected: 13\n" +
                    "     got: 6\n", thrown.getMessage());


            Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation());
        });
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenJsonMatcherForBeanDoesNotMatchWithReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitivesBuilder().build();
        String expected = "{\n" +
                "  \"beanBoolean\": true,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanShort\": 1\n" +
                "}";

        runJsonMatcherTestWithDummyTestInfo(expected, testInfo -> {
            AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                    () -> TEST_ASSERT_IMPl.assertThat("This is the reason", testInput, MATCHER_FACTORY.jsonMatcher(testInfo, getDefaultFileMatcherConfig()),
                            comparisonDescriptionHandler()));

            Assertions.assertEquals("This is the reason\n" +
                    "Expected file 4ac405/11b2ef-approved.json\n" +
                    "beanLong\n" +
                    "Expected: 13\n" +
                    "     got: 6\n", thrown.getMessage());


            Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation());
        });
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenJsonMatcherForJsonStringDoesNotMatchWithReason() {
        String testInput = "{\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanShort\": 1,\n" +
                "  \"beanLong\": 6,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanBoolean\": true\n" +
                "}";
        ;
        String expected = "{\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanShort\": 1,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanBoolean\": true\n" +
                "}";

        runJsonMatcherTestWithDummyTestInfo(expected, testInfo -> {
            AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                    () -> TEST_ASSERT_IMPl.assertThat("This is the reason", testInput, MATCHER_FACTORY.jsonMatcher(testInfo, getDefaultFileMatcherConfig()),
                            comparisonDescriptionHandler()));

            Assertions.assertEquals("This is the reason\n" +
                    "Expected file 4ac405/11b2ef-approved.json\n" +
                    "beanLong\n" +
                    "Expected: 13\n" +
                    "     got: 6\n", thrown.getMessage());


            Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation());
        });
    }
}
