<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=0726f4f4-27ae-4491-99ae-1553b09dad73" />

# junit5-jupiter

Using approvalcrest with JUnit 5 & 6 Jupiter (Java). The `approvalcrest-junit-jupiter` artifact works with both JUnit 5 and JUnit 6 (JUnit Platform) without any code changes.

## Dependency

```xml
<dependency>
    <groupId>com.github.karsaig</groupId>
    <artifactId>approvalcrest-junit-jupiter</artifactId>
    <version>1.6.0</version>
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

### Automatic per-case file names

When `TestInfo` is passed, the index JUnit puts at the front of the default display name (`[1] ...`) is used
as the unique id, so each case gets its own approved file without asking for one:

```java
@ParameterizedTest
@ValueSource(strings = {"alpha", "beta"})
void valueSource(String value, TestInfo testInfo) {
    // writes ...-1-approved.json and ...-2-approved.json
    assertThat(beanFor(value), sameJsonAsApproved(testInfo));
}
```

This holds for every argument source (`@ValueSource`, `@CsvSource`, `@EnumSource`, `@NullSource`,
`@EmptySource`, `@MethodSource`, `@ArgumentsSource`) and for `sameContentAsApproved`. A custom
`@ParameterizedTest(name = ...)` pattern that does not start with the index switches it off — then use
`withUniqueId` to keep one file per case, which is also the more readable option:

```java
@ParameterizedTest(name = "case {0}")
@ValueSource(strings = {"alpha", "beta"})
void customName(String value, TestInfo testInfo) {
    assertThat(beanFor(value), sameJsonAsApproved(testInfo).withUniqueId(value));
}
```

## Repeated Tests

`@RepeatedTest` is meta-annotated with `@TestTemplate`, so the stack trace resolves it without `TestInfo`.
The repetition number is not in the `[1] ...` shape, so it never reaches the matcher on its own — pass it
in, otherwise every repetition compares against a single approved file:

```java
@RepeatedTest(3)
void repeated(RepetitionInfo repetitionInfo) {
    assertThat(actual, sameJsonAsApproved().withUniqueId("rep" + repetitionInfo.getCurrentRepetition()));
}
```

## Dynamic Tests

A `DynamicTest` executable is a lambda, not a test method: by the time it runs the factory has returned and
there is nothing on the stack to resolve, so the no-argument matchers fail with the "pass `TestInfo`" error.
Take `TestInfo` in the factory — it names the factory method — and give each case a unique id:

```java
@TestFactory
Stream<DynamicTest> dynamicTests(TestInfo testInfo) {
    return Stream.of("case1", "case2")
            .map(name -> dynamicTest(name,
                    () -> assertThat(beanFor(name), sameJsonAsApproved(testInfo).withUniqueId(name))));
}
```

The same applies to tests nested in a `DynamicContainer`.

## Test Templates

A `@TestTemplate` driven by a custom `TestTemplateInvocationContextProvider` is resolved by both routes.
Its display name comes from the provider, so per-invocation files need `withUniqueId`.

## Lifecycle Methods

A matcher created inside `@BeforeEach`, `@AfterEach` or `@BeforeAll` cannot resolve the running test —
no test method is on the stack. Capture the injected `TestInfo` in a field instead; it names the test that
is about to run, so a shared fixture still gets one approved file per test:

```java
private TestInfo testInfo;

@BeforeEach
void captureTestInfo(TestInfo testInfo) {
    this.testInfo = testInfo;
}

@AfterEach
void verifyState() {
    assertThat(state, sameJsonAsApproved(testInfo).withUniqueId("afterEach"));
}
```

`@TestInstance(PER_CLASS)` does not change any of this.

## Display Names, Extensions and Composed Annotations

None of these move an approved file:

- `@DisplayName` and `@DisplayNameGeneration` — the method name identifies a test, not its display name
  (the automatic index above is the one exception).
- `@Nested` — a nested class is its own test class and gets its own directory.
- extension-injected parameters, `@RegisterExtension` and an `InvocationInterceptor`.
- an annotation composed on top of `@Test` or `@TestTemplate`, directly or through another annotation.

## `assertThrows`

Verify that an assertion fails with a specific matcher:

```java
assertThrows(sameBeanAs(expectedException),
    () -> assertThat(actual, sameBeanAs(wrongExpected)));
```

## Inherited test methods

A test method declared in an abstract base class and run by several subclasses cannot be resolved by
the default stack-trace detection: a stack frame names the class a method is *declared* in, not the
subclass running it, so every subclass would resolve to the same approved file and overwrite the
others. Approvalcrest fails with a clear error rather than doing that.

Pass `TestInfo` to get one approved file per subclass — JUnit provides it, and it knows the concrete
class:

```java
public abstract class AbstractContractTest {
    @Test
    void sharedTest(TestInfo testInfo) {
        assertThat(actual, sameJsonAsApproved(testInfo));
    }
}
```

A **concrete** base class with subclasses has the same ambiguity but is indistinguishable from an
ordinary test class, so it cannot be detected — pass `TestInfo` there too.

## Related

- [same-json-as-approved](same-json-as-approved.md)
- [same-bean-as](same-bean-as.md)
- [file-control](file-control.md)
- [kotlin](kotlin.md)
