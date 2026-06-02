package com.github.karsaig.approvalcrest.matcher.machinereadable;

import com.github.karsaig.approvalcrest.ComparisonDescription;
import com.github.karsaig.approvalcrest.matcher.AbstractTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMatcherFactory;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives.Builder.beanWithPrimitives;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.junit.jupiter.api.Assertions.*;

public class BeanMatcherMachineReadableTest extends AbstractTest {

    private static final TestMatcherFactory MATCHER_FACTORY = new TestMatcherFactory();

    @Test
    public void shouldOutputMachineReadableMessageOnMismatchWhenFluentApiEnabled() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).beanInt(99).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).beanInt(42).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        assertAll(
                () -> assertEquals("MISMATCH", json.get("failureType").getAsString(), "Should contain failureType"),
                () -> assertTrue(json.has("expected"), "Should contain expected field"),
                () -> assertTrue(json.has("actual"), "Should contain actual field"),
                () -> assertTrue(json.has("ignoredFields"), "Should contain ignoredFields array"),
                () -> assertTrue(json.has("aliasedFields"), "Should contain aliasedFields array"),
                () -> assertTrue(json.has("sortedFields"), "Should contain sortedFields array"),
                () -> assertFalse(json.has("approvedFile"), "Should NOT contain approvedFile for bean matchers"),
                () -> assertTrue(error.isExpectedDefined(), "Expected must be defined for IDE diff view"),
                () -> assertTrue(error.isActualDefined(), "Actual must be defined for IDE diff view"),
                () -> assertNotNull(error.getExpected().getValue(), "Expected value must be non-null for IDE diff view"),
                () -> assertNotNull(error.getActual().getValue(), "Actual value must be non-null for IDE diff view")
        );
    }

    @Test
    public void shouldOutputDiffOnMismatchWhenMachineReadableDisabled() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected);

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        assertAll(
                () -> assertFalse(msg.contains("\"failureType\""), "Should NOT contain JSON failureType field"),
                () -> assertFalse(msg.contains("\"expected\""), "Should NOT contain JSON expected field")
        );
    }

    @Test
    public void shouldAppendAiTipAtEndOfNonMachineReadableFailureMessage() {
        ComparisonDescription desc = new ComparisonDescription();
        desc.setDifferencesMessage("some difference");
        String message = desc.toFailureMessage("some reason");
        assertTrue(message.endsWith(AI_TIP_SUFFIX),
                "Non-machine-readable failure message must end with AI discovery tip. Got: " + message);
    }

    @Test
    public void shouldTrackIgnoredPathsInMachineReadableOutput() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).beanInt(99).beanLong(5L).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).beanInt(42).beanLong(5L).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .ignoring("beanBoolean")
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        com.google.gson.JsonArray ignoredFields = json.getAsJsonArray("ignoredFields");
        assertTrue(ignoredFields.size() > 0, "ignoredFields should not be empty when a field was actually ignored");

        JsonObject firstIgnored = ignoredFields.get(0).getAsJsonObject();
        assertEquals("beanBoolean", firstIgnored.get("path").getAsString());
        assertEquals("IGNORE_PATH", firstIgnored.get("reason").getAsString());
    }

    @Test
    public void shouldShowEmptyIgnoredFieldsWhenConfiguredButNothingRemoved() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(true).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(false).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .ignoring("nonExistentField")
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        com.google.gson.JsonArray ignoredFields = json.getAsJsonArray("ignoredFields");
        assertEquals(0, ignoredFields.size(), "ignoredFields should be empty when configured path doesn't exist in data");
    }

    @Test
    public void shouldTrackCustomMatcherPathInIgnoredFields() {
        BeanWithPrimitives expected = beanWithPrimitives().beanInt(99).beanBoolean(false).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanInt(42).beanBoolean(true).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .with("beanInteger", org.hamcrest.Matchers.equalTo(42))
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        com.google.gson.JsonArray ignoredFields = json.getAsJsonArray("ignoredFields");

        boolean foundCustomMatcher = false;
        for (int i = 0; i < ignoredFields.size(); i++) {
            JsonObject entry = ignoredFields.get(i).getAsJsonObject();
            if ("beanInteger".equals(entry.get("path").getAsString())
                    && "CUSTOM_MATCHER".equals(entry.get("reason").getAsString())) {
                foundCustomMatcher = true;
                break;
            }
        }
        assertTrue(foundCustomMatcher, "Should track beanInteger as CUSTOM_MATCHER in ignoredFields");
    }

    @Test
    public void shouldTrackAliasedFieldsInMachineReadableOutput() {
        BeanWithPrimitives expected = beanWithPrimitives().beanInt(99).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanInt(42).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .withAlias("99", "EXPECTED_VALUE")
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        com.google.gson.JsonArray aliasedFields = json.getAsJsonArray("aliasedFields");
        assertTrue(aliasedFields.size() > 0, "aliasedFields should not be empty when aliases are applied");

        JsonObject firstAlias = aliasedFields.get(0).getAsJsonObject();
        assertEquals("99", firstAlias.get("originalValue").getAsString());
        assertEquals("EXPECTED_VALUE", firstAlias.get("alias").getAsString());
    }

    @Test
    public void shouldIncludeNoteWhenTypesIgnoredConfigured() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .ignoring(String.class)
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        assertTrue(json.has("note"), "Should contain note when type-based ignoring is configured");
        assertTrue(json.get("note").getAsString().contains("Type-based ignoring"),
                "Note should mention type-based ignoring");
    }

    @Test
    public void shouldTrackSortedFieldsByPathInMachineReadableOutput() {
        ParentBean expected = parent().parentString("expected").addToChildBeanList("b", 2).addToChildBeanList("a", 1).build();
        ParentBean actual = parent().parentString("actual").addToChildBeanList("a", 1).addToChildBeanList("b", 2).build();

        DiagnosingCustomisableMatcher<ParentBean> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .sortField("childBeanList")
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        com.google.gson.JsonArray sortedFields = json.getAsJsonArray("sortedFields");
        assertTrue(sortedFields.size() > 0, "sortedFields should not be empty when sorting is applied");

        JsonObject firstSorted = sortedFields.get(0).getAsJsonObject();
        assertEquals("childBeanList", firstSorted.get("path").getAsString());
        assertEquals("SORT_PATH", firstSorted.get("reason").getAsString());
    }

    @Test
    public void shouldTrackSortedFieldsByPatternInMachineReadableOutput() {
        ParentBean expected = parent().parentString("expected").addToChildBeanList("b", 2).addToChildBeanList("a", 1).build();
        ParentBean actual = parent().parentString("actual").addToChildBeanList("a", 1).addToChildBeanList("b", 2).build();

        DiagnosingCustomisableMatcher<ParentBean> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .sortField(org.hamcrest.Matchers.containsString("childBean"))
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        com.google.gson.JsonArray sortedFields = json.getAsJsonArray("sortedFields");
        assertTrue(sortedFields.size() > 0, "sortedFields should not be empty when pattern sort is applied");

        JsonObject firstSorted = sortedFields.get(0).getAsJsonObject();
        assertEquals("childBeanList", firstSorted.get("path").getAsString());
        assertEquals("SORT_PATTERN", firstSorted.get("reason").getAsString());
        assertTrue(firstSorted.has("pattern"), "Should include pattern description");
    }

    @Test
    public void shouldShowEmptySortedFieldsWhenNoSortingConfigured() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        assertTrue(json.has("sortedFields"), "Should always contain sortedFields array");
        assertEquals(0, json.getAsJsonArray("sortedFields").size(), "sortedFields should be empty when no sorting configured");
    }

    @Test
    public void shouldIncludeNoteWhenTypeSortingConfigured() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .sortType(String.class)
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        assertTrue(json.has("note"), "Should contain note when type-based sorting is configured");
        assertTrue(json.get("note").getAsString().contains("Type-based sorting"),
                "Note should mention type-based sorting");
    }

    @Test
    public void shouldOutputCompactExpectedAndActualInMachineReadableJson() {
        BeanWithPrimitives expected = beanWithPrimitives().beanBoolean(false).beanInt(99).build();
        BeanWithPrimitives actual = beanWithPrimitives().beanBoolean(true).beanInt(42).build();

        DiagnosingCustomisableMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY
                .beanMatcher(expected)
                .withMachineReadableOutput();

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        String msg = error.getMessage();
        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
        String expectedValue = json.get("expected").getAsString();
        String actualValue = json.get("actual").getAsString();
        assertFalse(expectedValue.contains("\n"), "expected JSON value in machine-readable output must be compact (no newlines)");
        assertFalse(actualValue.contains("\n"), "actual JSON value in machine-readable output must be compact (no newlines)");
    }
}
