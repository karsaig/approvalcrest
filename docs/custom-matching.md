<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=4e9babdf-6a30-4e7b-a09d-f12bf35f3a1f" />

# custom-matching

Replace field-level comparison with a custom Hamcrest matcher.

## Basic Usage

Use `.with(String path, Matcher<T> matcher)` to assert a structural property of a field instead of an exact value. All other fields are still compared normally — either against the expected object (for `sameBeanAs`) or against the approved file (for `sameJsonAsApproved`).

This is useful for fields whose exact value you cannot pin down (e.g. a generated ID) but whose shape you want to assert:

```java
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static org.hamcrest.Matchers.notNullValue;

// The id field must be present but its exact value is not pinned in the approved file
assertThat(actual, sameJsonAsApproved()
    .with("id", notNullValue()));
```

With `sameBeanAs`, the custom matcher replaces the field comparison for that path while everything else is diffed against `expected`:

```java
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

// Field has a minimum length
assertThat(actual, sameJsonAsApproved()
    .with("description", hasLength(greaterThan(0))));

// Nested field must not be null
assertThat(actual, sameJsonAsApproved()
    .with("order.trackingCode", notNullValue()));

// Path through a collection (fan-out): asserts trackingCode is non-null in EVERY order
assertThat(actual, sameJsonAsApproved()
    .with("orders.trackingCode", notNullValue()));

// Assert the collection itself (not each element) — orders must not be empty
assertThat(actual, sameJsonAsApproved()
    .with("orders", not(empty())));
```

## Paths Through Collections (Fan-out)

When any path segment resolves to a collection or array, `.with()` **fans out** — the matcher is applied to the resolved value **in every element** of that collection.

```java
// 'orders' is a List<Order>; each Order has a 'trackingCode' field.
// The matcher must pass on trackingCode in EVERY order.
assertThat(actual, sameJsonAsApproved()
    .with("orders.trackingCode", notNullValue()));
```

**Important:** the matcher must pass on **every** element. If any one element fails, the whole assertion fails, and the error message identifies the first failing element.

**Empty collection fails:** if the fanned-out collection is empty, the assertion fails. This prevents silent vacuous-truth passes when a list is unexpectedly empty.

Fan-out is recursive — it applies at each level if multiple path segments are collections:

```java
// orders → List<Order>, each Order has items → List<Item>
// Every item in every order must have a non-null sku
assertThat(actual, sameJsonAsApproved()
    .with("orders.items.sku", notNullValue()));
```

To assert a property of the collection **itself** (e.g. its size or which elements it contains) rather than each element individually, target the collection field directly:

```java
// The orders list as a whole must have at least one element
assertThat(actual, sameJsonAsApproved()
    .with("orders", not(empty())));
```

## Match All Fields Whose Name Matches a Pattern

Use `.withMatcher(Matcher<String> fieldNamePattern, Matcher<V> matcher)` to apply a custom matcher to **every field at any depth** whose name matches the pattern. This is useful when many fields share a naming convention:

```java
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.matchesPattern;

// Every field whose name ends with "Id" must be non-null
assertThat(actual, sameJsonAsApproved()
    .withMatcher(endsWith("Id"), notNullValue()));

// Every field whose name starts with "url" must be a valid HTTPS URL
assertThat(actual, sameJsonAsApproved()
    .withMatcher(startsWith("url"), matchesPattern("https://.*")));
```

Chain multiple patterns freely:

```java
assertThat(actual, sameJsonAsApproved()
    .withMatcher(endsWith("Id"),    notNullValue())
    .withMatcher(startsWith("url"), matchesPattern("https://.*")));
```

Unlike `.with(path, matcher)` — which targets a single named field — `.withMatcher` scans the entire object graph and applies to every matching field name wherever it appears. Pattern-based matchers are applied before sorting.

## Works With

- `sameBeanAs` — compare a specific field against a matcher while diffing the rest against `expected`
- `sameJsonAsApproved` — override a field's comparison in an approval test

## Kotlin

Kotlin's type inference mishandles F-bounded generics ([KT-5464](https://youtrack.jetbrains.com/issue/KT-5464)). The Kotlin module provides extension functions that make chaining compile correctly. See [kotlin](kotlin.md).

## Related

- [dynamic-values](dynamic-values.md) — choosing between ignore, alias, and custom matching
- [ignoring-fields](ignoring-fields.md)
- [kotlin](kotlin.md)
