<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=4629c939-407c-461a-bbee-8b42aefcef3a" />

# aliasing

Replace volatile runtime values with stable aliases in approved files.

Aliases let approved files contain human-readable placeholders (e.g. `<userId>`) instead of actual volatile values that change every run. The approved file documents the field clearly without being tied to a specific runtime value.

Works with `sameJsonAsApproved` and `sameBeanAs`.

**Applies to primitives only.** Aliases match against `String` and numeric values (numbers are coerced to their string form — `13` matches the rule for `"13"`). Boolean (`true`/`false`) and `null` values are never aliased. Custom types that are normally serialised as JSON objects are not aliasable by default — register a custom Gson adapter that serialises the type as a JSON string to make it aliasable (see [supported-types](supported-types.md#custom-types)).

## By Value (Global)

Any occurrence of the runtime value across the entire serialised output is replaced with the alias:

```java
assertThat(actual, sameJsonAsApproved()
    .withAlias("runtime-uuid-1234", "<userId>"));
```

In the approved file:

```json
{
  "userId": "<userId>"
}
```

Because the alias is applied before writing the not-approved file on first run, you review and approve a file that already contains `<userId>` — you never see the raw runtime value in source control.

## By Field and Value (Scoped)

Replace the value only when it appears in a specific named field. This is useful when the same raw value appears in multiple fields but you want to alias only one of them:

```java
// Replaces "volatile-uuid-123" only in the field named "userId";
// the same value in any other field is left untouched
assertThat(actual, sameJsonAsApproved()
    .withAlias("userId", "volatile-uuid-123", "<userId>"));
```

## Multiple Aliases

`withAlias` calls accumulate — each call adds a new rule, later rules win on conflict:

```java
assertThat(actual, sameJsonAsApproved()
    .withAlias("uuid-111", "<orderId>")
    .withAlias("uuid-222", "<customerId>"));
```

## Batch Rules with `AliasMap`

`AliasMap` collects multiple rules. Pass it to the matcher with `withAliasMap`. Multiple `withAliasMap` calls accumulate — they do not replace each other.

### Simple value replacement

```java
import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;

AliasMap aliases = AliasMap.builder()
    .add("runtime-uuid-1234", "<userId>")
    .add("secret-token-abc",  "<token>")
    .build();

assertThat(actual, sameJsonAsApproved()
    .withAliasMap(aliases));
```

### Field-scoped rule in `AliasMap`

```java
AliasMap aliases = AliasMap.builder()
    .add("userId",  "volatile-uuid-1", "<userId>")
    .add("orderId", "volatile-uuid-2", "<orderId>")
    .build();
```

### Dynamic alias via resolver function

Instead of a static alias string, provide a `Function<String, String>` that receives the raw value and returns the alias. This is useful when the alias should encode information from the value itself:

```java
AliasMap aliases = AliasMap.builder()
    // Captures the numeric suffix: "user-42" → "<user:42>"
    .add("user-42", v -> "<user:" + v.split("-")[1] + ">")
    .build();
```

### Regex-based field and value matching

Use `addByPattern` to match field names and values against regular expressions:

```java
import java.util.regex.Pattern;

AliasMap aliases = AliasMap.builder()
    // Replace any value matching the date-time pattern, in any field containing "Date"
    .addByPattern(
        Pattern.compile(".*Date"),
        Pattern.compile("\\d{4}-\\d{2}-\\d{2}T.*"),
        "<timestamp>")
    .build();
```

To alias every value in fields whose name matches a pattern (regardless of the value), use `addByPattern` with a resolver function:

```java
AliasMap aliases = AliasMap.builder()
    // All fields ending in "Id" or "id": alias each value, preserving a short prefix
    .addByPattern(
        Pattern.compile(".*[Ii]d$"),
        v -> "<id:" + v.substring(0, Math.min(4, v.length())) + "...>")
    .build();
```

For value-pattern-only matching (any field name), use the fluent `entry()` builder below.

### Full predicate entry builder

For the most complex rules, use the fluent `entry()` builder. Every constraint is optional — omit any you don't need:

```java
AliasMap aliases = AliasMap.builder()
    .entry()
        .path(p -> p.startsWith("response"))     // JSON path to the field (optional)
        .field("id")                              // exact field name (optional)
        .valuePattern(Pattern.compile("\\d{8}"))  // value regex (optional)
        .alias("<responseId>")                    // static alias or Function<String,String>
    .register()
    .build();
```

Available predicates on `EntryBuilder`:
- `.path(Predicate<String>)` — matches the full dot-path to the value (e.g. `"order.lineItems.productId"`)
- `.field(String)` — exact field name
- `.field(Predicate<String>)` — arbitrary field-name predicate
- `.fieldPattern(Pattern)` — field name regex
- `.value(String)` — exact value
- `.value(Predicate<String>)` — arbitrary value predicate
- `.valuePattern(Pattern)` — value regex
- `.alias(String)` — static alias
- `.alias(Function<String, String>)` — dynamic resolver

## Behaviour with `sameBeanAs`

With file-based matchers (`sameJsonAsApproved`), aliasing replaces values before writing and comparing the approved file — only the alias appears in source control.

With `sameBeanAs`, both the **actual** and the **expected** object go through aliasing before comparison. This means two objects with different raw values for the same field can still match, as long as both values map to the same alias:

```java
// actual.userId = "uuid-111", expected.userId = "uuid-999"
// Both are aliased to "<userId>" before comparison → test passes
assertThat(actual, sameBeanAs(expected)
    .withAlias("uuid-111", "<userId>")
    .withAlias("uuid-999", "<userId>"));
```

## Interaction with Sorting

Aliases are applied **before** sorting. If two elements sort differently after aliasing than they would on their raw values, the aliased order is what the approved file must reflect. Keep this in mind when combining `withAlias` and `sortField`.

## Last-registered Wins

When two rules match the same value, the **last registered** rule wins. This applies across both `withAlias` calls and `withAliasMap` calls:

```java
// "<override>" wins because it was registered last
assertThat(actual, sameJsonAsApproved()
    .withAliasMap(baseAliases)      // contains: "shared-value" → "<base>"
    .withAliasMap(overrideAliases)  // contains: "shared-value" → "<override>"
);
```

## Related

- [dynamic-values](dynamic-values.md) — choosing between ignore, alias, and custom matching
- [supported-types](supported-types.md) — how types are serialised; registering custom Gson adapters
