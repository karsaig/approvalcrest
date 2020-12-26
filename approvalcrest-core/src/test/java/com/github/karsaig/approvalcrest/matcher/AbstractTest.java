package com.github.karsaig.approvalcrest.matcher;

import org.hamcrest.Matcher;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.TestAssertImpl;

public abstract class AbstractTest {

    protected static final TestAssertImpl TEST_ASSERT_IMPl = new TestAssertImpl();

    protected <T> void assertThat(T input, Matcher<? super T> matcher) {
        TEST_ASSERT_IMPl.assertThat(null, input, matcher, (s, cd) -> {
            throw new AssertionFailedError(
                    s,
                    cd.getExpected(),
                    cd.getActual());
        });
    }
}
