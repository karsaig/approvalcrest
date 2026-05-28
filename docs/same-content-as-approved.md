# same-content-as-approved

Assert that arbitrary text content matches a stored approved file.

`sameContentAsApproved` follows the same approval workflow as [`sameJsonAsApproved`](same-json-as-approved.md) but:
- Uses a `.content` file extension instead of `.json`
- Accepts any `String` value — no JSON parsing is performed

Useful for verifying rendered templates, API responses, log output, generated reports, or any text where the exact content matters.

## Basic Usage

```java
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameContentAsApproved;

@Test
void assertContent() {
    String actual = renderTemplate();
    assertThat(actual, sameContentAsApproved());
}
```

## Approval Workflow

1. **First run** — creates `<classHash>/<methodHash>-not-approved.content` next to the test source.
2. **Review** the generated file.
3. **Rename** to `*-approved.content`.
4. **Subsequent runs** compare against the approved file.

## Parameterized Tests (JUnit 5)

Pass `TestInfo` and combine with `.withUniqueId()` to create a separate approved file per case:

```java
@ParameterizedTest
@MethodSource("cases")
void parameterized(String input, TestInfo testInfo) {
    assertThat(input, sameContentAsApproved(testInfo).withUniqueId(input));
}
```

## Unicode Support

Full Unicode is supported, including non-Latin scripts. The library stores and compares content byte-for-byte in the approved file.

## Related

- [file-control](file-control.md) — custom file names, paths, and in-place update
- [same-json-as-approved](same-json-as-approved.md) — JSON-specific approval matching
