package com.github.karsaig.approvalcrest.matcher.types.ordering;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ChildBean;
import com.github.karsaig.approvalcrest.testdata.cyclic.One;

/**
 * The {@code sameBeanAs} side of the complex-key map pair order. It serialises both sides itself, through
 * the same Gson, so both carry the Map marker and suppression is symmetric — there is no approved file and
 * no strict-matching setting to consult.
 *
 * <p>Also covers a map at the ROOT, which has no field name to mark and is instead described from the
 * object itself -- and depth, where a rendered expectation would be larger than what it proves: a transpose
 * that no longer matches says the key and the value stayed apart.
 */
public class BeanMatcherComplexKeyMapOrderTest extends AbstractBeanMatcherTest {

    static class MapHolder {
        Map<ChildBean, ChildBean> m = new LinkedHashMap<>();

        MapHolder(String key, String value) {
            m.put(child().childString(key).build(), child().childString(value).build());
        }
    }

    static class NestedMapHolder {
        Map<ChildBean, Map<ChildBean, ChildBean>> outer = new LinkedHashMap<>();
    }

    static class FourDeepHolder {
        Map<ChildBean, Map<ChildBean, Map<ChildBean, Map<ChildBean, ChildBean>>>> outer = new LinkedHashMap<>();
    }

    static class SetOfMapOfSetOfMapHolder {
        Set<Map<ChildBean, Set<Map<ChildBean, ChildBean>>>> s = new LinkedHashSet<>();
    }

    static class MapUnderTwoCollectionLevelsHolder {
        Map<ChildBean, List<List<Map<ChildBean, ChildBean>>>> outer = new LinkedHashMap<>();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static class MapInAnArrayOfArraysHolder {
        Map<ChildBean, ChildBean>[][] grid = new Map[1][1];
    }

    private static Map<ChildBean, ChildBean> rootMap(String key, String value) {
        Map<ChildBean, ChildBean> m = new LinkedHashMap<>();
        m.put(child().childString(key).build(), child().childString(value).build());
        return m;
    }

    @Test
    public void equalMapsStillMatch() {
        assertDiagnosingMatcher(new MapHolder("zk", "av"), new MapHolder("zk", "av"));
    }

    @Test
    public void aMapNoLongerMatchesItsTranspose() {
        // Both sides are serialised here, so before the fix both were normalised to the same bytes and
        // this passed -- a map and its transpose were indistinguishable.
        assertDiagnosingMatcher(new MapHolder("zk", "av"), new MapHolder("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("[0][0].childString")
                                && error.getMessage().contains("[0][1].childString"),
                        "Expected mismatches at both pair positions, was: " + error.getMessage()));
    }

    // --- the levels a field name cannot reach -----------------------------------------------------
    // Depth is asserted here rather than against a rendered file: a transpose that no longer matches says
    // the key and the value stayed apart, without an expectation the size of the data.

    @Test
    public void aNestedMapNoLongerMatchesItsTranspose() {
        assertDiagnosingMatcher(nested("zk", "av"), nested("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("outer[0][1][0][0].childString")
                                && error.getMessage().contains("outer[0][1][0][1].childString"),
                        "Expected mismatches at both halves of the inner pair, was: " + error.getMessage()));
    }

    @Test
    public void equalNestedMapsStillMatch() {
        assertDiagnosingMatcher(nested("zk", "av"), nested("zk", "av"));
    }

    @Test
    public void aMapNestedFourDeepNoLongerMatchesItsTranspose() {
        // One level further than any other case here, so a chain that stops one short cannot pass.
        assertDiagnosingMatcher(fourDeep("zk", "av"), fourDeep("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("outer[0][1][0][1][0][1][0][0].childString"),
                        "Expected a mismatch at the innermost key, was: " + error.getMessage()));
    }

    @Test
    public void aMapNestedFourDeepStillMatchesItself() {
        assertDiagnosingMatcher(fourDeep("zk", "av"), fourDeep("zk", "av"));
    }

    @Test
    public void setOfMapOfSetOfMapNoLongerMatchesItsTranspose() {
        // Collection and map levels alternating, which a chain of map levels alone would not describe.
        assertDiagnosingMatcher(setOfMapOfSetOfMap("zk", "av"), setOfMapOfSetOfMap("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childString"),
                        "Expected a mismatch inside the innermost pair, was: " + error.getMessage()));
    }

    @Test
    public void aMapUnderTwoCollectionLevelsNoLongerMatchesItsTranspose() {
        assertDiagnosingMatcher(underTwoCollections("zk", "av"), underTwoCollections("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("outer[0][1][0][0][0][0].childString"),
                        "Expected a mismatch at the innermost key, was: " + error.getMessage()));
    }

    @Test
    public void aMapInsideAnArrayOfArraysNoLongerMatchesItsTranspose() {
        // Arrays describe a level exactly as collections do.
        assertDiagnosingMatcher(grid("zk", "av"), grid("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childString"),
                        "Expected a mismatch inside the pair, was: " + error.getMessage()));
    }

    private static MapInAnArrayOfArraysHolder grid(String key, String value) {
        MapInAnArrayOfArraysHolder holder = new MapInAnArrayOfArraysHolder();
        holder.grid[0][0] = rootMap(key, value);
        return holder;
    }

    @Test
    public void aNestedMapInACircularReferenceGraphNoLongerMatchesItsTranspose() {
        // A graph-adapter envelope wraps only beans -- the cyclic-reference detector excludes Maps and
        // Iterables -- and the walk that describes levels stops at a bean, so no level is ever pending at
        // an envelope. This is that invariant: with envelopes in the tree, the nested map is still
        // protected, and if the two ever met the envelope would have to be stepped over without consuming
        // a level.
        assertDiagnosingMatcher(cyclicWithNestedMap("zk", "av"), cyclicWithNestedMap("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childString"),
                        "Expected a mismatch inside the inner pair, was: " + error.getMessage()));
    }

    @Test
    public void anEqualNestedMapInACircularReferenceGraphStillMatches() {
        assertDiagnosingMatcher(cyclicWithNestedMap("zk", "av"), cyclicWithNestedMap("zk", "av"));
    }

    static class CyclicHolder {
        One cycle;
        Map<ChildBean, Map<ChildBean, ChildBean>> nested = new LinkedHashMap<>();
    }

    private static CyclicHolder cyclicWithNestedMap(String key, String value) {
        CyclicHolder holder = new CyclicHolder();
        One one = new One();
        one.setGenericObject(one);
        holder.cycle = one;
        holder.nested.put(child().childString("L1").build(), rootMap(key, value));
        return holder;
    }

    private static NestedMapHolder nested(String key, String value) {
        NestedMapHolder holder = new NestedMapHolder();
        holder.outer.put(child().childString("L1").build(), rootMap(key, value));
        return holder;
    }

    private static FourDeepHolder fourDeep(String key, String value) {
        Map<ChildBean, Map<ChildBean, ChildBean>> third = new LinkedHashMap<>();
        third.put(child().childString("L3").build(), rootMap(key, value));
        Map<ChildBean, Map<ChildBean, Map<ChildBean, ChildBean>>> second = new LinkedHashMap<>();
        second.put(child().childString("L2").build(), third);
        FourDeepHolder holder = new FourDeepHolder();
        holder.outer.put(child().childString("L1").build(), second);
        return holder;
    }

    private static SetOfMapOfSetOfMapHolder setOfMapOfSetOfMap(String key, String value) {
        Set<Map<ChildBean, ChildBean>> innerSet = new LinkedHashSet<>();
        innerSet.add(rootMap(key, value));
        Map<ChildBean, Set<Map<ChildBean, ChildBean>>> middle = new LinkedHashMap<>();
        middle.put(child().childString("L2").build(), innerSet);
        SetOfMapOfSetOfMapHolder holder = new SetOfMapOfSetOfMapHolder();
        holder.s.add(middle);
        return holder;
    }

    private static MapUnderTwoCollectionLevelsHolder underTwoCollections(String key, String value) {
        MapUnderTwoCollectionLevelsHolder holder = new MapUnderTwoCollectionLevelsHolder();
        holder.outer.put(child().childString("L1").build(),
                Arrays.asList(Arrays.asList(rootMap(key, value))));
        return holder;
    }

    @Test
    public void equalRootMapsStillMatch() {
        assertDiagnosingMatcher(rootMap("zk", "av"), rootMap("zk", "av"));
    }

    @Test
    public void aRootMapNoLongerMatchesItsTranspose() {
        // A root map is recognised from the object's type, since there is no field name to carry a marker.
        assertDiagnosingMatcher(rootMap("zk", "av"), rootMap("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("[0][0].childString")
                                && error.getMessage().contains("[0][1].childString"),
                        "Expected mismatches at both pair positions, was: " + error.getMessage()));
    }

    @Test
    public void aRootMapNoLongerMatchesItsTransposeWhenASelectorReachesTheRoot() {
        // The root case is only protected because applySorting is told the root is a map. It runs before
        // applyRootCollectionSorting, so that method cannot make up for a wrong answer here -- the halves
        // would already be swapped. The tests above never reach this code, because with no selector
        // matching "" the root array is not sorted at all.
        assertDiagnosingMatcher(rootMap("zk", "av"), rootMap("av", "zk"),
                beanMatcher -> beanMatcher.sortField(Matchers.any(String.class)),
                AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("[0][0].childString")
                                && error.getMessage().contains("[0][1].childString"),
                        "Expected mismatches at both pair positions, was: " + error.getMessage()));
    }

    @Test
    public void aRootMapKeepsItsKeyFirstUnderAnEmptyPathSort() {
        // sortField("") is the other way a selector reaches the root.
        assertDiagnosingMatcher(rootMap("zk", "av"), rootMap("zk", "av"),
                beanMatcher -> beanMatcher.sortField(""));
    }

    // --- a root map's inner levels ----------------------------------------------------------------
    // The root has no field name, so its shape is read off the object itself. That has to reach
    // applySorting, which runs first: a wrong answer there cannot be repaired afterwards.

    @Test
    public void aRootMapsNestedMapNoLongerMatchesItsTranspose() {
        assertDiagnosingMatcher(nestedRootMap("zk", "av"), nestedRootMap("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("[0][1][0][0].childString")
                                && error.getMessage().contains("[0][1][0][1].childString"),
                        "Expected mismatches at both halves of the inner pair, was: " + error.getMessage()));
    }

    @Test
    public void aRootMapsNestedMapStillMatchesItselfUnderAnEmptyPathSort() {
        assertDiagnosingMatcher(nestedRootMap("zk", "av"), nestedRootMap("zk", "av"),
                beanMatcher -> beanMatcher.sortField(""));
    }

    @Test
    public void aRootMapsNestedMapNoLongerMatchesItsTransposeUnderANameMatcherSort() {
        assertDiagnosingMatcher(nestedRootMap("zk", "av"), nestedRootMap("av", "zk"),
                beanMatcher -> beanMatcher.sortField(Matchers.any(String.class)),
                AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("[0][1][0][0].childString")
                                && error.getMessage().contains("[0][1][0][1].childString"),
                        "Expected mismatches at both halves of the inner pair, was: " + error.getMessage()));
    }

    @Test
    public void aRootMapOfObjectDeclaredValuesHoldingMapsIsProtected() {
        // Declared as Object, so no generic type could describe this -- the object itself can.
        assertDiagnosingMatcher(looselyTypedRootMap("zk", "av"), looselyTypedRootMap("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("[0][1][0][0].childString"),
                        "Expected a mismatch at the inner key, was: " + error.getMessage()));
    }

    @Test
    public void aRootSetOfMapsNoLongerMatchesItsTranspose() {
        Set<Map<ChildBean, ChildBean>> actual = new LinkedHashSet<>();
        actual.add(rootMap("zk", "av"));
        Set<Map<ChildBean, ChildBean>> transposed = new LinkedHashSet<>();
        transposed.add(rootMap("av", "zk"));

        assertDiagnosingMatcher(actual, transposed, beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childString"),
                        "Expected a mismatch inside the pair, was: " + error.getMessage()));
    }

    @Test
    public void aRootMapWithOnlySomeMapValuesKeepsTheOldOrder() {
        // The walk appends a level only while every value at it is the same kind of container, so a root map
        // whose values are not all maps is described one level deep and no further. Its inner map is then
        // unprotected and still matches its transpose, exactly as before the fix. Pinned as it behaves, not
        // as an endorsement: claiming the level for the values that are maps would claim it for the one that
        // is not, and nothing in the tree says which is which.
        assertDiagnosingMatcher(mixedRootMap("zk", "av"), mixedRootMap("av", "zk"));
    }

    @Test
    public void twoEqualRootMapsBuiltInDifferentOrdersStillMatch() {
        // The root's shape is read off the object, and both sides are read separately, so it has to depend
        // on content alone. Reading one representative child would make it depend on values() iteration
        // order instead, and two maps that are equal could then be described differently and fail against
        // each other -- with no file to regenerate out of it.
        Map<ChildBean, Object> forward = new LinkedHashMap<>();
        forward.put(child().childString("L1-a").build(), rootMap("zk", "av"));
        forward.put(child().childString("L1-b").build(), child().childString("notAMap").build());

        Map<ChildBean, Object> backward = new LinkedHashMap<>();
        backward.put(child().childString("L1-b").build(), child().childString("notAMap").build());
        backward.put(child().childString("L1-a").build(), rootMap("zk", "av"));

        assertDiagnosingMatcher(forward, backward);
    }

    private static Map<ChildBean, Map<ChildBean, ChildBean>> nestedRootMap(String key, String value) {
        Map<ChildBean, Map<ChildBean, ChildBean>> root = new LinkedHashMap<>();
        root.put(child().childString("L1").build(), rootMap(key, value));
        return root;
    }

    private static Map<ChildBean, Object> looselyTypedRootMap(String key, String value) {
        Map<ChildBean, Object> root = new LinkedHashMap<>();
        root.put(child().childString("L1").build(), rootMap(key, value));
        return root;
    }

    private static Map<ChildBean, Object> mixedRootMap(String key, String value) {
        Map<ChildBean, Object> root = new LinkedHashMap<>();
        root.put(child().childString("L1").build(), rootMap(key, value));
        root.put(child().childString("L2").build(), child().childString("notAMap").build());
        return root;
    }
}
