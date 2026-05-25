package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameJsonAsApproved
import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

//fe1cd5
class KotlinSameJsonParameterizedTest {

    companion object {
        @JvmStatic
        fun data(): Array<Array<Any>> = arrayOf(
            arrayOf("case1", BeanOne("dummy1", "val1")),
            arrayOf("case2", BeanOne("dummy2", "val2"))
        )
    }

    //ec1105
    @ParameterizedTest
    @MethodSource("data")
    fun testPublicParameterizedWorks(name: String, value: BeanOne) {
        assertThat(value, sameJsonAsApproved<BeanOne>().withUniqueId(name))
    }

    //3fe39a
    @ParameterizedTest
    @MethodSource("data")
    fun testPublicParameterizedWorksWithTestInfo(name: String, value: BeanOne, testInfo: TestInfo) {
        assertThat(value, sameJsonAsApproved<BeanOne>(testInfo).withUniqueId(name))
    }
}
