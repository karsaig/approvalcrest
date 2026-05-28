# junit5-jupiter

Using approvalcrest with JUnit 5 Jupiter (Java).

## Dependency

```xml
<dependency>
    <groupId>com.github.karsaig</groupId>
    <artifactId>approvalcrest-junit-jupiter</artifactId>
    <version>1.0.1</version>
    <scope>test</scope>
</dependency>
```

## Imports

```java
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameContentAsApproved;
```

## Basic Test

No extra extension is needed for non-parameterized tests:

```java
@Test
public void myTest() {
    MyBean actual = buildMyBean();
    assertThat(actual, sameJsonAsApproved());
}
```

## Parameterized Tests

Add `TestInfo` as a parameter — JUnit 5 injects it automatically. Pass it to `sameJsonAsApproved(testInfo)` so the matcher can resolve the correct test method. Use `.withUniqueId(name)` to create a separate approved file per case:

```java
@ParameterizedTest
@MethodSource("data")
public void testParameterized(String name, BeanOne value, TestInfo testInfo) {
    assertThat(value, sameJsonAsApproved(testInfo).withUniqueId(name));
}

static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of("case1", new BeanOne("dummy1", "val1")),
        Arguments.of("case2", new BeanOne("dummy2", "val2"))
    );
}
```

The same pattern applies to `sameContentAsApproved`:

```java
@ParameterizedTest
@MethodSource("templates")
public void testContentParameterized(String name, String content, TestInfo testInfo) {
    assertThat(content, sameContentAsApproved(testInfo).withUniqueId(name));
}
```

Without `TestInfo`:
- **Private** test methods cannot be resolved and throw a `NullPointerException` with a message explaining to add `TestInfo`.
- **Public** test methods work — the stack trace is used to resolve method metadata.

## `assertThrows`

Verify that an assertion fails with a specific matcher:

```java
assertThrows(sameBeanAs(expectedException),
    () -> assertThat(actual, sameBeanAs(wrongExpected)));
```

## JUnit 6 Compatibility

The `approvalcrest-junit-jupiter` module is verified against **JUnit Platform 6** (tested on JUnit 6.1.0 with Java 17, 21, and 25). No code changes are needed to run on JUnit 6 — the existing dependency and API work without modification.

## Related

- [same-json-as-approved](same-json-as-approved.md)
- [same-bean-as](same-bean-as.md)
- [file-control](file-control.md)
- [kotlin](kotlin.md)
