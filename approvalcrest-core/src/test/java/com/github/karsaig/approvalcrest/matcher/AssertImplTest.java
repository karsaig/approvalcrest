package com.github.karsaig.approvalcrest.matcher;

import org.apache.commons.lang3.NotImplementedException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.PreBuilt;

class AssertImplTest {

    private AssertImpl underTest = new AssertImpl();
    private TestMatcherFactory matcherFactory = new TestMatcherFactory();

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
    void assertThatShouldDoNothingWhenHamcrestMatcherWithoutReason() {
        String testInput = "Dummy string";

        underTest.assertThat(null, testInput, Matchers.is(testInput),
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
    void assertThatShouldDoNothingWhenHamcrestMatcherForStringMatchesWithReason() {
        String testInput = "Dummy string";

        underTest.assertThat("This is the reason", testInput, Matchers.is(testInput),
                (s, cd) -> {
                    throw new NotImplementedException("Shouldn't throw this");
                });
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
                        (s, cd) -> {
                            throw new AssertionFailedError(
                                    s,
                                    cd.getExpected(),
                                    cd.getActual()
                            );
                        }));

        Assertions.assertEquals("beanLong\n" +
                "Expected: 13\n" +
                "     got: 6\n", thrown.getMessage());


        Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
        Assertions.assertEquals("{\n" +
                "  \"beanBoolean\": true,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanShort\": 1\n" +
                "}", thrown.getExpected().getStringRepresentation());
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenHamcrestMatcherForStringDoesNotMatchWithoutReason() {
        String testInput = "Dummy string";
        String expected = "Different expectation";
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat(null, testInput, Matchers.containsString(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        Assertions.assertEquals("\n" +
                "Expected: a string containing \"Different expectation\"\n" +
                "     but: was \"Dummy string\"", thrown.getMessage());

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
                        (s, cd) -> {
                            throw new AssertionFailedError(
                                    s,
                                    cd.getExpected(),
                                    cd.getActual()
                            );
                        }));

        Assertions.assertEquals("This is a reason\n" +
                "beanLong\n" +
                "Expected: 13\n" +
                "     got: 6\n", thrown.getMessage());


        Assertions.assertEquals(PreBuilt.getBeanWithPrimitivesAsJsonString(), thrown.getActual().getStringRepresentation());
        Assertions.assertEquals("{\n" +
                "  \"beanBoolean\": true,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanLong\": 13,\n" +
                "  \"beanShort\": 1\n" +
                "}", thrown.getExpected().getStringRepresentation());
    }

    @Test
    void assertThatShouldThrowNonComparisonExceptionWhenHamcrestMatcherForStringDoesNotMatchWithReason() {
        String testInput = "Dummy string";
        String expected = "Different expectation";
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat("This is a reason", testInput, Matchers.is(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        Assertions.assertEquals("This is a reason\n" +
                "Expected: is \"Different expectation\"\n" +
                "     but: was \"Dummy string\"", thrown.getMessage());

    }

    @Test
    void assertThatShouldWorkTheSameWayAsHamcrestOneWhenBeanMatcherForStringDoesNotMatchWithoutReason() {
        String testInput = "Dummy string";
        String expected = "Different expectation";
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat(null, testInput, matcherFactory.beanMatcher(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        AssertionError hamcrestThrown = Assertions.assertThrows(AssertionError.class,
                () -> org.hamcrest.MatcherAssert.assertThat(testInput, matcherFactory.beanMatcher(expected)));

        Assertions.assertEquals(hamcrestThrown.getMessage(), thrown.getMessage());

    }

    @Test
    void assertThatShouldWorkTheSameWayAsHamcrestOneWhenHamcrestMatcherForStringDoesNotMatchWithoutReason() {
        String testInput = "Dummy string";
        String expected = "Different expectation";
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat(null, testInput, Matchers.is(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        AssertionError hamcrestThrown = Assertions.assertThrows(AssertionError.class,
                () -> org.hamcrest.MatcherAssert.assertThat(testInput, Matchers.is(expected)));

        Assertions.assertEquals(hamcrestThrown.getMessage(), thrown.getMessage());

    }

    @Test
    void assertThatShouldWorkTheSameWayAsHamcrestOneWhenBeanMatcherForStringDoesNotMatchWithReason() {
        String testInput = "Dummy string";
        String expected = "Different expectation";
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat("This is a reason!", testInput, matcherFactory.beanMatcher(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        AssertionError hamcrestThrown = Assertions.assertThrows(AssertionError.class,
                () -> org.hamcrest.MatcherAssert.assertThat("This is a reason!", testInput, matcherFactory.beanMatcher(expected)));

        Assertions.assertEquals(hamcrestThrown.getMessage(), thrown.getMessage());

    }

    @Test
    void assertThatShouldWorkTheSameWayAsHamcrestOneWhenHamcrestMatcherForStringDoesNotMatchWithReason() {
        String testInput = "Dummy string";
        String expected = "Different expectation";
        AssertionError thrown = Assertions.assertThrows(AssertionError.class,
                () -> underTest.assertThat("This is a reason!", testInput, Matchers.is(expected),
                        (s, cd) -> {
                            throw new NotImplementedException("Shouldn't throw this");
                        }));

        AssertionError hamcrestThrown = Assertions.assertThrows(AssertionError.class,
                () -> org.hamcrest.MatcherAssert.assertThat("This is a reason!", testInput, Matchers.is(expected)));

        Assertions.assertEquals(hamcrestThrown.getMessage(), thrown.getMessage());

    }
}