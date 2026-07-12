package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.machinereadable.AliasTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.SortedFieldsTracker;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Unit tests for {@link ComparisonDescription}, which renders an assertion mismatch either as a
 * human-readable message (with the AI tip) or as a compact machine-readable JSON document that
 * embeds the ignored/aliased/sorted field trackers. Both rendering branches and the tracker
 * serialisation are exercised here.
 */
public class ComparisonDescriptionTest {

    @Test
    void humanReadableMessageIncludesReasonDifferencesAndAiTip() {
        ComparisonDescription description = new ComparisonDescription();
        description.setDifferencesMessage("field x differs");

        String message = description.toFailureMessage("things went wrong");

        assertThat(message, containsString("things went wrong"));
        assertThat(message, containsString("field x differs"));
        assertThat(message, containsString(ComparisonDescription.AI_TIP.trim()));
    }

    @Test
    void humanReadableMessageOmitsBlankReason() {
        ComparisonDescription description = new ComparisonDescription();
        description.setDifferencesMessage("only differences");

        String message = description.toFailureMessage("");

        // No leading reason line when the reason is blank.
        assertThat(message, is("only differences" + ComparisonDescription.AI_TIP));
    }

    @Test
    void machineReadableMessageIsValidCompactJsonWithoutAiTip() {
        ComparisonDescription description = new ComparisonDescription();
        description.setMachineReadable(true);
        description.setTestInfo("MyTest.myMethod");
        description.setApprovedFilePath("/tmp/approved.json");
        description.setExpected("{\"a\": 1}");
        description.setActual("{\"a\": 2}");

        String json = description.toFailureMessage("mismatch");
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        assertThat(root.get("failureType").getAsString(), is("MISMATCH"));
        assertThat(root.get("test").getAsString(), is("MyTest.myMethod"));
        assertThat(root.get("approvedFile").getAsString(), is("/tmp/approved.json"));
        assertThat(root.has("action"), is(true));
        // expected/actual are recompacted (no whitespace).
        assertThat(root.get("expected").getAsString(), is("{\"a\":1}"));
        assertThat(root.get("actual").getAsString(), is("{\"a\":2}"));
        assertThat(json, not(containsString("[AI tip]")));
    }

    @Test
    void machineReadableMessageEmbedsTrackers() {
        IgnoredFieldsTracker ignored = new IgnoredFieldsTracker();
        ignored.recordIgnoredPattern("a.secret", IgnoredFieldsTracker.Reason.CUSTOM_MATCHER_PATTERN, "startsWith(\"sec\")");
        ignored.recordRemovedEmpty("a.parent", Arrays.asList("a.parent.child (IGNORE_PATH)"));

        AliasTracker aliases = new AliasTracker();
        aliases.recordAlias("a.id", "abc-123", "<id>");

        SortedFieldsTracker sorted = new SortedFieldsTracker();
        sorted.recordSortedByPath("a.list");
        sorted.recordSortedByPattern("a.other", "endsWith(\"List\")");

        ComparisonDescription description = new ComparisonDescription();
        description.setMachineReadable(true);
        description.setIgnoredFieldsTracker(ignored);
        description.setAliasTracker(aliases);
        description.setSortedFieldsTracker(sorted);
        description.setNote("a note");

        JsonObject root = JsonParser.parseString(description.toFailureMessage(null)).getAsJsonObject();

        assertThat(root.getAsJsonArray("ignoredFields").size(), is(2));
        assertThat(root.getAsJsonArray("aliasedFields").size(), is(1));
        assertThat(root.getAsJsonArray("sortedFields").size(), is(2));
        assertThat(root.get("note").getAsString(), is("a note"));
        // pattern + causes present on the ignored entries
        assertThat(root.getAsJsonArray("ignoredFields").toString(), containsString("pattern"));
        assertThat(root.getAsJsonArray("ignoredFields").toString(), containsString("causes"));
    }

    @Test
    void machineReadableMessageWithEmptyTrackersHasEmptyArrays() {
        ComparisonDescription description = new ComparisonDescription();
        description.setMachineReadable(true);
        // no trackers set, no expected/actual

        JsonObject root = JsonParser.parseString(description.toFailureMessage(null)).getAsJsonObject();

        assertThat(root.getAsJsonArray("ignoredFields").size(), is(0));
        assertThat(root.getAsJsonArray("aliasedFields").size(), is(0));
        assertThat(root.getAsJsonArray("sortedFields").size(), is(0));
        assertThat(root.has("expected"), is(false));
    }

    @Test
    void invalidJsonInExpectedIsPassedThroughVerbatim() {
        ComparisonDescription description = new ComparisonDescription();
        description.setMachineReadable(true);
        description.setExpected("not valid json {");

        JsonObject root = JsonParser.parseString(description.toFailureMessage(null)).getAsJsonObject();

        // compactJson catches the parse failure and keeps the raw string.
        assertThat(root.get("expected").getAsString(), is("not valid json {"));
    }

    @Test
    void typesIgnoredConfiguredAddsLegacyNote() {
        ComparisonDescription description = new ComparisonDescription();
        description.setMachineReadable(true);
        description.setTypesIgnoredConfigured(true);

        JsonObject root = JsonParser.parseString(description.toFailureMessage(null)).getAsJsonObject();

        assertThat(root.get("note").getAsString(), containsString("Type-based ignoring"));
    }

    @Test
    void accessorsRoundTrip() {
        ComparisonDescription description = new ComparisonDescription();
        description.setActual("A");
        description.setExpected("E");
        description.setDifferencesMessage("D");
        description.setComparisonFailure(true);

        assertThat(description.getActual(), is("A"));
        assertThat(description.getExpected(), is("E"));
        assertThat(description.getDifferencesMessage(), is("D"));
        assertThat(description.isComparisonFailure(), is(true));
    }
}
