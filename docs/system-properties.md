# System Properties Reference

All boolean system properties for approvalcrest can be set via `-Dproperty=value` on the command line or via `System.setProperty(...)` in test setup.

Accepted truthy values (case-insensitive): `true`, `t`, `1`, `yes`, `y`.  
All other values are treated as `false`.

Each property also accepts a **short alias** — a compact name formed by taking the initial of each camelCase word except the last, then appending the last word in full (first word contributes a lowercase initial, subsequent words an uppercase initial).

Setting both the canonical name and the alias to the **same value** is allowed.  
Setting them to **different values** throws `IllegalStateException` at test startup.

## Property table

| Property | Alias | Default | Description | Docs |
|---|---|---|---|---|
| `approvalcrestReflection` | `aCReflection` | `safe` | Field access mode: `safe` (auto-opens modules), `force` (requires --add-opens), or `fallback` (getter-based). | [field-access-modes](field-access-modes.md) |
| `fileMatcherUpdateInPlace` | `fMUInPlace` | `false` | Re-write approved files in-place from actual output instead of failing. The legacy name `jsonMatcherUpdateInPlace` is also accepted. | [same-json-as-approved](same-json-as-approved.md) |
| `fileMatcherPassOnCreate` | `fMPOnCreate` | `false` | Pass the test (instead of failing) when no approved file exists yet. | [same-json-as-approved](same-json-as-approved.md) |
| `useApprovedDirectory` | `uADirectory` | `false` | Store approved files in a dedicated `approved/` directory tree instead of alongside test sources. | [file-control](file-control.md) |
| `fileMatcherSharedDir` | `fmSharedDir` | `src/test/java/shared-approvals` | Path (relative to project root) of the shared canonical files directory used by the pointer-file system. | [shared-approvals](shared-approvals.md) |
| `fileMatcherSharedEnabled` | `fmSharedEnabled` | `false` | Activates write-side integration: new approved files and in-place updates check for matching canonicals and write pointer files instead of duplicate content. | [shared-approvals](shared-approvals.md) |
| `fileMatcherSharedDirBucketDepth` | `fmSharedDirBucketDepth` | `2` | Number of leading hash characters used as the bucket subdirectory name inside the shared directory (1–6). | [shared-approvals](shared-approvals.md) |
| `sortInputFile` | `sIFile` | `false` | Sort the input before writing it to the approved file. | [sorting](sorting.md) |
| `fileMatcherStrictFileMatching` | `fMStrictMatching` | `true` | Ignore stripping applies to the actual side only; approved files must not contain the ignored field's value. Disable to restore two-sided ignore behaviour. | [ignoring-fields](ignoring-fields.md) |
| `fileMatcherMachineReadable` | `fMMReadable`, `fmAI` | `false` | Replace human-readable failure messages with structured, machine-actionable output for AI agents and CI pipelines. | [file-control](file-control.md) |
| `beanMatcherSkipClassComparison` | `bMSCComparison` | `false` | Skip the runtime-type check in `sameBeanAs`; allows comparing objects of different but structurally compatible classes. | [same-bean-as](same-bean-as.md) |
| `approvalcrestSerializeNulls` | `aSerNulls` | `true` | Include null-valued fields in Gson serialization. Disabling reverts to the pre-1.0.2 behaviour where null fields were silently omitted. | [ignoring-fields](ignoring-fields.md) |

## Example usage

```shell
# Update approved files in-place using the short alias
mvn test -DfMUInPlace=true

# Disable null serialization globally using the short alias
mvn test -DaSerNulls=false

# Enable machine-readable output for AI agents
mvn test -DfmAI=true
```
