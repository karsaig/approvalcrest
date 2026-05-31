package com.github.karsaig.approvalcrest.matcher.machinereadable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Accumulates records of fields whose arrays were actually sorted due to user-configured sorting.
 * Only instantiated when machine-readable output is enabled.
 */
public class SortedFieldsTracker {

    public enum Reason {
        SORT_PATH,
        SORT_PATTERN
    }

    public static class SortedField {
        private final String path;
        private final Reason reason;
        private final String pattern;

        private SortedField(String path, Reason reason, String pattern) {
            this.path = path;
            this.reason = reason;
            this.pattern = pattern;
        }

        public static SortedField ofPath(String path) {
            return new SortedField(path, Reason.SORT_PATH, null);
        }

        public static SortedField ofPattern(String path, String patternDescription) {
            return new SortedField(path, Reason.SORT_PATTERN, patternDescription);
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
    }

    private final List<SortedField> fields = new ArrayList<>();

    public void recordSortedByPath(String path) {
        fields.add(SortedField.ofPath(path));
    }

    public void recordSortedByPattern(String path, String patternDescription) {
        fields.add(SortedField.ofPattern(path, patternDescription));
    }

    public List<SortedField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
