package com.github.karsaig.approvalcrest.matcher.sorting;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Test;

import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
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
}
