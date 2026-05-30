package samejson;

import static com.github.karsaig.approvalcrest.testng.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.testng.matcher.Matchers.sameJsonAsApproved;

import java.lang.reflect.Method;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

//75a695
public class SameJsonDataProviderTest {

    @DataProvider(name = "data")
    public Object[][] data() {
        return new Object[][]{
                {"case1", new BeanOne("dummy1", "val1")},
                {"case2", new BeanOne("dummy2", "val2")}
        };
    }

    //ec1105
    @Test(dataProvider = "data")
    public void testPublicDataProviderWorks(String name, BeanOne value) {
        assertThat(value, sameJsonAsApproved().withUniqueId(name));
    }

    //3fe39a
    @Test(dataProvider = "data")
    public void testDataProviderWorksWithMethodInjection(String name, BeanOne value, Method method) {
        assertThat(value, sameJsonAsApproved(method).withUniqueId(name));
    }
}
