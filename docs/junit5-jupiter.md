<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=0726f4f4-27ae-4491-99ae-1553b09dad73" />

# junit5-jupiter

Using approvalcrest with JUnit 5 & 6 Jupiter (Java). The `approvalcrest-junit-jupiter` artifact works with both JUnit 5 and JUnit 6 (JUnit Platform) without any code changes.

## Dependency

```xml
<dependency>
    <groupId>com.github.karsaig</groupId>
    <artifactId>approvalcrest-junit-jupiter</artifactId>
    <version>1.4.0</version>
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

Add `TestInfo` as a parameter — JUnit 5 & 6 inject it automatically. Pass it to `sameJsonAsApproved(testInfo)` so the matcher can resolve the correct test method. Use `.withUniqueId(name)` to create a separate approved file per case:

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

## Related

- [same-json-as-approved](same-json-as-approved.md)
- [same-bean-as](same-bean-as.md)
- [file-control](file-control.md)
- [kotlin](kotlin.md)
