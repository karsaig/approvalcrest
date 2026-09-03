<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=308d73c7-f1a0-403c-8df9-56ae120707f0" />

Changelog
===========

Version 1.5.1 - Unreleased
-----
- A class that inherits fields from a locked JDK module — most commonly your own exception class, which extends `RuntimeException` and so carries `Throwable`'s private fields — no longer fails to serialise on a JDK where those fields cannot be reached. It threw `JsonIOException: Failed making field 'java.lang.Throwable#detailMessage' accessible` instead of falling back to getter-based output the way a locked type itself does, because the class's own module is not locked, so it was never sent to the getter-based path. This applies only where the inherited fields are ones that get serialised: a class extending, say, `java.util.EventObject`, whose only field is transient and was never read, keeps its existing output. Only affects runs where no module can be opened at all, which today means JDK 26 or `fallback` mode; every other run is unchanged.
- **[breaking]** On JDK 26 and later, types from locked JDK modules (`java.time.*`, `java.lang.*`, `java.math.*`, etc.) no longer come out as an empty `{}`. JDK 26 disables `sun.misc.Unsafe` rather than removing it, so the default safe mode kept using it, read nothing, and produced an object with no fields — an approved file could match `{}` and report a pass having compared nothing. Safe mode now checks that Unsafe can still read a field before relying on it, and where it cannot, serialises those types from their public getters instead. JDK 8 to 25 are unaffected and their approved files are unchanged. **Migration:** on JDK 26, either attach the new agent described below to keep the old field-based output, or re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate approved files for locked-module types.
- Added `approvalcrest-agent`, an optional java agent that keeps output identical on JDK 26 with no `--add-opens` and no approved files to regenerate. Add it as a test dependency and pass one JVM argument. Output is unchanged on JDK 8 to 25 whether or not it is attached, so it can be adopted before JDK 26 arrives. Force mode still requires `--add-opens`. See [field-access-modes](docs/field-access-modes.md#the-agent).
- A `*` segment no longer matches fields that are absent from the serialised output — `static`, `transient` and compiler-generated ones. It used to read the other constants off an enum, and follow a non-static inner class's hidden reference to its enclosing instance, so an assertion could pass on data outside the object being compared. It also failed beside an empty collection or an unreachable sibling, reporting a path the caller never wrote.
- A collection matcher now behaves the same whether the field is named by a path or by a name pattern. `withMatcher(...)` handed the matcher the raw serialised form of a collection-valued field, so `.with("tags", hasSize(2))` passed while `.withMatcher(is("tags"), hasSize(2))` failed on the same field. `iterableWithSize` worked in both.
- A `*` path segment now means "every named child at this position" — a map key, an object property or a bean field — so `.with("ordersByRef.*.trackingCode", notNullValue())` and `.ignoring("ordersByRef.*.generatedId")` apply to every entry of a map. Previously each key had to be named individually. As the *last* segment `*` keeps its old meaning of a key literally named `*`. Collections and arrays need no wildcard, since they are already traversed: `orders.trackingCode` already means "in every order". See [custom-matching](docs/custom-matching.md) and [ignoring-fields](docs/ignoring-fields.md).
- Ignoring a wildcard path can remove the container itself. If the ignored field is the only one on each map value, every value empties, so each entry goes, then the array, then the map field — keys included. A named key scopes that to one entry, whereas `*` applies to every entry at once, so the whole field usually disappears. Check what the comparison still covers rather than reading a pass as proof the map was compared.
- `sortField(...)` accepts the same `*` segment, so a sort path can reach the collection under every map entry instead of naming each key. A wildcard and a literal key at the same level both apply rather than one overriding the other, and a `SortField`'s ignored sub-paths accept `*` too, so a field can be kept out of the sort key under every child. Previously a `*` there was read as a literal key name, so nothing matched and the field silently stayed in the key, recording an order nobody asked for in the approved file. See [sorting](docs/sorting.md).
- `ignoringElementsWhere(...)` accepts it too, which is what makes a `Map<String, List<T>>` filterable: `ordersByRef.*.system` filters the list under each entry, where the wildcard-free `ordersByRef.system` filters the outer entry array instead and matches nothing. Naming a key reaches the same list as the wildcard does, for that one entry.
- A field path can now cross a `Set`- or `Map`-typed field, so `.with("ordersByRef.A-1.trackingCode", …)` and `.with("tagSet.name", …)` work. Both failed with `… does not exist` however well-formed they were, because such a field is written under a prefixed key that the path resolver did not recognise. `.ignoring(...)` and `.sortField(...)` already worked. Omitting a map key still does not reach the values — a map is traversed by key, not fanned out over. See [custom-matching](docs/custom-matching.md).
- Ignoring a field inside a circular-reference graph now reaches every object in that graph, not only the first one carrying it. The two sides ended up filtered differently, so the assertion failed on unrelated data and the message pointed at the wrong field. Only graphs holding more than one object with the ignored field were affected.
- A negated collection matcher can now fail. `.with("orders", not(empty()))` passed on an empty collection, as did `not(hasSize(n))`, `not(hasItem(x))`, `not(contains(...))` and `not(containsInAnyOrder(...))` — assertions that could never fail whatever the data held. They now report their mismatch. One combination is not fixed by this and cannot be — see the next entry.
- Watch out for one combination that still cannot fail: `not(hasItem(x))`, `not(contains(...))` and `not(containsInAnyOrder(...))` where the input is a raw JSON string, the elements are objects rather than scalars, and the matcher is written against the element's own class. Compare the object rather than a JSON string, or write the matcher against the JSON shape. Over scalar elements all of them work on both input forms. See [custom-matching](docs/custom-matching.md).
- Documented what `.with(path, matcher)` does when the path names a collection, map or array, and which Hamcrest matchers hold for each input form. `sameJsonAsApproved` accepts a raw JSON string as well as an object, and the two forms differ in what they can assert about a collection's elements. See [custom-matching](docs/custom-matching.md).
- **[breaking]** A map holding two entries whose key and value both serialise identically now writes each entry as its own key/value pair. The two entries used to come out as two copies of one four-element array, misrepresenting the map. Only maps whose keys are not primitives, `String`s or enums are affected, and only where two entries collide; every other map is written byte-for-byte as before. An approved file recording the old shape will now fail. **Migration:** re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate the affected approved files.
- **[breaking]** Ignoring everything under a `Set`- or `Map`-typed field now removes the field, instead of leaving `"ordersByRef": []` behind in the approved file. An ordinary field emptied the same way already disappeared. Only fields that ignoring empties completely are affected. Strict matching is on by default, so an existing approved file still holding the empty collection will now fail. **Migration:** re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate the affected approved files.
- **[breaking]** Ignoring a field inside an object held in a collection no longer removes the other values in that collection. `.ignoring("list.a")` over `{"list":[{"a":1},"keep-me",42]}` used to leave the collection empty and then drop the field, taking two values the rule never mentioned — and because both sides were filtered alike, the assertion still passed. Watch out for one shape that is still emptied, because it cannot be told apart from a map entry: a collection of exactly two elements, itself held inside another collection, reduced to plain values. A collection held by a field is safe, whatever its length. An approved file generated while those values were dropped will now fail. **Migration:** re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate the affected approved files.
- **[breaking]** `sortType(...)` now reaches a collection whose field is declared as a subclass, such as `class OrderList extends ArrayList<Order>`. Naming `Order` had no effect on such a field before — it was skipped and compared in whatever order it arrived in — so the order recorded for it changes. Sorting is still only applied to the types you name: a collection field is untouched unless `sortType` names its element type. **Migration:** re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate approved files holding one. See [sorting](docs/sorting.md).
- `withMatcher(Matcher<String>, ...)` now reaches a `Set`- or `Map`-typed field. Such a field is held under an internal name, and the pattern was tested against that rather than the field's own, so it never matched — and a pattern matching no field passes, which made `.withMatcher(is("tagSet"), hasSize(2))` an assertion that could not fail whatever the data held. It now behaves as the path-based `.with("tagSet", ...)` always did. Fields of any other type were already reached. See [custom-matching](docs/custom-matching.md).
- `.ignoring(Matcher<String>)` no longer takes away a `Set`- or `Map`-typed field for a pattern that matches only the internal name it is held under — `startsWith("!")` and the like — and a field removed or aliased inside one is now reported in machine-readable output under the name it was declared with, rather than with that internal name in its path. Patterns written against a field's own name are unaffected.
- Machine-readable output now lists an ignore rule that took effect below the top of its path, and lists it once rather than twice. `.ignoring("a.b.c")` went unrecorded whenever `a.b` kept its other fields, and a rule matching both the actual value and the approved content was counted twice. Element indexes, and parents removed for becoming empty, are still listed per occurrence. This affects the report only; what gets compared is unchanged.
- Documented that a map key spelled like a circular-reference wrapper key — `0x` followed by lowercase hex — is skipped during path navigation as if it were one, so a path that omits such a key still reaches through it, where omitting any other key matches nothing. See [ignoring-fields](docs/ignoring-fields.md).
- **[breaking]** A map whose keys are not primitives, `String`s or enums now records which half of an entry was the key. Such a map is written as `[key, value]` pairs, and the pair used to be reordered by its content — so `{a: z}` and `{z: a}` wrote the same bytes and an approved file for one matched the other. The pair now keeps its order at every level the declared type describes — a map inside another map, inside a `Set`, or under a list of lists, subclasses such as `EnumMap` or your own included wherever the declaration names the value type, and at the root, where the object itself is inspected instead; entries and a collection-valued half are still sorted. Entry order can change as a result, so regenerate rather than hand-edit. With `sameBeanAs` this applies unconditionally — there is no setting to turn it off and no file to regenerate: an assertion that passed only because a map and its transpose compared equal now fails and has to be rewritten. For a file matcher it applies under strict file matching, which is the default. **Migration:** re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate approved files holding such a map. See [sorting](docs/sorting.md).
- Corrected a wrong entry in the released 1.0.1 notes. It claimed that sorting an array-of-arrays preserves inner array order; inner arrays are sorted together with the outer one, and no release has behaved otherwise. The one exception is a complex-key map's `[key, value]` pair, which is two positions rather than a collection and keeps its order at any depth — see the entry above. The entry is annotated in place rather than rewritten, so anyone who followed its migration advice can see that it was never needed.
- A map with a `null` key no longer aborts the comparison. It threw a `NullPointerException` before any file was written, and whether it threw at all depended on the map's other keys. The key is now recorded as the member name `"null"`, in a map whose other keys are primitives, `String`s or enums; where any other key forces the `[key, value]` form, it is written as a bare JSON null in the entry, as any key is there. A `null` key and a `String` key of `"null"` are then indistinguishable: both entries are recorded, but a comparison between one map and the other reports a match. See [sorting](docs/sorting.md).

Version 1.5.0 - 2026/07/29
-----

- `SortField.ignoring(...)` now returns a new `SortField` instead of mutating the receiver. Deriving two configurations from one base previously produced a single object carrying both sets of ignores, so a `SortField` held in shared configuration accumulated every caller's paths, order-dependently, and was unsafe under parallel execution. Chaining (`SortField.of("x").ignoring("a").ignoring("b")`) is unaffected; code that called `ignoring(...)` for its side effect and discarded the result must now use the returned instance.
- `assertThrows` no longer treats a skipped test or a JVM-level error as the exception under test. An assumption failure (`TestAbortedException`, `AssumptionViolatedException`, TestNG's `SkipException`) is now rethrown so the test is reported as skipped rather than compared against the expected exception, and a `VirtualMachineError` is rethrown as-is instead of being matched or wrapped. Asserting that a matcher fails still works: `AssertionError` is deliberately still matched.
- The error reported when an in-place approved-file overwrite fails now names the file it could not write. It previously interpolated the object under comparison instead, so the message identified no file and put the whole serialised content into the exception, the log and the CI output.
- `ignoringElementsWhere` now rejects a null value or matcher where the rule is declared, naming the path, instead of failing with an unattributed `NullPointerException` later during comparison.
- Reflection failures now keep their underlying cause. An `IllegalAccessException`, `InvocationTargetException` or `SecurityException` previously collapsed into the same "Cannot access field" message, which made the accompanying `--add-opens` guidance impossible to check.
- The Kotlin matchers now honour `fileMatcherSourceRoot` (alias `fmSourceRoot`), so a Kotlin project whose tests live outside `src/test/kotlin` can configure where approved files are stored. The property was previously ignored there, leaving no way to change it. Only the default differs per framework — `src/test/kotlin` for the Kotlin matchers, `src/test/java` elsewhere — so projects that do not set the property are unaffected. Note that setting it in a project containing both Java and Kotlin tests now moves both. See [file-control](docs/file-control.md).
- Fixed a `StackOverflowError` when comparing a graph of `Throwable`s that reference each other in a cycle. Only throwables were affected; cyclic ordinary beans were already handled correctly.
- `fileMatcherSharedEnabled` (alias `fmSharedEnabled`) now accepts a file type as well as a boolean, so the write-side shared-approval integration can be limited to `json` or `content` rather than always covering both. `true` and `false` keep their existing meanings — `true` is equivalent to `all` — so no existing configuration changes behaviour. This pairs with the matching `--types` option on the dedup tool. See [shared-approvals](docs/shared-approvals.md).
- `approvalcrest:dedup` and `approvalcrest:reinstate` can now be limited to one approved file type with `-DfileMatcherSharedTypes=json|content` (or `--types` on the standalone CLI), so a project can consolidate its JSON approvals without touching its `.content` ones. The default is both types, so existing runs are unchanged. Only conversion and canonical deletion are restricted; whether a canonical is still referenced is always decided across every type, so a single-type run cannot strand the other type's pointers. See [shared-approvals](docs/shared-approvals.md).
- Test methods inherited from an **abstract** base class now fail with a clear error instead of silently sharing one approved file. Every subclass resolved to the same file and overwrote the others, because the class a method is declared in is all that the stack trace — or TestNG's injected `Method` — can report. Pass `TestInfo` (JUnit 5), use `Junit4DesciptionWatcher` (JUnit 4), or supply a custom `TestMetaInformation` to get one approved file per subclass. Concrete test classes are unaffected.
- **Behaviour change.** `Set` elements that serialise to the same JSON are no longer collapsed into a single entry. Sets are sorted by JSON representation to keep output stable, and that sort was previously also deduplicating, so elements that are not `equals()` but serialise identically were dropped — which happens with classes that do not override `equals()`, or whose distinguishing field is not serialised. A set that lost or gained such an element therefore serialised identically either way and the difference could not fail a test. Approved files containing such a set will now differ and need re-approving; the previously recorded content was incomplete. Set `-DapprovalcrestLegacySetCollapse=true` (alias `aLSCollapse`) to restore the old behaviour while migrating. See [supported-types](docs/supported-types.md).
- Object-graph reference ids containing hexadecimal letters are now recognised everywhere they are parsed. From the tenth object in a graph onwards an id contains the letters `a`-`f`, which the throwable adapter did not accept.
- `mvn approvalcrest:dedup` no longer deletes shared canonical files that pointer files outside the scanned directory still reference. Which files get converted is still controlled by the scan directory, but whether a canonical is still in use is now determined across the whole project. Previously, narrowing the scan (or running the goal per module against a shared directory covering several) deleted those canonicals and left the pointers resolving to content that existed nowhere on disk.
- `mvn approvalcrest:reinstate` and `--reinstate` now support `--dry-run` / `-DdryRun=true`. The flag was accepted on the command line but ignored, so a run intended as a preview rewrote every pointer and deleted every canonical.
- `approvalcrest:reinstate` now only deletes canonicals that nothing references any more, instead of clearing the shared directory unconditionally.
- Both dedup goals now refuse to run when the shared approvals directory contains the scan directory, rather than treating every approved file as a canonical and deleting it. This was reachable by a single mistyped `--shared-dir`, because the default shared directory sits inside the default scan directory.
- Tests declared with package-private methods are now supported. JUnit Jupiter and TestNG both allow non-public test methods, but approvalcrest looked the running test up among public methods only, so such a test failed with "Cannot determine test method" instead of resolving its approved file. A test method that shares its name with an overload is now also identified reliably.
- Approved files are now read correctly when their comment header ends with a CRLF rather than a LF. Headers are written with a LF, so a Windows checkout with `core.autocrlf=true` previously failed to strip the header (the comment ended up compared as part of the content) and pointer files were not recognised as pointers at all.
- `fileMatcherSharedDirBucketDepth` (alias `fmSharedDirBucketDepth`) is now validated against its documented 1–6 range and reports a configuration error naming the property. Out-of-range values previously failed with an unrelated `StringIndexOutOfBoundsException` from inside the matcher. See [shared-approvals](docs/shared-approvals.md).

Version 1.4.2 - 2026/07/28
-----

- Added the `fileMatcherSourceRoot` property (alias `fmSourceRoot`) to configure the test source directory approved files are stored in. Defaults to `src/test/java`; set it to e.g. `src/it/java` or `src/test/kotlin` when tests live outside the standard directory.

Version 1.4.1 - 2026/07/12
-----

- Added `ignoringElementsWhere` to remove array elements from the comparison based on the value of a nested field. The path points at a field within each element; the innermost array on the path is filtered, and every element whose leaf field value satisfies the given matcher (or equals the given String) is removed before comparison. Intermediate collections are traversed transparently, so a path such as `entry.resource.meta.tag.system` filters the `tag` array of every `entry`. Works with `sameBeanAs` and `sameJsonAsApproved`. See [ignoring-fields](docs/ignoring-fields.md).

Version 1.4.0 - 2026/06/29
-----

- Added pointer-file support for approved files and a new `approvalcrest-dedup` module. An approved file whose post-header content is `/*pointer:relative/path*/` is transparently followed at read time with no configuration required; the test comment header is preserved. In-place updates (`-DfMUInPlace=true`) on a pointer file detach the test from the shared canonical rather than modifying it. Enabling `-DfmSharedEnabled=true` activates write-side integration: new files whose content matches an existing canonical get a pointer `-not-approved` for the developer to approve, and in-place updates re-point to a matching canonical when one exists. The configurable shared directory (`-DfmSharedDir=`, default `src/test/java/shared-approvals`) is the authoritative location for canonical files. The `approvalcrest-dedup` module provides `mvn approvalcrest:dedup` (group duplicates → create canonicals → write pointers, GC orphans) and `mvn approvalcrest:reinstate` (replace all pointers with standalone approved files and clear the shared directory). See [shared-approvals](docs/shared-approvals.md).
- Removed the previously declared but unused `buildIndex` / `bFIndex` flags from `FileMatcherConfig`.
- Fixed `JsonMatcher` silently passing in update-in-place mode when a custom matcher (`.with(fieldPath, matcher)`) fails. The approved file was rewritten and the assertion passed, so the same custom matcher failed again on the next normal run, apparently out of nowhere. The file is now left untouched and the assertion fails.

Version 1.3.4 - 2026/06/24
-----

- Fixed path-based custom matchers (`.with("fieldPath", matcher)`) failing with `IllegalArgumentException: <field> does not exist` on a `Map<String, Object>`, a `List<Map<String, Object>>` or a JSON string input. A path through a collection now collects values from the elements that carry the field, and fails only when none of them does; previously a single element without it failed the whole path, which is the normal case for a map, where each key lives in its own element. Heterogeneous collections behave the same way.

Version 1.3.3 - 2026/06/17
-----

- Fixed `withAlias` / `withAliasMap` being applied to the approved-file content in strict mode (`fileMatcherStrictFileMatching=true`, the default). All other transformations — `.ignoring()`, `.with()`, pattern-based sorting — were already correctly skipped on the approved side in strict mode; aliases were the only exception. After this fix, aliases are applied to the actual side only in strict mode, consistent with the strict-mode contract that the approved file is used as-is. An approved file that still contains the raw (non-aliased) value is now correctly detected as stale and causes the assertion to fail, prompting regeneration. Approved files that already contain the aliased form (the normal case) are unaffected — no re-generation needed.
- Added AI tip to the "not approved file created" failure message: the human-readable message now ends with `[AI tip] Re-run with fmAI=true for the absolute path of the file that needs to be renamed.` Previously only content-mismatch failures carried the tip. Also fixed the machine-readable `action` field for `NEW_FILE` failures: it previously incorrectly suggested `fMUInPlace=true`, which throws when no approved file exists yet; the correct action is to rename/copy the generated file.
- Added AI tip to the type-mismatch failure message (`sameBeanAs` with incompatible types): the non-machine-readable branch now ends with the standard `[AI tip] Re-run with fmAI=true for structured, machine-readable output.` tip, consistent with content-mismatch failures.
- Fixed `JsonMatcher.describeTo()` applying transformations (aliases, ignored-field removal, custom sorting) to the approved-file content in strict mode, inconsistently with `doMatches()`. In strict mode the approved file is used as-is on the expected side of the comparison, but `describeTo()` was silently aliasing/stripping the same content, causing the "Expected:" section of the Hamcrest failure message to show different values than the diff. For normal (up-to-date) approved files this was a no-op difference; for stale approved files the failure message was contradictory.

Version 1.3.2 - 2026/06/03
-----

- Machine-readable JSON output is now emitted in compact form (no pretty-printing) to minimise token usage for AI consumers. The `expected` and `actual` field values, as well as the outer JSON wrapper, are all serialised without whitespace across all failure types (`MISMATCH`, `NEW_FILE`, `TYPE_MISMATCH`).
- Fixed path-based operations (`.ignoring("path")`, `.with("path", matcher)`, `.sortField("path")`, `SortField.ignoring("path")`) failing when the path traverses through a type detected as having a circular reference. Such types are written wrapped in synthetic `0xN` envelope keys, which broke path resolution. These envelope keys are now transparently skipped during path navigation — the user-specified path works identically whether the target type has circular references or not.

Version 1.3.1 - 2026/06/01
-----

- Fixed `Optional<Interface>` and `Optional<AbstractClass>` being written as an empty `{}`. Where the declared type parameter is an interface or abstract class, the runtime type is used instead. See [supported-types](docs/supported-types.md).
- Fixed polymorphic base class handling in `Optional`: `Optional<Animal>` holding a `Dog` instance now serializes all `Dog`-specific fields. See [supported-types](docs/supported-types.md).
- Added custom serializers for `OptionalInt`, `OptionalLong`, and `OptionalDouble`. These primitive-specialized Optional types are now serialized with the same `{"value": N}` / `{}` format as `java.util.Optional`. See [supported-types](docs/supported-types.md).
- Added `GsonConfiguration.addTypeToSkipInFallbackFactories(Class<?>)`: allows users to prevent `UnsafeFieldTypeAdapterFactory` and `GetterBasedTypeAdapterFactory` from claiming specific types, so that custom `TypeAdapter`/`TypeAdapterFactory` registrations (e.g. for Vavr's `Option`, `Either`) take precedence. See [supported-types](docs/supported-types.md).
- Added documentation: migration guide for path handling ([file-control](docs/file-control.md)) and IDE run template best practices ([best-practices](docs/best-practices.md)).

Version 1.3.0 - 2026/06/01
-----

- Machine-readable output is now structured JSON. Failure messages emitted with `-DfileMatcherMachineReadable=true` (or `withMachineReadableOutput()`) are valid JSON objects containing `failureType`, `test`, `approvedFile`/`approveTo`, `expected`, `actual`, plus metadata arrays `ignoredFields`, `aliasedFields`, and `sortedFields` that document what transformations were applied. See [file-control](docs/file-control.md).
- Added `fmAI` as an additional short alias for the machine-readable output system property (`-DfmAI=true`). The passive AI discovery tip appended to non-machine-readable failures now references `fmAI` for brevity. The original alias `fMMReadable` remains valid. See [system-properties](docs/system-properties.md).
- Records and sealed classes confirmed working on JDK 17 and above.
- Fixed generic type serialization bug with Immutables `@Gson.TypeAdapters`: types registered via `GsonBuilder.registerTypeAdapterFactory` no longer cause `IllegalStateException` during recursive adapter resolution.
- `LenientTypeAdapterFactory` now logs the full exception (including stack trace) when catching `IllegalStateException`, and deduplicates repeated warnings for the same type to reduce log noise.

Version 1.2.0 - 2026/05/30
-----

- Eliminated the `--add-opens` JVM flag requirement. The library now opens modules programmatically at runtime using a three-tier field access strategy: force-open the module → getter-based fallback → direct field reflection. No JVM flags are needed on JDK 9+. See [field-access-modes](docs/field-access-modes.md).
- Added `approvalcrest-testng` module: TestNG 6+ adapter with full feature parity — `sameBeanAs`, `sameJsonAsApproved`, `sameContentAsApproved`, DataProvider support, and optional `Method` injection (TestNG 6.14.2+). See [testng](docs/testng.md).

Version 1.1.0 - 2026/05/29
-----

- **[breaking]** Gson now serializes null fields by default. Previously, null-valued fields were silently omitted from JSON serialization, which caused `.ignoring("field")` to silently fail when the field value was null inside a collection element — the field was stripped before ignore logic ran, so the ignored element was never removed from the collection. **Migration:** re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate approved files that now include null fields. To restore the old behaviour globally set `-DapprovalcrestSerializeNulls=false`. To restore it per-matcher call `.withoutSerializingNulls()` on the matcher. See [ignoring-fields](docs/ignoring-fields.md).
- Added short alias names for all boolean system properties. Each property can now also be set using a compact alias, e.g. `-DfMUInPlace=true` instead of `-DfileMatcherUpdateInPlace=true`. Setting both the canonical name and an alias to the same value is allowed; setting them to conflicting values throws `IllegalStateException` at startup. See [system-properties](docs/system-properties.md) for the full alias table.
- Improved machine-readable output mode (`-DfileMatcherMachineReadable=true` / `withMachineReadableOutput()`). Failure messages now include structured headers: `FAILURE_TYPE: MISMATCH|NEW_FILE|TYPE_MISMATCH`, `TEST: ClassName#methodName`, `APPROVED_FILE:` or `APPROVE_TO:` (absolute path), and `ACTION:` with the exact property to set. In non-machine-readable mode a passive discovery tip is appended at the end of every failure message so AI agents can discover the mode even in default output. See [file-control](docs/file-control.md).
- Fixed missing Kotlin extension functions on `DiagnosingCustomisableMatcher`: `withMachineReadableOutput()`, `withoutSerializingNulls()`, and `sortType()` (added in 1.0.1) were absent from the Kotlin extension functions file, preventing their use in arbitrarily chained Kotlin expressions. Same extensions added for `JsonMatcher`; `withMachineReadableOutput()` added for `ContentMatcher`.
- Added `Matchers.sameBeanAsType<T>(expected: Any)` Kotlin-only factory: creates a `DiagnosingCustomisableMatcher<T>` from any object without requiring an `as Any` cast. Intended for cross-type structural comparisons where the actual and expected have different class types; pair with `.skipClassComparison()`: `sameBeanAsType<Any>(expected).skipClassComparison()` instead of `sameBeanAs(expected as Any).skipClassComparison()`.

Version 1.0.1 - 2026/05/28
-----

- Added `approvalcrest-junit-jupiter-kotlin` module: Kotlin-friendly wrappers and matchers for JUnit Jupiter. Updated Kotlin to 2.1.21 and Dokka to 1.9.20. See [kotlin](docs/kotlin.md).
- Added Kotlin extension functions for `DiagnosingCustomisableMatcher`, `JsonMatcher`, and `ContentMatcher` in the `approvalcrest-junit-jupiter-kotlin` module. These work around Kotlin's type-inference limitation with F-bounded (recursive) generics ([KT-5464](https://youtrack.jetbrains.com/issue/KT-5464)): chained calls such as `sameBeanAs(expected).with("field", myMatcher)`, `sameJsonAsApproved<T>().ignoring("field").withUniqueId("id")`, and `sameContentAsApproved<T>().withUniqueId("id")` all now compile cleanly in Kotlin.
- Added `sortType(Class<?>...)` fluent method on `sameBeanAs` and `sameJsonAsApproved`. Collections and arrays whose element type matches one of the specified classes are sorted automatically during comparison and file creation — no `sortField` path needed. Works the same way as automatic Set sorting already did for unordered collections. See [sorting](docs/sorting.md).
- Added `withMachineReadableOutput()` fluent API and `-DfileMatcherMachineReadable=true` system property. When active, assertion failure messages are replaced with structured, machine-actionable text intended for AI agents and CI pipelines. File matchers emit the absolute path to the approved file plus a full `=== ACTUAL (full) === / === END ACTUAL ===` block. Bean matchers emit both expected and actual inline in `=== EXPECTED (full) === / === ACTUAL (full) ===` blocks. IDE diff view is preserved — `getExpected()`/`getActual()` on the thrown exception remain populated. See [file-control](docs/file-control.md).
- Fixed JUnit 4 `ComparisonFailure.getMessage()` being polluted by `ComparisonCompactor` when `withMachineReadableOutput()` is active. The structured text is now preserved, and `getExpected()`/`getActual()` — and therefore IDE diff view — still work.
- `approvalcrest-junit-jupiter` is compatible with JUnit Platform 6 (verified against 6.0.3) on Java 17, 21 and 25. See [junit5-jupiter](docs/junit5-jupiter.md).
- **[breaking]** Added strict matching mode (`fileMatcherStrictFileMatching` system property, on by default): `ignoring()` strips fields from the actual side only, so approved files that contain the value of an ignored field will now fail. **Migration:** re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate approved files without the ignored fields. To restore the old two-sided behaviour globally set `-DfileMatcherStrictFileMatching=false`. In tests the config object uses the last boolean: `new FileMatcherConfig(false, false, false, false, false, /*strict=*/false)`. See [ignoring-fields](docs/ignoring-fields.md).
- **[breaking]** Added type comparison to `sameBeanAs`: fails with a clear message when actual and expected have incompatible runtime types. **Migration:** add `.skipClassComparison()` to the matcher: `assertThat(actual, sameBeanAs(expected).skipClassComparison())`, or set `-DbeanMatcherSkipClassComparison=true` to suppress globally. See [same-bean-as](docs/same-bean-as.md).
- Added `withAliasMap(AliasMap)` / `withAlias(value, alias)` / `withAlias(field, value, alias)`: replaces volatile values (UUIDs, timestamps) with stable aliases before comparison and file creation. Aliases are applied after ignores and before sorting. See [aliasing](docs/aliasing.md).
- Added `withMatcher(Matcher<String>, Matcher<V>)`: pattern-based custom matcher that applies to all fields at any depth whose name matches the supplied `Matcher<String>`. Supported by both `sameBeanAs` and `sameJsonAsApproved`. See [custom-matching](docs/custom-matching.md).
- Custom matchers now fall back to the JSON-serialised form when the Java-bean reflection path is unavailable (e.g. JSON string input to `sameJsonAsApproved`).
- Fixed sorting of root-level `List` / array inputs via `sortField("")`.
- Fixed sort order to be bottom-up: nested arrays sorted before their parent's key is computed.
- **[breaking]** Fixed sorting of arrays-of-arrays: only the outer array is reordered; inner array element order is preserved. Previously `sortField("groups")` on a `groups: [[Z,B],[A,C]]` also sorted the inner arrays into `[[A,C],[B,Z]]`; now the inner arrays stay as-is and only the outer order changes. **Migration:** if inner array order matters, switch the inner collection type to `Set` (auto-sorted by type) or add an explicit `sortField("innerFieldName")` for the inner field. See [sorting](docs/sorting.md). **[Correction, 1.5.1]** this entry is wrong, and no release has behaved as it describes. Inner arrays are sorted together with the outer one whenever the outer array's sort is configured. The migration advice above was therefore unnecessary, though harmless — switching to a `Set` or adding an inner `sortField` both still sort.
- Fixed three bugs in sort-key filtering: complex fields not stripped, multi-level paths (e.g. `addr.city`) broken, `SortField<Matcher<String>>.ignoring()` had no effect.
- Fixed `withMatcher` pattern ignores being applied after sorting in `sameBeanAs` instead of before.
- Fixed an error when an ignore path fanned out through an array of primitives.
- Fixed ignoring all fields of a bean `Map` key: the whole entry is now removed cleanly.
- Fixed ignoring a field inside a `Map` value leaving an orphaned empty array.
- **[breaking]** `withPathName(relativeStr)` approved file location changed: was `{relativeStr}/{file}` (working-directory relative), now `{testClassPath}/{relativeStr}/{file}`. Example: `.withPathName("mydir")` previously wrote to `mydir/cd3006-approved.json`; now writes to `{testClassPath}/mydir/cd3006-approved.json`. **Migration:** move existing approved files into the `{testClassPath}` subtree. Absolute paths (e.g. `/abs/path`) are unaffected. See [file-control](docs/file-control.md).
- **[breaking]** `withRelativePathName(relStr)` approved file location changed: was `{testClassPath}/{relStr}/{file}`, now `{workingDir}/{relStr}/{classHash}/{file}` (base changed to working directory, class name hash inserted). Example: `.withRelativePathName("snapshots")` previously wrote to `{testClassPath}/snapshots/cd3006-approved.json`; now writes to `{workingDir}/snapshots/87668f/cd3006-approved.json`. **Migration:** move existing approved files to the new location. See [file-control](docs/file-control.md).
- **[breaking]** `withUniqueId(id)` when `id` starts with `-`: previously generated a double separator (`hash--myid-approved.json`), now produces `hash-myid-approved.json`. Example: `.withUniqueId("-scenario1")` previously wrote `cd3006--scenario1-approved.json`; now writes `cd3006-scenario1-approved.json`. **Migration:** rename affected approved files to remove the extra `-`. See [file-control](docs/file-control.md).
- Improved `--add-opens` missing error: reports the exact flag and shows ready-to-paste Maven/Gradle snippets.
- Added `toString()` to `Junit4TestMetaBase` and `Junit5TestMetaBase` (`TestMeta[cn=…,mn=…,cp=…,ad=…,wd=…]`).

Version 1.0.0 - 2026/05/28
-----

Broken release — artefacts published to Maven Central were incomplete. Do not use. All features are available in 1.0.1.

Version 0.62.3 - 2024/05/27
-----

- Fix to work with JDK 17 & 21
- Updated dependencies to Guava 33.2.0-jre, Gson 2.11.0, Commons-lang3 3.14.0
- Testing with newer JUnit5 5.10.2

Version 0.61.6 - 2023/08/28
-----

- Added support for directory name override
- Updated dependencies to Guava 32.1.1
- Testing with newer JUnit5 5.4.0

Version 0.61.2 - 2022/11/17
-----

- Updated dependencies to Guava 31.1, Gson 2.10, Commons-lang3 3.12.0

Version 0.61.1 - 2022/07/07
-----

- Update Gson to 2.9.0

Version 0.60.3 - 2021/04/20
-----

- Fixed handling of empty approved json file

Version 0.60.2 - 2021/04/19
-----

- Fixed bug with exception serialization
- Fixed handling of empty approved json file

Version 0.60.0 - 2021/04/18
-----

- Upgrade JUnit to latest versions and make them provided dependencies, so it is easier to use with different versions
- JUnit 5 modules require JUnit 5.7.0+ now
- **Non-backward compatible change!** Added automatic sorting of field names, so the approved files and diff view will display fields in natural order.
Without this there were changes on pull requests without any reason. Only the serialization order have changed.
Doesn't affect constructs where order matters (example: Lists). This sorting is enabled by default and will fail assertions when approved file isn't sorted. 
  Anyone wants to revert to **old behaviour**, use **"-DsortInputFile=true"**
  This was done in order to avoid above mentioned noise on pull requests, and extending the migration and adding this noise to many pull request.
  
- Added support for sorting parts of json files, so collections which aren't sorted by default, and could have caused flaky tests due to non-deterministic ordering, can now be sorted to stabilize tests. When in use the approved file also have to be sorted, but can be switched with **"-DsortInputFile=true"**
- Fixed many bugs related to not working ignores, jsonMatcher not working for String containing json correctly, same matcher for different inputs working differently, assert failures sometimes missing description and actual / expected content.
- **Non-backward compatible change!** Ignored values should no longer be visible in approved files. It is backward compatible for some of the ignores, but not all, so approved files have to updated.
- Unified how assertions for JUnit 4 and 5 work, so there shouldn't be any difference between the two.
  This means JUnit 5 assertion errors won't contain the whole actual / expected content in the descriptions, those are already in the exception supported by major IDEs.
  Description will contain the difference only.
- Added additional convenience method for some ignores
- **Non-backward compatible change!** Up until now, asserting exceptions ignored the exception type, it is now added to the serialized format and asserted. 
Stacktrace in exceptions are automatically ignored from now on, as that caused frequent test failures without ignores, forcing everyone to add ignore in many places.
- Extended support for floating point numbers.
- Fixed a bug where files and directories could have wrong permissions in some cases
- Preliminary Kotlin support.

Version 0.56.3 - 2020/09/13
-----

 - Fixed permissions on created directories and files as it had a bug which caused permission problems in some cases
 - Fixed bugs around pass on create flag 
 
Version 0.56.2 - 2020/05/30
-----

 - Fixed sameBeanAs return value

Version 0.56.1 - 2020/05/24
-----

 - Fixed OffsetTime serialization issue
 - Upgraded Guava and Gson versions

Version 0.56 - 2020/05/18
-----

 - New package for JUnit5 Jupiter matcher, so gradual migration of existing JUnit 4 projects are possible

Version 0.55.4 - 2020/05/18
-----

 - Fixed illegal reflective access warnings

Version 0.55.3 - 2020/05/12
-----

 - pom file was still missing from release

Version 0.55.2 - 2020/05/09
-----

 - fileMatcherUpdateInPlace alias for jsonMatcherUpdateInPlace
 - fixed partial previous release

Version 0.55 - 2020/05/03
-----

 - Fixed dependencies in released pom file
 - Added support for custom TestMetaInformation
 - Parameterized Junit 5 support
 - Added nio.Path serialization support

Version 0.54 - 2020/04/28
-----

 - Dropped Java 6 support, requires Java 8 now
 - Dependency upgrades
 - Junit 5 support
 - Being a popular request, added new Gson serializers for util.Date, java.time.*, java.lang.Class
 - Preliminary assertThrows implementation (serialization format will change shortly)
 - NPE fix

Version 0.21 - 2019/02/21
-----

 - Added support to skip circle detection for a field
 - Upgraded GSON to the latest version

Version 0.19 - 2018/09/06
-----

 - Fixed cycle check to skip ignored fields
 - Enabled custom fields matchers in JsonMatcher
 - Added convenience method for setting field ignores

Version 0.18 - 2018/08/09
-----

 - Fixed NPE with sameJsonAs while using it with data driven tests.

Version 0.17 - 2018/01/29
-----

 - Added flag for in place update of existing approved files.
   This helps to change existing files in a test library for every test affected by a change simply adding a command line property. (jsonMatcherUpdateInPlace=true)
 - Fixed custom matching for inherited fields

Version 0.16 - 2017/08/20
-----

 - Fixed an NPE in path ignore
 - Minor error message wording changes

Version 0.15 - 2017/04/22
-----

 - First release of ApprovalCrest
 - Added new matchers sameJsonAsApproved and sameContentAsApproved
 - Updated dependencies
 - Added possibility to configure custom type adapters

Version 0.11 - 2015/03/04
-----

It's now possible to ignore all the fields which name matches a given Hamcrest matcher.
Fixed diagnostic in case actual value is null.

Version 0.10 - 2015/02/16
-----

Automatic detection of circular references.
Fixed comparison of Guava Optional.

Version 0.9 - 2014/09/17
-----

Fixed random comparison failures for sets and maps.

Version 0.8 - 2014/07/16
-----

Handled circular references.

Version 0.7 - 2013/10/20
-----

Fixed NullPointerException thrown when custom matching is applied to a null object.

Version 0.6 - 2013/10/16
-----

The matcher is now using IsEqual Hamcrest matcher when Enums are compared.

Version 0.5 - 2013/10/14
-----

Description given to assertThat is now preserved in ComparisonFailure.

Version 0.4 - 2013/10/10
-----

Added option to match specific fields with custom matchers.
The matcher is now using IsEqual Hamcrest matcher when Strings or primitives are compared.

Version 0.3 - 2013/08/16
-----

Added option to ignore specific fields or Java types from the comparison.

Version 0.2 - 2013/05/15
-----

Fixed Map serialisation.

Version 0.1 - 2013/03/20
-----

Initial release.
