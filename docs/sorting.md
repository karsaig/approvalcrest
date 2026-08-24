<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=c4c692b7-64c6-4a83-be80-f1fbb22b9e65" />

# sorting

Control collection ordering to get stable comparisons regardless of iteration order.

## Why Sorting Matters

Collections that don't have a guaranteed iteration order — `HashSet`, `HashMap` values, results from a database query without an `ORDER BY` — can appear in a different order on every test run. Without sorting, a test that passes today may fail tomorrow because the JVM happened to hash elements differently. Sorting makes the approved file stable and the comparison deterministic.

## Automatic Set Ordering

`HashSet` and other unordered sets are **automatically sorted** before comparison — no configuration needed. Elements with the same serialised form are treated as equal.

## Explicit Sort by Field Path

For `List` or `Map` values where insertion order is not guaranteed, sort by field name:

```java
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

// Sort the "tags" collection
assertThat(actual, sameJsonAsApproved()
    .sortField("tags"));
```

Nested paths are supported — sort at multiple levels independently:

```java
// Sort the orders list, then sort the items within each order.
// 'orders' is a List; sortField("orders.items") fans out into every order element
// and sorts its items sub-collection.
assertThat(actual, sameJsonAsApproved()
    .sortField("orders")
    .sortField("orders.items"));
```

**Collection fan-out:** when a path segment resolves to a collection, the sort is applied to the matching sub-field inside **every element** of that collection. You write the path the same way whether intermediate segments are single objects or collections.

Sort the root-level list itself (use an empty string for the root):

```java
assertThat(actual, sameJsonAsApproved()
    .sortField(""));
```

## Sort by Hamcrest Matcher on Field Name

Pass `Matcher<String>` arguments to match multiple field names at once:

```java
import static org.hamcrest.Matchers.is;

assertThat(actual, sameJsonAsApproved()
    .sortField(is("orders"), is("items")));
```

## Excluding Fields from the Sort Key (`SortField`) — Advanced

When sorting a collection of objects, the sort key for each element is its **full serialised JSON**. If elements contain volatile or irrelevant fields (IDs, timestamps, …), those fields make the sort key unstable. `SortField` lets you strip specified fields from the sort key so only the stable fields determine the order:

```
sort key = (full element JSON) − (excluded fields)
```

### Exclude one field from the sort key

```java
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

// Elements in "items" are sorted by all their fields except "age"
assertThat(actual, sameJsonAsApproved()
    .sortFieldPath(SortField.of("items", "age")));
```

### Exclude multiple fields

Pass all of them as varargs, or chain `.ignoring()` calls — the result is identical:

```java
// Exclude both "id" and "createdAt" from the sort key
assertThat(actual, sameJsonAsApproved()
    .sortFieldPath(SortField.of("items", "id", "createdAt")));

// Equivalent with chained calls
assertThat(actual, sameJsonAsApproved()
    .sortFieldPath(SortField.of("items").ignoring("id").ignoring("createdAt")));
```

### Exclude fields matching a pattern

Use `.ignoring(Matcher<String>)` to strip every field whose **name** matches a Hamcrest matcher. Useful when volatile fields share a naming convention:

```java
import static org.hamcrest.Matchers.containsString;

// Strip all fields whose name contains "Date" (e.g. "createdDate", "updatedDate")
assertThat(actual, sameJsonAsApproved()
    .sortFieldPath(SortField.of("items").ignoring(containsString("Date"))));
```

### Exclude nested fields from the sort key

Dot-paths work — you can strip a sub-field of a nested object without removing the whole nested object from the sort key:

```java
// Sort "items" by all fields except the nested "address.city"
assertThat(actual, sameJsonAsApproved()
    .sortFieldPath(SortField.of("items").ignoring("address.city")));
```

### Matcher selector — apply to multiple collections at once

When the collection selector itself is a `Matcher<String>` rather than a literal path, the sort is applied to **every collection whose name matches**, anywhere in the object tree. Use `.sortFieldMatcher()` for these:

```java
import static org.hamcrest.Matchers.is;

// Sort every collection literally named "tags" throughout the whole object graph,
// excluding "weight" from each element's sort key
assertThat(actual, sameJsonAsApproved()
    .sortFieldMatcher(SortField.of(is("tags"), "weight")));
```

This fans out through intermediate collections too — if the object has `groups[].tags[]`, every `tags` inside each group element is sorted automatically.

### Compound sort key

When multiple fields remain in the sort key after exclusion, they all contribute to the element's order. Fields are serialised in **alphabetical order by field name**, so with `id` excluded from `{id, age, name}` the sort key becomes `{age, name}`, sorting primarily by `age` (alphabetically first) and using `name` as a tiebreaker. There is no explicit field-priority API — the alphabetical order of field names determines priority.

## Works With

`sameBeanAs` and `sameJsonAsApproved`.

Both `.sortFieldPath(SortField)` (literal-path selector) and `.sortFieldMatcher(SortField)` (Hamcrest-matcher selector) are available on both matchers.

## Sort by Element Type

Use `sortType(Class<?>...)` to automatically sort any `Collection` or array whose element type matches one of the specified classes. This is equivalent to the automatic sorting that already applies to `Set` fields — no field path is needed:

```java
// Sort all List<Person> and array-of-Person fields automatically
assertThat(actual, sameJsonAsApproved()
    .sortType(Person.class));

// Multiple types at once — covers every collection of Person or Address anywhere in the graph
assertThat(actual, sameBeanAs(expected)
    .sortType(Person.class, Address.class));
```

This is particularly useful when the same element type appears in multiple collections throughout a deep object graph and you want all of them sorted without listing each field path individually.

Works with `sameBeanAs` and `sameJsonAsApproved`.

## Paths Through Maps

A sort path reaches a map entry by its key, and `*` stands for every entry:

```java
// Sorts the tags collection under ONE entry
assertThat(actual, sameJsonAsApproved()
    .sortField("ordersByRef.A-1.tags"));

// Sorts the tags collection under EVERY entry
assertThat(actual, sameJsonAsApproved()
    .sortField("ordersByRef.*.tags"));
```

`*` means the same thing here as in [custom-matching](custom-matching.md) and
[ignoring-fields](ignoring-fields.md) — every named child at that position — so one syntax covers all
three. A wildcard and a literal key at the same level both apply rather than one overriding the other,
so `sortField("ordersByRef.*.tags", "ordersByRef.A-1.notes")` sorts `tags` everywhere and `notes` under
`A-1`.

A `SortField`'s ignored sub-paths take `*` as well, so a field can be kept out of the sort key under
every child rather than under one named one:

```java
// Sort orders by everything except each entry's generated id
assertThat(actual, sameJsonAsApproved()
    .sortFieldPath(SortField.of("orders", "*.generatedId")));
```

As elsewhere, `*` is a wildcard only in a non-final segment. `sortField("ordersByRef.*")` addresses a key
literally named `*`; to sort the map itself, name the map field.

## How a complex-key map is written

A map whose keys are not strings, primitives or enums is written as an array of `[key, value]` pairs, key
first:

```json
"ordersByRef": [
  [ { "ref": "A-1" }, { "total": 12 } ],
  [ { "ref": "A-2" }, { "total": 30 } ]
]
```

The pair keeps that order. Everything else about it is still sorted — the entries are ordered by their
content, and a collection-valued half is sorted like any other collection — but the two positions inside a
pair are not swapped, so the approved file records which half was the key.

Until 1.5.1 the pair was sorted like a collection, which discarded that: `{a: z}` and `{z: a}` wrote the same
bytes and an approved file for one matched the other.

Two consequences worth knowing:

- **Entry order can differ from earlier versions.** A pair's sort key is computed after its halves are
  sorted, so where some pairs used to be reordered and others were not, the entries themselves could land in
  a different order. Regenerate rather than hand-edit.
- **This applies under strict file matching**, which is the default. With `-DfileMatcherStrictFileMatching=false`
  the approved content is filtered and sorted as well, and a tree parsed from a file cannot tell a `[key, value]`
  pair from a nested collection — so with strict off, pairs are still sorted on both sides and a map remains
  indistinguishable from its transpose. Because of that, the written form depends on the setting: switching it
  in either direction needs the affected approved files regenerating.

  Note that an unrecognised value for that property is read as `false`, so a typo such as
  `-DfMStrictMatching=ture` silently turns strict matching off, and this behaviour with it.

Still not covered, because the marker that identifies a map is attached to a *field name*: a map reached
without one — as another map's value, as a collection element, or through an `Object`-declared field — is
unprotected, so its pairs are reordered if anything sorts at that level. Often nothing does, and then they
come out key first by default; add a `sortField` that reaches them and they swap. A map held in a field,
including a field of another map's value, is protected.

A map at the **root** is protected, even though it has no field name: it is recognised from the object's own
type instead. That matters when a sort selector reaches the root — `sortField("")`, or any name matcher that
matches the empty string, such as `sortField(not(equalTo("id")))`.

A **raw JSON string** input carries no marker, so the type-driven sort never fires on it and its pairs are
left exactly as written. Naming the field in a `sortField` still reaches them, and there they are sorted like
any other array.

## A map key that looks like a circular-reference marker

An object holding a circular reference is written wrapped under a generated key — `{"0x1": {…}}` — and that
key is skipped during path navigation, so a path reads the same whether or not the type has cycles. The test
for "is this a wrapper key" is `0x` followed by lowercase hex digits, which a map key of your own can spell.

Nothing in the serialised text separates the two. A `Map<String, ?>` entry is written as a single-key object,
and so is a wrapper. The consequence is that a path which omits such a key still reaches through it, where
for any other key it would match nothing:

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

This is not fixable without changing how wrappers are written, which would invalidate every approved file
containing a circular reference, including hand-written ones. The one discriminator that looked usable — that
a wrapper is never an array element — was measured and is false: a collection of objects that each carry a
cycle writes a wrapper at every array position.
