package com.github.karsaig.approvalcrest.matcher;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;

public abstract class AbstractBeanMatcherTest extends AbstractTest {


    protected static final TestMatcherFactory MATCHER_FACTORY = new TestMatcherFactory();

    protected <T> void assertDiagnosingMatcher(T input, T expected, String expectedExceptionMessage) {
        assertDiagnosingMatcher(input, expected, Function.identity(), expectedExceptionMessage);
    }

    protected <T> void assertDiagnosingMatcher(T input, T expected, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator, String expectedExceptionMessage) {
        DiagnosingCustomisableMatcher<Object> matcherWithDefaultConfig = MATCHER_FACTORY.beanMatcher(expected);
        DiagnosingCustomisableMatcher<Object> diagnosingMatcher = configurator.apply(matcherWithDefaultConfig);
        if (expectedExceptionMessage == null) {
            assertThat(input, diagnosingMatcher);
        } else {
            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> assertThat(input, diagnosingMatcher));

            Assertions.assertEquals(expectedExceptionMessage, actualError.getMessage());
        }
    }
}
