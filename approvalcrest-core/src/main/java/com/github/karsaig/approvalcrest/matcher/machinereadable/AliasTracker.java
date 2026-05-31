package com.github.karsaig.approvalcrest.matcher.machinereadable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Accumulates records of values that were replaced by aliases during JSON processing.
 * Only instantiated when machine-readable output is enabled.
 */
public class AliasTracker {

    public static class AliasedField {
        private final String path;
        private final String originalValue;
        private final String alias;

        public AliasedField(String path, String originalValue, String alias) {
            this.path = path;
            this.originalValue = originalValue;
            this.alias = alias;
        }

        public String getPath() {
            return path;
        }

        public String getOriginalValue() {
            return originalValue;
        }

        public String getAlias() {
            return alias;
        }
    }

    private final List<AliasedField> fields = new ArrayList<>();

    public void recordAlias(String path, String originalValue, String alias) {
        fields.add(new AliasedField(path, originalValue, alias));
    }

    public List<AliasedField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
