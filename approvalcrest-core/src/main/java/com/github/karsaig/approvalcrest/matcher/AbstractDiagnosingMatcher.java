package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.PathNullPointerException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;

import com.github.karsaig.approvalcrest.ComparisonDescription;
import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.Map;

import static com.github.karsaig.approvalcrest.BeanFinder.findBeanAt;

public abstract class AbstractDiagnosingMatcher<T> extends DiagnosingMatcher<T> {

    private boolean comparisonDescriptionNeeded = false;

    protected boolean appendMismatchDescription(Description mismatchDescription, String expected, String actual, String message) {
        if (comparisonDescriptionNeeded && ComparisonDescription.class.isInstance(mismatchDescription)) {
            ComparisonDescription shazamMismatchDescription = (ComparisonDescription) mismatchDescription;
            shazamMismatchDescription.setComparisonFailure(true);
            shazamMismatchDescription.setExpected(expected);
            shazamMismatchDescription.setActual(actual);
            shazamMismatchDescription.setDifferencesMessage(message);
        }
        mismatchDescription.appendText(message);
        return false;
    }

    protected void setComparisonDescriptionNeeded(boolean comparisonDescriptionNeeded) {
        this.comparisonDescriptionNeeded = comparisonDescriptionNeeded;
    }

    protected boolean isComparisonDescriptionNeeded() {
        return comparisonDescriptionNeeded;
    }

    protected boolean areCustomMatchersMatching(Object actual, Description mismatchDescription, Gson gson, MatcherConfiguration matcherConfiguration) {
        if (!String.class.isInstance(actual)) {
            Map<Object, Matcher<?>> customMatching = new HashMap<>();
            for (Map.Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
                try {
                    Object object = actual == null ? null : findBeanAt(entry.getKey(), actual);
                    customMatching.put(object, matcherConfiguration.getCustomMatchers().get(entry.getKey()));
                } catch (PathNullPointerException e) {
                    mismatchDescription.appendText(String.format("parent bean of %s is null", e.getPath()));
                    return false;
                }
            }

            for (Map.Entry<Object, Matcher<?>> entry : customMatching.entrySet()) {
                Matcher<?> matcher = entry.getValue();
                Object object = entry.getKey();
                if (!matcher.matches(object)) {
                    appendFieldPath(matcher, mismatchDescription, matcherConfiguration);
                    matcher.describeMismatch(object, mismatchDescription);
                    appendFieldJsonSnippet(object, mismatchDescription, gson);
                    return false;
                }
            }
        }
        return true;
    }

    protected void appendFieldPath(Matcher<?> matcher, Description mismatchDescription, MatcherConfiguration matcherConfiguration) {
        for (Map.Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
            if (entry.getValue().equals(matcher)) {
                mismatchDescription.appendText(entry.getKey()).appendText(" ");
            }
        }
    }

    protected void appendFieldJsonSnippet(Object actual, Description mismatchDescription, Gson gson) {
        JsonElement jsonTree = gson.toJsonTree(actual);
        if (!jsonTree.isJsonPrimitive() && !jsonTree.isJsonNull()) {
            mismatchDescription.appendText("\n" + gson.toJson(actual));
        }
    }
}
