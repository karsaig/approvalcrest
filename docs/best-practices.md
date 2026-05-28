# best-practices

Guidelines for effective use of approvalcrest in real projects.

## Choosing Between `sameBeanAs` and File Matchers

- **`sameBeanAs(expected)`** — you maintain the expected object in code. The assertion is self-contained in the test file. Best when the expected value is straightforward to build and you want the test to read clearly alongside its assertion.
- **`sameJsonAsApproved()`** — you review the serialised output once, rename the file to approve it, and commit it. Best for complex or large object graphs where maintaining an expected object in code becomes cumbersome. The approved file becomes the precise, versioned specification of the expected output.

## What an Approved File Represents

Once renamed from `*-not-approved.json` to `*-approved.json`, an approved file is functionally equivalent to an expected value written directly in code — but stricter. Traditional assertions assert the specific fields you remembered to check; an approved file asserts every field in the entire object graph. Any unexpected change, anywhere, fails the test immediately.

Treat approved files as production artefacts:
- **Commit them to version control** — they are part of the test suite
- **Review changes in pull requests** the same way you review application code
- **Never commit `*-not-approved.*` files** — if one appears in CI, the test has not been approved; treat it as a failing build

## Testing at Every Level

approvalcrest works at every testing level. The API and approval workflow are identical regardless of scope.

### Unit Tests — Best Fit

Both `sameBeanAs` and `sameJsonAsApproved` work equally well at the unit level. State control is trivial — everything is in-process and fast to reset. Tests run quickly, so even a change that affects many tests can be handled locally: run with `-DfileMatcherUpdateInPlace=true`, review the diff, and commit the updated approved files. No CI involvement needed for small changes.

### Integration Tests — Effective but Fiddlier

Integration tests sit close to implementation code, which means two challenges:
- Implementation details can leak into the serialised output, making approved files sensitive to internal changes that do not affect external behaviour
- State (databases, caches, queues) is harder to reset cleanly than in unit tests

With good state management (see below) integration tests work well with approvalcrest. Be deliberate about what you include in the snapshot — sometimes ignoring or aliasing internal-only fields makes the approved file a more stable contract.

### API Tests — Natural Fit

The output is already a serialised format (JSON, XML, plain text). Feed the response body directly to `sameJsonAsApproved()` or `sameContentAsApproved()`. State setup is done via API calls, which is natural at this level. Changes affecting many endpoints are handled cleanly with the CI patch workflow below.

### E2E and UI Tests

Two patterns have proven effective:

**Page Object Model with `getModel()`:**

Add a `getModel()` method to each Page Object that extracts the interesting state of the page into a DTO. Feed the DTO to `sameJsonAsApproved()`:

```java
ProductPage page = navigator.to(ProductPage.class);
assertThat(page.getModel(), sameJsonAsApproved());
```

The DTO captures exactly what matters — prices, labels, availability — without fragile element selectors scattered across the test. Approved files document the expected page state precisely. When a UI change affects many pages, the CI update job regenerates all approved files in one run.

**DOM fragment with `sameContentAsApproved()`:**

Grab a section of the rendered DOM and assert it directly:

```java
String component = driver.findElement(By.id("product-card")).getAttribute("outerHTML");
assertThat(component, sameContentAsApproved());
```

This is more brittle than the DTO approach (whitespace, generated class names, and attribute ordering can cause noise), but it is simpler to set up and has proven useful for catching unexpected DOM structure changes.

Page Object `getModel()` is one natural fit for E2E testing, but any pattern where you can extract meaningful state into an assertable form works the same way.

## State Management

Tests must produce deterministic output. The best approach is to reset every piece of stateful infrastructure to a known baseline before each test:

- **Database**: truncate tables, roll back transactions, or restore a fixed fixture snapshot
- **Caches**: clear or pre-warm to a defined state
- **Message queues**: drain or seed with known messages
- **Clocks and ID generators**: inject and control so output is fully predictable

Full state isolation has three compounding benefits:
1. Each test is **independent** — no test can be broken by side effects from a previous one
2. Tests are **reproducible** when run in isolation — the same result every time
3. The entire suite can run **in parallel** safely — no shared mutable state means no race conditions

See [dynamic-values](dynamic-values.md) for detailed strategies including clock injection, state reset patterns, and fallback approaches for legacy systems.

## CI Workflow

### Two-Job Setup

Run two separate CI jobs:

**Job 1 — Verify (runs on every push, required to pass before merge):**

```bash
mvn test
```

Fails immediately if any approved file is missing or differs from actual output.

**Job 2 — Patch Generation (triggered manually or on-demand):**

```bash
mvn test -DfileMatcherUpdateInPlace=true
git diff > approved-files.patch
```

Publish `approved-files.patch` as a CI artifact. After reviewing the patch, download and apply it:

```bash
git apply approved-files.patch
```

Then commit the approved file changes (in a separate commit — see below) and push. Job 1 will now pass.

### Choosing Your Path

**Small change (few affected tests):** run locally with `-DfileMatcherUpdateInPlace=true`, review the diff, commit the updated approved files.

**Large change (many affected tests):** push the code change, trigger the patch-generation job, review the patch artifact, iterate or fix locally if issues are found, then apply and commit the approved files once happy.

## Reviewing Approved File Changes

### Keep Approved File Changes in Separate Commits

When a code change causes many approved files to update, commit the approved file changes separately from the application code changes:

```
commit 1: Rename UserDto fields (application code)
commit 2: Update approved files for UserDto rename
```

This makes pull request review tractable — reviewers can look at the code change and the approved file change independently and understand the relationship between them.

### De-duplicate Large Diffs

When a single change affects hundreds of approved files — for example, renaming a field that appears in every snapshot — the diff is highly repetitive. A small script that collapses identical change patterns into a summary makes review trivial even for enormous change sets.

### Review Gets Faster Over Time

The first few times a reviewer sees approved files they focus on the structure: field names, nesting, types. After a couple of reviews the schema becomes familiar and trusted. Subsequent reviews can focus directly on the interesting parts — the values that changed, new fields, business logic output.

Problems surface automatically in both contexts:
- **Locally**: the IDE diff view highlights exactly which field changed and what both values are — no hunting through logs
- **In the PR**: the approved file appears as a normal file diff — the change is visible in context, just like any other code change; reviewers do not need to run anything

## File Organisation

Keep approved files alongside the test source files in the same package directory. This is the default behaviour and the correct approach: each approved file lives next to the test that owns it, making it easy to find, review, and understand what it covers. Separating approved files from their tests makes navigation harder and obscures the relationship between test and expectation.

See [file-control](file-control.md) for options to customise file naming and paths when needed for specific scenarios.

## Multiple Assertions in One Test

Each file-based matcher call derives its filename from the test class and method name. If a test makes more than one assertion with `sameJsonAsApproved()` or `sameContentAsApproved()`, all calls would resolve to the same filename and collide. Use `.withUniqueId(String id)` on every call to give each one a distinct filename:

```java
@Test
void checkBothStates() {
    assertThat(before, sameJsonAsApproved().withUniqueId("before"));
    assertThat(after,  sameJsonAsApproved().withUniqueId("after"));
}
```

The same rule applies to parameterized tests — each iteration must pass a distinct ID, typically the parameter name or index.

## Related

- [dynamic-values](dynamic-values.md)
- [file-control](file-control.md)
