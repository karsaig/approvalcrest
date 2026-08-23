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
    // findPaths — the * wildcard segment
    // -------------------------------------------------------------------------

    @Test
    void wildcardIgnoresPathUnderEveryMapValue() {
        // A Map serialises to an array of single-entry objects, so * lands on each entry's one
        // named child: the value.
        JsonObject json = parseObject(
                "{\"map\":[{\"k1\":{\"leaf\":1,\"keep\":9}},{\"k2\":{\"leaf\":2,\"keep\":9}}]}");

        FieldsIgnorer.findPaths(json, paths("map.*.leaf"));

        assertThat(json.toString(), is("{\"map\":[{\"k1\":{\"keep\":9}},{\"k2\":{\"keep\":9}}]}"));
    }

    @Test
    void wildcardOverASingleKeyMapMatchesNamingThatKey() {
        // Acceptance criterion: on a single-entry map the wildcard and the named key must produce
        // byte-identical output, so the wildcard cannot be doing anything else along the way.
        String input = "{\"map\":[{\"k1\":{\"leaf\":1,\"keep\":9}}]}";
        JsonObject viaWildcard = parseObject(input);
        JsonObject viaKey = parseObject(input);

        FieldsIgnorer.findPaths(viaWildcard, paths("map.*.leaf"));
        FieldsIgnorer.findPaths(viaKey, paths("map.k1.leaf"));

        assertThat(viaWildcard.toString(), is(viaKey.toString()));
    }

    @Test
    void wildcardOverATwoKeyMapDoesNotMatchNamingOneKey() {
        // The negative twin: naming one key must leave the other alone.
        String input = "{\"map\":[{\"k1\":{\"leaf\":1}},{\"k2\":{\"leaf\":2}}]}";
        JsonObject viaWildcard = parseObject(input);
        JsonObject viaKey = parseObject(input);

        FieldsIgnorer.findPaths(viaWildcard, paths("map.*.leaf"));
        FieldsIgnorer.findPaths(viaKey, paths("map.k1.leaf"));

        // Every value emptied, so the entries, the array and the field itself cascade away.
        assertThat(viaWildcard.toString(), is("{}"));
        assertThat(viaKey.toString(), is("{\"map\":[{\"k2\":{\"leaf\":2}}]}"));
    }

    @Test
    void wildcardResolvesTheValueEvenWhenAKeyCollidesWithAFieldNameOfTheValues() {
        // The case that rules out deciding by shape: the first entry's key is also a field name of
        // the values, so an implementation that treated the entry itself as the child would strip a
        // different field here than in the sibling entry -- and empty this entry away entirely.
        JsonObject json = parseObject(
                "{\"map\":[{\"firstName\":{\"firstName\":\"x\",\"o\":1}},"
                        + "{\"p2\":{\"firstName\":\"y\",\"o\":2}}]}");

        FieldsIgnorer.findPaths(json, paths("map.*.firstName"));

        assertThat(json.toString(), is("{\"map\":[{\"firstName\":{\"o\":1}},{\"p2\":{\"o\":2}}]}"));
    }

    @Test
    void wildcardIgnoresPathUnderEveryFieldOfAnObject() {
        JsonObject json = parseObject("{\"a\":{\"leaf\":1,\"keep\":9},\"b\":{\"leaf\":2,\"keep\":9}}");

        FieldsIgnorer.findPaths(json, paths("*.leaf"));

        assertThat(json.toString(), is("{\"a\":{\"keep\":9},\"b\":{\"keep\":9}}"));
    }

    @Test
    void wildcardFindsAMarkerPrefixedChild() {
        // Set- and Map-typed field names carry the MARKER prefix; the wildcard matches any named
        // child, so it needs no special handling -- this pins that.
        JsonObject json = parseObject(
                "{\"" + FieldsIgnorer.MARKER + "aSet\":{\"leaf\":1,\"keep\":9}}");

        FieldsIgnorer.findPaths(json, paths("*.leaf"));

        assertThat(json.getAsJsonObject(FieldsIgnorer.MARKER + "aSet").has("leaf"), is(false));
        assertThat(json.getAsJsonObject(FieldsIgnorer.MARKER + "aSet").has("keep"), is(true));
    }

    @Test
    void wildcardDescendsThroughAGraphAdapterEnvelopeRatherThanMatchingIt() {
        // An envelope key is a named child too, so the wildcard must not consume it -- otherwise
        // "*.leaf" would mean "the leaf of the envelope" and find nothing.
        JsonObject json = parseObject("{\"0x1\":{\"a\":{\"leaf\":7,\"keep\":9}}}");

        FieldsIgnorer.findPaths(json, paths("*.leaf"));

        JsonObject a = json.getAsJsonObject("0x1").getAsJsonObject("a");
        assertThat(a.has("leaf"), is(false));
        assertThat(a.has("keep"), is(true));
    }

    @Test
    void nestedWildcardsResolveThroughTwoLevels() {
        JsonObject json = parseObject("{\"a\":{\"k\":{\"b\":{\"j\":{\"c\":5,\"keep\":9}}}}}");

        FieldsIgnorer.findPaths(json, paths("a.*.b.*.c"));

        JsonObject j = json.getAsJsonObject("a").getAsJsonObject("k")
                .getAsJsonObject("b").getAsJsonObject("j");
        assertThat(j.has("c"), is(false));
        assertThat(j.has("keep"), is(true));
    }

    @Test
    void wildcardMatchingNothingIsANoOp() {
        JsonObject json = parseObject("{\"map\":[{\"k1\":{\"leaf\":1}}]}");

        FieldsIgnorer.findPaths(json, paths("map.*.absent"));

        assertThat(json.toString(), is("{\"map\":[{\"k1\":{\"leaf\":1}}]}"));
    }

    @Test
    void wildcardDoesNotTouchASiblingCollection() {
        // The negative case that matters most: a wildcard aimed at the map must leave the list alone,
        // even though both serialise to a JsonArray of objects carrying the same field name.
        JsonObject json = parseObject(
                "{\"map\":[{\"k1\":{\"leaf\":1,\"keep\":9}}],\"list\":[{\"leaf\":5}]}");

        FieldsIgnorer.findPaths(json, paths("map.*.leaf"));

        assertThat(json.toString(), is("{\"map\":[{\"k1\":{\"keep\":9}}],\"list\":[{\"leaf\":5}]}"));
    }

    @Test
    void pathThroughANonPrimitiveKeyMapReachesTheKeyObjectsToo() {
        // A map with non-primitive keys serialises to [[key, value], ...] rather than to single-entry
        // objects, so the transparent array traversal visits the key object as well as the value.
        // Pre-existing behaviour, pinned because a wildcard through such a map inherits it.
        JsonObject json = parseObject(
                "{\"map\":[[{\"leaf\":1,\"keep\":9},{\"leaf\":2,\"keep\":9}]]}");

        FieldsIgnorer.findPaths(json, paths("map.leaf"));

        assertThat(json.toString(), is("{\"map\":[[{\"keep\":9},{\"keep\":9}]]}"));
    }

    @Test
    void wildcardSkipsScalarChildren() {
        // A bean's ordinary shape is some object fields and some scalar ones. A scalar child has no
        // field for the rest of the path to name, so it is skipped -- exactly as the array fan-out
        // skips a scalar element -- rather than being descended into, which would reject a non-object
        // and take the whole assertion down with it.
        JsonObject json = parseObject("{\"a\":{\"name\":1,\"keep\":9},\"s\":\"str\"}");

        FieldsIgnorer.findPaths(json, paths("*.name"));

        assertThat(json.toString(), is("{\"a\":{\"keep\":9},\"s\":\"str\"}"));
    }

    @Test
    void wildcardSkipsScalarMapValues() {
        // The same shape through a Map<String,Object> holding a bean and a bare scalar.
        JsonObject json = parseObject(
                "{\"map\":[{\"k1\":{\"name\":1,\"keep\":9}},{\"k2\":\"scalar\"}]}");

        FieldsIgnorer.findPaths(json, paths("map.*.name"));

        assertThat(json.toString(), is("{\"map\":[{\"k1\":{\"keep\":9}},{\"k2\":\"scalar\"}]}"));
    }

    @Test
    void wildcardSkipsNullChildren() {
        JsonObject json = parseObject("{\"a\":{\"name\":1,\"keep\":9},\"n\":null}");

        FieldsIgnorer.findPaths(json, paths("*.name"));

        assertThat(json.toString(), is("{\"a\":{\"keep\":9},\"n\":null}"));
    }

    // -------------------------------------------------------------------------
    // A trailing * is a literal key, not a wildcard
    // -------------------------------------------------------------------------

    @Test
    void trailingWildcardRemovesTheKeyLiterallyNamedStar() {
        // "*" is a legal JSON key and a legal Map<String,?> key -- CORS configs and route tables use
        // one -- so as the final segment it keeps its ordinary meaning.
        JsonObject json = parseObject("{\"headers\":{\"*\":\"any\",\"accept\":\"json\"}}");

        FieldsIgnorer.findPaths(json, paths("headers.*"));

        assertThat(json.toString(), is("{\"headers\":{\"accept\":\"json\"}}"));
    }

    @Test
    void trailingWildcardWithoutALiteralStarKeyIsANoOp() {
        // The accepted sharp edge: someone writing map.* expecting every value gets a no-op, which is
        // what any non-matching ignore path against an object already does.
        JsonObject json = parseObject("{\"map\":{\"k1\":{\"leaf\":1}}}");

        FieldsIgnorer.findPaths(json, paths("map.*"));

        assertThat(json.toString(), is("{\"map\":{\"k1\":{\"leaf\":1}}}"));
    }

    @Test
    void loneWildcardPathRemovesARootLevelStarKey() {
        JsonObject json = parseObject("{\"*\":\"any\",\"keep\":9}");

        FieldsIgnorer.findPaths(json, paths("*"));

        assertThat(json.toString(), is("{\"keep\":9}"));
    }

    @Test
    void trailingWildcardIsNotAWildcard() {
        // The rule stated as an assertion: on one fixture, a non-final * fans out over the values
        // while a final * addresses only the entry literally keyed "*".
        String input = "{\"map\":[{\"k1\":{\"leaf\":1}},{\"*\":{\"leaf\":2}}]}";
        JsonObject nonFinal = parseObject(input);
        JsonObject asFinal = parseObject(input);

        FieldsIgnorer.findPaths(nonFinal, paths("map.*.leaf"));
        FieldsIgnorer.findPaths(asFinal, paths("map.*"));

        // Non-final: every value loses leaf, so everything cascades away.
        assertThat(nonFinal.toString(), is("{}"));
        // Final: only the entry keyed "*" goes; the k1 entry is untouched.
        assertThat(asFinal.toString(), is("{\"map\":[{\"k1\":{\"leaf\":1}}]}"));
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
    void ignoresPathInEveryEnvelopeWhenTheFirstOneEmpties() {
        // One graph, two objects, each carrying a.b. Removing b empties a, which empties the
        // envelope. The descent must still visit the second envelope: because findPaths runs
        // separately over the actual and the expected side, stopping early filters the two
        // differently and fails the comparison on data rather than on the ignore rule.
        JsonObject json = parseObject("{\"0x1\":{\"a\":{\"b\":1}},\"0x2\":{\"a\":{\"b\":2}}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        assertThat(json.keySet().isEmpty(), is(true));
    }

    @Test
    void ignoresPathInEveryEnvelopeWhenNoneEmpties() {
        JsonObject json = parseObject(
                "{\"0x1\":{\"a\":{\"b\":1,\"k\":9}},\"0x2\":{\"a\":{\"b\":2,\"k\":9}}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        assertThat(json.getAsJsonObject("0x1").getAsJsonObject("a").has("b"), is(false));
        assertThat(json.getAsJsonObject("0x2").getAsJsonObject("a").has("b"), is(false));
        assertThat(json.getAsJsonObject("0x1").getAsJsonObject("a").has("k"), is(true));
        assertThat(json.getAsJsonObject("0x2").getAsJsonObject("a").has("k"), is(true));
    }

    @Test
    void ignoresPathWhenOnlyOneEnvelopeEmpties() {
        // Mixed: 0x1 empties and is removed, 0x2 survives with its remaining field.
        JsonObject json = parseObject("{\"0x1\":{\"a\":{\"b\":1}},\"0x2\":{\"a\":{\"b\":2,\"k\":9}}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        assertThat(json.has("0x1"), is(false));
        assertThat(json.getAsJsonObject("0x2").getAsJsonObject("a").has("b"), is(false));
        assertThat(json.getAsJsonObject("0x2").getAsJsonObject("a").has("k"), is(true));
    }

    @Test
    void skipsAnEnvelopeKeyWhoseValueIsNotAnObject() {
        // Only an object under a graph-adapter key is an envelope. A graph-shaped key holding anything
        // else is passed over rather than descended into.
        JsonObject json = parseObject("{\"0x1\":\"scalar\",\"0x2\":{\"a\":{\"b\":1,\"k\":9}}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        assertThat(json.get("0x1").getAsString(), is("scalar"));
        assertThat(json.getAsJsonObject("0x2").getAsJsonObject("a").has("b"), is(false));
        assertThat(json.getAsJsonObject("0x2").getAsJsonObject("a").has("k"), is(true));
    }

    @Test
    void keepsAnEnvelopeThatChangedWithoutEmptying() {
        // The envelope loses the ignored field but keeps another, so the envelope itself stays.
        JsonObject json = parseObject("{\"0x1\":{\"a\":{\"b\":1}},\"0x2\":{\"a\":{\"b\":2}},\"keep\":9}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        // Both envelopes emptied out and went; the unrelated sibling is untouched.
        assertThat(json.toString(), is("{\"keep\":9}"));
    }

    @Test
    void wildcardDescendsThroughAnEnvelopeInAnElementRule() {
        // A graph-adapter envelope is transparent here too: the wildcard applies to the real fields
        // underneath it, so the array to filter is found inside the envelope rather than the envelope
        // being consumed as the wildcard's child.
        JsonObject json = parseObject(
                "{\"map\":{\"0x1\":{\"k1\":{\"tags\":[{\"n\":\"drop\"},{\"n\":\"keep\"}]}}}}");

        FieldsIgnorer.removeMatchingElements(json,
                Collections.singletonList(ElementIgnoreRule.ofValue("map.*.tags.n", "drop")), null);

        assertThat(json.toString(),
                is("{\"map\":{\"0x1\":{\"k1\":{\"tags\":[{\"n\":\"keep\"}]}}}}"));
    }

    @Test
    void wildcardSkipsANullChildInAnElementRule() {
        // A null child of the wildcard level has no array below it to filter.
        JsonObject json = parseObject(
                "{\"map\":{\"k1\":{\"tags\":[{\"n\":\"drop\"},{\"n\":\"keep\"}]},\"k2\":null}}");

        FieldsIgnorer.removeMatchingElements(json,
                Collections.singletonList(ElementIgnoreRule.ofValue("map.*.tags.n", "drop")), null);

        assertThat(json.toString(),
                is("{\"map\":{\"k1\":{\"tags\":[{\"n\":\"keep\"}]},\"k2\":null}}"));
    }

    @Test
    void ignoresLeafPathWhereFieldLivesUnderEnvelopeKey() {
        // Last-segment removal falls back to descending envelope keys.
        JsonObject json = parseObject("{\"a\":{\"0x1\":{\"b\":\"drop\"}}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        assertThat(json.getAsJsonObject("a").getAsJsonObject("0x1").has("b"), is(false));
    }

    @Test
    void cascadesRemovalWhenAMarkedParentBecomesEmpty() {
        // Same shape and same outcome as cascadesRemovalWhenParentBecomesEmpty, which is the point:
        // the MARKER prefix the naming strategy puts on Set- and Map-typed fields is stripped before
        // anyone reads the file, so it must not decide whether an emptied parent survives. It used
        // to: the child was found under the prefixed name and removed by the bare one, so the empty
        // husk stayed and the file showed an empty collection where the field should have gone.
        JsonObject json = parseObject("{\"" + FieldsIgnorer.MARKER + "a\":{\"b\":\"drop\"}}");

        FieldsIgnorer.findPaths(json, paths("a.b"));

        assertThat(json.has(FieldsIgnorer.MARKER + "a"), is(false));
        assertThat(json.size(), is(0));
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
