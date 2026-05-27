package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

/**
 * Regression tests for the JSON processing pipeline used by {@link DiagnosingCustomisableMatcher}.
 *
 * <p>These tests cover all pipeline stages—path-based field ignoring ({@code findPaths}),
 * pattern-based field ignoring ({@code filterByCustomMatcherPatterns}), alias substitution
 * ({@code applyAliases}), object-key sorting ({@code sortJsonFields}), and array sorting
 * ({@code applySorting})—in combination with deeply-nested (3-level) input data.
 *
 * <p>They are designed to catch regressions introduced by pipeline optimizations:
 * <ul>
 *   <li>#1 / #2 – {@code toJsonTree} round-trip elimination</li>
 *   <li>#3 – {@code PathLevel} map caching inside {@code applySorting}</li>
 *   <li>#4 – merging {@code findPaths} + {@code filterByCustomMatcherPatterns} into one traversal</li>
 *   <li>#5 – merging {@code sortJsonFields} into {@code applySorting}</li>
 *   <li>#6 – merging {@code applyAliases} into {@code applySorting}</li>
 * </ul>
 */
public class BeanMatcherPipelineRegressionTest extends AbstractBeanMatcherTest {

    // =========================================================================
    // Test data model — 3 levels of nesting
    // =========================================================================

    /** Leaf-level bean (depth 3). */
    static class L3Bean {
        String id;
        String stableValue;
        String changeableValue;

        L3Bean(String id, String stableValue, String changeableValue) {
            this.id = id;
            this.stableValue = stableValue;
            this.changeableValue = changeableValue;
        }
    }

    /** Middle-level bean (depth 2). */
    static class L2Bean {
        String id;
        String stableValue;
        String changeableValue;
        List<L3Bean> items;
        Set<String> tags;

        L2Bean(String id, String stableValue, String changeableValue) {
            this.id = id;
            this.stableValue = stableValue;
            this.changeableValue = changeableValue;
            this.items = new ArrayList<L3Bean>();
            this.tags = new LinkedHashSet<String>();
        }

        L2Bean item(L3Bean child) {
            items.add(child);
            return this;
        }

        L2Bean tag(String tag) {
            tags.add(tag);
            return this;
        }
    }

    /** Top-level bean (depth 1). */
    static class L1Bean {
        String id;
        String stableValue;
        String changeableValue;
        List<L2Bean> items;
        Set<String> tags;
        Map<String, String> attrs;

        L1Bean(String id, String stableValue, String changeableValue) {
            this.id = id;
            this.stableValue = stableValue;
            this.changeableValue = changeableValue;
            this.items = new ArrayList<L2Bean>();
            this.tags = new LinkedHashSet<String>();
            this.attrs = new LinkedHashMap<String, String>();
        }

        L1Bean item(L2Bean child) {
            items.add(child);
            return this;
        }

        L1Bean tag(String tag) {
            tags.add(tag);
            return this;
        }

        L1Bean attr(String key, String value) {
            attrs.put(key, value);
            return this;
        }
    }

    private static L1Bean l1(String id, String stableValue, String changeableValue) {
        return new L1Bean(id, stableValue, changeableValue);
    }

    private static L2Bean l2(String id, String stableValue, String changeableValue) {
        return new L2Bean(id, stableValue, changeableValue);
    }

    private static L3Bean l3(String id, String stableValue, String changeableValue) {
        return new L3Bean(id, stableValue, changeableValue);
    }

    // =========================================================================
    // Group 1: Path-based ignoring (findPaths) with deep nesting
    //
    // Relevant optimizations: #1 (toJsonTree round-trip), #2 (same in findPaths overload),
    // #4 (merge findPaths + filterByCustomMatcherPatterns)
    // =========================================================================

    @Test
    void ignorePathAtDepth1WithDeepNesting() {
        // ignoring("changeableValue") removes the field at depth 1.
        // L2 and L3 content is stable; only L1.changeableValue differs between actual and expected.
        L1Bean actual = l1("root", "stable", "actual-volatile")
                .item(l2("l2a", "l2-stable", "l2-same")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "expected-volatile")
                .item(l2("l2a", "l2-stable", "l2-same")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoring("changeableValue"));
    }

    @Test
    void ignorePathAtDepth2WithDeepNesting() {
        // ignoring("items.changeableValue") removes L2.changeableValue at every
        // element in the items[] array. L1 and L3 content is stable.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "actual-l2-volatile")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "expected-l2-volatile")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoring("items.changeableValue"));
    }

    @Test
    void ignorePathAtDepth3WithDeepNesting() {
        // ignoring("items.items.changeableValue") removes L3.changeableValue.
        // Tests that the path parser correctly navigates through two array levels
        // (the main target of optimization #3: PathLevel two-level navigation).
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "l2-same")
                        .item(l3("l3a", "l3-stable", "actual-l3-volatile")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "l2-same")
                        .item(l3("l3a", "l3-stable", "expected-l3-volatile")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoring("items.items.changeableValue"));
    }

    @Test
    void ignorePathAtDepth3WithMultipleItemsAtEachLevel() {
        // Multiple L2 items, each with multiple L3 items — verifies that the ignore
        // applies to ALL array occurrences (not just the first), stressing the
        // recursive traversal with higher branching factor.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable-A", "l2-same-A")
                        .item(l3("l3-a1", "l3-stable-A1", "volatile-A1-actual"))
                        .item(l3("l3-a2", "l3-stable-A2", "volatile-A2-actual")))
                .item(l2("l2b", "l2-stable-B", "l2-same-B")
                        .item(l3("l3-b1", "l3-stable-B1", "volatile-B1-actual"))
                        .item(l3("l3-b2", "l3-stable-B2", "volatile-B2-actual")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable-A", "l2-same-A")
                        .item(l3("l3-a1", "l3-stable-A1", "volatile-A1-expected"))
                        .item(l3("l3-a2", "l3-stable-A2", "volatile-A2-expected")))
                .item(l2("l2b", "l2-stable-B", "l2-same-B")
                        .item(l3("l3-b1", "l3-stable-B1", "volatile-B1-expected"))
                        .item(l3("l3-b2", "l3-stable-B2", "volatile-B2-expected")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoring("items.items.changeableValue"));
    }

    // =========================================================================
    // Group 2: Pattern-based field removal (filterByCustomMatcherPatterns) with deep nesting
    //
    // Relevant optimizations: #4 (merge findPaths + filterByCustomMatcherPatterns)
    // =========================================================================

    @Test
    void withMatcherIgnoresFieldAtAllDepths() {
        // withMatcher(fieldNamePattern, anything()) removes matching fields from the
        // JSON tree via filterByCustomMatcherPatterns — a separate traversal from findPaths.
        // Tests that this traversal reaches all 3 nesting levels.
        L1Bean actual = l1("root", "stable", "actual-l1-volatile")
                .item(l2("l2a", "l2-stable", "actual-l2-volatile")
                        .item(l3("l3a", "l3-stable", "actual-l3-volatile")));

        L1Bean expected = l1("root", "stable", "expected-l1-volatile")
                .item(l2("l2a", "l2-stable", "expected-l2-volatile")
                        .item(l3("l3a", "l3-stable", "expected-l3-volatile")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.withMatcher(is("changeableValue"), anything()));
    }

    // =========================================================================
    // Group 3: Alias substitution (applyAliases) with deep nesting
    //
    // Relevant optimizations: #6 (merge applyAliases into applySorting)
    // =========================================================================

    @Test
    void aliasNormalizesValueAtAllDepths() {
        // A global alias replaces the volatile value at all 3 levels of the tree.
        // Tests that applyAliases recursion reaches depth 3.
        L1Bean actual = l1("root", "stable", "volatile-actual")
                .item(l2("l2a", "l2-stable", "volatile-actual")
                        .item(l3("l3a", "l3-stable", "volatile-actual")));

        L1Bean expected = l1("root", "stable", "volatile-expected")
                .item(l2("l2a", "l2-stable", "volatile-expected")
                        .item(l3("l3a", "l3-stable", "volatile-expected")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.withAlias("volatile-actual", "<ALIASED>")
                        .withAlias("volatile-expected", "<ALIASED>"));
    }

    @Test
    void fieldScopedAliasOnlyAffectsNamedField() {
        // withAlias(fieldName, value, alias) replaces a value only when both the field
        // name AND the value match — other fields with the same value are NOT aliased.
        // actual: every field at every level has value "raw".
        // Only "changeableValue" fields should be aliased; "stableValue" must remain "raw".
        L1Bean actual = l1("root", "raw", "raw")
                .item(l2("l2a", "raw", "raw")
                        .item(l3("l3a", "raw", "raw")));

        L1Bean expected = l1("root", "raw", "<ALIASED>")
                .item(l2("l2a", "raw", "<ALIASED>")
                        .item(l3("l3a", "raw", "<ALIASED>")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.withAlias("changeableValue", "raw", "<ALIASED>"));
    }

    @Test
    void aliasMapWithValuePatternNormalizesDeepValues() {
        // AliasMap using an entry with a valuePattern normalizes values matching the
        // regex at all depths. Tests that the EntryBuilder fluent API integrates
        // correctly with the deep-nesting traversal.
        L1Bean actual = l1("root", "stable", "volatile-uuid-111")
                .item(l2("l2a", "l2-stable", "volatile-uuid-222")
                        .item(l3("l3a", "l3-stable", "volatile-uuid-333")));

        L1Bean expected = l1("root", "stable", "volatile-uuid-999")
                .item(l2("l2a", "l2-stable", "volatile-uuid-888")
                        .item(l3("l3a", "l3-stable", "volatile-uuid-777")));

        AliasMap aliasMap = AliasMap.builder()
                .entry()
                    .valuePattern(Pattern.compile("volatile-uuid-\\d+"))
                    .alias("<UUID>")
                    .register()
                .build();

        assertDiagnosingMatcher(actual, expected, m -> m.withAliasMap(aliasMap));
    }

    // =========================================================================
    // Group 4: Array sorting (applySorting) with deep nesting
    //
    // Relevant optimizations: #3 (PathLevel map caching), #5 (merge sortJsonFields)
    // =========================================================================

    @Test
    void sortL2ListAtDepth1WithDeepContent() {
        // sortFieldPath("items") sorts the L2 items[] array at depth 1.
        // Each L2 element carries nested L3 children — verifies that sorting
        // preserves nested content correctly (bottom-up ordering is required so
        // nested elements are already sorted when the outer sort key is computed).
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("B", "stable-B", "change-B")
                        .item(l3("l3-B1", "l3-stable-B1", "l3-change-B1"))
                        .item(l3("l3-B2", "l3-stable-B2", "l3-change-B2")))
                .item(l2("A", "stable-A", "change-A")
                        .item(l3("l3-A1", "l3-stable-A1", "l3-change-A1"))
                        .item(l3("l3-A2", "l3-stable-A2", "l3-change-A2")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("A", "stable-A", "change-A")
                        .item(l3("l3-A1", "l3-stable-A1", "l3-change-A1"))
                        .item(l3("l3-A2", "l3-stable-A2", "l3-change-A2")))
                .item(l2("B", "stable-B", "change-B")
                        .item(l3("l3-B1", "l3-stable-B1", "l3-change-B1"))
                        .item(l3("l3-B2", "l3-stable-B2", "l3-change-B2")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.sortFieldPath(SortField.of("items", "changeableValue")));
    }

    @Test
    void sortL3ListAtDepth2() {
        // sortFieldPath("items.items") sorts L3 items[] nested inside L2.
        // Tests PathLevel two-level navigation and the caching optimization target (#3):
        // getPathsMap({"items.items": [SF]}) must be computed once and reused at
        // each recursive descent, not rebuilt on every call.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "l2-change")
                        .item(l3("C", "l3-stable-C", "l3-change-C"))
                        .item(l3("A", "l3-stable-A", "l3-change-A"))
                        .item(l3("B", "l3-stable-B", "l3-change-B")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "l2-change")
                        .item(l3("A", "l3-stable-A", "l3-change-A"))
                        .item(l3("B", "l3-stable-B", "l3-change-B"))
                        .item(l3("C", "l3-stable-C", "l3-change-C")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.sortFieldPath(SortField.of("items.items", "changeableValue")));
    }

    @Test
    void sortL2AndL3ListsSimultaneously() {
        // Both items[] (L2) and items.items[] (L3) are sorted simultaneously.
        // Tests that two PathLevel entries at different depths don't interfere
        // with each other when both are evaluated during the same traversal.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("B", "stable-B", "change-B")
                        .item(l3("B3", "l3-stable-B3", "l3-change-B3"))
                        .item(l3("B1", "l3-stable-B1", "l3-change-B1"))
                        .item(l3("B2", "l3-stable-B2", "l3-change-B2")))
                .item(l2("A", "stable-A", "change-A")
                        .item(l3("A3", "l3-stable-A3", "l3-change-A3"))
                        .item(l3("A1", "l3-stable-A1", "l3-change-A1"))
                        .item(l3("A2", "l3-stable-A2", "l3-change-A2")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("A", "stable-A", "change-A")
                        .item(l3("A1", "l3-stable-A1", "l3-change-A1"))
                        .item(l3("A2", "l3-stable-A2", "l3-change-A2"))
                        .item(l3("A3", "l3-stable-A3", "l3-change-A3")))
                .item(l2("B", "stable-B", "change-B")
                        .item(l3("B1", "l3-stable-B1", "l3-change-B1"))
                        .item(l3("B2", "l3-stable-B2", "l3-change-B2"))
                        .item(l3("B3", "l3-stable-B3", "l3-change-B3")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.sortFieldPath(SortField.of("items", "changeableValue"))
                        .sortFieldPath(SortField.of("items.items", "changeableValue")));
    }

    // =========================================================================
    // Group 5: Auto-sorting of Set and Map fields with deep nesting
    //
    // Relevant optimizations: #5 (merge sortJsonFields into applySorting)
    // =========================================================================

    @Test
    void setAutoSortAtDepth1() {
        // Set<String> tags at L1 are auto-sorted regardless of insertion order.
        // Tests that the MARKER mechanism and auto-sort still work with deep nested content.
        L1Bean actual = l1("root", "stable", "same").tag("zebra").tag("apple").tag("mango")
                .item(l2("l2a", "l2-stable", "l2-same")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "same").tag("apple").tag("mango").tag("zebra")
                .item(l2("l2a", "l2-stable", "l2-same")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        assertDiagnosingMatcher(actual, expected, m -> m);
    }

    @Test
    void setAutoSortAtDepth2() {
        // Set<String> tags at L2 auto-sorted — tests that Set auto-sort works correctly
        // in nested objects (not just at the root level).
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "l2-same").tag("cherry").tag("apple").tag("banana")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "l2-same").tag("apple").tag("banana").tag("cherry")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        assertDiagnosingMatcher(actual, expected, m -> m);
    }

    @Test
    void mapAutoSortAtDepth1() {
        // Map<String,String> attrs at L1 auto-sorted regardless of insertion order.
        L1Bean actual = l1("root", "stable", "same").attr("z-key", "z-val").attr("a-key", "a-val").attr("m-key", "m-val")
                .item(l2("l2a", "l2-stable", "l2-same")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "same").attr("a-key", "a-val").attr("m-key", "m-val").attr("z-key", "z-val")
                .item(l2("l2a", "l2-stable", "l2-same")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        assertDiagnosingMatcher(actual, expected, m -> m);
    }

    // =========================================================================
    // Group 6: sortType feature with deep nesting
    //
    // Relevant optimizations: #3, #5 (same traversals as explicit sorting)
    // =========================================================================

    @Test
    void sortTypeOnL2BeanListWithDeepContent() {
        // sortType(L2Bean.class) auto-sorts items[] at depth 1 by marking it at
        // Gson serialization time. Tests that the MARKER mechanism survives when
        // each L2 element contains deep nested L3 content.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("B", "stable-B", "change-B")
                        .item(l3("l3b", "l3-stable", "l3-same")))
                .item(l2("A", "stable-A", "change-A")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("A", "stable-A", "change-A")
                        .item(l3("l3a", "l3-stable", "l3-same")))
                .item(l2("B", "stable-B", "change-B")
                        .item(l3("l3b", "l3-stable", "l3-same")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.sortType(L2Bean.class));
    }

    @Test
    void sortTypeOnL3BeanListWithDeepNesting() {
        // sortType(L3Bean.class) auto-sorts L3 items[] nested inside L2 elements.
        // Tests that the sortType MARKER is set and detected correctly two levels deep.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "l2-change")
                        .item(l3("C", "l3-stable-C", "l3-change-C"))
                        .item(l3("A", "l3-stable-A", "l3-change-A"))
                        .item(l3("B", "l3-stable-B", "l3-change-B")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("l2a", "l2-stable", "l2-change")
                        .item(l3("A", "l3-stable-A", "l3-change-A"))
                        .item(l3("B", "l3-stable-B", "l3-change-B"))
                        .item(l3("C", "l3-stable-C", "l3-change-C")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.sortType(L3Bean.class));
    }

    // =========================================================================
    // Group 7: Feature combinations — primary regression targets
    //
    // Each test combines two or more pipeline stages to catch regressions in
    // their interactions after the optimizations are applied.
    // =========================================================================

    @Test
    void combinePathIgnoreAndSortWithDeepNesting() {
        // findPaths (path ignore at L2) + applySorting (sort items[]) active simultaneously.
        // Primary regression test for optimization #4: merging findPaths traversal with the
        // sorting traversal must not affect each other.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("B", "stable-B", "actual-l2-volatile")
                        .item(l3("l3b", "l3-stable", "l3-same")))
                .item(l2("A", "stable-A", "actual-l2-volatile")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("A", "stable-A", "expected-l2-volatile")
                        .item(l3("l3a", "l3-stable", "l3-same")))
                .item(l2("B", "stable-B", "expected-l2-volatile")
                        .item(l3("l3b", "l3-stable", "l3-same")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoring("items.changeableValue")
                        .sortFieldPath(SortField.of("items")));
    }

    @Test
    void combineWithMatcherAndSortWithDeepNesting() {
        // filterByCustomMatcherPatterns (withMatcher) + applySorting active simultaneously.
        // Tests that the two separate JSON-tree traversals interoperate correctly
        // when array sorting is also configured (optimization #4 interaction).
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("B", "stable-B", "actual-l2-volatile")
                        .item(l3("l3b", "l3-stable", "l3-same")))
                .item(l2("A", "stable-A", "actual-l2-volatile")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("A", "stable-A", "expected-l2-volatile")
                        .item(l3("l3a", "l3-stable", "l3-same")))
                .item(l2("B", "stable-B", "expected-l2-volatile")
                        .item(l3("l3b", "l3-stable", "l3-same")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.withMatcher(is("changeableValue"), anything())
                        .sortFieldPath(SortField.of("items")));
    }

    @Test
    void combinePathIgnoreAndWithMatcherOnDifferentFields() {
        // findPaths AND filterByCustomMatcherPatterns active simultaneously, each targeting
        // a DIFFERENT field. This is the critical scenario for optimization #4 correctness:
        // both traversals must execute fully and independently.
        // - L2.stableValue: ignored via path (findPaths)
        // - changeableValue at all depths: removed via withMatcher (filterByCustomMatcherPatterns)
        L1Bean actual = l1("root", "stable-L1", "actual-l1-change")
                .item(l2("l2a", "actual-l2-stable", "actual-l2-change")
                        .item(l3("l3a", "stable-L3", "actual-l3-change")));

        L1Bean expected = l1("root", "stable-L1", "expected-l1-change")
                .item(l2("l2a", "expected-l2-stable", "expected-l2-change")
                        .item(l3("l3a", "stable-L3", "expected-l3-change")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoring("items.stableValue")
                        .withMatcher(is("changeableValue"), anything()));
    }

    @Test
    void combineAliasAndSortWithDeepNesting() {
        // applyAliases + applySorting active simultaneously.
        // Tests that alias substitution (optimization #6 target) and array sorting
        // compose correctly: aliases must be visible in the sort key when applied
        // before sorting, and must not corrupt the sort order.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("B", "stable-B", "actual-volatile-B")
                        .item(l3("l3b", "l3-stable", "l3-same")))
                .item(l2("A", "stable-A", "actual-volatile-A")
                        .item(l3("l3a", "l3-stable", "l3-same")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("A", "stable-A", "expected-volatile-A")
                        .item(l3("l3a", "l3-stable", "l3-same")))
                .item(l2("B", "stable-B", "expected-volatile-B")
                        .item(l3("l3b", "l3-stable", "l3-same")));

        // Alias normalizes the volatile values; sorting is then stable on the remaining id field.
        assertDiagnosingMatcher(actual, expected,
                m -> m.withAlias("actual-volatile-A", "<ALIAS-A>")
                        .withAlias("expected-volatile-A", "<ALIAS-A>")
                        .withAlias("actual-volatile-B", "<ALIAS-B>")
                        .withAlias("expected-volatile-B", "<ALIAS-B>")
                        .sortFieldPath(SortField.of("items", "changeableValue")));
    }

    @Test
    void combineAliasAndSortWithDeepNestingAndMultipleChildren() {
        // Same as combineAliasAndSortWithDeepNesting but with 3 elements (odd count) and 2
        // L3 children in each, to exercise the merge-sort comparator at multiple depths.
        L1Bean actual = l1("root", "stable", "same")
                .item(l2("C", "stable-C", "actual-volatile-C")
                        .item(l3("l3-c1", "l3-stable-C1", "l3-same"))
                        .item(l3("l3-c2", "l3-stable-C2", "l3-same")))
                .item(l2("A", "stable-A", "actual-volatile-A")
                        .item(l3("l3-a1", "l3-stable-A1", "l3-same"))
                        .item(l3("l3-a2", "l3-stable-A2", "l3-same")))
                .item(l2("B", "stable-B", "actual-volatile-B")
                        .item(l3("l3-b1", "l3-stable-B1", "l3-same"))
                        .item(l3("l3-b2", "l3-stable-B2", "l3-same")));

        L1Bean expected = l1("root", "stable", "same")
                .item(l2("A", "stable-A", "expected-volatile-A")
                        .item(l3("l3-a1", "l3-stable-A1", "l3-same"))
                        .item(l3("l3-a2", "l3-stable-A2", "l3-same")))
                .item(l2("B", "stable-B", "expected-volatile-B")
                        .item(l3("l3-b1", "l3-stable-B1", "l3-same"))
                        .item(l3("l3-b2", "l3-stable-B2", "l3-same")))
                .item(l2("C", "stable-C", "expected-volatile-C")
                        .item(l3("l3-c1", "l3-stable-C1", "l3-same"))
                        .item(l3("l3-c2", "l3-stable-C2", "l3-same")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.withAlias("actual-volatile-A", "<ALIAS-A>")
                        .withAlias("expected-volatile-A", "<ALIAS-A>")
                        .withAlias("actual-volatile-B", "<ALIAS-B>")
                        .withAlias("expected-volatile-B", "<ALIAS-B>")
                        .withAlias("actual-volatile-C", "<ALIAS-C>")
                        .withAlias("expected-volatile-C", "<ALIAS-C>")
                        .sortFieldPath(SortField.of("items", "changeableValue")));
    }

    @Test
    void combineAllFeaturesWithDeepNesting() {
        // ALL pipeline stages active simultaneously on a 3-level deep structure.
        // This is the most comprehensive regression test:
        // - findPaths: ignores L1.changeableValue and L2.changeableValue via separate paths
        // - applyAliases: normalizes L3.changeableValue to the same alias in both trees
        // - sortJsonFields: alphabetizes JSON keys at all levels (merged into applySorting in #5)
        // - applySorting: sorts items[] at depth 1
        // Any regression in any pipeline stage or their interaction would cause a failure.
        L1Bean actual = l1("root", "stable", "actual-l1-volatile")
                .item(l2("B", "stable-B", "actual-l2-volatile-B")
                        .item(l3("l3-B", "l3-stable-B", "actual-l3-volatile")))
                .item(l2("A", "stable-A", "actual-l2-volatile-A")
                        .item(l3("l3-A", "l3-stable-A", "actual-l3-volatile")));

        L1Bean expected = l1("root", "stable", "expected-l1-volatile")
                .item(l2("A", "stable-A", "expected-l2-volatile-A")
                        .item(l3("l3-A", "l3-stable-A", "expected-l3-volatile")))
                .item(l2("B", "stable-B", "expected-l2-volatile-B")
                        .item(l3("l3-B", "l3-stable-B", "expected-l3-volatile")));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoring("changeableValue")           // findPaths: removes L1.changeableValue
                        .ignoring("items.changeableValue")   // findPaths: removes L2.changeableValue
                        .withAlias("actual-l3-volatile", "<L3>")    // applyAliases: normalize L3
                        .withAlias("expected-l3-volatile", "<L3>")
                        .sortFieldPath(SortField.of("items")));     // applySorting: sort L2 items
    }
}
