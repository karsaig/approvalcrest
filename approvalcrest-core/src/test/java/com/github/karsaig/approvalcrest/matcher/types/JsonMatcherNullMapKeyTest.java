package com.github.karsaig.approvalcrest.matcher.types;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;

/**
 * A {@code null} map key. It used to abort the comparison with a NullPointerException, and whether it did
 * depended on what the map's other keys rendered as — the predicate walks keys in natural order of their
 * JSON, so a collection key (rendering {@code [}) short-circuited it before the null was reached while a
 * bean key (rendering <code>{</code>) did not.
 *
 * <p>It is now recorded as the member name {@code "null"}, which keeps the map in the single-key-object
 * branch: a form a JSON string input can express and a path can address. In a map whose other keys are not
 * primitives, Strings or enums the whole map takes the pair branch anyway, and the key is written as a bare
 * JSON null there.
 */
public class JsonMatcherNullMapKeyTest extends AbstractFileMatcherTest {

    static class Holder {
        Map<Object, Object> m;

        Holder(Map<Object, Object> m) {
            this.m = m;
        }
    }

    private static Map<Object, Object> map(Object... keyThenValue) {
        Map<Object, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keyThenValue.length; i += 2) {
            m.put(keyThenValue[i], keyThenValue[i + 1]);
        }
        return m;
    }

    private static final String NULL_KEY_ONLY = "{\n  \"m\": [\n    {\n      \"null\": \"v\"\n    }\n  ]\n}";

    private static final String NULL_KEY_AND_ORDINARY_KEY =
            "{\n  \"m\": [\n    {\n      \"k\": \"w\"\n    },\n    {\n      \"null\": \"v\"\n    }\n  ]\n}";

    @Test
    public void aNullKeyIsRecordedAsTheMemberNameNull() {
        assertJsonMatcherWithDummyTestInfo(new Holder(map((Object) null, "v")), NULL_KEY_ONLY,
                Function.identity(), null);
    }

    @Test
    public void theSameTextAsAJsonStringInputComparesEqual() {
        // The point of choosing this rendering: what the object produces is text a JSON string input can
        // express, so both input forms agree.
        assertJsonMatcherWithDummyTestInfo(NULL_KEY_ONLY, NULL_KEY_ONLY, Function.identity(), null);
    }

    @Test
    public void theGeneratedFileUsesTheSameText() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(new Holder(map((Object) null, "v")),
                NULL_KEY_ONLY, Function.identity());
    }

    @Test
    public void aNullKeyIsOrderedAmongOrdinaryKeys() {
        assertJsonMatcherWithDummyTestInfo(new Holder(map(null, "v", "k", "w")), NULL_KEY_AND_ORDINARY_KEY,
                Function.identity(), null);
    }

    @Test
    public void aNullTolerantTreeMapIsNotExcludedByItsType() {
        // Natural ordering rejects a null key, but a null-tolerant comparator does not, so a TreeMap reaches
        // this code like any other map.
        Map<Object, Object> m = new TreeMap<>(Comparator.nullsFirst(Comparator.comparing(Object::toString)));
        m.put(null, "v");
        m.put("k", "w");

        assertJsonMatcherWithDummyTestInfo(new Holder(m), NULL_KEY_AND_ORDINARY_KEY, Function.identity(), null);
    }

    @Test
    public void aNullValueIsRecorded() {
        // Shares this code path and already worked; pinned so it stays that way.
        assertJsonMatcherWithDummyTestInfo(new Holder(map("k", null)),
                "{\n  \"m\": [\n    {\n      \"k\": null\n    }\n  ]\n}", Function.identity(), null);
    }

    @Test
    public void aNullKeyBesideABeanKeyIsWrittenAsABareJsonNull() {
        // The scope limit: one complex key sends the whole map to the pair branch, where the null key is a
        // bare JSON null rather than a member name. This is the case that threw before.
        String expected = "{\n  \"m\": [\n    [\n      null,\n      \"v\"\n    ],\n    [\n      {\n"
                + "        \"childInteger\": 0,\n        \"childString\": \"b\"\n      },\n      \"w\"\n    ]\n  ]\n}";

        assertJsonMatcherWithDummyTestInfo(new Holder(map(null, "v", child().childString("b").build(), "w")),
                expected, Function.identity(), null);
    }

    @Test
    public void aNullKeyBesideACollectionKeyIsUnchanged() {
        // A guard, not proof: a collection key renders "[", which sorts before "null", so the predicate
        // already returned false before reaching the null and this case never threw. Its output is
        // byte-identical before and after the fix.
        String expected = "{\n  \"m\": [\n    [\n      [\n        \"x\"\n      ],\n      \"w\"\n    ],\n"
                + "    [\n      null,\n      \"v\"\n    ]\n  ]\n}";

        assertJsonMatcherWithDummyTestInfo(new Holder(map(null, "v", Arrays.asList("x"), "w")),
                expected, Function.identity(), null);
    }

    @Test
    public void aNullKeyAndTheStringNullRenderIdentically() {
        // The cost of this rendering. Both entries survive as separate elements in a stable order, so the
        // file is ambiguous rather than lossy -- but it cannot say which entry had the null key.
        assertJsonMatcherWithDummyTestInfo(new Holder(map(null, "a", "null", "b")),
                "{\n  \"m\": [\n    {\n      \"null\": \"a\"\n    },\n    {\n      \"null\": \"b\"\n    }\n  ]\n}",
                Function.identity(), null);
    }
}
