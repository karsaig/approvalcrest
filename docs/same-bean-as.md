# same-bean-as

Compare two Java beans by serialising both to JSON and diffing the result.

## Basic Usage

```java
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;

@Test
void orderMatchesExpected() {
    Order actual   = buildOrder();
    Order expected = expectedOrder();

    assertThat(actual, sameBeanAs(expected));
}
```

## Why Use It

Traditional Hamcrest requires a matcher for every field you care about:

```java
// Before — tedious and easy to forget a field
assertThat(actual.getId(),    equalTo(expected.getId()));
assertThat(actual.getName(),  equalTo(expected.getName()));
assertThat(actual.getPrice(), equalTo(expected.getPrice()));
// ... and so on for every field
```

With `sameBeanAs`, the entire object graph is compared in one call:

```java
// After — complete comparison with no boilerplate
assertThat(actual, sameBeanAs(expected));
```

If any field differs, the error message shows the JSON diff with the exact path and both values.

## Error Messages

When a field differs, you get a structured diff:

```
Expected:
{
  "name": "Widget",
  "price": 9.99
}
     but:
{
  "name": "Widget",
  "price": 12.99     ← actual value
}
```

Use `MatcherAssert.assertThat` from the approvalcrest package (not Hamcrest's) to get a `ComparisonFailure` with IDE side-by-side diff support.

## Skipping Class Comparison

By default, `sameBeanAs` requires both objects to be the same type. Use `.skipClassComparison()` when comparing objects of different types that have the same serialised structure:

```java
assertThat(actualDto, sameBeanAs(expectedEntity).skipClassComparison());
```

To suppress the type check globally, set `-DbeanMatcherSkipClassComparison=true`.

## Circular References

Circular object graphs are handled automatically. Each object is serialised once; subsequent references to the same instance are replaced with a `0xN` pointer:

```json
{
  "parent": {
    "0x1": {
      "children": [
        { "parent": "0x1", "childAttribute": "child1" }
      ],
      "parentAttribute": "parent"
    }
  }
}
```

No configuration is needed — the library detects cycles automatically.

## Related

- [ignoring-fields](ignoring-fields.md) — exclude fields from comparison
- [custom-matching](custom-matching.md) — assert constraints on individual fields
- [sorting](sorting.md) — stabilise collection order; `sortType` for element-type-based sorting
- [supported-types](supported-types.md) — how common Java types are serialised
