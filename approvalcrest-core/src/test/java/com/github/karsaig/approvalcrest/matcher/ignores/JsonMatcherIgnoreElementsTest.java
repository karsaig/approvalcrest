package com.github.karsaig.approvalcrest.matcher.ignores;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for {@code ignoringElementsWhere} on {@code sameJsonAsApproved}. Inputs are FHIR-shaped
 * JSON strings ({@code meta.tag} = array of {@code {system, code}}). In strict file matching the
 * rule strips elements from the actual side only, so the approved content must already omit them;
 * in lenient matching the rule strips both sides, so an approved file that still contains the
 * element also passes.
 */
public class JsonMatcherIgnoreElementsTest extends AbstractFileMatcherTest {

    private static final String TRACKING_SYSTEM = "https://tracking.example/flow-id";
    private static final String REAL_SYSTEM = "https://real.example/category";

    private static final String INPUT_WITH_TRACKING_TAG = "{\n" +
            "  \"meta\": {\n" +
            "    \"tag\": [\n" +
            "      { \"code\": \"flow-123\", \"system\": \"" + TRACKING_SYSTEM + "\" },\n" +
            "      { \"code\": \"keep\", \"system\": \"" + REAL_SYSTEM + "\" }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"resourceType\": \"Patient\"\n" +
            "}";

    private static final String APPROVED_WITHOUT_TRACKING_TAG = "{\n" +
            "  \"meta\": {\n" +
            "    \"tag\": [\n" +
            "      { \"code\": \"keep\", \"system\": \"" + REAL_SYSTEM + "\" }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"resourceType\": \"Patient\"\n" +
            "}";

    @Test
    void strictMatchingStripsTrackingTagFromActualOnly() {
        assertJsonMatcherWithDummyTestInfo(INPUT_WITH_TRACKING_TAG, APPROVED_WITHOUT_TRACKING_TAG,
                getDefaultFileMatcherConfig(),
                m -> m.ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM)), null);
    }

    @Test
    void stringValueFormStripsTrackingTag() {
        assertJsonMatcherWithDummyTestInfo(INPUT_WITH_TRACKING_TAG, APPROVED_WITHOUT_TRACKING_TAG,
                getDefaultFileMatcherConfig(),
                m -> m.ignoringElementsWhere("meta.tag.system", TRACKING_SYSTEM), null);
    }

    @Test
    void lenientMatchingStripsTrackingTagFromBothSides() {
        // Approved file still contains the tracking tag; lenient matching filters it from the
        // approved side too, so the assertion passes.
        assertJsonMatcherWithDummyTestInfo(INPUT_WITH_TRACKING_TAG, INPUT_WITH_TRACKING_TAG,
                getDefaultFileMatcherConfigWithLenientMatching(),
                m -> m.ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM)), null);
    }

    @Test
    void allElementsRemovedLeavesEmptyArray() {
        String inputAllTracking = "{\n" +
                "  \"meta\": {\n" +
                "    \"tag\": [\n" +
                "      { \"code\": \"flow-1\", \"system\": \"" + TRACKING_SYSTEM + "\" },\n" +
                "      { \"code\": \"flow-2\", \"system\": \"" + TRACKING_SYSTEM + "\" }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"resourceType\": \"Patient\"\n" +
                "}";
        String approvedEmptyTag = "{\n" +
                "  \"meta\": {\n" +
                "    \"tag\": []\n" +
                "  },\n" +
                "  \"resourceType\": \"Patient\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(inputAllTracking, approvedEmptyTag,
                getDefaultFileMatcherConfig(),
                m -> m.ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM)), null);
    }

    @Test
    void strictMatchingFailsWhenApprovedStillContainsTrackingTag() {
        // Strict matching does not touch the approved side, so an approved file that still
        // contains the tracking tag no longer matches the stripped actual.
        assertJsonMatcherWithDummyTestInfo(INPUT_WITH_TRACKING_TAG, INPUT_WITH_TRACKING_TAG,
                getDefaultFileMatcherConfig(),
                m -> m.ignoringElementsWhere("meta.tag.system", equalTo(TRACKING_SYSTEM)),
                thrown -> org.junit.jupiter.api.Assertions.assertTrue(
                        thrown.getMessage().contains("tag"),
                        "failure should report the tag difference: " + thrown.getMessage()),
                AssertionError.class);
    }
}
