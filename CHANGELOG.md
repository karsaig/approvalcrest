Changelog
===========

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
