package com.github.karsaig.approvalcrest.matcher.alias;

import java.util.ArrayList;
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
 *
 * <p>Internally, two resolution strategies are chosen at build time:
 * <ul>
 *   <li>{@link ExactAliasMapStrategy} — used when every entry has an exact field name, an exact
 *       value, a static alias string, and no path constraint. Resolution is O(1).</li>
 *   <li>{@link IndexedAliasMapStrategy} — used for all other cases. Entries are indexed by their
 *       exact field key so that only entries for the queried field are scanned.</li>
 * </ul>
 */
public final class AliasMap {

    private final ResolveStrategy strategy;

    AliasMap(List<AliasEntry> entries) {
        this.strategy = chooseStrategy(entries);
    }

    private static ResolveStrategy chooseStrategy(List<AliasEntry> entries) {
        for (AliasEntry e : entries) {
            if (!e.isFullyExact()) {
                return new IndexedAliasMapStrategy(entries);
            }
        }
        return new ExactAliasMapStrategy(entries);
    }

    /**
     * Resolves an alias for the given path, field name, and coerced primitive value.
     * The last registered matching rule wins.
     */
    public Optional<String> resolve(String path, String fieldName, String coercedValue) {
        return strategy.resolve(path, fieldName, coercedValue);
    }

    /**
     * Returns a new {@code AliasMap} containing the entries of this map followed by the entries
     * of {@code other}. Because last-match-wins, {@code other}'s entries take precedence when
     * both match the same primitive.
     */
    public AliasMap merge(AliasMap other) {
        List<AliasEntry> merged = new ArrayList<>(strategy.getEntries());
        merged.addAll(other.strategy.getEntries());
        return new AliasMap(merged);
    }

    public boolean isEmpty() {
        return strategy.isEmpty();
    }

    List<AliasEntry> getEntries() {
        return strategy.getEntries();
    }

    /** Package-visible for tests: returns true when the Tier-2 exact strategy is active. */
    boolean usesExactStrategy() {
        return strategy instanceof ExactAliasMapStrategy;
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
            entries.add(new AliasEntry(null, value, alias, null, null, exactPredicate(value), v -> alias));
            return this;
        }

        public Builder add(String value, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, value, null, null, null, exactPredicate(value), resolver));
            return this;
        }

        // --- field + value ---

        public Builder add(String fieldName, String value, String alias) {
            entries.add(new AliasEntry(fieldName, value, alias, null, exactPredicate(fieldName), exactPredicate(value), v -> alias));
            return this;
        }

        public Builder add(String fieldName, String value, Function<String, String> resolver) {
            entries.add(new AliasEntry(fieldName, value, null, null, exactPredicate(fieldName), exactPredicate(value), resolver));
            return this;
        }

        // --- regex field + regex value ---

        public Builder addByPattern(Pattern fieldPattern, Pattern valuePattern, String alias) {
            entries.add(new AliasEntry(null, null, null, null, patternPredicate(fieldPattern), patternPredicate(valuePattern), v -> alias));
            return this;
        }

        public Builder addByPattern(Pattern fieldPattern, Pattern valuePattern, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, null, null, null, patternPredicate(fieldPattern), patternPredicate(valuePattern), resolver));
            return this;
        }

        // --- regex field only (any value) ---

        public Builder addByPattern(Pattern fieldPattern, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, null, null, null, patternPredicate(fieldPattern), null, resolver));
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
        // Metadata for strategy selection
        private String exactFieldKey;
        private String exactValueKey;
        private String staticAlias;

        private EntryBuilder(Builder parent) {
            this.parent = parent;
        }

        public EntryBuilder path(Predicate<String> predicate) {
            this.pathMatcher = predicate;
            return this;
        }

        public EntryBuilder field(Predicate<String> predicate) {
            this.fieldMatcher = predicate;
            this.exactFieldKey = null;
            return this;
        }

        public EntryBuilder field(String exactFieldName) {
            this.fieldMatcher = exactPredicate(exactFieldName);
            this.exactFieldKey = exactFieldName;
            return this;
        }

        public EntryBuilder fieldPattern(Pattern pattern) {
            this.fieldMatcher = patternPredicate(pattern);
            this.exactFieldKey = null;
            return this;
        }

        public EntryBuilder value(Predicate<String> predicate) {
            this.valueMatcher = predicate;
            this.exactValueKey = null;
            return this;
        }

        public EntryBuilder value(String exactValue) {
            this.valueMatcher = exactPredicate(exactValue);
            this.exactValueKey = exactValue;
            return this;
        }

        public EntryBuilder valuePattern(Pattern pattern) {
            this.valueMatcher = patternPredicate(pattern);
            this.exactValueKey = null;
            return this;
        }

        public EntryBuilder alias(String alias) {
            this.resolver = v -> alias;
            this.staticAlias = alias;
            return this;
        }

        public EntryBuilder alias(Function<String, String> resolver) {
            this.resolver = resolver;
            this.staticAlias = null;
            return this;
        }

        public Builder register() {
            parent.addEntry(new AliasEntry(exactFieldKey, exactValueKey, staticAlias,
                    pathMatcher, fieldMatcher, valueMatcher, resolver));
            return parent;
        }
    }

    // -------------------------------------------------------------------------
    // Predicate factories
    // -------------------------------------------------------------------------

    static Predicate<String> exactPredicate(String value) {
        return s -> s.equals(value);
    }

    static Predicate<String> patternPredicate(Pattern pattern) {
        return s -> pattern.matcher(s).matches();
    }
}

