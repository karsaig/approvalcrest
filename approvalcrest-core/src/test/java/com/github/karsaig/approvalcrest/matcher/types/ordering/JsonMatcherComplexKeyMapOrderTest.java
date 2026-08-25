package com.github.karsaig.approvalcrest.matcher.types.ordering;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static org.hamcrest.Matchers.startsWith;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import com.github.karsaig.approvalcrest.testdata.ChildBean;

/**
 * A complex-key map entry is written as {@code [key, value]}. The sorter used to recurse into that array
 * like any other and reorder it by JSON, which lost the key/value distinction, so a map and its transpose
 * produced identical bytes. These cover every flow the fix touches.
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
        Map<ChildBean, Map<ChildBean, String>> outer = new LinkedHashMap<>();
    }

    static class SetOfLists {
        Set<List<String>> s = new LinkedHashSet<>();
    }

    static class StringKeyed {
        Map<String, ChildBean> m = new LinkedHashMap<>();
    }

    private static String pair(String first, String second) {
        return "    [\n"
                + "      {\n        \"childInteger\": 0,\n        \"childString\": \"" + first + "\"\n      },\n"
                + "      {\n        \"childInteger\": 0,\n        \"childString\": \"" + second + "\"\n      }\n"
                + "    ]";
    }

    private static String mapOf(String... pairs) {
        return "{\n  \"m\": [\n" + String.join(",\n", pairs) + "\n  ]\n}";
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
    public void aMapAsAnotherMapsValueIsOnlyFixedAtTheOuterLevel() {
        // Known limitation. The marker is on a FIELD name, and the inner map is a value, so nothing marks
        // it: the outer pair keeps its key first while the inner one is still reordered. Reaching it would
        // need a form both comparison sides can recognise in text, which is a wire-format change.
        MapOfMaps h = new MapOfMaps();
        Map<ChildBean, String> inner = new LinkedHashMap<>();
        inner.put(child().childString("zk").build(), "av");
        h.outer.put(child().childString("ok").build(), inner);

        String expected = "{\n  \"outer\": [\n    [\n"
                + "      {\n        \"childInteger\": 0,\n        \"childString\": \"ok\"\n      },\n"
                + "      [\n        [\n          \"av\",\n"
                + "          {\n            \"childInteger\": 0,\n            \"childString\": \"zk\"\n          }\n"
                + "        ]\n      ]\n"
                + "    ]\n  ]\n}";
        assertJsonMatcherWithDummyTestInfo(h, expected, Function.identity(), null);
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
        // limitation the CHANGELOG claims. Compare with explicitSortFieldOnAMapKeepsTheKeyFirst, the same
        // configuration over object input, where the key stays first.
        String input = mapOf(pair("zk", "av"));

        assertJsonMatcherWithDummyTestInfo(input, mapOf(pair("av", "zk")),
                jsonMatcher -> jsonMatcher.sortField("m"), null);
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
