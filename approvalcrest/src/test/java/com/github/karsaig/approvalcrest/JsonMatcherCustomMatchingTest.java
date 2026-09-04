package com.github.karsaig.approvalcrest;

import org.junit.Test;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Integration tests for the custom matcher JSON fallback feature via the JUnit 4 API.
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

    /**
     * The additional mode: childString is asserted by the matcher and still compared against the approved file,
     * where {@code with} above would have removed it.
     */
    @Test
    public void alsoChecksPrimitiveWhileStillComparingIt() {
        Object actual = parent().childBean(child().childString("banana")).build();
        assertThat(actual, sameJsonAsApproved()
                .alsoCheck("childBean.childString", equalTo("banana")));
    }


}
