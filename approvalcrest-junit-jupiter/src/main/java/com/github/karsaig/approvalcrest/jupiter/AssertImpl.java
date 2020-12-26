package com.github.karsaig.approvalcrest.jupiter;

import java.util.function.BiConsumer;

import org.hamcrest.Matcher;

import com.github.karsaig.approvalcrest.ComparisonDescription;

class AssertImpl extends com.github.karsaig.approvalcrest.AssertImplProxy {

    @Override
    protected <T> void assertThat(String reason, T actual, Matcher<? super T> matcher, BiConsumer<String, ComparisonDescription> failureHandler) {
        super.assertThat(reason, actual, matcher, failureHandler);
    }
}
