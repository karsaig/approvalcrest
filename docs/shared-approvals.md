<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=eb4371f0-15ff-46c6-b867-c365bff5a636" />

# Shared Approvals

When many tests verify identical content — for example, all tests that assert on the same email template rendered for the same user — the repository accumulates thousands of duplicate approved files. Shared approvals solve this by replacing duplicate files with lightweight _pointer files_ that reference a single _canonical_ approved file.

## How it works

### Pointer files (always active)

An approved file can contain a _pointer reference_ instead of JSON content:

```
/*com.example.EmailServiceTest.testSendWelcomeEmailToUser1*/
/*pointer:src/test/java/shared-approvals/a1/a1b2c3d4e5f6-4827-approved.json*/
```

The first line is the normal test comment header. The second line, instead of JSON, is a pointer comment with the path to the canonical file — relative to the module's working directory (project root).

When a test loads an approved file that contains a pointer, the library follows the pointer transparently and uses the canonical's content for comparison. **No configuration is required** to follow pointers; any test that checks out a repository containing committed pointer files and canonicals will pass without setting any system properties.

### Canonical files

A canonical file lives in the configured shared directory (`src/test/java/shared-approvals` by default) and contains the actual approved content. Its name is derived from the content's SHA-256 hash prefix and byte length:

```
{sharedDir}/{bucket}/{sha256prefix12}-{byteLen}-approved.{ext}
```

For example:
```
src/test/java/shared-approvals/a1/a1b2c3d4e5f6-4827-approved.json
```

The `{bucket}` directory is the first N characters of the hash (default N=2, giving 256 buckets). Using both the hash prefix and the byte size effectively prevents hash collisions for practical file sizes. A value outside the supported range of 1–6 is rejected with a configuration error naming the property.

> **Changing the bucket depth relocates every canonical.** The depth is part of the path a pointer resolves to, so after changing it the existing canonicals are no longer found and duplicate content is silently written again under the new layout. To migrate, run `mvn approvalcrest:reinstate` with the old depth first, then `mvn approvalcrest:dedup` with the new one.

## Deduplication workflow

### One-time setup (no test re-runs required)

After your test suite has generated all approved files, run the dedup tool. It performs pure file I/O — no tests are executed:

```bash
# as a Maven plugin (reads fmSharedDir from system property or POM)
mvn approvalcrest:dedup

# standalone JAR
java -jar approvalcrest-dedup-VERSION-standalone.jar
```

**What the dedup tool does:**

1. Scans for all `*-approved.{json|content}` files that are not yet pointer files (excludes the shared directory)
2. Groups files by content key (SHA-256 prefix + byte size, per file extension)
3. For each group of 2 or more identical files: writes one canonical to the shared directory, then replaces every group member with a pointer file
4. For files whose content already matches an existing canonical: writes a pointer immediately (handles new tests added after an earlier dedup run)
5. Garbage-collects any canonical in the shared directory not referenced by any pointer file
6. Prints a summary: canonicals created, pointers written, orphans removed

Every run is **idempotent** — running it again on an already-deduped tree is a no-op.

**Scan directory vs. reference lookup.** `--dir` controls which files are *converted* into pointers. It does not control which canonicals are considered *in use*: that is looked up across the whole project, because a pointer outside the scanned subtree needs its canonical just as much as one inside it. This matters when you narrow `--dir`, or run the goal per module against a shared directory covering several modules — the canonicals belonging to the modules you did not scan are left alone.

If the shared directory is outside the project directory, the pointers referencing it cannot be found, so garbage collection is skipped entirely and the summary says so.

**Overlapping directories are refused.** If the shared approvals directory contains the scan directory, every approved file in the scan tree would be treated as a canonical. Both goals refuse to start in that case rather than delete them.

### Subsequent test runs

Normal test runs work without any configuration. After a code change that affects the output, the library detects a mismatch as usual. In-place update behaviour depends on the `fmSharedEnabled` setting:

| `fmSharedEnabled` | Behaviour on content change |
|---|---|
| `false` (default) | Detaches the pointer and writes standalone content (canonical untouched) |
| `true` | If a matching canonical exists: writes a new pointer. If not: detaches and writes standalone content, printing a notice |

After detachment, run `mvn approvalcrest:dedup` again to re-consolidate tests that converged on the same new content.

### Reinstate (undo deduplication)

To replace all pointer files with standalone approved files and clear the shared directory:

```bash
mvn approvalcrest:reinstate

# or with the standalone JAR
java -jar approvalcrest-dedup-VERSION-standalone.jar --reinstate
```

Reinstate replaces the pointers under `--dir` with their content, then deletes the canonicals that nothing references any more. As with dedup, the reference lookup spans the whole project, so reinstating one module does not strip the canonicals another module still points at.

Pass `--dry-run` (or `-DdryRun=true` for the Maven goal) to print what would happen without writing or deleting anything:

```bash
mvn approvalcrest:reinstate -DdryRun=true
java -jar approvalcrest-dedup-VERSION-standalone.jar --reinstate --dry-run
```

Use reinstate when:
- Changing the shared directory location (reinstate with old config → dedup with new config)
- Fully opting out of the shared file system

## Configuration

| Property | Alias | Default | Description |
|---|---|---|---|
| `fileMatcherSharedDir` | `fmSharedDir` | `src/test/java/shared-approvals` | Path to the shared directory for canonical files, relative to the project root |
| `fileMatcherSharedEnabled` | `fmSharedEnabled` | `false` | Activates write-side integration: new approved files and in-place updates check for matching canonicals |
| `fileMatcherSharedDirBucketDepth` | `fmSharedDirBucketDepth` | `2` | Number of leading hash characters used as the bucket subdirectory name (1–6) |

> **Warning:** Changing `fileMatcherSharedDirBucketDepth` after pointer files have been committed invalidates all existing pointers (the canonical paths are different). Use `mvn approvalcrest:reinstate` with the old depth, then `mvn approvalcrest:dedup` with the new depth.

### Enabling write-side integration

Set `-DfmSharedEnabled=true` when running tests to activate pointer creation:

```bash
# New test: creates a pointer not-approved file if a matching canonical exists
mvn test -DfmSharedEnabled=true

# In-place update: writes a pointer if a matching canonical exists
mvn test -DfileMatcherUpdateInPlace=true -DfmSharedEnabled=true
```

With `fmSharedEnabled=false` (the default), the library still **reads** pointer files correctly — it just does not create new ones automatically during test runs. Deduplication itself is always done via the CLI/plugin tool.

## Stale pointers

A stale pointer is one whose target path is outside the currently configured `fmSharedDir` (e.g., the shared directory was renamed and pointers still reference the old path). Behaviour is controlled by the existing `fMStrictMatching` flag:

| `fMStrictMatching` | Behaviour |
|---|---|
| `true` (default) | Test fails with a clear "Stale pointer" error that includes the target path and instructions to run reinstate + dedup |
| `false` | The pointer is followed regardless of whether the target is within `fmSharedDir` (lenient mode) |

A pointer whose target file does not exist at all fails unconditionally.

## CLI options

```
java -jar approvalcrest-dedup-VERSION-standalone.jar [options]

  --dir <path>          Scan directory (default: src/test/java)
  --shared-dir <path>   Shared approvals directory (default: src/test/java/shared-approvals)
  --bucket-depth <n>    Bucket prefix depth 1-6 (default: 2)
  --dry-run             Print what would happen without writing files
  --reinstate           Replace all pointers with standalone content and clear shared dir
```

## CI integration example

Run dedup as a post-test step in CI to automatically consolidate newly identical approved files:

```yaml
# GitHub Actions example
- name: Run tests
  run: mvn test

- name: Deduplicate approved files
  run: mvn approvalcrest:dedup

- name: Commit deduplicated approvals (if changed)
  run: |
    git diff --quiet || (git add -A && git commit -m "chore: deduplicate approved files")
```

## Pointer file format reference

A pointer approved file has two lines:

```
/*{testClass}.{testMethod}*/
/*pointer:{relativePathToCanonical}*/
```

- Line 1: the normal comment header (same as every approved file)
- Line 2: `/*pointer:` followed by the path relative to the **project root** (working directory), then `*/`
- The extension (`.json` or `.content`) matches the original approved file

## Related

- [file-control](file-control.md)
- [system-properties](system-properties.md)
