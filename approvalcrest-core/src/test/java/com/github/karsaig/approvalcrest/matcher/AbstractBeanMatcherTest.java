package com.github.karsaig.approvalcrest.matcher;

import org.junit.jupiter.api.Assertions;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractBeanMatcherTest extends AbstractTest {

    protected static final TestMatcherFactory MATCHER_FACTORY = new TestMatcherFactory();

    protected <T> void assertDiagnosingMatcher(T input, T expected) {
        assertDiagnosingMatcher(input, expected, Function.identity());
    }

    protected <T> void assertDiagnosingMatcher(T input, T expected, String expectedExceptionMessage) {
        assertDiagnosingErrorMatcher(input, expected, Function.identity(), expectedExceptionMessage);
    }

    protected <T> void assertDiagnosingMatcher(T input, T expected, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator) {
        assertDiagnosingErrorMatcher(input, expected, configurator, (String) null, null);
    }

    protected <T> void assertDiagnosingErrorMatcher(T input, T expected, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator, String expectedExceptionMessage) {
        assertDiagnosingErrorMatcher(input, expected, configurator, expectedExceptionMessage, AssertionError.class);
    }

    protected <T, C extends Throwable> void assertDiagnosingErrorMatcher(T input, T expected, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator, String expectedExceptionMessage, Class<C> clazz) {
        assertDiagnosingMatcher(input, expected, configurator, clazz, expectedExceptionMessage == null ? null : ex -> Assertions.assertEquals(expectedExceptionMessage, ex.getMessage()));
    }

    protected <T, C extends Throwable> void assertDiagnosingMatcher(T input, T expected, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator, Class<C> clazz, Consumer<C> exceptionHandler) {
        DiagnosingCustomisableMatcher<Object> matcherWithDefaultConfig = MATCHER_FACTORY.beanMatcher(expected);
        DiagnosingCustomisableMatcher<Object> diagnosingMatcher = configurator.apply(matcherWithDefaultConfig);
        if (exceptionHandler == null) {
            assertThat(input, diagnosingMatcher);
        } else {
            C actualError = assertThrows(clazz,
                    () -> assertThat(input, diagnosingMatcher));

            exceptionHandler.accept(actualError);
        }
    }


}
