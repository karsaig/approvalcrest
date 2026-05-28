# ignoring-fields

Exclude specific fields from comparison using path, Hamcrest matcher, or type.

Works with `sameBeanAs`, `sameJsonAsApproved`, and `sameContentAsApproved`. Multiple `.ignoring()` calls can be chained.

## By Field Path

Use a dot-separated path to exclude a specific nested field:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoring("address.streetName"));
```

For top-level fields, just use the field name:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoring("createdAt")
    .ignoring("id"));
```

## By Hamcrest Matcher on Field Name

Pass a `Matcher<String>` to exclude all fields whose name matches:

```java
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.is;

// Ignore all fields whose name starts with "street"
assertThat(actual, sameJsonAsApproved()
    .ignoring(startsWith("street")));

// Ignore a specific field by exact name
assertThat(actual, sameJsonAsApproved()
    .ignoring(is("transientId")));
```

## By Type

Ignore all fields of a given Java type throughout the entire object graph:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoring(String.class));
```

Useful for excluding all timestamp fields when they share a type like `Instant`.

## Chaining

All three styles chain freely:

```java
assertThat(actual, sameBeanAs(expected)
    .ignoring("id")
    .ignoring(startsWith("internal"))
    .ignoring(Instant.class));
```

## Related

- [dynamic-values](dynamic-values.md) — choosing between ignore, alias, and custom matching
- [same-bean-as](same-bean-as.md)
- [same-json-as-approved](same-json-as-approved.md)
