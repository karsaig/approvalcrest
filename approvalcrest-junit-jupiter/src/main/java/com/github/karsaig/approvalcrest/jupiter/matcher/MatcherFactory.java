package com.github.karsaig.approvalcrest.jupiter.matcher;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.ContentMatcher;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;

class MatcherFactory extends com.github.karsaig.approvalcrest.matcher.MatcherFactory {

    @Override
    protected <T> DiagnosingCustomisableMatcher<T> beanMatcher(T expected) {
        return super.beanMatcher(expected);
    }

    @Override
    protected <T> JsonMatcher<T> jsonMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        return super.jsonMatcher(testMetaInformation, fileMatcherConfig);
    }

    @Override
    protected <T> ContentMatcher<T> contentMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        return super.contentMatcher(testMetaInformation, fileMatcherConfig);
    }
}
