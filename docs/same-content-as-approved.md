# same-content-as-approved

Assert that arbitrary text content matches a stored approved file.

`sameContentAsApproved` follows the same approval workflow as [`sameJsonAsApproved`](same-json-as-approved.md) but:
- Uses a `.content` file extension instead of `.json`
- Accepts any `String` value — no JSON parsing is performed
- Content is compared byte-for-byte (Windows `\r\n` line endings are normalised to `\n`)

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

## File Control

All file-naming and path options available to `sameJsonAsApproved` work identically:

```java
// Distinct approved file per assertion in the same test
assertThat(header, sameContentAsApproved().withUniqueId("header"));
assertThat(body,   sameContentAsApproved().withUniqueId("body"));

// Custom file name
assertThat(actual, sameContentAsApproved().withFileName("my-template"));

// Machine-readable output for CI and AI agents
assertThat(actual, sameContentAsApproved().withMachineReadableOutput());
```

See [file-control](file-control.md) for the full list of options.

## What Is Not Available

The following features are specific to JSON and bean matchers and do **not** apply to `sameContentAsApproved`:

- `.ignoring()` — field exclusion (there are no fields in raw text)
- `.with()` / `.withMatcher()` — field-level custom matching
- `.sortField()` / `.sortType()` — collection sorting
- `.withAlias()` / `.withAliasMap()` — value aliasing

For content with volatile sub-strings (generated IDs, timestamps), the recommended approach is to make the system deterministic at the source (see [dynamic-values](dynamic-values.md)), or pre-process the string before asserting:

```java
// Replace the volatile token before asserting
String stable = actual.replaceAll("\\d{4}-\\d{2}-\\d{2}", "<date>");
assertThat(stable, sameContentAsApproved());
```

## Unicode Support

Full Unicode is supported, including non-Latin scripts. The library stores and compares content byte-for-byte in the approved file.

## Related

- [file-control](file-control.md) — custom file names, paths, and in-place update
- [same-json-as-approved](same-json-as-approved.md) — JSON-specific approval matching
- [dynamic-values](dynamic-values.md) — strategies for volatile values
