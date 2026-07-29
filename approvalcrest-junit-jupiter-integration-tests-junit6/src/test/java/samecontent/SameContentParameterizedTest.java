package samecontent;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameContentAsApproved;

import java.util.stream.Stream;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

//9dffb5
public class SameContentParameterizedTest {

    //ec1105
    public static Stream<Arguments> testPublicParameterizedWorks() {
        return Stream.of(
                Arguments.of("case1", "value1"),
                Arguments.of("case2", "value2")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testPublicParameterizedWorks(String name, String value) {
        assertThat(value, sameContentAsApproved().withUniqueId(name));
    }

    //3fe39a
    public static Stream<Arguments> testPublicParameterizedWorksWithTestInfo() {
        return Stream.of(
                Arguments.of("case1", "value1"),
                Arguments.of("case2", "value2")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testPublicParameterizedWorksWithTestInfo(String name, String value, TestInfo testInfo) {
        assertThat(value, sameContentAsApproved(testInfo).withUniqueId(name));
    }

    public static Stream<Arguments> testPrivateParameterizedWorksWithBothRoutes() {
        return Stream.of(
                Arguments.of("case1", "value1"),
                Arguments.of("case2", "value2")
        );
    }

    /**
     * The stack-trace route and the TestInfo route must agree, so a package-private test
     * asserting both ways resolves the same approved file. If they diverged, the second
     * assertion would look for a file that does not exist.
     */
    @ParameterizedTest
    @MethodSource
    void testPrivateParameterizedWorksWithBothRoutes(String name, String value, TestInfo testInfo) {
        assertThat(value, sameContentAsApproved().withUniqueId(name));
        assertThat(value, sameContentAsApproved(testInfo).withUniqueId(name));
    }

    //f2c9f1
    public static Stream<Arguments> testPrivateParameterizedWorksWithTestInfo() {
        return Stream.of(
                Arguments.of("case1", "value1"),
                Arguments.of("case2", "value2")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testPrivateParameterizedWorksWithTestInfo(String name, String value, TestInfo testInfo) {
        assertThat(value, sameContentAsApproved(testInfo).withUniqueId(name));
    }
}
