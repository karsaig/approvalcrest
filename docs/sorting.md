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

## A complex-key map and its transpose are indistinguishable

A map whose keys are not strings, primitives or enums is written as an array of `[key, value]` pairs. That
pair is an array, and the sort recurses into arrays, so the pair itself is reordered by its JSON — which
discards which half was the key:

```java
Map<Bean, Bean> forward    = { beanNamed("a") -> beanNamed("z") };
Map<Bean, Bean> transposed  = { beanNamed("z") -> beanNamed("a") };
// both write the same bytes, so an approved file for one is matched by the other
```

The comparison cannot tell them apart, and the approved file cannot record the difference. Order within a
pair follows whichever side's JSON sorts first, which is not always the key: a value carrying a nested map
sorts ahead of a key that does not, because `"map": [` precedes `"map": null`.

If the direction matters to your assertion, do not rely on the file to capture it — assert on the key or the
value explicitly with `.with(...)`, or use a `String` key, which is written as `{"key": value}` and keeps its
position.

Not fixed, because excluding pair arrays from the sort changes the written form of every approved file that
holds such a map.

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
