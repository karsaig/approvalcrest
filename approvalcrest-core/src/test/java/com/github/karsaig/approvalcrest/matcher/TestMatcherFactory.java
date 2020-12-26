package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.FileMatcherConfig;

public class TestMatcherFactory extends MatcherFactory {
    @Override
    public <T> DiagnosingCustomisableMatcher<T> beanMatcher(T expected) {
        return super.beanMatcher(expected);
    }

    @Override
    public <T> JsonMatcher<T> jsonMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        return super.jsonMatcher(testMetaInformation, fileMatcherConfig);
    }

    @Override
    public <T> ContentMatcher<T> contentMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        return super.contentMatcher(testMetaInformation, fileMatcherConfig);
    }
}
