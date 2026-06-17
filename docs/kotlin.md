# kotlin

Using approvalcrest with Kotlin and JUnit 5 & 6.

## Dependency

```xml
<dependency>
    <groupId>com.github.karsaig</groupId>
    <artifactId>approvalcrest-junit-jupiter-kotlin</artifactId>
    <version>1.3.3</version>
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

Kotlin's type inference mishandles F-bounded (recursive) generics — [KT-5464](https://youtrack.jetbrains.com/issue/KT-5464). Without the Kotlin module, chaining methods like `.with()`, `.ignoring()`, and `.withUniqueId()` fail to compile. The `approvalcrest-junit-jupiter-kotlin` module provides extension functions that work around this.

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

## Related

- [junit5-jupiter](junit5-jupiter.md)
- [custom-matching](custom-matching.md)
- [same-json-as-approved](same-json-as-approved.md)
