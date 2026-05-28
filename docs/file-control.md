# file-control

Customise approved file names, paths, and update behaviour.

## `.withUniqueId(String id)`

Appends an ID to the filename — essential for parameterized tests where multiple cases share a method name:

```java
assertThat(value, sameJsonAsApproved().withUniqueId("case1"));
// → creates: <classHash>/<methodHash>-case1-approved.json
```

**Note:** if `id` starts with `-`, the leading `-` is absorbed into the separator so there is no double-dash:

```java
assertThat(value, sameJsonAsApproved().withUniqueId("-scenario1"));
// → creates: <classHash>/<methodHash>-scenario1-approved.json  (single dash)
```

## `.withFileName(String name)`

Override the auto-generated filename entirely:

```java
assertThat(actual, sameJsonAsApproved().withFileName("my-custom-name"));
// → creates: <classHash>/my-custom-name-approved.json
```

## `.withPath(Path path)`

Override the directory where approved files are stored:

```java
assertThat(actual, sameJsonAsApproved()
    .withPath(Paths.get("src/test/resources/approved")));
```

Useful for centralising approved files in a shared directory rather than alongside each test class.

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
