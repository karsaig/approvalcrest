package com.github.karsaig.approvalcrest.matcher.alias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * An immutable, ordered set of alias substitution rules. Rules are evaluated in registration
 * order and the <em>last</em> matching rule wins, so later entries override earlier ones.
 *
 * <p>Create instances via {@link #builder()}, then apply them to a matcher with
 * {@code sameJsonAsApproved().withAliasMap(myMap)}.
 */
public final class AliasMap {

    private final List<AliasEntry> entries;

    AliasMap(List<AliasEntry> entries) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    /**
     * Resolves an alias for the given path, field name, and coerced primitive value.
     * Iterates in reverse so the last registered matching rule wins.
     */
    public Optional<String> resolve(String path, String fieldName, String coercedValue) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            AliasEntry entry = entries.get(i);
            if (entry.matches(path, fieldName, coercedValue)) {
                return Optional.of(entry.resolve(coercedValue));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns a new {@code AliasMap} containing the entries of this map followed by the entries
     * of {@code other}. Because last-match-wins, {@code other}'s entries take precedence when
     * both match the same primitive.
     */
    public AliasMap merge(AliasMap other) {
        List<AliasEntry> merged = new ArrayList<>(entries);
        merged.addAll(other.entries);
        return new AliasMap(merged);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    List<AliasEntry> getEntries() {
        return entries;
    }

    public static Builder builder() {
        return new Builder();
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static final class Builder {

        private final List<AliasEntry> entries = new ArrayList<>();

        private Builder() {
        }

        // --- value only ---

        public Builder add(String value, String alias) {
            return add(value, v -> alias);
        }

        public Builder add(String value, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, null, exactPredicate(value), resolver));
            return this;
        }

        // --- field + value ---

        public Builder add(String fieldName, String value, String alias) {
            return add(fieldName, value, v -> alias);
        }

        public Builder add(String fieldName, String value, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, exactPredicate(fieldName), exactPredicate(value), resolver));
            return this;
        }

        // --- regex field + regex value ---

        public Builder addByPattern(String fieldRegex, String valueRegex, String alias) {
            return addByPattern(fieldRegex, valueRegex, v -> alias);
        }

        public Builder addByPattern(String fieldRegex, String valueRegex, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, regexPredicate(fieldRegex), regexPredicate(valueRegex), resolver));
            return this;
        }

        // --- regex field only (any value) ---

        public Builder addByPattern(String fieldRegex, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, regexPredicate(fieldRegex), null, resolver));
            return this;
        }

        // --- fluent entry builder ---

        public EntryBuilder entry() {
            return new EntryBuilder(this);
        }

        // --- merge another map ---

        public Builder merge(AliasMap other) {
            entries.addAll(other.getEntries());
            return this;
        }

        public AliasMap build() {
            return new AliasMap(entries);
        }

        void addEntry(AliasEntry entry) {
            entries.add(entry);
        }
    }

    // -------------------------------------------------------------------------
    // EntryBuilder (fluent, for complex rules)
    // -------------------------------------------------------------------------

    public static final class EntryBuilder {

        private final Builder parent;
        private Predicate<String> pathMatcher;
        private Predicate<String> fieldMatcher;
        private Predicate<String> valueMatcher;
        private Function<String, String> resolver;

        private EntryBuilder(Builder parent) {
            this.parent = parent;
        }

        public EntryBuilder path(Predicate<String> predicate) {
            this.pathMatcher = predicate;
            return this;
        }

        public EntryBuilder field(Predicate<String> predicate) {
            this.fieldMatcher = predicate;
            return this;
        }

        public EntryBuilder field(String exactFieldName) {
            this.fieldMatcher = exactPredicate(exactFieldName);
            return this;
        }

        public EntryBuilder fieldPattern(String fieldRegex) {
            this.fieldMatcher = regexPredicate(fieldRegex);
            return this;
        }

        public EntryBuilder value(Predicate<String> predicate) {
            this.valueMatcher = predicate;
            return this;
        }

        public EntryBuilder value(String exactValue) {
            this.valueMatcher = exactPredicate(exactValue);
            return this;
        }

        public EntryBuilder valuePattern(String valueRegex) {
            this.valueMatcher = regexPredicate(valueRegex);
            return this;
        }

        public EntryBuilder alias(String alias) {
            this.resolver = v -> alias;
            return this;
        }

        public EntryBuilder alias(Function<String, String> resolver) {
            this.resolver = resolver;
            return this;
        }

        public Builder register() {
            parent.addEntry(new AliasEntry(pathMatcher, fieldMatcher, valueMatcher, resolver));
            return parent;
        }
    }

    // -------------------------------------------------------------------------
    // Predicate factories
    // -------------------------------------------------------------------------

    private static Predicate<String> exactPredicate(String value) {
        return s -> s.equals(value);
    }

    private static Predicate<String> regexPredicate(String regex) {
        Pattern compiled = Pattern.compile(regex);
        return s -> compiled.matcher(s).matches();
    }
}
