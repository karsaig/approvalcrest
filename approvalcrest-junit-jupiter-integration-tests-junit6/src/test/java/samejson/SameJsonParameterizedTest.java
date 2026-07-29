package samejson;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import java.util.stream.Stream;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

//75a695
public class SameJsonParameterizedTest {

    //ec1105
    public static Stream<Arguments> testPublicParameterizedWorks() {
        return Stream.of(
                Arguments.of("case1", new BeanOne("dummy1", "val1")),
                Arguments.of("case2", new BeanOne("dummy2", "val2"))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testPublicParameterizedWorks(String name, BeanOne value) {
        assertThat(value, sameJsonAsApproved().withUniqueId(name));
    }

    //3fe39a
    public static Stream<Arguments> testPublicParameterizedWorksWithTestInfo() {
        return Stream.of(
                Arguments.of("case1", new BeanOne("dummy1", "val1")),
                Arguments.of("case2", new BeanOne("dummy2", "val2"))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testPublicParameterizedWorksWithTestInfo(String name, BeanOne value, TestInfo testInfo) {
        assertThat(value, sameJsonAsApproved(testInfo).withUniqueId(name));
    }

    public static Stream<Arguments> testPrivateParameterizedWorksWithBothRoutes() {
        return Stream.of(
                Arguments.of("case1", new BeanOne("dummy1", "val1")),
                Arguments.of("case2", new BeanOne("dummy2", "val2"))
        );
    }

    /**
     * The stack-trace route and the TestInfo route must agree, so a package-private test
     * asserting both ways resolves the same approved file. If they diverged, the second
     * assertion would look for a file that does not exist.
     */
    @ParameterizedTest
    @MethodSource
    void testPrivateParameterizedWorksWithBothRoutes(String name, BeanOne value, TestInfo testInfo) {
        assertThat(value, sameJsonAsApproved().withUniqueId(name));
        assertThat(value, sameJsonAsApproved(testInfo).withUniqueId(name));
    }

    //f2c9f1
    public static Stream<Arguments> testPrivateParameterizedWorksWithTestInfo() {
        return Stream.of(
                Arguments.of("case1", new BeanOne("dummy1", "val1")),
                Arguments.of("case2", new BeanOne("dummy2", "val2"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testPrivateParameterizedWorksWithTestInfo(String name, BeanOne value, TestInfo testInfo) {
        assertThat(value, sameJsonAsApproved(testInfo).withUniqueId(name));
    }
}
