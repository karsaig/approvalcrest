package com.github.karsaig.approvalcrest.matcher.alias;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ChildBean;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for alias substitution in {@code sameJsonAsApproved} ({@link com.github.karsaig.approvalcrest.matcher.JsonMatcher}).
 */
public class JsonMatcherAliasTest extends AbstractFileMatcherTest {

    // -------------------------------------------------------------------------
    // Basic substitution — value in approved file is the alias, not the raw value
    // -------------------------------------------------------------------------

    @Test
    void withAliasReplacesValueBeforeComparison() {
        ParentBean input = parent().parentString("volatile-uuid-123").build();

        String approvedFileContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"<id>\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAlias("volatile-uuid-123", "<id>"),
                null);
    }

    @Test
    void withAliasByFieldAndValueReplacesOnlyMatchingField() {
        ParentBean input = parent()
                .parentString("volatile-uuid-123")
                .childBean(child().childString("volatile-uuid-123"))
                .build();

        // "id" field gets alias; childString with same value is NOT aliased (different field name)
        String approvedFileContent =
                "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0,\n" +
                "    \"childString\": \"volatile-uuid-123\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"<parentId>\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAlias("parentString", "volatile-uuid-123", "<parentId>"),
                null);
    }

    @Test
    void withAliasMapReplacesValueBeforeComparison() {
        ParentBean input = parent().parentString("secret-value").build();

        AliasMap map = AliasMap.builder()
                .add("secret-value", "<secret>")
                .build();

        String approvedFileContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"<secret>\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAliasMap(map),
                null);
    }

    // -------------------------------------------------------------------------
    // Not-approved file creation writes alias, not raw value
    // -------------------------------------------------------------------------

    @Test
    void firstRunNotApprovedFileContainsAlias() {
        ParentBean input = parent().parentString("volatile-uuid-123").build();

        // Gson HTML-escapes < and > to \u003c / \u003e when writing files
        String expectedNotApprovedContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"\\u003cid\\u003e\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input, expectedNotApprovedContent,
                m -> m.withAlias("volatile-uuid-123", "<id>"));
    }

    // -------------------------------------------------------------------------
    // String and number coercion: "13" (string) and 13 (int) both alias
    // -------------------------------------------------------------------------

    @Test
    void aliasMatchesStringAndNumericWithSameCoercedValue() {
        // childInteger=13 is a JSON number; alias for "13" should replace it
        ParentBean input = parent().childBean(child().childInteger(13)).build();

        String approvedFileContent =
                "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": \"<thirteen>\",\n" +
                "    \"childString\": null\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": null\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAlias("13", "<thirteen>"),
                null);
    }

    // -------------------------------------------------------------------------
    // Merge: multiple withAliasMap / withAlias calls accumulate
    // -------------------------------------------------------------------------

    @Test
    void multipleWithAliasCallsAccumulate() {
        ParentBean input = parent()
                .parentString("id-value")
                .childBean(child().childString("name-value"))
                .build();

        String approvedFileContent =
                "{\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0,\n" +
                "    \"childString\": \"<name>\"\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"<id>\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAlias("id-value", "<id>").withAlias("name-value", "<name>"),
                null);
    }

    @Test
    void withAliasMapMergeLastWinsOnConflict() {
        ParentBean input = parent().parentString("shared-value").build();

        AliasMap base = AliasMap.builder().add("shared-value", "<base>").build();
        AliasMap override = AliasMap.builder().add("shared-value", "<override>").build();

        String approvedFileContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"<override>\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAliasMap(base).withAliasMap(override),
                null);
    }

    // -------------------------------------------------------------------------
    // Alias before sort: aliased value changes sort key → correct ordering
    // -------------------------------------------------------------------------

    @Test
    void aliasIsAppliedBeforeSortSoAliasedValueAffectsSortOrder() {
        // Two items: first has childString="B", second has childString="A".
        // Without alias they sort as A, B.
        // With alias: "A" → "<zzz>", "B" → "<aaa>" → alias-order sorts <aaa> before <zzz>,
        // so second item (originally "A") ends up first.
        java.util.List<ParentBean> input = java.util.Arrays.asList(
                parent().parentString("item1").childBean(child().childString("A")).build(),
                parent().parentString("item2").childBean(child().childString("B")).build()
        );

        // After alias substitution: item1.childBean.childString="<zzz>", item2.childBean.childString="<aaa>"
        // sort by childString → <aaa> (item2) first, <zzz> (item1) second
        String approvedFileContent =
                "[\n" +
                "  {\n" +
                "    \"childBean\": {\n" +
                "      \"childInteger\": 0,\n" +
                "      \"childString\": \"<aaa>\"\n" +
                "    },\n" +
                "    \"childBeanList\": [],\n" +
                "    \"childBeanMap\": [],\n" +
                "    \"parentString\": \"item2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"childBean\": {\n" +
                "      \"childInteger\": 0,\n" +
                "      \"childString\": \"<zzz>\"\n" +
                "    },\n" +
                "    \"childBeanList\": [],\n" +
                "    \"childBeanMap\": [],\n" +
                "    \"parentString\": \"item1\"\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAlias("A", "<zzz>").withAlias("B", "<aaa>").sortField(""),
                null);
    }

    // -------------------------------------------------------------------------
    // Regex-based alias (via AliasMap.builder().addByPattern)
    // -------------------------------------------------------------------------

    @Test
    void regexPatternAliasReplacesMatchingValues() {
        ParentBean input = parent().parentString("abc-123-def").build();

        AliasMap map = AliasMap.builder()
                .addByPattern(Pattern.compile(".*String"), Pattern.compile("[a-z]+-\\d+-[a-z]+"), "<token>")
                .build();

        String approvedFileContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"<token>\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAliasMap(map),
                null);
    }

    // -------------------------------------------------------------------------
    // Booleans and nulls are NOT aliased
    // -------------------------------------------------------------------------

    @Test
    void booleanValuesAreNotAliasedEvenWhenRuleCouldMatchTheirStringForm() {
        // BeanWithPrimitives has a boolean field; alias for "true" should NOT replace booleans
        // Use a JSON string input with explicit boolean to test this
        String jsonInput =
                "{\n" +
                "  \"flag\": true,\n" +
                "  \"name\": \"true\"\n" +
                "}";

        // alias for "true" should only replace the string "true", not the boolean true
        String approvedFileContent =
                "{\n" +
                "  \"flag\": true,\n" +
                "  \"name\": \"<yes>\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(jsonInput, approvedFileContent,
                m -> m.withAlias("true", "<yes>"),
                null);
    }

    // -------------------------------------------------------------------------
    // Dynamic alias (Function resolver)
    // -------------------------------------------------------------------------

    @Test
    void dynamicAliasViaEntryBuilder() {
        ParentBean input = parent().parentString("user-42").build();

        AliasMap map = AliasMap.builder()
                .entry()
                    .field("parentString")
                    .valuePattern(Pattern.compile("user-\\d+"))
                    .alias(v -> "<user:" + v.split("-")[1] + ">")
                .register()
                .build();

        String approvedFileContent =
                "{\n" +
                "  \"childBean\": null,\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": [],\n" +
                "  \"parentString\": \"<user:42>\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,
                m -> m.withAliasMap(map),
                null);
    }
}
