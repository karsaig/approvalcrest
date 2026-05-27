package com.github.karsaig.approvalcrest.matcher.alias;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A single alias substitution rule. Each field is optional (null = wildcard).
 * The resolver is required and maps the raw coerced primitive value to the alias string.
 *
 * <p>The three metadata fields ({@code exactFieldKey}, {@code exactValueKey}, {@code staticAlias})
 * are set at construction time to enable fast-path strategy selection in {@link AliasMap}.
 */
class AliasEntry {

    /** Literal field name when the field matcher is exact-string equality; null otherwise. */
    final String exactFieldKey;
    /** Literal value when the value matcher is exact-string equality; null otherwise. */
    final String exactValueKey;
    /** Constant alias string when the resolver is a constant function; null for lambda/function resolvers. */
    final String staticAlias;

    private final Predicate<String> pathMatcher;
    private final Predicate<String> fieldMatcher;
    private final Predicate<String> valueMatcher;
    private final Function<String, String> resolver;

    AliasEntry(String exactFieldKey, String exactValueKey, String staticAlias,
               Predicate<String> pathMatcher, Predicate<String> fieldMatcher,
               Predicate<String> valueMatcher, Function<String, String> resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("resolver must not be null");
        }
        this.exactFieldKey = exactFieldKey;
        this.exactValueKey = exactValueKey;
        this.staticAlias = staticAlias;
        this.pathMatcher = pathMatcher;
        this.fieldMatcher = fieldMatcher;
        this.valueMatcher = valueMatcher;
        this.resolver = resolver;
    }

    /**
     * Returns true when this entry qualifies for the {@link ExactAliasMapStrategy}:
     * exact field name, exact value, static alias, and no path constraint.
     */
    boolean isFullyExact() {
        return exactFieldKey != null
                && exactValueKey != null
                && staticAlias != null
                && pathMatcher == null;
    }

    /**
     * Returns true if this entry matches the given path, field name, and coerced primitive value.
     */
    boolean matches(String path, String fieldName, String coercedValue) {
        if (pathMatcher != null && !pathMatcher.test(path)) {
            return false;
        }
        if (fieldMatcher != null && !fieldMatcher.test(fieldName)) {
            return false;
        }
        if (valueMatcher != null && !valueMatcher.test(coercedValue)) {
            return false;
        }
        return true;
    }

    String resolve(String coercedValue) {
        return resolver.apply(coercedValue);
    }
}
