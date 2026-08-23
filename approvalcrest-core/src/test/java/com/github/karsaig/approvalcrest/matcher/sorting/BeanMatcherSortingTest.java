package com.github.karsaig.approvalcrest.matcher.sorting;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ChildBean;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Tests for sorting support on {@link com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher}
 * (the {@code sameBeanAs} matcher). Covers sort-gap-7 from the coverage gap plan.
 */
public class BeanMatcherSortingTest extends AbstractBeanMatcherTest {

    // -------------------------------------------------------------------------
    // sortFieldPath on sameBeanAs
    // -------------------------------------------------------------------------

    @Test
    public void sortFieldPathSortsCollectionByNaturalOrder() {
        // Basic sortFieldPath: childBeanList is unsorted in actual, sorted in expected.
        // sort key ignores childInteger so sorting is driven by childString ("apple" < "banana").
        ParentBean actual = parent()
                .addToChildBeanList("banana", 1)
                .addToChildBeanList("apple", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 2)
                .addToChildBeanList("banana", 1)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortFieldPath(SortField.of("childBeanList", "childInteger")));
    }

    @Test
    public void sortFieldPathIgnoredFieldIsStillCompared() {
        // The field ignored for sorting ("childInteger") is still part of the comparison;
        // when the expected has different childInteger values the assertion fails.
        ParentBean actual = parent()
                .addToChildBeanList("banana", 1)
                .addToChildBeanList("apple", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 99)   // wrong childInteger
                .addToChildBeanList("banana", 99)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortFieldPath(SortField.of("childBeanList", "childInteger")),
                AssertionError.class, thrown -> {});
    }

    // -------------------------------------------------------------------------
    // sortFieldMatcher on sameBeanAs
    // -------------------------------------------------------------------------

    @Test
    public void sortFieldMatcherSortsCollectionByNaturalOrder() {
        // sortFieldMatcher uses a Matcher<String> selector; here is("childBeanList") matches
        // the field name exactly and triggers sorting — same outcome as sortFieldPath.
        ParentBean actual = parent()
                .addToChildBeanList("cherry", 3)
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .addToChildBeanList("cherry", 3)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortFieldMatcher(SortField.of(is("childBeanList"), "childInteger")));
    }

    @Test
    public void sortFieldPathWithoutIgnoringFailsWhenOrderDiffers() {
        // Confirms that without sortField the bean matcher is order-sensitive;
        // the same data that passes with sorting fails without it.
        ParentBean actual = parent()
                .addToChildBeanList("banana", 1)
                .addToChildBeanList("apple", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 2)
                .addToChildBeanList("banana", 1)
                .build();

        // no sortFieldPath configured — order mismatch → fails
        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher, AssertionError.class, thrown -> {});
    }

    @Test
    public void withMatcherPatternFieldsAreExcludedFromSortKey() {
        // withMatcher(pattern) fields must be stripped BEFORE sort key computation.
        // actual[0]: childString="B", childInteger=10
        // actual[1]: childString="A", childInteger=20
        // If childInteger stays in the sort key its numeric prefix "10" < "20" makes
        // "B"-element sort first — the wrong order.  After stripping it the sort key
        // is childString only: "A" < "B" → "A"-element comes first (correct).
        ParentBean actual = parent()
                .addToChildBeanList("B", 10)
                .addToChildBeanList("A", 20)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("A", 20)
                .addToChildBeanList("B", 10)
                .build();
        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher
                        .withMatcher(containsString("childInteger"), anything())
                        .sortFieldPath(SortField.of("childBeanList")));
    }

    // -------------------------------------------------------------------------
    // sortField(String) shorthand — plainest API
    // -------------------------------------------------------------------------

    @Test
    public void sortFieldStringShorthandSortsCollection() {
        // sortField(String) is the simplest API: equivalent to sortFieldPath(SortField.of(path)).
        // Confirms the shorthand reaches the same code path as the full SortField API.
        ParentBean actual = parent()
                .addToChildBeanList("cherry", 3)
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .addToChildBeanList("cherry", 3)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortField("childBeanList"));
    }

    // -------------------------------------------------------------------------
    // sortField(Matcher<String>) shorthand — matcher-selector API
    // -------------------------------------------------------------------------

    @Test
    public void sortFieldMatcherShorthandSortsCollection() {
        // sortField(Matcher<String>) is the matcher-selector shorthand.
        // Using containsString matches any field whose name contains "childBeanList".
        ParentBean actual = parent()
                .addToChildBeanList("zebra", 3)
                .addToChildBeanList("ant", 1)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("ant", 1)
                .addToChildBeanList("zebra", 3)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortField(is("childBeanList")));
    }

    // -------------------------------------------------------------------------
    // Chain of multiple sortField calls on different collections
    // -------------------------------------------------------------------------

    @Test
    public void chainedSortFieldCallsSortMultipleCollectionsIndependently() {
        // Two separate sortField calls, each targeting a different field.
        // childBeanList is unsorted in actual; we sort it with sortField("childBeanList").
        // A second sortField call chains on the same matcher for correctness.
        // (ParentBean only has one list field, so we chain two path calls on the same field
        //  with different ignored-field configurations to verify chaining works.)
        ParentBean actual = parent()
                .addToChildBeanList("cherry", 3)
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .addToChildBeanList("cherry", 3)
                .build();

        // Two chained calls: first sorts by full element, second is a redundant
        // sort on the same path — the last sort wins or they compose harmlessly.
        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher
                        .sortField("childBeanList")
                        .sortField(is("childBeanList")));
    }

    // -------------------------------------------------------------------------
    // sortType — auto-sort List<T> without explicit field path
    // -------------------------------------------------------------------------

    @Test
    public void sortTypeAutomaticallySortsListOfThatType() {
        // sortType(ChildBean.class) marks the childBeanList field for auto-sorting,
        // just as Set fields are sorted automatically.
        ParentBean actual = parent()
                .addToChildBeanList("banana", 1)
                .addToChildBeanList("apple", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 2)
                .addToChildBeanList("banana", 1)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortType(ChildBean.class));
    }

    @Test
    public void sortTypeWithThreeElementsSortsListOfThatType() {
        ParentBean actual = parent()
                .addToChildBeanList("cherry", 3)
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .addToChildBeanList("cherry", 3)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortType(ChildBean.class));
    }

    @Test
    public void withoutSortTypeListOrderMatters() {
        // Baseline negative test: without sortType the list is order-sensitive.
        ParentBean actual = parent()
                .addToChildBeanList("banana", 1)
                .addToChildBeanList("apple", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 2)
                .addToChildBeanList("banana", 1)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher, AssertionError.class, thrown -> {});
    }

    @Test
    public void sortTypeWithUnrelatedTypeIsNoOp() {
        // sortType with a class not present in the bean should have no effect;
        // the test still passes because actual and expected are in the same order.
        ParentBean actual = parent()
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .build();
        ParentBean expected = parent()
                .addToChildBeanList("apple", 1)
                .addToChildBeanList("banana", 2)
                .build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortType(String.class));
    }

    @Test
    public void sortTypeOnEmptyListDoesNotThrow() {
        ParentBean actual = parent().build();
        ParentBean expected = parent().build();

        assertDiagnosingMatcher(actual, expected,
                matcher -> matcher.sortType(ChildBean.class));
    }

    // -------------------------------------------------------------------------
    // The * wildcard segment in a sort path
    // -------------------------------------------------------------------------

    /**
     * Holds two collections, so one can be reached by a wildcard and the other by a literal key —
     * which is what makes the merge of the two sort levels observable.
     */
    static class TagHolder {
        List<String> tags;
        List<String> notes;

        TagHolder(List<String> tags, List<String> notes) {
            this.tags = tags;
            this.notes = notes;
        }
    }

    /** Two map entries, each holding collections that need sorting before comparison. */
    static class TagMapHolder {
        Map<String, TagHolder> byKey = new LinkedHashMap<>();

        TagMapHolder(List<String> firstTags, List<String> firstNotes, List<String> secondTags) {
            byKey.put("k1", new TagHolder(firstTags, firstNotes));
            byKey.put("k2", new TagHolder(secondTags, Collections.emptyList()));
        }
    }

    private static TagMapHolder unsorted() {
        return new TagMapHolder(Arrays.asList("b", "a"), Arrays.asList("y", "x"), Arrays.asList("d", "c"));
    }

    private static TagMapHolder sorted() {
        return new TagMapHolder(Arrays.asList("a", "b"), Arrays.asList("x", "y"), Arrays.asList("c", "d"));
    }

    /**
     * A * segment applies the sort path to every map value, so the same rule that governs ignoring
     * and custom matching governs sorting — one syntax across all three.
     */
    @Test
    public void wildcardSortPathSortsTheCollectionUnderEveryMapValue() {
        assertDiagnosingMatcher(unsorted(), sorted(),
                matcher -> matcher.sortField("byKey.*.tags", "byKey.*.notes"));
    }

    /** The wildcard does the same as naming every key, which is the point of it. */
    @Test
    public void wildcardSortPathMatchesNamingEveryKey() {
        assertDiagnosingMatcher(unsorted(), sorted(),
                matcher -> matcher.sortField("byKey.k1.tags", "byKey.k2.tags", "byKey.k1.notes"));
    }

    /**
     * Naming one key leaves the other entry unsorted. The assertion has to be about *which* entry is
     * reported, not merely that something failed: a message naming only k2 shows k1 really was sorted,
     * where a message naming both would mean the sort had not run at all.
     */
    @Test
    public void namingOneKeyLeavesTheOtherEntryUnsorted() {
        assertDiagnosingMatcher(unsorted(), sorted(),
                matcher -> matcher.sortField("byKey.k1.tags", "byKey.k1.notes"),
                AssertionError.class, error -> {
                    Assertions.assertTrue(error.getMessage().contains("k2"),
                            "Expected the unsorted k2 entry to be reported, was: " + error.getMessage());
                    Assertions.assertFalse(error.getMessage().contains("k1"),
                            "k1 was sorted, so it should not be reported: " + error.getMessage());
                });
    }

    /**
     * A wildcard and a literal key at the same level both take effect rather than one winning. Two
     * different leaves are needed to show it: tags is reached only by the wildcard and notes only by
     * the literal key, so dropping either level fails the assertion.
     */
    @Test
    public void wildcardAndLiteralKeyAtTheSameLevelBothApply() {
        assertDiagnosingMatcher(unsorted(), sorted(),
                matcher -> matcher.sortField("byKey.*.tags", "byKey.k1.notes"));
    }

    /**
     * A wildcard and a literal key naming the <em>same</em> leaf combine rather than one replacing the
     * other. The test above uses different leaves, which proves both levels are consulted but never
     * makes them meet on one key; this one does, so the per-key combine is exercised.
     */
    @Test
    public void wildcardAndLiteralKeyNamingTheSameLeafCombine() {
        assertDiagnosingMatcher(unsorted(), sorted(),
                matcher -> matcher.sortField("byKey.*.tags", "byKey.k1.tags", "byKey.*.notes"));
    }

    /** Each half alone is insufficient, which is what makes the test above meaningful. */
    @Test
    public void neitherTheWildcardNorTheLiteralKeyAloneIsEnough() {
        assertDiagnosingMatcher(unsorted(), sorted(), matcher -> matcher.sortField("byKey.*.tags"),
                AssertionError.class, error -> Assertions.assertTrue(
                        error.getMessage().contains("notes"),
                        "Expected unsorted notes to be reported, was: " + error.getMessage()));
        assertDiagnosingMatcher(unsorted(), sorted(), matcher -> matcher.sortField("byKey.k1.notes"),
                AssertionError.class, error -> Assertions.assertTrue(
                        error.getMessage().contains("tags"),
                        "Expected unsorted tags to be reported, was: " + error.getMessage()));
    }
}
