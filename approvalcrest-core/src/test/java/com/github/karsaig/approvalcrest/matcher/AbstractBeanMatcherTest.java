package com.github.karsaig.approvalcrest.matcher;

import org.junit.jupiter.api.Assertions;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractBeanMatcherTest extends AbstractTest {

    protected static final TestMatcherFactory MATCHER_FACTORY = new TestMatcherFactory();

    protected <T> void assertDiagnosingMatcher(T input, T expected) {
        assertDiagnosingMatcher(input, expected, Function.identity());
    }

    protected <T> void assertDiagnosingMatcher(T input, T expected, String expectedExceptionMessage) {
        assertDiagnosingMatcherError(input, expected, Function.identity(), expectedExceptionMessage);
    }

    protected <T> void assertDiagnosingMatcher(T input, T expected, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator) {
        assertDiagnosingMatcher(input, expected, configurator, null, null);
    }

    protected <T> void assertDiagnosingMatcherError(T input, T expected, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator, String expectedExceptionMessage) {
        assertDiagnosingMatcher(input, expected, configurator, expectedExceptionMessage, AssertionError.class);
    }

    protected <T, C extends Throwable> void assertDiagnosingMatcher(T input, T expected, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator, String expectedExceptionMessage, Class<C> clazz) {
        DiagnosingCustomisableMatcher<Object> matcherWithDefaultConfig = MATCHER_FACTORY.beanMatcher(expected);
        DiagnosingCustomisableMatcher<Object> diagnosingMatcher = configurator.apply(matcherWithDefaultConfig);
        if (expectedExceptionMessage == null) {
            assertThat(input, diagnosingMatcher);
        } else {
            C actualError = assertThrows(clazz,
                    () -> assertThat(input, diagnosingMatcher));

            Assertions.assertEquals(expectedExceptionMessage, actualError.getMessage());
        }
    }
}
