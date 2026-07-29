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
     *
     * @param path         JSON path of the primitive being considered
     * @param fieldName    name of the field holding it
     * @param coercedValue the primitive value, coerced to a String
     * @return the replacement text, or empty when no rule matches
     */
    public Optional<String> resolve(String path, String fieldName, String coercedValue) {
        return strategy.resolve(path, fieldName, coercedValue);
    }

    /**
     * Returns a new {@code AliasMap} containing the entries of this map followed by the entries
     * of {@code other}. Because last-match-wins, {@code other}'s entries take precedence when
     * both match the same primitive.
     *
     * @param other the map whose entries are appended
     * @return a new map containing both sets of entries
     */
    public AliasMap merge(AliasMap other) {
        List<AliasEntry> merged = new ArrayList<>(strategy.getEntries());
        merged.addAll(other.strategy.getEntries());
        return new AliasMap(merged);
    }

    /**
     * Reports whether this map has any rules.
     *
     * @return true when no alias rules are registered, in which case aliasing is skipped entirely
     */
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

    /**
     * Starts building a set of alias rules.
     *
     * @return a builder for assembling alias rules
     */
    public static Builder builder() {
        return new Builder();
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /**
     * Collects alias rules. Rules are evaluated last-match-wins, so a rule added later overrides an
     * earlier one that matches the same primitive.
     */
    public static final class Builder {

        private final List<AliasEntry> entries = new ArrayList<>();

        private Builder() {
        }

        // --- value only ---

        /**
         * Replaces every occurrence of an exact value, in any field, with a fixed alias.
         *
         * @param value the primitive value to match exactly
         * @param alias the text to substitute
         * @return this builder
         */
        public Builder add(String value, String alias) {
            entries.add(new AliasEntry(null, value, alias, null, null, exactPredicate(value), v -> alias));
            return this;
        }

        /**
         * As {@link #add(String, String)}, but the replacement is computed from the matched value.
         *
         * @param value    the primitive value to match exactly
         * @param resolver receives the matched value and returns the text to substitute
         * @return this builder
         */
        public Builder add(String value, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, value, null, null, null, exactPredicate(value), resolver));
            return this;
        }

        // --- field + value ---

        /**
         * Replaces an exact value with a fixed alias, but only within the named field.
         *
         * @param fieldName the field the rule applies to
         * @param value     the primitive value to match exactly
         * @param alias     the text to substitute
         * @return this builder
         */
        public Builder add(String fieldName, String value, String alias) {
            entries.add(new AliasEntry(fieldName, value, alias, null, exactPredicate(fieldName), exactPredicate(value), v -> alias));
            return this;
        }

        /**
         * As {@link #add(String, String, String)}, but the replacement is computed from the matched
         * value.
         *
         * @param fieldName the field the rule applies to
         * @param value     the primitive value to match exactly
         * @param resolver  receives the matched value and returns the text to substitute
         * @return this builder
         */
        public Builder add(String fieldName, String value, Function<String, String> resolver) {
            entries.add(new AliasEntry(fieldName, value, null, null, exactPredicate(fieldName), exactPredicate(value), resolver));
            return this;
        }

        // --- regex field + regex value ---

        /**
         * Replaces values matching a pattern with a fixed alias, in fields matching a pattern.
         *
         * @param fieldPattern matched against the field name
         * @param valuePattern matched against the primitive value
         * @param alias        the text to substitute
         * @return this builder
         */
        public Builder addByPattern(Pattern fieldPattern, Pattern valuePattern, String alias) {
            entries.add(new AliasEntry(null, null, null, null, patternPredicate(fieldPattern), patternPredicate(valuePattern), v -> alias));
            return this;
        }

        /**
         * As {@link #addByPattern(Pattern, Pattern, String)}, but the replacement is computed from
         * the matched value.
         *
         * @param fieldPattern matched against the field name
         * @param valuePattern matched against the primitive value
         * @param resolver     receives the matched value and returns the text to substitute
         * @return this builder
         */
        public Builder addByPattern(Pattern fieldPattern, Pattern valuePattern, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, null, null, null, patternPredicate(fieldPattern), patternPredicate(valuePattern), resolver));
            return this;
        }

        // --- regex field only (any value) ---

        /**
         * Applies to every value in fields matching the pattern, whatever the value.
         *
         * @param fieldPattern matched against the field name
         * @param resolver     receives the matched value and returns the text to substitute
         * @return this builder
         */
        public Builder addByPattern(Pattern fieldPattern, Function<String, String> resolver) {
            entries.add(new AliasEntry(null, null, null, null, patternPredicate(fieldPattern), null, resolver));
            return this;
        }

        // --- fluent entry builder ---

        /**
         * Starts a fluent rule for cases the {@code add} overloads cannot express, such as matching
         * on the JSON path.
         *
         * @return a builder for a single rule, returning here when completed
         */
        public EntryBuilder entry() {
            return new EntryBuilder(this);
        }

        // --- merge another map ---

        /**
         * Appends another map's rules to this builder. Since last-match-wins, {@code other}'s rules
         * take precedence over those already added.
         *
         * @param other the map whose rules to append
         * @return this builder
         */
        public Builder merge(AliasMap other) {
            entries.addAll(other.getEntries());
            return this;
        }

        /**
         * Completes the map.
         *
         * @return an immutable {@code AliasMap} of the rules added so far
         */
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

    /**
     * Assembles a single alias rule from optional path, field and value constraints, then
     * {@link #register()}s it with the enclosing {@link Builder}. Any constraint left unset matches
     * everything.
     */
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

        /**
         * Restricts the rule to JSON paths satisfying the predicate.
         *
         * @param predicate receives the path of the primitive being considered
         * @return this builder
         */
        public EntryBuilder path(Predicate<String> predicate) {
            this.pathMatcher = predicate;
            return this;
        }

        /**
         * Restricts the rule to field names satisfying the predicate.
         *
         * @param predicate receives the field name
         * @return this builder
         */
        public EntryBuilder field(Predicate<String> predicate) {
            this.fieldMatcher = predicate;
            this.exactFieldKey = null;
            return this;
        }

        /**
         * Restricts the rule to one field name.
         *
         * @param exactFieldName the field name to match exactly
         * @return this builder
         */
        public EntryBuilder field(String exactFieldName) {
            this.fieldMatcher = exactPredicate(exactFieldName);
            this.exactFieldKey = exactFieldName;
            return this;
        }

        /**
         * Restricts the rule to field names matching the pattern.
         *
         * @param pattern matched against the field name
         * @return this builder
         */
        public EntryBuilder fieldPattern(Pattern pattern) {
            this.fieldMatcher = patternPredicate(pattern);
            this.exactFieldKey = null;
            return this;
        }

        /**
         * Restricts the rule to values satisfying the predicate.
         *
         * @param predicate receives the primitive value
         * @return this builder
         */
        public EntryBuilder value(Predicate<String> predicate) {
            this.valueMatcher = predicate;
            this.exactValueKey = null;
            return this;
        }

        /**
         * Restricts the rule to one value.
         *
         * @param exactValue the primitive value to match exactly
         * @return this builder
         */
        public EntryBuilder value(String exactValue) {
            this.valueMatcher = exactPredicate(exactValue);
            this.exactValueKey = exactValue;
            return this;
        }

        /**
         * Restricts the rule to values matching the pattern.
         *
         * @param pattern matched against the primitive value
         * @return this builder
         */
        public EntryBuilder valuePattern(Pattern pattern) {
            this.valueMatcher = patternPredicate(pattern);
            this.exactValueKey = null;
            return this;
        }

        /**
         * Substitutes a fixed text for whatever this rule matches.
         *
         * @param alias the text to substitute
         * @return this builder
         */
        public EntryBuilder alias(String alias) {
            this.resolver = v -> alias;
            this.staticAlias = alias;
            return this;
        }

        /**
         * Computes the substitution from the matched value.
         *
         * @param resolver receives the matched value and returns the text to substitute
         * @return this builder
         */
        public EntryBuilder alias(Function<String, String> resolver) {
            this.resolver = resolver;
            this.staticAlias = null;
            return this;
        }

        /**
         * Adds the assembled rule and returns to the enclosing builder.
         *
         * @return the {@link Builder} this rule was started from
         */
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

