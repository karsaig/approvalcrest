package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.BeanFinder.FanoutResult;
import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
import com.github.karsaig.approvalcrest.matcher.machinereadable.AliasTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link JsonElementUtil}, the low-level JSON tree engine used by the matchers to
 * locate values by path, filter fields by pattern, and substitute aliases. The scenarios below
 * target the transparent-array fan-out, graph-adapter envelope descent, cascading empty-parent
 * removal, and array-primitive aliasing — all real behaviours a user triggers through
 * {@code .with(path, matcher)}, {@code .withMatcher(...)} and {@code .withAlias(...)}.
 */
public class JsonElementUtilTest {

    private static JsonElement parse(String json) {
        return JsonParser.parseString(json);
    }

    // -------------------------------------------------------------------------
    // asList
    // -------------------------------------------------------------------------

    @Test
    void asListCoercesScalarElementsSoValueMatchersCanMatch() {
        List<Object> view = JsonElementUtil.asList(parse("[1,2]").getAsJsonArray());

        assertThat(view, hasSize(2));
        assertThat(view, contains(1L, 2L));
        assertThat(view, hasItem(1L));
    }

    @Test
    void asListCoercesStringsAndBooleans() {
        List<Object> view = JsonElementUtil.asList(parse("[\"a\",true]").getAsJsonArray());

        assertThat(view, contains("a", true));
    }

    @Test
    void asListHandsBackObjectElementsUnchanged() {
        // JSON carries no type information, so an object element cannot be coerced to anything useful.
        List<Object> view = JsonElementUtil.asList(parse("[{\"a\":1}]").getAsJsonArray());

        assertThat(view, hasSize(1));
        assertThat(view.get(0), instanceOf(JsonObject.class));
    }

    @Test
    void asListReflectsLaterChangesToTheBackingArray() {
        // The view delegates rather than copying.
        JsonArray array = parse("[1]").getAsJsonArray();
        List<Object> view = JsonElementUtil.asList(array);
        array.add(2);

        assertThat(view, contains(1L, 2L));
    }

    @Test
    void asListViewSerialisesWithoutFailing() {
        // The view reaches gson.toJsonTree when a failure message is built, so a mixed view of
        // coerced scalars and untouched objects has to serialise.
        Gson gson = new Gson();
        List<Object> scalars = JsonElementUtil.asList(parse("[1,\"a\",true]").getAsJsonArray());
        List<Object> objects = JsonElementUtil.asList(parse("[{\"a\":1}]").getAsJsonArray());

        assertThat(gson.toJsonTree(scalars).toString(), is("[1,\"a\",true]"));
        assertThat(gson.toJsonTree(objects).toString(), is("[{\"a\":1}]"));
    }

    // -------------------------------------------------------------------------
    // findJsonValueAt
    // -------------------------------------------------------------------------

    @Test
    void findsNestedValueByPath() {
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("a.b", parse("{\"a\":{\"b\":\"val\"}}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), is("val"));
    }

    @Test
    void findsValueBehindMarkerPrefixedKey() {
        // The field naming strategy prefixes Set- and Map-typed field names with MARKER, so a path
        // crossing such a field has to tolerate it the way FieldsIgnorer already does.
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("aMap.k",
                parse("{\"" + FieldsIgnorer.MARKER + "aMap\":{\"k\":\"val\"}}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), is("val"));
    }

    @Test
    void findsValueBehindMarkerPrefixedKeyInSerialisedMapShape() {
        // A Map serialises to an array of single-entry objects under a MARKER-prefixed key.
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("aMap.k.leaf",
                parse("{\"" + FieldsIgnorer.MARKER + "aMap\":[{\"k\":{\"leaf\":\"val\"}}]}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), instanceOf(FanoutResult.class));
        assertThat((FanoutResult) result.getRight(), contains("val"));
    }

    @Test
    void prefersThePlainKeyOverTheMarkerPrefixedOne() {
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("x",
                parse("{\"x\":\"plain\",\"" + FieldsIgnorer.MARKER + "x\":\"marked\"}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), is("plain"));
    }

    @Test
    void stillFailsForAKeyThatExistsInNeitherForm() {
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("aMap.absent",
                parse("{\"" + FieldsIgnorer.MARKER + "aMap\":{\"k\":\"val\"}}"));

        assertTrue(result.isLeft());
    }

    @Test
    void wildcardResolvesUnderEveryMapValue() {
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("map.*.leaf",
                parse("{\"map\":[{\"k1\":{\"leaf\":1}},{\"k2\":{\"leaf\":2}}]}"));

        assertTrue(result.isRight());
        assertThat(result.getRight().toString(), is("[[1], [2]]"));
    }

    @Test
    void wildcardResolvesTheValueEvenWhenAKeyCollidesWithAFieldNameOfTheValues() {
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("map.*.name",
                parse("{\"map\":[{\"name\":{\"name\":\"x\"}},{\"p2\":{\"name\":\"y\"}}]}"));

        assertTrue(result.isRight());
        assertThat(result.getRight().toString(), is("[[x], [y]]"));
    }

    @Test
    void wildcardDescendsThroughAGraphAdapterEnvelopeRatherThanMatchingIt() {
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("*.leaf", parse("{\"0x1\":{\"a\":{\"leaf\":7}}}"));

        assertTrue(result.isRight());
        assertThat(result.getRight().toString(), is("[[7]]"));
    }

    @Test
    void wildcardCollectsOnlyFromTheChildrenThatResolveTheRestOfThePath() {
        // Lenient, matching the array fan-out it sits beside: k2 has no leaf, so the result covers k1
        // only rather than failing. A path where NO child resolves is an error -- see the next test --
        // so a typo still surfaces, but a heterogeneous map narrows what is asserted.
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("map.*.leaf",
                parse("{\"map\":[{\"k1\":{\"leaf\":1}},{\"k2\":{\"other\":2}}]}"));

        assertTrue(result.isRight());
        assertThat(result.getRight().toString(), is("[[1]]"));
    }

    @Test
    void wildcardSkipsANullChild() {
        // A null cannot be traversed further, so it is passed over rather than contributing a null.
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("*.leaf",
                parse("{\"a\":{\"leaf\":1},\"n\":null}"));

        assertTrue(result.isRight());
        assertThat((FanoutResult) result.getRight(), contains((Object) 1L));
    }

    @Test
    void wildcardOverAnEmptyObjectYieldsAnEmptyFanout() {
        // Not an error: there is nothing to reject. The empty fan-out is what the matcher then fails on,
        // so an empty container still cannot pass vacuously.
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("map.*.leaf", parse("{\"map\":{}}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), instanceOf(FanoutResult.class));
        assertThat((FanoutResult) result.getRight(), hasSize(0));
    }

    @Test
    void wildcardTreatsAGraphKeyWithANonObjectValueAsAnOrdinaryChild() {
        // Only an object under a graph-adapter key is an envelope to descend through. A graph-shaped
        // key holding an array is a plain named child, and the wildcard consumes it as one.
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("*.leaf",
                parse("{\"0x1\":[{\"leaf\":1}]}"));

        assertTrue(result.isRight());
        assertThat(result.getRight().toString(), is("[[1]]"));
    }

    @Test
    void wildcardThatResolvesNothingIsAnError() {
        // Otherwise a mistyped wildcard path would pass by matching nothing at all.
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("map.*.absent",
                parse("{\"map\":[{\"k1\":{\"leaf\":1}}]}"));

        assertTrue(result.isLeft());
    }

    @Test
    void trailingWildcardResolvesTheKeyLiterallyNamedStar() {
        Either<RuntimeException, Object> result = JsonElementUtil.findJsonValueAt("headers.*",
                parse("{\"headers\":{\"*\":\"any\",\"accept\":\"json\"}}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), is("any"));
    }

    @Test
    void fansOutThroughArray() {
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("list.id", parse("{\"list\":[{\"id\":1},{\"id\":2}]}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), instanceOf(FanoutResult.class));
        assertThat((FanoutResult) result.getRight(), contains(1L, 2L));
    }

    @Test
    void emptyArrayFansOutToEmptyResult() {
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("list.id", parse("{\"list\":[]}"));

        assertTrue(result.isRight());
        assertThat((FanoutResult) result.getRight(), hasSize(0));
    }

    @Test
    void descendsThroughGraphAdapterEnvelopeKey() {
        // 0x1 is a synthetic envelope key inserted for circular-reference types.
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("a", parse("{\"0x1\":{\"a\":\"deep\"}}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), is("deep"));
    }

    @Test
    void missingPathYieldsLeft() {
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("b", parse("{\"a\":\"v\"}"));

        assertTrue(result.isLeft());
    }

    @Test
    void descendingIntoPrimitiveIsNotNavigable() {
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("a.b", parse("{\"a\":\"v\"}"));

        assertTrue(result.isLeft());
    }

    @Test
    void nullRootYieldsLeft() {
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("a", JsonNull.INSTANCE);

        assertTrue(result.isLeft());
    }

    // -------------------------------------------------------------------------
    // jsonElementToJavaValue
    // -------------------------------------------------------------------------

    @Test
    void coercesPrimitiveTypes() {
        assertThat(JsonElementUtil.jsonElementToJavaValue(new JsonPrimitive("x")), is("x"));
        assertThat(JsonElementUtil.jsonElementToJavaValue(new JsonPrimitive(true)), is(true));
        // Whole numbers coerce to long, fractional to double.
        assertThat(JsonElementUtil.jsonElementToJavaValue(new JsonPrimitive(5)), is(5L));
        assertThat(JsonElementUtil.jsonElementToJavaValue(new JsonPrimitive(2.5)), is(2.5d));
    }

    @Test
    void coercesNullToNull() {
        assertThat(JsonElementUtil.jsonElementToJavaValue(JsonNull.INSTANCE), is((Object) null));
    }

    @Test
    void nonPrimitiveIsReturnedAsIs() {
        JsonObject obj = new JsonObject();
        assertThat(JsonElementUtil.jsonElementToJavaValue(obj), is((JsonElement) obj));
    }

    // -------------------------------------------------------------------------
    // isEmpty
    // -------------------------------------------------------------------------

    @Test
    void isEmptyForNullPrimitiveAndEmptyContainers() {
        assertThat(JsonElementUtil.isEmpty(JsonNull.INSTANCE), is(true));
        assertThat(JsonElementUtil.isEmpty(new JsonPrimitive("x")), is(true));
        assertThat(JsonElementUtil.isEmpty(new JsonArray()), is(true));
        assertThat(JsonElementUtil.isEmpty(new JsonObject()), is(true));
    }

    @Test
    void isNotEmptyForPopulatedContainers() {
        assertThat(JsonElementUtil.isEmpty(parse("[1]")), is(false));
        assertThat(JsonElementUtil.isEmpty(parse("{\"a\":1}")), is(false));
    }

    // -------------------------------------------------------------------------
    // filterByFieldMatchers
    // -------------------------------------------------------------------------

    @Test
    void removesTopLevelMatchingField() {
        JsonElement json = parse("{\"keep\":\"a\",\"secret\":\"b\"}");

        JsonElementUtil.filterByFieldMatchers(json, matchers(equalTo("secret")));

        assertThat(json.getAsJsonObject().has("secret"), is(false));
        assertThat(json.getAsJsonObject().has("keep"), is(true));
    }

    @Test
    void removesNestedMatchingFieldButKeepsNonEmptyParent() {
        JsonElement json = parse("{\"outer\":{\"secret\":\"b\",\"keep\":\"c\"}}");

        JsonElementUtil.filterByFieldMatchers(json, matchers(equalTo("secret")));

        JsonObject outer = json.getAsJsonObject().getAsJsonObject("outer");
        assertThat(outer.has("secret"), is(false));
        assertThat(outer.has("keep"), is(true));
    }

    @Test
    void cascadesRemovalWhenParentBecomesEmpty() {
        JsonElement json = parse("{\"outer\":{\"secret\":\"b\"}}");

        JsonElementUtil.filterByFieldMatchers(json, matchers(equalTo("secret")));

        // outer had only the removed field → outer itself is removed.
        assertThat(json.getAsJsonObject().has("outer"), is(false));
    }

    @Test
    void removesArrayElementThatBecomesEmpty() {
        JsonElement json = parse("{\"list\":[{\"secret\":\"b\"},{\"keep\":\"c\"}]}");

        JsonElementUtil.filterByFieldMatchers(json, matchers(equalTo("secret")));

        JsonArray list = json.getAsJsonObject().getAsJsonArray("list");
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getAsJsonObject().has("keep"), is(true));
    }

    // -------------------------------------------------------------------------
    // collectValuesByFieldNamePattern
    // -------------------------------------------------------------------------

    @Test
    void collectsValuesForMatchingFieldNames() {
        JsonElement json = parse("{\"a\":{\"id\":1},\"b\":{\"id\":2},\"c\":{\"other\":3}}");

        List<JsonElement> values =
                JsonElementUtil.collectValuesByFieldNamePattern(json, equalTo("id"));

        assertThat(values, hasSize(2));
    }

    // -------------------------------------------------------------------------
    // applyAliases
    // -------------------------------------------------------------------------

    @Test
    void appliesAliasToObjectPrimitive() {
        JsonElement json = parse("{\"id\":\"abc\"}");
        AliasMap aliases = AliasMap.builder().add("abc", "<id>").build();

        JsonElementUtil.applyAliases(json, aliases);

        assertThat(json.getAsJsonObject().get("id").getAsString(), is("<id>"));
    }

    @Test
    void appliesAliasToArrayPrimitiveElements() {
        JsonElement json = parse("{\"ids\":[\"abc\",\"def\"]}");
        AliasMap aliases = AliasMap.builder()
                .add("abc", "<id-a>")
                .add("def", "<id-d>")
                .build();

        JsonElementUtil.applyAliases(json, aliases);

        JsonArray ids = json.getAsJsonObject().getAsJsonArray("ids");
        assertThat(ids.get(0).getAsString(), is("<id-a>"));
        assertThat(ids.get(1).getAsString(), is("<id-d>"));
    }

    @Test
    void booleanPrimitivesAreNeverAliased() {
        JsonElement json = parse("{\"flag\":true}");
        AliasMap aliases = AliasMap.builder().add("true", "<aliased>").build();

        JsonElementUtil.applyAliases(json, aliases);

        assertThat(json.getAsJsonObject().get("flag").getAsBoolean(), is(true));
    }

    @Test
    void appliesAliasWithinNestedObject() {
        JsonElement json = parse("{\"outer\":{\"id\":\"abc\"}}");
        AliasMap aliases = AliasMap.builder().add("abc", "<id>").build();

        JsonElementUtil.applyAliases(json, aliases);

        assertThat(json.getAsJsonObject().getAsJsonObject("outer").get("id").getAsString(), is("<id>"));
    }

    // -------------------------------------------------------------------------
    // anyMatchesFieldName
    // -------------------------------------------------------------------------

    @Test
    void anyMatchesFieldNameReportsMatchAndMiss() {
        List<Matcher<String>> patterns = matchers(equalTo("secret"));

        assertThat(JsonElementUtil.anyMatchesFieldName("secret", patterns), is(true));
        assertThat(JsonElementUtil.anyMatchesFieldName("keep", patterns), is(false));
        assertThat(JsonElementUtil.anyMatchesFieldName("x", Collections.<Matcher<String>>emptyList()), is(false));
    }

    // -------------------------------------------------------------------------
    // filterByFieldMatchers with an IgnoredFieldsTracker (machine-readable output)
    // -------------------------------------------------------------------------

    @Test
    void trackerRecordsIgnoredPatternWhenFieldRemoved() {
        JsonElement json = parse("{\"keep\":\"a\",\"secret\":\"b\"}");
        IgnoredFieldsTracker tracker = new IgnoredFieldsTracker();

        JsonElementUtil.filterByFieldMatchers(json, matchers(equalTo("secret")), tracker,
                IgnoredFieldsTracker.Reason.CUSTOM_MATCHER_PATTERN);

        assertThat(tracker.isEmpty(), is(false));
        IgnoredFieldsTracker.IgnoredField field = tracker.getFields().get(0);
        assertThat(field.getPath(), is("secret"));
        assertThat(field.getReason(), is(IgnoredFieldsTracker.Reason.CUSTOM_MATCHER_PATTERN));
    }

    @Test
    void trackerRecordsRemovedEmptyParentWithCauses() {
        JsonElement json = parse("{\"outer\":{\"secret\":\"b\"}}");
        IgnoredFieldsTracker tracker = new IgnoredFieldsTracker();

        JsonElementUtil.filterByFieldMatchers(json, matchers(equalTo("secret")), tracker,
                IgnoredFieldsTracker.Reason.CUSTOM_MATCHER_PATTERN);

        boolean removedEmptyRecorded = tracker.getFields().stream()
                .anyMatch(f -> f.getReason() == IgnoredFieldsTracker.Reason.REMOVED_EMPTY
                        && "outer".equals(f.getPath())
                        && f.getCauses() != null && !f.getCauses().isEmpty());
        assertThat(removedEmptyRecorded, is(true));
    }

    // -------------------------------------------------------------------------
    // applyAliases with an AliasTracker
    // -------------------------------------------------------------------------

    @Test
    void aliasTrackerRecordsObjectAlias() {
        JsonElement json = parse("{\"id\":\"abc\"}");
        AliasTracker tracker = new AliasTracker();

        JsonElementUtil.applyAliases(json, AliasMap.builder().add("abc", "<id>").build(), tracker);

        assertThat(tracker.isEmpty(), is(false));
        AliasTracker.AliasedField field = tracker.getFields().get(0);
        assertThat(field.getPath(), is("id"));
        assertThat(field.getOriginalValue(), is("abc"));
        assertThat(field.getAlias(), is("<id>"));
    }

    @Test
    void aliasTrackerRecordsArrayElementAliasWithIndex() {
        JsonElement json = parse("{\"ids\":[\"abc\"]}");
        AliasTracker tracker = new AliasTracker();

        JsonElementUtil.applyAliases(json, AliasMap.builder().add("abc", "<id>").build(), tracker);

        assertThat(tracker.getFields().get(0).getPath(), is("ids[0]"));
        assertThat(tracker.getFields().get(0).getAlias(), is("<id>"));
    }

    @Test
    void filterByFieldMatchersRemovesFieldsInsideArrayElements() {
        JsonElement json = parse("[{\"secret\":1,\"keep\":2},{\"secret\":3,\"keep\":4}]");

        JsonElementUtil.filterByFieldMatchers(json, matchers(equalTo("secret")));

        for (JsonElement el : json.getAsJsonArray()) {
            assertThat(el.getAsJsonObject().has("secret"), is(false));
            assertThat(el.getAsJsonObject().has("keep"), is(true));
        }
    }

    @Test
    void collectValuesRecursesThroughArrays() {
        JsonElement json = parse("{\"list\":[{\"id\":1},{\"id\":2}],\"other\":{\"id\":3}}");

        List<JsonElement> values = JsonElementUtil.collectValuesByFieldNamePattern(json, equalTo("id"));

        assertThat(values, hasSize(3));
    }

    @Test
    void findJsonValueThroughNullMidPathReturnsNull() {
        Either<RuntimeException, Object> result =
                JsonElementUtil.findJsonValueAt("a.b", parse("{\"a\":null}"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), is((Object) null));
    }

    private static List<Matcher<String>> matchers(Matcher<String> matcher) {
        return Arrays.asList(matcher);
    }
}
