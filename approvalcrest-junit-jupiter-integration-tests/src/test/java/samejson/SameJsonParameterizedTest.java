package samejson;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

//75a695
public class SameJsonParameterizedTest {

    public static Object[][] data() {
        return new Object[][] {
                { "case1", new BeanOne("dummy1","val1") },{ "case2", new BeanOne("dummy2","val2") }
        };
    }

    //ec1105
    @ParameterizedTest
    @MethodSource("data")
    public void testPublicParameterizedWorks(String name, BeanOne value){
        assertThat(value,sameJsonAsApproved().withUniqueId(name));
    }

    //3fe39a
    @ParameterizedTest
    @MethodSource("data")
    public void testPublicParameterizedWorksWithTestInfo(String name, BeanOne value, TestInfo testInfo){
        assertThat(value,sameJsonAsApproved(testInfo).withUniqueId(name));
    }

    /**
     * The stack-trace route and the TestInfo route must agree, so a package-private test
     * asserting both ways resolves the same approved file. If they diverged, the second
     * assertion would look for a file that does not exist.
     */
    @ParameterizedTest
    @MethodSource("data")
    void testPrivateParameterizedWorksWithBothRoutes(String name, BeanOne value, TestInfo testInfo) {
        assertThat(value, sameJsonAsApproved().withUniqueId(name));
        assertThat(value, sameJsonAsApproved(testInfo).withUniqueId(name));
    }

    //f2c9f1
    @ParameterizedTest
    @MethodSource("data")
    void testPrivateParameterizedWorksWithTestInfo(String name, BeanOne value, TestInfo testInfo){
        assertThat(value,sameJsonAsApproved(testInfo).withUniqueId(name));
    }
}
