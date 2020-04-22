package com.github.karsaig.approvalcrest.matcher;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;

import com.github.karsaig.approvalcrest.ComparisonDescription;

public abstract class AbstractDiagnosingMatcher<T> extends DiagnosingMatcher<T> {

    protected boolean appendMismatchDescription(Description mismatchDescription, String expectedJson, String actualJson, String message) {
        if (mismatchDescription instanceof ComparisonDescription) {
            ComparisonDescription shazamMismatchDescription = (ComparisonDescription) mismatchDescription;
            shazamMismatchDescription.setComparisonFailure(true);
            shazamMismatchDescription.setExpected(expectedJson);
            shazamMismatchDescription.setActual(actualJson);
            shazamMismatchDescription.setDifferencesMessage(message);
        }
        mismatchDescription.appendText(message);
        return false;
    }
}
