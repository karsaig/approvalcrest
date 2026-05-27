package com.github.karsaig.approvalcrest.matcher.alias;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AliasMap} builder, matching logic, and merge behaviour.
 */
public class AliasMapTest {

    // -------------------------------------------------------------------------
    // Basic exact-value matching
    // -------------------------------------------------------------------------

    @Test
    void exactValueMatchReturnsAlias() {
        AliasMap map = AliasMap.builder()
                .add("abc-123", "<id>")
                .build();

        Optional<String> result = map.resolve("response.id", "id", "abc-123");

        assertThat(result, is(Optional.of("<id>")));
    }

    @Test
    void exactValueNoMatchReturnsEmpty() {
        AliasMap map = AliasMap.builder()
                .add("abc-123", "<id>")
                .build();

        Optional<String> result = map.resolve("response.id", "id", "xyz-999");

        assertThat(result, is(Optional.empty()));
    }

    @Test
    void emptyMapAlwaysReturnsEmpty() {
        AliasMap map = AliasMap.builder().build();

        assertThat(map.resolve("any.path", "field", "anything"), is(Optional.empty()));
        assertThat(map.isEmpty(), is(true));
    }

    // -------------------------------------------------------------------------
    // Field-scoped matching
    // -------------------------------------------------------------------------

    @Test
    void fieldScopedRuleMatchesCorrectField() {
        AliasMap map = AliasMap.builder()
                .add("id", "abc-123", "<userId>")
                .build();

        assertThat(map.resolve("user.id", "id", "abc-123"), is(Optional.of("<userId>")));
    }

    @Test
    void fieldScopedRuleDoesNotMatchWrongField() {
        AliasMap map = AliasMap.builder()
                .add("id", "abc-123", "<userId>")
                .build();

        assertThat(map.resolve("user.name", "name", "abc-123"), is(Optional.empty()));
    }

    // -------------------------------------------------------------------------
    // Regex matching
    // -------------------------------------------------------------------------

    @Test
    void regexFieldMatchesMatchingField() {
        AliasMap map = AliasMap.builder()
                .addByPattern(Pattern.compile(".*[Ii]d.*"), Pattern.compile("[a-f0-9\\-]{36}"), "<uuid>")
                .build();

        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        assertThat(map.resolve("user.userId", "userId", uuid), is(Optional.of("<uuid>")));
    }

    @Test
    void regexFieldDoesNotMatchNonMatchingField() {
        AliasMap map = AliasMap.builder()
                .addByPattern(Pattern.compile(".*[Ii]d.*"), Pattern.compile("[a-f0-9\\-]{36}"), "<uuid>")
                .build();

        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        assertThat(map.resolve("user.name", "name", uuid), is(Optional.empty()));
    }

    @Test
    void regexFieldOnlyWithDynamicAlias() {
        AliasMap map = AliasMap.builder()
                .addByPattern(Pattern.compile(".*[Ii]d$"), v -> "<id:" + v.substring(0, 4) + "...>")
                .build();

        Optional<String> result = map.resolve("user.userId", "userId", "abc-123-xyz");
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is("<id:abc-...>"));
    }

    // -------------------------------------------------------------------------
    // Predicate-based entry builder
    // -------------------------------------------------------------------------

    @Test
    void entryBuilderWithAllPredicates() {
        AliasMap map = AliasMap.builder()
                .entry()
                    .path(p -> p.startsWith("response"))
                    .field("id")
                    .value("secret-value")
                    .alias("<secret>")
                .register()
                .build();

        assertThat(map.resolve("response.id", "id", "secret-value"), is(Optional.of("<secret>")));
        assertThat(map.resolve("request.id", "id", "secret-value"), is(Optional.empty()));
    }

    @Test
    void entryBuilderFieldExact() {
        AliasMap map = AliasMap.builder()
                .entry()
                    .field("token")
                    .value("T-12345")
                    .alias("<token>")
                .register()
                .build();

        assertThat(map.resolve("auth.token", "token", "T-12345"), is(Optional.of("<token>")));
        assertThat(map.resolve("auth.other", "other", "T-12345"), is(Optional.empty()));
    }

    @Test
    void entryBuilderFieldPattern() {
        AliasMap map = AliasMap.builder()
                .entry()
                    .fieldPattern(Pattern.compile(".*[Tt]oken.*"))
                    .value("T-12345")
                    .alias("<token>")
                .register()
                .build();

        assertThat(map.resolve("auth.accessToken", "accessToken", "T-12345"), is(Optional.of("<token>")));
    }

    @Test
    void entryBuilderValuePattern() {
        AliasMap map = AliasMap.builder()
                .entry()
                    .valuePattern(Pattern.compile("\\d{4}-\\d{2}-\\d{2}T.*"))
                    .alias("<timestamp>")
                .register()
                .build();

        assertThat(map.resolve("event.createdAt", "createdAt", "2024-01-15T10:30:00Z"), is(Optional.of("<timestamp>")));
        assertThat(map.resolve("event.name", "name", "myEvent"), is(Optional.empty()));
    }

    @Test
    void entryBuilderWithDynamicAlias() {
        AliasMap map = AliasMap.builder()
                .entry()
                    .field("id")
                    .alias(v -> "<id:" + v.length() + ">")
                .register()
                .build();

        assertThat(map.resolve("item.id", "id", "abc12"), is(Optional.of("<id:5>")));
    }

    // -------------------------------------------------------------------------
    // Last-match-wins (reverse iteration)
    // -------------------------------------------------------------------------

    @Test
    void lastRegisteredEntryWinsWhenBothMatch() {
        AliasMap map = AliasMap.builder()
                .add("target", "<first>")
                .add("target", "<second>")
                .build();

        assertThat(map.resolve("any.field", "field", "target"), is(Optional.of("<second>")));
    }

    @Test
    void firstEntryUsedWhenOnlyFirstMatches() {
        AliasMap map = AliasMap.builder()
                .add("target", "<first>")
                .add("other-value", "<second>")
                .build();

        assertThat(map.resolve("any.field", "field", "target"), is(Optional.of("<first>")));
    }

    // -------------------------------------------------------------------------
    // Merge
    // -------------------------------------------------------------------------

    @Test
    void mergeAppendsEntriesAndLastWins() {
        AliasMap base = AliasMap.builder()
                .add("base-value", "<base>")
                .add("shared", "<from-base>")
                .build();

        AliasMap override = AliasMap.builder()
                .add("shared", "<from-override>")
                .build();

        AliasMap merged = base.merge(override);

        assertThat(merged.resolve("x", "f", "base-value"), is(Optional.of("<base>")));
        assertThat(merged.resolve("x", "f", "shared"), is(Optional.of("<from-override>")));
    }

    @Test
    void mergeDoesNotMutateOriginalMaps() {
        AliasMap base = AliasMap.builder().add("v", "<base>").build();
        AliasMap other = AliasMap.builder().add("v", "<other>").build();

        AliasMap merged = base.merge(other);

        assertThat(base.resolve("x", "f", "v"), is(Optional.of("<base>")));
        assertThat(other.resolve("x", "f", "v"), is(Optional.of("<other>")));
        assertThat(merged.resolve("x", "f", "v"), is(Optional.of("<other>")));
    }

    @Test
    void builderMergeMethod() {
        AliasMap existing = AliasMap.builder().add("v", "<existing>").build();

        AliasMap result = AliasMap.builder()
                .add("v", "<new>")
                .merge(existing)
                .build();

        // existing was appended last → it wins
        assertThat(result.resolve("x", "f", "v"), is(Optional.of("<existing>")));
    }

    // -------------------------------------------------------------------------
    // Number coercion (int and String "13" both become "13")
    // -------------------------------------------------------------------------

    @Test
    void numericValueIsCoercedToStringForMatching() {
        // In actual use the coercion happens in applyAliases; here we test the map itself
        // with an already-coerced value.
        AliasMap map = AliasMap.builder()
                .add("13", "<thirteen>")
                .build();

        // "13" coming from a JsonPrimitive number (coerced via getAsString())
        assertThat(map.resolve("obj.count", "count", "13"), is(Optional.of("<thirteen>")));
        // "13" coming from a JsonPrimitive string
        assertThat(map.resolve("obj.label", "label", "13"), is(Optional.of("<thirteen>")));
    }

    // -------------------------------------------------------------------------
    // Builder validates resolver not-null
    // -------------------------------------------------------------------------

    @Test
    void entryBuilderRequiresAlias() {
        assertThrows(IllegalArgumentException.class, () ->
            AliasMap.builder()
                .entry()
                    .field("id")
                    .value("x")
                    // no .alias() call
                .register()
                .build()
        );
    }

    // -------------------------------------------------------------------------
    // Strategy selection: ExactAliasMapStrategy (Tier 2)
    // -------------------------------------------------------------------------

    @Test
    void allExactEntriesUseExactStrategy() {
        AliasMap map = AliasMap.builder()
                .add("userId", "abc-123", "<id>")
                .add("name", "Alice", "<name>")
                .build();

        assertThat(map.usesExactStrategy(), is(true));
    }

    @Test
    void valueOnlyEntryDowngradesToIndexedStrategy() {
        // add(value, alias) has no field key → not fully exact
        AliasMap map = AliasMap.builder()
                .add("abc-123", "<id>")
                .build();

        assertThat(map.usesExactStrategy(), is(false));
    }

    @Test
    void regexEntryDowngradesToIndexedStrategy() {
        AliasMap map = AliasMap.builder()
                .addByPattern(Pattern.compile(".*[Ii]d.*"), Pattern.compile("[a-f0-9\\-]{36}"), "<uuid>")
                .build();

        assertThat(map.usesExactStrategy(), is(false));
    }

    @Test
    void functionResolverDowngradesToIndexedStrategy() {
        AliasMap map = AliasMap.builder()
                .add("id", "abc-123", v -> "<id:" + v.length() + ">")
                .build();

        assertThat(map.usesExactStrategy(), is(false));
    }

    @Test
    void pathConstraintDowngradesToIndexedStrategy() {
        AliasMap map = AliasMap.builder()
                .entry()
                    .path(p -> p.startsWith("response"))
                    .field("id")
                    .value("abc")
                    .alias("<id>")
                .register()
                .build();

        assertThat(map.usesExactStrategy(), is(false));
    }

    @Test
    void emptyMapUsesExactStrategy() {
        AliasMap map = AliasMap.builder().build();

        // An empty map trivially satisfies the all-exact condition.
        assertThat(map.usesExactStrategy(), is(true));
        assertThat(map.isEmpty(), is(true));
    }

    @Test
    void exactStrategyResolvesCorrectly() {
        AliasMap map = AliasMap.builder()
                .add("userId", "abc-123", "<id>")
                .add("name", "Alice", "<name>")
                .build();

        assertThat(map.resolve("user.userId", "userId", "abc-123"), is(Optional.of("<id>")));
        assertThat(map.resolve("user.name", "name", "Alice"), is(Optional.of("<name>")));
        // wrong field → no match
        assertThat(map.resolve("user.name", "name", "abc-123"), is(Optional.empty()));
        // wrong value → no match
        assertThat(map.resolve("user.userId", "userId", "unknown"), is(Optional.empty()));
    }

    @Test
    void exactStrategyLastRegisteredWins() {
        AliasMap map = AliasMap.builder()
                .add("id", "v", "<first>")
                .add("id", "v", "<second>")
                .build();

        assertThat(map.usesExactStrategy(), is(true));
        assertThat(map.resolve("x", "id", "v"), is(Optional.of("<second>")));
    }

    @Test
    void mergeTwoExactMapsStaysExact() {
        AliasMap a = AliasMap.builder().add("id", "val-a", "<a>").build();
        AliasMap b = AliasMap.builder().add("name", "val-b", "<b>").build();

        AliasMap merged = a.merge(b);

        assertThat(merged.usesExactStrategy(), is(true));
        assertThat(merged.resolve("x", "id", "val-a"), is(Optional.of("<a>")));
        assertThat(merged.resolve("x", "name", "val-b"), is(Optional.of("<b>")));
    }

    @Test
    void mergeExactWithIndexedDowngradesToIndexed() {
        AliasMap exact = AliasMap.builder().add("id", "v", "<id>").build();
        AliasMap indexed = AliasMap.builder().add("v", "<wild>").build(); // value-only → indexed

        AliasMap merged = exact.merge(indexed);

        assertThat(merged.usesExactStrategy(), is(false));
    }

    // -------------------------------------------------------------------------
    // Cross-bucket ordering: wildcard vs exact-field entries in IndexedStrategy
    // -------------------------------------------------------------------------

    @Test
    void wildcardEntryRegisteredAfterExactFieldEntryWins() {
        // exact-field entry first, then a value-only (wildcard) entry for the same value
        AliasMap map = AliasMap.builder()
                .add("id", "target", "<from-exact>")   // exact field, registered first
                .add("target", "<from-wildcard>")       // wildcard field, registered second → should win
                .build();

        assertThat(map.usesExactStrategy(), is(false)); // value-only → indexed
        assertThat(map.resolve("x", "id", "target"), is(Optional.of("<from-wildcard>")));
    }

    @Test
    void exactFieldEntryRegisteredAfterWildcardEntryWins() {
        // wildcard entry first, exact-field entry second
        AliasMap map = AliasMap.builder()
                .add("target", "<from-wildcard>")       // wildcard field, registered first
                .add("id", "target", "<from-exact>")   // exact field, registered second → should win
                .build();

        assertThat(map.usesExactStrategy(), is(false));
        assertThat(map.resolve("x", "id", "target"), is(Optional.of("<from-exact>")));
    }

    @Test
    void wildcardDoesNotAffectNonMatchingField() {
        AliasMap map = AliasMap.builder()
                .add("target", "<from-wildcard>")
                .add("id", "target", "<from-exact>")
                .build();

        // "name" field: exact entry doesn't match (field is "id"), wildcard does
        assertThat(map.resolve("x", "name", "target"), is(Optional.of("<from-wildcard>")));
    }
}

