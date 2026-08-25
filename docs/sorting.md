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

The two positions inside a pair are never swapped, so the file records which half was the key. Everything
else is still sorted: entries are ordered by their content, and a collection-valued half is sorted like any
other collection. Before 1.5.1 the pair was ordered by content too, so `{a: z}` and `{z: a}` wrote the same
bytes.

This holds for a map held in a field — including a field *on* another map's value — and for a map at the
root. It does not hold for a map that *is* another map's value, or one reached as a collection element or
through an `Object`-declared field. Nothing sorts those pairs unless a `sortField` reaches that level, so most of the
time they come out key first anyway — but add one that does reach them and the key and value can swap.

For a **file matcher** this also requires strict file matching, which is on by default. With
`-DfileMatcherStrictFileMatching=false` pairs are ordered by content on both sides, and a map then compares
equal to the same map with its keys and values swapped. Because the written form depends on that setting,
switching it either way means regenerating any approved file holding such a map. Note also that an
unrecognised value for the property reads as `false`, so a typo such as `-DfMStrictMatching=ture` turns
strict matching off. `sameBeanAs` serialises both sides itself, so the key stays first there whatever the
setting.

A raw JSON string input keeps its pairs exactly as written, unless a `sortField` names the field holding
them, in which case they are ordered by content.

## A null map key

A `null` key is recorded as the member name `"null"`:

```json
"m": [ { "null": "someValue" } ]
```

A path addresses the entry by that name, like any other key. Where the map holds a key that is not a
primitive, `String` or enum, the whole map is written as `[key, value]` pairs instead, and a null key appears
there as a bare JSON null.

A `null` key and a `String` key of `"null"` write the same member name. Both entries are recorded, but a
comparison cannot tell them apart, so a map keyed `null` compares equal to a map keyed `"null"`. If that
distinction matters, assert on it directly rather than relying on the file.

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
