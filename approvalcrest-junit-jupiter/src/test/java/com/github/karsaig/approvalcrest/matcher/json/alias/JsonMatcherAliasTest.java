package com.github.karsaig.approvalcrest.matcher.json.alias;

import org.junit.jupiter.api.Test;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;

/**
 * JUnit Jupiter smoke test for the alias substitution feature via the public API.
 */
public class JsonMatcherAliasTest {

    @Test
    public void aliasReplacesValueBeforeComparison() {
        Object actual = parent().parentString("volatile-uuid-123").build();

        assertThat(actual, sameJsonAsApproved()
                .withAlias("volatile-uuid-123", "<id>"));
    }

    @Test
    public void aliasAppliedBeforeSort() {
        java.util.List<Object> actual = java.util.Arrays.asList(
                parent().parentString("item1").childBean(child().childString("A")).build(),
                parent().parentString("item2").childBean(child().childString("B")).build()
        );

        // "A"→"<zzz>", "B"→"<aaa>": after alias+sort, item2 (<aaa>) comes first
        assertThat(actual, sameJsonAsApproved()
                .withAlias("A", "<zzz>")
                .withAlias("B", "<aaa>")
                .sortField(""));
    }
}
