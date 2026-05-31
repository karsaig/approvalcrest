package com.github.karsaig.approvalcrest.matcher.machinereadable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public void recordIgnored(String path, Reason reason) {
        fields.add(IgnoredField.of(path, reason));
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
