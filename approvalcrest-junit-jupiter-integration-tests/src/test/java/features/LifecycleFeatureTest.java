package features;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

/**
 * Lifecycle methods are not tests, so there is no test frame on the stack while one runs: a matcher built
 * from a {@code @BeforeEach} cannot resolve anything. A {@code TestInfo} captured into a field can,
 * though, and it names the test that is about to run — which is how a fixture shared by every test in the
 * class still writes one approved file per test.
 *
 * <p>{@code @TestInstance(PER_CLASS)} keeps one instance for the whole class and allows non-static
 * {@code @BeforeAll}/{@code @AfterAll}; neither changes what identifies a test.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//53ce47
public class LifecycleFeatureTest {

    private final List<String> events = new ArrayList<>();

    private TestInfo capturedTestInfo;

    @BeforeAll
    void beforeAll() {
        events.add("beforeAll");
    }

    @BeforeEach
    void captureTestInfo(TestInfo testInfo) {
        events.add("beforeEach");
        capturedTestInfo = testInfo;

        NullPointerException actual = Assertions.assertThrows(NullPointerException.class,
                () -> sameJsonAsApproved());

        Assertions.assertTrue(actual.getMessage().contains("org.junit.jupiter.api.TestInfo"),
                "The error must tell the user to pass TestInfo, but was: " + actual.getMessage());
    }

    @AfterEach
    void assertFromALifecycleMethod() {
        assertThat(new BeanOne("from", "afterEach"),
                sameJsonAsApproved(capturedTestInfo).withUniqueId("afterEach"));
    }

    @AfterAll
    void afterAll() {
        Assertions.assertEquals("beforeAll", events.get(0));
    }

    //ec732a
    @Test
    public void aCapturedTestInfoResolvesTheSameFileAsTheStackTraceRoute() {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(capturedTestInfo));
    }

    //8ba176
    @Test
    void perClassLifecycleDoesNotChangeTheApprovedFile(TestInfo testInfo) {
        Assertions.assertTrue(events.contains("beforeAll"), "PER_CLASS lifecycle must run @BeforeAll once");

        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    private static BeanOne bean() {
        return new BeanOne("dummy", "value");
    }
}
