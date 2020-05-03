Changelog
===========

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
