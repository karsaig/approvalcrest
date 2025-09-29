package samejson;

import com.github.karsaig.approvalcrest.Junit4DesciptionWatcher;
import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

//75a695
@RunWith(Parameterized.class)
public class SameJsonParameterizedTest {

    @Rule
    public Junit4DesciptionWatcher testWatcher = new Junit4DesciptionWatcher();

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "case1", new BeanOne("dummy1","val1") },{ "case2", new BeanOne("dummy2","val2") }
        });
    }

    private final String name;
    private final BeanOne value;

    public SameJsonParameterizedTest(String name, BeanOne value) {
        this.name = name;
        this.value = value;
    }

    @Test
    public void testParameterizedWorks(){
        assertThat(value,sameJsonAsApproved().withUniqueId(name));
    }

    //f07cc4
    @Test
    public void testParameterizedWorksWithDescription(){
        assertThat(value,sameJsonAsApproved(testWatcher.getDescription()).withUniqueId(name));
    }
}
