# dynamic-values

How to handle values that change between test runs.

## The Problem

UUIDs, auto-incremented IDs, timestamps, random values, and other generated data break approved files because they differ on every run.

## Three Tools — Choose by Use Case

### 1. Ignore

When the value is irrelevant to correctness, exclude it from comparison entirely:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoring("createdAt")
    .ignoring("id"));
```

The field does not appear in the approved file at all.

### 2. Alias

When you want the approved file to document the field but not pin the exact value:

```java
assertThat(actual, sameJsonAsApproved()
    .withAlias("2024-05-01T12:00:00Z", "<createdAt>"));
```

The approved file contains `"createdAt": "<createdAt>"` — clearly readable and stable across runs.

### 3. Custom Match

When you want to assert a structural property (non-null, matches a pattern, within a range):

```java
assertThat(actual, sameJsonAsApproved()
    .with("id",    notNullValue())
    .with("email", matchesPattern(".*@example\\.com")));
```

## Decision Guide

| Situation | Tool |
|---|---|
| Value is totally irrelevant | [`.ignoring()`](ignoring-fields.md) |
| Value is relevant; want a readable approved file | [`.withAlias()`](aliasing.md) |
| Value must satisfy a constraint | [`.with(path, matcher)`](custom-matching.md) |

## Related

- [ignoring-fields](ignoring-fields.md)
- [aliasing](aliasing.md)
- [custom-matching](custom-matching.md)
