package samejson;

import static com.github.karsaig.approvalcrest.testng.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameJsonAsApproved;

import java.lang.reflect.Method;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

import org.testng.annotations.Test;

/**
 * The stack-trace route and the injected {@link Method} route must resolve the same approved file,
 * so that accepting a {@code Method} parameter on an existing test does not orphan its golden
 * master.
 *
 * <p>Each test asserts through both routes against a single approved file; a divergence makes the
 * second assertion look for a file that does not exist.
 */
public class RouteEvolutionTest {

    private static BeanOne bean() {
        return new BeanOne("dummy", "value");
    }

    @Test
    public void bothRoutesUseOneFile(Method testMethod) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testMethod));
    }

    @Test
    public void bothRoutesUseOneFileForASecondTest(Method testMethod) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testMethod));
    }
}
