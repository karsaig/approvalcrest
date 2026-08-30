<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=932587be-8781-4843-af47-fa58cbfd05d1" />

# Field Access Modes

Approvalcrest serializes Java objects to JSON for comparison using Gson. On JDK 9+, the module system restricts reflective access to internal fields of platform classes (`java.time.*`, `java.lang.*`, `java.math.*`, etc.). Historically this required adding `--add-opens` JVM flags to your test configuration.

Starting with version 1.2.0, approvalcrest handles this automatically — no `--add-opens` flags are needed by default. The behavior is controlled by the `approvalcrestReflection` system property (alias: `aCReflection`), which selects one of three modes.

## Mode Overview

| Mode | Property value | How it accesses fields | Output format | `--add-opens` needed | JDK support |
|---|---|---|---|---|---|
| **Safe** | `safe` (default) | Opens modules programmatically via Unsafe, then standard reflection | Field-based (identical to pre-module era) | No | 8, 9–25+ |
| **Force** | `force` | `setAccessible(true)` directly | Field-based (identical to pre-module era) | Yes (on JDK 9+) | 8, 9–25+ (with --add-opens) |
| **Fallback** | `fallback` | Public getter methods (`getX()` / `isX()`) | Getter-based (different property names) | No | 8, 9–25+ |

## Safe Mode (Default)

Safe mode is the default and recommended mode. It requires **zero configuration** — just add the dependency and run your tests.

### How it works

When approvalcrest encounters a type from a locked module (e.g. `java.time.LocalDate`), it uses a three-tier strategy:

1. **Programmatic module opening** — at startup, uses `sun.misc.Unsafe` to obtain a trusted `MethodHandles.Lookup`, then calls `Module.implAddOpens()` to open locked packages. This is the runtime equivalent of `--add-opens` but happens automatically.
2. **Standard reflection** — once the module package is opened, `field.setAccessible(true)` works normally. Gson's built-in serialization produces the same field-based JSON as always.
3. **Graceful degradation** — if Unsafe stops working, the code transparently falls back to getter-based serialization (same as fallback mode). This covers both stages of the JDK's plan: from **JDK 26** the memory-access methods throw `UnsupportedOperationException` by default even though `sun.misc.Unsafe` still exists, and in a later release the class is removed outright.

### When to use

- **Always**, unless you have a specific reason to use another mode.
- Migrating from `--add-opens`? Just remove the flags — safe mode replaces them.

### Example output

For a `RuntimeException("test error")`:
```json
{
  "class": "java.lang.RuntimeException",
  "detailMessage": "test error",
  "cause": null,
  "suppressedExceptions": []
}
```

This is field-based — the JSON keys match the actual field names in `java.lang.Throwable`.

## Force Mode

Force mode uses `field.setAccessible(true)` directly without any module-opening logic. This is the traditional behavior and will **always work as long as `--add-opens` is available on the JDK**.

### When to use

- You already have `--add-opens` configured and prefer explicit control.
- You're on a JDK where the safe mode's Unsafe-based approach doesn't work (unlikely but possible with custom security managers).
- Corporate policy requires explicit `--add-opens` declarations.

### Configuration

```shell
mvn test -DapprovalcrestReflection=force
```

### Required `--add-opens` Flags

The exact flags needed depend on which types your tests serialize. The most commonly needed flags for approvalcrest are:

```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.time=ALL-UNNAMED
--add-opens java.base/java.time.zone=ALL-UNNAMED
--add-opens java.base/java.math=ALL-UNNAMED
--add-opens java.base/java.io=ALL-UNNAMED
--add-opens java.base/java.security=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/sun.util.calendar=ALL-UNNAMED
--add-opens java.sql/java.sql=ALL-UNNAMED
```

If a flag is missing, approvalcrest detects the `InaccessibleObjectException` and shows you the exact flag you need to add.

### Maven Surefire Configuration

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            -DapprovalcrestReflection=force
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/java.time=ALL-UNNAMED
            --add-opens java.base/java.time.zone=ALL-UNNAMED
            --add-opens java.base/java.math=ALL-UNNAMED
            --add-opens java.base/java.io=ALL-UNNAMED
            --add-opens java.base/java.security=ALL-UNNAMED
            --add-opens java.base/java.util=ALL-UNNAMED
            --add-opens java.base/sun.util.calendar=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

### Gradle Configuration

```groovy
test {
    jvmArgs '-DapprovalcrestReflection=force',
            '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
            '--add-opens', 'java.base/java.time=ALL-UNNAMED',
            '--add-opens', 'java.base/java.time.zone=ALL-UNNAMED',
            '--add-opens', 'java.base/java.math=ALL-UNNAMED',
            '--add-opens', 'java.base/java.io=ALL-UNNAMED',
            '--add-opens', 'java.base/java.security=ALL-UNNAMED',
            '--add-opens', 'java.base/java.util=ALL-UNNAMED',
            '--add-opens', 'java.base/sun.util.calendar=ALL-UNNAMED'
}
```

### IntelliJ IDEA Configuration

In **Run/Debug Configuration** → **VM options**, add:

```
-DapprovalcrestReflection=force --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED
```

Or set it globally for all test runs: **Settings → Build, Execution, Deployment → Build Tools → Maven → Runner → VM Options**.

### Example output

Same as safe mode — field-based JSON:
```json
{
  "class": "java.lang.RuntimeException",
  "detailMessage": "test error",
  "cause": null,
  "suppressedExceptions": []
}
```

## Fallback Mode

Fallback mode disables Unsafe entirely and serializes locked-module types using their **public getter methods** (`getX()` / `isX()`). This produces different JSON property names compared to field-based modes.

### When to use

- **Future-proofing** — this is what approvalcrest degrades to automatically in safe mode once Unsafe stops working, which for most projects means **JDK 26**. Use fallback mode today to prepare your approved files for that.
- **Strict compliance** — your organization forbids any use of `sun.misc.Unsafe` or module-bypassing hacks.
- **Testing** — verify that your test suite works without relying on internal JDK APIs.

### Configuration

```shell
mvn test -DapprovalcrestReflection=fallback
```

### How it works

1. Types on the classpath (unnamed module) — your own POJOs, test data, third-party library classes — are serialized via **standard field access** (same as safe/force mode). The classpath is always accessible.
2. Types in locked JDK modules (`java.time.*`, `java.lang.*`, `java.math.*`) — serialized via **public getters**. The JSON keys correspond to getter names with the `get`/`is` prefix stripped and lowercased.
3. Types with registered custom adapters (e.g. `LocalDate`, `Instant`, `Path`) — still use their custom adapters regardless of mode, producing the same string representations.

### Example output

For a `RuntimeException("test error")`:
```json
{
  "cause": null,
  "class": "java.lang.RuntimeException",
  "localizedMessage": "test error",
  "message": "test error",
  "suppressed": []
}
```

Compare with field-based output:
```json
{
  "class": "java.lang.RuntimeException",
  "detailMessage": "test error",
  "cause": null,
  "suppressedExceptions": []
}
```

Key differences:
- `detailMessage` → `message` (from `getMessage()`) and `localizedMessage` (from `getLocalizedMessage()`)
- `suppressedExceptions` → `suppressed` (from `getSuppressed()`)
- Property ordering follows getter discovery order

### Impact on approved files

If you switch an existing project from safe/force to fallback mode, **approved files for types in locked modules will need to be regenerated**. Your own POJO types (on the classpath) are unaffected — they serialize identically in all modes.

To regenerate:
```shell
mvn test -DapprovalcrestReflection=fallback -DfileMatcherUpdateInPlace=true
```

## Setting the Mode

### System property

The mode is controlled by the `approvalcrestReflection` system property (alias: `aCReflection`):

```shell
# Command line
mvn test -DapprovalcrestReflection=safe      # explicit default
mvn test -DapprovalcrestReflection=force     # requires --add-opens
mvn test -DapprovalcrestReflection=fallback  # getter-based

# Short alias
mvn test -DaCReflection=fallback
```

### Programmatic (before any assertion)

```java
System.setProperty("approvalcrestReflection", "fallback");
```

Note: the mode is read once during class loading of `ReflectUtil`. Set it before any approvalcrest matcher is created.

### Maven Surefire

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-DapprovalcrestReflection=fallback</argLine>
    </configuration>
</plugin>
```

### Gradle

```groovy
test {
    systemProperty 'approvalcrestReflection', 'fallback'
}
```

## JDK Compatibility Matrix

| JDK | Safe mode | Force mode | Fallback mode |
|---|---|---|---|
| 8 | ✅ Field-based (no modules) | ✅ Field-based | ✅ Field-based (no locked modules) |
| 9–15 | ✅ Field-based (opens modules) | ✅ Field-based (with --add-opens) | ✅ Getter-based for locked types |
| 16 | ✅ Field-based (opens modules) | ✅ Field-based (with --add-opens) | ✅ Getter-based for locked types |
| 17–25 | ✅ Field-based (opens modules) | ✅ Field-based (with --add-opens) | ✅ Getter-based for locked types |
| 26+ | ⚠️ Getter-based for locked types (see below) | ✅ Field-based (with --add-opens) | ✅ Getter-based for locked types |

Note: JDK 16 is where strong encapsulation became the default (`--illegal-access=deny`). On JDK 9–15 the default was `--illegal-access=permit`, which allowed reflective access with a warning. Safe mode works identically on JDK 8 through 25.

JDK 26 is the break. [JEP 471](https://openjdk.org/jeps/471) deprecated the `sun.misc.Unsafe` memory-access methods for removal, [JEP 498](https://openjdk.org/jeps/498) made JDK 24 warn on first use (the `WARNING: A terminally deprecated method in sun.misc.Unsafe has been called` line you may already see in your build logs), and from JDK 26 they throw by default. Safe mode can no longer open modules there, so locked-module types switch to getter-based output. To keep field-based output on JDK 26, either run with `--sun-misc-unsafe-memory-access=allow` or switch to force mode with the relevant `--add-opens` flags.

## Migration Guide

### Removing `--add-opens` (recommended)

If you previously used `--add-opens` flags:

1. Remove all `--add-opens` flags from your Maven/Gradle/IDE configuration.
2. Remove any `-DapprovalcrestReflection=force` if set.
3. Run your tests — they should pass unchanged.

The default safe mode opens the same modules automatically. Your approved files remain valid because the output format is identical.

### Switching from safe to fallback

1. Set `-DapprovalcrestReflection=fallback`.
2. Regenerate affected approved files: `mvn test -DapprovalcrestReflection=fallback -DfileMatcherUpdateInPlace=true`.
3. Review and commit the updated approved files.

Only approved files for types in JDK locked modules will change. Your own POJO types produce identical JSON in all modes.

## FAQ

### Will switching to safe mode break my existing approved files?

No. Safe mode produces identical field-based JSON to what force mode (with `--add-opens`) produces. Your approved files will match without changes.

### What happens on JDK 26, when Unsafe stops working?

Safe mode gracefully degrades to getter-based serialization for locked-module types, exactly as fallback mode does. Your tests won't crash — but approved files for locked-module types will need regenerating, as the output format changes from field-based to getter-based.

Note that on JDK 26 `sun.misc.Unsafe` is disabled rather than removed: the class still loads and its methods still resolve, but calling one throws `UnsupportedOperationException`. Approvalcrest therefore makes a probe call at start-up and treats a failing probe as "no Unsafe", rather than assuming that the class being present means it works. The same degradation happens when the class is removed for real in a later release.

To postpone the change, run with `--sun-misc-unsafe-memory-access=allow` (available from JDK 24) or switch to force mode with `--add-opens`. Both keep field-based output and leave your approved files untouched.

### Do circular references still work in all modes?

Yes. Circular reference detection works identically in all modes. The circular reference marker format (`"0x1"`, `"0x2"`, etc.) is the same regardless of the field access mode.

### Can I mix modes in the same JVM?

No. The mode is set once at class-loading time and applies to all approvalcrest assertions in that JVM. Use separate Surefire executions if you need to test multiple modes.

### My POJO has no public getters — will fallback mode work?

For your own types on the classpath (unnamed module): yes, they are always accessed via field reflection regardless of mode. Fallback mode only uses getters for types in **locked JDK modules**.

### Does this affect `sameBeanAs` or only `sameJsonAsApproved`?

All matchers use the same serialization pipeline. The mode affects `sameBeanAs`, `sameJsonAsApproved`, and `sameContentAsApproved` equally.
