package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.testdata.Bean;
import com.github.karsaig.approvalcrest.testdata.ChildBean;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.github.karsaig.approvalcrest.matchers.ChildBeanMatchers.childStringEqualTo;
import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class BeanMatcherCustomSuccessTest extends AbstractBeanMatcherTest {

    @Test
    public void matchesPrimitiveWithCustomMatcher() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean.childString", equalTo("banana")));
    }

    // -----------------------------------------------------------------------
    // with(Matcher<String>, Matcher<V>) — pattern-based custom matchers
    // -----------------------------------------------------------------------

    @Test
    public void matchesNestedFieldWithPatternMatcher() {
        // Pattern "childString" matches childBean.childString — the actual value "banana" passes.
        // Expected has "apple" which would fail structurally; the pattern matcher rescues it.
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.withMatcher(equalTo("childString"), equalTo("banana")));
    }

    @Test
    public void matchesTopLevelFieldWithPatternMatcher() {
        // Pattern "parentString" matches parentString at the top level of ParentBean.
        ParentBean expected = parent().childBean(child().childString("apple")).parentString("hello").build();
        ParentBean actual = parent().childBean(child().childString("apple")).parentString("world").build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.withMatcher(equalTo("parentString"), equalTo("world")));
    }

    @Test
    public void matchesWithChainOfPatternMatchers() {
        // Chain two pattern matchers: one for childString, one for parentString.
        ParentBean expected = parent().childBean(child().childString("apple")).parentString("hello").build();
        ParentBean actual = parent().childBean(child().childString("banana")).parentString("world").build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .withMatcher(equalTo("childString"), equalTo("banana"))
                .withMatcher(equalTo("parentString"), equalTo("world")));
    }

    @Test
    public void patternMatcherCombinedWithPathMatcher() {
        // Path-based matcher handles parentString; pattern-based handles all "childString" fields.
        ParentBean expected = parent().childBean(child().childString("apple")).parentString("hello").build();
        ParentBean actual = parent().childBean(child().childString("banana")).parentString("world").build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .with("parentString", equalTo("world"))
                .withMatcher(equalTo("childString"), equalTo("banana")));
    }

    @Test
    public void patternMatcherMatchesMultipleFieldsAtDifferentLevels() {
        // Pattern "childString" matches childBean.childString AND each element in childBeanList.
        // Both actual values are "banana" → both pass equalTo("banana").
        ParentBean expected = parent()
                .childBean(child().childString("apple"))
                .addToChildBeanList(child().childString("apple"))
                .build();
        ParentBean actual = parent()
                .childBean(child().childString("banana"))
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withMatcher(equalTo("childString"), equalTo("banana")));
    }

    @Test
    public void patternMatcherPassesVacuouslyWhenNoFieldsMatch() {
        // Pattern "nonExistentField" matches nothing → vacuous pass; structural comparison decides.
        ParentBean expected = parent().childBean(child().childString("banana")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withMatcher(equalTo("nonExistentField"), equalTo("anything")));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchOnPrimitive() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean.childString", equalTo("kiwi")), "\n" +
                "Expected: {\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}\n" +
                "and childBean.childString \"kiwi\"\n" +
                "     but: childBean.childString was \"banana\"");
    }

    @Test
    public void matchesFieldWithCustomMatcher() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana").childInteger(2)).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean", childStringEqualTo("banana")));
    }

    @Test
    public void matchesFieldWithChainOfCustomMatchers() {
        ParentBean expected = parent().childBean(child().childString("apple")).parentString("kiwi").build();
        ParentBean actual = parent().childBean(child().childString("banana").childInteger(2)).parentString("strawberry").build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean", childStringEqualTo("banana")).with("parentString", equalTo("strawberry")));
    }

    @Test
    public void matchesWithThreeIndependentCustomMatchers() {
        // expected and actual differ on all three paths.  Removing any one matcher would leave
        // that path in the structural comparison and the differing value would cause a failure.
        // childBean.childInteger also exercises the int/Long JSON-fallback bridge (Integer vs Long).
        ParentBean expected = parent()
                .childBean(child().childString("kiwi").childInteger(5))
                .parentString("apple")
                .build();
        ParentBean actual = parent()
                .childBean(child().childString("banana").childInteger(9))
                .parentString("strawberry")
                .build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .with("childBean.childString", equalTo("banana"))
                .with("childBean.childInteger", equalTo(9L))
                .with("parentString", equalTo("strawberry")));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchOnField() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean", childStringEqualTo("kiwi")), "\n" +
                "Expected: {\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}\n" +
                "and childBean having string field \"kiwi\"\n" +
                "     but: childBean string field was \"banana\"\n" +
                "{\n" +
                "  \"childString\": \"banana\",\n" +
                "  \"childInteger\": 0\n" +
                "}");
    }

    @Test
    public void matchesItemInCollectionWithCustomMatcher() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple")).addToChildBeanList(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", hasItem(childStringEqualTo("banana"))));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchACollection() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple")).addToChildBeanList(child().childString("banana")).build();

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", hasItem(childStringEqualTo("kiwi"))), "\n" +
                "Expected: {\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}\n" +
                "and childBeanList a collection containing having string field \"kiwi\"\n" +
                "     but: childBeanList mismatches were: [string field was \"apple\", string field was \"banana\"]\n" +
                "[\n" +
                "  {\n" +
                "    \"childString\": \"apple\",\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  {\n" +
                "    \"childString\": \"banana\",\n" +
                "    \"childInteger\": 0\n" +
                "  }\n" +
                "]");
    }

    @Test
    public void matchesItemInMap() {
        ParentBean expected = parent().putToChildBeanMap("key", child().childString("apple")).build();
        ParentBean actual = parent().putToChildBeanMap("key", child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanMap", hasEntry(equalTo("key"), childStringEqualTo("banana"))));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchAMap() {
        ParentBean expected = parent().putToChildBeanMap("key", child().childString("apple")).build();
        ParentBean actual = parent().putToChildBeanMap("key", child().childString("banana")).build();

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanMap", hasEntry(equalTo("key"), childStringEqualTo("kiwi"))), "\n" +
                "Expected: {\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"parentString\": null\n" +
                "}\n" +
                "and childBeanMap map containing [\"key\"->having string field \"kiwi\"]\n" +
                "     but: childBeanMap map was [<key=ChildBean{childString='banana', childInteger=0}>]\n" +
                "[\n" +
                "  {\n" +
                "    \"key\": {\n" +
                "      \"childString\": \"banana\",\n" +
                "      \"childInteger\": 0\n" +
                "    }\n" +
                "  }\n" +
                "]");
    }

    @Test
    public void failsWhenActualIsNull() {
        Bean expected = bean().build();
        Bean actual = null;

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("string", startsWith("field")), "\n" +
                "Expected: {\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": null,\n" +
                "  \"set\": null\n" +
                "}\n" +
                "and string a string starting with \"field\"\n" +
                "     but: string was null");
    }

    @Test
    public void matchesIntFieldViaJsonFallback() {
        // Bean path for an int/Integer field returns Integer(0); equalTo(0L) fails because
        // Integer != Long.  The JSON fallback returns Long(0), which passes the matcher.
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("apple")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean.childInteger", equalTo(0L)));
    }

    @Test
    public void matchesPrimitiveWithCustomMatcherRescuingDifferentValue() {
        // expected has "kiwi", actual has "banana".  Without the custom matcher the structural
        // comparison would see kiwi != banana and fail.  With the custom matcher the field is
        // filtered from both sides before comparison, so only the matching parts are compared.
        ParentBean expected = parent().childBean(child().childString("kiwi")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean.childString", equalTo("banana")));
    }

    @Test
    public void matchesDeeplyNestedFieldPath() {
        // 3-level deep path box.item.value is verified by the custom matcher; box.label and name
        // are compared structurally.
        BeanContainer.BeanBox.BeanItem item = new BeanContainer.BeanBox.BeanItem();
        item.value = "deepValue";
        BeanContainer.BeanBox box = new BeanContainer.BeanBox();
        box.label = "box1";
        box.item = item;
        BeanContainer expected = new BeanContainer();
        expected.name = "container";
        expected.box = box;

        BeanContainer.BeanBox.BeanItem actualItem = new BeanContainer.BeanBox.BeanItem();
        actualItem.value = "deepValue";
        BeanContainer.BeanBox actualBox = new BeanContainer.BeanBox();
        actualBox.label = "box1";
        actualBox.item = actualItem;
        BeanContainer actual = new BeanContainer();
        actual.name = "container";
        actual.box = actualBox;

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("box.item.value", equalTo("deepValue")));
    }

    // 3-level nested data structure used by matchesDeeplyNestedFieldPath
    static class BeanContainer {
        String name;
        BeanBox box;

        static class BeanBox {
            String label;
            BeanItem item;

            static class BeanItem {
                String value;
            }
        }
    }

    @Test
    public void matchesPropertyOfItemInCollectionWithCustomMatcher() {
        // childString values differ between expected and actual but are handled by the custom matcher.
        // Only childInteger is compared structurally, so it must match.
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).addToChildBeanList(child().childString("pear")).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple")).addToChildBeanList(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList.childString", Matchers.oneOf("apple","banana")));
    }

    /**
     * Verifies transparent fanout through TWO nested collection levels.
     * Path "parentBeans.childBeanList.childString" traverses:
     *   List&lt;ParentBean&gt; → List&lt;ChildBean&gt; → childString
     * Every leaf value must satisfy the custom matcher.
     */
    @Test
    public void matchesPathThroughNestedCollections() {
        ParentBean p1 = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("apple"))
                .build();
        ParentBean p2 = parent()
                .addToChildBeanList(child().childString("apple"))
                .build();

        NestedCollectionWrapper expected = new NestedCollectionWrapper(Arrays.asList(p1, p2));
        NestedCollectionWrapper actual   = new NestedCollectionWrapper(Arrays.asList(p1, p2));

        // All leaf childString values are "apple" → all must pass the matcher
        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("parentBeans.childBeanList.childString", equalTo("apple")));
    }

    @Test
    public void matchesPathThroughCollectionContainingNullElement() {
        // childBeanList contains a null element; the custom matcher uses anyOf so both
        // null and "banana" pass — confirming no NPE occurs on the null element.
        ParentBean expected = parent()
                .addToChildBeanList((com.github.karsaig.approvalcrest.testdata.ChildBean) null)
                .addToChildBeanList(child().childString("banana"))
                .build();
        ParentBean actual = parent()
                .addToChildBeanList((com.github.karsaig.approvalcrest.testdata.ChildBean) null)
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("childBeanList.childString", org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.nullValue(), equalTo("banana"))));
    }

    static class NestedCollectionWrapper {
        List<ParentBean> parentBeans;
        NestedCollectionWrapper(List<ParentBean> parentBeans) {
            this.parentBeans = parentBeans;
        }
    }

    @Test
    public void patternMatcherPassesVacuouslyWhenNullFieldAbsentFromSerializedJson() {
        // With serializeNulls enabled (default), null fields appear in the JSON and pattern
        // matchers can find them. Call withoutSerializingNulls() to restore the old behaviour
        // where null fields are absent, causing the pattern matcher to pass vacuously.
        ParentBean actual   = parent().build();   // childBean == null
        ParentBean expected = parent().build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withoutSerializingNulls()
                                          .withMatcher(equalTo("childBean"), notNullValue()));
    }

    @Test
    public void toStringReturnsSameBeanAsMatcherForBeanInput() {
        DiagnosingCustomisableMatcher<Object> underTest = MATCHER_FACTORY.beanMatcher(Bean.Builder.bean().build());
        org.junit.jupiter.api.Assertions.assertEquals("SameBeanAs matcher", underTest.toString());
    }

    @Test
    public void toStringReturnsSameBeanAsNullMatcherForNullInput() {
        DiagnosingCustomisableMatcher<Object> underTest = MATCHER_FACTORY.beanMatcher(null);
        org.junit.jupiter.api.Assertions.assertEquals("SameBeanAs null matcher", underTest.toString());
    }

    @Test
    public void toStringReturnsSameBeanAsEqualsMatcherForPrimitiveInput() {
        DiagnosingCustomisableMatcher<Object> underTest = MATCHER_FACTORY.beanMatcher("hello");
        org.junit.jupiter.api.Assertions.assertEquals("SameBeanAs equals matcher", underTest.toString());
    }

    // -----------------------------------------------------------------------
    // plain-string input (no JSON object/array)
    // -----------------------------------------------------------------------

    @Test
    public void withMatcherOnPlainStringIsVacuouslyIgnored() {
        // For plain strings, beanMatcher() returns an IsEqualMatcher which overrides
        // doMatches() with a plain equalTo() check and never inspects matcherConfiguration.
        // Any withMatcher()/with()/sortFieldPath() calls are accepted but silently ignored;
        // the assertion passes or fails purely by string equality.
        assertDiagnosingMatcher("hello", "hello",
                beanMatcher -> beanMatcher.withMatcher(containsString("field"), equalTo("anything")));
    }

    // -----------------------------------------------------------------------
    // Container matchers: size, order and content of the collection itself
    // -----------------------------------------------------------------------

    /**
     * hasSize on a List field. The path terminates at the collection, so BeanFinder returns the
     * real {@code List<ChildBean>} and Collection-typed Hamcrest matchers apply directly.
     */
    @Test
    public void matchesCollectionSizeWithHasSize() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", hasSize(2)));
    }

    /** hasSize composed with another matcher — asserts "at least one element" without pinning the count. */
    @Test
    public void matchesCollectionSizeWithHasSizeOfMatcher() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", hasSize(greaterThan(0))));
    }

    /** contains asserts the exact elements in the exact order. */
    @Test
    public void matchesCollectionInOrderWithContains() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList",
                contains(childStringEqualTo("apple"), childStringEqualTo("banana"))));
    }

    /** containsInAnyOrder asserts the exact elements, order irrelevant. */
    @Test
    public void matchesCollectionIgnoringOrderWithContainsInAnyOrder() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList",
                containsInAnyOrder(childStringEqualTo("banana"), childStringEqualTo("apple"))));
    }

    /** aMapWithSize on a Map field — the map itself is handed to the matcher, not its entries. */
    @Test
    public void matchesMapSizeWithAMapWithSize() {
        ParentBean expected = parent().putToChildBeanMap("key", child().childString("kiwi")).build();
        ParentBean actual = parent().putToChildBeanMap("key", child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanMap", aMapWithSize(1)));
    }

    // -----------------------------------------------------------------------
    // Set and array fields
    // -----------------------------------------------------------------------

    /** A Set field is a Collection, so hasSize applies exactly as it does to a List. */
    @Test
    public void matchesSetSizeWithHasSize() {
        SetHolder expected = new SetHolder("kiwi", "melon");
        SetHolder actual = new SetHolder("apple", "banana");

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanSet", hasSize(2)));
    }

    /**
     * containsInAnyOrder is the right matcher for a Set: iteration order is not part of the
     * contract, so contains() would be testing an implementation detail.
     */
    @Test
    public void matchesSetContentIgnoringOrderWithContainsInAnyOrder() {
        SetHolder expected = new SetHolder("kiwi", "melon");
        SetHolder actual = new SetHolder("apple", "banana");

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanSet",
                containsInAnyOrder(childStringEqualTo("banana"), childStringEqualTo("apple"))));
    }

    /**
     * An array is not a Collection, so array-specific matchers are required. hasSize on an array
     * field fails — see BeanMatcherCustomFailureTest#failsWhenHasSizeIsAppliedToAnArrayField.
     */
    @Test
    public void matchesArraySizeWithArrayWithSize() {
        ArrayHolder expected = new ArrayHolder("kiwi", "melon");
        ArrayHolder actual = new ArrayHolder("apple", "banana");

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanArray", arrayWithSize(2)));
    }

    /** arrayContaining is the array counterpart of contains — exact elements, exact order. */
    @Test
    public void matchesArrayInOrderWithArrayContaining() {
        ArrayHolder expected = new ArrayHolder("kiwi", "melon");
        ArrayHolder actual = new ArrayHolder("apple", "banana");

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanArray",
                arrayContaining(childStringEqualTo("apple"), childStringEqualTo("banana"))));
    }

    /**
     * Fan-out works through an array as well as through a Collection. BeanFinder only fans out
     * through Collection, so the bean lookup for "childBeanArray.childString" fails and the
     * matcher is retried against the serialised JSON, where the array is a JsonArray and does
     * fan out. The observable behaviour is the same as for a List.
     */
    @Test
    public void matchesPathThroughArrayFansOutOverElements() {
        ArrayHolder expected = new ArrayHolder("kiwi", "melon");
        ArrayHolder actual = new ArrayHolder("apple", "banana");

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanArray.childString",
                anyOf(equalTo("apple"), equalTo("banana"))));
        // The negative is what evidences fan-out: pinning one value must fail, because the matcher is
        // applied to every element rather than to whichever one happens to be found first.
        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("childBeanArray.childString", equalTo("apple")),
                AssertionError.class, error -> org.junit.jupiter.api.Assertions.assertTrue(
                        error.getMessage().contains("childBeanArray.childString"),
                        "Expected the path in the failure message, was: " + error.getMessage()));
    }

    // -----------------------------------------------------------------------
    // Container matcher combined with ignoring() on a sibling collection
    // -----------------------------------------------------------------------

    /**
     * A container matcher on one collection and ignoring() on a sibling collection are
     * independent: childBeanList is asserted by hasSize while childBeanMap is dropped from the
     * structural comparison, so its differing content does not fail the assertion.
     */
    @Test
    public void matchesCollectionWhileIgnoringSiblingCollection() {
        ParentBean expected = parent()
                .addToChildBeanList(child().childString("kiwi"))
                .putToChildBeanMap("key", child().childString("kiwi"))
                .build();
        ParentBean actual = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .putToChildBeanMap("key", child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .with("childBeanList", hasSize(2))
                .ignoring("childBeanMap"));
    }

    /** Fan-out through one collection while a sibling collection is ignored. */
    @Test
    public void matchesPathThroughCollectionWhileIgnoringSiblingCollection() {
        ParentBean expected = parent()
                .addToChildBeanList(child().childString("apple"))
                .putToChildBeanMap("key", child().childString("kiwi"))
                .build();
        ParentBean actual = parent()
                .addToChildBeanList(child().childString("apple"))
                .putToChildBeanMap("key", child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .with("childBeanList.childString", equalTo("apple"))
                .ignoring("childBeanMap"));
    }


    // -----------------------------------------------------------------------
    // Negated container matchers
    // -----------------------------------------------------------------------

    /** not(empty()) passes on a non-empty collection — and fails on an empty one. */
    @Test
    public void matchesNegatedEmptyOnNonEmptyCollection() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", not(empty())));
    }

    /** not(hasItem(x)) passes when x is absent. */
    @Test
    public void matchesNegatedHasItemWhenElementIsAbsent() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("childBeanList", not(hasItem(childStringEqualTo("kiwi")))));
    }

    /** not(hasSize(n)) passes when the size differs from n. */
    @Test
    public void matchesNegatedHasSizeWhenSizeDiffers() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", not(hasSize(3))));
    }

    /** not(hasEntry(...)) passes when the map holds no such entry. */
    @Test
    public void matchesNegatedHasEntryWhenEntryIsAbsent() {
        ParentBean expected = parent().putToChildBeanMap("key", child().childString("kiwi")).build();
        ParentBean actual = parent().putToChildBeanMap("key", child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanMap",
                not(hasEntry(equalTo("key"), childStringEqualTo("kiwi")))));
    }

    /**
     * A map is traversed by key: the key is a path segment. Until the MARKER-prefixed key fix this
     * could not resolve at all, because childBeanMap is Map-typed and so carries a prefixed JSON key,
     * and the bean walker has no Map branch so the path falls through to the JSON tree.
     */
    @Test
    public void matchesPropertyOfMapEntryAddressedByKey() {
        ParentBean expected = parent().putToChildBeanMap("key", child().childString("kiwi")).build();
        ParentBean actual = parent().putToChildBeanMap("key", child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("childBeanMap.key.childString", equalTo("banana")));
    }

    // -----------------------------------------------------------------------
    // withMatcher agrees with with() on a collection-valued field
    // -----------------------------------------------------------------------

    /** Holds a collection of scalars, reached by field name rather than by path. */
    static class TagBean {
        List<String> tags;

        TagBean(List<String> tags) {
            this.tags = tags;
        }
    }

    /**
     * A Collection matcher must behave the same whether the field is named by a path or by a name
     * pattern. Both routes resolve against the serialised JSON for a field the object walk cannot
     * reach, so both need the value presented as a Collection; only the path route did.
     */
    @Test
    public void withMatcherAgreesWithWithOnACollectionValuedField() {
        TagBean bean = new TagBean(Arrays.asList("a", "b"));

        assertDiagnosingMatcher(bean, bean, beanMatcher -> beanMatcher.with("tags", hasSize(2)));
        assertDiagnosingMatcher(bean, bean,
                beanMatcher -> beanMatcher.withMatcher(equalTo("tags"), hasSize(2)));
    }

    /** And they agree when the matcher should fail, so neither passes vacuously. */
    @Test
    public void withMatcherAndWithBothFailOnAWrongSize() {
        TagBean bean = new TagBean(Arrays.asList("a", "b"));

        assertDiagnosingErrorMatcherOfType(bean, beanMatcher -> beanMatcher.with("tags", hasSize(3)));
        assertDiagnosingErrorMatcherOfType(bean,
                beanMatcher -> beanMatcher.withMatcher(equalTo("tags"), hasSize(3)));
    }

    private void assertDiagnosingErrorMatcherOfType(Object bean,
            java.util.function.Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator) {
        assertDiagnosingMatcher(bean, bean, configurator, AssertionError.class,
                error -> org.junit.jupiter.api.Assertions.assertTrue(
                        error.getMessage().contains("tags"),
                        "Expected the field in the failure message, was: " + error.getMessage()));
    }

    // -----------------------------------------------------------------------
    // The * wildcard segment
    // -----------------------------------------------------------------------

    /** A non-final * fans out over every map value, so no key has to be named. */
    @Test
    public void wildcardMatchesPropertyOfEveryMapValue() {
        ParentBean expected = parent()
                .putToChildBeanMap("k1", child().childString("kiwi"))
                .putToChildBeanMap("k2", child().childString("kiwi")).build();
        ParentBean actual = parent()
                .putToChildBeanMap("k1", child().childString("banana"))
                .putToChildBeanMap("k2", child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("childBeanMap.*.childString", equalTo("banana")));
    }

    /**
     * The wildcard resolves against the object, so an int field arrives as an Integer and an int
     * literal matches it. Naming the key instead cannot resolve against the object — there is no field
     * called k1 on a Map — so it falls through to the serialised JSON, where the field is a Long and
     * only the Long literal matches.
     * <p>
     * The Long literal also passes through the wildcard, but for a different reason: Integer does not
     * equal Long, so the object-level attempt fails and the JSON retry supplies the Long. So the
     * wildcard accepts either literal and the named form only the Long — wider, never narrower.
     */
    @Test
    public void wildcardKeepsTheRealTypeOfAMapValuesField() {
        ParentBean bean = parent()
                .putToChildBeanMap("k1", child().childString("banana").childInteger(0)).build();

        assertDiagnosingMatcher(bean, bean,
                beanMatcher -> beanMatcher.with("childBeanMap.*.childInteger", equalTo(0)));
        assertDiagnosingMatcher(bean, bean,
                beanMatcher -> beanMatcher.with("childBeanMap.*.childInteger", equalTo(0L)));
        // The named form: only the Long literal, since it resolves through the JSON form.
        assertDiagnosingMatcher(bean, bean,
                beanMatcher -> beanMatcher.with("childBeanMap.k1.childInteger", equalTo(0L)));
        assertDiagnosingMatcher(bean, bean,
                beanMatcher -> beanMatcher.with("childBeanMap.k1.childInteger", equalTo(0)),
                AssertionError.class, error -> org.junit.jupiter.api.Assertions.assertTrue(
                        error.getMessage().contains("childBeanMap.k1.childInteger"),
                        "Expected the path in the failure message, was: " + error.getMessage()));
    }

    /**
     * A wildcard aimed at the map leaves a sibling collection alone. The sibling's childString is a
     * value the matcher would reject, so a wildcard that leaked into the list would fail rather than
     * pass unnoticed.
     */
    @Test
    public void wildcardOnAMapDoesNotTouchASiblingCollection() {
        ParentBean expected = parent()
                .putToChildBeanMap("k1", child().childString("kiwi"))
                .addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent()
                .putToChildBeanMap("k1", child().childString("banana"))
                .addToChildBeanList(child().childString("kiwi")).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("childBeanMap.*.childString", equalTo("banana")));
    }

    /** A wildcard at the root fans out over every field of the bean. */
    @Test
    public void wildcardAtTheRootFansOutOverEveryField() {
        ParentBean expected = parent()
                .putToChildBeanMap("k1", child().childString("kiwi")).build();
        ParentBean actual = parent()
                .putToChildBeanMap("k1", child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("*.k1.childString", equalTo("banana")));
    }

    // -----------------------------------------------------------------------
    // A * stays within the fields the serialised form has
    // -----------------------------------------------------------------------

    /** A field the serialiser omits, alongside one it keeps. */
    static class WithHiddenFields {
        static ChildBean sharedStatic = child().childString("static-leak").build();
        transient ChildBean cached = child().childString("transient-leak").build();
        ChildBean real;

        WithHiddenFields(String childString) {
            real = child().childString(childString).build();
        }
    }

    /**
     * A guard rather than a regression test: on this shape a leak into the static and transient fields
     * is masked, because the bean-level failure is retried against the serialised JSON, which omits
     * them. It pins the intended field set so a future change is visible. The enum test below is the
     * discriminating one, since an enum serialises to a bare string and so has no retry to hide behind.
     */
    @Test
    public void wildcardDoesNotWalkStaticOrTransientFields() {
        WithHiddenFields expected = new WithHiddenFields("kiwi");
        WithHiddenFields actual = new WithHiddenFields("banana");

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("*.childString", equalTo("banana")));
    }

    enum Fruit { APPLE, BANANA }

    /** Holds one enum constant; the others are static fields of the same class. */
    static class FruitHolder {
        Fruit fruit;

        FruitHolder(Fruit fruit) {
            this.fruit = fruit;
        }
    }

    /**
     * An enum makes the static-field leak observable: its constants are static fields of its own class,
     * so a wildcard that walked them would read every other constant off the one actually held. There
     * is no retry to hide it, because an enum serialises to a bare string. With the fields filtered
     * nothing under the enum resolves, so the path is rejected — the honest answer.
     */
    @Test
    public void wildcardDoesNotWalkAnEnumsOtherConstants() {
        FruitHolder holder = new FruitHolder(Fruit.APPLE);

        assertDiagnosingMatcher(holder, holder,
                beanMatcher -> beanMatcher.with("fruit.*.name", equalTo("APPLE")),
                IllegalArgumentException.class, error -> org.junit.jupiter.api.Assertions.assertEquals(
                        "fruit.*.name does not exist", error.getMessage()));
    }

    /** A non-static inner class, whose synthetic reference points back at the enclosing instance. */
    class Enclosed {
        ChildBean real = child().childString("banana").build();
    }

    /**
     * The one that fails silently rather than loudly. The synthetic reference an inner class holds to
     * its enclosing instance is a named child as far as reflection is concerned, so a wildcard could
     * resolve through it and assert over the *enclosing* object. The path below names a field that
     * exists only on this test class, never on the object under comparison: if the synthetic reference
     * were followed it would resolve and the assertion would pass, having checked nothing about the
     * object at all. Filtered, nothing resolves, so the path is rejected — which is the correct answer.
     */
    @Test
    public void wildcardDoesNotFollowTheSyntheticEnclosingReference() {
        Enclosed actual = new Enclosed();

        assertDiagnosingMatcher(actual, actual,
                beanMatcher -> beanMatcher.with("*.leakTarget.childString", equalTo("leak-target")),
                IllegalArgumentException.class, error -> org.junit.jupiter.api.Assertions.assertEquals(
                        "*.leakTarget.childString does not exist", error.getMessage()));
    }

    /** Exists only on the enclosing test class, so it is the bait for the test above. */
    private final ChildBean leakTarget = child().childString("leak-target").build();

    /**
     * A sibling that resolves to nothing must not make the wildcard unsatisfiable. An empty fan-out is
     * a deliberate failure when the path names the empty collection, but a wildcard's children are
     * selected by pattern, so a sibling yielding nothing is an irrelevance.
     */
    @Test
    public void wildcardIsNotDefeatedByAnEmptySiblingCollection() {
        ParentBean expected = parent().putToChildBeanMap("k1", child().childString("kiwi")).build();
        ParentBean actual = parent().putToChildBeanMap("k1", child().childString("banana")).build();

        // childBeanList is an empty list on both sides; childBean and parentString are null.
        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("*.k1.childString", equalTo("banana")));
    }

    /** Holds a single Set of ChildBean so container matchers can be exercised on a Set field. */
    static class SetHolder {
        Set<ChildBean> childBeanSet;

        SetHolder(String first, String second) {
            childBeanSet = new LinkedHashSet<>(Arrays.asList(
                    child().childString(first).build(),
                    child().childString(second).build()));
        }
    }

    /** Holds a single array of ChildBean so array matchers can be exercised on an array field. */
    static class ArrayHolder {
        ChildBean[] childBeanArray;

        ArrayHolder(String first, String second) {
            childBeanArray = new ChildBean[]{
                    child().childString(first).build(),
                    child().childString(second).build()};
        }
    }
}
