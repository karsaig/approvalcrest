package com.github.karsaig.approvalcrest.matcher.sorting;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SortField<T> {

    public static <U> SortField<U> of(U sortFieldSelector){
        return new SortField<>(sortFieldSelector,Collections.emptyList(),Collections.emptyList());
    }

    public static <U> SortField<U> of(U sortFieldSelector, String... ignoredPathsForSorting){
        return new SortField<>(sortFieldSelector, Arrays.asList(ignoredPathsForSorting),Collections.emptyList());
    }

    public static <U> SortField<U> of(U sortFieldSelector, String ignoredPathsForSorting){
        return new SortField<>(sortFieldSelector,Collections.singletonList(ignoredPathsForSorting),Collections.emptyList());
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <U> SortField<U> of(U sortFieldSelector, Matcher<String>... ignoredFieldMatchersForSorting){
        return new SortField<>(sortFieldSelector,Collections.emptyList(),Arrays.asList(ignoredFieldMatchersForSorting));
    }

    public static <U> SortField<U> of(U sortFieldSelector, Matcher<String> ignoredFieldMatchersForSorting){
        return new SortField<>(sortFieldSelector,Collections.emptyList(),Collections.singletonList(ignoredFieldMatchersForSorting));
    }

    public static <U> SortField<U> of(U sortFieldSelector, String ignoredPathsForSorting, Matcher<String> ignoredFieldMatchersForSorting){
        return new SortField<>(sortFieldSelector,Collections.singletonList(ignoredPathsForSorting),Collections.singletonList(ignoredFieldMatchersForSorting));
    }

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
     */
    public SortField<T> ignoring(String fieldPath) {
        List<String> paths = new ArrayList<>(ignoredPathsForSorting);
        paths.add(fieldPath);
        return new SortField<>(sortFieldSelector, paths, ignoredFieldMatchersForSorting);
    }

    /** @see #ignoring(String) */
    public SortField<T> ignoring(String... fieldPaths) {
        List<String> paths = new ArrayList<>(ignoredPathsForSorting);
        Collections.addAll(paths, fieldPaths);
        return new SortField<>(sortFieldSelector, paths, ignoredFieldMatchersForSorting);
    }

    /** @see #ignoring(String) */
    public SortField<T> ignoring(Matcher<String> matcher) {
        List<Matcher<String>> matchers = new ArrayList<>(ignoredFieldMatchersForSorting);
        matchers.add(matcher);
        return new SortField<>(sortFieldSelector, ignoredPathsForSorting, matchers);
    }

    /** @see #ignoring(String) */
    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    public final SortField<T> ignoring(Matcher<String>... matchers) {
        List<Matcher<String>> combined = new ArrayList<>(ignoredFieldMatchersForSorting);
        Collections.addAll(combined, matchers);
        return new SortField<>(sortFieldSelector, ignoredPathsForSorting, combined);
    }

    public T getSortFieldSelector() {
        return sortFieldSelector;
    }

    public List<String> getIgnoredPathsForSorting() {
        return Collections.unmodifiableList(ignoredPathsForSorting);
    }

    public List<Matcher<String>> getIgnoredFieldMatchersForSorting() {
        return Collections.unmodifiableList(ignoredFieldMatchersForSorting);
    }

    public boolean isEmpty(){
        return ignoredPathsForSorting.isEmpty() && ignoredFieldMatchersForSorting.isEmpty();
    }
}
