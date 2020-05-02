package samejson;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;

//f4eff9
public class SameJsonTest {

    //dac21e
    @Test
    public void runWithDefaultConfigShouldPassWithMatchingApprovedFile() {
        BeanWithPrimitives actual = getBeanWithPrimitivesMaxValues();

        assertThat(actual, sameJsonAsApproved());
    }

    //fd51ac
    @Test
    public void runWithDefaultConfigShouldFailWithDifferentContentInApprovedFile() {
        BeanWithPrimitives actual = getBeanWithPrimitivesMaxValues();

        assertThrows(sameJsonAsApproved().withUniqueId("thrown").ignoring(is("identityHashCode")), () -> assertThat(actual, sameJsonAsApproved()));
    }

    protected BeanWithPrimitives getBeanWithPrimitivesMaxValues() {
        short beanShort = Short.MAX_VALUE;
        boolean beanBoolean = false;
        byte beanByte = Byte.MAX_VALUE;
        char beanChar = Character.MAX_VALUE;
        float beanFloat = Float.MAX_VALUE;
        int beanInt = Integer.MAX_VALUE;
        double beanDouble = Double.MAX_VALUE;
        long beanLong = Long.MAX_VALUE;

        BeanWithPrimitives bean = BeanWithPrimitives.Builder.beanWithPrimitives()
                .beanShort(beanShort)
                .beanBoolean(beanBoolean)
                .beanByte(beanByte)
                .beanChar(beanChar)
                .beanFloat(beanFloat)
                .beanInt(beanInt)
                .beanDouble(beanDouble)
                .beanLong(beanLong)
                .build();

        return bean;
    }
}
