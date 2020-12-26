package com.github.karsaig.approvalcrest.matcher;

import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;

import com.github.karsaig.approvalcrest.FileMatcherConfig;

public class MatcherFactory {

    protected <T> DiagnosingCustomisableMatcher<T> beanMatcher(T expected) {
        if (expected == null) {
            return new NullMatcher<>(expected);
        }

        if (isPrimitiveOrWrapper(expected.getClass()) || expected.getClass() == String.class
                || expected.getClass().isEnum()) {
            return new IsEqualMatcher<>(expected);
        }

        return new DiagnosingCustomisableMatcher<>(expected);
    }

    protected <T> JsonMatcher<T> jsonMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        return new JsonMatcher<>(testMetaInformation, fileMatcherConfig);
    }

    protected <T> ContentMatcher<T> contentMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        return new ContentMatcher<>(testMetaInformation, fileMatcherConfig);
    }
}
