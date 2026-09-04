package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;
import com.github.karsaig.approvalcrest.matcher.ignores.AbstractJsonMatcherIgnoreTest;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

/**
 * {@code alsoCheck} and {@code alsoCheckMatching} on {@code sameJsonAsApproved}.
 *
 * <p>The file matcher is where the two modes differ most visibly: under {@code with(...)} the field never reaches
 * the approved file at all, so a value that drifts cannot fail. Under {@code alsoCheck} it is written and compared
 * as usual and the matcher runs on top. Each case here therefore pins the file content as well as the verdict.
 *
 * <p>Ordering matchers are written against {@code 0L} rather than {@code 0}: a whole number arrives from the
 * serialised JSON as a {@code Long}, which is the same in both modes and documented in custom-matching.md.
 */
public class JsonMatcherAlsoCheckTest extends AbstractJsonMatcherIgnoreTest {

    /** Three list entries per level, inserted neither in sorted nor in reversed order. */
    private static ParentBean.Builder threeChildren() {
        return parent()
                .addToChildBeanList(child().childString("L1-c").childInteger(3))
                .addToChildBeanList(child().childString("L1-a").childInteger(1))
                .addToChildBeanList(child().childString("L1-b").childInteger(2));
    }

    private static final String THREE_CHILDREN_JSON = "{\n" +
            "  \"childBean\": null,\n" +
            "  \"childBeanList\": [\n" +
            "    {\n" +
            "      \"childInteger\": 3,\n" +
            "      \"childString\": \"L1-c\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"childInteger\": 1,\n" +
            "      \"childString\": \"L1-a\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"childInteger\": 2,\n" +
            "      \"childString\": \"L1-b\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"childBeanMap\": [],\n" +
            "  \"parentString\": null\n" +
            "}";

    /** A Map field is written as an array of single-key objects, entries sorted by key. */
    private static final String MAP_OF_THREE_JSON = "{\n" +
            "  \"childBean\": null,\n" +
            "  \"childBeanList\": [],\n" +
            "  \"childBeanMap\": [\n" +
            "    {\n" +
            "      \"k-a\": {\n" +
            "        \"childInteger\": 0,\n" +
            "        \"childString\": \"L2-a\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"k-b\": {\n" +
            "        \"childInteger\": 0,\n" +
            "        \"childString\": \"L2-b\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"k-c\": {\n" +
            "        \"childInteger\": 0,\n" +
            "        \"childString\": \"L2-c\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"parentString\": null\n" +
            "}";

    /** Both forms carry childBean with childString="L1-c", childInteger=7. */
    public static Object[][] childBeanInputs() {
        return new Object[][]{
                {"Object input", parent().childBean(child().childString("L1-c").childInteger(7)).build()},
                {"Json string input", CHILD_BEAN_JSON}
        };
    }

    private static final String CHILD_BEAN_JSON = "{\n" +
            "  \"childBean\": {\n" +
            "    \"childInteger\": 7,\n" +
            "    \"childString\": \"L1-c\"\n" +
            "  },\n" +
            "  \"childBeanList\": [],\n" +
            "  \"childBeanMap\": [],\n" +
            "  \"parentString\": null\n" +
            "}";

    // ------------------------------------------- the discriminating behaviour

    /**
     * The point of the feature: the field is written to the file that gets approved. The matcher has to fail on
     * a second run too, otherwise this passes just as well when alsoCheck does nothing at all -- a no-op also
     * leaves the field in the file.
     */
    @Test
    public void keepsTheFieldInTheGeneratedNotApprovedFile() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(),
                CHILD_BEAN_JSON,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L)));

        // the same registration is live, not inert: a matcher that cannot hold fails the assertion
        assertJsonMatcherWithDummyTestInfo(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(), CHILD_BEAN_JSON,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(1000L)),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childBean.childInteger"), err.getMessage()),
                AssertionError.class);
    }

    /** actual == null takes its own branch through the evaluation, and it was changed by the boxing fix. */
    @Test
    public void reportsTheMatcherWhenActualIsNull() {
        assertJsonMatcherWithDummyTestInfo(null, CHILD_BEAN_JSON,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L)),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childBean.childInteger"), err.getMessage()),
                AssertionError.class);
    }

    /** Contrast: {@code with} strips it, so the value is never recorded and can never fail later. */
    @Test
    public void withStripsTheFieldFromTheGeneratedNotApprovedFile() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(),
                "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childString\": \"L1-c\"\n" +
                        "  },\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}",
                m -> m.with("childBean.childInteger", greaterThan(0L)));
    }

    // ------------------------------------------------------ verdict against a file

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void passesWhenTheFieldMatchesTheApprovedFileAndTheMatcher(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, CHILD_BEAN_JSON,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L)), null);
    }

    /**
     * A matcher that holds does not rescue a value that drifted from the approved file -- the whole difference
     * from {@code with}, which passes on exactly this input.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void failsWhenTheValueDiffersFromTheApprovedFileEvenThoughTheMatcherHolds(String testName, Object input) {
        String approvedWithDifferentInteger = CHILD_BEAN_JSON.replace("\"childInteger\": 7", "\"childInteger\": 9");

        assertJsonMatcherWithDummyTestInfo(input, approvedWithDifferentInteger,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L)),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childInteger"), err.getMessage()),
                AssertionError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void theSameInputPassesUnderWithBecauseTheFieldIsRemovedFromBothSides(String testName, Object input) {
        String approvedWithoutInteger = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"L1-c\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedWithoutInteger,
                m -> m.with("childBean.childInteger", greaterThan(0L)), null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void failsWhenTheMatcherDoesNotHoldEvenThoughTheFileMatches(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, CHILD_BEAN_JSON,
                m -> m.alsoCheck("childBean.childString", startsWith("nope")),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childBean.childString was \"L1-c\""), err.getMessage()),
                AssertionError.class);
    }

    /**
     * The migration case. An approved file generated while the call was {@code with} has never held the field,
     * so switching to {@code alsoCheck} fails under strict matching until the file is regenerated.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void strictModeFailsWhenTheFieldIsAbsentFromAnApprovedFileWrittenUnderWith(String testName, Object input) {
        String approvedWithoutInteger = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"L1-c\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedWithoutInteger, getDefaultFileMatcherConfig(),
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L)),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childInteger"), err.getMessage()),
                AssertionError.class);
    }

    /**
     * Under lenient matching both sides are filtered, so a passing case cannot tell the modes apart -- with
     * would strip the field from both and pass too. Drifting the approved value is what discriminates.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void lenientMatchingStillComparesTheFieldSoADriftedValueFails(String testName, Object input) {
        String approvedWithDifferentInteger = CHILD_BEAN_JSON.replace("\"childInteger\": 7", "\"childInteger\": 9");

        assertJsonMatcherWithDummyTestInfo(input, approvedWithDifferentInteger,
                getDefaultFileMatcherConfigWithLenientMatching(),
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L)),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childInteger"), err.getMessage()),
                AssertionError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void lenientMatchingPassesWhenTheFieldAgrees(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, CHILD_BEAN_JSON, getDefaultFileMatcherConfigWithLenientMatching(),
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L)), (String) null);
    }

    // ------------------------------------------------------------- precedence

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void lastRegistrationWinsWhenAlsoCheckFollowsWith(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, CHILD_BEAN_JSON,
                m -> m.with("childBean.childInteger", greaterThan(0L))
                        .alsoCheck("childBean.childInteger", greaterThan(0L)),
                (String) null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void lastRegistrationWinsWhenWithFollowsAlsoCheck(String testName, Object input) {
        String approvedWithoutInteger = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"L1-c\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedWithoutInteger,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L))
                        .with("childBean.childInteger", greaterThan(0L)),
                (String) null);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("childBeanInputs")
    public void explicitIgnoringStillRemovesTheField(String testName, Object input) {
        String approvedWithoutInteger = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"L1-c\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedWithoutInteger,
                m -> m.alsoCheck("childBean.childInteger", greaterThan(0L))
                        .ignoring("childBean.childInteger"),
                (String) null);
    }

    // ------------------------------------------------------- value shapes

    @Test
    public void matchesStringFieldWithStartsWith() {
        assertJsonMatcherWithDummyTestInfo(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(), CHILD_BEAN_JSON,
                m -> m.alsoCheck("childBean.childString", startsWith("L1-")), (String) null);
    }

    @Test
    public void matchesNullFieldWithNullValueMatcher() {
        assertJsonMatcherWithDummyTestInfo(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(), CHILD_BEAN_JSON,
                m -> m.alsoCheck("parentString", nullValue()), (String) null);
    }

    @Test
    public void matchesCollectionSizeWithHasSize() {
        assertJsonMatcherWithDummyTestInfo(threeChildren().build(), THREE_CHILDREN_JSON,
                m -> m.alsoCheck("childBeanList", hasSize(3)), (String) null);
    }

    @Test
    public void matchesCollectionSizeWithIterableWithSize() {
        assertJsonMatcherWithDummyTestInfo(threeChildren().build(), THREE_CHILDREN_JSON,
                m -> m.alsoCheck("childBeanList", iterableWithSize(3)), (String) null);
    }

    @Test
    public void matchesNegatedEmptyOnNonEmptyCollection() {
        assertJsonMatcherWithDummyTestInfo(threeChildren().build(), THREE_CHILDREN_JSON,
                m -> m.alsoCheck("childBeanList", not(empty())), (String) null);
    }

    /** A size matcher that holds cannot rescue a list whose contents drifted, unlike under {@code with}. */
    @Test
    public void collectionContentIsStillComparedUnlikeUnderWith() {
        String approvedWithDifferentEntry = THREE_CHILDREN_JSON.replace("\"L1-b\"", "\"L1-zzz\"");

        assertJsonMatcherWithDummyTestInfo(threeChildren().build(), approvedWithDifferentEntry,
                m -> m.alsoCheck("childBeanList", hasSize(3)),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childBeanList[2].childString")
                                && err.getMessage().contains("L1-zzz")
                                && err.getMessage().contains("L1-b"),
                        err.getMessage()),
                AssertionError.class);

    }

    @Test
    public void matchesPathThroughCollectionFanningOutOverElements() {
        assertJsonMatcherWithDummyTestInfo(threeChildren().build(), THREE_CHILDREN_JSON,
                m -> m.alsoCheck("childBeanList.childString", startsWith("L1-")), (String) null);
    }

    @Test
    public void failsWhenCollectionSizeDiffersFromHasSize() {
        assertJsonMatcherWithDummyTestInfo(threeChildren().build(), THREE_CHILDREN_JSON,
                m -> m.alsoCheck("childBeanList", hasSize(2)),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childBeanList"), err.getMessage()),
                AssertionError.class);
    }

    /** A Map-typed field is held under a marker-prefixed name; alsoCheck leaves it in the compared output. */
    @Test
    public void matchesMapSizeWithAMapWithSizeOnObjectInput() {
        ParentBean input = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-a"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input,
                MAP_OF_THREE_JSON,
                m -> m.alsoCheck("childBeanMap", aMapWithSize(3)));
    }

    @Test
    public void matchesPropertyOfEveryMapValueWithAWildcard() {
        ParentBean input = parent()
                .putToChildBeanMap("k-c", child().childString("L2-c"))
                .putToChildBeanMap("k-a", child().childString("L2-a"))
                .putToChildBeanMap("k-b", child().childString("L2-b")).build();

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input,
                MAP_OF_THREE_JSON,
                m -> m.alsoCheck("childBeanMap.*.childString", startsWith("L2-")));
    }

    // ------------------------------------------------------------ pattern form

    @Test
    public void alsoCheckMatchingKeepsTheMatchedFieldsInTheFile() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(),
                CHILD_BEAN_JSON,
                m -> m.alsoCheckMatching(containsString("childString"), startsWith("L1-")));
    }

    @Test
    public void withMatcherStripsTheMatchedFieldsFromTheFile() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(),
                "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childInteger\": 7\n" +
                        "  },\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}",
                m -> m.withMatcher(containsString("childString"), startsWith("L1-")));
    }

    @Test
    public void alsoCheckMatchingPassesVacuouslyWhenNoFieldMatches() {
        assertJsonMatcherWithDummyTestInfo(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(), CHILD_BEAN_JSON,
                m -> m.alsoCheckMatching(containsString("noSuchName"), equalTo("whatever")), (String) null);
    }

    @Test
    public void alsoCheckMatchingFailsWhenAMatchedFieldValueDoesNotMatch() {
        assertJsonMatcherWithDummyTestInfo(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(), CHILD_BEAN_JSON,
                m -> m.alsoCheckMatching(containsString("childString"), startsWith("nope")),
                (AssertionError err) -> Assertions.assertTrue(
                        err.getMessage().contains("childString"), err.getMessage()),
                AssertionError.class);
    }

    /** The documented asymmetry: the pattern form cannot take back a {@code withMatcher} registration. */
    @Test
    public void alsoCheckMatchingCannotUndoAnEarlierWithMatcher() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(),
                "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childInteger\": 7\n" +
                        "  },\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": [],\n" +
                        "  \"parentString\": null\n" +
                        "}",
                m -> m.withMatcher(containsString("childString"), startsWith("L1-"))
                        .alsoCheckMatching(containsString("childString"), startsWith("L1-")));
    }

    // ------------------------------------------------------- more value shapes

    @Test
    public void matchesSetContentKeepingTheSetInTheFile() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(
                new SetHolder(),
                "{\n" +
                        "  \"values\": [\n" +
                        "    \"L1-a\",\n" +
                        "    \"L1-b\",\n" +
                        "    \"L1-c\"\n" +
                        "  ]\n" +
                        "}",
                m -> m.alsoCheck("values", containsInAnyOrder("L1-a", "L1-b", "L1-c")));
    }

    @Test
    public void throwsIllegalArgumentExceptionWhenFieldPathDoesNotExist() {
        assertJsonMatcherWithDummyTestInfo(
                parent().childBean(child().childString("L1-c").childInteger(7)).build(), CHILD_BEAN_JSON,
                m -> m.alsoCheck("noSuchField", notNullValue()),
                (IllegalArgumentException e) -> Assertions.assertEquals("noSuchField does not exist", e.getMessage()),
                IllegalArgumentException.class);
    }

    /**
     * The JsonMatcher route through describeCustomMatchers. Only the bean route was covered before, and the
     * "and also" wording is a real behaviour change here -- describeTo never printed it.
     */
    @Test
    public void describeToDistinguishesTheTwoModesOnTheFileMatcher() {
        inMemoryUnixFs(imfsi -> {
            Path jsonDir = imfsi.getTestPath().resolve("4ac405");
            // childString is stripped by the replacing matcher, so the approved side must not hold it
            writeApproved(jsonDir, "{\n" +
                    "  \"childBean\": {\n" +
                    "    \"childInteger\": 7\n" +
                    "  },\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": [],\n" +
                    "  \"parentString\": null\n" +
                    "}");

            JsonMatcher<Object> matcher = MATCHER_FACTORY
                    .jsonMatcher(dummyInformation(imfsi), getDefaultFileMatcherConfig());
            matcher.with("childBean.childString", startsWith("L1-"))
                    .alsoCheck("childBean.childInteger", greaterThan(0L));
            assertThat(parent().childBean(child().childString("L1-c").childInteger(7)).build(), matcher);

            org.hamcrest.StringDescription description = new org.hamcrest.StringDescription();
            matcher.describeTo(description);
            String text = description.toString();

            Assertions.assertTrue(text.contains("\nand childBean.childString a string starting with \"L1-\""), text);
            Assertions.assertTrue(text.contains("\nand also childBean.childInteger a value greater than <0L>"), text);
        });
    }

    /**
     * describeTo reads the registration, so it recognises ignoring(path) alongside the replacing mode. A field
     * removed by an explicit ignore must not claim to be additionally checked.
     */
    @Test
    public void describeToDoesNotClaimAnExplicitlyIgnoredFieldIsStillCompared() {
        inMemoryUnixFs(imfsi -> {
            Path jsonDir = imfsi.getTestPath().resolve("4ac405");
            // parentString is removed by the explicit ignore, so the approved side must not hold it
            writeApproved(jsonDir, "{\n" +
                    "  \"childBean\": {\n" +
                    "    \"childInteger\": 7,\n" +
                    "    \"childString\": \"L1-c\"\n" +
                    "  },\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}");

            JsonMatcher<Object> matcher = MATCHER_FACTORY
                    .jsonMatcher(dummyInformation(imfsi), getDefaultFileMatcherConfig());
            matcher.alsoCheck("parentString", nullValue()).ignoring("parentString");
            assertThat(parent().childBean(child().childString("L1-c").childInteger(7)).build(), matcher);

            org.hamcrest.StringDescription description = new org.hamcrest.StringDescription();
            matcher.describeTo(description);
            String text = description.toString();

            Assertions.assertTrue(text.contains("\nand parentString null"), text);
            Assertions.assertFalse(text.contains("and also parentString"), text);
        });
    }

    /** alsoCheckMatching over a Set-typed field, which is held under a marker-prefixed name. */
    @Test
    public void alsoCheckMatchingReachesASetTypedFieldAndKeepsIt() {
        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(
                new SetHolder(),
                "{\n" +
                        "  \"values\": [\n" +
                        "    \"L1-a\",\n" +
                        "    \"L1-b\",\n" +
                        "    \"L1-c\"\n" +
                        "  ]\n" +
                        "}",
                m -> m.alsoCheckMatching(containsString("values"), iterableWithSize(3)));
    }

    /** The regeneration route the docs and CHANGELOG point at, with the matcher passing. */
    @Test
    public void inPlaceOverwriteRegeneratesAnApprovedFileWrittenUnderWith() {
        String approvedWithoutInteger = "{\n" +
                "  \"childBean\": {\n" +
                "    \"childString\": \"L1-c\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";

        inMemoryUnixFs(imfsi -> {
            Path jsonDir = imfsi.getTestPath().resolve("4ac405");
            writeApproved(jsonDir, approvedWithoutInteger);

            JsonMatcher<Object> matcher = MATCHER_FACTORY
                    .jsonMatcher(dummyInformation(imfsi), enableInPlaceOverwrite());
            matcher.alsoCheck("childBean.childInteger", greaterThan(0L));

            assertThat(parent().childBean(child().childString("L1-c").childInteger(7)).build(), matcher);

            Assertions.assertIterableEquals(
                    java.util.Collections.singletonList(new InMemoryFiles("4ac405/11b2ef-approved.json",
                            "/*dummyTestClassName.dummyTestMethodName*/\n" + CHILD_BEAN_JSON)),
                    getFiles(imfsi));
        });
    }

    private void writeApproved(Path dir, String content) {
        try {
            java.nio.file.Files.createDirectories(dir);
            java.nio.file.Files.write(dir.resolve("11b2ef-approved.json"),
                    content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** A Set field, to exercise the marker-prefixed name and the automatic sort. */
    static class SetHolder {
        java.util.Set<String> values = new java.util.LinkedHashSet<>(
                java.util.Arrays.asList("L1-c", "L1-a", "L1-b"));
    }
}
