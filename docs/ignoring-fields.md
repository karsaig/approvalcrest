<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=d3ba28e7-0e56-403b-934a-aa06f4b8c47c" />

# ignoring-fields

Exclude specific fields from comparison using path, Hamcrest matcher, or type — or remove whole array elements by value with `ignoringElementsWhere`.

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

## Paths Through Collections (Fan-out)

When any path segment resolves to a collection or array, traversal **fans out** — the rest of the path is applied to every element of that collection. You write the same path regardless of whether intermediate segments are single objects or collections.

```java
// 'orders' is a List<Order>; each Order has a 'trackingCode' field.
// This removes trackingCode from EVERY element of orders.
assertThat(actual, sameJsonAsApproved()
    .ignoring("orders.trackingCode"));
```

Fan-out applies at every level — if a sub-field is also a collection, it fans out again:

```java
// orders → List<Order>, each Order has items → List<Item>, each Item has a price
// Removes price from every item in every order
assertThat(actual, sameJsonAsApproved()
    .ignoring("orders.items.price"));
```

This works identically whether the type is `List`, `Set`, any `Collection` subtype, or a JSON array. Paths through empty collections do nothing silently.

## Removing Array Elements by Value

`.ignoring()` removes *fields*; it cannot drop an individual array element based on what that element contains. `.ignoringElementsWhere()` fills that gap: it removes the array elements whose nested field has a given value.

The path points at a field **within each element**. The **innermost array on the path** is the one filtered — every element whose leaf field value satisfies the matcher is removed:

```java
import static org.hamcrest.Matchers.equalTo;

// meta.tag is an array of { system, code, ... }; remove the elements
// whose system is the tracking system, keeping the rest of the array.
assertThat(actual, sameJsonAsApproved()
    .ignoringElementsWhere("meta.tag.system", equalTo(FLOW_ID_TAG_SYSTEM)));
```

Any `Matcher` works (`startsWith`, `greaterThan`, `not(...)`, …). A convenience overload takes a plain `String`, matching the field's value coerced to a String:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoringElementsWhere("meta.tag.system", FLOW_ID_TAG_SYSTEM));
```

Like the other ignore styles, the path fans out through intermediate collections, so it works for nested structures such as a FHIR `Bundle` — this removes matching `tag` elements from every entry's resource, not the entries themselves:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoringElementsWhere("entry.resource.meta.tag.system", equalTo(FLOW_ID_TAG_SYSTEM)));
```

Behaviour:

- Matching happens against the **serialized JSON** field names. When the input is a JSON string (or a value that serializes with those names) the path is written exactly as it appears in the JSON. Objects that serialize under different internal names (for example raw library model objects with a custom Gson `TypeAdapter`) must expose the JSON-level names for the path to match.
- If **all** elements match, the array is left empty (`[]`) rather than removed — the approved file is expected to contain the empty array after regeneration.
- An element without the leaf field, or that is not a JSON object, is kept. A missing, empty or non-array path is a silent no-op.
- In strict file matching (the default), elements are removed from the **actual** side only, so the approved file must be regenerated to drop them — identical to how `.ignoring()` behaves.

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
```

## Null Field Serialization

By default, null-valued fields are included in the serialized JSON. This is important for `.ignoring()` to work correctly when a field has a null value inside a collection element — without null serialization, the field is stripped before ignore logic runs and the ignored element is never removed from the collection.

**Disabling null serialization globally** (restores the old behaviour where null fields are omitted):

```bash
mvn test -DapprovalcrestSerializeNulls=false
```

**Disabling null serialization per matcher:**

```java
assertThat(actual, sameJsonAsApproved()
    .withoutSerializingNulls()
    .ignoring("id"));
```

When null serialization is off, `.ignoring("field")` will have no effect if `field` is null — the field is not present in the JSON so there is nothing to remove.

**Migration from pre-1.0.2 approved files:**

Re-run your tests with `-DfileMatcherUpdateInPlace=true` to regenerate approved files that now include null fields:

```bash
mvn test -DfileMatcherUpdateInPlace=true
```

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
