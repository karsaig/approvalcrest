<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=bf2ead15-ef93-47bf-bbfb-32d354a9f801" />

# kotlin

Using approvalcrest with Kotlin and JUnit 5 & 6.

## Dependency

```xml
<dependency>
    <groupId>com.github.karsaig</groupId>
    <artifactId>approvalcrest-junit-jupiter-kotlin</artifactId>
    <version>1.5.0</version>
    <scope>test</scope>
</dependency>
```

## Imports

```kotlin
import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameBeanAs
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameJsonAsApproved
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameContentAsApproved
```

## Why a Kotlin-Specific Module?

Kotlin's type inference mishandles F-bounded (recursive) generics — [KT-5464](https://youtrack.jetbrains.com/issue/KT-5464). Without the Kotlin module, chaining methods like `.with()` / `.alsoCheck()`, `.ignoring()`, and `.withUniqueId()` fail to compile. The `approvalcrest-junit-jupiter-kotlin` module provides extension functions that work around this.

## Basic Tests

```kotlin
@Test
fun sameBeanComparison() {
    val actual   = buildBean()
    val expected = buildBean()
    assertThat(actual, sameBeanAs(expected))
}

@Test
fun approvalTest() {
    assertThat(buildBean(), sameJsonAsApproved<MyBean>())
}

@Test
fun contentApprovalTest() {
    assertThat("hello world", sameContentAsApproved<String>())
}
```

## Chaining

These chains compile correctly thanks to the extension functions (they would not compile using the plain Java API):

```kotlin
// sameBeanAs with .with() and .ignoring()
assertThat(actual, sameBeanAs(expected)
    .ignoring("transientId")
    .with("beanInteger", equalTo(42)))

// sameJsonAsApproved with .withUniqueId() and .ignoring()
assertThat(actual, sameJsonAsApproved<MyBean>()
    .withUniqueId("myCase")
    .ignoring(is("createdAt")))

// sameContentAsApproved with .withUniqueId()
assertThat("content", sameContentAsApproved<String>().withUniqueId("myCase"))
```

## Parameterized Tests

Same pattern as JUnit 5 & 6 Java — add `TestInfo` as a parameter:

```kotlin
@ParameterizedTest
@MethodSource("data")
fun testParameterized(name: String, value: BeanOne, testInfo: TestInfo) {
    assertThat(value, sameJsonAsApproved<BeanOne>(testInfo).withUniqueId(name))
}

companion object {
    @JvmStatic
    fun data(): Stream<Arguments> = Stream.of(
        Arguments.of("case1", BeanOne("dummy1", "val1")),
        Arguments.of("case2", BeanOne("dummy2", "val2"))
    )
}
```

## Inherited test methods

The Kotlin matchers use the JUnit 5 machinery, so the same rule applies: a test function declared in
an abstract base class and run by several subclasses fails with a clear error, because the stack
frame names the declaring class rather than the subclass. Pass `TestInfo` to get one approved file
per subclass. See [junit5-jupiter](junit5-jupiter.md#inherited-test-methods).

## Where approved files are stored

Kotlin approved files default to `src/test/kotlin`, alongside the test class, rather than the `src/test/java` used by the Java matchers. If your Kotlin tests live somewhere else, point approvalcrest at it with the shared `fileMatcherSourceRoot` property:

```bash
mvn verify -DfileMatcherSourceRoot=src/it/kotlin
```

Only the default differs per framework — the property itself is shared, so in a project with both Java and Kotlin tests it moves both. See [file-control](file-control.md#configuring-the-test-source-root).

## Related

- [junit5-jupiter](junit5-jupiter.md)
- [custom-matching](custom-matching.md)
- [same-json-as-approved](same-json-as-approved.md)
