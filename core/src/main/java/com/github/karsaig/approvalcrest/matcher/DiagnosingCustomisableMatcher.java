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
import static com.github.karsaig.json.JsonProvider.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.github.karsaig.approvalcrest.ComparisonDescription;
import com.github.karsaig.json.Json;
import com.github.karsaig.json.JsonConfiguration;
import com.github.karsaig.json.JsonElement;

/**
 * Extends the functionalities of {@link DiagnosingMatcher} with the possibility to specify fields and object types to
 * ignore in the comparison, or fields to be matched with a custom matcher
 */
class DiagnosingCustomisableMatcher<T> extends DiagnosingMatcher<T> implements CustomisableMatcher<T> {
    private final Set<String> pathsToIgnore = new HashSet<String>();
    private final Map<String, Matcher<?>> customMatchers = new HashMap<String, Matcher<?>>();
    protected final List<Class<?>> typesToIgnore = new ArrayList<Class<?>>();
    protected final List<Matcher<String>> patternsToIgnore = new ArrayList<Matcher<String>>();
    protected final Set<Class<?>> circularReferenceTypes = new HashSet<Class<?>>();
    protected final T expected;
    private JsonConfiguration configuration;

    public DiagnosingCustomisableMatcher(T expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matches(Object actual, Description mismatchDescription) {
        circularReferenceTypes.addAll(getClassesWithCircularReferences(actual));
        circularReferenceTypes.addAll(getClassesWithCircularReferences(expected));
        Json json = json(typesToIgnore, patternsToIgnore, circularReferenceTypes, configuration);

        if (!areCustomMatchersMatching(actual, mismatchDescription, json)) {
            return false;
        }

        String expectedJson = filterJson(json, expected);

        if (actual == null) {
            return appendMismatchDescription(mismatchDescription, expectedJson, "null", "actual was null");
        }

        String actualJson = filterJson(json, actual);

        return assertEquals(expectedJson, actualJson, mismatchDescription);
    }

    @Override
    public void describeTo(Description description) {
        Json json = json(typesToIgnore, patternsToIgnore, circularReferenceTypes, configuration);
        description.appendText(filterJson(json, expected));
        for (String fieldPath : customMatchers.keySet()) {
            description.appendText("\nand ")
                    .appendText(fieldPath).appendText(" ")
                    .appendDescriptionOf(customMatchers.get(fieldPath));
        }
    }

    @Override
    public CustomisableMatcher<T> ignoring(String fieldPath) {
        pathsToIgnore.add(fieldPath);
        return this;
    }

    @Override
    public CustomisableMatcher<T> ignoring(Class<?> clazz) {
        typesToIgnore.add(clazz);
        return this;
    }

    @Override
    public CustomisableMatcher<T> ignoring(Matcher<String> fieldNamePattern) {
        patternsToIgnore.add(fieldNamePattern);
        return this;
    }

    @Override
    public <V> CustomisableMatcher<T> with(String fieldPath, Matcher<V> matcher) {
        customMatchers.put(fieldPath, matcher);
        return this;
    }

    @Override
    public CustomisableMatcher<T> withJsonConfiguration(final JsonConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    private String filterJson(Json json, Object object) {
        Set<String> pathsToBeFilteredOut = new LinkedHashSet<String>();
        pathsToBeFilteredOut.addAll(pathsToIgnore);
        pathsToBeFilteredOut.addAll(customMatchers.keySet());
        JsonElement filteredJson = findPaths(json, object, pathsToBeFilteredOut);

        return removeSetMarker(json.toJson(filteredJson));
    }

    private boolean areCustomMatchersMatching(Object actual, Description mismatchDescription, Json json) {
        Map<Object, Matcher<?>> customMatching = new HashMap<Object, Matcher<?>>();
        for (Entry<String, Matcher<?>> entry : customMatchers.entrySet()) {
            Object object = actual == null ? null : findBeanAt(entry.getKey(), actual);
            customMatching.put(object, customMatchers.get(entry.getKey()));
        }

        for (Entry<Object, Matcher<?>> entry : customMatching.entrySet()) {
            Matcher<?> matcher = entry.getValue();
            Object object = entry.getKey();
            if (!matcher.matches(object)) {
                appendFieldPath(matcher, mismatchDescription);
                matcher.describeMismatch(object, mismatchDescription);
                appendFieldJsonSnippet(object, mismatchDescription, json);
                return false;
            }
        }
        return true;
    }

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

    private boolean assertEquals(final String expectedJson, String actualJson, Description mismatchDescription) {
        try {
            JSONAssert.assertEquals(expectedJson, actualJson, true);
        } catch (AssertionError e) {
            return appendMismatchDescription(mismatchDescription, expectedJson, actualJson, e.getMessage());
        } catch (JSONException e) {
            return appendMismatchDescription(mismatchDescription, expectedJson, actualJson, e.getMessage());
        }

        return true;
    }

    private void appendFieldJsonSnippet(Object actual, Description mismatchDescription, Json json) {
        JsonElement jsonTree = json.toJsonTree(actual);
        if (!jsonTree.isJsonPrimitive() && !jsonTree.isJsonNull()) {
            mismatchDescription.appendText("\n" + json.toJson(actual));
        }
    }

    private void appendFieldPath(Matcher<?> matcher, Description mismatchDescription) {
        for (Entry<String, Matcher<?>> entry : customMatchers.entrySet()) {
            if (entry.getValue().equals(matcher)) {
                mismatchDescription.appendText(entry.getKey()).appendText(" ");
            }
        }
    }

    private String removeSetMarker(String json) {
        return json.replaceAll(MARKER, "");
    }
}