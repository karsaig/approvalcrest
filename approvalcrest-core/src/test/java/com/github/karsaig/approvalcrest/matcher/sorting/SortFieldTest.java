package com.github.karsaig.approvalcrest.matcher.sorting;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link SortField} factory overloads and the {@code ignoring(...)} fluent API.
 * These cover the configuration permutations a user has when telling the matcher how to sort a
 * collection and which nested paths/fields to leave out of the sort key.
 */
public class SortFieldTest {

    @Test
    void ofWithSelectorOnlyHasNoIgnoreRules() {
        SortField<String> sortField = SortField.of("path.to.list");

        assertThat(sortField.getSortFieldSelector(), is("path.to.list"));
        assertThat(sortField.getIgnoredPathsForSorting(), hasSize(0));
        assertThat(sortField.getIgnoredFieldMatchersForSorting(), hasSize(0));
        assertThat(sortField.isEmpty(), is(true));
    }

    @Test
    void ofWithSingleIgnoredPath() {
        SortField<String> sortField = SortField.of("list", "list.id");

        assertThat(sortField.getIgnoredPathsForSorting(), contains("list.id"));
        assertThat(sortField.isEmpty(), is(false));
    }

    @Test
    void ofWithVarargIgnoredPaths() {
        SortField<String> sortField = SortField.of("list", "list.id", "list.timestamp");

        assertThat(sortField.getIgnoredPathsForSorting(), contains("list.id", "list.timestamp"));
        assertThat(sortField.isEmpty(), is(false));
    }

    @Test
    void ofWithSingleFieldMatcher() {
        Matcher<String> matcher = startsWith("id");
        SortField<String> sortField = SortField.of("list", matcher);

        List<Matcher<String>> matchers = sortField.getIgnoredFieldMatchersForSorting();
        assertThat(matchers, hasSize(1));
        assertThat(matchers.get(0) == matcher, is(true));
        assertThat(sortField.getIgnoredPathsForSorting(), hasSize(0));
        assertThat(sortField.isEmpty(), is(false));
    }

    @Test
    void ofWithVarargFieldMatchers() {
        Matcher<String> first = startsWith("id");
        Matcher<String> second = startsWith("ts");
        SortField<String> sortField = SortField.of("list", first, second);

        List<Matcher<String>> matchers = sortField.getIgnoredFieldMatchersForSorting();
        assertThat(matchers, hasSize(2));
        assertThat(matchers.get(0) == first, is(true));
        assertThat(matchers.get(1) == second, is(true));
    }

    @Test
    void ofWithBothPathAndMatcher() {
        Matcher<String> matcher = startsWith("id");
        SortField<String> sortField = SortField.of("list", "list.id", matcher);

        assertThat(sortField.getIgnoredPathsForSorting(), contains("list.id"));
        List<Matcher<String>> matchers = sortField.getIgnoredFieldMatchersForSorting();
        assertThat(matchers, hasSize(1));
        assertThat(matchers.get(0) == matcher, is(true));
    }

    @Test
    void ofWithListsOfPathsAndMatchers() {
        Matcher<String> matcher = startsWith("id");
        List<String> paths = Arrays.asList("a", "b");
        List<Matcher<String>> inputMatchers = Arrays.asList(matcher);

        SortField<String> sortField = SortField.of("list", paths, inputMatchers);

        assertThat(sortField.getIgnoredPathsForSorting(), contains("a", "b"));
        List<Matcher<String>> matchers = sortField.getIgnoredFieldMatchersForSorting();
        assertThat(matchers, hasSize(1));
        assertThat(matchers.get(0) == matcher, is(true));
    }

    @Test
    void ignoringChainsMultiplePaths() {
        SortField<String> sortField = SortField.of("list")
                .ignoring("list.id")
                .ignoring("list.a", "list.b");

        assertThat(sortField.getIgnoredPathsForSorting(), contains("list.id", "list.a", "list.b"));
        assertThat(sortField.isEmpty(), is(false));
    }

    @Test
    void ignoringChainsMultipleMatchers() {
        Matcher<String> first = startsWith("id");
        Matcher<String> second = startsWith("ts");
        Matcher<String> third = startsWith("x");

        SortField<String> sortField = SortField.of("list")
                .ignoring(first)
                .ignoring(second, third);

        List<Matcher<String>> matchers = sortField.getIgnoredFieldMatchersForSorting();
        assertThat(matchers, hasSize(3));
        assertThat(matchers.get(0) == first, is(true));
        assertThat(matchers.get(1) == second, is(true));
        assertThat(matchers.get(2) == third, is(true));
    }

    @Test
    void getIgnoredPathsReturnsUnmodifiableView() {
        SortField<String> sortField = SortField.of("list", "list.id");

        assertThrows(UnsupportedOperationException.class,
                () -> sortField.getIgnoredPathsForSorting().add("extra"));
    }

    @Test
    void getIgnoredFieldMatchersReturnsUnmodifiableView() {
        SortField<String> sortField = SortField.of("list", startsWith("id"));

        assertThrows(UnsupportedOperationException.class,
                () -> sortField.getIgnoredFieldMatchersForSorting().add(startsWith("x")));
    }
    // --- deriving must not mutate the original ---

    /**
     * The fluent form reads as though it produces a new value, and the getters return unmodifiable
     * views, so deriving has to leave the original alone. When it mutated in place, a SortField held
     * in shared configuration accumulated every caller's paths, order-dependently.
     */
    @Test
    void ignoringAPathDerivesRatherThanMutating() {
        SortField<String> base = SortField.of("path.to.list");

        SortField<String> first = base.ignoring("p1");
        SortField<String> second = base.ignoring("p2");

        assertThat(base.getIgnoredPathsForSorting(), hasSize(0));
        assertThat(first.getIgnoredPathsForSorting(), contains("p1"));
        assertThat(second.getIgnoredPathsForSorting(), contains("p2"));
    }

    @Test
    void ignoringMatchersDerivesRatherThanMutating() {
        Matcher<String> one = startsWith("a");
        Matcher<String> two = startsWith("b");
        SortField<String> base = SortField.of("path.to.list");

        SortField<String> first = base.ignoring(one);
        SortField<String> second = base.ignoring(two);

        assertThat(base.getIgnoredFieldMatchersForSorting(), hasSize(0));
        assertThat(first.getIgnoredFieldMatchersForSorting(), hasSize(1));
        assertSame(one, first.getIgnoredFieldMatchersForSorting().get(0));
        assertThat(second.getIgnoredFieldMatchersForSorting(), hasSize(1));
        assertSame(two, second.getIgnoredFieldMatchersForSorting().get(0));
    }

    @Test
    void varargsOverloadsAlsoDerive() {
        SortField<String> base = SortField.of("path.to.list");

        SortField<String> paths = base.ignoring("p1", "p2");
        SortField<String> matchers = base.ignoring(startsWith("a"), startsWith("b"));

        assertThat(base.isEmpty(), is(true));
        assertThat(paths.getIgnoredPathsForSorting(), contains("p1", "p2"));
        assertThat(matchers.getIgnoredFieldMatchersForSorting(), hasSize(2));
    }

    /** Chaining still accumulates, which is how the fluent API is normally used. */
    @Test
    void chainingAccumulatesOntoTheDerivedInstance() {
        SortField<String> chained = SortField.of("path.to.list").ignoring("p1").ignoring("p2");

        assertThat(chained.getIgnoredPathsForSorting(), contains("p1", "p2"));
    }
}
