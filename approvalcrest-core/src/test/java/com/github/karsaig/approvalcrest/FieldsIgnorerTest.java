package com.github.karsaig.approvalcrest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link FieldsIgnorer}, the JSON tree walker behind {@code .ignoring(path)} and
 * {@code .ignoringElementsWhere(...)}. The scenarios target the harder, previously-uncovered
 * branches: cascading removal of emptied parents, path descent through graph-adapter envelope
 * keys, transparent fan-out through intermediate collections, and the guard rails on the
 * element-removal engine.
 */
public class FieldsIgnorerTest {

    private static JsonObject parseObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static Set<String> paths(String... p) {
        return new LinkedHashSet<>(Arrays.asList(p));
    }

    // -------------------------------------------------------------------------
    // findPaths — path ignoring
    // -------------------------------------------------------------------------

    @Test
    void ignoresTopLevelField() {
        JsonObject json = parseObject("{\"keep\":\"a\",\"drop\":\"b\"}");

        FieldsIgnorer.findPaths(json, paths("drop"));

        assertThat(json.has("drop"), is(false));
        assertThat(json.has("keep"), is(true));
    }

    @Test
    void ignoresNestedFieldKeepingNonEmptyParent() {
        JsonObject json = parseObject("{\"outer\":{\"drop\":\"b\",\"keep\":\"c\"}}");

        FieldsIgnorer.findPaths(json, paths("outer.drop"));

        JsonObject outer = json.getAsJsonObject("outer");
        assertThat(outer.has("drop"), is(false));
        assertThat(outer.has("keep"), is(true));
    }

    @Test
    void cascadesRemovalWhenParentBecomesEmpty() {
        JsonObject json = parseObject("{\"outer\":{\"drop\":\"b\"}}");

        FieldsIgnorer.findPaths(json, paths("outer.drop"));

        // outer had only the ignored field → the now-empty outer is removed too.
        assertThat(json.has("outer"), is(false));
    }

    @Test
    void ignoresFieldFanningOutThroughArray() {
        JsonObject json = parseObject("{\"list\":[{\"drop\":1,\"keep\":2},{\"drop\":3,\"keep\":4}]}");

        FieldsIgnorer.findPaths(json, paths("list.drop"));

        JsonArray list = json.getAsJsonArray("list");
        for (JsonElement element : list) {
            JsonObject obj = element.getAsJsonObject();
            assertThat(obj.has("drop"), is(false));
            assertThat(obj.has("keep"), is(true));
        }
    }

    @Test
    void ignoresPathUnderGraphAdapterEnvelopeKey() {
        // Circular-reference types are wrapped by GraphAdapterBuilder under 0xN envelope keys.
        JsonObject json = parseObject("{\"0x1\":{\"drop\":\"b\",\"keep\":\"c\"}}");

        FieldsIgnorer.findPaths(json, paths("drop"));

        JsonObject inner = json.getAsJsonObject("0x1");
        assertThat(inner.has("drop"), is(false));
        assertThat(inner.has("keep"), is(true));
    }

    @Test
    void ignoresMarkedField() {
        // Fields queued for sorting are prefixed with the MARKER; ignoring must still find them.
        JsonObject json = parseObject("{\"" + FieldsIgnorer.MARKER + "drop\":\"b\",\"keep\":\"c\"}");

        FieldsIgnorer.findPaths(json, paths("drop"));

        assertThat(json.has(FieldsIgnorer.MARKER + "drop"), is(false));
        assertThat(json.has("keep"), is(true));
    }

    @Test
    void ignoringNonExistentPathIsANoOp() {
        JsonObject json = parseObject("{\"a\":{\"b\":\"c\"}}");

        FieldsIgnorer.findPaths(json, paths("a.missing"));

        assertThat(json.getAsJsonObject("a").get("b").getAsString(), is("c"));
    }

    @Test
    void ignoringPathThatDescendsIntoPrimitiveThrows() {
        JsonObject json = parseObject("{\"a\":\"primitive\"}");

        // "a" is a primitive, so "a.b" cannot be resolved — this is reported as non-existent.
        assertThrows(IllegalArgumentException.class,
                () -> FieldsIgnorer.findPaths(json, paths("a.b")));
    }

    @Test
    void emptyPathSetLeavesJsonUnchanged() {
        JsonObject json = parseObject("{\"a\":\"b\"}");

        FieldsIgnorer.findPaths(json, Collections.<String>emptySet());

        assertThat(json.get("a").getAsString(), is("b"));
    }

    // -------------------------------------------------------------------------
    // removeMatchingElements — element ignoring
    // -------------------------------------------------------------------------

    @Test
    void removesArrayElementsByLeafValue() {
        JsonObject json = parseObject(
                "{\"tags\":[{\"system\":\"drop\"},{\"system\":\"keep\"}]}");
        List<ElementIgnoreRule> rules =
                Arrays.asList(ElementIgnoreRule.ofValue("tags.system", "drop"));

        FieldsIgnorer.removeMatchingElements(json, rules, null);

        JsonArray tags = json.getAsJsonArray("tags");
        assertThat(tags.size(), is(1));
        assertThat(tags.get(0).getAsJsonObject().get("system").getAsString(), is("keep"));
    }

    @Test
    void removesAllMatchingElementsLeavingEmptyArray() {
        JsonObject json = parseObject(
                "{\"tags\":[{\"system\":\"x\"},{\"system\":\"x\"}]}");
        List<ElementIgnoreRule> rules =
                Arrays.asList(ElementIgnoreRule.of("tags.system", startsWith("x")));

        FieldsIgnorer.removeMatchingElements(json, rules, null);

        assertThat(json.getAsJsonArray("tags").size(), is(0));
    }

    @Test
    void removesElementsFanningThroughIntermediateCollections() {
        // Path entry.tag.system: 'entry' is an array; each entry's 'tag' array is the one filtered.
        JsonObject json = parseObject(
                "{\"entry\":[{\"tag\":[{\"system\":\"drop\"},{\"system\":\"keep\"}]}," +
                            "{\"tag\":[{\"system\":\"drop\"}]}]}");
        List<ElementIgnoreRule> rules =
                Arrays.asList(ElementIgnoreRule.ofValue("entry.tag.system", "drop"));

        FieldsIgnorer.removeMatchingElements(json, rules, null);

        JsonArray entries = json.getAsJsonArray("entry");
        assertThat(entries.get(0).getAsJsonObject().getAsJsonArray("tag").size(), is(1));
        assertThat(entries.get(1).getAsJsonObject().getAsJsonArray("tag").size(), is(0));
    }

    @Test
    void elementWithoutLeafFieldIsKept() {
        JsonObject json = parseObject(
                "{\"tags\":[{\"system\":\"drop\"},{\"other\":\"y\"}]}");
        List<ElementIgnoreRule> rules =
                Arrays.asList(ElementIgnoreRule.ofValue("tags.system", "drop"));

        FieldsIgnorer.removeMatchingElements(json, rules, null);

        JsonArray tags = json.getAsJsonArray("tags");
        assertThat(tags.size(), is(1));
        assertThat(tags.get(0).getAsJsonObject().has("other"), is(true));
    }

    @Test
    void removesElementsUnderGraphAdapterEnvelopeKey() {
        JsonObject json = parseObject(
                "{\"0x1\":{\"tags\":[{\"system\":\"drop\"},{\"system\":\"keep\"}]}}");
        List<ElementIgnoreRule> rules =
                Arrays.asList(ElementIgnoreRule.ofValue("tags.system", "drop"));

        FieldsIgnorer.removeMatchingElements(json, rules, null);

        JsonArray tags = json.getAsJsonObject("0x1").getAsJsonArray("tags");
        assertThat(tags.size(), is(1));
    }

    @Test
    void removeMatchingElementsGuardRailsAreNoOps() {
        List<ElementIgnoreRule> rule =
                Arrays.asList(ElementIgnoreRule.ofValue("tags.system", "drop"));
        JsonObject json = parseObject("{\"tags\":[{\"system\":\"drop\"}]}");

        // null root, JSON-null root, null rules and empty rules must all be silent no-ops.
        FieldsIgnorer.removeMatchingElements(null, rule, null);
        FieldsIgnorer.removeMatchingElements(JsonNull.INSTANCE, rule, null);
        FieldsIgnorer.removeMatchingElements(json, null, null);
        FieldsIgnorer.removeMatchingElements(json, Collections.<ElementIgnoreRule>emptyList(), null);

        // The empty/null-rule calls did not touch the array.
        assertThat(json.getAsJsonArray("tags").size(), is(1));
    }

    // -------------------------------------------------------------------------
    // isGraphAdapterKey / removeSetMarker
    // -------------------------------------------------------------------------

    @Test
    void ignoresNestedPathDescendingThroughEnvelopeKey() {
        // "a" only exists under the 0x1 envelope; ignoring "a.b" must descend into it.
        JsonObject json = parseObject("{\"0x1\":{\"a\":{\"b\":\"drop\",\"keep\":\"c\"}}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        JsonObject a = json.getAsJsonObject("0x1").getAsJsonObject("a");
        assertThat(a.has("b"), is(false));
        assertThat(a.has("keep"), is(true));
    }

    @Test
    void ignoresLeafPathWhereFieldLivesUnderEnvelopeKey() {
        // Last-segment removal falls back to descending envelope keys.
        JsonObject json = parseObject("{\"a\":{\"0x1\":{\"b\":\"drop\"}}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        assertThat(json.getAsJsonObject("a").getAsJsonObject("0x1").has("b"), is(false));
    }

    @Test
    void ignoresPathUnderMarkedFieldEmptyingIt() {
        // The ignored path is resolved against a field queued for sorting (MARKER prefix);
        // its leaf is removed, leaving the marked object empty.
        JsonObject json = parseObject("{\"" + FieldsIgnorer.MARKER + "a\":{\"b\":\"drop\"}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        assertThat(json.getAsJsonObject(FieldsIgnorer.MARKER + "a").has("b"), is(false));
        assertThat(json.getAsJsonObject(FieldsIgnorer.MARKER + "a").size(), is(0));
    }

    @Test
    void removesElementsByMatcherRule() {
        JsonObject json = parseObject(
                "{\"tags\":[{\"n\":5},{\"n\":50},{\"n\":500}]}");
        List<ElementIgnoreRule> rules =
                Arrays.asList(ElementIgnoreRule.of("tags.n", org.hamcrest.Matchers.greaterThan(10L)));

        FieldsIgnorer.removeMatchingElements(json, rules, null);

        // only n=5 survives (50 and 500 are > 10)
        JsonArray tags = json.getAsJsonArray("tags");
        assertThat(tags.size(), is(1));
        assertThat(tags.get(0).getAsJsonObject().get("n").getAsInt(), is(5));
    }

    @Test
    void elementRuleKeepsNonObjectArrayElements() {
        // Primitive array elements are never matched/removed by an element rule.
        JsonObject json = parseObject("{\"tags\":[\"plain\",{\"system\":\"drop\"}]}");
        List<ElementIgnoreRule> rules =
                Arrays.asList(ElementIgnoreRule.ofValue("tags.system", "drop"));

        FieldsIgnorer.removeMatchingElements(json, rules, null);

        JsonArray tags = json.getAsJsonArray("tags");
        assertThat(tags.size(), is(1));
        assertThat(tags.get(0).getAsString(), is("plain"));
    }

    @Test
    void recognisesGraphAdapterKeys() {
        assertThat(FieldsIgnorer.isGraphAdapterKey("0x1"), is(true));
        assertThat(FieldsIgnorer.isGraphAdapterKey("0xabc123"), is(true));
        assertThat(FieldsIgnorer.isGraphAdapterKey("0x"), is(false));      // nothing after prefix
        assertThat(FieldsIgnorer.isGraphAdapterKey("0xZ"), is(false));     // non-hex digit
        assertThat(FieldsIgnorer.isGraphAdapterKey("field"), is(false));   // no prefix
        assertThat(FieldsIgnorer.isGraphAdapterKey(null), is(false));
    }

    @Test
    void removeSetMarkerStripsAllMarkers() {
        String withMarkers = FieldsIgnorer.MARKER + "a and " + FieldsIgnorer.MARKER + "b";

        assertThat(FieldsIgnorer.removeSetMarker(withMarkers), is("a and b"));
    }
}
