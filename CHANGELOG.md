Changelog
===========

Version 1.3.3 - WIP
-----

- Fixed `withAlias` / `withAliasMap` being applied to the approved-file content in strict mode (`fileMatcherStrictFileMatching=true`, the default). All other transformations — `.ignoring()`, `.with()`, pattern-based sorting — were already correctly skipped on the approved side in strict mode; aliases were the only exception. After this fix, aliases are applied to the actual side only in strict mode, consistent with the strict-mode contract that the approved file is used as-is. An approved file that still contains the raw (non-aliased) value is now correctly detected as stale and causes the assertion to fail, prompting regeneration. Approved files that already contain the aliased form (the normal case) are unaffected — no re-generation needed.
- Added AI tip to the "not approved file created" failure message: the human-readable message now ends with `[AI tip] Re-run with fmAI=true for the absolute path of the file that needs to be renamed.` Previously only content-mismatch failures carried the tip. Also fixed the machine-readable `action` field for `NEW_FILE` failures: it previously incorrectly suggested `fMUInPlace=true`, which throws when no approved file exists yet; the correct action is to rename/copy the generated file.
- Added AI tip to the type-mismatch failure message (`sameBeanAs` with incompatible types): the non-machine-readable branch now ends with the standard `[AI tip] Re-run with fmAI=true for structured, machine-readable output.` tip, consistent with content-mismatch failures.
- Fixed `JsonMatcher.describeTo()` applying transformations (aliases, ignored-field removal, custom sorting) to the approved-file content in strict mode, inconsistently with `doMatches()`. In strict mode the approved file is used as-is on the expected side of the comparison, but `describeTo()` was silently aliasing/stripping the same content, causing the "Expected:" section of the Hamcrest failure message to show different values than the diff. For normal (up-to-date) approved files this was a no-op difference; for stale approved files the failure message was contradictory. Fixed by extracting a shared `filterExpectedJson()` helper used by both `describeTo()` and `doMatches()`, so the two call sites cannot diverge in the future.

Version 1.3.2 - 2026/06/03
-----

- Machine-readable JSON output is now emitted in compact form (no pretty-printing) to minimise token usage for AI consumers. The `expected` and `actual` field values, as well as the outer JSON wrapper, are all serialised without whitespace across all failure types (`MISMATCH`, `NEW_FILE`, `TYPE_MISMATCH`).
- Fixed path-based operations (`.ignoring("path")`, `.with("path", matcher)`, `.sortField("path")`, `SortField.ignoring("path")`) failing when the path traverses through a type detected as having a circular reference. `GraphAdapterBuilder` wraps such types with synthetic `0xN` envelope keys, which broke path resolution. These envelope keys are now transparently skipped during path navigation — the user-specified path works identically whether the target type has circular references or not.

Version 1.3.1 - 2026/06/01
-----

- Fixed `JavaOptionalSerializer` for `Optional<Interface>` and `Optional<AbstractClass>`: when the declared type parameter is an interface or abstract class, the serializer now falls back to runtime-type serialization instead of producing an empty `{}`. Previously, Gson's `ReflectiveTypeAdapterFactory` would find zero fields on the declared type and silently serialize nothing. See [supported-types](docs/supported-types.md).
- Fixed polymorphic base class handling in `Optional`: `Optional<Animal>` holding a `Dog` instance now serializes all `Dog`-specific fields. A new `isPolymorphic()` check detects when the runtime type differs from the declared type and uses the runtime type. See [supported-types](docs/supported-types.md).
- Added custom serializers for `OptionalInt`, `OptionalLong`, and `OptionalDouble`. These primitive-specialized Optional types are now serialized with the same `{"value": N}` / `{}` format as `java.util.Optional`, instead of falling through to reflection on locked `java.util` internals. See [supported-types](docs/supported-types.md).
- Added `GsonConfiguration.addTypeToSkipInFallbackFactories(Class<?>)`: allows users to prevent `UnsafeFieldTypeAdapterFactory` and `GetterBasedTypeAdapterFactory` from claiming specific types, so that custom `TypeAdapter`/`TypeAdapterFactory` registrations (e.g. for Vavr's `Option`, `Either`) take precedence. See [supported-types](docs/supported-types.md).
- Added documentation: migration guide for path handling ([file-control](docs/file-control.md)) and IDE run template best practices ([best-practices](docs/best-practices.md)).

Version 1.3.0 - 2026/06/01
-----

- Machine-readable output is now structured JSON. Failure messages emitted with `-DfileMatcherMachineReadable=true` (or `withMachineReadableOutput()`) are valid JSON objects containing `failureType`, `test`, `approvedFile`/`approveTo`, `expected`, `actual`, plus metadata arrays `ignoredFields`, `aliasedFields`, and `sortedFields` that document what transformations were applied. See [file-control](docs/file-control.md).
- Added `fmAI` as an additional short alias for the machine-readable output system property (`-DfmAI=true`). The passive AI discovery tip appended to non-machine-readable failures now references `fmAI` for brevity. The original alias `fMMReadable` remains valid. See [system-properties](docs/system-properties.md).
- Added JDK 17+ integration tests for records and sealed classes (`approvalcrest-jdk17-integration-tests` module).
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
- Fixed JUnit 4 `ComparisonFailure.getMessage()` being polluted by `ComparisonCompactor` when `withMachineReadableOutput()` is active. A package-private `CleanMessageComparisonFailure` subclass is now thrown in that case, overriding `getMessage()` to return the original structured text while leaving `getExpected()`/`getActual()` (and therefore IDE diff view) intact.
- Verified compatibility with JUnit 6: all `approvalcrest-junit-jupiter` matchers and integration tests pass against JUnit Platform 6.0.3 on Java 17, 21, and 25. See [junit5-jupiter](docs/junit5-jupiter.md).
- **[breaking]** Added strict matching mode (`fileMatcherStrictFileMatching` system property, on by default): `ignoring()` strips fields from the actual side only, so approved files that contain the value of an ignored field will now fail. **Migration:** re-run tests with `-DfileMatcherUpdateInPlace=true` to regenerate approved files without the ignored fields. To restore the old two-sided behaviour globally set `-DfileMatcherStrictFileMatching=false`. In tests the config object uses the last boolean: `new FileMatcherConfig(false, false, false, false, false, /*strict=*/false)`. See [ignoring-fields](docs/ignoring-fields.md).
- **[breaking]** Added type comparison to `sameBeanAs`: fails with a clear message when actual and expected have incompatible runtime types. **Migration:** add `.skipClassComparison()` to the matcher: `assertThat(actual, sameBeanAs(expected).skipClassComparison())`, or set `-DbeanMatcherSkipClassComparison=true` to suppress globally. See [same-bean-as](docs/same-bean-as.md).
- Added `withAliasMap(AliasMap)` / `withAlias(value, alias)` / `withAlias(field, value, alias)`: replaces volatile values (UUIDs, timestamps) with stable aliases before comparison and file creation. Aliases are applied after ignores and before sorting. See [aliasing](docs/aliasing.md).
- Added `withMatcher(Matcher<String>, Matcher<V>)`: pattern-based custom matcher that applies to all fields at any depth whose name matches the supplied `Matcher<String>`. Supported by both `sameBeanAs` and `sameJsonAsApproved`. See [custom-matching](docs/custom-matching.md).
- Custom matchers now fall back to the JSON-serialised form when the Java-bean reflection path is unavailable (e.g. JSON string input to `sameJsonAsApproved`).
- Fixed sorting of root-level `List` / array inputs via `sortField("")`.
- Fixed sort order to be bottom-up: nested arrays sorted before their parent's key is computed.
- **[breaking]** Fixed sorting of arrays-of-arrays: only the outer array is reordered; inner array element order is preserved. Previously `sortField("groups")` on a `groups: [[Z,B],[A,C]]` also sorted the inner arrays into `[[A,C],[B,Z]]`; now the inner arrays stay as-is and only the outer order changes. **Migration:** if inner array order matters, switch the inner collection type to `Set` (auto-sorted by type) or add an explicit `sortField("innerFieldName")` for the inner field. See [sorting](docs/sorting.md).
- Fixed three bugs in sort-key filtering: complex fields not stripped, multi-level paths (e.g. `addr.city`) broken, `SortField<Matcher<String>>.ignoring()` had no effect.
- Fixed `withMatcher` pattern ignores being applied after sorting in `sameBeanAs` instead of before.
- Fixed `FieldsIgnorer` array fan-out erroring on primitive array elements.
- Fixed `FieldsIgnorer`: ignoring all fields of a bean `Map` key now removes the whole entry cleanly.
- Fixed `FieldsIgnorer`: ignoring a field inside a `Map` value no longer leaves an orphaned empty array.
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
