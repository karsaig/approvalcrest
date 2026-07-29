package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import org.hamcrest.Matcher;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.github.karsaig.approvalcrest.EnvVarReader.getBooleanProperties;


public class MatcherConfiguration {

    private static final String SERIALIZE_NULLS_PROPERTY = "approvalcrestSerializeNulls";
    private static final String SERIALIZE_NULLS_ALIAS = "aSerNulls";
    private static final String LEGACY_SET_COLLAPSE_PROPERTY = "approvalcrestLegacySetCollapse";
    private static final String LEGACY_SET_COLLAPSE_ALIAS = "aLSCollapse";

    private final Set<String> pathsToIgnore = new HashSet<>();
    private final Map<String, Matcher<?>> customMatchers = new HashMap<>();
    private final List<Class<?>> typesToIgnore = new ArrayList<>();
    private final List<Matcher<String>> patternsToIgnore = new ArrayList<>();
    private final List<Function<Object, Boolean>> skipCircularReferenceCheck = new ArrayList<>();
    private final Map<String, List<SortField<String>>> pathsToSort = new HashMap<>();
    private final List<SortField<Matcher<String>>> patternsToSort = new ArrayList<>();
    private final List<Class<?>> typesToSort = new ArrayList<>();
    private final List<AbstractMap.SimpleEntry<Matcher<String>, Matcher<?>>> customMatcherPatterns = new ArrayList<>();
    private final List<ElementIgnoreRule> elementIgnoreRules = new ArrayList<>();
    private AliasMap aliasMap = AliasMap.builder().build();
    private boolean serializeNulls = getBooleanProperties("true", SERIALIZE_NULLS_PROPERTY, SERIALIZE_NULLS_ALIAS);
    private boolean legacySetCollapse = getBooleanProperties("false", LEGACY_SET_COLLAPSE_PROPERTY, LEGACY_SET_COLLAPSE_ALIAS);

    public MatcherConfiguration() {
        skipCircularReferenceCheck.add(o -> Path.class.isInstance(o));
    }

    public Map<String, Matcher<?>> getCustomMatchers() {
        return customMatchers;
    }

    public Set<String> getPathsToIgnore() {
        return pathsToIgnore;
    }

    public List<Matcher<String>> getPatternsToIgnore() {
        return patternsToIgnore;
    }

    public List<Function<Object, Boolean>> getSkipCircularReferenceCheck() {
        return skipCircularReferenceCheck;
    }

    public List<Class<?>> getTypesToIgnore() {
        return typesToIgnore;
    }

    public Map<String, List<SortField<String>>> getPathsToSort() {
        return pathsToSort;
    }

    public List<SortField<Matcher<String>>> getPatternsToSort() {
        return patternsToSort;
    }

    public List<Class<?>> getTypesToSort() {
        return typesToSort;
    }

    public List<AbstractMap.SimpleEntry<Matcher<String>, Matcher<?>>> getCustomMatcherPatterns() {
        return customMatcherPatterns;
    }

    public List<ElementIgnoreRule> getElementIgnoreRules() {
        return elementIgnoreRules;
    }

    public MatcherConfiguration addElementIgnoreRule(String path, Matcher<?> valueMatcher) {
        elementIgnoreRules.add(ElementIgnoreRule.of(path, valueMatcher));
        return this;
    }

    public MatcherConfiguration addElementIgnoreRule(String path, String value) {
        elementIgnoreRules.add(ElementIgnoreRule.ofValue(path, value));
        return this;
    }

    public MatcherConfiguration addPathToIgnore(String path) {
        pathsToIgnore.add(path);
        return this;
    }

    public MatcherConfiguration addPathToIgnore(String[] fieldPaths) {
        for (String fieldPath : fieldPaths) {
            pathsToIgnore.add(fieldPath);
        }
        return this;
    }

    public MatcherConfiguration addPathToIgnore(Collection<String> fieldPaths) {
        pathsToIgnore.addAll(fieldPaths);
        return this;
    }

    public MatcherConfiguration addCustomMatcher(String fieldPath, Matcher<?> matcher) {
        customMatchers.put(fieldPath, matcher);
        return this;
    }

    public <V> MatcherConfiguration addCustomMatcherPattern(Matcher<String> fieldNamePattern, Matcher<V> matcher) {
        customMatcherPatterns.add(new AbstractMap.SimpleEntry<Matcher<String>, Matcher<?>>(fieldNamePattern, matcher));
        return this;
    }

    public MatcherConfiguration addTypeToIgnore(Class<?> clazz) {
        typesToIgnore.add(clazz);
        return this;
    }

    public MatcherConfiguration addTypeToIgnore(Class<?>[] clazzs) {
        Collections.addAll(typesToIgnore,clazzs);
        return this;
    }

    public MatcherConfiguration addTypeToIgnore(Collection<Class<?>> clazzs) {
        typesToIgnore.addAll(clazzs);
        return this;
    }

    public MatcherConfiguration addTypeToSort(Class<?> clazz) {
        typesToSort.add(clazz);
        return this;
    }

    public MatcherConfiguration addTypeToSort(Class<?>[] clazzs) {
        Collections.addAll(typesToSort, clazzs);
        return this;
    }

    public MatcherConfiguration addTypeToSort(Collection<Class<?>> clazzs) {
        typesToSort.addAll(clazzs);
        return this;
    }

    public MatcherConfiguration addPatternToIgnore(Matcher<String> fieldNamePattern) {
        patternsToIgnore.add(fieldNamePattern);
        return this;
    }

    public MatcherConfiguration addPatternToIgnore(Matcher<String>[] fieldNamePatterns) {
        Collections.addAll(patternsToIgnore,fieldNamePatterns);
        return this;
    }

    public MatcherConfiguration addPatternToIgnore(Collection<Matcher<String>> fieldNamePattern) {
        patternsToIgnore.addAll(fieldNamePattern);
        return this;
    }

    public MatcherConfiguration addSkipCircularReferenceChecker(Function<Object, Boolean> checker) {
        skipCircularReferenceCheck.add(checker);
        return this;
    }


    public MatcherConfiguration addSkipCircularReferenceChecker(Function<Object, Boolean>[] checkers) {
        Collections.addAll(skipCircularReferenceCheck,checkers);
        return this;
    }

    public MatcherConfiguration addPatternToSort(Matcher<String> fieldNamePattern) {
        patternsToSort.add(SortField.of(fieldNamePattern));
        return this;
    }

    public MatcherConfiguration addPatternToSort(SortField<Matcher<String>> fieldNamePattern) {
        patternsToSort.add(fieldNamePattern);
        return this;
    }

    public MatcherConfiguration addPatternToSort(Matcher<String>[] fieldNamePatterns) {
        for (Matcher<String> matcher : fieldNamePatterns) {
            patternsToSort.add(SortField.of(matcher));
        }
        return this;
    }

    public MatcherConfiguration addPatternToSort(SortField<Matcher<String>>[] fieldNamePatterns) {
        Collections.addAll(patternsToSort,fieldNamePatterns);
        return this;
    }

    public MatcherConfiguration addPatternToSort(Collection<Matcher<String>> fieldNamePattern) {
        for (Matcher<String> matcher : fieldNamePattern) {
            patternsToSort.add(SortField.of(matcher));
        }
        return this;
    }

    public MatcherConfiguration addPathToSort(String path) {
        pathsToSort.computeIfAbsent(path, k -> new ArrayList<>()).add(SortField.of(path));
        return this;
    }

    public MatcherConfiguration addPathToSort(String[] fieldPaths) {
        for (String fieldPath : fieldPaths) {
            pathsToSort.computeIfAbsent(fieldPath, k -> new ArrayList<>()).add(SortField.of(fieldPath));
        }
        return this;
    }

    public MatcherConfiguration addPathToSort(SortField<String> path) {
        pathsToSort.computeIfAbsent(path.getSortFieldSelector(), k -> new ArrayList<>()).add(path);
        return this;
    }

    public MatcherConfiguration addPathToSort(SortField<String>[] fieldPaths) {
        for (SortField<String> fieldPath : fieldPaths) {
            pathsToSort.computeIfAbsent(fieldPath.getSortFieldSelector(), k -> new ArrayList<>()).add(fieldPath);
        }
        return this;
    }

    public MatcherConfiguration addPathToSort(Collection<String> fieldPaths) {
        for (String fieldPath : fieldPaths) {
            pathsToSort.computeIfAbsent(fieldPath, k -> new ArrayList<>()).add(SortField.of(fieldPath));
        }
        return this;
    }

    public AliasMap getAliasMap() {
        return aliasMap;
    }

    public boolean isSerializeNulls() {
        return serializeNulls;
    }

    public MatcherConfiguration setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
        return this;
    }

    /**
     * When true, {@code Set} elements that serialise to the same JSON are collapsed into a single
     * entry, as they were before this behaviour was corrected.
     *
     * <p>This is a migration escape hatch for codebases with a large approved-file corpus to
     * re-approve, not a mode worth staying in: while it is on, a set that loses or gains a
     * duplicate element cannot fail a test.
     */
    public boolean isLegacySetCollapse() {
        return legacySetCollapse;
    }

    public MatcherConfiguration setLegacySetCollapse(boolean legacySetCollapse) {
        this.legacySetCollapse = legacySetCollapse;
        return this;
    }

    public MatcherConfiguration addAliasMap(AliasMap other) {
        aliasMap = aliasMap.merge(other);
        return this;
    }

    public MatcherConfiguration addAlias(String value, String alias) {
        aliasMap = aliasMap.merge(AliasMap.builder().add(value, alias).build());
        return this;
    }

    public MatcherConfiguration addAlias(String fieldName, String value, String alias) {
        aliasMap = aliasMap.merge(AliasMap.builder().add(fieldName, value, alias).build());
        return this;
    }
}
