# supported-types

How approvalcrest serialises common Java types to JSON.

## Primitives and Strings

Serialised as their natural JSON values (`42`, `true`, `"hello"`).

## Date and Time — Legacy

| Type | Example serialised form |
|---|---|
| `java.util.Date` | `"1970-01-01T00:00:13.000Z"` |
| `java.sql.Date` | `"1970-01-01T00:00:13.000Z"` |
| `java.sql.Timestamp` | `"1970-01-01T00:00:13.000Z"` |

All serialised as ISO-8601 UTC strings.

## Date and Time — `java.time.*`

| Type | Example serialised form |
|---|---|
| `Instant` | `"1970-01-01T00:00:00.042Z"` |
| `LocalDate` | `"2019-04-01"` |
| `LocalDateTime` | `"2020-04-21T18:38:15.000000013"` |
| `LocalTime` | `"18:45:13.000000125"` |
| `OffsetDateTime` | `"2020-04-21T20:54:15.000000013Z"` |
| `OffsetTime` | `"21:58:17.000000163Z"` |
| `ZonedDateTime` | `"2020-04-01T22:01:02.000000003Z[UTC]"` |

`ZonedDateTime` and `OffsetDateTime` are normalised to UTC for comparison — two values representing the same instant in different timezones will compare equal; two values at different instants will fail.

## Optional

| Value | Serialised form |
|---|---|
| `java.util.Optional.empty()` | `{}` |
| `java.util.Optional.of(13L)` | `{"value": 13}` |
| `com.google.common.base.Optional.absent()` | `{}` |
| `com.google.common.base.Optional.of(13L)` | `{"reference": 13}` |

## `java.nio.file.Path`

Serialised as a string:

```json
"/something/anything"
```

## `Class<?>`

Serialised as the fully qualified class name:

```json
"com.example.MyClass"
```

## `Throwable`

Serialised with the following fields:

```json
{
  "detailMessage": "something went wrong",
  "cause": { ... },
  "suppressedExceptions": [],
  "class": "java.lang.RuntimeException"
}
```

## Circular References

Circular object graphs are handled automatically. Each object instance is serialised once; all subsequent references to the same instance are replaced with a `0xN` pointer:

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

No configuration is needed — cycle detection is automatic.

## Custom Types

For types approvalcrest does not natively handle, register a custom Gson adapter using `withGsonConfiguration`. This also lets you change how any existing type is serialised — for example, serialising a class as a single string instead of a JSON object:

```java
import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import java.util.UUID;

GsonConfiguration gsonConfig = new GsonConfiguration();
gsonConfig.addTypeAdapter(UUID.class,
    (JsonSerializer<UUID>) (src, type, ctx) -> new JsonPrimitive(src.toString()));

assertThat(actual, sameJsonAsApproved()
    .withGsonConfiguration(gsonConfig));
```

Three registration methods are available on `GsonConfiguration`:

| Method | When to use |
|---|---|
| `addTypeAdapter(Type, Object)` | Exact type — `UUID.class`, `MyValueType.class` |
| `addTypeHierarchyAdapter(Class<?>, Object)` | Type and all subclasses — useful for interfaces or abstract base types |
| `addTypeAdapterFactory(TypeAdapterFactory)` | When you need to inspect the type at resolution time |

### Custom types and aliasing

Aliasing applies only to JSON primitives (strings and numbers). When a custom adapter serialises a type as a JSON **string**, that field's value becomes available for aliasing:

```java
// UUID serialised as a string → value can be aliased
GsonConfiguration gsonConfig = new GsonConfiguration();
gsonConfig.addTypeAdapter(UUID.class,
    (JsonSerializer<UUID>) (src, type, ctx) -> new JsonPrimitive(src.toString()));

assertThat(actual, sameJsonAsApproved()
    .withGsonConfiguration(gsonConfig)
    .withAlias("orderId", "550e8400-e29b-41d4-a716-446655440000", "<orderId>"));
```

If the type is left as a Gson JSON object (the default for class instances), aliasing does not apply to it.

## Related

- [same-bean-as](same-bean-as.md)
- [same-json-as-approved](same-json-as-approved.md)
