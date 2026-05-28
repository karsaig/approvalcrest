# best-practices

Guidelines for effective use of approvalcrest in real projects.

## Testing at Any Level

approvalcrest assertions work identically at every testing level — unit, integration, API, UI, and system. Whether you are snapshotting a domain object, a REST response, a rendered HTML page, or full system output, the API and workflow are the same.

## Approved Files Are Production Artefacts

- **Commit them to version control.** They are part of the test suite.
- **Review changes in pull requests** the same way you review application code. An unexpected diff in an approved file is a change signal, not just a test update.
- **Never commit `*-not-approved.*` files.** If one appears in CI, the test has not been approved — treat it as a failing build.

## State Management

Tests must produce deterministic output. Before each test:

- Reset any database, cache, or service state
- Use transactions that roll back, in-memory stores, or test fixtures
- Control non-deterministic input (random data, current time, UUIDs) — see [dynamic-values](dynamic-values.md)

## CI Behaviour

- Run with the standard Maven command — do **not** pass `-DfileMatcherUpdateInPlace=true` in CI.
- All approved files must already exist before CI runs.
- A missing approved file causes the test to create a `*-not-approved.*` file and fail — this is the correct behaviour.

## In-Place Update Is a Local Workflow Tool

After an intentional change (adding a field, changing serialisation):

```bash
mvn test -DfileMatcherUpdateInPlace=true
```

Review the diffs, then commit the updated approved files. Never automate this in CI.

## File Organisation

By default, approved files live alongside the test source files in the same package directory. For large projects, centralise them:

```java
assertThat(actual, sameJsonAsApproved()
    .withPath(Paths.get("src/test/resources/approved")));
```

## Dynamic Values

See [dynamic-values](dynamic-values.md) for strategies to handle UUIDs, timestamps, and other values that change between runs.

## Related

- [dynamic-values](dynamic-values.md)
- [file-control](file-control.md)
