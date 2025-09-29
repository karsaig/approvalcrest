package samecontent;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameContentAsApproved;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

//75a695
public class SameContentParameterizedTest {

    public static Object[][] data() {
        return new Object[][] {
                { "case1", "value1" },{ "case2", "value2" }
        };
    }

    //ec1105
    @ParameterizedTest
    @MethodSource("data")
    public void testPublicParameterizedWorks(String name, String value){
        assertThat(value,sameContentAsApproved().withUniqueId(name));
    }

    //3fe39a
    @ParameterizedTest
    @MethodSource("data")
    public void testPublicParameterizedWorksWithTestInfo(String name, String value, TestInfo testInfo){
        assertThat(value,sameContentAsApproved(testInfo).withUniqueId(name));
    }

    @ParameterizedTest
    @MethodSource("data")
    void testPrivateParameterizedThrows(String name, String value){
        NullPointerException expected = new NullPointerException("Cannot determine test method for JunitJupiterTestMeta, do either of the following to solve it:\n1. Pass org.junit.jupiter.api.TestInfo in as constructor parameter to matcher, if you add it as a parameter to the test method, junit will provide it\n2. Provide a custom implementation of TestMetaInformation, this is rarely needed.");

        assertThrows(sameBeanAs(expected),() -> assertThat(value, sameContentAsApproved().withUniqueId(name)));
    }

    //f2c9f1
    @ParameterizedTest
    @MethodSource("data")
    void testPrivateParameterizedWorksWithTestInfo(String name, String value, TestInfo testInfo){
        assertThat(value,sameContentAsApproved(testInfo).withUniqueId(name));
    }
}
