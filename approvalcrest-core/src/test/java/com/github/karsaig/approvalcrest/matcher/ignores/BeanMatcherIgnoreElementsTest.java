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
}
