# same-json-as-approved

Assert that a Java object or JSON string matches a stored approved file.

## The Approval Workflow

1. **First run** — no approved file exists. The library creates `<classHash>/<methodHash>-not-approved.json` next to the test source.
2. **Review** the generated file and confirm the output is correct.
3. **Rename** it from `*-not-approved.json` to `*-approved.json`.
4. **Subsequent runs** compare against the approved file and pass (or fail on diff).

Approved files are committed to version control. Review changes to them in pull requests just like application code.

## Basic Usage

```java
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

@Test
void runWithDefaultConfigShouldPassWithMatchingApprovedFile() {
    MyBean actual = buildMyBean();
    assertThat(actual, sameJsonAsApproved());
}
```

## JSON String as Input

The matcher also accepts a pre-serialised JSON string directly:

```java
@Test
void rawJsonStringMatchesApprovedFile() {
    String json = fetchJsonFromApi();
    assertThat(json, sameJsonAsApproved());
}
```

## Parameterized Tests (JUnit 5)

Add `TestInfo` as a test parameter — JUnit 5 injects it automatically. Pass it to `sameJsonAsApproved(testInfo)` so the matcher can resolve the correct test method. Use `.withUniqueId(name)` to create a separate approved file per case:

```java
@ParameterizedTest
@MethodSource("data")
void testParameterized(String name, BeanOne value, TestInfo testInfo) {
    assertThat(value, sameJsonAsApproved(testInfo).withUniqueId(name));
}
```

Without `TestInfo`, **private** test methods cannot be resolved and will throw a `NullPointerException` with a message explaining the fix. Public test methods work without `TestInfo` (the stack trace is used to resolve method metadata).

## Parameterized Tests (JUnit 4)

JUnit 4 requires a `Junit4DesciptionWatcher` rule. See [junit4-vintage](junit4-vintage.md) for a complete example.

## Related

- [file-control](file-control.md) — custom file names, paths, and in-place update
- [ignoring-fields](ignoring-fields.md) — exclude fields from comparison
- [aliasing](aliasing.md) — stable placeholders for volatile values
- [sorting](sorting.md) — stabilise collection order
- [supported-types](supported-types.md) — how common Java types are serialised
- [junit4-vintage](junit4-vintage.md) — JUnit 4 / Vintage usage
- [junit5-jupiter](junit5-jupiter.md) — JUnit 5 Jupiter usage
