package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameContentAsApproved
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

//e2a34f
class KotlinSameContentParameterizedTest {

    companion object {
        @JvmStatic
        fun data(): Array<Array<Any>> = arrayOf(
            arrayOf("case1", "value1"),
            arrayOf("case2", "value2")
        )
    }

    //ec1105
    @ParameterizedTest
    @MethodSource("data")
    fun testPublicParameterizedWorks(name: String, value: String) {
        assertThat(value, sameContentAsApproved<String>().withUniqueId(name))
    }

    //3fe39a
    @ParameterizedTest
    @MethodSource("data")
    fun testPublicParameterizedWorksWithTestInfo(name: String, value: String, testInfo: TestInfo) {
        assertThat(value, sameContentAsApproved<String>(testInfo).withUniqueId(name))
    }
}
