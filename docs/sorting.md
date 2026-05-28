# sorting

Control collection ordering to get stable comparisons regardless of iteration order.

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

Chain multiple paths:

```java
assertThat(actual, sameJsonAsApproved()
    .sortField("orders")
    .sortField("orders.items"));
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

`sameBeanAs`, `sameJsonAsApproved`, and `sameContentAsApproved`.
