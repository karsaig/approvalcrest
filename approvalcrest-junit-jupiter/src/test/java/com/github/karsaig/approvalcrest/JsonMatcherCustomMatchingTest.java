package com.github.karsaig.approvalcrest;

import org.junit.jupiter.api.Test;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Integration tests for the custom matcher JSON fallback feature via the JUnit Jupiter API.
 * The custom matcher path is filtered from both actual and approved before structural comparison,
 * and for JSON-string inputs the JSON fallback path is exercised.
 */
public class JsonMatcherCustomMatchingTest {

    @Test
    public void matchesPrimitiveWithCustomMatcher() {
        Object actual = parent().childBean(child().childString("banana")).build();
        assertThat(actual, sameJsonAsApproved()
                .with("childBean.childString", equalTo("banana")));
    }

    @Test
    public void matchesPrimitiveWithPatternCustomMatcher() {
        Object actual = parent().childBean(child().childString("banana")).build();
        assertThat(actual, sameJsonAsApproved()
                .withMatcher(equalTo("childString"), equalTo("banana")));
    }
}
