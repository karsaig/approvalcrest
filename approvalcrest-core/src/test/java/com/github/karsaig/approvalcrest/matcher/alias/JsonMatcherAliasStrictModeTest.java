package com.github.karsaig.approvalcrest.matcher.alias;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Test;

import org.opentest4j.AssertionFailedError;

import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that verify alias substitution behaviour with respect to strict file matching.
 *
 * <p>In strict mode (the default, {@code fileMatcherStrictFileMatching=true}) the approved file is
 * used as-is: no transformation is applied to its content. Aliases must therefore only be applied
 * to the actual side. An approved file that still contains a raw (non-aliased) value is stale and
 * must cause the assertion to fail so the developer is prompted to regenerate it.
 *
 * <p>In non-strict mode ({@code fileMatcherStrictFileMatching=false}) aliases are applied to both
 * sides, so a stale approved file with a raw value will still match after substitution.
 */
public class JsonMatcherAliasStrictModeTest extends AbstractFileMatcherTest {

    /**
     * In strict mode an approved file that contains the raw (pre-alias) value must cause the
     * assertion to fail. Before the fix, the alias was applied to the approved content as well,
     * making it look like {@code "<id>"} on both sides and silently masking the stale file.
     */
    @Test
    void aliasNotAppliedToApprovedContentInStrictMode() {
        ParentBean input = parent().parentString("volatile-uuid-123").build();

        // Approved file still holds the raw value — it is stale (was not regenerated after
        // the alias was introduced). Strict mode must detect this and fail.
        String staleApprovedContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"volatile-uuid-123\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, staleApprovedContent,
                getDefaultFileMatcherConfig(),    // strict = true (default)
                m -> m.withAlias("volatile-uuid-123", "<id>"),
                error -> {
                    String msg = removeAiTip(error.getMessage());
                    assertTrue(msg.contains("volatile-uuid-123"),
                            "Failure message should show the raw value from the approved file");
                    assertTrue(msg.contains("<id>") || msg.contains("\\u003cid\\u003e"),
                            "Failure message should show the aliased actual value");
                },
                AssertionError.class);
    }

    /**
     * In non-strict mode the alias is applied to both the actual and the approved content, so a
     * stale approved file with a raw value still passes after substitution. This is the legacy
     * behaviour preserved for users who opt out of strict matching.
     */
    @Test
    void aliasAppliedToApprovedContentInNonStrictMode() {
        ParentBean input = parent().parentString("volatile-uuid-123").build();

        // Same stale approved file — raw value, not yet aliased.
        String staleApprovedContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"volatile-uuid-123\"\n" +
                "}";

        // Non-strict: alias transforms both sides → raw becomes "<id>" on approved side too → match.
        assertJsonMatcherWithDummyTestInfo(input, staleApprovedContent,
                getDefaultFileMatcherConfigWithLenientMatching(),    // strict = false
                m -> m.withAlias("volatile-uuid-123", "<id>"),
                null);    // null = expect success
    }

    /**
     * In strict mode the "Expected:" section of the Hamcrest failure message (from describeTo)
     * must use the same transformation settings as the expected side of the diff (from doMatches).
     * Both are now backed by the shared filterExpectedJson() helper, making divergence impossible.
     *
     * Concrete invariant: in strict mode, describeTo must NOT apply the alias — it shows the raw
     * approved-file value, which is consistent with the diff's "Expected:" line.
     */
    @Test
    void describeToShowsRawApprovedFileContentInStrictMode() {
        ParentBean input = parent().parentString("volatile-uuid-123").build();

        String staleApprovedContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"volatile-uuid-123\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, staleApprovedContent,
                getDefaultFileMatcherConfig(),
                m -> m.withAlias("volatile-uuid-123", "<id>"),
                error -> {
                    // The expected content (from both the comparison diff and describeTo, which
                    // now share filterExpectedJson()) must show the raw approved-file value.
                    AssertionFailedError afe = (AssertionFailedError) error;
                    String expectedContent = (String) afe.getExpected().getValue();

                    assertTrue(expectedContent.contains("volatile-uuid-123"),
                            "Expected content should show the raw value from the stale approved file");
                    assertFalse(expectedContent.contains("<id>") || expectedContent.contains("\\u003cid\\u003e"),
                            "Expected content must not contain the aliased value in strict mode");
                },
                AssertionError.class);
    }
}
