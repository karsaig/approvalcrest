<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=4e9babdf-6a30-4e7b-a09d-f12bf35f3a1f" />

# custom-matching

Assert a field with a custom Hamcrest matcher, either instead of comparing it or as well as comparing it.

## Basic Usage

Use `.with(String path, Matcher<T> matcher)` to assert a structural property of a field instead of an exact value. All other fields are still compared normally — either against the expected object (for `sameBeanAs`) or against the approved file (for `sameJsonAsApproved`).

This is useful for fields whose exact value you cannot pin down (e.g. a generated ID) but whose shape you want to assert:

```java
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static org.hamcrest.Matchers.notNullValue;

// The id field must be present but its exact value is not pinned in the approved file
assertThat(actual, sameJsonAsApproved()
    .with("id", notNullValue()));
```

With `sameBeanAs`, the custom matcher replaces the field comparison for that path while everything else is diffed against `expected`:

```java
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.startsWith;

assertThat(actual, sameBeanAs(expected)
    .with("address.streetName", startsWith("Via")));
```

## Asserting *in addition to* the comparison

`.with(...)` removes the field, which is what you want for a value you cannot pin down. When you *can* pin the
value down but want a stronger guarantee about it, use `.alsoCheck(String path, Matcher<T> matcher)` instead. The
field stays in the comparison and the matcher runs on top:

```java
import static org.hamcrest.Matchers.greaterThan;

// score must still equal the value in the approved file, AND be positive
assertThat(actual, sameJsonAsApproved()
    .alsoCheck("score", greaterThan(0L)));
```

The difference is what reaches the approved file. Under `.with("score", ...)` the field is never written, so a
score that later drifts from 5 to 5000 cannot fail. Under `.alsoCheck("score", ...)` it is written and compared as
usual.

It works the same way for `sameBeanAs`, where "the approved file" is instead the expected object: `.with` removes
the field from both sides before they are diffed, `.alsoCheck` leaves it in.

```java
// address.streetName must equal expected's value AND start with "Via"
assertThat(actual, sameBeanAs(expected)
    .alsoCheck("address.streetName", startsWith("Via")));
```

| | in the approved file | comparison | custom matcher |
|---|---|---|---|
| `.with(path, m)` | removed | skipped for that field | runs |
| `.alsoCheck(path, m)` | kept | runs | runs |

`.alsoCheckMatching(namePattern, matcher)` is the same choice for the name-pattern form of
[`.withMatcher(...)`](#match-all-fields-whose-name-matches-a-pattern).

Everything else on this page — path syntax, fan-out through collections, the `*` wildcard, container matchers,
and how a path behaves when it hits a null — applies identically to both modes. They register into the same place
and are evaluated by the same code; only the removal differs.

Registering the same path both ways is allowed and **the last call wins**. An explicit `.ignoring(path)` still
removes the field whichever order the two are written in.

**Two limitations worth knowing.**

- **The pattern form cannot be undone.** Patterns accumulate and two matcher instances never compare equal, so
  `.withMatcher(is("tags"), m1).alsoCheckMatching(is("tags"), m2)` still ignores `tags`. Only the path form has
  last-call-wins.
- **A field that fails both is reported once.** Custom matchers are evaluated before the content comparison and
  the first failure stops there, so a field that both fails its matcher and differs from the approved file reports
  the matcher mismatch only.

**Switching an existing `.with(...)` call to `.alsoCheck(...)` needs the approved file regenerating.** That file
was written while the field was being removed, so it has never contained it. Re-run with
`-DfileMatcherUpdateInPlace=true`, which works as long as the new matcher passes — it is applied before the
rewrite.

## Chaining with `.ignoring()`

```java
import static org.hamcrest.Matchers.notNullValue;

assertThat(actual, sameBeanAs(expected)
    .ignoring("transientId")
    .with("createdAt", notNullValue()));
```

## Common Patterns

```java
// Field must not be null
assertThat(actual, sameJsonAsApproved()
    .with("id", notNullValue()));

// Field matches a regex pattern
assertThat(actual, sameJsonAsApproved()
    .with("email", matchesPattern(".*@example\\.com")));

// Field is within a numeric range
assertThat(actual, sameJsonAsApproved()
    .with("score", both(greaterThan(0)).and(lessThan(100))));

// Field has a minimum length
assertThat(actual, sameJsonAsApproved()
    .with("description", hasLength(greaterThan(0))));

// Nested field must not be null
assertThat(actual, sameJsonAsApproved()
    .with("order.trackingCode", notNullValue()));

// Path through a collection (fan-out): asserts trackingCode is non-null in EVERY order
assertThat(actual, sameJsonAsApproved()
    .with("orders.trackingCode", notNullValue()));

// Assert the collection itself (not each element) — orders must not be empty
assertThat(actual, sameJsonAsApproved()
    .with("orders", not(empty())));
```

## Paths Through Collections (Fan-out)

When any path segment resolves to a collection or array, `.with()` **fans out** — the matcher is applied to the resolved value **in every element** of that collection.

```java
// 'orders' is a List<Order>; each Order has a 'trackingCode' field.
// The matcher must pass on trackingCode in EVERY order.
assertThat(actual, sameJsonAsApproved()
    .with("orders.trackingCode", notNullValue()));
```

These two rules apply to the `*` segment below in exactly the same way.

**Matching is strict:** the matcher must pass on **every** value the path resolved to. If one fails, the
whole assertion fails, and the message identifies the first failing element.

**Resolution is lenient:** elements that do not have the field are skipped rather than failing. So on a
list where only some orders carry `trackingCode`, the matcher is applied to the ones that do and the
assertion can pass. Only a path that resolves against **no** element at all is an error. Skipping is what
makes a map's array-of-entries form reachable, but it also means a heterogeneous collection narrows what
you asserted without saying so — assert the container's size alongside it when that matters.

**Empty collection fails:** if the fanned-out collection is empty, the assertion fails. This prevents silent vacuous-truth passes when a list is unexpectedly empty.

Fan-out is recursive — it applies at each level if multiple path segments are collections:

```java
// orders → List<Order>, each Order has items → List<Item>
// Every item in every order must have a non-null sku
assertThat(actual, sameJsonAsApproved()
    .with("orders.items.sku", notNullValue()));
```

Fan-out applies to `Collection` fields and to Java arrays. A `Map` works differently: it is not fanned
out over, it is **traversed by key**. The key is a path segment, so name the entry you mean:

```java
// The trackingCode of the order stored under key "A-1"
assertThat(actual, sameJsonAsApproved()
    .with("ordersByRef.A-1.trackingCode", notNullValue()));
```

Skipping the key does not reach the values — there is no entry called `trackingCode`, so the path is
rejected:

```java
// throws IllegalArgumentException: "ordersByRef.trackingCode does not exist"
assertThat(actual, sameJsonAsApproved()
    .with("ordersByRef.trackingCode", notNullValue()));
```

To assert something about the map as a whole rather than one entry, target the map itself with a map
matcher — see below.

### Every value: the `*` segment

Naming a key is fine when you know it. When you want the same assertion for every entry, use `*`:

```java
// The trackingCode of EVERY order in the map must be non-null
assertThat(actual, sameJsonAsApproved()
    .with("ordersByRef.*.trackingCode", notNullValue()));
```

`*` stands for **every named child at that position** — a map key, an object property, or a bean field.
It works the same way in `.ignoring(path)`, so the two agree:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoring("ordersByRef.*.generatedId"));
```

Things to know:

- **`*` is only a wildcard in a non-final segment.** As the last segment it keeps its ordinary meaning —
  a key literally named `*`, which a JSON document or a `Map<String,?>` may genuinely have. So
  `.ignoring("headers.*")` removes that key, and `.ignoring("ordersByRef.*")` looks for one rather than
  meaning "every value". To assert something about the map as a whole, target the map field itself.
- **`*` is not needed for collections or arrays.** They are already traversed, so
  `orders.trackingCode` already means "in every order". Writing `orders.*.trackingCode` is a *different*
  query — "any named field of each order, then trackingCode" — not a synonym.
- **A `*` that matches nothing is an error** for `.with(...)`, so a mistyped path cannot pass by matching
  nothing. For `.ignoring(...)` it is a no-op, as any non-matching ignore path is.
- **Strict matching, lenient resolution** — the same two rules as the collection fan-out above. Every
  child the path resolves against must pass; children that do not carry the rest of the path are
  skipped.
- **A scalar child is skipped, not an error.** A bean has scalar fields as well as object ones, so
  `*.trackingCode` passes over the scalars and applies to the objects.

A `*` resolves against the object when there is one, so the values keep their real types. Naming a key
instead falls through to the serialised JSON, where a whole number arrives as a `Long`. So
`.with("ordersByRef.*.quantity", equalTo(1))` and `equalTo(1L)` both match, while
`.with("ordersByRef.A-1.quantity", equalTo(1))` needs the `1L` form.

The same applies to ordering matchers — `greaterThan`, `lessThan`, `greaterThanOrEqualTo` and their
counterparts — with one extra trap. A field path is resolved twice: against the object first, then against the
serialised JSON if that did not settle it. An `int` field is therefore an `Integer` on the first attempt and a
`Long` on the second, so `greaterThan(0L)` is answered by the retry and works. A `long` field is a `Long` on
both, so `greaterThan(0)` matches nothing however large the value — and a field reached by
`.withMatcher(...)` / `.alsoCheckMatching(...)` is read from the serialised tree only, so a whole number there
is always a `Long`.

**Write the matcher's number in the same form as the value's** — `greaterThan(0L)` for a `long` field, and for
any name-pattern matcher. A `double` field needs `greaterThan(1.0)` rather than `greaterThan(1)` for the same
reason. Getting it wrong is a mismatch, not an error, and the failure message names the value, the type it
arrived as and the cast that failed. The same applies to a matcher wrapped in `allOf`, `both().and()`,
`hasItem` or `everyItem`.

## Matching the Container Itself

To assert a property of the container rather than of each element, point the path **at the container field** and use a container matcher. The path stops there, so no fan-out happens and the matcher receives the collection, map or array itself.

### Size

```java
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;

// The orders list must contain exactly one element
assertThat(actual, sameBeanAs(expected)
    .with("orders", hasSize(1)));

// At least one element, without pinning the exact count
assertThat(actual, sameBeanAs(expected)
    .with("orders", hasSize(greaterThan(0))));
```

### Content and order

```java
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;

// Exactly these elements, in exactly this order
assertThat(actual, sameBeanAs(expected)
    .with("orders", contains(orderWithRef("A-1"), orderWithRef("A-2"))));

// Exactly these elements, order irrelevant — the right choice for a Set
assertThat(actual, sameBeanAs(expected)
    .with("orders", containsInAnyOrder(orderWithRef("A-2"), orderWithRef("A-1"))));

// At least one matching element; others may be present
assertThat(actual, sameBeanAs(expected)
    .with("orders", hasItem(orderWithRef("A-1"))));
```

### Maps

```java
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;

assertThat(actual, sameBeanAs(expected)
    .with("ordersByRef", aMapWithSize(1)));

assertThat(actual, sameBeanAs(expected)
    .with("ordersByRef", hasEntry(equalTo("A-1"), orderWithRef("A-1"))));

assertThat(actual, sameBeanAs(expected)
    .with("ordersByRef", hasKey("A-1")));
```

### Arrays

A Java array is not a `Collection`, so `hasSize` and `contains` do not apply to an array field. Use the array-specific matchers:

```java
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.arrayContaining;

assertThat(actual, sameBeanAs(expected)
    .with("orderArray", arrayWithSize(2)));

assertThat(actual, sameBeanAs(expected)
    .with("orderArray", arrayContaining(orderWithRef("A-1"), orderWithRef("A-2"))));
```

Fan-out through an array is unaffected — `.with("orderArray.trackingCode", notNullValue())` works exactly as it does for a `List`.

## Container Matchers and JSON String Input

`sameJsonAsApproved` accepts either an object or a raw JSON string. A container matcher is resolved against the Java object; only if the path cannot be resolved there is it retried against the serialised JSON, where a collection is a Gson `JsonArray`. A container matcher that does resolve against the object is settled by it and never retried, so negating one works. On JSON-string input the path never resolves against the object, and one combination there cannot fail; see the negation note below. A `JsonArray` is an `Iterable` but not a `Collection`, so it is presented to the matcher as a read-only `List` view over the live array. Nothing is copied, and a scalar element is coerced on read — a JSON number arrives as a `Long` or `Double`, a JSON string as a `String`.

Size, emptiness and scalar contents therefore hold for both input forms. What a JSON string cannot supply is element *classes*:

| Matcher | Object input | JSON string input |
|---|---|---|
| `hasSize`, `empty`, `iterableWithSize`, `emptyIterable` | works | works |
| `hasItem`, `contains`, `containsInAnyOrder` over **scalars** | works | works |
| `hasItem`, `contains`, `containsInAnyOrder` over **objects** | works | container is matched, but an element matcher written against the field's own class cannot match a `JsonObject` — and see the negation note below, where this becomes a silent pass |
| `hasEntry`, `hasKey`, `aMapWithSize` | works | fails — a map serialises to a `JsonArray` of single-entry objects, not a `java.util.Map` |

```java
// Both hold whether the input is an object or a JSON string
assertThat(actual, sameJsonAsApproved()
    .with("orders", hasSize(2)));

assertThat(actual, sameJsonAsApproved()
    .with("tags", contains("urgent", "review")));
```

A size mismatch reports the size, for either input form:

```
orders collection size was <2>
```

Negation works for a container matcher on either input form, because the container matcher is settled by the value at the path:

```java
// Fails when orders is empty, for object and JSON-string input alike
assertThat(actual, sameJsonAsApproved()
    .with("orders", not(empty())));

// Fails when an order with this reference is present
assertThat(actual, sameBeanAs(expected)
    .with("orders", not(hasItem(orderWithRef("A-9")))));
```

**One combination cannot fail.** `not(hasItem(...))`, `not(contains(...))` and `not(containsInAnyOrder(...))` pass whatever the data holds when *all three* of these are true: the input is a raw JSON string, the elements are objects rather than scalars, and the element matcher is written against the element's own class.

```java
// Object input: fails when an order with this reference is present. Correct.
assertThat(actual, sameBeanAs(expected)
    .with("orders", not(hasItem(orderWithRef("A-9")))));

// JSON string input: passes even when A-9 IS present. The matcher is handed a
// JsonObject, which no Order matcher can match, so hasItem is false and not(...) is true.
assertThat(jsonString, sameJsonAsApproved()
    .with("orders", not(hasItem(orderWithRef("A-9")))));
```

Un-negated, the same mismatch is reported — `mismatches were: [was JsonObject <{...}>]` — which is the limitation already described in the table above. Negation turns that non-match into a pass.

The two ways round it: compare the object rather than a JSON string, or write the matcher against the JSON shape. Over **scalar** elements all three work on both input forms, because the list view coerces a scalar on read, so `not(hasItem("urgent"))` over a `tags` array fails correctly whichever form the input takes.

## A null half-way along a path

A path resolves against the Java object first and is retried against the serialised JSON wherever the object walk cannot reach — an array segment, a map entry addressed by its key, a member whose serialised name differs from the field's, and everything at all when the input is a raw JSON string.

When a reference along the path is `null`, that retry answers `null` for **everything** below it, because a null in parsed JSON carries no type. So a path whose remaining segments name nothing still resolves, and `nullValue()` accepts it:

```java
// customer is null. This passes whether or not Customer has an "nmae" member.
assertThat(actual, sameBeanAs(expected)
    .with("customer.nmae", nullValue()));
```

A path left behind by a rename therefore asserts nothing, so long as some reference along it is null. Read a passing `nullValue()` assertion over a nested path as evidence about the reference, not about the leaf.

The way round it is to assert on the reference itself, so the assertion says what you mean:

```java
assertThat(actual, sameBeanAs(expected)
    .with("customer", nullValue()));
```

## Match All Fields Whose Name Matches a Pattern

Use `.withMatcher(Matcher<String> fieldNamePattern, Matcher<V> matcher)` to apply a custom matcher to **every field at any depth** whose name matches the pattern. This is useful when many fields share a naming convention:

```java
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.matchesPattern;

// Every field whose name ends with "Id" must be non-null
assertThat(actual, sameJsonAsApproved()
    .withMatcher(endsWith("Id"), notNullValue()));

// Every field whose name starts with "url" must be a valid HTTPS URL
assertThat(actual, sameJsonAsApproved()
    .withMatcher(startsWith("url"), matchesPattern("https://.*")));
```

Chain multiple patterns freely:

```java
assertThat(actual, sameJsonAsApproved()
    .withMatcher(endsWith("Id"),    notNullValue())
    .withMatcher(startsWith("url"), matchesPattern("https://.*")));
```

Unlike `.with(path, matcher)` — which targets a single named field — `.withMatcher` scans the entire object graph and applies to every matching field name wherever it appears. Pattern-based matchers are applied before sorting.

Values are presented to the matcher exactly as they are for `.with(path, matcher)`, so a
collection-valued field takes a container matcher here too and the table above applies unchanged:

```java
// Every field named "tags" must hold two elements
assertThat(actual, sameJsonAsApproved()
    .withMatcher(is("tags"), hasSize(2)));
```

A pattern is matched against the field's own name whatever its type, `Set`- and `Map`-typed fields included.
One consequence of "every matching field": a pattern matching **no** field passes, since there is nothing
to check — so a pattern that names a field wrongly reads as a green assertion rather than an error.

`.withMatcher(...)` removes every field it matches, the same way `.with(...)` does. To keep those fields in the
comparison and assert them as well, use `.alsoCheckMatching(fieldNamePattern, matcher)` — see
[Asserting *in addition to* the comparison](#asserting-in-addition-to-the-comparison).

**The pattern form has no last-call-wins.** Patterns accumulate in a list and two matcher instances never compare
equal, so `.withMatcher(is("tags"), m1).alsoCheckMatching(is("tags"), m2)` still removes `tags`. Only the path
form can be re-registered the other way.

## Works With

- `sameBeanAs` — assert a specific field with a matcher, either instead of or as well as diffing it against `expected`
- `sameJsonAsApproved` — the same, against the approved file

Both modes work with both matchers. `sameContentAsApproved` has no fields, so none of them apply to it.

## Kotlin

Kotlin's type inference mishandles F-bounded generics ([KT-5464](https://youtrack.jetbrains.com/issue/KT-5464)). The Kotlin module provides extension functions that make chaining compile correctly. See [kotlin](kotlin.md).

## Related

- [dynamic-values](dynamic-values.md) — choosing between ignore, alias, and custom matching
- [ignoring-fields](ignoring-fields.md)
- [kotlin](kotlin.md)
