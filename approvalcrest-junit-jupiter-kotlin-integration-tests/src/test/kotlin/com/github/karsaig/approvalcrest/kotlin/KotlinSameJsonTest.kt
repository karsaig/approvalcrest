package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameJsonAsApproved
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.Optional

//6eeaa9
class KotlinSameJsonTest {

    //dac21e
    @Test
    fun runWithDefaultConfigShouldPassWithMatchingApprovedFile() {
        assertThat(getBeanWithPrimitivesMaxValues(), sameJsonAsApproved<BeanWithPrimitives>())
    }

    //fd51ac
    @Test
    fun runWithDefaultConfigShouldFailWithDifferentContentInApprovedFile() {
        assertThrows(sameJsonAsApproved<Any>().withUniqueId("thrown").ignoring(`is`("identityHashCode"))) {
            assertThat(getBeanWithPrimitivesMaxValues(), sameJsonAsApproved<BeanWithPrimitives>())
        }
    }

    companion object {
        @JvmStatic
        fun parameterizedTestCases(): Array<Array<Any>> = arrayOf(
            arrayOf(Optional.empty<Any>()),
            arrayOf(Optional.of(13L)),
            arrayOf(Optional.of("14"))
        )
    }

    //cc5ead
    @ParameterizedTest
    @MethodSource("parameterizedTestCases")
    fun parameterizedTest(input: Any, testInfo: TestInfo) {
        assertThat(input, sameJsonAsApproved<Any>(testInfo))
    }

    private fun getBeanWithPrimitivesMaxValues(): BeanWithPrimitives =
        BeanWithPrimitives.Builder.beanWithPrimitives()
            .beanShort(Short.MAX_VALUE)
            .beanBoolean(false)
            .beanByte(Byte.MAX_VALUE)
            .beanChar(Character.MAX_VALUE)
            .beanFloat(Float.MAX_VALUE)
            .beanInt(Int.MAX_VALUE)
            .beanDouble(Double.MAX_VALUE)
            .beanLong(Long.MAX_VALUE)
            .build()
}
