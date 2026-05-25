package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameBeanAs
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import java.util.Optional

class KotlinSameBeanTest {

    @Test
    fun runWithDefaultConfigShouldPassWithMatchingExpectation() {
        val actual = getBeanWithPrimitivesMinValues()
        val expected = getBeanWithPrimitivesMinValues()
        assertThat(actual, sameBeanAs(expected))
    }

    @Test
    fun runWithDefaultConfigShouldFailWithDifferentExpectation() {
        val actual = getBeanWithPrimitivesMinValues()
        val expected = Optional.empty<BeanWithPrimitives>()
        val expectedMessage = "\n" +
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
                "Unexpected: beanShort\n"
        val expectedActualValue = "{\n" +
                "  \"beanBoolean\": false,\n" +
                "  \"beanByte\": -128,\n" +
                "  \"beanChar\": \"\\u0000\",\n" +
                "  \"beanDouble\": 4.9E-324,\n" +
                "  \"beanFloat\": 1.4E-45,\n" +
                "  \"beanInteger\": -2147483648,\n" +
                "  \"beanLong\": -9223372036854775808,\n" +
                "  \"beanShort\": -32768\n" +
                "}"
        val expectedException = AssertionFailedError(expectedMessage, "{}", expectedActualValue)
        assertThrows(sameBeanAs(expectedException).ignoring(`is`("identityHashCode"))) {
            val matcher: DiagnosingCustomisableMatcher<Any> = sameBeanAs(expected)
            matcher.skipClassComparison()
            assertThat(actual, matcher)
        }
    }

    private fun getBeanWithPrimitivesMinValues(): BeanWithPrimitives =
        BeanWithPrimitives.Builder.beanWithPrimitives()
            .beanShort(Short.MIN_VALUE)
            .beanBoolean(false)
            .beanByte(Byte.MIN_VALUE)
            .beanChar(Character.MIN_VALUE)
            .beanFloat(Float.MIN_VALUE)
            .beanInt(Int.MIN_VALUE)
            .beanDouble(Double.MIN_VALUE)
            .beanLong(Long.MIN_VALUE)
            .build()
}
