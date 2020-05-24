package samejson;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

    public static Object[][] parameterizedTestCases() {
        return new Object[][]{
                {Optional.empty()},
                {Optional.of(13L)},
                {Optional.of("14")},
        };
    }

    @ParameterizedTest
    @MethodSource("parameterizedTestCases")
    void parameterizedTest(Object input, TestInfo testInfo) {
        assertThat(input, sameJsonAsApproved(testInfo));
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
