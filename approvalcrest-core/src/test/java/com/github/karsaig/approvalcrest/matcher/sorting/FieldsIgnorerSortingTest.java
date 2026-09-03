package com.github.karsaig.approvalcrest.matcher.sorting;

import com.github.karsaig.approvalcrest.FieldsIgnorer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

/**
 * Unit tests for the sorting engine in {@link FieldsIgnorer} — {@code sortJsonFields},
 * {@code applySorting}, {@code applyRootCollectionSorting} and the object-graph
 * {@code findPaths} overloads. These are the deterministic transformations behind
 * {@code .sortField(...)} / {@code .sortFieldMatcher(...)} and the implicit ordering of
 * Set/Map collections, driven here directly through the public static API for precise
 * branch coverage.
 */
public class FieldsIgnorerSortingTest {

    private static final List<SortField<Matcher<String>>> NO_MATCHERS = Collections.emptyList();

    private static JsonElement parse(String json) {
        return JsonParser.parseString(json);
    }

    /** Build a paths-to-sort map with a plain {@link SortField} per path. */
    private static Map<String, List<SortField<String>>> sortPaths(String... paths) {
        Map<String, List<SortField<String>>> map = new HashMap<>();
        for (String p : paths) {
            map.computeIfAbsent(p, k -> new ArrayList<>()).add(SortField.of(p));
        }
        return map;
    }

    private static Map<String, List<SortField<String>>> noPaths() {
        return Collections.emptyMap();
    }

    // -------------------------------------------------------------------------
    // sortJsonFields — recursive key ordering
    // -------------------------------------------------------------------------

    @Test
    void sortJsonFieldsOrdersObjectKeysAlphabetically() {
        JsonElement json = parse("{\"b\":1,\"a\":2,\"c\":3}");

        FieldsIgnorer.sortJsonFields(json, true);

        assertThat(json.toString(), is("{\"a\":2,\"b\":1,\"c\":3}"));
    }

    @Test
    void sortJsonFieldsIsNoOpWhenSortFileFalse() {
        JsonElement json = parse("{\"b\":1,\"a\":2}");

        FieldsIgnorer.sortJsonFields(json, false);

        assertThat(json.toString(), is("{\"b\":1,\"a\":2}"));
    }

    @Test
    void sortJsonFieldsRecursesIntoNestedObjects() {
        JsonElement json = parse("{\"outer\":{\"y\":1,\"x\":2}}");

        FieldsIgnorer.sortJsonFields(json, true);

        assertThat(json.toString(), is("{\"outer\":{\"x\":2,\"y\":1}}"));
    }

    @Test
    void sortJsonFieldsRecursesIntoArraysAndSkipsNulls() {
        JsonElement json = parse("{\"list\":[null,{\"b\":1,\"a\":2}]}");

        FieldsIgnorer.sortJsonFields(json, true);

        assertThat(json.toString(), is("{\"list\":[null,{\"a\":2,\"b\":1}]}"));
    }

    @Test
    void sortJsonFieldsHandlesNullElementGracefully() {
        FieldsIgnorer.sortJsonFields(JsonNull.INSTANCE, true);
        FieldsIgnorer.sortJsonFields(null, true);
        // no exception == pass
    }

    // -------------------------------------------------------------------------
    // applySorting — path/field-matcher driven array ordering
    // -------------------------------------------------------------------------

    @Test
    void applySortingSortsArrayByConfiguredPath() {
        JsonElement json = parse("{\"list\":[{\"v\":3},{\"v\":1},{\"v\":2}]}");

        FieldsIgnorer.applySorting(json, sortPaths("list"), NO_MATCHERS, true);

        assertThat(json.toString(), is("{\"list\":[{\"v\":1},{\"v\":2},{\"v\":3}]}"));
    }

    @Test
    void applySortingDescendsThroughGraphAdapterEnvelopeKey() {
        JsonElement json = parse("{\"0x1\":{\"list\":[{\"v\":2},{\"v\":1}]}}");

        FieldsIgnorer.applySorting(json, sortPaths("list"), NO_MATCHERS, true);

        assertThat(json.getAsJsonObject().getAsJsonObject("0x1").getAsJsonArray("list").toString(),
                is("[{\"v\":1},{\"v\":2}]"));
    }

    // -------------------------------------------------------------------------
    // A trailing * is a literal key here too
    //
    // The rule is pinned for .ignoring() and ignoringElementsWhere(); for sortField it lived only in
    // docs/sorting.md, which is the one place a wrong claim would go unnoticed.
    // -------------------------------------------------------------------------

    @Test
    void trailingWildcardSortsOnlyTheKeyLiterallyNamedStar() {
        JsonElement json = parse("{\"map\":{\"*\":[3,1],\"k1\":[3,1]}}");

        FieldsIgnorer.applySorting(json, sortPaths("map.*"), NO_MATCHERS, true);

        // Only the entry keyed "*" is sorted; the sibling keeps the order it was given.
        assertThat(json.toString(), is("{\"map\":{\"*\":[1,3],\"k1\":[3,1]}}"));
    }

    @Test
    void nonFinalWildcardSortsUnderEveryMapValue() {
        // The contrast on the same shape: with a segment after it, * fans out over every value.
        JsonElement json = parse("{\"map\":{\"*\":{\"l\":[3,1]},\"k1\":{\"l\":[3,1]}}}");

        FieldsIgnorer.applySorting(json, sortPaths("map.*.l"), NO_MATCHERS, true);

        assertThat(json.toString(), is("{\"map\":{\"*\":{\"l\":[1,3]},\"k1\":{\"l\":[1,3]}}}"));
    }

    @Test
    void trailingWildcardWithoutALiteralStarKeySortsNothing() {
        // The accepted limitation: someone writing map.* expecting every value gets a no-op.
        JsonElement json = parse("{\"map\":{\"k1\":[3,1]}}");

        FieldsIgnorer.applySorting(json, sortPaths("map.*"), NO_MATCHERS, true);

        assertThat(json.toString(), is("{\"map\":{\"k1\":[3,1]}}"));
    }

    @Test
    void trailingWildcardSortsALiteralStarKeyInASerialisedMap() {
        // A real Map serialises to an array of single-entry objects, so cover that shape too.
        JsonElement json = parse("{\"map\":[{\"*\":[3,1]},{\"k1\":[3,1]}]}");

        FieldsIgnorer.applySorting(json, sortPaths("map.*"), NO_MATCHERS, true);

        assertThat(json.toString(), is("{\"map\":[{\"*\":[1,3]},{\"k1\":[3,1]}]}"));
    }

    @Test
    void applySortingSortsByFieldNameMatcher() {
        JsonElement json = parse("{\"myList\":[{\"v\":2},{\"v\":1}]}");
        List<SortField<Matcher<String>>> matchers =
                Arrays.asList(SortField.of(startsWith("my")));

        FieldsIgnorer.applySorting(json, noPaths(), matchers, true);

        assertThat(json.getAsJsonObject().getAsJsonArray("myList").toString(),
                is("[{\"v\":1},{\"v\":2}]"));
    }

    @Test
    void applySortingSortsRootArrayViaEmptyPath() {
        JsonElement json = parse("[{\"v\":3},{\"v\":1},{\"v\":2}]");

        FieldsIgnorer.applySorting(json, sortPaths(""), NO_MATCHERS, true);

        assertThat(json.toString(), is("[{\"v\":1},{\"v\":2},{\"v\":3}]"));
    }

    @Test
    void applySortingSortsNestedArraysBottomUp() {
        // Inner arrays are sorted before the outer array computes its key.
        JsonElement json = parse("{\"outer\":[[{\"v\":2},{\"v\":1}],[{\"v\":1}]]}");

        FieldsIgnorer.applySorting(json, sortPaths("outer"), NO_MATCHERS, true);

        // Each inner array is sorted; the outer array is ordered by the resulting key string
        // ("[{\"v\":1},{\"v\":2}]" sorts before "[{\"v\":1}]" because ',' < ']').
        assertThat(json.getAsJsonObject().getAsJsonArray("outer").toString(),
                is("[[{\"v\":1},{\"v\":2}],[{\"v\":1}]]"));
    }

    @Test
    void applySortingIsNoOpForNull() {
        FieldsIgnorer.applySorting(JsonNull.INSTANCE, sortPaths("x"), NO_MATCHERS, true);
        FieldsIgnorer.applySorting(null, sortPaths("x"), NO_MATCHERS, true);
    }

    @Test
    void applySortingStripsIgnoredFieldFromSortKey() {
        // SortField.ignoring("id") means the sort key ignores id → order by remaining fields.
        JsonElement json = parse("{\"list\":[{\"id\":9,\"name\":\"b\"},{\"id\":1,\"name\":\"a\"}]}");
        Map<String, List<SortField<String>>> paths = new HashMap<>();
        paths.put("list", Arrays.asList(SortField.of("list").ignoring("id")));

        FieldsIgnorer.applySorting(json, paths, NO_MATCHERS, true);

        // "a" sorts before "b" despite id 1 vs 9 being irrelevant to the key.
        assertThat(json.getAsJsonObject().getAsJsonArray("list").get(0).getAsJsonObject()
                .get("name").getAsString(), is("a"));
    }

    // -------------------------------------------------------------------------
    // applyRootCollectionSorting — Set/Map/Collection/type-based root ordering
    // -------------------------------------------------------------------------

    @Test
    void rootSetIsAlwaysSorted() {
        JsonElement json = parse("[{\"v\":2},{\"v\":1}]");
        Set<Object> setForTypeCheck = new LinkedHashSet<>(Arrays.asList("a", "b"));

        FieldsIgnorer.applyRootCollectionSorting(json, setForTypeCheck, NO_MATCHERS, noPaths());

        assertThat(json.toString(), is("[{\"v\":1},{\"v\":2}]"));
    }

    @Test
    void rootMapIsAlwaysSorted() {
        JsonElement json = parse("[{\"v\":2},{\"v\":1}]");
        Map<String, String> mapForTypeCheck = new LinkedHashMap<>();
        mapForTypeCheck.put("k", "v");

        FieldsIgnorer.applyRootCollectionSorting(json, mapForTypeCheck, NO_MATCHERS, noPaths());

        assertThat(json.toString(), is("[{\"v\":1},{\"v\":2}]"));
    }

    @Test
    void rootListIsSortedOnlyWhenExplicitlyConfigured() {
        JsonElement unconfigured = parse("[{\"v\":2},{\"v\":1}]");
        List<String> listForTypeCheck = Arrays.asList("a", "b");

        FieldsIgnorer.applyRootCollectionSorting(unconfigured, listForTypeCheck, NO_MATCHERS, noPaths());
        // No "" path and no field matchers → List left in original order.
        assertThat(unconfigured.toString(), is("[{\"v\":2},{\"v\":1}]"));

        JsonElement configured = parse("[{\"v\":2},{\"v\":1}]");
        FieldsIgnorer.applyRootCollectionSorting(configured, listForTypeCheck, NO_MATCHERS, sortPaths(""));
        assertThat(configured.toString(), is("[{\"v\":1},{\"v\":2}]"));
    }

    @Test
    void rootListIsSortedByTypeWhenTypeMatches() {
        JsonElement json = parse("[{\"v\":2},{\"v\":1}]");
        List<String> listForTypeCheck = Arrays.asList("a", "b");

        FieldsIgnorer.applyRootCollectionSorting(json, listForTypeCheck, NO_MATCHERS, noPaths(),
                Collections.<Class<?>>singletonList(String.class));

        assertThat(json.toString(), is("[{\"v\":1},{\"v\":2}]"));
    }

    @Test
    void rootListIsNotSortedWhenTypeDoesNotMatch() {
        JsonElement json = parse("[{\"v\":2},{\"v\":1}]");
        List<String> listForTypeCheck = Arrays.asList("a", "b");

        FieldsIgnorer.applyRootCollectionSorting(json, listForTypeCheck, NO_MATCHERS, noPaths(),
                Collections.<Class<?>>singletonList(Integer.class));

        assertThat(json.toString(), is("[{\"v\":2},{\"v\":1}]"));
    }

    @Test
    void rootCollectionSortingIgnoresNonCollectionTypeCheck() {
        JsonElement json = parse("[{\"v\":2},{\"v\":1}]");

        // A non-collection objectForTypeCheck (and null) must be a no-op.
        FieldsIgnorer.applyRootCollectionSorting(json, "not-a-collection", NO_MATCHERS, sortPaths(""));
        FieldsIgnorer.applyRootCollectionSorting(json, null, NO_MATCHERS, sortPaths(""));

        assertThat(json.toString(), is("[{\"v\":2},{\"v\":1}]"));
    }

    // -------------------------------------------------------------------------
    // findPaths(object graph) — combined ignore + sort
    // -------------------------------------------------------------------------

    static class Bean {
        final int keep;
        final int drop;

        Bean(int keep, int drop) {
            this.keep = keep;
            this.drop = drop;
        }
    }

    static class Root {
        final String drop;
        final List<Bean> list;

        Root(String drop, List<Bean> list) {
            this.drop = drop;
            this.list = list;
        }
    }

    @Test
    void findPathsFromGsonIgnoresFieldAndSortsList() {
        Gson gson = new Gson();
        Root root = new Root("secret", Arrays.asList(bean(3), bean(1), bean(2)));

        JsonElement result = FieldsIgnorer.findPaths(gson, root, set("drop"), NO_MATCHERS, sortPaths("list"));

        // ignored field removed, list retained and sorted ascending by keep
        assertThat(result.getAsJsonObject().has("drop"), is(false));
        assertThat(result.getAsJsonObject().has("list"), is(true));
        JsonArray list = result.getAsJsonObject().getAsJsonArray("list");
        assertThat(list.get(0).getAsJsonObject().get("keep").getAsInt(), is(1));
        assertThat(list.get(2).getAsJsonObject().get("keep").getAsInt(), is(3));
    }

    @Test
    void applySortingHandlesNestedSortPath() {
        // Path "a.b" exercises getPathsMap's dotted-path split and nested descent.
        JsonElement json = parse("{\"a\":{\"b\":[{\"v\":2},{\"v\":1}]}}");
        Map<String, List<SortField<String>>> paths = sortPaths("a.b");

        FieldsIgnorer.applySorting(json, paths, NO_MATCHERS, true);

        assertThat(json.getAsJsonObject().getAsJsonObject("a").getAsJsonArray("b").toString(),
                is("[{\"v\":1},{\"v\":2}]"));
    }

    @Test
    void applySortingByFieldMatcherIgnoringNestedField() {
        // SortField<Matcher>.ignoring(matcher) strips matching fields from the sort key.
        JsonElement json = parse("{\"myList\":[{\"id\":9,\"name\":\"b\"},{\"id\":1,\"name\":\"a\"}]}");
        List<SortField<Matcher<String>>> matchers =
                Arrays.asList(SortField.of(startsWith("my")).ignoring(is("id")));

        FieldsIgnorer.applySorting(json, noPaths(), matchers, true);

        // id ignored in the key → ordered by name (a before b).
        assertThat(json.getAsJsonObject().getAsJsonArray("myList").get(0).getAsJsonObject()
                .get("name").getAsString(), is("a"));
    }

    @Test
    void findPathsFromGsonSortsListRootByEmptyPath() {
        Gson gson = new Gson();
        List<Bean> list = Arrays.asList(bean(3), bean(1), bean(2));

        JsonElement result = FieldsIgnorer.findPaths(gson, list, set(), NO_MATCHERS, sortPaths(""));

        JsonArray arr = result.getAsJsonArray();
        assertThat(arr.get(0).getAsJsonObject().get("keep").getAsInt(), is(1));
        assertThat(arr.get(2).getAsJsonObject().get("keep").getAsInt(), is(3));
    }

    @Test
    void findPathsFromGsonSortsSetRoot() {
        Gson gson = new Gson();
        Set<Bean> set = new LinkedHashSet<>(Arrays.asList(bean(2, 0), bean(1, 0)));

        JsonElement result = FieldsIgnorer.findPaths(gson, set, set(), NO_MATCHERS, noPaths());

        // Set root is always sorted regardless of configuration.
        JsonArray arr = result.getAsJsonArray();
        assertThat(arr.get(0).getAsJsonObject().get("keep").getAsInt(), is(1));
    }

    @Test
    void findPathsFromPreComputedJsonSortsCollectionRootByEmptyPath() {
        JsonElement pre = parse("[{\"v\":3},{\"v\":1},{\"v\":2}]");
        List<String> listForTypeCheck = Arrays.asList("a");

        JsonElement result = FieldsIgnorer.findPaths(pre, listForTypeCheck, set(), NO_MATCHERS, sortPaths(""));

        assertThat(result.toString(), is("[{\"v\":1},{\"v\":2},{\"v\":3}]"));
    }

    @Test
    void findPathsFromPreComputedJsonSortsMapRoot() {
        JsonElement pre = parse("[{\"v\":2},{\"v\":1}]");
        Map<String, String> mapForTypeCheck = new LinkedHashMap<>();
        mapForTypeCheck.put("k", "v");

        JsonElement result = FieldsIgnorer.findPaths(pre, mapForTypeCheck, set(), NO_MATCHERS, noPaths());

        assertThat(result.toString(), is("[{\"v\":1},{\"v\":2}]"));
    }

    // --- helpers ---

    private static Bean bean(int keep) {
        return new Bean(keep, 0);
    }

    private static Bean bean(int keep, int drop) {
        return new Bean(keep, drop);
    }

    private static Set<String> set(String... s) {
        return new LinkedHashSet<>(Arrays.asList(s));
    }

    // -------------------------------------------------------------------------
    // A * in a SortField's ignored sub-paths
    // -------------------------------------------------------------------------

    private static JsonElement twoWrappedLeaves() {
        return JsonParser.parseString("{\"list\":[{\"o\":{\"id\":1,\"v\":\"zz\"}},{\"o\":{\"id\":2,\"v\":\"aa\"}}]}");
    }

    private static JsonElement sortedIgnoring(String ignoredPath) {
        JsonElement root = twoWrappedLeaves();
        Map<String, List<SortField<String>>> paths = new LinkedHashMap<>();
        paths.put("list", Collections.singletonList(
                ignoredPath == null ? SortField.of("list") : SortField.of("list", ignoredPath)));
        FieldsIgnorer.applySorting(root, paths, Collections.<SortField<Matcher<String>>>emptyList(), true);
        return root;
    }

    /**
     * A {@code SortField}'s ignored sub-paths take a {@code *} segment too, so the field can be kept out
     * of the sort key under every child rather than under one named one. Without it the {@code *} is
     * read as a literal key name, no child matches, and the field stays in the key silently — writing
     * an order nobody asked for into the approved file.
     * <p>
     * The three cases together are what make this meaningful: id in the key sorts by id, id out of it
     * sorts by v, and the wildcard must land on the latter.
     */
    @Test
    void wildcardInIgnoredSortPathsExcludesTheFieldUnderEveryChild() {
        // Control: with id in the sort key the elements order by id.
        assertThat(sortedIgnoring(null).toString(),
                is("{\"list\":[{\"o\":{\"id\":1,\"v\":\"zz\"}},{\"o\":{\"id\":2,\"v\":\"aa\"}}]}"));

        // Naming the child excludes id, so they order by v instead.
        String byValue = "{\"list\":[{\"o\":{\"id\":2,\"v\":\"aa\"}},{\"o\":{\"id\":1,\"v\":\"zz\"}}]}";
        assertThat(sortedIgnoring("o.id").toString(), is(byValue));

        // The wildcard must do the same without naming the child.
        assertThat(sortedIgnoring("*.id").toString(), is(byValue));
    }
}
