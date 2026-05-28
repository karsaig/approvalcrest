# file-control

Customise approved file names, paths, and update behaviour.

All options on this page apply to both `sameJsonAsApproved` and `sameContentAsApproved`.

## `.withUniqueId(String id)`

Appends an ID to the approved filename. This is required in two common situations:

**Multiple assertions in the same test method** — each file-based matcher call in a test resolves to the same filename by default (class hash + method hash). Without a unique ID the second assertion would read/write the same file as the first, causing a collision:

```java
@Test
void myTest() {
    assertThat(result1, sameJsonAsApproved().withUniqueId("first"));
    assertThat(result2, sameJsonAsApproved().withUniqueId("second"));
    // → first-approved.json and second-approved.json — no collision
}
```

**Parameterized tests** — all iterations share the same method name, so each iteration needs a distinct ID:

```java
@ParameterizedTest
@MethodSource("cases")
void paramTest(String name, MyDto value) {
    assertThat(value, sameJsonAsApproved().withUniqueId(name));
    // → <methodHash>-<name>-approved.json per iteration
}
```

**Note:** if `id` starts with `-`, the leading separator is not doubled:

```java
assertThat(value, sameJsonAsApproved().withUniqueId("-scenario1"));
// → <methodHash>-scenario1-approved.json  (single dash)
```

## `.withFileName(String name)`

Override the auto-generated filename entirely:

```java
assertThat(actual, sameJsonAsApproved().withFileName("my-custom-name"));
// → creates: <classHash>/my-custom-name-approved.json
```

## `.withPath(Path path)`

Override the directory where approved files are stored. Use this for specific scenarios where the default location (alongside the test source) is not suitable — for example, storing approved files in a resources directory that is excluded from compilation:

```java
assertThat(actual, sameJsonAsApproved()
    .withPath(Paths.get("src/test/resources/approved")));
```

The default behaviour — approved files living next to the test class — is generally preferred. It keeps the approved file and the test that owns it together, making them easy to find and review.

## `.withPathName(String relativeStr)`

Override the approved file directory using a path **relative to the test class source directory**. Absolute paths are accepted as-is:

```java
assertThat(actual, sameJsonAsApproved()
    .withPathName("mydir"));
// → writes to: {testClassPath}/mydir/<methodHash>-approved.json
```

**Absolute path example:**

```java
assertThat(actual, sameJsonAsApproved()
    .withPathName("/abs/path"));
// → writes to: /abs/path/<methodHash>-approved.json
```

**Migration from pre-1.0.1:** previously `withPathName("mydir")` wrote to `mydir/` relative to the working directory. Move existing approved files into the `{testClassPath}/mydir/` subtree.

## `.withRelativePathName(String relStr)`

Override the approved file directory using a path **relative to the working directory**, with the class-name hash inserted as a subdirectory:

```java
assertThat(actual, sameJsonAsApproved()
    .withRelativePathName("snapshots"));
// → writes to: {workingDir}/snapshots/<classHash>/<methodHash>-approved.json
```

**Migration from pre-1.0.1:** previously `withRelativePathName("snapshots")` wrote to `{testClassPath}/snapshots/`. Move existing approved files to `{workingDir}/snapshots/<classHash>/`.

## In-Place Update

Re-running tests with `-DfileMatcherUpdateInPlace=true` overwrites approved files with the current actual output:

```bash
mvn test -DfileMatcherUpdateInPlace=true
```

Use this **locally** after intentional changes (e.g. adding a field, changing serialisation). Review the diffs, then commit the updated approved files.

**Never pass this flag in CI** — all approved files must exist before CI runs.

## Machine-Readable / AI-Friendly Output

When a mismatch occurs, enable structured output so AI agents and CI tooling can parse failure details.

**Fluent API:**

```java
assertThat(actual, sameJsonAsApproved().withMachineReadableOutput());
```

**System property (applies to all tests in the run):**

```bash
mvn test -DfileMatcherMachineReadable=true
```

In machine-readable mode, failure messages include:
- Absolute path to the approved file
- `=== ACTUAL (full) ===` … `=== END ACTUAL ===` blocks with the full current value
- A tip to re-run with `-DfileMatcherUpdateInPlace=true`

## Related

- [best-practices](best-practices.md)
- [same-json-as-approved](same-json-as-approved.md)
