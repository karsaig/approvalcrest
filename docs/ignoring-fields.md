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

## Paths Through Maps

A `Map` is **not** fanned out over — it is traversed by key. The key is a path segment:

```java
// Removes generatedId from the order stored under key "A-1" only
assertThat(actual, sameJsonAsApproved()
    .ignoring("ordersByRef.A-1.generatedId"));
```

To apply the same path to every entry, use `*`:

```java
// Removes generatedId from EVERY order in the map
assertThat(actual, sameJsonAsApproved()
    .ignoring("ordersByRef.*.generatedId"));
```

`*` means "every named child at that position" — a map key, an object property, or a bean field. It
behaves the same in `.with(path, matcher)`, so an ignore and a custom matcher written over the same
shape agree.

Two limits worth knowing:

- **`*` is only a wildcard in a non-final segment.** As the last segment it keeps its ordinary meaning —
  a key literally named `*`, which a JSON document or a `Map<String,?>` may have. So
  `.ignoring("headers.*")` removes that key, and `.ignoring("ordersByRef.*")` looks for one rather than
  meaning "every value", which is a silent no-op if there is none.
- **Emptying every value cascades.** If the ignored field is the only one on each value, every value
  empties, so each entry is removed, the array empties, and the map field disappears from the output
  altogether. That is the same cascade a named key triggers, but `*` hits every entry at once, so it is
  the common case rather than the corner.

### Removing elements under every map entry

`ignoringElementsWhere` takes the same `*` segment, which is the only way to reach the array under each
entry of a `Map<String, List<T>>`:

```java
// Removes tags with that system from the list under EVERY entry
assertThat(actual, sameJsonAsApproved()
    .ignoringElementsWhere("ordersByRef.*.tag.system", FLOW_ID_TAG_SYSTEM));
```

The comparison to draw is against the **wildcard-free** form, not against a named key.
`ordersByRef.system` filters the outer array of map entries, testing each entry object for a `system`
field — which matches nothing, so it is a silent no-op. `ordersByRef.*.system` filters the list *inside*
each entry, which is what you want for a map of lists. Naming a key, `ordersByRef.A-1.system`, reaches
that same inner list for that one entry, so the wildcard is the named form applied to every entry.

A `*` that ends the whole path is the leaf field name rather than a wildcard, as everywhere else.

A map with non-primitive keys serialises differently — as pairs rather than as single-entry objects — so
a path through it reaches fields of the **key** objects as well as the values. That is pre-existing
behaviour for a named path; `*` inherits it.

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

## A map key that looks like a circular-reference marker

An object holding a circular reference is written wrapped under a generated key — `{"0x1": {…}}` — and that
key is skipped during path navigation, so a path reads the same whether or not the type has cycles. The test
for "is this a wrapper key" is `0x` followed by lowercase hex digits, which a map key of your own can spell.

A path that omits such a key still reaches through it, where for any other key it would match nothing:

```java
// map has one entry, keyed "0x1"
.ignoring("map.0x1.leaf")   // removes leaf, as it should
.ignoring("map.leaf")       // ALSO removes leaf -- the key is skipped as if it were a wrapper
.ignoring("map.k1.leaf")    // for an ordinary key "k1", omitting it matches nothing
```

Both sides of a comparison are filtered the same way, so this does not produce a spurious failure; it
silently removes more from the comparison than you asked, which weakens the assertion rather than breaking
it. The same applies to `sortField(...)` and `ignoringElementsWhere(...)`, which navigate paths the same way.

Affected keys are exactly those matching `0x` plus lowercase hex: `0x1`, `0xff`, `0xdeadbeef`. A key with an
uppercase digit (`0xFF`), a prefix, or a suffix is unaffected. If you hold such keys and need paths under
them to be exact, rename the key for the comparison or address the entry by a path that does not cross it.

## Related

- [dynamic-values](dynamic-values.md) — choosing between ignore, alias, and custom matching
- [same-bean-as](same-bean-as.md)
- [same-json-as-approved](same-json-as-approved.md)
