package samecontent;

import static com.github.karsaig.approvalcrest.testng.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameContentAsApproved;

import java.lang.reflect.Method;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

//75a695
public class SameContentDataProviderTest {

    @DataProvider(name = "data")
    public Object[][] data() {
        return new Object[][]{
                {"case1", "Lorem ipsum dolor"},
                {"case2", "Árvízűtűrőtükörfúrógép"},
                {"case3", " L'apostrophe 用的名字☺\\\\nд1@00000☺☹❤\\\\naA@AA1A猫很可爱\""}
        };
    }

    //ec1105
    @Test(dataProvider = "data")
    public void testPublicDataProviderWorks(String name, String value) {
        assertThat(value, sameContentAsApproved().withUniqueId(name));
    }

    //3fe39a
    @Test(dataProvider = "data")
    public void testDataProviderWorksWithMethodInjection(String name, String value, Method method) {
        assertThat(value, sameContentAsApproved(method).withUniqueId(name));
    }
}
