package com.github.karsaig.approvalcrest.kotlin.matcher

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

class KotlinMatchersTest {

    @Test
    fun `works with manually provided test info`(testInfo: TestInfo) {
        assertThat(Bean(), Matchers.sameJsonAsApproved(testInfo))
    }

    @Test
    fun `works with discovered test meta info`() {
        assertThat(Bean(), Matchers.sameJsonAsApproved())
    }

    @Suppress("unused")
    class Bean {
        val foo = "foo"
        val bar = false

    }
}