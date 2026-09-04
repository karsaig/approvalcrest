package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ChildBean;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

/**
 * {@code alsoCheck} and {@code alsoCheckMatching} on {@code sameBeanAs}.
 *
 * <p>Where {@code with(...)} takes the field out of the comparison so only the matcher speaks for it, these leave
 * it in. Every case therefore has to be read twice: the matcher must hold <em>and</em> the field must still be
 * compared against {@code expected}. A case that only asserts the matcher would pass just as well under
 * {@code with}, which is what the {@code …IsStillComparedUnlike…} tests exist to rule out.
 *
 * <p>Fixtures carry three elements per level in an order that is neither sorted nor reversed, so a sort and a
 * reversal cannot be mistaken for each other.
 */
public class BeanMatcherAlsoCheckTest extends AbstractBeanMatcherTest {

    private static ParentBean.Builder threeChildren(String first, String second, String third) {
        return parent()
                .addToChildBeanList(child().childString(first).childInteger(3))
                .addToChildBeanList(child().childString(second).childInteger(1))
                .addToChildBeanList(child().childString(third).childInteger(2));
    }

    // ---------------------------------------------------------------- wiring

    @Test
    public void passesWhenTheFieldMatchesExpectedAndTheMatcherHolds() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(7)).build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBean.childInteger", greaterThan(0)));
    }

    /**
     * The discriminating case. {@code with} would delete childInteger and pass on a value that differs from
     * expected; {@code alsoCheck} keeps it and the comparison fails.
     */
    @Test
    public void failsWhenTheFieldDiffersFromExpectedEvenThoughTheMatcherHolds() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(9)).build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0)),
                AssertionError.class, expectMismatchOn("childBean.childInteger", "7", "9"));
    }

    @Test
    public void theSameConfigurationWithWithPassesBecauseTheFieldIsRemoved() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(9)).build();

        assertDiagnosingMatcher(actual, expected, m -> m.with("childBean.childInteger", greaterThan(0)));
    }

    @Test
    public void failsWhenTheMatcherDoesNotHoldEvenThoughTheFieldMatchesExpected() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(7)).build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(100)),
                AssertionError.class,
                err -> org.junit.jupiter.api.Assertions.assertTrue(
                        err.getMessage().contains("childBean.childInteger <7> was less than <100>"),
                        err.getMessage()));
    }

    @Test
    public void lastRegistrationWinsWhenWithFollowsAlsoCheck() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(9)).build();

        // with(...) last, so the field is removed and the differing value is tolerated
        assertDiagnosingMatcher(actual, expected, m -> m
                .alsoCheck("childBean.childInteger", greaterThan(0))
                .with("childBean.childInteger", greaterThan(0)));
    }

    @Test
    public void lastRegistrationWinsWhenAlsoCheckFollowsWith() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(9)).build();

        // alsoCheck(...) last, so the field comes back into the comparison and the difference is caught
        assertDiagnosingMatcher(actual, expected, m -> m
                        .with("childBean.childInteger", greaterThan(0))
                        .alsoCheck("childBean.childInteger", greaterThan(0)),
                AssertionError.class, expectMismatchOn("childBean.childInteger", "7", "9"));
    }

    @Test
    public void explicitIgnoringStillRemovesTheFieldWhenAlsoCheckIsRegisteredAfterIt() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(9)).build();

        assertDiagnosingMatcher(actual, expected, m -> m
                .ignoring("childBean.childInteger")
                .alsoCheck("childBean.childInteger", greaterThan(0)));
    }

    @Test
    public void explicitIgnoringStillRemovesTheFieldWhenAlsoCheckIsRegisteredBeforeIt() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(9)).build();

        assertDiagnosingMatcher(actual, expected, m -> m
                .alsoCheck("childBean.childInteger", greaterThan(0))
                .ignoring("childBean.childInteger"));
    }

    @Test
    public void throwsIllegalArgumentExceptionWhenFieldPathDoesNotExist() {
        ParentBean expected = parent().childBean(child().childString("L1-c")).build();
        ParentBean actual = parent().childBean(child().childString("L1-c")).build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("noSuchField", notNullValue()),
                IllegalArgumentException.class,
                e -> org.junit.jupiter.api.Assertions.assertEquals("noSuchField does not exist", e.getMessage()));
    }

    // ------------------------------------------------- primitives and strings

    @Test
    public void matchesPrimitiveWithAlsoCheck() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();
        ParentBean actual = parent().childBean(child().childString("L1-c").childInteger(7)).build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBean.childInteger", equalTo(7)));
    }

    @Test
    public void matchesStringFieldWithContainsString() {
        ParentBean expected = parent().parentString("L1-cherry").build();
        ParentBean actual = parent().parentString("L1-cherry").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("parentString", containsString("cherry")));
    }

    @Test
    public void matchesStringFieldWithStartsWith() {
        ParentBean expected = parent().parentString("L1-cherry").build();
        ParentBean actual = parent().parentString("L1-cherry").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("parentString", startsWith("L1-")));
    }

    @Test
    public void matchesStringFieldWithNegatedMatcher() {
        ParentBean expected = parent().parentString("L1-cherry").build();
        ParentBean actual = parent().parentString("L1-cherry").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("parentString", not(containsString("apple"))));
    }

    @Test
    public void matchesNullFieldWithNullValueMatcher() {
        ParentBean expected = parent().build();
        ParentBean actual = parent().build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("parentString", nullValue()));
    }

    @Test
    public void failsWhenNullFieldMatchedWithNotNullMatcher() {
        ParentBean expected = parent().build();
        ParentBean actual = parent().build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("parentString", notNullValue()),
                AssertionError.class, expectMessageContaining("parentString"));
    }

    @Test
    public void matchesWithThreeIndependentAlsoChecks() {
        ParentBean expected = parent().parentString("L1-cherry")
                .childBean(child().childString("L2-apple").childInteger(7)).build();
        ParentBean actual = parent().parentString("L1-cherry")
                .childBean(child().childString("L2-apple").childInteger(7)).build();

        assertDiagnosingMatcher(actual, expected, m -> m
                .alsoCheck("parentString", startsWith("L1-"))
                .alsoCheck("childBean.childString", startsWith("L2-"))
                .alsoCheck("childBean.childInteger", greaterThan(0)));
    }

    // ------------------------------------------------------------ collections

    @Test
    public void matchesCollectionSizeWithHasSize() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "L1-b").build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "L1-b").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanList", hasSize(3)));
    }

    @Test
    public void matchesCollectionSizeWithIterableWithSize() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "L1-b").build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "L1-b").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanList", iterableWithSize(3)));
    }

    /**
     * A size matcher that holds cannot rescue a collection whose contents differ, which is the whole point of the
     * additional mode: {@code with("childBeanList", hasSize(3))} would delete the list and pass.
     */
    @Test
    public void collectionContentIsStillComparedUnlikeUnderWith() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "L1-b").build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "L1-zzz").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanList", hasSize(3)),
                AssertionError.class, expectMismatchOn("childBeanList[2].childString", "L1-b", "L1-zzz"));
        assertDiagnosingMatcher(actual, expected, m -> m.with("childBeanList", hasSize(3)));
    }

    @Test
    public void matchesScalarCollectionInOrderWithContains() {
        ScalarListHolder expected = new ScalarListHolder();
        ScalarListHolder actual = new ScalarListHolder();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("values", contains("L1-c", "L1-a", "L1-b")));
    }

    @Test
    public void matchesScalarCollectionIgnoringOrderWithContainsInAnyOrder() {
        ScalarListHolder expected = new ScalarListHolder();
        ScalarListHolder actual = new ScalarListHolder();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("values", containsInAnyOrder("L1-a", "L1-b", "L1-c")));
    }

    @Test
    public void matchesScalarCollectionMemberWithHasItem() {
        ScalarListHolder expected = new ScalarListHolder();
        ScalarListHolder actual = new ScalarListHolder();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", hasItem("L1-b")));
    }

    @Test
    public void matchesNegatedEmptyOnNonEmptyCollection() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "L1-b").build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "L1-b").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanList", not(empty())));
    }

    @Test
    public void failsWhenCollectionSizeDiffersFromHasSize() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "L1-b").build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "L1-b").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanList", hasSize(2)),
                AssertionError.class, expectMessageContaining("childBeanList", "collection with size <2>"));
    }

    /** Fan-out: the matcher applies to the leaf in every element, and the list is still compared. */
    @Test
    public void matchesPathThroughCollectionFanningOutOverElements() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "L1-b").build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "L1-b").build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanList.childString", startsWith("L1-")));
    }

    @Test
    public void failsWhenOneElementInTheFanOutDoesNotMatch() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "other").build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "other").build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("childBeanList.childString", startsWith("L1-")),
                AssertionError.class, expectMessageContaining("childBeanList.childString", "other"));
    }

    /** A matcher on one collection must leave its sibling alone. */
    @Test
    public void doesNotDisturbASiblingCollection() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "L1-b")
                .putToChildBeanMap("k-c", child().childString("L2-c")).build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "L1-b")
                .putToChildBeanMap("k-c", child().childString("L2-zzz")).build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanList", hasSize(3)),
                AssertionError.class, expectMessageContaining("childBeanMap"));
    }

    // ------------------------------------------------------------------ maps

    @Test
    public void matchesMapSizeWithAMapWithSize() {
        MapHolder expected = new MapHolder();
        MapHolder actual = new MapHolder();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", aMapWithSize(3)));
    }

    @Test
    public void matchesMapKeyWithHasKey() {
        MapHolder expected = new MapHolder();
        MapHolder actual = new MapHolder();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", hasKey("k-a")));
    }

    @Test
    public void matchesMapEntryWithHasEntry() {
        MapHolder expected = new MapHolder();
        MapHolder actual = new MapHolder();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", hasEntry("k-a", "L1-a")));
    }

    @Test
    public void failsWhenTheMatcherOnAMapTypedFieldDoesNotHold() {
        MapHolder expected = new MapHolder();
        MapHolder actual = new MapHolder();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", aMapWithSize(2)),
                AssertionError.class, expectMessageContaining("values", "map with size <2>"));
    }

    @Test
    public void matchesPropertyOfMapEntryAddressedByKey() {
        ParentBean expected = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-a"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();
        ParentBean actual = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-a"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanMap.k-a.childString", equalTo("L2-a")));
    }

    @Test
    public void wildcardMatchesPropertyOfEveryMapValue() {
        ParentBean expected = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-a"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();
        ParentBean actual = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-a"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanMap.*.childString", startsWith("L2-")));
    }

    // ------------------------------------------------------------------- set

    /**
     * A {@code Set} field is held under a marker-prefixed name and auto-sorted. Under {@code alsoCheck} it stays
     * in the comparison, so this is the shape most likely to expose a wiring mistake.
     */
    @Test
    public void matchesSetSizeWithHasSize() {
        SetHolder expected = new SetHolder("L1-c", "L1-a", "L1-b");
        SetHolder actual = new SetHolder("L1-c", "L1-a", "L1-b");

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childStringSet", hasSize(3)));
    }

    @Test
    public void matchesSetContentIgnoringOrderWithContainsInAnyOrder() {
        SetHolder expected = new SetHolder("L1-c", "L1-a", "L1-b");
        SetHolder actual = new SetHolder("L1-c", "L1-a", "L1-b");

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("childStringSet", containsInAnyOrder("L1-a", "L1-b", "L1-c")));
    }

    @Test
    public void setContentIsStillComparedUnlikeUnderWith() {
        SetHolder expected = new SetHolder("L1-c", "L1-a", "L1-b");
        SetHolder actual = new SetHolder("L1-c", "L1-a", "L1-zzz");

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childStringSet", hasSize(3)),
                AssertionError.class, expectMessageContaining("L1-zzz"));
        assertDiagnosingMatcher(actual, expected, m -> m.with("childStringSet", hasSize(3)));
    }

    @Test
    public void failsWhenTheMatcherOnASetTypedFieldDoesNotHold() {
        SetHolder expected = new SetHolder("L1-c", "L1-a", "L1-b");
        SetHolder actual = new SetHolder("L1-c", "L1-a", "L1-b");

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childStringSet", hasSize(2)),
                AssertionError.class, expectMessageContaining("childStringSet", "collection with size <2>"));
    }

    // ----------------------------------------------------------------- array

    @Test
    public void matchesArraySizeWithArrayWithSize() {
        ArrayThreeHolder expected = new ArrayThreeHolder("L1-c", "L1-a", "L1-b");
        ArrayThreeHolder actual = new ArrayThreeHolder("L1-c", "L1-a", "L1-b");

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", arrayWithSize(3)));
    }

    @Test
    public void matchesArrayInOrderWithArrayContaining() {
        ArrayThreeHolder expected = new ArrayThreeHolder("L1-c", "L1-a", "L1-b");
        ArrayThreeHolder actual = new ArrayThreeHolder("L1-c", "L1-a", "L1-b");

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("values", arrayContaining("L1-c", "L1-a", "L1-b")));
    }

    @Test
    public void arrayContentIsStillComparedUnlikeUnderWith() {
        ArrayThreeHolder expected = new ArrayThreeHolder("L1-c", "L1-a", "L1-b");
        ArrayThreeHolder actual = new ArrayThreeHolder("L1-c", "L1-a", "L1-zzz");

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", arrayWithSize(3)),
                AssertionError.class, expectMessageContaining("L1-zzz"));
        assertDiagnosingMatcher(actual, expected, m -> m.with("values", arrayWithSize(3)));
    }

    // ------------------------------------------------------- deeply nested

    @Test
    public void matchesDeeplyNestedFieldPath() {
        Level1 expected = new Level1();
        Level1 actual = new Level1();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("level2c.level3a.values", contains("L4-c", "L4-a", "L4-b")));
    }

    @Test
    public void deeplyNestedFieldIsStillComparedUnlikeUnderWith() {
        Level1 expected = new Level1();
        Level1 actual = new Level1();
        actual.level2c.level3a.values.set(2, "L4-zzz");

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("level2c.level3a.values", hasSize(3)),
                AssertionError.class, expectMessageContaining("L4-zzz"));
        assertDiagnosingMatcher(actual, expected, m -> m.with("level2c.level3a.values", hasSize(3)));
    }

    // --------------------------------------------------------- pattern form

    @Test
    public void alsoCheckMatchingMatchesEveryFieldWhoseNameMatches() {
        ParentBean expected = parent().parentString("L1-cherry")
                .childBean(child().childString("L2-apple")).build();
        ParentBean actual = parent().parentString("L1-cherry")
                .childBean(child().childString("L2-apple")).build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheckMatching(containsString("String"), containsString("-")));
    }

    @Test
    public void alsoCheckMatchingKeepsTheMatchedFieldsInTheComparison() {
        ParentBean expected = parent().childBean(child().childString("L2-apple")).build();
        ParentBean actual = parent().childBean(child().childString("L2-banana")).build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheckMatching(containsString("childString"), containsString("L2-")),
                AssertionError.class, expectMismatchOn("childBean.childString", "L2-apple", "L2-banana"));
        assertDiagnosingMatcher(actual, expected,
                m -> m.withMatcher(containsString("childString"), containsString("L2-")));
    }

    @Test
    public void alsoCheckMatchingPassesVacuouslyWhenNoFieldsMatch() {
        ParentBean expected = parent().parentString("L1-cherry").build();
        ParentBean actual = parent().parentString("L1-cherry").build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheckMatching(containsString("noSuchName"), containsString("whatever")));
    }

    @Test
    public void alsoCheckMatchingFailsWhenAMatchedFieldValueDoesNotMatch() {
        ParentBean expected = parent().parentString("L1-cherry").build();
        ParentBean actual = parent().parentString("L1-cherry").build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheckMatching(containsString("parentString"), containsString("apple")),
                AssertionError.class, expectMessageContaining("L1-cherry"));
    }

    /**
     * The documented asymmetry with the path form: patterns accumulate in a list and two matcher instances never
     * compare equal, so {@code alsoCheckMatching} cannot take back a {@code withMatcher} registration.
     */
    @Test
    public void alsoCheckMatchingCannotUndoAnEarlierWithMatcher() {
        ParentBean expected = parent().childBean(child().childString("L2-apple")).build();
        ParentBean actual = parent().childBean(child().childString("L2-banana")).build();

        assertDiagnosingMatcher(actual, expected, m -> m
                .withMatcher(containsString("childString"), containsString("L2-"))
                .alsoCheckMatching(containsString("childString"), containsString("L2-")));
    }

    // ---------------------------------- drift: the matcher holds, the value moved

    /**
     * Each of these pairs a matcher that passes with data that differs from expected. Under {@code with} the
     * field is gone and the difference is invisible; under {@code alsoCheck} it has to fail. Without them a
     * shape is only tested for "the matcher runs", which {@code with} satisfies too.
     */
    @Test
    public void mapContentIsStillComparedUnlikeUnderWith() {
        MapHolder expected = new MapHolder();
        MapHolder actual = new MapHolder();
        actual.values.put("k-a", "L1-zzz");

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", aMapWithSize(3)),
                AssertionError.class, expectMessageContaining("L1-zzz"));
        assertDiagnosingMatcher(actual, expected, m -> m.with("values", aMapWithSize(3)));
    }

    @Test
    public void arrayContentDriftIsStillCompared() {
        ArrayThreeHolder expected = new ArrayThreeHolder("L1-c", "L1-a", "L1-b");
        ArrayThreeHolder actual = new ArrayThreeHolder("L1-c", "L1-a", "L1-zzz");

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("values", arrayContaining("L1-c", "L1-a", "L1-zzz")),
                AssertionError.class, expectMessageContaining("L1-zzz"));
        assertDiagnosingMatcher(actual, expected, m -> m.with("values", arrayContaining("L1-c", "L1-a", "L1-zzz")));
    }

    @Test
    public void fanOutContentIsStillComparedUnlikeUnderWith() {
        ParentBean expected = threeChildren("L1-c", "L1-a", "L1-b").build();
        ParentBean actual = threeChildren("L1-c", "L1-a", "L1-b")
                .parentString("drifted").build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("childBeanList.childString", startsWith("L1-")),
                AssertionError.class, expectMessageContaining("drifted"));
    }

    @Test
    public void aWildcardPathLeavesTheMapValuesComparedUnlikeUnderWith() {
        ParentBean expected = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-a"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();
        ParentBean actual = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-zzz"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();

        assertDiagnosingMatcher(actual, expected, m -> m.alsoCheck("childBeanMap.*.childString", startsWith("L2-")),
                AssertionError.class, expectMessageContaining("L2-zzz"));
        assertDiagnosingMatcher(actual, expected, m -> m.with("childBeanMap.*.childString", startsWith("L2-")));
    }

    @Test
    public void aMapKeyPathLeavesTheEntryComparedUnlikeUnderWith() {
        ParentBean expected = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-a"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();
        ParentBean actual = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-zzz"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheck("childBeanMap.k-a.childString", startsWith("L2-")),
                AssertionError.class, expectMessageContaining("L2-zzz"));
        assertDiagnosingMatcher(actual, expected,
                m -> m.with("childBeanMap.k-a.childString", startsWith("L2-")));
    }

    // ------------------------- the pattern form over marker-prefixed fields

    /**
     * A Set- or Map-typed field is held under an internal name, which is exactly where a name pattern used to
     * silently reach nothing -- and a pattern that matches no field passes whatever the data holds. These pin
     * that {@code alsoCheckMatching} reaches such a field *and* leaves it compared.
     */
    @Test
    public void alsoCheckMatchingReachesASetTypedFieldAndLeavesItCompared() {
        SetHolder expected = new SetHolder("L1-c", "L1-a", "L1-b");
        SetHolder actual = new SetHolder("L1-c", "L1-a", "L1-zzz");

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheckMatching(containsString("childStringSet"), iterableWithSize(3)),
                AssertionError.class, expectMessageContaining("L1-zzz"));
        assertDiagnosingMatcher(actual, expected,
                m -> m.withMatcher(containsString("childStringSet"), iterableWithSize(3)));
    }

    @Test
    public void alsoCheckMatchingFailsWhenTheMatcherDoesNotHoldOnASetTypedField() {
        SetHolder expected = new SetHolder("L1-c", "L1-a", "L1-b");
        SetHolder actual = new SetHolder("L1-c", "L1-a", "L1-b");

        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheckMatching(containsString("childStringSet"), iterableWithSize(2)),
                AssertionError.class, expectMessageContaining("childStringSet"));
    }

    @Test
    public void alsoCheckMatchingReachesAMapTypedFieldAndLeavesItCompared() {
        MapHolder expected = new MapHolder();
        MapHolder actual = new MapHolder();
        actual.values.put("k-a", "L1-zzz");

        // the pattern form reads the serialised tree, where a map is an array of single-key objects, so the
        // matcher has to be written against that shape -- aMapWithSize would not apply on either mode
        assertDiagnosingMatcher(actual, expected,
                m -> m.alsoCheckMatching(containsString("values"), iterableWithSize(3)),
                AssertionError.class, expectMessageContaining("L1-zzz"));
        assertDiagnosingMatcher(actual, expected,
                m -> m.withMatcher(containsString("values"), iterableWithSize(3)));
    }

    // ------------------------------------------------------------- describeTo

    /**
     * The description has to distinguish the two modes. A replaced field is absent from the expected content, so
     * its clause is the only thing said about it; an additional field is present in that content, so the same
     * clause would be ambiguous and reads "and also" instead.
     */
    @Test
    public void describeToDistinguishesTheTwoModes() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();

        org.hamcrest.StringDescription description = new org.hamcrest.StringDescription();
        MATCHER_FACTORY.beanMatcher(expected)
                .with("parentString", nullValue())
                .alsoCheck("childBean.childInteger", greaterThan(0))
                .describeTo(description);
        String text = description.toString();

        org.junit.jupiter.api.Assertions.assertTrue(text.contains("\nand parentString null"), text);
        org.junit.jupiter.api.Assertions.assertTrue(
                text.contains("\nand also childBean.childInteger a value greater than <0>"), text);
        // the replaced field is gone from the content, the additional one is still in it
        org.junit.jupiter.api.Assertions.assertFalse(text.contains("\"parentString\""), text);
        org.junit.jupiter.api.Assertions.assertTrue(text.contains("\"childInteger\": 7"), text);
    }

    /** Asserts the message names the path and shows both sides, the way the sibling suites do. */
    private static java.util.function.Consumer<AssertionError> expectMismatchOn(String path, String expected, String got) {
        return err -> {
            String text = err.getMessage();
            org.junit.jupiter.api.Assertions.assertTrue(text.contains(path), text);
            org.junit.jupiter.api.Assertions.assertTrue(text.contains(expected), text);
            org.junit.jupiter.api.Assertions.assertTrue(text.contains(got), text);
        };
    }

    private static java.util.function.Consumer<AssertionError> expectMessageContaining(String... fragments) {
        return err -> {
            String text = err.getMessage();
            for (String fragment : fragments) {
                org.junit.jupiter.api.Assertions.assertTrue(text.contains(fragment), fragment + " not in: " + text);
            }
        };
    }

    /**
     * The three cases the wording cannot see, pinned so they cannot drift unnoticed in either direction. The
     * branch reads the registration, not the rendered content, so a field removed for a reason that does not
     * name the path still reads "and also". This affects the description only, never a verdict —
     * {@link com.github.karsaig.approvalcrest.matcher.AbstractDiagnosingMatcher} documents it.
     */
    @Test
    public void describeToStillSaysAndAlsoForAFieldRemovedWithItsParent() {
        ParentBean expected = parent().childBean(child().childString("L1-c").childInteger(7)).build();

        org.hamcrest.StringDescription description = new org.hamcrest.StringDescription();
        MATCHER_FACTORY.beanMatcher(expected)
                .alsoCheck("childBean.childInteger", greaterThan(0))
                .ignoring("childBean")
                .describeTo(description);
        String text = description.toString();

        org.junit.jupiter.api.Assertions.assertTrue(
                text.contains("\nand also childBean.childInteger"), text);
        org.junit.jupiter.api.Assertions.assertFalse(text.contains("childInteger\":"), text);
    }

    @Test
    public void describeToStillSaysAndAlsoForAFieldRemovedByType() {
        ParentBean expected = parent().parentString("L1-cherry").build();

        org.hamcrest.StringDescription description = new org.hamcrest.StringDescription();
        MATCHER_FACTORY.beanMatcher(expected)
                .alsoCheck("parentString", notNullValue())
                .ignoring(String.class)
                .describeTo(description);

        org.junit.jupiter.api.Assertions.assertTrue(
                description.toString().contains("\nand also parentString"), description.toString());
    }

    @Test
    public void describeToStillSaysAndAlsoForAFieldRemovedByNamePattern() {
        ParentBean expected = parent().parentString("L1-cherry").build();

        org.hamcrest.StringDescription description = new org.hamcrest.StringDescription();
        MATCHER_FACTORY.beanMatcher(expected)
                .alsoCheck("parentString", notNullValue())
                .ignoring(containsString("parentString"))
                .describeTo(description);

        org.junit.jupiter.api.Assertions.assertTrue(
                description.toString().contains("\nand also parentString"), description.toString());
    }

    // ------------------------------------------------------------- fixtures

    /** Three entries per level, inserted neither in sorted nor reversed order. */
    static class MapHolder {
        Map<String, String> values = new LinkedHashMap<>();

        MapHolder() {
            values.put("k-c", "L1-c");
            values.put("k-a", "L1-a");
            values.put("k-b", "L1-b");
        }
    }

    static class SetHolder {
        Set<String> childStringSet;

        SetHolder(String first, String second, String third) {
            childStringSet = new LinkedHashSet<>(Arrays.asList(first, second, third));
        }
    }

    static class ArrayThreeHolder {
        String[] values;

        ArrayThreeHolder(String first, String second, String third) {
            values = new String[]{first, second, third};
        }
    }

    /** A List of scalars, so container matchers can be applied to the field itself. */
    static class ScalarListHolder {
        List<String> values = new ArrayList<>(Arrays.asList("L1-c", "L1-a", "L1-b"));
    }

    /** Three named children at every level, so a resolver that reaches a sibling branch is caught. */
    static class Level1 {
        Level2 level2c = new Level2("L2-c");
        Level2 level2a = new Level2("L2-a");
        Level2 level2b = new Level2("L2-b");
    }

    static class Level2 {
        String tag;
        Level3 level3c = new Level3("L3-c");
        Level3 level3a = new Level3("L3-a");
        Level3 level3b = new Level3("L3-b");

        Level2(String tag) {
            this.tag = tag;
        }
    }

    static class Level3 {
        String tag;
        List<String> values = new ArrayList<>(Arrays.asList("L4-c", "L4-a", "L4-b"));

        Level3(String tag) {
            this.tag = tag;
        }
    }
}
