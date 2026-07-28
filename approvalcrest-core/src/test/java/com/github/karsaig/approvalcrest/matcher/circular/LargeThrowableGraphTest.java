package com.github.karsaig.approvalcrest.matcher.circular;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;

import org.junit.jupiter.api.Test;

/**
 * Coverage for throwable graphs large enough that object-graph reference ids would contain the hex
 * letters a-f (ids are generated with {@code Integer.toHexString}, so that starts at the tenth
 * object).
 *
 * <p>Note: these pass both before and after the fix to the reference-id grammar in
 * {@code ThrowableTypeAdapterFactory}. A plain cause chain does not engage the graph adapter, and
 * no input reaching the divergent branch could be constructed from the public API, so the fix
 * addresses a latent mismatch rather than a demonstrated failure.
 */
public class LargeThrowableGraphTest extends AbstractBeanMatcherTest {

    /**
     * Builds a cause chain deep enough to push the graph past nine nodes.
     */
    private static Throwable deeplyNestedThrowable(int depth) {
        Throwable current = new IllegalStateException("innermost");
        for (int i = 0; i < depth; i++) {
            current = new RuntimeException("level " + i, current);
        }
        return current;
    }

    @Test
    public void handlesThrowableGraphWithFewerThanTenObjects() {
        Throwable actual = deeplyNestedThrowable(3);

        assertDiagnosingMatcher(actual, actual);
    }

    @Test
    public void handlesThrowableGraphWithMoreThanNineObjects() {
        Throwable actual = deeplyNestedThrowable(15);

        assertDiagnosingMatcher(actual, actual);
    }

    @Test
    public void handlesThrowableGraphSpanningMultipleHexOrdersOfMagnitude() {
        Throwable actual = deeplyNestedThrowable(40);

        assertDiagnosingMatcher(actual, actual);
    }
}
