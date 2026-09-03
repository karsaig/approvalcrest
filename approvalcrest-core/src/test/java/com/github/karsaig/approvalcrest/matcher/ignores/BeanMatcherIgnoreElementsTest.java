package com.github.karsaig.approvalcrest.matcher.ignores;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMatcherFactory;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

/**
 * Tests for {@code ignoringElementsWhere} on {@code sameBeanAs}. The fixtures mirror the FHIR
 * JSON shape ({@code meta.tag} = array of {@code {system, code, display}}) that motivated the
 * feature: a runtime "tracking tag" that must be removed before comparison.
 */
public class BeanMatcherIgnoreElementsTest extends AbstractBeanMatcherTest {

    private static final String TRACKING_SYSTEM = "https://tracking.example/flow-id";
    private static final String REAL_SYSTEM = "https://real.example/category";

    @Test
    void matcherFormRemovesOnlyTheMatchingElement() {
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(TRACKING_SYSTEM, "flow-123"),
                new Tag(REAL_SYSTEM, "keep"))));
        Resource expected = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "keep"))));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM)));
    }

    @Test
    void stringValueFormRemovesOnlyTheMatchingElement() {
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(TRACKING_SYSTEM, "flow-123"),
                new Tag(REAL_SYSTEM, "keep"))));
        Resource expected = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "keep"))));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoringElementsWhere("meta.tag.system", TRACKING_SYSTEM));
    }

    @Test
    void removesEveryMatchingElementAndKeepsOthersInOrder() {
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(TRACKING_SYSTEM, "flow-1"),
                new Tag(REAL_SYSTEM, "first"),
                new Tag(TRACKING_SYSTEM, "flow-2"),
                new Tag(REAL_SYSTEM, "second"))));
        Resource expected = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "first"),
                new Tag(REAL_SYSTEM, "second"))));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM)));
    }

    @Test
    void allElementsRemovedLeavesEmptyArray() {
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(TRACKING_SYSTEM, "flow-1"),
                new Tag(TRACKING_SYSTEM, "flow-2"))));
        Resource expected = new Resource(new Meta(Lists.<Tag>newArrayList()));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM)));
    }

    @Test
    void fanOutThroughBundleRemovesTagNotEntry() {
        Bundle actual = new Bundle(Lists.newArrayList(
                new Entry(new Resource(new Meta(Lists.newArrayList(
                        new Tag(TRACKING_SYSTEM, "flow-1"),
                        new Tag(REAL_SYSTEM, "keep-1"))))),
                new Entry(new Resource(new Meta(Lists.newArrayList(
                        new Tag(REAL_SYSTEM, "keep-2")))))));
        Bundle expected = new Bundle(Lists.newArrayList(
                new Entry(new Resource(new Meta(Lists.newArrayList(
                        new Tag(REAL_SYSTEM, "keep-1"))))),
                new Entry(new Resource(new Meta(Lists.newArrayList(
                        new Tag(REAL_SYSTEM, "keep-2")))))));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoringElementsWhere("entry.resource.meta.tag.system", equalTo(TRACKING_SYSTEM)));
    }

    @Test
    void matcherFormSupportsRichMatchers() {
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(TRACKING_SYSTEM, "flow-1"),
                new Tag(REAL_SYSTEM, "keep"))));
        Resource expected = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "keep"))));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoringElementsWhere("meta.tag.system", startsWith("https://tracking")));
    }

    @Test
    void withoutRuleTheExtraElementFailsTheComparison() {
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(TRACKING_SYSTEM, "flow-123"),
                new Tag(REAL_SYSTEM, "keep"))));
        Resource expected = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "keep"))));

        assertDiagnosingMatcher(actual, expected, DiagnosingCustomisableMatcher::skipClassComparison,
                AssertionFailedError.class,
                thrown -> Assertions.assertTrue(thrown.getMessage().contains("tag"),
                        "failure should report the tag array difference: " + thrown.getMessage()));
    }

    @Test
    void nonMatchingValueIsANoOp() {
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "keep"))));
        Resource expected = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "keep"))));

        // No element has the tracking system, so nothing is removed and the beans still match.
        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM)));
    }

    @Test
    void missingPathIsANoOp() {
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "keep"))));
        Resource expected = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "keep"))));

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoringElementsWhere("does.not.exist", equalTo(TRACKING_SYSTEM)));
    }

    @Test
    void machineReadableOutputListsRemovedElement() {
        // Actual carries a tracking tag (removed) and a real tag whose code differs from expected,
        // so the comparison still fails and the machine-readable output is produced.
        Resource actual = new Resource(new Meta(Lists.newArrayList(
                new Tag(TRACKING_SYSTEM, "flow-123"),
                new Tag(REAL_SYSTEM, "actual-code"))));
        Resource expected = new Resource(new Meta(Lists.newArrayList(
                new Tag(REAL_SYSTEM, "expected-code"))));

        DiagnosingCustomisableMatcher<Resource> underTest = new TestMatcherFactory()
                .beanMatcher(expected)
                .ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM))
                .withMachineReadableOutput();

        AssertionFailedError error = Assertions.assertThrows(AssertionFailedError.class,
                () -> assertThat(actual, underTest));

        JsonObject json = JsonParser.parseString(error.getMessage()).getAsJsonObject();
        JsonArray ignored = json.getAsJsonArray("ignoredFields");
        boolean found = false;
        for (JsonElement e : ignored) {
            JsonObject o = e.getAsJsonObject();
            if ("IGNORE_ELEMENT_MATCH".equals(o.get("reason").getAsString())
                    && o.get("path").getAsString().startsWith("meta.tag[")) {
                found = true;
            }
        }
        Assertions.assertTrue(found,
                "ignoredFields should list the removed element with reason IGNORE_ELEMENT_MATCH: " + ignored);
    }

    @SuppressWarnings("unused")
    private static class Bundle {
        private final List<Entry> entry;

        Bundle(List<Entry> entry) {
            this.entry = entry;
        }
    }

    @SuppressWarnings("unused")
    private static class Entry {
        private final Resource resource;

        Entry(Resource resource) {
            this.resource = resource;
        }
    }

    @SuppressWarnings("unused")
    private static class Resource {
        private final Meta meta;

        Resource(Meta meta) {
            this.meta = meta;
        }
    }

    @SuppressWarnings("unused")
    private static class Meta {
        private final List<Tag> tag;

        Meta(List<Tag> tag) {
            this.tag = tag;
        }
    }

    @SuppressWarnings("unused")
    private static class Tag {
        private final String system;
        private final String code;

        Tag(String system, String code) {
            this.system = system;
            this.code = code;
        }
    }

    // -------------------------------------------------------------------------
    // The * wildcard segment in an element-ignore path
    // -------------------------------------------------------------------------

    /** A tag carrying the field the rule tests. */
    static class WildcardTag {
        String system;

        WildcardTag(String system) {
            this.system = system;
        }
    }

    /** Two map entries, each holding a list of tags to filter. */
    static class WildcardTagMap {
        java.util.Map<String, List<WildcardTag>> byKey = new java.util.LinkedHashMap<>();

        WildcardTagMap(List<String> first, List<String> second) {
            byKey.put("k1", tags(first));
            byKey.put("k2", tags(second));
        }

        private static List<WildcardTag> tags(List<String> systems) {
            List<WildcardTag> result = new java.util.ArrayList<>();
            for (String system : systems) {
                result.add(new WildcardTag(system));
            }
            return result;
        }
    }

    private static WildcardTagMap withDropTags() {
        return new WildcardTagMap(Lists.newArrayList("keep", "drop"), Lists.newArrayList("keep", "drop"));
    }

    private static WildcardTagMap keepOnly() {
        return new WildcardTagMap(Lists.newArrayList("keep"), Lists.newArrayList("keep"));
    }

    /**
     * A * segment moves the rule from one named entry to every entry, which is the case a map of
     * lists cannot otherwise express: the filtered array sits under each map value.
     */
    @Test
    public void wildcardRemovesMatchingElementsUnderEveryMapValue() {
        assertDiagnosingMatcher(withDropTags(), keepOnly(),
                matcher -> matcher.ignoringElementsWhere("byKey.*.system", "drop"));
    }

    /** The wildcard does the same as naming every key. */
    @Test
    public void wildcardMatchesNamingEveryKey() {
        assertDiagnosingMatcher(withDropTags(), keepOnly(),
                matcher -> matcher.ignoringElementsWhere("byKey.k1.system", "drop")
                        .ignoringElementsWhere("byKey.k2.system", "drop"));
    }

    /**
     * Naming one key leaves the other entry's list unfiltered. The assertion is about *which* entry is
     * reported: only k2 means k1 really was filtered, where both being reported would mean the rule had
     * not run at all.
     */
    @Test
    public void namingOneKeyLeavesTheOtherEntryUnfiltered() {
        assertDiagnosingMatcher(withDropTags(), keepOnly(),
                matcher -> matcher.ignoringElementsWhere("byKey.k1.system", "drop"),
                AssertionError.class, error -> {
                    Assertions.assertTrue(error.getMessage().contains("k2"),
                            "Expected the unfiltered k2 entry to be reported, was: " + error.getMessage());
                    Assertions.assertFalse(error.getMessage().contains("k1"),
                            "k1 was filtered, so it should not be reported: " + error.getMessage());
                });
    }

    /**
     * A * that ends the whole path is the leaf field name, not a wildcard, so it looks for a field
     * literally called * on the array elements and filters nothing. Asserted against an expected value
     * that differs, so a wildcard reading — which would filter the drop tags and make the comparison
     * pass — is ruled out rather than merely unobserved.
     */
    @Test
    public void trailingWildcardIsTheLeafFieldNameNotAWildcard() {
        assertDiagnosingMatcher(withDropTags(), keepOnly(),
                matcher -> matcher.ignoringElementsWhere("byKey.*", "drop"),
                AssertionError.class, error -> Assertions.assertTrue(
                        error.getMessage().contains("k1") && error.getMessage().contains("k2"),
                        "Nothing should have been filtered, so both entries differ: " + error.getMessage()));
    }

    /**
     * The comparison that makes the wildcard worth having: without it the rule filters the outer array
     * of map entries, testing each entry object for the leaf field, which matches nothing.
     */
    @Test
    public void wildcardFreePathFiltersTheOuterEntryArrayAndSoMatchesNothing() {
        assertDiagnosingMatcher(withDropTags(), keepOnly(),
                matcher -> matcher.ignoringElementsWhere("byKey.system", "drop"),
                AssertionError.class, error -> Assertions.assertTrue(
                        error.getMessage().contains("k1") && error.getMessage().contains("k2"),
                        "Nothing should have been filtered: " + error.getMessage()));
    }
}
