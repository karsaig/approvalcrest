package samecontent;

import com.github.karsaig.approvalcrest.Junit4DesciptionWatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

//9dffb5
@RunWith(Parameterized.class)
public class SameContentParameterizedTest {

    @Rule
    public Junit4DesciptionWatcher testWatcher = new Junit4DesciptionWatcher();

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "case1", "value1" },{ "case2", "value2" }
        });
    }

    private final String name;
    private final String value;

    public SameContentParameterizedTest(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Test
    public void testParameterizedWorks(){
        assertThat(value,sameContentAsApproved().withUniqueId(name));
    }

    //f07cc4
    @Test
    public void testParameterizedWorksWithDescription(){
        assertThat(value,sameContentAsApproved(testWatcher.getDescription()).withUniqueId(name));
    }
}
