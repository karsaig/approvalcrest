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

import com.github.karsaig.approvalcrest.JsonElementUtil;
import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
import com.github.karsaig.approvalcrest.matcher.machinereadable.AliasTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.github.karsaig.approvalcrest.CyclicReferenceDetector.getClassesWithCircularReferences;
import static com.github.karsaig.approvalcrest.EnvVarReader.getBooleanProperties;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.applySorting;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.applyRootCollectionSorting;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.findPaths;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.removeSetMarker;
import static com.github.karsaig.approvalcrest.matcher.GsonProvider.gson;

/**
 * Extends the functionalities of {@link DiagnosingMatcher} with the possibility to specify fields and object types to
 * ignore in the comparison, or fields to be matched with a custom matcher
 */
public class DiagnosingCustomisableMatcher<T> extends AbstractDiagnosingMatcher<T> implements CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>> {
    protected final Set<Class<?>> circularReferenceTypes = new HashSet<>();
    protected final T expected;
    private GsonConfiguration configuration;
    protected MatcherConfiguration matcherConfiguration = new MatcherConfiguration();
    private static final String SKIP_CLASS_COMPARISON_ALIAS = "bMSCComparison";
    private boolean skipClassComparison = getBooleanProperties("false", "beanMatcherSkipClassComparison", SKIP_CLASS_COMPARISON_ALIAS);
    private boolean jsonDescription = true;

    public DiagnosingCustomisableMatcher(T expected) {
        this.expected = expected;
    }

    @Override
    public void describeTo(Description description) {
        if(jsonDescription){
        Gson gson = gson(matcherConfiguration, circularReferenceTypes, configuration);
        description.appendText(filterJson(gson, expected, null, null));
        for (String fieldPath : matcherConfiguration.getCustomMatchers().keySet()) {
            description.appendText("\nand ")
                    .appendText(fieldPath).appendText(" ")
                    .appendDescriptionOf(matcherConfiguration.getCustomMatchers().get(fieldPath));
        }
        }
    }

    @Override
    protected boolean doMatches(Object actual, Description mismatchDescription) {
        if (actual != null  && expected != null) {
            if(!skipClassComparison && !expected.getClass().isInstance(actual)){
                if (machineReadableOutput) {
                    JsonObject root = new JsonObject();
                    root.addProperty("failureType", "TYPE_MISMATCH");
                    root.addProperty("expectedType", expected.getClass().getName());
                    root.addProperty("actualType", actual.getClass().getName());
                    root.addProperty("action", "Add .skipClassComparison() to the matcher, or set system property bMSCComparison=true");
                    Gson outputGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                    mismatchDescription.appendText(outputGson.toJson(root));
                } else {
                    mismatchDescription.appendText("Actual type ["+actual.getClass()+"] is not an instance of expected type ["+expected.getClass()+"]!\nThis can be ignored with skipClassComparison or\nsetting beanMatcherSkipClassComparison env variable to true");
                }
                jsonDescription = false;
                return false;
            }
        }
        circularReferenceTypes.addAll(getClassesWithCircularReferences(actual, matcherConfiguration));
        circularReferenceTypes.addAll(getClassesWithCircularReferences(expected, matcherConfiguration));
        Gson gson = gson(matcherConfiguration, circularReferenceTypes, configuration);

        JsonElement actualAsJsonElement = actual != null ? gson.toJsonTree(actual) : null;

        if (!areCustomMatchersMatchingBeanOrJson(actual, actualAsJsonElement, mismatchDescription, gson, matcherConfiguration)) {
            return false;
        }

        IgnoredFieldsTracker ignoredTracker = machineReadableOutput ? new IgnoredFieldsTracker() : null;
        AliasTracker aliasTracker = machineReadableOutput ? new AliasTracker() : null;
        String untrackedNote = buildUntrackedNote();

        String expectedJson = filterJson(gson, expected, ignoredTracker, aliasTracker);

        if (actual == null) {
            return appendMismatchDescriptionWithNote(mismatchDescription, expectedJson, "null", "actual was null",
                    ignoredTracker, aliasTracker, untrackedNote);
        }

        String actualJson = filterJson(gson, actualAsJsonElement, actual, ignoredTracker, aliasTracker);

        return assertJsonEquals(expectedJson, actualJson, mismatchDescription, ignoredTracker, aliasTracker, untrackedNote);
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

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    @Override
    public final DiagnosingCustomisableMatcher<T> ignoring(Matcher<String>... fieldNamePatterns) {
        matcherConfiguration.addPatternToIgnore(fieldNamePatterns);
        return this;
    }

    @Override
    public <V> DiagnosingCustomisableMatcher<T> with(String fieldPath, Matcher<V> matcher) {
        matcherConfiguration.addCustomMatcher(fieldPath, matcher);
        return this;
    }

    @Override
    public <V> DiagnosingCustomisableMatcher<T> withMatcher(Matcher<String> fieldNamePattern, Matcher<V> matcher) {
        matcherConfiguration.addCustomMatcherPattern(fieldNamePattern, matcher);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> withGsonConfiguration(GsonConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }


    private boolean assertJsonEquals(String expectedJson, String actualJson, Description mismatchDescription,
                                      IgnoredFieldsTracker ignoredTracker, AliasTracker aliasTracker, String note) {
        try {
            org.skyscreamer.jsonassert.JSONAssert.assertEquals(expectedJson, actualJson, true);
        } catch (AssertionError | org.json.JSONException e) {
            return appendMismatchDescriptionWithNote(mismatchDescription, expectedJson, actualJson, e.getMessage(),
                    ignoredTracker, aliasTracker, note);
        }
        return true;
    }

    private String filterJson(Gson gson, Object object, IgnoredFieldsTracker tracker, AliasTracker aliasTracker) {
        return filterJson(gson, gson.toJsonTree(object), object, tracker, aliasTracker);
    }

    private String filterJson(Gson gson, JsonElement preComputedJson, Object objectForTypeCheck,
                               IgnoredFieldsTracker tracker, AliasTracker aliasTracker) {
        Set<String> set = new HashSet<>();
        set.addAll(matcherConfiguration.getPathsToIgnore());
        set.addAll(matcherConfiguration.getCustomMatchers().keySet());

        Map<String, IgnoredFieldsTracker.Reason> reasonMap = null;
        if (tracker != null) {
            reasonMap = new HashMap<>();
            for (String path : matcherConfiguration.getPathsToIgnore()) {
                reasonMap.put(path, IgnoredFieldsTracker.Reason.IGNORE_PATH);
            }
            for (String path : matcherConfiguration.getCustomMatchers().keySet()) {
                reasonMap.put(path, IgnoredFieldsTracker.Reason.CUSTOM_MATCHER);
            }
        }

        JsonElement filteredJson = findPaths(preComputedJson, set, tracker, reasonMap);
        JsonElementUtil.filterByCustomMatcherPatterns(filteredJson, matcherConfiguration, tracker);
        AliasMap aliasMap = matcherConfiguration.getAliasMap();
        if (!aliasMap.isEmpty()) {
            JsonElementUtil.applyAliases(filteredJson, aliasMap, aliasTracker);
        }
        applySorting(filteredJson, matcherConfiguration.getPathsToSort(), matcherConfiguration.getPatternsToSort(), true);
        applyRootCollectionSorting(filteredJson, objectForTypeCheck, matcherConfiguration.getPatternsToSort(), matcherConfiguration.getPathsToSort(), matcherConfiguration.getTypesToSort());
        return removeSetMarker(gson.toJson(filteredJson));
    }

    private String buildUntrackedNote() {
        return buildUntrackedNote(matcherConfiguration);
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

    public DiagnosingCustomisableMatcher<T> skipClassComparison() {
        this.skipClassComparison = true;
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> sortField(Matcher<String> fieldNamePattern) {
        matcherConfiguration.addPatternToSort(fieldNamePattern);
        return this;
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    @Override
    public final DiagnosingCustomisableMatcher<T> sortField(Matcher<String>... fieldNamePatterns) {
        matcherConfiguration.addPatternToSort(fieldNamePatterns);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> sortField(String fieldPath) {
        matcherConfiguration.addPathToSort(fieldPath);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> sortField(String... fieldPaths) {
        matcherConfiguration.addPathToSort(fieldPaths);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> sortFieldMatcher(SortField<Matcher<String>> fieldNamePattern) {
        matcherConfiguration.addPatternToSort(fieldNamePattern);
        return this;
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    @Override
    public final DiagnosingCustomisableMatcher<T> sortFieldMatcher(SortField<Matcher<String>>... fieldNamePatterns) {
        matcherConfiguration.addPatternToSort(fieldNamePatterns);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> sortFieldPath(SortField<String> fieldPath) {
        matcherConfiguration.addPathToSort(fieldPath);
        return this;
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    @Override
    public final DiagnosingCustomisableMatcher<T> sortFieldPath(SortField<String>... fieldPaths) {
        matcherConfiguration.addPathToSort(fieldPaths);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> sortType(Class<?>... types) {
        matcherConfiguration.addTypeToSort(types);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> withAliasMap(AliasMap aliasMap) {
        matcherConfiguration.addAliasMap(aliasMap);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> withAlias(String value, String alias) {
        matcherConfiguration.addAlias(value, alias);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> withAlias(String fieldName, String value, String alias) {
        matcherConfiguration.addAlias(fieldName, value, alias);
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> withMachineReadableOutput() {
        machineReadableOutput = true;
        return this;
    }

    @Override
    public DiagnosingCustomisableMatcher<T> withoutSerializingNulls() {
        matcherConfiguration.setSerializeNulls(false);
        return this;
    }

    @Override
    public String toString() {
        return "SameBeanAs matcher";
    }
}