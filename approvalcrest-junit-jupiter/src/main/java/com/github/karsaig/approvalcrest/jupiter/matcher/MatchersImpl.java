package com.github.karsaig.approvalcrest.jupiter.matcher;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.TestInfo;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.jupiter.Junit5InfoBasedTestMeta;
import com.github.karsaig.approvalcrest.jupiter.JunitJupiterTestMeta;
import com.github.karsaig.approvalcrest.matcher.ContentMatcher;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;

public class MatchersImpl {

    private static final MatcherFactory MATCHER_FACTORY = new MatcherFactory();

    protected TestMetaInformation getTestMetaInformation() {
        return new JunitJupiterTestMeta();
    }

    protected Junit5InfoBasedTestMeta getTestMetaInformation(TestInfo testInfo) {
        return new Junit5InfoBasedTestMeta(testInfo);
    }

    public <T> DiagnosingCustomisableMatcher<T> sameBeanAs(T expected) {
        return MATCHER_FACTORY.beanMatcher(expected);
    }

    public <T> JsonMatcher<T> sameJsonAsApproved() {
        return Matchers.sameJsonAsApproved(getTestMetaInformation());
    }

    public <T> JsonMatcher<T> sameJsonAsApproved(TestMetaInformation testMetaInformation) {
        return MATCHER_FACTORY.jsonMatcher(testMetaInformation, new FileMatcherConfig());
    }

    public <T> JsonMatcher<T> sameJsonAsApproved(TestInfo testInfo) {
        return getUniqueIndex(testInfo)
                .map(s -> MATCHER_FACTORY.<T>jsonMatcher(getTestMetaInformation(testInfo), new FileMatcherConfig()).withUniqueId(s))
                .orElse(MATCHER_FACTORY.jsonMatcher(getTestMetaInformation(testInfo), new FileMatcherConfig()));
    }

    public <T> ContentMatcher<T> sameContentAsApproved() {
        return Matchers.sameContentAsApproved(getTestMetaInformation());
    }

    public <T> ContentMatcher<T> sameContentAsApproved(TestMetaInformation testMetaInformation) {
        return MATCHER_FACTORY.contentMatcher(testMetaInformation, new FileMatcherConfig());
    }

    public <T> ContentMatcher<T> sameContentAsApproved(TestInfo testInfo) {
        return getUniqueIndex(testInfo)
                .map(s -> MATCHER_FACTORY.<T>contentMatcher(getTestMetaInformation(testInfo), new FileMatcherConfig()).withUniqueId(s))
                .orElse(MATCHER_FACTORY.contentMatcher(getTestMetaInformation(testInfo), new FileMatcherConfig()));
    }

    private static final Pattern TEST_INDEX_MATCHER = Pattern.compile("^\\[(\\d+)].*");

    private static Optional<String> getUniqueIndex(TestInfo testInfo) {
        Matcher m = TEST_INDEX_MATCHER.matcher(testInfo.getDisplayName());
        if (m.matches()) {
            return Optional.of(m.group(1));
        }
        return Optional.empty();
    }
}
