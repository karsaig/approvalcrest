package com.github.karsaig.approvalcrest.matcher;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.function.BiConsumer;

import org.hamcrest.Matcher;

import com.github.karsaig.approvalcrest.ComparisonDescription;

public class AssertImpl {

    protected <T> void assertThat(String reason, T actual, Matcher<? super T> matcher, BiConsumer<String, ComparisonDescription> failureHandler) {
        if (AbstractDiagnosingMatcher.class.isInstance(matcher)) {
            AbstractDiagnosingMatcher<?> m = AbstractDiagnosingMatcher.class.cast(matcher);
            m.setComparisonDescriptionNeeded(true);
            ComparisonDescription description = new ComparisonDescription();
            if (isNotBlank(reason)) {
                description.appendText(reason);
            }
            if (!matcher.matches(actual)) {

                description
                        .appendText("\nExpected: ")
                        .appendDescriptionOf(matcher)
                        .appendText("\n     but: ");
                matcher.describeMismatch(actual, description);

                if (description.isComparisonFailure()) {
                    failureHandler.accept(comparisonFailureMessage(reason, description), description);
                }

                throw new AssertionError(description.toString());
            }
        } else {
            if (isNotBlank(reason)) {
                org.hamcrest.MatcherAssert.assertThat(reason, actual, matcher);
            } else {
                org.hamcrest.MatcherAssert.assertThat(actual, matcher);
            }
        }
    }

    private static String comparisonFailureMessage(String reason, ComparisonDescription shazamDescription) {
        return (isNotBlank(reason) ? reason + "\n" : "") + shazamDescription.getDifferencesMessage();
    }

    /*
    private static String assertionFailedErrorMessage(final String reason, final ComparisonDescription shazamDescription) {
        return (isNotBlank(reason) ? reason + "\n" : "") + shazamDescription.getDifferencesMessage() +
                String.format(" expected:<[%s]> but was:<[%s]>", shazamDescription.getExpected(), shazamDescription.getActual());
    }

     */
}
