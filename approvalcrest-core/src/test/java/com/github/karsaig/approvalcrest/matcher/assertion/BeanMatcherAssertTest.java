package com.github.karsaig.approvalcrest.matcher.assertion;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.TestAssertImpl;
import com.github.karsaig.approvalcrest.matcher.AbstractTest;
import com.github.karsaig.approvalcrest.matcher.TestMatcherFactory;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.PreBuilt;

public class BeanMatcherAssertTest extends AbstractTest {

    private TestAssertImpl underTest = new TestAssertImpl();
    private TestMatcherFactory matcherFactory = new TestMatcherFactory();

    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForNullMatchesWithoutReason() {
        String testInput = null;

        underTest.assertThat(null, testInput, matcherFactory.beanMatcher(testInput),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }

    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForNullBeanMatchesWithoutReason() {
        BeanWithPrimitives testInput = null;
        BeanWithPrimitives expected = null;

        underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }

    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForPrimitiveMatchesWithoutReason() {
        long testInput = 42L;

        underTest.assertThat(null, testInput, matcherFactory.beanMatcher(testInput),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }

    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForStringMatchesWithoutReason() {
        String testInput = "Dummy string";

        underTest.assertThat(null, testInput, matcherFactory.beanMatcher(testInput),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }

    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForBeanMatchesWithoutReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitives();
        BeanWithPrimitives expected = PreBuilt.getBeanWithPrimitives();

        underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }

    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForNullMatchesWithReason() {
        String testInput = null;

        underTest.assertThat("This is the reason", testInput, matcherFactory.beanMatcher(testInput),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }

    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForPrimitiveMatchesWithReason() {
        long testInput = 42L;

        underTest.assertThat("This is the reason", testInput, matcherFactory.beanMatcher(testInput),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }

    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForStringMatchesWithReason() {
        String testInput = "Dummy string";

        underTest.assertThat("This is the reason", testInput, matcherFactory.beanMatcher(testInput),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }


    @Test
    void assertThatShouldDoNothingWhenBeanMatcherForBeanMatchesWithReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitives();
        BeanWithPrimitives expected = PreBuilt.getBeanWithPrimitives();

        underTest.assertThat("This is the reason", testInput, matcherFactory.beanMatcher(expected),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenBeanMatcherForPrimitiveDoesNotMatchWithoutReason() {
        long testInput = 42L;
        long expected = 43L;
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        Assertions.assertEquals("\n" +
                "Expected: <43L>\n" +
                "     but: was <42L>", thrown.getMessage());

    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenBeanMatcherForPrimitiveAndNullActualDoesNotMatchWithoutReason() {
        Long testInput = null;
        long expected = 43L;
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        Assertions.assertEquals("\n" +
                "Expected: <43L>\n" +
                "     but: was null", thrown.getMessage());

    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenBeanMatcherForPrimitiveAndNullExpectedDoesNotMatchWithoutReason() {
        long testInput = 42L;
        Long expected = null;

        AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                () -> underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                        comparisonDescriptionHandler()));

        Assertions.assertEquals("actual is not null", thrown.getMessage());


        Assertions.assertEquals("42", thrown.getActual().getStringRepresentation());
        Assertions.assertEquals("null", thrown.getExpected().getStringRepresentation());

    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenBeanMatcherForStringDoesNotMatchWithoutReason() {
        String testInput = "Dummy string";
        String expected = "Different expectation";
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        Assertions.assertEquals("\n" +
                "Expected: \"Different expectation\"\n" +
                "     but: was \"Dummy string\"", thrown.getMessage());

    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenBeanMatcherForBeanDoesNotMatchWithoutReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitivesBuilder().build();
        BeanWithPrimitives expected = PreBuilt.getBeanWithPrimitivesBuilder()
                .beanLong(13L)
                .build();

        AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                () -> underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                        comparisonDescriptionHandler()));

        Assertions.assertEquals("beanLong\n" +
                "Expected: 13\n" +
                "     got: 6\n", thrown.getMessage());


        Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
        Assertions.assertEquals("{\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanShort\": 1,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanBoolean\": true\n" +
                "}", thrown.getExpected().getStringRepresentation());
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenBeanMatcherForBeanWithMultipleDifferenceDoesNotMatchWithoutReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitivesBuilder().build();
        BeanWithPrimitives expected = PreBuilt.getBeanWithPrimitivesBuilder()
                .beanLong(13L)
                .beanInt(42)
                .build();

        AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                () -> underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                        comparisonDescriptionHandler()));

        Assertions.assertEquals("beanInteger\n" +
                "Expected: 42\n" +
                "     got: 4\n" +
                " ; beanLong\n" +
                "Expected: 13\n" +
                "     got: 6\n", thrown.getMessage());


        Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
        Assertions.assertEquals("{\n" +
                "  \"beanInteger\": 42,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanShort\": 1,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanBoolean\": true\n" +
                "}", thrown.getExpected().getStringRepresentation());
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenBeanMatcherForStringDoesNotMatchWithReason() {
        String testInput = "Dummy string";
        String expected = "Different expectation";
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat("This is a reason", testInput, matcherFactory.beanMatcher(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        Assertions.assertEquals("This is a reason\n" +
                "Expected: \"Different expectation\"\n" +
                "     but: was \"Dummy string\"", thrown.getMessage());

    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenBeanMatcherForBeanDoesNotMatchWithReason() {
        BeanWithPrimitives testInput = PreBuilt.getBeanWithPrimitivesBuilder().build();
        BeanWithPrimitives expected = PreBuilt.getBeanWithPrimitivesBuilder()
                .beanLong(13L)
                .build();

        AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                () -> underTest.assertThat("This is a reason", testInput, matcherFactory.beanMatcher(expected),
                        comparisonDescriptionHandler()));

        Assertions.assertEquals("This is a reason\n" +
                "beanLong\n" +
                "Expected: 13\n" +
                "     got: 6\n", thrown.getMessage());


        Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
        Assertions.assertEquals("{\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanShort\": 1,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanBoolean\": true\n" +
                "}", thrown.getExpected().getStringRepresentation());
    }
}
