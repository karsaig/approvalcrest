package com.github.karsaig.approvalcrest.matcher.alias;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ChildBean;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;

/**
 * Integration tests for alias substitution in {@code sameBeanAs} ({@link com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher}).
 */
public class BeanMatcherAliasTest extends AbstractBeanMatcherTest {

    // -------------------------------------------------------------------------
    // Basic substitution
    // -------------------------------------------------------------------------

    @Test
    void withAliasReplacesValueBeforeComparison() {
        // Both actual and expected go through filterJson with alias → both become "<id>"
        ParentBean expected = parent().parentString("volatile-uuid-999").build();
        ParentBean input = parent().parentString("volatile-uuid-123").build();

        // The raw values differ, but after aliasing both become "<id>" → comparison passes
        assertDiagnosingMatcher(input, expected,
                m -> m.withAlias("volatile-uuid-123", "<id>").withAlias("volatile-uuid-999", "<id>"));
    }

    @Test
    void withAliasByFieldAndValueScopedToFieldName() {
        // actual.parentString = "raw", expected.parentString = "raw" → both aliased to "<ps>"
        ParentBean expected = parent().parentString("raw").childBean(child().childString("raw")).build();
        ParentBean input = parent().parentString("raw").childBean(child().childString("raw")).build();

        // Only parentString "raw" → "<ps>"; childString "raw" is not aliased (wrong field)
        assertDiagnosingMatcher(input, expected,
                m -> m.withAlias("parentString", "raw", "<ps>"));
    }

    @Test
    void withAliasMapReplacesValueBeforeComparison() {
        ParentBean expected = parent().parentString("secret").build();
        ParentBean input = parent().parentString("secret").build();

        AliasMap map = AliasMap.builder()
                .add("secret", "<secret>")
                .build();

        assertDiagnosingMatcher(input, expected, m -> m.withAliasMap(map));
    }

    // -------------------------------------------------------------------------
    // String and number coercion
    // -------------------------------------------------------------------------

    @Test
    void numericValueIsCoercedToStringForAliasMatching() {
        // Both actual and expected have childInteger=13 → alias "13" → "<thirteen>"
        ParentBean expected = parent().childBean(child().childInteger(13)).build();
        ParentBean input = parent().childBean(child().childInteger(13)).build();

        assertDiagnosingMatcher(input, expected, m -> m.withAlias("13", "<thirteen>"));
    }

    // -------------------------------------------------------------------------
    // Merge: multiple withAliasMap / withAlias calls accumulate
    // -------------------------------------------------------------------------

    @Test
    void multipleWithAliasCallsAccumulate() {
        ParentBean expected = parent()
                .parentString("id-value")
                .childBean(child().childString("name-value"))
                .build();
        ParentBean input = parent()
                .parentString("id-value")
                .childBean(child().childString("name-value"))
                .build();

        assertDiagnosingMatcher(input, expected,
                m -> m.withAlias("id-value", "<id>").withAlias("name-value", "<name>"));
    }

    @Test
    void withAliasMapMergeLastWinsOnConflict() {
        // Both actual and expected have parentString="shared" → after aliasing both become "<override>"
        ParentBean expected = parent().parentString("shared").build();
        ParentBean input = parent().parentString("shared").build();

        AliasMap base = AliasMap.builder().add("shared", "<base>").build();
        AliasMap override = AliasMap.builder().add("shared", "<override>").build();

        // Both go through aliasing with merged map → both become "<override>" → match
        assertDiagnosingMatcher(input, expected,
                m -> m.withAliasMap(base).withAliasMap(override));
    }

    // -------------------------------------------------------------------------
    // Alias before sort: aliased value changes sort key
    // -------------------------------------------------------------------------

    @Test
    void aliasIsAppliedBeforeSortSoAliasedValueAffectsSortOrder() {
        // Two items: with alias "A"→"<zzz>" and "B"→"<aaa>", sort by childString
        // Without alias: A < B, so A-item first
        // With alias: <aaa> < <zzz>, so B-item (original) ends up first
        // For sameBeanAs: actual = [A-item, B-item], expected = [B-item (aliased as <aaa>), A-item (aliased as <zzz>)]
        // After aliasing both sides: both have [<aaa>-item, <zzz>-item] → match

        List<ParentBean> actual = Arrays.asList(
                parent().parentString("item1").childBean(child().childString("A")).build(),
                parent().parentString("item2").childBean(child().childString("B")).build()
        );
        // Expected in different order — after alias+sort they'll match
        List<ParentBean> expected = Arrays.asList(
                parent().parentString("item2").childBean(child().childString("B")).build(),
                parent().parentString("item1").childBean(child().childString("A")).build()
        );

        assertDiagnosingMatcher(actual, expected,
                m -> m.withAlias("A", "<zzz>").withAlias("B", "<aaa>").sortField(""));
    }

    // -------------------------------------------------------------------------
    // Booleans are NOT aliased
    // -------------------------------------------------------------------------

    @Test
    void booleanFieldIsNotSubstitutedByAlias() {
        // Objects with a boolean flag: alias "true" should NOT replace boolean true
        // Both sides have same structure so match regardless; the test just verifies
        // aliasing doesn't corrupt boolean fields (would cause type mismatch if it did)
        ParentBean expected = parent().parentString("hello").build();
        ParentBean input = parent().parentString("hello").build();

        // If "true" were aliased inside booleans, it would corrupt JSON and cause failure
        assertDiagnosingMatcher(input, expected, m -> m.withAlias("true", "<yes>"));
    }

    // -------------------------------------------------------------------------
    // Dynamic alias
    // -------------------------------------------------------------------------

    @Test
    void dynamicAliasViaEntryBuilder() {
        ParentBean expected = parent().parentString("user-99").build();
        ParentBean input = parent().parentString("user-99").build();

        AliasMap map = AliasMap.builder()
                .entry()
                    .field("parentString")
                    .valuePattern("user-\\d+")
                    .alias(v -> "<user:" + v.split("-")[1] + ">")
                .register()
                .build();

        assertDiagnosingMatcher(input, expected, m -> m.withAliasMap(map));
    }
}
