package com.github.karsaig.approvalcrest;

import java.util.function.BiConsumer;

import org.hamcrest.Matcher;

import com.github.karsaig.approvalcrest.matcher.AssertImpl;

public class TestAssertImpl extends AssertImpl {

    @Override
    public <T> void assertThat(String reason, T actual, Matcher<? super T> matcher, BiConsumer<String, ComparisonDescription> failureHandler) {
        super.assertThat(reason, actual, matcher, failureHandler);
    }


}
