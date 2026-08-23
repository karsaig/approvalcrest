package com.github.karsaig.approvalcrest.matcher.machinereadable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Accumulates records of fields that were actually removed during JSON filtering.
 * Only instantiated when machine-readable output is enabled.
 */
public class IgnoredFieldsTracker {

    public enum Reason {
        IGNORE_PATH,
        IGNORE_PATTERN,
        CUSTOM_MATCHER,
        CUSTOM_MATCHER_PATTERN,
        IGNORE_ELEMENT_MATCH,
        REMOVED_EMPTY
    }

    public static class IgnoredField {
        private final String path;
        private final Reason reason;
        private final String pattern;
        private final List<String> causes;

        private IgnoredField(String path, Reason reason, String pattern, List<String> causes) {
            this.path = path;
            this.reason = reason;
            this.pattern = pattern;
            this.causes = causes;
        }

        public static IgnoredField of(String path, Reason reason) {
            return new IgnoredField(path, reason, null, null);
        }

        public static IgnoredField ofPattern(String path, Reason reason, String pattern) {
            return new IgnoredField(path, reason, pattern, null);
        }

        public static IgnoredField removedEmpty(String path, List<String> causes) {
            return new IgnoredField(path, Reason.REMOVED_EMPTY, null, causes);
        }

        public String getPath() {
            return path;
        }

        public Reason getReason() {
            return reason;
        }

        public String getPattern() {
            return pattern;
        }

        public List<String> getCauses() {
            return causes;
        }
    }

    private final List<IgnoredField> fields = new ArrayList<>();
    private final Set<String> recordedRules = new HashSet<>();

    public void recordIgnored(String path, Reason reason) {
        fields.add(IgnoredField.of(path, reason));
    }

    /**
     * Records a rule whose path identifies the RULE rather than a location, deduplicating it.
     * <p>
     * One tracker spans both filter runs, over the actual value and over the approved content, so a
     * rule applying to both sides was reported twice. Only a rule path can be deduplicated this way.
     * A location must not be — an element index, or the path of a parent removed for becoming empty —
     * because sibling locations can share a path today: an intermediate array is traversed without
     * appending an index, so two removals in different branches both report {@code entry.tag[0]}.
     * Deduplicating those turns a mislabelled but complete report into a mislabelled and short one,
     * which is worse.
     */
    public void recordIgnoredRule(String path, Reason reason) {
        if (recordedRules.add(reason + "\u0000" + path)) {
            fields.add(IgnoredField.of(path, reason));
        }
    }

    public void recordIgnoredPattern(String path, Reason reason, String patternDescription) {
        fields.add(IgnoredField.ofPattern(path, reason, patternDescription));
    }

    public void recordRemovedEmpty(String path, List<String> causes) {
        fields.add(IgnoredField.removedEmpty(path, causes));
    }

    public List<IgnoredField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
