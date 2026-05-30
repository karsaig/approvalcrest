# TestNG

Using approvalcrest with TestNG.

## Dependency

```xml
<dependency>
    <groupId>com.github.karsaig</groupId>
    <artifactId>approvalcrest-testng</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
</dependency>
```

## Imports

```java
import static com.github.karsaig.approvalcrest.testng.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.testng.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameContentAsApproved;
```

## Basic Test

No extra configuration is needed for non-parameterized tests:

```java
@Test
public void myTest() {
    MyBean actual = buildMyBean();
    assertThat(actual, sameJsonAsApproved());
}
```

## Parameterized Tests with `@DataProvider`

Add `Method` as a parameter — TestNG injects it automatically. Pass it to `sameJsonAsApproved(method)` so the matcher can resolve the correct test method. Use `.withUniqueId(name)` to create a separate approved file per case:

```java
@DataProvider
public Object[][] data() {
    return new Object[][] {
        { "case1", new BeanOne("dummy1", "val1") },
        { "case2", new BeanOne("dummy2", "val2") }
    };
}

@Test(dataProvider = "data")
public void testParameterized(String name, BeanOne value, Method method) {
    assertThat(value, sameJsonAsApproved(method).withUniqueId(name));
}
```

The same pattern applies to `sameContentAsApproved`:

```java
@Test(dataProvider = "templates")
public void testContentParameterized(String name, String content, Method method) {
    assertThat(content, sameContentAsApproved(method).withUniqueId(name));
}
```

**Note:** `Method` injection requires **TestNG 6.14.2+**. On older versions, use the zero-arg `sameJsonAsApproved()` which resolves the test method via stack-trace walking (works on any TestNG 6.0+, but requires the test method to be public).

## `assertThrows`

Verify that an assertion fails with a specific matcher:

```java
assertThrows(sameBeanAs(expectedException),
    () -> assertThat(actual, sameBeanAs(wrongExpected)));
```

## Version Compatibility

| Feature | Minimum TestNG version |
|---|---|
| Basic API (`sameJsonAsApproved()`) | 6.0 |
| Method injection (`sameJsonAsApproved(Method)`) | 6.14.2 |

The `approvalcrest-testng` artifact uses TestNG as a `provided` dependency — you bring your own TestNG version at runtime.

## Related

- [same-json-as-approved](same-json-as-approved.md)
- [same-bean-as](same-bean-as.md)
- [same-content-as-approved](same-content-as-approved.md)
- [file-control](file-control.md)
