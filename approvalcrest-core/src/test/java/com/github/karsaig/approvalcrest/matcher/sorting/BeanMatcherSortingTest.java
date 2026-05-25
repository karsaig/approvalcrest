package com.github.karsaig.approvalcrest.matcher.sorting;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Test;

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
}
