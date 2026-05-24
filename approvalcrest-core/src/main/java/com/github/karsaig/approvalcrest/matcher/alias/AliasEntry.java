package com.github.karsaig.approvalcrest.matcher.alias;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A single alias substitution rule. Each field is optional (null = wildcard).
 * The resolver is required and maps the raw coerced primitive value to the alias string.
 */
class AliasEntry {

    private final Predicate<String> pathMatcher;
    private final Predicate<String> fieldMatcher;
    private final Predicate<String> valueMatcher;
    private final Function<String, String> resolver;

    AliasEntry(Predicate<String> pathMatcher, Predicate<String> fieldMatcher,
               Predicate<String> valueMatcher, Function<String, String> resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("resolver must not be null");
        }
        this.pathMatcher = pathMatcher;
        this.fieldMatcher = fieldMatcher;
        this.valueMatcher = valueMatcher;
        this.resolver = resolver;
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
