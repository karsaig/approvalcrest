package com.github.karsaig.approvalcrest.jupiter.matcher;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.jupiter.JunitJupiterTestMeta;
import com.github.karsaig.approvalcrest.matcher.ContentMatcher;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.IsEqualMatcher;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.matcher.NullMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;
import org.junit.jupiter.api.TestInfo;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;

public class MatchersImpl {
    protected TestMetaInformation getTestMetaInformation() {
        return new JunitJupiterTestMeta();
    }

    protected JunitJupiterTestMeta getTestMetaInformation(TestInfo testInfo) {
        return new JunitJupiterTestMeta(testInfo);
    }

    public <T> DiagnosingCustomisableMatcher<T> _sameBeanAs(T expected) {
        if (expected == null) {
            return new NullMatcher<>(expected);
        }

        if (isPrimitiveOrWrapper(expected.getClass()) || expected.getClass() == String.class
                || expected.getClass().isEnum()) {
            return new IsEqualMatcher<>(expected);
        }

        return new DiagnosingCustomisableMatcher<>(expected);
    }

    public <T> JsonMatcher<T> _sameJsonAsApproved() {
        return Matchers.sameJsonAsApproved(getTestMetaInformation());
    }

    public <T> JsonMatcher<T> _sameJsonAsApproved(TestMetaInformation testMetaInformation) {
        return new JsonMatcher<>(testMetaInformation, new FileMatcherConfig());
    }

    public <T> JsonMatcher<T> _sameJsonAsApproved(TestInfo testInfo) {
        return getUniqueIndex(testInfo)
                .map(s -> new JsonMatcher<T>(getTestMetaInformation(testInfo), new FileMatcherConfig()).withUniqueId(s))
                .orElse(new JsonMatcher<>(getTestMetaInformation(testInfo), new FileMatcherConfig()));
    }

    public <T> ContentMatcher<T> _sameContentAsApproved() {
        return Matchers.sameContentAsApproved(getTestMetaInformation());
    }

    public <T> ContentMatcher<T> _sameContentAsApproved(TestMetaInformation testMetaInformation) {
        return new ContentMatcher<>(testMetaInformation, new FileMatcherConfig());
    }

    public <T> ContentMatcher<T> _sameContentAsApproved(TestInfo testInfo) {
        return getUniqueIndex(testInfo)
                .map(s -> new ContentMatcher<T>(getTestMetaInformation(testInfo), new FileMatcherConfig()).withUniqueId(s))
                .orElse(new ContentMatcher<>(getTestMetaInformation(testInfo), new FileMatcherConfig()));
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
