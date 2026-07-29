package samejson;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import com.github.karsaig.approvalcrest.Junit4DesciptionWatcher;
import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

import org.junit.Rule;
import org.junit.Test;

/**
 * The stack-trace route and the Description route must resolve the same approved file, so that
 * adopting {@link Junit4DesciptionWatcher} on an existing test does not orphan its golden master.
 *
 * <p>Each test asserts through both routes against a single approved file; a divergence makes the
 * second assertion look for a file that does not exist.
 */
public class RouteEvolutionTest {

    @Rule
    public Junit4DesciptionWatcher testWatcher = new Junit4DesciptionWatcher();

    private static BeanOne bean() {
        return new BeanOne("dummy", "value");
    }

    @Test
    public void bothRoutesUseOneFile() {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testWatcher.getDescription()));
    }

    @Test
    public void bothRoutesUseOneFileForASecondTest() {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testWatcher.getDescription()));
    }
}
