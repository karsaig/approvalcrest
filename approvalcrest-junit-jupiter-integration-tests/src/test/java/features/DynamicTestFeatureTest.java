package features;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

/**
 * Dynamic tests are not methods, so there is no test method for the stack-trace route to find: by the
 * time a {@code DynamicTest} executable runs, the factory method has returned and the nearest frame is a
 * synthetic lambda. The factory itself can take a {@code TestInfo}, which names the factory method, so a
 * unique id per dynamic case is what separates the approved files.
 */
//38c8dc
public class DynamicTestFeatureTest {

    //6d29a8
    @TestFactory
    public Stream<DynamicTest> dynamicTestsGetOneFilePerCase(TestInfo testInfo) {
        return Stream.of("case1", "case2")
                .map(name -> DynamicTest.dynamicTest(name,
                        () -> assertThat(beanFor(name), sameJsonAsApproved(testInfo).withUniqueId(name))));
    }

    //e1729d
    @TestFactory
    Stream<DynamicNode> dynamicContainersGetOneFilePerCase(TestInfo testInfo) {
        return Stream.of(DynamicContainer.dynamicContainer("group", Stream.of(
                DynamicTest.dynamicTest("first",
                        () -> assertThat(beanFor("first"), sameJsonAsApproved(testInfo).withUniqueId("first"))),
                DynamicTest.dynamicTest("second",
                        () -> assertThat(beanFor("second"), sameJsonAsApproved(testInfo).withUniqueId("second"))))));
    }

    /**
     * Pins the diagnostic a user gets when they forget the {@code TestInfo} in a dynamic test. Nothing on
     * the stack is annotated as a test, so the matcher cannot be built at all.
     */
    @TestFactory
    Stream<DynamicTest> stackTraceRouteCannotResolveADynamicTest() {
        return Stream.of(DynamicTest.dynamicTest("without TestInfo", () -> {
            NullPointerException actual = Assertions.assertThrows(NullPointerException.class,
                    () -> sameJsonAsApproved());

            Assertions.assertTrue(actual.getMessage().contains("org.junit.jupiter.api.TestInfo"),
                    "The error must tell the user to pass TestInfo, but was: " + actual.getMessage());
        }));
    }

    private static BeanOne beanFor(String name) {
        return new BeanOne("dummy-" + name, "value-" + name);
    }
}
