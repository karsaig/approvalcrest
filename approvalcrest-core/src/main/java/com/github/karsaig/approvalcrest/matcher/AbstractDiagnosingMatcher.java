package com.github.karsaig.approvalcrest.matcher;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;

import com.github.karsaig.approvalcrest.ComparisonDescription;

public abstract class AbstractDiagnosingMatcher<T> extends DiagnosingMatcher<T> {

    protected boolean appendMismatchDescription(Description mismatchDescription, String expected, String actual, String message) {
        if (mismatchDescription instanceof ComparisonDescription) {
            ComparisonDescription shazamMismatchDescription = (ComparisonDescription) mismatchDescription;
            shazamMismatchDescription.setComparisonFailure(true);
            shazamMismatchDescription.setExpected(expected);
            shazamMismatchDescription.setActual(actual);
            shazamMismatchDescription.setDifferencesMessage(message);
        }
        mismatchDescription.appendText(message);
        return false;
    }
}
