# junit4-vintage

Using approvalcrest with JUnit 4 and JUnit 5 Vintage.

## Dependency

```xml
<dependency>
    <groupId>com.github.karsaig</groupId>
    <artifactId>approvalcrest</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
</dependency>
```

## Imports

```java
import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;
```

## Basic Test

```java
public class MyTest {

    @Test
    public void myTest() {
        MyBean actual = buildMyBean();
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void myContentTest() {
        String actual = renderTemplate();
        assertThat(actual, sameContentAsApproved());
    }
}
```

No extra runner or rule is needed for non-parameterized tests.

## Parameterized Tests

JUnit 4 cannot automatically provide test metadata to the matcher. Add a `Junit4DesciptionWatcher` rule and optionally pass the description to the matcher:

```java
import com.github.karsaig.approvalcrest.Junit4DesciptionWatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MyParameterizedTest {

    @Rule
    public Junit4DesciptionWatcher testWatcher = new Junit4DesciptionWatcher();

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "case1", new BeanOne("dummy1", "val1") },
            { "case2", new BeanOne("dummy2", "val2") }
        });
    }

    private final String name;
    private final BeanOne value;

    public MyParameterizedTest(String name, BeanOne value) {
        this.name  = name;
        this.value = value;
    }

    @Test
    public void testParameterized() {
        // withUniqueId disambiguates the approved file per case
        assertThat(value, sameJsonAsApproved().withUniqueId(name));
    }

    @Test
    public void testParameterizedWithDescription() {
        // Pass the description explicitly for full metadata resolution
        assertThat(value, sameJsonAsApproved(testWatcher.getDescription()).withUniqueId(name));
    }

    @Test
    public void testContentParameterized() {
        // sameContentAsApproved works the same way — pass .withUniqueId per case
        String content = renderForCase(name);
        assertThat(content, sameContentAsApproved().withUniqueId(name));
    }
}
```

## `assertThrows`

Verify that an assertion fails with an expected exception:

```java
assertThrows(sameBeanAs(expectedException),
    () -> assertThat(actual, sameBeanAs(wrongExpected)));
```
