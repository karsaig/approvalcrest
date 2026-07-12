package com.github.karsaig.approvalcrest.testng;

import static com.github.karsaig.approvalcrest.testng.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Smoke test that {@code ignoringElementsWhere} is reachable through the TestNG module. The
 * behaviour itself is covered in depth in {@code approvalcrest-core}.
 */
public class IgnoreElementsWhereTest {

    private static final String TRACKING_SYSTEM = "https://tracking.example/flow-id";
    private static final String REAL_SYSTEM = "https://real.example/category";

    @Test
    public void removesArrayElementsMatchingValue() {
        Meta actual = new Meta(Arrays.asList(new Tag(TRACKING_SYSTEM, "flow-1"), new Tag(REAL_SYSTEM, "keep")));
        Meta expected = new Meta(Arrays.asList(new Tag(REAL_SYSTEM, "keep")));

        assertThat(actual, sameBeanAs(expected).ignoringElementsWhere("tag.system", equalTo(TRACKING_SYSTEM)));
        assertThat(actual, sameBeanAs(expected).ignoringElementsWhere("tag.system", TRACKING_SYSTEM));
    }

    @SuppressWarnings("unused")
    private static class Meta {
        private final List<Tag> tag;

        Meta(List<Tag> tag) {
            this.tag = tag;
        }
    }

    @SuppressWarnings("unused")
    private static class Tag {
        private final String system;
        private final String code;

        Tag(String system, String code) {
            this.system = system;
            this.code = code;
        }
    }
}
