# custom-matching

Replace field-level comparison with a custom Hamcrest matcher.

## Basic Usage

Use `.with(String path, Matcher<T> matcher)` to assert a structural property of a field instead of an exact value. All other fields are still compared normally.

```java
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.startsWith;

assertThat(actual, sameBeanAs(expected)
    .with("address.streetName", startsWith("Via")));
```

## Chaining with `.ignoring()`

```java
import static org.hamcrest.Matchers.notNullValue;

assertThat(actual, sameBeanAs(expected)
    .ignoring("transientId")
    .with("createdAt", notNullValue()));
```

## Common Patterns

```java
// Field must not be null
assertThat(actual, sameJsonAsApproved()
    .with("id", notNullValue()));

// Field matches a regex pattern
assertThat(actual, sameJsonAsApproved()
    .with("email", matchesPattern(".*@example\\.com")));

// Field is within a numeric range
assertThat(actual, sameJsonAsApproved()
    .with("score", both(greaterThan(0)).and(lessThan(100))));
```

## Works With

- `sameBeanAs` — compare a specific field against a matcher while diffing the rest against `expected`
- `sameJsonAsApproved` — override a field's comparison in an approval test

## Kotlin

Kotlin's type inference mishandles F-bounded generics ([KT-5464](https://youtrack.jetbrains.com/issue/KT-5464)). The Kotlin module provides extension functions that make chaining compile correctly. See [kotlin](kotlin.md).

## Related

- [dynamic-values](dynamic-values.md) — choosing between ignore, alias, and custom matching
- [ignoring-fields](ignoring-fields.md)
- [kotlin](kotlin.md)
