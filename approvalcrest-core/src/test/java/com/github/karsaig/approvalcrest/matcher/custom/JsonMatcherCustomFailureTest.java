package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.ignores.AbstractJsonMatcherIgnoreTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static com.github.karsaig.approvalcrest.matchers.ChildBeanMatchers.childStringEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
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
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
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
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
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
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
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
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
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
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
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
    // Null actual object
    // -----------------------------------------------------------------------

    @Test
    public void failsWhenActualIsNull() {
        // When actual is null, the custom matcher sees null as the value for the path.
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(null, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("banana")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("null"),
                        "Expected null-related mismatch message, was: " + error.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Null parent bean in path — JSON-string input
    // -----------------------------------------------------------------------

    @Test
    public void doesNotIncludeParentBeanFromFieldPathForJsonStringInput() {
        // Same scenario as doesNotIncludeParentBeanFromFieldPath but using a JSON string
        // as the actual input.  childBean is null in the JSON; the path childBean.childString
        // cannot be resolved and the mismatch should mention "childBean".
        String input = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("apple")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childBean"),
                        "Expected childBean-related message, was: " + error.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Empty collection path
    // -----------------------------------------------------------------------

    public static Object[][] emptyParentInputs() {
        return new Object[][]{
                {"Object input", parent().build()},
                {"Json string input", "{\n  \"childBeanList\": [],\n  \"childBeanMap\": []\n}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("emptyParentInputs")
    public void failsWhenPathThroughEmptyCollectionIsUsed(String testName, Object input) {
        // When childBeanList is empty there are no values at childBeanList.childString to
        // validate against.  The custom matcher must NOT silently pass (vacuous truth).
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList.childString", equalTo("apple")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childBeanList.childString"),
                        "Expected mismatch mentioning the path, was: " + error.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Two custom matchers both failing — only first reported
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void reportsOnlyFirstFailureWhenBothMatchersFail(String testName, Object input) {
        // Both matchers fail (childString is "banana", not "kiwi"; childInteger is 0, not 99).
        // Only the first failure (HashMap iteration order) should appear in the mismatch section.
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher
                        .with("childBean.childString", equalTo("kiwi"))
                        .with("childBean.childInteger", equalTo(99L)),
                error -> {
                    String msg = error.getMessage();
                    String mismatchSection = msg.contains("but:") ? msg.substring(msg.lastIndexOf("but:")) : msg;
                    Assertions.assertFalse(
                            mismatchSection.contains("childBean.childString") && mismatchSection.contains("childBean.childInteger"),
                            "Only first failure should be reported in mismatch section, was: " + msg);
                },
                AssertionError.class);
    }



    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void throwsIllegalArgumentExceptionWhenFieldPathDoesNotExist(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
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
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("apple")),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBean.childString was null"),
                        "Expected parent-null message, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Known limitation: below a null, a path that names nothing cannot fail
    //
    // These assert a pass, and that is the point. A path resolves against the object first and is
    // retried against the serialised JSON, because that retry is the only thing that reaches a map
    // entry addressed by its key, a path through an array, a member whose serialised name differs
    // from the field's, and anything at all when the input is a raw JSON string. A null in parsed
    // JSON carries no type, so the retry answers null for every path below it and nullValue()
    // accepts that. A path left behind by a rename asserts nothing, as long as some reference along
    // it is null.
    //
    // Rejecting such a path by checking the field's DECLARED TYPE for the next segment was tried and
    // reverted as unsound: the retry resolves serialised MEMBER names, and those are not Java field
    // names. @SerializedName renames a member, a registered JsonSerializer invents them freely, a
    // JsonObject-typed field's members are data, a bounded type variable erases to its bound rather
    // than to Object, and a field declared as a supertype legitimately carries a subtype's fields.
    // Each makes "does not exist" a false statement about a path that resolves for real data.
    // Throwing also escapes not(...), so a negated matcher could no longer express "does not match".
    // -----------------------------------------------------------------------

    @Test
    public void aPathNamingNothingBelowANullCannotFail() {
        // childBean is null and ChildBean declares no "nonExistingField".
        Object input = parent().build();
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.nonExistingField", nullValue()), null);
    }

    @Test
    public void aPathNamingNothingBelowANullCannotFailForJsonStringInputEither() {
        // childBean itself stays in the comparison: only the segment below it is stripped, and it
        // was never there.
        String input = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, input, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBean.nonExistingField", nullValue()), null);
    }

    // -----------------------------------------------------------------------
    // Null field value
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputsWithNullChildBean")
    public void failsWhenNullFieldMatchedWithNotNullMatcher(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
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
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.with("childBean.childString", equalTo("banana")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childString"),
                        "Expected 'childString' in error but was: " + error.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // with(Matcher<String>, Matcher<V>) — pattern-based failures
    // -----------------------------------------------------------------------

    /** Object + JSON-string inputs where multiple "childString" fields are present. */
    public static Object[][] patternMultipleFieldInputs() {
        return new Object[][]{
                {"Object input", parent()
                        .childBean(child().childString("banana"))
                        .addToChildBeanList(child().childString("banana"))
                        .build()},
                {"Json string input", "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childInteger\": 0,\n" +
                        "    \"childString\": \"banana\"\n" +
                        "  },\n" +
                        "  \"childBeanList\": [\n" +
                        "    {\n" +
                        "      \"childInteger\": 0,\n" +
                        "      \"childString\": \"banana\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void patternMatcherFailsWhenMatchedFieldValueDoesNotMatch(String testName, Object input) {
        // Pattern "childString" matches childBean.childString; actual value "banana" does not match "kiwi".
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.withMatcher(equalTo("childString"), equalTo("kiwi")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("was \"banana\""),
                        "Expected mismatch mentioning actual value, was: " + error.getMessage()),
                AssertionError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("patternMultipleFieldInputs")
    public void patternMatcherFailsOnFirstNonMatchingFieldWhenMultipleMatch(String testName, Object input) {
        // Pattern "childString" matches both childBean.childString and childBeanList[0].childString.
        // First matched value "banana" does not match "kiwi" → fails immediately.
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [\n" +
                "    {\n" +
                "      \"childInteger\": 0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.withMatcher(equalTo("childString"), equalTo("kiwi")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("was \"banana\""),
                        "Expected mismatch on matched field value, was: " + error.getMessage()),
                AssertionError.class);
    }

    @Test
    public void patternMatcherFailsWhenExplicitNullFieldMatchedWithNotNullMatcher() {
        // JSON-string input with childBean: null — the field IS present in the JSON element as
        // JsonNull, so collectValuesByFieldNamePattern finds it. notNullValue() then fails.
        // (For Object inputs, Gson omits null fields from serialisation, so the pattern would find
        // nothing and pass vacuously — this is tested in BeanMatcherCustomSuccessTest.)
        String input = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.withMatcher(equalTo("childBean"), notNullValue()),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("was null"),
                        "Expected null mismatch in message, was: " + error.getMessage()),
                AssertionError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("customMatcherInputs")
    public void strictModeFailsWhenPatternMatcherFieldPresentInApprovedFile(String testName, Object input) {
        // In strict mode the approved file is taken as-is: filterByCustomMatcherPatterns is skipped
        // on expected. The actual still has childString stripped. So expected retains childString but
        // actual does not → structural diff → fails.
        // This documents that approved files must not contain fields handled by withMatcher(), just
        // as they must not contain fields handled by the path-based with().
        String approvedFileContent = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"banana\",\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.withMatcher(equalTo("childString"), equalTo("banana")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childString"),
                        "Expected 'childString' in error but was: " + error.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Null element inside fanned-out collection — failure side
    // -----------------------------------------------------------------------

    /** Object + JSON-string inputs: childBeanList = [null, child("apple")]. */
    public static Object[][] nullElementCollectionFailureInputs() {
        return new Object[][]{
                {"Object input", parent()
                        .addToChildBeanList((com.github.karsaig.approvalcrest.testdata.ChildBean) null)
                        .addToChildBeanList(child().childString("apple"))
                        .build()},
                {"Json string input",
                        "{\n" +
                        "  \"childBean\": null,\n" +
                        "  \"childBeanList\": [null, {\"childString\": \"apple\", \"childInteger\": 0}],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("nullElementCollectionFailureInputs")
    public void failsWhenNullElementInFanoutCollectionAndMatcherDoesNotAcceptNull(String testName, Object input) {
        // Fan-out over [null, child("apple")] collects [null, "apple"] for childBeanList.childString.
        // equalTo("apple") fails on null → overall match fails; approved file retains [null, {childInteger:0}].
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [\n" +
                "    null,\n" +
                "    {\"childInteger\": 0}\n" +
                "  ],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList.childString", equalTo("apple")),
                error -> Assertions.assertTrue(
                        error.getMessage().contains("childBeanList.childString"),
                        "Expected path in failure message, was: " + error.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // Container matcher limits on JSON string input
    // -----------------------------------------------------------------------

    /** A two-element childBeanList supplied as a JSON string, so no bean is available. */
    private static final String TWO_CHILD_BEANS_AS_JSON = "{\n" +
            "  \"childBean\": null,\n" +
            "  \"childBeanList\": [\n" +
            "    {\n" +
            "      \"childInteger\": 0,\n" +
            "      \"childString\": \"apple\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"childInteger\": 0,\n" +
            "      \"childString\": \"banana\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"childBeanMap\": [],\n" +
            "  \"parentString\": null\n" +
            "}";

    private static final String APPROVED_WITHOUT_CHILD_BEAN_LIST = "{\n" +
            "  \"childBean\": null,\n" +
            "  \"childBeanMap\": [],\n" +
            "  \"parentString\": null\n" +
            "}";

    /** hasSize reports the actual size on JSON string input, as it does on a bean. */
    @Test
    public void failsWithSizeMismatchWhenHasSizeIsUsedOnJsonStringInput() {
        assertJsonMatcherWithDummyTestInfo(TWO_CHILD_BEANS_AS_JSON, APPROVED_WITHOUT_CHILD_BEAN_LIST,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", hasSize(3)),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBeanList collection size was <2>"),
                        "Expected size mismatch, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    /**
     * The list view coerces scalar elements but hands objects back as they are, so contains() and
     * containsInAnyOrder() still cannot match element matchers written against the bean type.
     * The container is matchable; an object element carries no type information.
     */
    @Test
    public void failsWhenContainsIsUsedOnJsonStringInput() {
        assertJsonMatcherWithDummyTestInfo(TWO_CHILD_BEANS_AS_JSON, APPROVED_WITHOUT_CHILD_BEAN_LIST,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList",
                        contains(childStringEqualTo("apple"), childStringEqualTo("banana"))),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBeanList"),
                        "Expected path in failure message, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    /** iterableWithSize works on a JsonArray, so it fails on the size — the useful failure. */
    @Test
    public void failsWithSizeMismatchWhenIterableWithSizeIsUsedOnJsonStringInput() {
        assertJsonMatcherWithDummyTestInfo(TWO_CHILD_BEANS_AS_JSON, APPROVED_WITHOUT_CHILD_BEAN_LIST,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", iterableWithSize(3)),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBeanList iterable size was <2>"),
                        "Expected size mismatch, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    /**
     * A Map is traversed by key, not fanned out over: skipping the key reaches nothing, because no
     * entry is called childString. Name the key, use a * segment to mean every value, or target the
     * map itself with hasEntry or aMapWithSize.
     */
    @Test
    public void failsWhenPathDescendsIntoMapValues() {
        Object input = parent().putToChildBeanMap("key", child().childString("apple")).build();
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanMap.childString", equalTo("apple")),
                thrown -> Assertions.assertEquals("childBeanMap.childString does not exist", thrown.getMessage()),
                IllegalArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // Negated container matchers
    // -----------------------------------------------------------------------

    /**
     * not(empty()) on an empty collection must fail. Previously the bean-level rejection was
     * retried against the JsonArray form, where empty() is false on type grounds, making the
     * negation true and the assertion unfailable.
     */
    @Test
    public void failsWhenNegatedEmptyIsFalseForAnEmptyCollection() {
        Object input = parent().build();
        String approvedFileContent = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", not(empty())),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBeanList"),
                        "Expected path in failure message, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    /**
     * not(empty()) on an empty collection must fail for JSON string input too. There is no bean to
     * resolve the path against, so the matcher sees the JSON form; the list view over the JsonArray
     * lets empty() evaluate for real rather than answering false on the type check.
     */
    @Test
    public void failsWhenNegatedEmptyIsFalseForAnEmptyCollectionOnJsonStringInput() {
        String emptyListAsJson = "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(emptyListAsJson, APPROVED_WITHOUT_CHILD_BEAN_LIST,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", not(empty())),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBeanList"),
                        "Expected path in failure message, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    /** not(hasSize(n)) must fail when the collection does have size n. */
    @Test
    public void failsWhenNegatedHasSizeIsFalse() {
        Object input = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("banana"))
                .build();
        assertJsonMatcherWithDummyTestInfo(input, APPROVED_WITHOUT_CHILD_BEAN_LIST,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("childBeanList", not(hasSize(2))),
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("childBeanList"),
                        "Expected path in failure message, was: " + thrown.getMessage()),
                AssertionError.class);
    }

    // -----------------------------------------------------------------------
    // The boundary of the negated-element-matcher limitation: scalar elements
    //
    // Over objects on JSON-string input these three cannot fail, which is pinned in
    // JsonMatcherCustomSuccessTest. Over scalars they fail correctly, because the read-only list
    // view over the JsonArray coerces a JSON scalar on read -- a string arrives as a String. That
    // is what makes the limitation about element classes rather than about the input form, and it
    // is what any future fix must not break.
    // -----------------------------------------------------------------------

    private static final String SCALAR_LIST_JSON = "{\n"
            + "  \"other\": 1,\n"
            + "  \"tags\": [\n"
            + "    \"urgent\",\n"
            + "    \"review\"\n"
            + "  ]\n"
            + "}";

    private static final String SCALAR_LIST_JSON_APPROVED = "{\n"
            + "  \"other\": 1\n"
            + "}";

    @Test
    public void negatedHasItemOverScalarsFailsOnJsonStringInput() {
        assertJsonMatcherWithDummyTestInfo(SCALAR_LIST_JSON, SCALAR_LIST_JSON_APPROVED,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("tags", not(hasItem("urgent"))),
                error -> Assertions.assertTrue(error.getMessage().contains("tags"),
                        "Expected a tags mismatch, was: " + error.getMessage()),
                AssertionError.class);
    }

    @Test
    public void negatedContainsOverScalarsFailsOnJsonStringInput() {
        assertJsonMatcherWithDummyTestInfo(SCALAR_LIST_JSON, SCALAR_LIST_JSON_APPROVED,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("tags", not(contains("urgent", "review"))),
                error -> Assertions.assertTrue(error.getMessage().contains("tags"),
                        "Expected a tags mismatch, was: " + error.getMessage()),
                AssertionError.class);
    }

    @Test
    public void negatedContainsInAnyOrderOverScalarsFailsOnJsonStringInput() {
        assertJsonMatcherWithDummyTestInfo(SCALAR_LIST_JSON, SCALAR_LIST_JSON_APPROVED,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("tags", not(containsInAnyOrder("review", "urgent"))),
                error -> Assertions.assertTrue(error.getMessage().contains("tags"),
                        "Expected a tags mismatch, was: " + error.getMessage()),
                AssertionError.class);
    }

    @Test
    public void negatedHasSizeFailsOnJsonStringInput() {
        // Claimed in the CHANGELOG and the docs for both input forms; only object input was covered.
        assertJsonMatcherWithDummyTestInfo(SCALAR_LIST_JSON, SCALAR_LIST_JSON_APPROVED,
                enableExpectedFileSortingWithLenientMatching(),
                jsonMatcher -> jsonMatcher.with("tags", not(hasSize(2))),
                error -> Assertions.assertTrue(error.getMessage().contains("tags"),
                        "Expected a tags mismatch, was: " + error.getMessage()),
                AssertionError.class);
    }
}
