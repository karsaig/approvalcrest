package com.github.karsaig.approvalcrest.testng.matcher;

import java.lang.reflect.Method;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.ContentMatcher;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.IsEqualMatcher;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.matcher.NullMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;
import com.github.karsaig.approvalcrest.testng.TestNgMethodBasedTestMeta;
import com.github.karsaig.approvalcrest.testng.TestNgTestMeta;

import com.google.common.annotations.Beta;

/**
 * Entry point for the matchers available in Approvalcrest for TestNG.
 */
public class Matchers {

    private static final MatcherFactory MATCHER_FACTORY = new MatcherFactory();

    /**
     * Returns a {@link NullMatcher} in case the expectation is null, a
     * {@link IsEqualMatcher} if it's a primitive, String or Enum or a
     * {@link DiagnosingCustomisableMatcher} otherwise.
     *
     * @param expected the expected bean to match against
     * @param <T>      type of actual object
     * @return an {@link DiagnosingCustomisableMatcher} instance
     */
    public static <T> DiagnosingCustomisableMatcher<T> sameBeanAs(T expected) {
        return MATCHER_FACTORY.beanMatcher(expected);
    }

    /**
     * Returns a {@link JsonMatcher} for matching an object with a generated
     * file.
     *
     * @param <T> Type of object to serialize to JSON
     * @return a new {@link JsonMatcher} instance
     */
    public static <T> JsonMatcher<T> sameJsonAsApproved() {
        return sameJsonAsApproved(new TestNgTestMeta());
    }

    /**
     * Returns a {@link JsonMatcher} for matching an object with a generated
     * file.
     * Should be used when the zero-arg version cannot determine the test method
     * (e.g. in private methods or DataProvider-driven tests).
     * TestNG automatically injects {@link Method} if declared as a test method parameter.
     *
     * @param testMethod the test method, injected by TestNG
     * @param <T>        Type of object to serialize to JSON
     * @return a new {@link JsonMatcher} instance
     */
    public static <T> JsonMatcher<T> sameJsonAsApproved(Method testMethod) {
        return sameJsonAsApproved(new TestNgMethodBasedTestMeta(testMethod));
    }

    /**
     * Returns a {@link JsonMatcher} for matching an object with a generated
     * file.
     * Should be used for cases when the default implementation of {@link TestMetaInformation} doesn't work for any reason.
     * <p>
     * <b>!! Beta, as such subject to change !!</b>
     *
     * @param testMetaInformation Information used to generate file names and path to use.
     * @param <T>                 Type of object to serialize to JSON
     * @return a new {@link JsonMatcher} instance
     */
    @Beta
    public static <T> JsonMatcher<T> sameJsonAsApproved(TestMetaInformation testMetaInformation) {
        return MATCHER_FACTORY.jsonMatcher(testMetaInformation, new FileMatcherConfig());
    }

    /**
     * Returns a {@link ContentMatcher} for matching a string with a generated file.
     *
     * @param <T> Only {@link String} is supported at the moment.
     * @return a new {@link ContentMatcher} instance
     */
    public static <T> ContentMatcher<T> sameContentAsApproved() {
        return sameContentAsApproved(new TestNgTestMeta());
    }

    /**
     * Returns a {@link ContentMatcher} for matching a string with a generated file.
     * Should be used when the zero-arg version cannot determine the test method
     * (e.g. in private methods or DataProvider-driven tests).
     * TestNG automatically injects {@link Method} if declared as a test method parameter.
     *
     * @param testMethod the test method, injected by TestNG
     * @param <T>        Only {@link String} is supported at the moment.
     * @return a new {@link ContentMatcher} instance
     */
    public static <T> ContentMatcher<T> sameContentAsApproved(Method testMethod) {
        return sameContentAsApproved(new TestNgMethodBasedTestMeta(testMethod));
    }

    /**
     * Returns a {@link ContentMatcher} for matching a string with a generated file.
     * Should be used for cases when the default implementation of {@link TestMetaInformation} doesn't work for any reason.
     * <p>
     * <b>!! Beta, as such subject to change !!</b>
     *
     * @param testMetaInformation Information used to generate file names and path to use.
     * @param <T>                 Only {@link String} is supported at the moment.
     * @return a new {@link ContentMatcher} instance
     */
    @Beta
    public static <T> ContentMatcher<T> sameContentAsApproved(TestMetaInformation testMetaInformation) {
        return MATCHER_FACTORY.contentMatcher(testMetaInformation, new FileMatcherConfig());
    }
}
