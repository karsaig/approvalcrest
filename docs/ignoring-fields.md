# ignoring-fields

Exclude specific fields from comparison using path, Hamcrest matcher, or type.

Works with `sameBeanAs` and `sameJsonAsApproved`. Multiple `.ignoring()` calls can be chained.

## When to Use Ignoring

`.ignoring()` is the right choice when a field is genuinely irrelevant to the test — not as a blanket way to silence noise. Overuse makes the approved file less precise and reduces the safety net.

For volatile fields that still matter, consider [aliasing](aliasing.md) (document the field with a placeholder) or [custom matching](custom-matching.md) (assert a structural property like non-null or matches a pattern). If you can control the source — injecting a clock, seeding an ID generator — see [dynamic-values](dynamic-values.md) for the preferred approach.

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
    .ignoring("updatedAt")
    .ignoring("id"));
```

Deep paths work at any nesting level:

```java
// Ignore a field nested several levels down
assertThat(actual, sameJsonAsApproved()
    .ignoring("order.customer.address.postCode"));
```

## By Hamcrest Matcher on Field Name

Pass a `Matcher<String>` to exclude all fields whose name matches, at any depth in the object graph:

```java
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

// Ignore all fields whose name starts with "street"
assertThat(actual, sameJsonAsApproved()
    .ignoring(startsWith("street")));

// Ignore all timestamp-style fields
assertThat(actual, sameJsonAsApproved()
    .ignoring(endsWith("At")));

// Ignore a specific field by exact name
assertThat(actual, sameJsonAsApproved()
    .ignoring(is("transientId")));
```

This is useful when many fields share a naming convention and you want to exclude the whole group without listing each one individually.

## By Type

Ignore all fields of a given Java type throughout the entire object graph:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoring(Instant.class));
```

Useful for excluding all timestamp fields when they share a type. Multiple types can be chained:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoring(Instant.class)
    .ignoring(UUID.class));

## Strict Mode (default on)

By default, `ignoring()` operates in **strict mode**: ignored fields are stripped from the **actual** side only. If the approved file was written before strict mode was enabled — and still contains the value of an ignored field — the test will fail.

**Migration from pre-1.0.1 approved files:**

Re-run your tests with `-DfileMatcherUpdateInPlace=true` to regenerate the approved files without the ignored fields:

```bash
mvn test -DfileMatcherUpdateInPlace=true
```

**Disabling strict mode globally** (restores the old two-sided behaviour where ignored fields are stripped from both actual and approved before comparison):

```bash
mvn test -DfileMatcherStrictFileMatching=false
```

**Disabling strict mode per test** (via `FileMatcherConfig` constructor — last parameter):

```java
// new FileMatcherConfig(overwriteInPlace, passOnCreate, buildIndex, approvedDir, sortInputFile, strictMatching)
FileMatcherConfig config = new FileMatcherConfig(false, false, false, false, false, false);
```

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
