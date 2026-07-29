package samecontent;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
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

    /**
     * The stack-trace route and the TestInfo route must agree, so a package-private test
     * asserting both ways resolves the same approved file. If they diverged, the second
     * assertion would look for a file that does not exist.
     */
    @ParameterizedTest
    @MethodSource("data")
    void testPrivateParameterizedWorksWithBothRoutes(String name, String value, TestInfo testInfo) {
        assertThat(value, sameContentAsApproved().withUniqueId(name));
        assertThat(value, sameContentAsApproved(testInfo).withUniqueId(name));
    }

    //f2c9f1
    @ParameterizedTest
    @MethodSource("data")
    void testPrivateParameterizedWorksWithTestInfo(String name, String value, TestInfo testInfo){
        assertThat(value,sameContentAsApproved(testInfo).withUniqueId(name));
    }
}
