package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.testng.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static org.hamcrest.CoreMatchers.equalTo;

import org.opentest4j.AssertionFailedError;
import org.testng.annotations.Test;

import com.github.karsaig.approvalcrest.testdata.Bean;

/**
 * MatcherAssert tests checking the failure cases
 */
public class MatcherAssertFailureTest {

    @Test(expectedExceptions = AssertionFailedError.class)
    public void throwsAssertionFailedErrorWhenBeansNotMatching() {
        Bean expected = bean().string("value1").integer(1).build();
        Bean actual = bean().string("value2").integer(2).build();

        assertThat(actual, sameBeanAs(expected));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void throwsAssertionErrorWhenNormalMatchersUsed() {
        assertThat("value1", equalTo("value2"));
    }
}
