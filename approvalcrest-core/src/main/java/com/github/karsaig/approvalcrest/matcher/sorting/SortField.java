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

    public SortField<T> ignoring(String fieldPath) {
        this.ignoredPathsForSorting.add(fieldPath);
        return this;
    }

    public SortField<T> ignoring(String... fieldPaths) {
        Collections.addAll(this.ignoredPathsForSorting, fieldPaths);
        return this;
    }

    public SortField<T> ignoring(Matcher<String> matcher) {
        this.ignoredFieldMatchersForSorting.add(matcher);
        return this;
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    public final SortField<T> ignoring(Matcher<String>... matchers) {
        Collections.addAll(this.ignoredFieldMatchersForSorting, matchers);
        return this;
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
