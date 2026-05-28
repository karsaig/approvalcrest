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
