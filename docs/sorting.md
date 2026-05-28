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

## Sort by Sub-Field (`SortField`) — Advanced

Use `SortField.of(collectionField, sortKey)` to sort a collection of objects by one of their sub-fields. Primarily useful with `sameBeanAs`:

```java
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;

assertThat(actual, sameBeanAs(expected)
    .sortFieldPath(SortField.of("childBeanList", "childInteger")));
```

## Works With

`sameBeanAs` and `sameJsonAsApproved`.

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
