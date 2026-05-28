# aliasing

Replace volatile runtime values with stable aliases in approved files.

Aliases let approved files contain human-readable placeholders (e.g. `<userId>`) instead of actual volatile values that change every run. The approved file documents the field clearly without being tied to a specific runtime value.

Works with `sameJsonAsApproved` and `sameContentAsApproved`.

## By Value (Global)

Any occurrence of the runtime value in the serialised output is replaced with the alias:

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

## By Field and Value (Scoped)

Replace the value only when it appears in a specific field:

```java
// Only replaces "runtime-uuid-1234" when it appears in the "parentString" field
assertThat(actual, sameJsonAsApproved()
    .withAlias("parentString", "runtime-uuid-1234", "<parentId>"));
```

## Batch Aliases with `AliasMap`

```java
import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;

AliasMap aliases = AliasMap.builder()
    .add("runtime-uuid-1234", "<userId>")
    .add("secret-token-abc",  "<token>")
    .build();

assertThat(actual, sameJsonAsApproved()
    .withAliasMap(aliases));
```

## Related

- [dynamic-values](dynamic-values.md) — choosing between ignore, alias, and custom matching
