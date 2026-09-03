package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.ignores.AbstractJsonMatcherIgnoreTest;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static com.github.karsaig.approvalcrest.matchers.ChildBeanMatchers.childStringEqualTo;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;

public class JsonMatcherCustomSuccessTest extends AbstractJsonMatcherIgnoreTest {

    /** Both inputs carry childBean with childString="banana", childInteger=0. */
    public static Object[][] customeMatchersTestcases() {
        return new Object[][]{
                {"Object input", parent().childBean(child().childString("banana")).build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childInteger\": 0,\n" +
                        "    \"childString\": \"banana\"\n" +
                        "  },\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }

    /** Both inputs carry parentString="hello" in addition to childBean. */
    public static Object[][] customMatcherInputsWithParentString() {
        return new Object[][]{
                {"Object input", parent().childBean(child().childString("banana")).parentString("hello").build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childInteger\": 0,\n" +
                        "    \"childString\": \"banana\"\n" +
                        "  },\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": \"hello\"\n" +
                        "}"}
        };
    }

    /** Both inputs have a null childBean (Object omits the key; JSON string uses explicit null). */
    public static Object[][] customMatcherInputsWithNullChildBean() {
        return new Object[][]{
                {"Object input", parent().build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": null,\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }

    /** Both inputs carry a single-element childBeanList. */
    public static Object[][] customMatcherInputsWithOneChildBean() {
        return new Object[][]{
                {"Object input", parent().addToChildBeanList(child().childString("banana")).build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": null,\n" +
                        "  \"childBeanList\": [\n" +
                        "    {\n" +
                        "      \"childInteger\": 0,\n" +
                        "      \"childString\": \"banana\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }

    // -----------------------------------------------------------------------
    // Primitive string field matchers
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void matchesPrimitiveWithCustomMatcher(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("banana")), null, null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void matchesStringFieldWithContainsString(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", containsString("nan")), null, null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void matchesStringFieldWithStartsWith(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", startsWith("ban")), null, null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void matchesStringFieldWithNotMatcher(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", not(equalTo("kiwi"))), null, null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void matchesStringFieldWithGreaterThanOrEqualTo(String testName, Object input) {
        // "banana" >= "apple" lexicographically
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", greaterThanOrEqualTo("apple")), null, null);
    }

    // -----------------------------------------------------------------------
    // Integer field via JSON fallback (int bean value vs Long JSON value)
    // -----------------------------------------------------------------------

    /**
     * The bean path returns Integer(0) for an int field; equalTo(0L) fails because Integer != Long.
     * The JSON fallback returns Long(0), which passes.  This is the core int/Long bridge test.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void matchesIntegerFieldViaJsonFallback(String testName, Object input) {
        // childString stays in approved (exact match); childInteger handled by custom matcher
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"banana\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childInteger", equalTo(0L)), null, null);
    }

    // -----------------------------------------------------------------------
    // Chain of custom matchers
    // -----------------------------------------------------------------------

    /**
     * Both childString and childInteger are handled by custom matchers.
     * After filterJson removes both, childBean becomes empty and is itself removed,
     * so the approved file has no childBean at all.
     * childInteger specifically exercises the JSON fallback int/Long bridge.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void matchesFieldWithChainOfCustomMatchers(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher
                        .with("childBean.childString", equalTo("banana"))
                        .with("childBean.childInteger", equalTo(0L)),
                null, null);
    }

    /**
     * Three independent custom matchers covering three different paths.
     * Removing any single matcher would leave its path in the structural comparison;
     * the differing value in the approved file would then cause a failure.
     * {@code childBean.childInteger} also exercises the int/Long JSON-fallback bridge.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithParentString")
    public void matchesWithThreeIndependentCustomMatchers(String testName, Object input) {
        // After filtering all three paths, nothing remains inside childBean (it is pruned),
        // and parentString is also absent – only the two empty collections are left.
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher
                        .with("childBean.childString", equalTo("banana"))
                        .with("childBean.childInteger", equalTo(0L))
                        .with("parentString", equalTo("hello")),
                null, null);
    }

    // -----------------------------------------------------------------------
    // Top-level field
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithParentString")
    public void matchesTopLevelStringField(String testName, Object input) {
        // parentString handled by matcher; all other fields in approved exactly
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0,\n" +
                "    \"childString\": \"banana\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("parentString", equalTo("hello")), null, null);
    }

    // -----------------------------------------------------------------------
    // Null field
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithNullChildBean")
    public void matchesNullFieldWithNullValueMatcher(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean", nullValue()), null, null);
    }

    // -----------------------------------------------------------------------
    // Collection matcher
    // -----------------------------------------------------------------------

    /**
     * iterableWithSize works for both a Java List (Object input) and a JsonArray (JSON fallback),
     * since JsonArray implements Iterable.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithOneChildBean")
    public void matchesCollectionSizeWithIterableHasSize(String testName, Object input) {
        // childBeanList handled by matcher; after filtering, only childBeanMap remains
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", iterableWithSize(1)), null, null);
    }

    // -----------------------------------------------------------------------
    // Custom matcher rescues a value that differs from the approved snapshot
    // -----------------------------------------------------------------------

    /**
     * The approved file stores "kiwi" for childString, but the actual value is "banana".
     * Without the custom matcher the structural comparison would see banana ≠ kiwi and fail.
     * With {@code .with("childBean.childString", equalTo("banana"))}, the matcher passes and
     * the field is filtered from both sides before structural comparison, so the test passes.
     * The counterpart failure test is in {@link JsonMatcherCustomFailureTest}.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void passesWhenCustomMatcherRescuesValueThatDiffersFromApproved(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0,\n" +
                "    \"childString\": \"kiwi\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("banana")), null, null);
    }

    // -----------------------------------------------------------------------
    // Deeply nested field path (3 levels: box.item.value)
    // -----------------------------------------------------------------------

    /**
     * Custom matcher on a 3-level deep path {@code box.item.value}.
     * The value at depth is verified by the matcher; the path is also filtered from both actual
     * and the approved snapshot before structural comparison, so only {@code box.label} and
     * {@code name} are compared structurally.
     * For the JSON-string input, {@code findBeanAt} fails on the raw String and the JSON
     * fallback ({@code findJsonValueAt}) successfully navigates the parsed tree.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void strictModePassesWhenCustomMatcherFieldAbsentFromApprovedFile(String testName, Object input) {
        // .with(path, matcher) internally calls ignoring(path), so in strict mode the path is
        // stripped from actual only. If the approved file was created without that field
        // (the normal case), both sides agree and the comparison passes.
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("banana")), null, null);
    }


    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("deepContainerInputs")
    public void matchesDeeplyNestedFieldPath(String testName, Object input) {
        // After filtering box.item.value, item becomes empty and is removed, leaving box.label intact
        String approvedFileContent = "{\n" +
                "  \"box\": {\n" +
                "    \"label\": \"box1\"\n" +
                "  },\n" +
                "  \"name\": \"container\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("box.item.value", equalTo("deepValue")), null, null);
    }

    public static Object[][] deepContainerInputs() {
        Container.Box.Item item = new Container.Box.Item();
        item.value = "deepValue";
        Container.Box box = new Container.Box();
        box.label = "box1";
        box.item = item;
        Container container = new Container();
        container.name = "container";
        container.box = box;
        return new Object[][]{
                {"Object input", container},
                // JSON fields are sorted alphabetically by sortJsonFields; box (b) < name (n), item (i) < label (l)
                {"Json string input", "{\n" +
                        "  \"box\": {\n" +
                        "    \"item\": {\n" +
                        "      \"value\": \"deepValue\"\n" +
                        "    },\n" +
                        "    \"label\": \"box1\"\n" +
                        "  },\n" +
                        "  \"name\": \"container\"\n" +
                        "}"}
        };
    }

    // Test-local deeply nested data structure used by matchesDeeplyNestedFieldPath
    static class Container {
        String name;
        Box box;

        static class Box {
            String label;
            Item item;

            static class Item {
                String value;
            }
        }
    }

    // -----------------------------------------------------------------------
    // hasItem / hasEntry on collection and map fields (f6-json-map-collection-matchers)
    // -----------------------------------------------------------------------

    @Test
    public void matchesItemInCollectionWithCustomMatcher() {
        // childBeanList has two elements; the custom matcher requires "banana" to be present.
        // The approved file omits childBeanList (it is handled by the custom matcher).
        Object input = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", hasItem(childStringEqualTo("banana"))), null, null);
    }

    @Test
    public void matchesItemInMap() {
        // childBeanMap has one entry; the custom matcher checks the entry for key "key".
        // The approved file omits childBeanMap (it is handled by the custom matcher).
        Object input = parent().putToChildBeanMap("key", child().childString("banana")).build();
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanMap", hasEntry(equalTo("key"), childStringEqualTo("banana"))), null, null);
    }

    /**
     * Both childBeanList and childBeanMap are covered by bean-only matchers (hasItem / hasEntry).
     * Both pass at the bean level and must NOT be retried against the JSON form:
     * hasItem and hasEntry operate on Java collection/map types and would fail if applied
     * to the JsonArray representation used in the retry path.
     * This test verifies that multiple bean-form matchers are each evaluated once at the
     * bean level, and that neither falls through to the JSON retry when it already passed.
     */
    @Test
    public void multipleBeanOnlyMatchersPassWithoutJsonRetry() {
        Object input = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .putToChildBeanMap("key", child().childString("banana"))
                .build();
        // With serializeNulls, childBean: null and parentString: null remain after both collections are consumed by matchers.
        assertJsonMatcherWithDummyTestInfo(input, "{\n" +
                "  \"childBean\": null,\n" +
                "  \"parentString\": null\n" +
                "}", enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher
                        .with("childBeanList", hasItem(childStringEqualTo("banana")))
                        .with("childBeanMap", hasEntry(equalTo("key"), childStringEqualTo("banana"))),
                null, null);
    }

    // -----------------------------------------------------------------------
    // Two-level nested collection fanout (f6-json-nested-fanout)
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("nestedCollectionInputs")
    public void matchesPathThroughNestedCollections(String testName, Object input) {
        // Path "parentBeans.childBeanList.childString" fans out through two collection levels.
        // All leaf childString values are "apple" so the matcher passes.
        // After stripping parentBeans.childBeanList.childString only childInteger remains
        // inside each childBeanList entry; childBeanMap is [] for each parentBean.
        String approvedFileContent = "{\n" +
                "  \"parentBeans\": [\n" +
                "    {\n" +
                "      \"childBean\": null,\n" +
                "      \"childBeanList\": [\n" +
                "        {\"childInteger\": 0},\n" +
                "        {\"childInteger\": 0}\n" +
                "      ],\n" +
                "      \"childBeanMap\": [],\n" +
                "      \"parentString\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"childBean\": null,\n" +
                "      \"childBeanList\": [\n" +
                "        {\"childInteger\": 0}\n" +
                "      ],\n" +
                "      \"childBeanMap\": [],\n" +
                "      \"parentString\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("parentBeans.childBeanList.childString", equalTo("apple")), null, null);
    }

    public static Object[][] nestedCollectionInputs() {
        ParentBean p1 = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("apple"))
                .build();
        ParentBean p2 = parent()
                .addToChildBeanList(child().childString("apple"))
                .build();
        return new Object[][]{
                {"Object input", new NestedWrapper(Arrays.asList(p1, p2))},
                {"Json string input",
                        "{\n" +
                        "  \"parentBeans\": [\n" +
                        "    {\n" +
                        "      \"childBean\": null,\n" +
                        "      \"childBeanList\": [\n" +
                        "        {\"childInteger\": 0, \"childString\": \"apple\"},\n" +
                        "        {\"childInteger\": 0, \"childString\": \"apple\"}\n" +
                        "      ],\n" +
                        "      \"childBeanMap\": [],\n" +
                        "      \"parentString\": null\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"childBean\": null,\n" +
                        "      \"childBeanList\": [\n" +
                        "        {\"childInteger\": 0, \"childString\": \"apple\"}\n" +
                        "      ],\n" +
                        "      \"childBeanMap\": [],\n" +
                        "      \"parentString\": null\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("nullElementCollectionInputs")
    public void matchesPathThroughCollectionContainingNullElement(String testName, Object input) {
        // childBeanList contains a null element followed by a real element.
        // anyOf(nullValue, equalTo("banana")) ensures no NPE on the null element.
        // After stripping childBeanList.childString the list retains [null, {childInteger:0}].
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [\n" +
                "    null,\n" +
                "    {\"childInteger\": 0}\n" +
                "  ],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList.childString", org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.nullValue(), equalTo("banana"))), null, null);
    }

    public static Object[][] nullElementCollectionInputs() {
        return new Object[][]{
                {"Object input", parent()
                        .addToChildBeanList((com.github.karsaig.approvalcrest.testdata.ChildBean) null)
                        .addToChildBeanList(child().childString("banana"))
                        .build()},
                {"Json string input",
                        "{\n" +
                        "  \"childBean\": null,\n" +
                        "  \"childBeanList\": [null, {\"childString\": \"banana\", \"childInteger\": 0}],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }

    static class NestedWrapper {
        List<ParentBean> parentBeans;
        NestedWrapper(List<ParentBean> parentBeans) { this.parentBeans = parentBeans; }
    }

    // -----------------------------------------------------------------------
    // with(Matcher<String>, Matcher<V>) — pattern-based custom matchers
    // -----------------------------------------------------------------------

    /** Both inputs carry childBean with childString="banana". */
    public static Object[][] patternMatcherTestCases() {
        return new Object[][]{
                {"Object input", parent().childBean(child().childString("banana")).build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childInteger\": 0,\n" +
                        "    \"childString\": \"banana\"\n" +
                        "  },\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }

    /** Both inputs have childString in childBean AND in one childBeanList element. */
    public static Object[][] patternMatcherMultiFieldTestCases() {
        return new Object[][]{
                {"Object input", parent().childBean(child().childString("banana")).addToChildBeanList(child().childString("banana")).build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childInteger\": 0,\n" +
                        "    \"childString\": \"banana\"\n" +
                        "  },\n" +
                        "  \"childBeanList\": [\n" +
                        "    {\n" +
                        "      \"childInteger\": 0,\n" +
                        "      \"childString\": \"banana\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("patternMatcherTestCases")
    public void matchesNestedFieldWithPatternMatcher(String testName, Object input) {
        // Pattern "childString" finds childBean.childString; custom matcher passes; field stripped from approved.
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.withMatcher(equalTo("childString"), equalTo("banana")), null, null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("patternMatcherMultiFieldTestCases")
    public void patternMatcherMatchesMultipleFieldsAtDifferentLevels(String testName, Object input) {
        // Pattern "childString" matches both childBean.childString and childBeanList[0].childString.
        // Both values are "banana" → matcher passes for each; both fields stripped from approved.
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [\n" +
                "    {\n" +
                "      \"childInteger\": 0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.withMatcher(equalTo("childString"), equalTo("banana")), null, null);
    }

    // -----------------------------------------------------------------------
    // withMatcher — strict mode
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customeMatchersTestcases")
    public void strictModePassesWhenPatternMatcherFieldAbsentFromApprovedFile(String testName, Object input) {
        // In strict mode the approved file is taken as-is (filterByCustomMatcherPatterns skipped on
        // expected). The actual still has the field stripped. If the approved file was correctly
        // created without the pattern-matched field, both sides agree → passes.
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.withMatcher(equalTo("childString"), equalTo("banana")), null, null);
    }

    // -----------------------------------------------------------------------
    // plain-string / JSON-primitive input
    // -----------------------------------------------------------------------

    @Test
    public void withMatcherOnJsonPrimitiveStringInputIsVacuouslyIgnored() {
        // When the input is a Java String containing a JSON primitive (e.g. "\"hello\""),
        // getAsJsonElement() parses it to JsonPrimitive.  withMatcher(pattern, valueMatcher)
        // calls collectValuesByFieldNamePattern on a primitive — no fields are found, so the
        // custom matcher is silently skipped.  The assertion falls back to JSON equality.
        String actual = "\"hello\"";
        String approved = "\"hello\"";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.withMatcher(containsString("field"), equalTo("anything")),
                (String) null);
    }

    @Test
    public void withMatcherOnRawNonJsonStringInput() {
        // When the input is a Java String that is NOT valid strict JSON (e.g. "hello" without
        // quotes), JsonParser.parseString() is called.  Gson's parser is lenient and treats bare
        // identifiers as string primitives, so the result is JsonPrimitive("hello").
        // withMatcher finds no fields → vacuously ignored → equality passes.
        String actual = "hello";
        String approved = "\"hello\"";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.withMatcher(containsString("field"), equalTo("anything")),
                (String) null);
    }

    // -----------------------------------------------------------------------
    // Container matchers: size, order and content of the collection itself
    // -----------------------------------------------------------------------

    /** Input carrying a two-element childBeanList, as an object and as a JSON string. */
    public static Object[][] customMatcherInputsWithTwoChildBeans() {
        return new Object[][]{
                {"Object input", parent()
                        .addToChildBeanList(child().childString("apple"))
                        .addToChildBeanList(child().childString("banana"))
                        .build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": null,\n" +
                        "  \"childBeanList\": [\n" +
                        "    {\n" +
                        "      \"childInteger\": 0,\n" +
                        "      \"childString\": \"apple\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"childInteger\": 0,\n" +
                        "      \"childString\": \"banana\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }


    /**
     * hasSize is typed on Collection. It resolves against the real {@code List<ChildBean>} for an
     * object input, and against a list view over the JsonArray for a JSON string input, so it holds
     * for both forms.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithTwoChildBeans")
    public void matchesCollectionSizeWithHasSize(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_CHILD_BEAN_LIST, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", hasSize(2)), null, null);
    }

    /** empty() likewise evaluates for real on a JSON string input. */
    @Test
    public void matchesEmptyCollectionWithEmptyOnJsonStringInput() {
        String emptyListAsJson = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(emptyListAsJson, APPROVED_WITHOUT_CHILD_BEAN_LIST,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", empty()), null, null);
    }

    /** hasSize composed with another matcher — "at least one element" without pinning the count. */
    @Test
    public void matchesCollectionSizeWithHasSizeOfMatcherOnObjectInput() {
        Object input = parent().addToChildBeanList(child().childString("apple")).build();
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_CHILD_BEAN_LIST, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", hasSize(greaterThan(0))), null, null);
    }

    /** contains asserts the exact elements in the exact order. Object input only, as for hasSize. */
    @Test
    public void matchesCollectionInOrderWithContainsOnObjectInput() {
        Object input = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_CHILD_BEAN_LIST, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList",
                        contains(childStringEqualTo("apple"), childStringEqualTo("banana"))), null, null);
    }

    /** containsInAnyOrder asserts the exact elements, order irrelevant. Object input only. */
    @Test
    public void matchesCollectionIgnoringOrderWithContainsInAnyOrderOnObjectInput() {
        Object input = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_CHILD_BEAN_LIST, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList",
                        containsInAnyOrder(childStringEqualTo("banana"), childStringEqualTo("apple"))), null, null);
    }

    /**
     * iterableWithSize works for both input forms, because JsonArray implements Iterable.
     * This is the portable way to assert a collection's size in an approval test.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithTwoChildBeans")
    public void matchesCollectionSizeWithIterableWithSizeForBothInputForms(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_CHILD_BEAN_LIST, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", iterableWithSize(2)), null, null);
    }

    /** aMapWithSize on a Map field — the map itself is handed to the matcher, not its entries. */
    @Test
    public void matchesMapSizeWithAMapWithSizeOnObjectInput() {
        Object input = parent().putToChildBeanMap("key", child().childString("banana")).build();
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanMap", aMapWithSize(1)), null, null);
    }

    // -----------------------------------------------------------------------
    // Container matcher combined with ignoring() on a sibling collection
    // -----------------------------------------------------------------------

    /**
     * A container matcher on one collection and ignoring() on a sibling collection are
     * independent: childBeanList is asserted by iterableWithSize while childBeanMap is dropped
     * from the comparison, so it need not appear in the approved file at all.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithTwoChildBeans")
    public void matchesCollectionWhileIgnoringSiblingCollection(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher
                        .with("childBeanList", iterableWithSize(2))
                        .ignoring("childBeanMap"), null, null);
    }

    /** Fan-out through one collection while a sibling collection is ignored. */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithTwoChildBeans")
    public void matchesPathThroughCollectionWhileIgnoringSiblingCollection(String testName, Object input) {
        // Only childString is consumed by the matcher; childInteger remains in each element.
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [\n" +
                "    {\n" +
                "      \"childInteger\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"childInteger\": 0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher
                        .with("childBeanList.childString", anyOf(equalTo("apple"), equalTo("banana")))
                        .ignoring("childBeanMap"), null, null);
    }

    /**
     * A map is traversed by key: the key is a path segment. childBeanMap is Map-typed and so carries
     * a prefixed JSON key, and the bean walker has no Map branch, so the path falls through to the
     * JSON tree and the prefix has to be recognised there.
     */
    @Test
    public void matchesPropertyOfMapEntryAddressedByKey() {
        Object input = parent().putToChildBeanMap("key", child().childString("banana")).build();
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [\n" +
                "    {\n" +
                "      \"key\": {\n" +
                "        \"childInteger\": 0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanMap.key.childString", equalTo("banana")), null, null);
    }

    // -----------------------------------------------------------------------
    // Collections of scalars
    // -----------------------------------------------------------------------

    /** A bean carrying a collection of scalars rather than of beans. */
    static class ScalarCollectionWrapper {
        String name;
        List<String> tags;

        ScalarCollectionWrapper(String name, List<String> tags) {
            this.name = name;
            this.tags = tags;
        }
    }

    public static Object[][] scalarCollectionInputs() {
        return new Object[][]{
                {"Object input", new ScalarCollectionWrapper("n", Arrays.asList("urgent", "review"))},
                {"Json string input", "{\n" +
                        "  \"name\": \"n\",\n" +
                        "  \"tags\": [\n" +
                        "    \"urgent\",\n" +
                        "    \"review\"\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    private static final String APPROVED_WITHOUT_TAGS = "{\n  \"name\": \"n\"\n}";

    /**
     * Element matchers phrased against the value match a collection of scalars on either input form:
     * the object resolves to a real List, and a JSON string resolves to a list view whose scalar
     * elements are coerced on read.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("scalarCollectionInputs")
    public void matchesScalarCollectionInOrderWithContains(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_TAGS, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("tags", contains("urgent", "review")), null, null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("scalarCollectionInputs")
    public void matchesScalarCollectionIgnoringOrderWithContainsInAnyOrder(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_TAGS, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("tags", containsInAnyOrder("review", "urgent")), null, null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("scalarCollectionInputs")
    public void matchesScalarCollectionMemberWithHasItem(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_TAGS, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("tags", hasItem("urgent")), null, null);
    }

    // -----------------------------------------------------------------------
    // Negated container matchers
    // -----------------------------------------------------------------------

    /** not(hasItem(x)) passes when x is absent from the collection. */
    @Test
    public void matchesNegatedHasItemWhenElementIsAbsentOnObjectInput() {
        Object input = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_CHILD_BEAN_LIST, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", not(hasItem(childStringEqualTo("kiwi")))), null, null);
    }

    /** not(empty()) passes on a non-empty collection. */
    @Test
    public void matchesNegatedEmptyOnNonEmptyCollectionOnObjectInput() {
        Object input = parent().addToChildBeanList(child().childString("apple")).build();
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_CHILD_BEAN_LIST, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", not(empty())), null, null);
    }

    // -----------------------------------------------------------------------
    // Known limitation: a negated element matcher over objects, JSON-string input
    //
    // These assert a pass, and that is the point. With a JSON-string input there is no element
    // class, so a matcher written against one cannot match a JsonObject; un-negated that is a loud
    // failure ("was JsonObject <...>"), but negated the same non-match makes not(...) true and the
    // assertion cannot fail whatever the data holds.
    //
    // Not fixable from the value side: a container matcher and an element matcher are handed the
    // same JsonArray, so any guard that rejected one would break hasSize and iterableWithSize,
    // which are pinned for this input form. Hamcrest's expected type and negation flag are private,
    // so the matcher cannot be interrogated either.
    //
    // The boundary is covered by the scalar cases in JsonMatcherCustomFailureTest, which do fail
    // because the list view coerces a scalar on read. Change any of these deliberately.
    // -----------------------------------------------------------------------

    private static final String CHILD_BEAN_LIST_JSON = "{\n"
            + "  \"childBean\": null,\n"
            + "  \"childBeanList\": [\n"
            + "    {\n"
            + "      \"childInteger\": 1,\n"
            + "      \"childString\": \"apple\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"childBeanMap\": [],\n"
            + "  \"parentString\": null\n"
            + "}";

    private static final String CHILD_BEAN_LIST_JSON_APPROVED = "{\n"
            + "  \"childBean\": null,\n"
            + "  \"childBeanMap\": [],\n"
            + "  \"parentString\": null\n"
            + "}";

    @Test
    public void negatedHasItemOverObjectsCannotFailOnJsonStringInput() {
        // "apple" is present, so this ought to fail.
        assertJsonMatcherWithDummyTestInfo(CHILD_BEAN_LIST_JSON, CHILD_BEAN_LIST_JSON_APPROVED,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", not(hasItem(childStringEqualTo("apple")))), null);
    }

    @Test
    public void negatedContainsOverObjectsCannotFailOnJsonStringInput() {
        assertJsonMatcherWithDummyTestInfo(CHILD_BEAN_LIST_JSON, CHILD_BEAN_LIST_JSON_APPROVED,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", not(contains(childStringEqualTo("apple")))), null);
    }

    @Test
    public void negatedContainsInAnyOrderOverObjectsCannotFailOnJsonStringInput() {
        assertJsonMatcherWithDummyTestInfo(CHILD_BEAN_LIST_JSON, CHILD_BEAN_LIST_JSON_APPROVED,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList",
                        not(containsInAnyOrder(childStringEqualTo("apple")))), null);
    }

    // -----------------------------------------------------------------------
    // Matchers the compatibility table documents as working
    // -----------------------------------------------------------------------

    @Test
    public void matchesMapKeyOnObjectInput() {
        // hasKey works on object input because the map arrives as a java.util.Map. Through a JSON
        // string it cannot, since a map serialises to a JsonArray of single-entry objects.
        Object input = parent().putToChildBeanMap("key", child().childString("banana")).build();
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanMap", hasKey("key")), null);
    }

    @Test
    public void matchesEmptyIterableOnObjectInput() {
        // emptyIterable is documented as working for both input forms.
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(parent().build(), approvedFileContent,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", emptyIterable()), null);
    }

    @Test
    public void matchesEmptyIterableOnJsonStringInput() {
        // The other half of that row: the read-only list view over the JsonArray is an Iterable,
        // so this holds without the elements needing a class.
        String input = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", emptyIterable()), null);
    }
}
