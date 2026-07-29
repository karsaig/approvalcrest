package com.github.karsaig.approvalcrest.matcher.sorting;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describes how one collection is sorted before comparison, and which of its fields are left out
 * of the sort key. Excluding a field means two elements differing only in that field keep a stable
 * relative order, so a value that changes per run cannot reshuffle the collection.
 *
 * <p>Instances are immutable; {@link #ignoring(String)} and friends return a new instance.
 *
 * @param <T> how the collection is selected - a path {@code String}, or a {@code Matcher<String>}
 *            over field names
 */
public class SortField<T> {

    /**
     * Sorts the selected collection using the whole element as the sort key.
     *
     * @param sortFieldSelector selects the collection to sort
     * @param <U>               selector type
     * @return a sort rule with no fields excluded from the sort key
     */
    public static <U> SortField<U> of(U sortFieldSelector){
        return new SortField<>(sortFieldSelector,Collections.emptyList(),Collections.emptyList());
    }

    /**
     * Sorts the selected collection, leaving the given paths out of the sort key.
     *
     * @param sortFieldSelector      selects the collection to sort
     * @param ignoredPathsForSorting paths excluded from the sort key
     * @param <U>                    selector type
     * @return a sort rule excluding the given paths
     */
    public static <U> SortField<U> of(U sortFieldSelector, String... ignoredPathsForSorting){
        return new SortField<>(sortFieldSelector, Arrays.asList(ignoredPathsForSorting),Collections.emptyList());
    }

    /**
     * Sorts the selected collection, leaving the given path out of the sort key.
     *
     * @param sortFieldSelector      selects the collection to sort
     * @param ignoredPathsForSorting a path excluded from the sort key
     * @param <U>                    selector type
     * @return a sort rule excluding the given path
     */
    public static <U> SortField<U> of(U sortFieldSelector, String ignoredPathsForSorting){
        return new SortField<>(sortFieldSelector,Collections.singletonList(ignoredPathsForSorting),Collections.emptyList());
    }

    /**
     * Sorts the selected collection, leaving matching field names out of the sort key.
     *
     * @param sortFieldSelector               selects the collection to sort
     * @param ignoredFieldMatchersForSorting  field names matching these are excluded from the sort key
     * @param <U>                             selector type
     * @return a sort rule excluding the matching fields
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <U> SortField<U> of(U sortFieldSelector, Matcher<String>... ignoredFieldMatchersForSorting){
        return new SortField<>(sortFieldSelector,Collections.emptyList(),Arrays.asList(ignoredFieldMatchersForSorting));
    }

    /**
     * Sorts the selected collection, leaving matching field names out of the sort key.
     *
     * @param sortFieldSelector              selects the collection to sort
     * @param ignoredFieldMatchersForSorting field names matching this are excluded from the sort key
     * @param <U>                            selector type
     * @return a sort rule excluding the matching fields
     */
    public static <U> SortField<U> of(U sortFieldSelector, Matcher<String> ignoredFieldMatchersForSorting){
        return new SortField<>(sortFieldSelector,Collections.emptyList(),Collections.singletonList(ignoredFieldMatchersForSorting));
    }

    /**
     * Sorts the selected collection, excluding both a path and matching field names.
     *
     * @param sortFieldSelector              selects the collection to sort
     * @param ignoredPathsForSorting         a path excluded from the sort key
     * @param ignoredFieldMatchersForSorting field names matching this are also excluded
     * @param <U>                            selector type
     * @return a sort rule excluding both
     */
    public static <U> SortField<U> of(U sortFieldSelector, String ignoredPathsForSorting, Matcher<String> ignoredFieldMatchersForSorting){
        return new SortField<>(sortFieldSelector,Collections.singletonList(ignoredPathsForSorting),Collections.singletonList(ignoredFieldMatchersForSorting));
    }

    /**
     * Sorts the selected collection, excluding both paths and matching field names.
     *
     * @param sortFieldSelector              selects the collection to sort
     * @param ignoredPathsForSorting         paths excluded from the sort key
     * @param ignoredFieldMatchersForSorting field names matching these are also excluded
     * @param <U>                            selector type
     * @return a sort rule excluding both
     */
    public static <U> SortField<U> of(U sortFieldSelector, List<String> ignoredPathsForSorting, List<Matcher<String>> ignoredFieldMatchersForSorting){
        return new SortField<>(sortFieldSelector,ignoredPathsForSorting,ignoredFieldMatchersForSorting);
    }

    private final T sortFieldSelector;
    private final List<String> ignoredPathsForSorting;
    private final List<Matcher<String>> ignoredFieldMatchersForSorting;

    private SortField(T sortFieldSelector, List<String> ignoredPathsForSorting, List<Matcher<String>> ignoredFieldMatchersForSorting) {
        this.sortFieldSelector = sortFieldSelector;
        this.ignoredPathsForSorting = new ArrayList<>(ignoredPathsForSorting);
        this.ignoredFieldMatchersForSorting = new ArrayList<>(ignoredFieldMatchersForSorting);
    }

    /**
     * Returns a new {@code SortField} with the given path added to the fields excluded from the
     * sort key.
     *
     * <p>Derives rather than mutates. The fluent form reads as though it produces a new value, and
     * the getters hand back unmodifiable views, so mutating in place made
     * {@code base.ignoring("a")} and {@code base.ignoring("b")} silently the same object — which
     * accumulated every caller's paths when an instance was shared, order-dependently, and raced
     * under parallel execution.
     *
     * @param fieldPath path excluded from the sort key
     * @return a new rule with the path added
     */
    public SortField<T> ignoring(String fieldPath) {
        List<String> paths = new ArrayList<>(ignoredPathsForSorting);
        paths.add(fieldPath);
        return new SortField<>(sortFieldSelector, paths, ignoredFieldMatchersForSorting);
    }

    /**
     * Derives a rule excluding further paths.
     *
     * @param fieldPaths paths excluded from the sort key
     * @return a new rule with the paths added
     * @see #ignoring(String)
     */
    public SortField<T> ignoring(String... fieldPaths) {
        List<String> paths = new ArrayList<>(ignoredPathsForSorting);
        Collections.addAll(paths, fieldPaths);
        return new SortField<>(sortFieldSelector, paths, ignoredFieldMatchersForSorting);
    }

    /**
     * Derives a rule excluding further field names.
     *
     * @param matcher field names matching this are excluded from the sort key
     * @return a new rule with the matcher added
     * @see #ignoring(String)
     */
    public SortField<T> ignoring(Matcher<String> matcher) {
        List<Matcher<String>> matchers = new ArrayList<>(ignoredFieldMatchersForSorting);
        matchers.add(matcher);
        return new SortField<>(sortFieldSelector, ignoredPathsForSorting, matchers);
    }

    /**
     * Derives a rule excluding further field names.
     *
     * @param matchers field names matching these are excluded from the sort key
     * @return a new rule with the matchers added
     * @see #ignoring(String)
     */
    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    public final SortField<T> ignoring(Matcher<String>... matchers) {
        List<Matcher<String>> combined = new ArrayList<>(ignoredFieldMatchersForSorting);
        Collections.addAll(combined, matchers);
        return new SortField<>(sortFieldSelector, ignoredPathsForSorting, combined);
    }

    /**
     * Returns the selector this rule applies to.
     *
     * @return the selector identifying the collection to sort
     */
    public T getSortFieldSelector() {
        return sortFieldSelector;
    }

    /**
     * Returns the excluded paths.
     *
     * @return paths excluded from the sort key, as an unmodifiable view
     */
    public List<String> getIgnoredPathsForSorting() {
        return Collections.unmodifiableList(ignoredPathsForSorting);
    }

    /**
     * Returns the excluded field-name matchers.
     *
     * @return field-name matchers whose matches are excluded from the sort key, as an unmodifiable view
     */
    public List<Matcher<String>> getIgnoredFieldMatchersForSorting() {
        return Collections.unmodifiableList(ignoredFieldMatchersForSorting);
    }

    /**
     * Reports whether anything is excluded from the sort key.
     *
     * @return true when nothing is excluded, so the whole element forms the sort key
     */
    public boolean isEmpty(){
        return ignoredPathsForSorting.isEmpty() && ignoredFieldMatchersForSorting.isEmpty();
    }
}
