/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.BeanFinder.findBeanAt;
import static com.github.karsaig.approvalcrest.CyclicReferenceDetector.getClassesWithCircularReferences;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.MARKER;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.findPaths;
import static com.github.karsaig.approvalcrest.matcher.GsonProvider.gson;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.PathNullPointerException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Extends the functionalities of {@link DiagnosingMatcher} with the possibility to specify fields and object types to
 * ignore in the comparison, or fields to be matched with a custom matcher
 */
class DiagnosingCustomisableMatcher<T> extends AbstractDiagnosingMatcher<T> implements CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>> {
    private static final Pattern MARKER_PATTERN = Pattern.compile(MARKER);
    protected final Set<Class<?>> circularReferenceTypes = new HashSet<>();
    protected final T expected;
    private GsonConfiguration configuration;
    protected MatcherConfiguration matcherConfiguration = new MatcherConfiguration();

    public DiagnosingCustomisableMatcher(T expected) {
        this.expected = expected;
    }

    @Override
    public void describeTo(Description description) {
        Gson gson = gson(matcherConfiguration, circularReferenceTypes, configuration);
        description.appendText(filterJson(gson, expected));
        for (String fieldPath : matcherConfiguration.getCustomMatchers().keySet()) {
            description.appendText("\nand ")
                    .appendText(fieldPath).appendText(" ")
                    .appendDescriptionOf(matcherConfiguration.getCustomMatchers().get(fieldPath));
        }
    }

    @Override
    protected boolean matches(Object actual, Description mismatchDescription) {
        circularReferenceTypes.addAll(getClassesWithCircularReferences(actual, matcherConfiguration));
        circularReferenceTypes.addAll(getClassesWithCircularReferences(expected, matcherConfiguration));
        Gson gson = gson(matcherConfiguration, circularReferenceTypes, configuration);

        if (!areCustomMatchersMatching(actual, mismatchDescription, gson)) {
            return false;
        }

        String expectedJson = filterJson(gson, expected);

        if (actual == null) {
            return appendMismatchDescription(mismatchDescription, expectedJson, "null", "actual was null");
        }

        String actualJson = filterJson(gson, actual);

        return assertEquals(expectedJson, actualJson, mismatchDescription);
    }

    private boolean areCustomMatchersMatching(Object actual, Description mismatchDescription, Gson gson) {
        Map<Object, Matcher<?>> customMatching = new HashMap<>();
        for (Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
            try {
                Object object = actual == null ? null : findBeanAt(entry.getKey(), actual);
                customMatching.put(object, matcherConfiguration.getCustomMatchers().get(entry.getKey()));
            } catch (PathNullPointerException e) {
                mismatchDescription.appendText(String.format("parent bean of %s is null", e.getPath()));
                return false;
            }
        }

        for (Entry<Object, Matcher<?>> entry : customMatching.entrySet()) {
            Matcher<?> matcher = entry.getValue();
            Object object = entry.getKey();
            if (!matcher.matches(object)) {
                appendFieldPath(matcher, mismatchDescription);
                matcher.describeMismatch(object, mismatchDescription);
                appendFieldJsonSnippet(object, mismatchDescription, gson);
                return false;
            }
        }
        return true;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> ignoring(String fieldPath) {
        matcherConfiguration.addPathToIgnore(fieldPath);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> ignoring(Class<?> clazz) {
        matcherConfiguration.addTypeToIgnore(clazz);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> ignoring(Matcher<String> fieldNamePattern) {
        matcherConfiguration.addPatternToIgnore(fieldNamePattern);
        return this;
    }

    @Override
    public <V> DiagnosingCustomisableMatcher<T> with(String fieldPath, Matcher<V> matcher) {
        matcherConfiguration.addCustomMatcher(fieldPath, matcher);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> withGsonConfiguration(GsonConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }


    private boolean assertEquals(String expectedJson, String actualJson, Description mismatchDescription) {
        try {
            JSONAssert.assertEquals(expectedJson, actualJson, true);
        } catch (AssertionError | JSONException e) {
            return appendMismatchDescription(mismatchDescription, expectedJson, actualJson, e.getMessage());
        }

        return true;
    }

    private void appendFieldJsonSnippet(Object actual, Description mismatchDescription, Gson gson) {
        JsonElement jsonTree = gson.toJsonTree(actual);
        if (!jsonTree.isJsonPrimitive() && !jsonTree.isJsonNull()) {
            mismatchDescription.appendText("\n" + gson.toJson(actual));
        }
    }

    private void appendFieldPath(Matcher<?> matcher, Description mismatchDescription) {
        for (Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
            if (entry.getValue().equals(matcher)) {
                mismatchDescription.appendText(entry.getKey()).appendText(" ");
            }
        }
    }

    private String filterJson(Gson gson, Object object) {
        Set<String> set = new HashSet<>();
        set.addAll(matcherConfiguration.getPathsToIgnore());
        set.addAll(matcherConfiguration.getCustomMatchers().keySet());
        JsonElement filteredJson = findPaths(gson, object, set);

        return removeSetMarker(gson.toJson(filteredJson));
    }

    private String removeSetMarker(String json) {
        return MARKER_PATTERN.matcher(json).replaceAll("");
    }

    @Override
    public DiagnosingCustomisableMatcher<T> ignoring(String... fieldPaths) {
        matcherConfiguration.addPathToIgnore(fieldPaths);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> ignoring(Class<?>... clazzs) {
        matcherConfiguration.addTypeToIgnore(clazzs);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> skipCircularReferenceCheck(Function<Object, Boolean> matcher) {
        matcherConfiguration.addSkipCircularReferenceChecker(matcher);
        return this;
    }

    @SuppressWarnings({"unchecked", "varargs"})
    @Override
    public final DiagnosingCustomisableMatcher<T> skipCircularReferenceCheck(Function<Object, Boolean> matcher, Function<Object, Boolean>... matchers) {
        matcherConfiguration.addSkipCircularReferenceChecker(matcher).addSkipCircularReferenceChecker(matchers);
        return this;
    }
}