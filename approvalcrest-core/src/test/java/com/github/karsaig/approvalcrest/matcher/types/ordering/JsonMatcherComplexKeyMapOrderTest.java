package com.github.karsaig.approvalcrest.matcher.types.ordering;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static org.hamcrest.Matchers.startsWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import com.github.karsaig.approvalcrest.testdata.ChildBean;

/**
 * A complex-key map entry is written as {@code [key, value]}. The sorter used to recurse into that array
 * like any other and reorder it by JSON, which lost the key/value distinction, so a map and its transpose
 * produced identical bytes. These cover every flow the fix touches, at every level a field's declared type
 * can describe -- a map held by another map, by a set, by a list of lists, and so on down.
 *
 * <p>Tests named {@code ...IsUnaffected} or {@code ...KeepsTheOldOrder} are regression guards and pass
 * before and after the fix; the rest fail before it.
 */
public class JsonMatcherComplexKeyMapOrderTest extends AbstractFileMatcherTest {

    /** Key JSON sorts after value JSON, so the defect swapped them. */
    static class MapHolder {
        Map<ChildBean, ChildBean> m = new LinkedHashMap<>();

        MapHolder(String key, String value) {
            m.put(child().childString(key).build(), child().childString(value).build());
        }

        MapHolder() {
        }
    }

    static class MapToList {
        Map<ChildBean, List<String>> m = new LinkedHashMap<>();
    }

    static class MapOfMaps {
        Map<ChildBean, Map<ChildBean, ChildBean>> outer = new LinkedHashMap<>();
    }

    static class MapOfMapOfMaps {
        Map<ChildBean, Map<ChildBean, Map<ChildBean, ChildBean>>> outer = new LinkedHashMap<>();
    }

    static class SetOfMaps {
        Set<Map<ChildBean, ChildBean>> s = new LinkedHashSet<>();
    }

    static class MapOfSetOfMaps {
        Map<ChildBean, Set<Map<ChildBean, ChildBean>>> outer = new LinkedHashMap<>();
    }

    static class StringKeyedOuterMap {
        Map<String, Map<ChildBean, ChildBean>> outer = new LinkedHashMap<>();
    }

    static class MapOfLooselyTypedMaps {
        Map<ChildBean, Map<Object, Object>> outer = new LinkedHashMap<>();
    }

    static class MapToListOfLists {
        Map<ChildBean, List<List<String>>> m = new LinkedHashMap<>();
    }

    static class MapToArrayOfArrays {
        Map<ChildBean, String[][]> m = new LinkedHashMap<>();
    }

    static class MapToSetOfLists {
        Map<ChildBean, Set<List<String>>> m = new LinkedHashMap<>();
    }

    static class MapToSetOfSets {
        Map<ChildBean, Set<Set<String>>> m = new LinkedHashMap<>();
    }

    static class MapOfMapsToDeepCollections {
        Map<ChildBean, Map<ChildBean, List<List<Set<String>>>>> outer = new LinkedHashMap<>();
    }

    static class MapOfMapsToSetOfBeans {
        Map<ChildBean, Map<ChildBean, Set<ChildBean>>> outer = new LinkedHashMap<>();
    }

    static class MapOfStringKeyedMaps {
        Map<ChildBean, Map<String, ChildBean>> outer = new LinkedHashMap<>();
    }

    static class HashMapOfHashMaps {
        Map<ChildBean, Map<ChildBean, ChildBean>> outer = new HashMap<>();
    }

    static class MyMap<V> extends HashMap<String, V> {
        private static final long serialVersionUID = 1L;
    }

    static class MyMapHolder {
        MyMap<ChildBean> m = new MyMap<>();
    }

    static class TypeVariableHolder<T extends Map<ChildBean, ChildBean>> {
        T data;
    }

    static class MapKeyedByMaps {
        Map<Map<ChildBean, ChildBean>, ChildBean> m = new LinkedHashMap<>();
    }

    static class MapToObject {
        Map<ChildBean, Object> m = new LinkedHashMap<>();
    }

    @SuppressWarnings("rawtypes")
    static class MapToRawMap {
        Map<ChildBean, Map> m = new LinkedHashMap<>();
    }

    static class ListOfMaps {
        List<Map<ChildBean, ChildBean>> l;
    }

    static class ObjectFieldHolder {
        Object o;
    }

    /** Registered with an adapter of its own, which wins over the one every Map gets. */
    static class AdaptedMap extends LinkedHashMap<ChildBean, ChildBean> {
        private static final long serialVersionUID = 1L;
    }

    static class AdaptedMapHolder {
        AdaptedMap m = new AdaptedMap();
    }

    static class SetOfLists {
        Set<List<String>> s = new LinkedHashSet<>();
    }

    static class StringKeyed {
        Map<String, ChildBean> m = new LinkedHashMap<>();
    }

    /** The single-entry shapes the first tests here were written against, in terms of the renderer below. */
    private static String pair(String first, String second) {
        return entry(bean(first), bean(second));
    }

    private static String mapOf(String... pairs) {
        return obj(member("m", arr(pairs)));
    }

    // --- an expectation renderer for the nested shapes ---------------------------------------------
    // The argument order IS the expected order: nothing here sorts, so an expectation still states the
    // answer instead of recomputing it the way the code under test does. Hand-written literals stay
    // where they are readable; below two levels they are not.

    private static String indented(String block) {
        return "  " + block.replace("\n", "\n  ");
    }

    private static String arr(String... elements) {
        return elements.length == 0 ? "[]" : "[\n" + indented(String.join(",\n", elements)) + "\n]";
    }

    private static String obj(String... members) {
        return members.length == 0 ? "{}" : "{\n" + indented(String.join(",\n", members)) + "\n}";
    }

    private static String member(String name, String value) {
        return "\"" + name + "\": " + value;
    }

    private static String str(String value) {
        return "\"" + value + "\"";
    }

    /** A ChildBean as the fixtures build it. Members come out alphabetically, so childInteger first. */
    private static String bean(String childString) {
        return bean(childString, 0);
    }

    private static String bean(String childString, int childInteger) {
        return obj(member("childInteger", String.valueOf(childInteger)), member("childString", str(childString)));
    }

    /** A complex-key map entry: key first, value second. */
    private static String entry(String key, String value) {
        return arr(key, value);
    }

    private static ChildBean key(String childString) {
        return child().childString(childString).build();
    }

    private static ChildBean key(String childString, int childInteger) {
        return child().childString(childString).childInteger(childInteger).build();
    }

    /**
     * Three entries, inserted in an order that is neither the sorted one nor its reverse, and mixing both
     * key/value orderings: one entry whose key sorts after its value, so a swap shows; one whose key sorts
     * before it, where a swap would be invisible -- which is what proves the rule is applied per pair
     * rather than to the map as a whole; and one whose halves differ only in a nested field.
     */
    private static Map<ChildBean, ChildBean> threeEntries() {
        return threeEntries("");
    }

    private static Map<ChildBean, ChildBean> threeEntries(String tag) {
        Map<ChildBean, ChildBean> entries = new LinkedHashMap<>();
        entries.put(key(tag + "zk"), key(tag + "av"));
        entries.put(key(tag + "eq", 1), key(tag + "eq", 2));
        entries.put(key(tag + "ak"), key(tag + "zv"));
        return entries;
    }

    /** What {@link #threeEntries()} must render as: ordered by key, and every pair key first. */
    private static String threeEntriesRendered() {
        return threeEntriesRendered("");
    }

    private static String threeEntriesRendered(String tag) {
        return arr(entry(bean(tag + "ak"), bean(tag + "zv")),
                entry(bean(tag + "zk"), bean(tag + "av")),
                entry(bean(tag + "eq", 1), bean(tag + "eq", 2)));
    }

    /**
     * The same three entries in the order serialisation gives them, which a level nothing re-sorts keeps.
     * The two orders differ, and deliberately: entries are ordered by their serialised text, and at
     * serialisation a bean's members are still in declaration order, so childString decides -- while the
     * JSON-level re-sort compares the member-sorted form, where childInteger decides. Only the eq entry,
     * whose halves differ in childInteger alone, can tell them apart.
     */
    private static String threeEntriesAsSerialised(String tag) {
        return arr(entry(bean(tag + "ak"), bean(tag + "zv")),
                entry(bean(tag + "eq", 1), bean(tag + "eq", 2)),
                entry(bean(tag + "zk"), bean(tag + "av")));
    }

    // --- the key is written before the value -------------------------------------------------------

    @Test
    public void keyComesBeforeTheValueEvenWhenTheValueSortsFirst() {
        assertJsonMatcherWithDummyTestInfo(new MapHolder("zk", "av"), mapOf(pair("zk", "av")),
                Function.identity(), null);
    }

    @Test
    public void anApprovedFileForOneMapDoesNotMatchItsTranspose() {
        // The reported symptom, and it needs both legs in one test. Before the fix a map and its transpose
        // produced identical bytes, so ONE file matched both. Asserting only "the transpose fails" passes
        // either way, because the file literal itself differs between the two behaviours -- the positive
        // leg is what pins it.
        String fileForKeyZk = mapOf(pair("zk", "av"));

        assertJsonMatcherWithDummyTestInfo(new MapHolder("zk", "av"), fileForKeyZk, Function.identity(), null);

        assertJsonMatcherWithDummyTestInfo(new MapHolder("av", "zk"), fileForKeyZk, Function.identity(),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("m[0][0].childString")
                                && error.getMessage().contains("m[0][1].childString"),
                        "Expected mismatches at both pair positions, was: " + error.getMessage()),
                AssertionError.class);
    }

    @Test
    public void explicitSortFieldOnAMapKeepsTheKeyFirst() {
        assertJsonMatcherWithDummyTestInfo(new MapHolder("zk", "av"), mapOf(pair("zk", "av")),
                jsonMatcher -> jsonMatcher.sortField("m"), null);
    }

    @Test
    public void fieldMatcherSortOnAMapKeepsTheKeyFirst() {
        assertJsonMatcherWithDummyTestInfo(new MapHolder("zk", "av"), mapOf(pair("zk", "av")),
                jsonMatcher -> jsonMatcher.sortField(startsWith("m")), null);
    }

    @Test
    public void aPairReachedAsTheSortInputItselfKeepsItsOrder() {
        // A pair arrives at the sorter as its own input, not just as an element, when a field matcher
        // matches at pair level. That is a separate branch from the element case and the only reader of
        // the "I am a pair" flag; without it the key and value swap here.
        assertJsonMatcherWithDummyTestInfo(new MapHolder("zk", "av"), mapOf(pair("zk", "av")),
                jsonMatcher -> jsonMatcher.sortField(org.hamcrest.Matchers.any(String.class)), null);
    }

    @Test
    public void entryOrderMovesWhenSomeEntriesSwappedAndOthersDidNot() {
        // Consequence of suppressing the pair swap: the pair's sort key is computed after the descent, so
        // the entries themselves reorder. Recorded deliberately rather than left for a user to discover.
        MapHolder h = new MapHolder();
        h.m.put(child().childString("z").build(), child().childString("a").build());
        h.m.put(child().childString("b").build(), child().childString("c").build());

        assertJsonMatcherWithDummyTestInfo(h, mapOf(pair("b", "c"), pair("z", "a")),
                Function.identity(), null);
    }

    @Test
    public void aMapInsideASetInsideAMapKeepsItsKeyFirst() {
        MapOfSetOfMaps h = new MapOfSetOfMaps();
        h.outer.put(key("L1-c"), setOfThreeMaps());
        h.outer.put(key("L1-a"), setOfThreeMaps());
        h.outer.put(key("L1-b"), setOfThreeMaps());

        String setRendered = arr(threeEntriesRendered("a-"), threeEntriesRendered("b-"), threeEntriesRendered("c-"));
        String expected = obj(member("outer", arr(entry(bean("L1-a"), setRendered),
                entry(bean("L1-b"), setRendered),
                entry(bean("L1-c"), setRendered))));
        assertJsonMatcherWithDummyTestInfo(h, expected, Function.identity(), null);
    }

    @Test
    public void aNestedMapReachedByAnExplicitSortFieldKeepsItsKeyFirst() {
        // The other route into the sorter: a configured path, and a field-name matcher, rather than the
        // unconditional sort a Map-typed field gets.
        String expected = obj(member("outer", nestedMapsRendered()));

        assertJsonMatcherWithDummyTestInfo(nestedMaps(), expected,
                jsonMatcher -> jsonMatcher.sortField("outer"), null);
        assertJsonMatcherWithDummyTestInfo(nestedMaps(), expected,
                jsonMatcher -> jsonMatcher.sortField(startsWith("out")), null);
    }

    @Test
    public void aStringKeyedMapHoldingComplexKeyMapsKeepsTheInnerKeyFirst() {
        // A simple-keyed map renders one object per entry, so the inner map is reached through a member
        // named by a map key, which carries no marker of its own. Nothing sorts it by default, so naming it
        // in a sortField is what makes the difference observable.
        StringKeyedOuterMap h = new StringKeyedOuterMap();
        h.outer.put("k-c", threeEntries("c-"));
        h.outer.put("k-a", threeEntries("a-"));
        h.outer.put("k-b", threeEntries("b-"));

        // Only k-a is named, so only its entries are re-sorted; k-b and k-c keep the order serialisation
        // gave them. Both orders are shown rather than smoothed over -- the difference is what says the
        // sort reached one level and not the others, while every pair stays key first either way.
        String expected = obj(member("outer", arr(obj(member("k-a", threeEntriesRendered("a-"))),
                obj(member("k-b", threeEntriesAsSerialised("b-"))),
                obj(member("k-c", threeEntriesAsSerialised("c-"))))));
        assertJsonMatcherWithDummyTestInfo(h, expected,
                jsonMatcher -> jsonMatcher.sortField("outer.k-a"), null);
    }

    @Test
    public void aNestedMapWithAMixedKeyMapKeepsItsKeyFirst() {
        // One complex key sends the whole inner map into the pair form, so a String key sits at a pair
        // position too -- and the value half is still the one the chain continues into.
        MapOfLooselyTypedMaps h = new MapOfLooselyTypedMaps();
        Map<Object, Object> inner = new LinkedHashMap<>();
        inner.put("zs", "av");
        inner.put(key("zk"), key("av"));
        inner.put("as", "zv");
        h.outer.put(key("L1-a"), inner);

        String innerRendered = arr(entry(str("as"), str("zv")),
                entry(str("zs"), str("av")),
                entry(bean("zk"), bean("av")));
        assertJsonMatcherWithDummyTestInfo(h, obj(member("outer", arr(entry(bean("L1-a"), innerRendered)))),
                Function.identity(), null);
    }

    @Test
    public void aNestedMapWithANullKeyKeepsItsKeyFirst() {
        // A null key is a bare JSON null at a pair position -- the half the chain deliberately says nothing
        // about. The entry beside it still keeps its key first.
        MapOfLooselyTypedMaps h = new MapOfLooselyTypedMaps();
        Map<Object, Object> inner = new LinkedHashMap<>();
        inner.put(key("zk"), key("av"));
        inner.put(null, "nullValue");
        h.outer.put(key("L1-a"), inner);

        String innerRendered = arr(entry("null", str("nullValue")), entry(bean("zk"), bean("av")));
        assertJsonMatcherWithDummyTestInfo(h, obj(member("outer", arr(entry(bean("L1-a"), innerRendered)))),
                Function.identity(), null);
    }

    @Test
    public void theGeneratedFileForANestedMapIsWrittenKeyFirst() {
        // The writer and the comparator have to agree, or a regenerated file fails on the next run.
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(nestedMaps(),
                obj(member("outer", nestedMapsRendered())), Function.identity());
    }

    private static Set<Map<ChildBean, ChildBean>> setOfThreeMaps() {
        Set<Map<ChildBean, ChildBean>> set = new LinkedHashSet<>();
        set.add(threeEntries("c-"));
        set.add(threeEntries("a-"));
        set.add(threeEntries("b-"));
        return set;
    }

    // --- the halves are still sorted --------------------------------------------------------------

    @Test
    public void theListValuedHalfOfAnEntryIsStillSorted() {
        MapToList h = new MapToList();
        h.m.put(child().childString("zk").build(), Arrays.asList("b", "a"));

        String expected = "{\n  \"m\": [\n    [\n"
                + "      {\n        \"childInteger\": 0,\n        \"childString\": \"zk\"\n      },\n"
                + "      [\n        \"a\",\n        \"b\"\n      ]\n"
                + "    ]\n  ]\n}";
        assertJsonMatcherWithDummyTestInfo(h, expected, Function.identity(), null);
    }

    // --- nothing stopped being sorted -------------------------------------------------------------

    @Test
    public void aListOfListsUnderAMapValueIsUnaffected() {
        MapToListOfLists h = new MapToListOfLists();
        h.m.put(key("L1-c"), listOfThreeLists());
        h.m.put(key("L1-a"), listOfThreeLists());
        h.m.put(key("L1-b"), listOfThreeLists());

        String lists = arr(threeSortedRendered("x-"), threeSortedRendered("y-"), threeSortedRendered("z-"));
        assertJsonMatcherWithDummyTestInfo(h, obj(member("m", arr(entry(bean("L1-a"), lists),
                entry(bean("L1-b"), lists),
                entry(bean("L1-c"), lists)))), Function.identity(), null);
    }

    @Test
    public void anArrayOfArraysUnderAMapValueIsUnaffected() {
        MapToArrayOfArrays h = new MapToArrayOfArrays();
        h.m.put(key("L1-a"), new String[][]{{"x-c", "x-a", "x-b"}, {"z-c", "z-a", "z-b"}, {"y-c", "y-a", "y-b"}});

        String arrays = arr(threeSortedRendered("x-"), threeSortedRendered("y-"), threeSortedRendered("z-"));
        assertJsonMatcherWithDummyTestInfo(h, obj(member("m", arr(entry(bean("L1-a"), arrays)))),
                Function.identity(), null);
    }

    @Test
    public void aSetOfListsUnderAMapValueIsUnaffected() {
        // Built both ways round against one expectation: a Set has no order of its own, and that it renders
        // the same either way is the whole flakiness guarantee.
        assertJsonMatcherWithDummyTestInfo(setOfLists(false), setOfListsRendered(), Function.identity(), null);
        assertJsonMatcherWithDummyTestInfo(setOfLists(true), setOfListsRendered(), Function.identity(), null);
    }

    @Test
    public void aSetOfSetsUnderAMapValueIsUnaffected() {
        MapToSetOfSets h = new MapToSetOfSets();
        Set<Set<String>> value = new LinkedHashSet<>();
        value.add(new LinkedHashSet<>(Arrays.asList("x-c", "x-a", "x-b")));
        value.add(new LinkedHashSet<>(Arrays.asList("z-c", "z-a", "z-b")));
        value.add(new LinkedHashSet<>(Arrays.asList("y-c", "y-a", "y-b")));
        h.m.put(key("L1-a"), value);

        String sets = arr(threeSortedRendered("x-"), threeSortedRendered("y-"), threeSortedRendered("z-"));
        assertJsonMatcherWithDummyTestInfo(h, obj(member("m", arr(entry(bean("L1-a"), sets)))),
                Function.identity(), null);
    }

    @Test
    public void deeplyNestedCollectionsUnderTwoMapLevelsAreStillOrderedAndKeepEveryKeyFirst() {
        // The combined case: pairs protected at both map levels, and every collection level below them still
        // ordered. One without the other would pass one of the two halves of this suite and fail the point.
        MapOfMapsToDeepCollections h = new MapOfMapsToDeepCollections();
        Map<ChildBean, List<List<Set<String>>>> inner = new LinkedHashMap<>();
        inner.put(key("L2-c"), deepCollections());
        inner.put(key("L2-a"), deepCollections());
        inner.put(key("L2-b"), deepCollections());
        h.outer.put(key("L1-a"), inner);

        String deep = arr(arr(threeSortedRendered("x-")), arr(threeSortedRendered("y-")), arr(threeSortedRendered("z-")));
        String innerRendered = arr(entry(bean("L2-a"), deep), entry(bean("L2-b"), deep), entry(bean("L2-c"), deep));
        assertJsonMatcherWithDummyTestInfo(h, obj(member("outer", arr(entry(bean("L1-a"), innerRendered)))),
                Function.identity(), null);
    }

    @Test
    public void aSetOfBeansUnderTwoMapLevelsIsStillOrderedAndKeepsEveryKeyFirst() {
        MapOfMapsToSetOfBeans h = new MapOfMapsToSetOfBeans();
        Map<ChildBean, Set<ChildBean>> inner = new LinkedHashMap<>();
        Set<ChildBean> beans = new LinkedHashSet<>(Arrays.asList(key("b-c"), key("b-a"), key("b-b")));
        inner.put(key("L2-c"), beans);
        inner.put(key("L2-a"), beans);
        inner.put(key("L2-b"), beans);
        h.outer.put(key("L1-a"), inner);

        String beansRendered = arr(bean("b-a"), bean("b-b"), bean("b-c"));
        String innerRendered = arr(entry(bean("L2-a"), beansRendered),
                entry(bean("L2-b"), beansRendered),
                entry(bean("L2-c"), beansRendered));
        assertJsonMatcherWithDummyTestInfo(h, obj(member("outer", arr(entry(bean("L1-a"), innerRendered)))),
                Function.identity(), null);
    }

    @Test
    public void aSetOfMapsIsStillOrderedAcrossItsEntriesAndKeepsEveryKeyFirst() {
        // The set field carries the collection marker and the level below it is described too, so the maps
        // it holds are recognised without being named by a field -- that is the `c m` case. Both halves are
        // asserted at once: this fails before the fix on the pairs, and would fail after it on the order.
        SetOfMaps forward = new SetOfMaps();
        forward.s.addAll(setOfThreeMaps());

        SetOfMaps backward = new SetOfMaps();
        backward.s.add(threeEntries("b-"));
        backward.s.add(threeEntries("c-"));
        backward.s.add(threeEntries("a-"));

        String expected = obj(member("s", arr(threeEntriesRendered("a-"),
                threeEntriesRendered("b-"),
                threeEntriesRendered("c-"))));
        assertJsonMatcherWithDummyTestInfo(forward, expected, Function.identity(), null);
        assertJsonMatcherWithDummyTestInfo(backward, expected, Function.identity(), null);
    }

    @Test
    public void aStringKeyedMapUnderAComplexKeyMapIsUnaffected() {
        // The inner map renders one object per entry rather than pairs, so a map level is claimed for it and
        // nothing acts on the claim. Its entries are still ordered, which is what matters.
        MapOfStringKeyedMaps h = new MapOfStringKeyedMaps();
        Map<String, ChildBean> inner = new LinkedHashMap<>();
        inner.put("k-c", key("v-c"));
        inner.put("k-a", key("v-a"));
        inner.put("k-b", key("v-b"));
        h.outer.put(key("L1-a"), inner);

        String innerRendered = arr(obj(member("k-a", bean("v-a"))),
                obj(member("k-b", bean("v-b"))),
                obj(member("k-c", bean("v-c"))));
        assertJsonMatcherWithDummyTestInfo(h, obj(member("outer", arr(entry(bean("L1-a"), innerRendered)))),
                Function.identity(), null);
    }

    @Test
    public void aHashMapOfHashMapsRendersTheSameOnEveryRunAndKeepsEveryKeyFirst() {
        // Real HashMaps, whose iteration order is unspecified, built from differently ordered sources. Keys
        // still come out in order at every level -- key-before-value inside an entry costs nothing here.
        assertJsonMatcherWithDummyTestInfo(hashMaps(false), hashMapsRendered(), Function.identity(), null);
        assertJsonMatcherWithDummyTestInfo(hashMaps(true), hashMapsRendered(), Function.identity(), null);
    }

    private static List<List<String>> listOfThreeLists() {
        return Arrays.asList(Arrays.asList("x-c", "x-a", "x-b"),
                Arrays.asList("z-c", "z-a", "z-b"),
                Arrays.asList("y-c", "y-a", "y-b"));
    }

    private static List<List<Set<String>>> deepCollections() {
        return Arrays.asList(Arrays.asList(new LinkedHashSet<>(Arrays.asList("x-c", "x-a", "x-b"))),
                Arrays.asList(new LinkedHashSet<>(Arrays.asList("z-c", "z-a", "z-b"))),
                Arrays.asList(new LinkedHashSet<>(Arrays.asList("y-c", "y-a", "y-b"))));
    }

    private static String threeSortedRendered(String tag) {
        return arr(str(tag + "a"), str(tag + "b"), str(tag + "c"));
    }

    private static MapToSetOfLists setOfLists(boolean reversed) {
        MapToSetOfLists h = new MapToSetOfLists();
        Set<List<String>> value = new LinkedHashSet<>();
        List<List<String>> elements = listOfThreeLists();
        if (reversed) {
            elements = new ArrayList<>(elements);
            Collections.reverse(elements);
        }
        value.addAll(elements);
        h.m.put(key("L1-a"), value);
        return h;
    }

    private static String setOfListsRendered() {
        return obj(member("m", arr(entry(bean("L1-a"), arr(threeSortedRendered("x-"),
                threeSortedRendered("y-"),
                threeSortedRendered("z-"))))));
    }

    private static HashMapOfHashMaps hashMaps(boolean reversed) {
        HashMapOfHashMaps h = new HashMapOfHashMaps();
        List<String> outerKeys = new ArrayList<>(Arrays.asList("L1-c", "L1-a", "L1-b"));
        if (reversed) {
            Collections.reverse(outerKeys);
        }
        for (String outerKey : outerKeys) {
            h.outer.put(key(outerKey), new HashMap<>(threeEntries()));
        }
        return h;
    }

    private static String hashMapsRendered() {
        return obj(member("outer", arr(entry(bean("L1-a"), threeEntriesRendered()),
                entry(bean("L1-b"), threeEntriesRendered()),
                entry(bean("L1-c"), threeEntriesRendered()))));
    }

    // --- regression guards ------------------------------------------------------------------------

    @Test
    public void aSetOfListsIsUnaffected() {
        SetOfLists h = new SetOfLists();
        h.s.add(Arrays.asList("z", "a"));

        assertJsonMatcherWithDummyTestInfo(h, "{\n  \"s\": [\n    [\n      \"a\",\n      \"z\"\n    ]\n  ]\n}",
                Function.identity(), null);
    }

    @Test
    public void aStringKeyedMapIsUnaffected() {
        StringKeyed h = new StringKeyed();
        h.m.put("zk", child().childString("av").build());

        assertJsonMatcherWithDummyTestInfo(h,
                "{\n  \"m\": [\n    {\n      \"zk\": {\n        \"childInteger\": 0,\n        \"childString\": \"av\"\n      }\n    }\n  ]\n}",
                Function.identity(), null);
    }

    @Test
    public void aMapAsAnotherMapsValueKeepsItsKeyFirst() {
        // The flagged gap. The marker is on a FIELD name and the inner map is a value, so nothing named it;
        // the field naming strategy now describes that level too, and the sorter carries the description
        // down. Three entries at each level, so a suppressed pair cannot be confused with luck.
        assertJsonMatcherWithDummyTestInfo(nestedMaps(), obj(member("outer", nestedMapsRendered())),
                Function.identity(), null);
    }

    @Test
    public void anApprovedFileForANestedMapDoesNotMatchItsTranspose() {
        // Both legs, as the one-level twin above needs them: the file for one map must match, and the file
        // for its transpose must not. Asserting only the failure passes either way, because the literal
        // itself differs between the two behaviours.
        String fileForNestedMaps = obj(member("outer", nestedMapsRendered()));

        assertJsonMatcherWithDummyTestInfo(nestedMaps(), fileForNestedMaps, Function.identity(), null);

        MapOfMaps transposed = new MapOfMaps();
        Map<ChildBean, ChildBean> inner = new LinkedHashMap<>();
        inner.put(key("av"), key("zk"));
        inner.put(key("eq", 2), key("eq", 1));
        inner.put(key("zv"), key("ak"));
        transposed.outer.put(key("L1-a"), inner);
        transposed.outer.put(key("L1-b"), inner);
        transposed.outer.put(key("L1-c"), inner);

        assertJsonMatcherWithDummyTestInfo(transposed, fileForNestedMaps, Function.identity(),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("outer[0][1][0][0].childString")
                                && error.getMessage().contains("outer[0][1][0][1].childString"),
                        "Expected mismatches at both halves of an inner pair, was: " + error.getMessage()),
                AssertionError.class);
    }

    @Test
    public void aMapNestedThreeDeepKeepsEveryKeyFirst() {
        MapOfMapOfMaps h = new MapOfMapOfMaps();
        Map<ChildBean, Map<ChildBean, ChildBean>> middle = new LinkedHashMap<>();
        middle.put(key("L2-c"), threeEntries());
        middle.put(key("L2-a"), threeEntries());
        middle.put(key("L2-b"), threeEntries());
        h.outer.put(key("L1-c"), middle);
        h.outer.put(key("L1-a"), middle);
        h.outer.put(key("L1-b"), middle);

        String middleRendered = arr(entry(bean("L2-a"), threeEntriesRendered()),
                entry(bean("L2-b"), threeEntriesRendered()),
                entry(bean("L2-c"), threeEntriesRendered()));
        String expected = obj(member("outer", arr(entry(bean("L1-a"), middleRendered),
                entry(bean("L1-b"), middleRendered),
                entry(bean("L1-c"), middleRendered))));

        assertJsonMatcherWithDummyTestInfo(h, expected, Function.identity(), null);
    }

    private static MapOfMaps nestedMaps() {
        MapOfMaps h = new MapOfMaps();
        h.outer.put(key("L1-c"), threeEntries());
        h.outer.put(key("L1-a"), threeEntries());
        h.outer.put(key("L1-b"), threeEntries());
        return h;
    }

    private static String nestedMapsRendered() {
        return arr(entry(bean("L1-a"), threeEntriesRendered()),
                entry(bean("L1-b"), threeEntriesRendered()),
                entry(bean("L1-c"), threeEntriesRendered()));
    }

    @Test
    public void strictMatchingOffKeepsTheOldOrder() {
        // Deliberate: with strict off the expected side is sorted too, and it cannot recognise a pair, so
        // suppressing on this side alone would fail permanently. Both sides keep the old behaviour.
        assertJsonMatcherWithDummyTestInfo(new MapHolder("zk", "av"), mapOf(pair("av", "zk")),
                getDefaultFileMatcherConfigWithLenientMatching(), Function.identity(), null);
    }

    @Test
    public void jsonStringInputIsUnaffected() {
        // A parsed tree carries no marker, so the type-driven sort never fires on it at all: the pairs
        // are neither reordered nor suppressed, they pass through as written. Object input was the broken
        // form, so the fix narrows the gap between the two rather than widening it.
        String input = mapOf(pair("zk", "av"));
        assertJsonMatcherWithDummyTestInfo(input, input, Function.identity(), null);
    }


    @Test
    public void jsonStringInputStillReordersUnderAnExplicitSortField() {
        // The one configuration where the input-form limitation is observable, and it was unpinned: the test
        // above uses no sort, so nothing sorted the pairs either way. Naming the field does reach them, and a
        // parsed tree carries no marker to say they are entries, so here they are reordered -- which is the
        // limitation docs/sorting.md records. Compare with explicitSortFieldOnAMapKeepsTheKeyFirst, the same
        // configuration over object input, where the key stays first.
        String input = mapOf(pair("zk", "av"));

        assertJsonMatcherWithDummyTestInfo(input, mapOf(pair("av", "zk")),
                jsonMatcher -> jsonMatcher.sortField("m"), null);
    }

    @Test
    public void aMapSubtypeWithItsOwnTypeParametersIsUnaffected() {
        // MyMap<V> extends HashMap<String,V>: one type argument where a Map has two, so reading the second
        // would throw inside the naming strategy and fail every serialisation of a bean holding one. The
        // walk stops instead, which costs a fix it could not have made anyway.
        MyMapHolder h = new MyMapHolder();
        h.m.put("k-c", key("v-c"));
        h.m.put("k-a", key("v-a"));
        h.m.put("k-b", key("v-b"));

        assertJsonMatcherWithDummyTestInfo(h, obj(member("m", arr(obj(member("k-a", bean("v-a"))),
                obj(member("k-b", bean("v-b"))),
                obj(member("k-c", bean("v-c")))))), Function.identity(), null);
    }

    @Test
    public void aTypeVariableDeclaredMapFieldIsUnaffected() {
        // The generic type says nothing, but the erasure is a Map, so the field keeps the marker it has
        // today and its entries are still re-sorted -- which the eq entry is what proves, since it lands in
        // a different place under the serialisation order.
        TypeVariableHolder<Map<ChildBean, ChildBean>> h = new TypeVariableHolder<>();
        h.data = threeEntries();

        assertJsonMatcherWithDummyTestInfo(h, obj(member("data", threeEntriesRendered())),
                Function.identity(), null);
    }

    @Test
    public void aNestedMapUnderStrictMatchingOffKeepsTheOldOrder() {
        // With strict matching off the expected side is sorted too and cannot recognise a pair, so
        // suppressing on this side alone would fail permanently. Both sides keep the old behaviour, at
        // every level rather than only the outermost.
        MapOfMaps h = new MapOfMaps();
        Map<ChildBean, ChildBean> inner = new LinkedHashMap<>();
        inner.put(key("zk"), key("av"));
        h.outer.put(key("L1-a"), inner);

        String innerRendered = arr(arr(bean("av"), bean("zk")));
        assertJsonMatcherWithDummyTestInfo(h,
                obj(member("outer", arr(arr(innerRendered, bean("L1-a"))))),
                getDefaultFileMatcherConfigWithLenientMatching(), Function.identity(), null);
    }

    // --- where the chain runs out -----------------------------------------------------------------
    // Pinned as they behave, not as an endorsement: each is a shape the declared type cannot describe, so
    // it keeps exactly the behaviour it has today.

    @Test
    public void aMapUsedAsAKeyKeepsTheOldOrder() {
        // The chain describes the value side of an entry; a map at the key position is not reached.
        MapKeyedByMaps h = new MapKeyedByMaps();
        Map<ChildBean, ChildBean> mapKey = new LinkedHashMap<>();
        mapKey.put(key("zk"), key("av"));
        h.m.put(mapKey, key("v"));

        assertJsonMatcherWithDummyTestInfo(h,
                obj(member("m", arr(entry(arr(arr(bean("av"), bean("zk"))), bean("v"))))),
                Function.identity(), null);
    }

    @Test
    public void anObjectDeclaredMapValueKeepsTheOldOrder() {
        MapToObject h = new MapToObject();
        Map<ChildBean, ChildBean> inner = new LinkedHashMap<>();
        inner.put(key("zk"), key("av"));
        h.m.put(key("L1-a"), inner);

        assertJsonMatcherWithDummyTestInfo(h,
                obj(member("m", arr(entry(bean("L1-a"), arr(arr(bean("av"), bean("zk"))))))),
                Function.identity(), null);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void aRawMapValueKeepsTheOldOrder() {
        MapToRawMap h = new MapToRawMap();
        Map<ChildBean, ChildBean> inner = new LinkedHashMap<>();
        inner.put(key("zk"), key("av"));
        h.m.put(key("L1-a"), inner);

        assertJsonMatcherWithDummyTestInfo(h,
                obj(member("m", arr(entry(bean("L1-a"), arr(arr(bean("av"), bean("zk"))))))),
                Function.identity(), null);
    }

    @Test
    public void aMapAsAPlainCollectionElementKeepsTheOldOrder() {
        // A List field carries no marker at all, so nothing describes what it holds. Only a sortField
        // naming it reaches the maps inside, and there they are still reordered.
        ListOfMaps h = new ListOfMaps();
        Map<ChildBean, ChildBean> inner = new LinkedHashMap<>();
        inner.put(key("zk"), key("av"));
        h.l = Arrays.asList(inner);

        assertJsonMatcherWithDummyTestInfo(h, obj(member("l", arr(arr(arr(bean("av"), bean("zk")))))),
                jsonMatcher -> jsonMatcher.sortField("l"), null);
    }

    @Test
    public void aMapBehindAnObjectDeclaredFieldKeepsTheOldOrder() {
        ObjectFieldHolder h = new ObjectFieldHolder();
        Map<ChildBean, ChildBean> inner = new LinkedHashMap<>();
        inner.put(key("zk"), key("av"));
        h.o = inner;

        assertJsonMatcherWithDummyTestInfo(h, obj(member("o", arr(arr(bean("av"), bean("zk"))))),
                jsonMatcher -> jsonMatcher.sortField("o"), null);
    }

    @Test
    public void aWildcardPathDoesNotReachAPairFormEntry() {
        // A wildcard fans out over NAMED children, and a pair has none, so it never reaches a complex-key
        // map's entries. Pinned as it behaves: the entries below are untouched by the rule.
        assertJsonMatcherWithDummyTestInfo(nestedMaps(), obj(member("outer", nestedMapsRendered())),
                jsonMatcher -> jsonMatcher.ignoring("outer.*.childString"), null);
    }

    @Test
    public void ignoringAwayAKeyHalfIsUnaffected() {
        // Ignoring both of ChildBean's fields empties the KEY half only, so it is removed and the entry is
        // left one position long with its value still there. A list-valued map, because a pair whose only
        // survivor is a primitive is cleared by the orphan cleanup instead. That is the shape the sorter
        // must survive -- a chain says "the value is at index 1" and there is no index 1 -- and the lists
        // are still sorted afterwards.
        MapToList h = new MapToList();
        h.m.put(key("zk"), Arrays.asList("z-b", "z-a"));
        h.m.put(key("ak"), Arrays.asList("a-b", "a-a"));

        assertJsonMatcherWithDummyTestInfo(h,
                obj(member("m", arr(arr(arr(str("a-a"), str("a-b"))), arr(arr(str("z-a"), str("z-b")))))),
                jsonMatcher -> jsonMatcher.ignoring("m.childString").ignoring("m.childInteger"), null);
    }

    @Test
    public void aCustomAdapterRenderingAMapAsPairsIsTakenAtItsWord() {
        // An adapter registered for the value type wins over ours, so what the declared type called a map
        // can arrive as anything. Where what arrives is an array of two-position arrays, it is
        // indistinguishable from an entry array and is treated as one: the inner arrays keep their order.
        // That is the type being trusted where JSON cannot tell, which is what the field's OWN level has
        // always done -- see the leg below, which behaves the same before this change and after it.
        GsonConfiguration config = new GsonConfiguration();
        config.addTypeAdapter(AdaptedMap.class,
                (com.google.gson.JsonSerializer<AdaptedMap>) (src, type, context) -> {
                    com.google.gson.JsonArray inner = new com.google.gson.JsonArray();
                    inner.add("z-inner");
                    inner.add("a-inner");
                    com.google.gson.JsonArray rendered = new com.google.gson.JsonArray();
                    rendered.add(inner);
                    return rendered;
                });

        AdaptedMap adapted = new AdaptedMap();
        adapted.putAll(threeEntries());
        MapOfMaps h = new MapOfMaps();
        h.outer.put(key("L1-a"), adapted);

        assertJsonMatcherWithDummyTestInfo(h,
                obj(member("outer", arr(entry(bean("L1-a"), arr(arr(str("z-inner"), str("a-inner"))))))),
                jsonMatcher -> jsonMatcher.withGsonConfiguration(config), null);
    }

    @Test
    public void aCustomAdapterOnAMapFieldItselfIsUnaffected() {
        // The same shape one level up, where a Map-typed field's own marker has always said "entries" with
        // no way to check. This is the behaviour the case above extends to the levels below the field.
        GsonConfiguration config = new GsonConfiguration();
        config.addTypeAdapter(AdaptedMap.class,
                (com.google.gson.JsonSerializer<AdaptedMap>) (src, type, context) -> {
                    com.google.gson.JsonArray inner = new com.google.gson.JsonArray();
                    inner.add("z-inner");
                    inner.add("a-inner");
                    com.google.gson.JsonArray rendered = new com.google.gson.JsonArray();
                    rendered.add(inner);
                    return rendered;
                });

        AdaptedMapHolder h = new AdaptedMapHolder();
        h.m.putAll(threeEntries());

        assertJsonMatcherWithDummyTestInfo(h, obj(member("m", arr(arr(str("z-inner"), str("a-inner"))))),
                jsonMatcher -> jsonMatcher.withGsonConfiguration(config), null);
    }

    @Test
    public void aPatternMatchingOnlyTheInternalNameLeavesTheFieldAlone() {
        // An ignore pattern is written against the name the caller declared. A Set- or Map-typed field is
        // held under a prefixed one, and matching that meant a pattern naming nothing real could take such
        // a field away -- silently, since a removed field simply stops being compared.
        MapOfMaps h = new MapOfMaps();
        h.outer.put(key("L1-a"), threeEntries());

        assertJsonMatcherWithDummyTestInfo(h,
                obj(member("outer", arr(entry(bean("L1-a"), threeEntriesRendered())))),
                jsonMatcher -> jsonMatcher.ignoring(startsWith("!")), null);
    }

    // --- the writer and the comparator must agree --------------------------------------------------

    @Test
    public void theGeneratedFileIsWrittenKeyFirst() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(new MapHolder("zk", "av"),
                mapOf(pair("zk", "av")), Function.identity());
    }

    @Test
    public void theEntryArrayIsStillSortedByContent() {
        // Suppression is confined to within a pair: the entries themselves are still ordered, so a map
        // built in either insertion order renders the same way.
        MapHolder forward = new MapHolder();
        forward.m.put(child().childString("b").build(), child().childString("c").build());
        forward.m.put(child().childString("zk").build(), child().childString("av").build());

        assertJsonMatcherWithDummyTestInfo(forward, mapOf(pair("b", "c"), pair("zk", "av")),
                Function.identity(), null);
    }
}
