<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=61ed9090-b10b-49a1-8e28-7950aacf9ddd" />

Approvalcrest
===========
[![Java CI with Maven](https://github.com/karsaig/approvalcrest/actions/workflows/maven.yml/badge.svg)](https://github.com/karsaig/approvalcrest/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.karsaig/approvalcrest.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.karsaig%22%20AND%20a:%22approvalcrest%22)

## Contents

- [What is Approvalcrest](#what-is-approvalcrest)
- [AI-friendly mode](#ai-friendly-mode)
- [Version support](#version-support)
- [Installation](#installation)
- [Documentation](#documentation)
- [Changelog](CHANGELOG.md)

## What is Approvalcrest

Approvalcrest is a Java testing library for [golden master / characterisation testing](https://en.wikipedia.org/wiki/Characterization_test). It provides three matchers:

- **`sameBeanAs(expected)`** — performs a deep structural comparison of two live Java objects. Both are serialised to JSON and diffed field by field across the entire object graph. This intentionally bypasses Java's `equals()` method, which is often inadequate for testing: it can miss fields, be incorrectly implemented, or return `true` for objects that differ in ways that matter.

- **`sameJsonAsApproved()`** — serialises the actual object to JSON and compares it against a stored *approved file* that you review once and commit. Any unexpected change to any field fails the test immediately.

- **`sameContentAsApproved()`** — same approval workflow as above, but for arbitrary text content (templates, API responses, rendered HTML, log output) rather than structured objects.

Traditional assertions *allowlist* specific properties — you explicitly name the fields you want to check. Characterisation testing takes the opposite approach: it *blocklists* changes. Once you approve a snapshot, any unexpected change in any field will fail the test immediately. This is analogous to default-deny in security: silent changes do not pass.

This makes approvalcrest a particularly effective guardrail when working with AI coding agents — snapshot coverage catches unintended side effects that targeted per-field assertions would miss. The library works at every level of testing: unit, integration, API, UI, and system tests all use the same API.

Approvalcrest was originally built on top of [Shazamcrest](https://github.com/shazam/shazamcrest), extending and evolving its functionality.

## AI-friendly mode

When a test fails, enable machine-readable output so that AI agents and CI tooling can parse the failure details:

```java
// Fluent API:
assertThat(actual, sameJsonAsApproved().withMachineReadableOutput());
```
```
# Or via system property (short alias fmAI also works):
mvn test -DfileMatcherMachineReadable=true
```

In this mode, failure messages are structured JSON containing the expected/actual content, the approved file path, an `ignoredFields` array showing which fields were removed and why, an `aliasedFields` array showing value replacements, and a `sortedFields` array showing which arrays were sorted. See [file-control.md](docs/file-control.md) for details.

## Version support

| Dimension | Supported | Constantly tested on |
|---|---|---|
| JDK | 8+ | 8, 11, 17, 21, 25 |
| JUnit | 4, 5, 6 | 4 (4.13.2), 5 (5.14.4), 6 (6.1.0) |
| TestNG | 6+ | 6.14.3, 7.10.2 |
| Kotlin | 1.x+ | 2.2.21 |

## Installation

Choose the artifact that matches your test runner.

### JUnit 4 & JUnit 5 Vintage

```xml
<dependency>
  <groupId>com.github.karsaig</groupId>
  <artifactId>approvalcrest</artifactId>
  <version>1.4.0</version>
  <scope>test</scope>
</dependency>
```

### JUnit 5 & 6 Jupiter

```xml
<dependency>
  <groupId>com.github.karsaig</groupId>
  <artifactId>approvalcrest-junit-jupiter</artifactId>
  <version>1.4.0</version>
  <scope>test</scope>
</dependency>
```

### Kotlin + JUnit 5 & 6

```xml
<dependency>
  <groupId>com.github.karsaig</groupId>
  <artifactId>approvalcrest-junit-jupiter-kotlin</artifactId>
  <version>1.4.0</version>
  <scope>test</scope>
</dependency>
```

### TestNG

```xml
<dependency>
  <groupId>com.github.karsaig</groupId>
  <artifactId>approvalcrest-testng</artifactId>
  <version>1.4.0</version>
  <scope>test</scope>
</dependency>
```

For Gradle, replace the `<dependency>` blocks with the equivalent `testImplementation` notation.

## Documentation

| Doc | Description |
|---|---|
| [Getting started](docs/getting-started.md) | Artifact selection, minimal examples, IDE diff view |
| [sameBeanAs](docs/same-bean-as.md) | Bean-to-bean comparison |
| [sameJsonAsApproved](docs/same-json-as-approved.md) | JSON approval workflow and file naming |
| [sameContentAsApproved](docs/same-content-as-approved.md) | Raw-text approval workflow |
| [Ignoring fields](docs/ignoring-fields.md) | `.ignoring()` by path, Hamcrest matcher, or type |
| [Custom matching](docs/custom-matching.md) | `.with(path, matcher)` for field-level assertions |
| [Sorting](docs/sorting.md) | Stable collection ordering |
| [Aliasing](docs/aliasing.md) | Replace volatile values with readable placeholders |
| [Dynamic values](docs/dynamic-values.md) | Handling UUIDs, timestamps, and other run-to-run changes |
| [File control](docs/file-control.md) | withUniqueId, withFileName, withPath, in-place update, machine-readable diff |
| [Field access modes](docs/field-access-modes.md) | Safe, force, and fallback modes — JDK module handling |
| [Supported types](docs/supported-types.md) | java.time.\*, Optional, Path, Throwable, circular references |
| [Best practices](docs/best-practices.md) | State management, CI workflow, approved file discipline |
| [JUnit 4 & Vintage](docs/junit4-vintage.md) | JUnit 4 / JUnit 5 Vintage specifics |
| [JUnit 5 & 6 Jupiter](docs/junit5-jupiter.md) | JUnit 5 & 6 Jupiter specifics |
| [TestNG](docs/testng.md) | TestNG specifics |
| [Kotlin](docs/kotlin.md) | Kotlin extension functions and KT-5464 workaround |

