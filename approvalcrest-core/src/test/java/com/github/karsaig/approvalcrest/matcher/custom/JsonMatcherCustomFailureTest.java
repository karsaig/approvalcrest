package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.ignores.AbstractJsonMatcherIgnoreTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;

public class JsonMatcherCustomFailureTest extends AbstractJsonMatcherIgnoreTest {

    public static Object[][] customMatcherInputs() {
        return new Object[][]{
                {"Object input", parent().childBean(child().childString("banana")).build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childInteger\": 0,\n" +
                        "    \"childString\": \"banana\"\n" +
                        "  },\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": []\n" +
                        "}"}
        };
    }

    /** Both inputs have a null childBean (Object omits the key; JSON string uses explicit null). */
    public static Object[][] customMatcherInputsWithNullChildBean() {
        return new Object[][]{
                {"Object input", parent().build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": null,\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": []\n" +
                        "}"}
        };
    }

    // -----------------------------------------------------------------------
    // Primitive string field failures
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void failsOnPrimitiveFieldMismatch(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("kiwi")),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBean.childString was \"banana\""),
                        "Expected mismatch message, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void failsWithContainsStringMatcherOnMismatch(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", containsString("xyz")),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBean.childString"),
                        "Expected mismatch on childBean.childString, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Integer field failure
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void failsOnIntegerFieldMismatch(String testName, Object input) {
        // childInteger is 0; matcher expects 5L — fails for both bean path and JSON fallback
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"banana\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childInteger", equalTo(5L)),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBean.childInteger"),
                        "Expected mismatch on childBean.childInteger, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Multiple matchers — only first failure is reported
    // -----------------------------------------------------------------------

    /**
     * Verifies that all custom matchers are evaluated (not short-circuited), but only the
     * first remaining failure is reported. Matcher 2 passes via JSON fallback so only
     * Matcher 1's failure appears in the error message.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void reportsFirstFailureWhenMultipleMatchersFail(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher
                        .with("childBean.childString", equalTo("kiwi"))
                        .with("childBean.childInteger", equalTo(0L)),
                thrown -> {
                    String msg = thrown.getMessage();
                    Assertions.assertTrue(
                            msg.contains("childBean.childString was \"banana\""),
                            "First matcher failure should be reported, was: " + msg);
                    // "childBean.childInteger" appears in describeTo (expected section);
                    // check only that no mismatch was reported for it.
                    String mismatchSection = msg.contains("but:") ? msg.substring(msg.lastIndexOf("but:")) : msg;
                    Assertions.assertFalse(
                            mismatchSection.contains("childBean.childInteger"),
                            "Second matcher (which passes via JSON fallback) should not appear in mismatch, was: " + msg);
                },
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Non-existing field path
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void throwsIllegalArgumentExceptionWhenFieldPathDoesNotExist(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.nonExistingField", equalTo("kiwi")),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBean.nonExistingField"),
                        "Expected IAE about non-existing field, was: " + thrown.getMessage()),
                IllegalArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // Null parent bean in path
    // -----------------------------------------------------------------------

    @Test
    public void doesNotIncludeParentBeanFromFieldPath() {
        Object input = parent().build(); // childBean is null
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("apple")),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBean is null"),
                        "Expected parent-null message, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Null field value
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithNullChildBean")
    public void failsWhenNullFieldMatchedWithNotNullMatcher(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean", notNullValue()),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBean was null"),
                        "Expected null mismatch, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Custom matcher is what makes the difference (counterpart of success test)
    // -----------------------------------------------------------------------

    /**
     * Without a custom matcher the structural comparison sees "banana" in actual vs "kiwi" in
     * the approved snapshot and fails.  This is the direct counterpart of
     * {@code passesWhenCustomMatcherRescuesValueThatDiffersFromApproved} in
     * {@link JsonMatcherCustomSuccessTest}: it proves that the custom matcher, not some other
     * mechanism, is what makes the difference.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void strictModeFailsWhenCustomMatcherFieldPresentInApprovedFile(String testName, Object input) {
        // .with(path, matcher) internally calls ignoring(path). In strict mode the path is
        // stripped from actual only, so if the approved file still contains the field, the
        // structural comparison sees it in expected but not in actual → fails.
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"banana\",\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("banana")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childString"),
                        "Expected 'childString' in error but was: " + error.getMessage()),
                AssertionError.class);
    }
}
