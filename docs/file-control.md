<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=eb4371f0-15ff-46c6-b867-c365bff5a636" />

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

## Configuring the test source root

By default approved files are stored alongside the test class under `src/test/java`. When your tests live in a different source directory — integration tests under `src/it/java`, or Kotlin/Groovy tests under `src/test/kotlin` — point approvalcrest at that directory with the `fileMatcherSourceRoot` property (alias `fmSourceRoot`):

```bash
# Integration tests under src/it/java
mvn verify -DfileMatcherSourceRoot=src/it/java

# Short alias
mvn verify -DfmSourceRoot=src/it/java
```

The value is a path relative to the project root. It sets the base directory the test class's package path is appended to when resolving approved files, so `com.example.MyTest` with `-DfileMatcherSourceRoot=src/it/java` resolves to `src/it/java/com/example/`. Per-matcher overrides (`.withPath`, `.withPathName`, `.withRelativePathName`) still take precedence when set.

The property applies to both ways of identifying a test — stack-trace detection and the injected `TestInfo` / `Description` / `Method` — so it cannot move approved files for some tests but not others.

### Defaults per framework

The property is shared by every framework; only the value used **when it is unset** differs, so a project that does not set it keeps the layout its framework expects:

| Matchers | Default when the property is unset |
|---|---|
| JUnit 4, JUnit 5 Jupiter, TestNG | `src/test/java` |
| `approvalcrest-junit-jupiter-kotlin` | `src/test/kotlin` |

> **One property, one root.** Because there is a single property, setting it in a project with **both** Java and Kotlin tests moves both. A mixed project that needs two different roots should leave the property unset and use the per-matcher overrides for whichever set of tests is non-standard. Getting this wrong fails loudly — the approved file is simply not found — rather than silently comparing against the wrong file.

## Migration from pre-1.0.0 Without Moving Files

In pre-1.0.0, `withPathName(str)` resolved relative to the **working directory**. Since 1.0.0, it resolves relative to **testClassPath**. If your tests used `withPathName` with a path from the project root, the approved files would now be looked up under the wrong directory.

To keep approved files in place **without moving them**, split the old `withPathName` call into `withRelativePathName` (base from working directory) + `withPathName` (subdirectory):

**Before (pre-1.0.0):**

```java
// Resolved from working directory → src/test/jsons/<file>
assertThat(actual, sameJsonAsApproved()
    .withPathName("src/test/jsons")
    .withFileName("my-snapshot"));
```

**After (1.0.0+):**

```java
// withRelativePathName resolves from workingDir, withPathName adds subdirectory
assertThat(actual, sameJsonAsApproved()
    .withRelativePathName("src/test")
    .withPathName("jsons")
    .withFileName("my-snapshot"));
```

Both produce the same absolute path: `{projectRoot}/src/test/jsons/my-snapshot-approved.json`. The approved file stays in place — no rename or move needed.

**Resolution logic when both are set:**

```
fileNameWithPath = workingDirectory() / relativePathName / pathName / fileName
```

Since `workingDirectory()` is the project root, this is equivalent to the old `withPathName` behaviour that resolved directly from the working directory.

## In-Place Update

Re-running tests with `-DfileMatcherUpdateInPlace=true` overwrites approved files with the current actual output:

```bash
mvn test -DfileMatcherUpdateInPlace=true
```

Use this **locally** after intentional changes (e.g. adding a field, changing serialisation). Review the diffs, then commit the updated approved files.

**Never set this in the build that gates merge.** A verification build has to fail on drift rather than absorb it, and every approved file must exist before it runs. A deliberate regeneration job is a different thing, triggered on demand and reviewed before its output is committed — see [best-practices](best-practices.md#ci-workflow).

### Custom matchers during a regeneration run

JSON only — `sameContentAsApproved` compares text and has no custom matchers.

Custom matchers are evaluated before the content comparison and stop the assertion on the first failure, which also stops the rewrite. That file then never regenerates, however many times the job is re-run — and any later assertion in the same test method is skipped with it, as is every later module unless the build uses `--fail-at-end`. `-DfileMatcherSkipCustomMatchersOnUpdate=true` defers the evaluation for that run:

```bash
mvn test -DfileMatcherUpdateInPlace=true -DfileMatcherSkipCustomMatchersOnUpdate=true
```

It does nothing unless `fileMatcherUpdateInPlace` is also set, so a verification build is unaffected however the property is configured.

**It applies to the whole run.** A test whose approved file is already current has its matchers skipped too, so it passes where it would otherwise have failed. Nothing is lost permanently — a custom matcher's verdict comes from the actual object, never from the approved file, so no regeneration can fix one and the failure returns on the next verification run. But a regeneration run is not a verification of anything, and its green result must not be read as one. That is why the flag is off by default and belongs on the regeneration job alone.

What it changes, and what it does not:

- `.with()` and `.withMatcher()` still remove their field, so the regenerated file is byte-for-byte the one a run whose matchers happened to pass would have written. That is the point: the file has to be what the next verification run compares against.
- `.alsoCheck()` and `.alsoCheckMatching()` keep their field, so the regenerated file carries the value the matcher rejects. Expect that test to fail on the next verification run.
- A field path that names nothing is no longer an error **for a file that needed rewriting**, because the check that raised it was part of the evaluation. On a file already in sync the deferred evaluation still runs, and still raises.
- A path that crosses a non-object — `beanChar.subpath` — still fails, exactly as the same path under `.ignoring()` does. That comes from removing the field, not from matching it.
- With `-DfmSharedEnabled=true`, tests that previously stopped at a failing matcher can now attach to or detach from a shared canonical.

## Machine-Readable / AI-Friendly Output

When a mismatch occurs, enable structured output so AI agents and CI tooling can parse failure details.

**Fluent API:**

```java
assertThat(actual, sameJsonAsApproved().withMachineReadableOutput());
```

**System property (applies to all tests in the run):**

```bash
mvn test -DfileMatcherMachineReadable=true
# or using the short aliases:
mvn test -DfmAI=true
mvn test -DfMMReadable=true
```

### Message format

All machine-readable messages are valid JSON objects emitted in compact form (no whitespace) to minimise token usage for AI consumers. The exact fields depend on the failure type.

**File matcher — content mismatch:**
```
{"failureType":"MISMATCH","test":"MyTest#myTestMethod","approvedFile":"/abs/path/to/4ac405/11b2ef-approved.json","action":"Set system property fMUInPlace=true and re-run to update the approved file","expected":"{...approved content...}","actual":"{...current serialized value...}","ignoredFields":[{"path":"createdAt","reason":"IGNORE_PATH"},{"path":"metadata","reason":"REMOVED_EMPTY","causes":["metadata.id","metadata.secret"]}],"aliasedFields":[{"path":"userId","originalValue":"a1b2c3d4-...","alias":"%%IGNORED-UUID%%"}],"sortedFields":[{"path":"items","reason":"SORT_PATH"},{"path":"tags","reason":"SORT_PATTERN","pattern":"a string containing \"tag\""}]}
```

**File matcher — no approved file yet:**
```
{"failureType":"NEW_FILE","test":"MyTest#myTestMethod","notApprovedFile":"/abs/path/to/4ac405/11b2ef-not-approved.json","approvedFile":"/abs/path/to/4ac405/11b2ef-approved.json","action":"Set system property fMUInPlace=true and re-run, or copy the not-approved file to approvedFile path"}
```

**Bean matcher — value mismatch:**
```
{"failureType":"MISMATCH","expected":"{...expected...}","actual":"{...actual...}","ignoredFields":[],"aliasedFields":[],"sortedFields":[]}
```

**Bean matcher — incompatible types:**
```
{"failureType":"TYPE_MISMATCH","expectedType":"com.example.Foo","actualType":"com.example.Bar","action":"Add .skipClassComparison() to the matcher, or set system property bMSCComparison=true"}
```

### ignoredFields tracking

The `ignoredFields` array records **which fields were actually removed** during filtering and **why**. Each entry contains:

| Field | Description |
|-------|-------------|
| `path` | The dotted path of the removed field (e.g. `address.zipCode`) |
| `reason` | One of: `IGNORE_PATH`, `CUSTOM_MATCHER`, `CUSTOM_MATCHER_PATTERN`, `IGNORE_PATTERN`, `REMOVED_EMPTY` |
| `pattern` | *(only for pattern-based)* The matcher description that matched |
| `causes` | *(only for `REMOVED_EMPTY`)* Child fields whose removal left this parent empty |

**Reason values:**
- `IGNORE_PATH` — removed by `.ignoring("fieldPath")`
- `CUSTOM_MATCHER` — removed by `.with("fieldPath", matcher)` (validated separately, unless a regeneration run deferred it)
- `CUSTOM_MATCHER_PATTERN` — removed by `.withMatcher(patternMatcher, valueMatcher)`
- `IGNORE_PATTERN` — removed by `.ignoring(Matcher<String>)` applied to the JSON tree
- `REMOVED_EMPTY` — parent became empty after its children were removed

`.alsoCheck(...)` and `.alsoCheckMatching(...)` produce no entry at all: they add an assertion without
removing anything, so there is nothing to report.

**Note:** Type-based ignoring (`.ignoring(Class)`) and pattern-based ignoring (`.ignoring(Matcher)`) are applied during Gson serialization via an `ExclusionStrategy`, so individual field removals cannot be tracked. When these are configured, a `"note"` field explains this.

### aliasedFields tracking

The `aliasedFields` array records value replacements made by aliasing. Each entry contains:

| Field | Description |
|-------|-------------|
| `path` | The dotted path of the aliased field |
| `originalValue` | The original value before aliasing |
| `alias` | The replacement value |

### sortedFields tracking

The `sortedFields` array records **which array fields were actually sorted** due to user-configured sorting. Each entry contains:

| Field | Description |
|-------|-------------|
| `path` | The dotted path of the field whose array was sorted (e.g. `items`, `order.lineItems`) |
| `reason` | One of: `SORT_PATH`, `SORT_PATTERN` |
| `pattern` | *(only for `SORT_PATTERN`)* The matcher description that matched |

**Reason values:**
- `SORT_PATH` — sorted by `.sortField("fieldPath")` or `.sortFieldPath(SortField.of("path"))`
- `SORT_PATTERN` — sorted by `.sortField(Matcher<String>)` or `.sortFieldMatcher(SortField.of(matcher))`

**Note:** Type-based sorting (`.sortType(Class)`) is applied during Gson serialization (via a field-name marker), so individual field sorts cannot be tracked. When this is configured, the `"note"` field explains this limitation.

### Passive AI discovery tip

In the default (non-machine-readable) mode, every failure message ends with:

```
[AI tip] Re-run with system property fmAI=true for structured, machine-readable output.
```

This allows AI agents that encounter a normal test failure to discover the machine-readable mode without any explicit configuration.

## Pointer Files and Shared Approvals

An approved file may contain a _pointer reference_ instead of content. The library follows the pointer transparently so tests require no code changes:

```
/*com.example.MyTest.myTestMethod*/
/*pointer:src/test/java/shared-approvals/a1/a1b2c3d4e5f6-4827-approved.json*/
```

**In-place update on a pointer file** (`-DfileMatcherUpdateInPlace=true`):

- With `-DfmSharedEnabled=false` (default): detaches the pointer and replaces it with a standalone approved file. The canonical is untouched.
- With `-DfmSharedEnabled=true`: writes a new pointer if a matching canonical exists; otherwise detaches as above.

Run `mvn approvalcrest:dedup` after tests to re-consolidate detached files back into shared canonicals.

See [shared-approvals](shared-approvals.md) for the full deduplication workflow.

## Related

- [best-practices](best-practices.md)
- [same-json-as-approved](same-json-as-approved.md)
- [shared-approvals](shared-approvals.md)
