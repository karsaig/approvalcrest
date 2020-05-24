package samebean;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;

public class SameBeanTest {

    @Test
    public void runWithDefaultConfigShouldPassWithMatchingExpectation() {
        BeanWithPrimitives actual = getBeanWithPrimitivesMinValues();

        BeanWithPrimitives expected = getBeanWithPrimitivesMinValues();
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void runWithDefaultConfigShouldFailWithDifferentExpectation() {
        BeanWithPrimitives actual = getBeanWithPrimitivesMinValues();

        Optional<BeanWithPrimitives> expected = Optional.empty();
        String expectedMessage = "\n" +
                "Unexpected: beanBoolean\n" +
                " ; \n" +
                "Unexpected: beanByte\n" +
                " ; \n" +
                "Unexpected: beanChar\n" +
                " ; \n" +
                "Unexpected: beanDouble\n" +
                " ; \n" +
                "Unexpected: beanFloat\n" +
                " ; \n" +
                "Unexpected: beanInteger\n" +
                " ; \n" +
                "Unexpected: beanLong\n" +
                " ; \n" +
                "Unexpected: beanShort\n" +
                " expected:<[{}]> but was:<[{\n" +
                "  \"beanInteger\": -2147483648,\n" +
                "  \"beanByte\": -128,\n" +
                "  \"beanChar\": \"\\u0000\",\n" +
                "  \"beanShort\": -32768,\n" +
                "  \"beanLong\": -9223372036854775808,\n" +
                "  \"beanFloat\": 1.4E-45,\n" +
                "  \"beanDouble\": 4.9E-324,\n" +
                "  \"beanBoolean\": false\n" +
                "}]>";
        String expectedActualValue = "{\n" +
                "  \"beanInteger\": -2147483648,\n" +
                "  \"beanByte\": -128,\n" +
                "  \"beanChar\": \"\\u0000\",\n" +
                "  \"beanShort\": -32768,\n" +
                "  \"beanLong\": -9223372036854775808,\n" +
                "  \"beanFloat\": 1.4E-45,\n" +
                "  \"beanDouble\": 4.9E-324,\n" +
                "  \"beanBoolean\": false\n" +
                "}";
        AssertionFailedError expectedException = new AssertionFailedError(expectedMessage, "{}", expectedActualValue);
        assertThrows(sameBeanAs(expectedException).ignoring(is("identityHashCode")), () -> assertThat(actual, sameBeanAs(expected)));
    }

    protected BeanWithPrimitives getBeanWithPrimitivesMinValues() {
        short beanShort = Short.MIN_VALUE;
        boolean beanBoolean = false;
        byte beanByte = Byte.MIN_VALUE;
        char beanChar = Character.MIN_VALUE;
        float beanFloat = Float.MIN_VALUE;
        int beanInt = Integer.MIN_VALUE;
        double beanDouble = Double.MIN_VALUE;
        long beanLong = Long.MIN_VALUE;

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
