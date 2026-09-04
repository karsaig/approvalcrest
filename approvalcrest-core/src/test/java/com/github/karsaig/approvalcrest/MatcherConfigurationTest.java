package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

/**
 * Unit tests for {@link MatcherConfiguration}, the mutable accumulator behind the matcher fluent
 * API. The bulk (array / {@link java.util.Collection}) overloads and the alias-merge helpers are
 * the untested business paths — this verifies each accumulates the expected configuration.
 */
public class MatcherConfigurationTest {

    // --- paths to ignore ---

    @Test
    void addPathToIgnoreAcceptsArray() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPathToIgnore(new String[]{"a.b", "c.d"});

        assertThat(configuration.getPathsToIgnore(), containsInAnyOrder("a.b", "c.d"));
    }

    @Test
    void addPathToIgnoreAcceptsCollection() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPathToIgnore(Arrays.asList("a.b", "c.d"));

        assertThat(configuration.getPathsToIgnore(), containsInAnyOrder("a.b", "c.d"));
    }

    // --- custom matchers: which mode removes the field ---

    @Test
    void addCustomMatcherMarksThePathForRemoval() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addCustomMatcher("a.b", is("x"));

        assertThat(configuration.getCustomMatchers(), hasKey("a.b"));
        assertThat(configuration.getCustomMatcherPathsToIgnore(), containsInAnyOrder("a.b"));
    }

    @Test
    void addAdditionalCustomMatcherLeavesThePathInTheComparison() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addAdditionalCustomMatcher("a.b", is("x"));

        assertThat(configuration.getCustomMatchers(), hasKey("a.b"));
        assertThat(configuration.getCustomMatcherPathsToIgnore(), hasSize(0));
    }

    /**
     * The two collections have to stay in step, because registering the same path twice already means the last
     * call wins on the map -- the removal flag must follow it in both directions.
     */
    @Test
    void registeringAdditionallyAfterReplacingTakesThePathBackOutOfTheRemovalSet() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addCustomMatcher("a.b", is("x"))
                .addAdditionalCustomMatcher("a.b", is("y"));

        assertThat(configuration.getCustomMatcherPathsToIgnore(), hasSize(0));
        // matcher instances are not equal to each other, so assert which one is stored by what it accepts
        assertThat(configuration.getCustomMatchers().get("a.b").matches("y"), is(true));
        assertThat(configuration.getCustomMatchers().get("a.b").matches("x"), is(false));
    }

    @Test
    void registeringReplacingAfterAdditionallyPutsThePathBackIntoTheRemovalSet() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addAdditionalCustomMatcher("a.b", is("x"))
                .addCustomMatcher("a.b", is("y"));

        assertThat(configuration.getCustomMatcherPathsToIgnore(), containsInAnyOrder("a.b"));
        // matcher instances are not equal to each other, so assert which one is stored by what it accepts
        assertThat(configuration.getCustomMatchers().get("a.b").matches("y"), is(true));
        assertThat(configuration.getCustomMatchers().get("a.b").matches("x"), is(false));
    }

    @Test
    void addCustomMatcherPatternMarksThePatternForRemoval() {
        Matcher<String> pattern = startsWith("gen");
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addCustomMatcherPattern(pattern, is("x"));

        assertThat(configuration.getCustomMatcherPatterns(), hasSize(1));
        assertThat(configuration.getCustomMatcherPatternsToIgnore(), hasSize(1));
    }

    @Test
    void addAdditionalCustomMatcherPatternLeavesTheMatchedFieldsInTheComparison() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addAdditionalCustomMatcherPattern(startsWith("gen"), is("x"));

        assertThat(configuration.getCustomMatcherPatterns(), hasSize(1));
        assertThat(configuration.getCustomMatcherPatternsToIgnore(), hasSize(0));
    }

    // --- types to ignore ---

    @Test
    void addTypeToIgnoreAcceptsArrayAndCollection() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addTypeToIgnore(new Class<?>[]{String.class})
                .addTypeToIgnore(Arrays.asList(Integer.class, Long.class));

        assertThat(configuration.getTypesToIgnore(),
                containsInAnyOrder(String.class, Integer.class, Long.class));
    }

    // --- types to sort ---

    @Test
    void addTypeToSortAcceptsArrayAndCollection() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addTypeToSort(new Class<?>[]{String.class})
                .addTypeToSort(Arrays.asList(Integer.class));

        assertThat(configuration.getTypesToSort(), containsInAnyOrder(String.class, Integer.class));
    }

    // --- patterns to ignore ---

    @Test
    void addPatternToIgnoreAcceptsArrayAndCollection() {
        Matcher<String> first = startsWith("id");
        Matcher<String> second = startsWith("ts");
        Matcher<String> third = startsWith("x");

        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPatternToIgnore(asMatcherArray(first, second))
                .addPatternToIgnore(Arrays.asList(third));

        // Matcher instances can't be used as expected values in contains(...) (Hamcrest would
        // treat them as element matchers), so assert on size and membership by identity.
        assertThat(configuration.getPatternsToIgnore(), hasSize(3));
        assertThat(configuration.getPatternsToIgnore().contains(first), is(true));
        assertThat(configuration.getPatternsToIgnore().contains(second), is(true));
        assertThat(configuration.getPatternsToIgnore().contains(third), is(true));
    }

    // --- patterns to sort ---

    @Test
    void addPatternToSortAcceptsMatcherArray() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPatternToSort(asMatcherArray(startsWith("id"), startsWith("ts")));

        assertThat(configuration.getPatternsToSort(), hasSize(2));
    }

    @Test
    void addPatternToSortAcceptsMatcherCollection() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPatternToSort(Arrays.asList(startsWith("id"), startsWith("ts")));

        assertThat(configuration.getPatternsToSort(), hasSize(2));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void addPatternToSortAcceptsSortFieldArray() {
        SortField<Matcher<String>> first = SortField.of(startsWith("id"));
        SortField<Matcher<String>> second = SortField.of(startsWith("ts"));

        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPatternToSort(new SortField[]{first, second});

        assertThat(configuration.getPatternsToSort(), containsInAnyOrder(first, second));
    }

    // --- paths to sort ---

    @Test
    void addPathToSortAcceptsArrayAndCollection() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPathToSort(new String[]{"a"})
                .addPathToSort(Arrays.asList("b", "c"));

        assertThat(configuration.getPathsToSort(), hasKey("a"));
        assertThat(configuration.getPathsToSort(), hasKey("b"));
        assertThat(configuration.getPathsToSort(), hasKey("c"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void addPathToSortAcceptsSortFieldArray() {
        SortField<String> a = SortField.of("a");
        SortField<String> b = SortField.of("b");

        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPathToSort(new SortField[]{a, b});

        assertThat(configuration.getPathsToSort(), hasKey("a"));
        assertThat(configuration.getPathsToSort(), hasKey("b"));
    }

    @Test
    void addPathToSortSamePathAccumulatesSortFields() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addPathToSort("a")
                .addPathToSort("a");

        assertThat(configuration.getPathsToSort().get("a"), hasSize(2));
    }

    // --- circular reference skip ---

    @Test
    void pathIsSkippedForCircularReferenceCheckByDefault() {
        MatcherConfiguration configuration = new MatcherConfiguration();

        // The constructor registers a skip rule for Path; verify it fires for a Path instance.
        boolean anySkips = configuration.getSkipCircularReferenceCheck().stream()
                .anyMatch(rule -> rule.apply(Paths.get(".")));

        assertThat(anySkips, is(true));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void addSkipCircularReferenceCheckerAcceptsArray() {
        MatcherConfiguration configuration = new MatcherConfiguration();
        int before = configuration.getSkipCircularReferenceCheck().size();

        configuration.addSkipCircularReferenceChecker(new java.util.function.Function[]{
                (java.util.function.Function<Object, Boolean>) o -> o instanceof String
        });

        assertThat(configuration.getSkipCircularReferenceCheck(), hasSize(before + 1));
    }

    // --- aliases ---

    @Test
    void addAliasValueOnlyMerges() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addAlias("secret-value", "<secret>");

        AliasMap aliasMap = configuration.getAliasMap();
        assertThat(aliasMap.resolve("x.field", "field", "secret-value"), is(Optional.of("<secret>")));
    }

    @Test
    void addAliasFieldScopedMerges() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addAlias("id", "abc-123", "<userId>");

        AliasMap aliasMap = configuration.getAliasMap();
        assertThat(aliasMap.resolve("user.id", "id", "abc-123"), is(Optional.of("<userId>")));
        assertThat(aliasMap.resolve("user.name", "name", "abc-123"), is(Optional.empty()));
    }

    @Test
    void addAliasMapLastAddedWins() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addAlias("target", "<first>")
                .addAliasMap(AliasMap.builder().add("target", "<second>").build());

        assertThat(configuration.getAliasMap().resolve("x", "f", "target"), is(Optional.of("<second>")));
    }

    // --- serialize nulls ---

    @Test
    void serializeNullsDefaultsToTrueAndCanBeOverridden() {
        MatcherConfiguration configuration = new MatcherConfiguration();
        assertThat(configuration.isSerializeNulls(), is(true));

        configuration.setSerializeNulls(false);
        assertThat(configuration.isSerializeNulls(), is(false));
    }

    // --- element ignore rules ---

    @Test
    void addElementIgnoreRuleAccumulates() {
        MatcherConfiguration configuration = new MatcherConfiguration()
                .addElementIgnoreRule("entry.tag.system", "http://example.com")
                .addElementIgnoreRule("entry.tag.system", startsWith("urn:"));

        assertThat(configuration.getElementIgnoreRules(), hasSize(2));
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    private static Matcher<String>[] asMatcherArray(Matcher<String>... matchers) {
        return matchers;
    }
}
