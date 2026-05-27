package samecontent;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
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

    public static Stream<Arguments> testPrivateParameterizedThrows() {
        return Stream.of(
                Arguments.of("case1", "value1"),
                Arguments.of("case2", "value2")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testPrivateParameterizedThrows(String name, String value) {
        NullPointerException expected = new NullPointerException("Cannot determine test method for JunitJupiterTestMeta, do either of the following to solve it:\n1. Pass org.junit.jupiter.api.TestInfo in as constructor parameter to matcher, if you add it as a parameter to the test method, junit will provide it\n2. Provide a custom implementation of TestMetaInformation, this is rarely needed.");

        assertThrows(sameBeanAs(expected), () -> assertThat(value, sameContentAsApproved().withUniqueId(name)));
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
