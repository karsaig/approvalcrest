package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.BeanFinder.FanoutResult;
import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
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

    private static List<Matcher<String>> matchers(Matcher<String> matcher) {
        return Arrays.asList(matcher);
    }
}
