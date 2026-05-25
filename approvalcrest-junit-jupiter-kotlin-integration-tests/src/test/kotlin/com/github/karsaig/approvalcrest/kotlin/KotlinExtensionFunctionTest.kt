package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameBeanAs
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameContentAsApproved
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameJsonAsApproved
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

/**
 * Verifies that F-bounded (recursive) generic matcher methods can be chained in Kotlin
 * without hitting Kotlin type-inference bug KT-5464.
 *
 * The key scenario from the ticket:
 *   assertThat(result, sameBeanAs(expected).with("property", myCustomMatcher))
 * would previously fail to compile because Kotlin inferred the return type of `.with`
 * as `CustomisableMatcher<*,*>`. Extension functions in DiagnosingCustomisableMatcherExtensions.kt,
 * JsonMatcherExtensions.kt, and ContentMatcherExtensions.kt work around this.
 */
class KotlinExtensionFunctionTest {

    // --- sameBeanAs chaining ---

    /**
     * Demonstrates that `.with(fieldPath, matcher)` compiles and works:
     * the custom matcher replaces the field comparison for "beanInteger".
     */
    @Test
    fun sameBeanAsChainedWithShouldPassWhenCustomMatcherMatches() {
        val actual = buildBean(beanInt = 42)
        val expected = buildBean(beanInt = 99)  // different beanInt, but overridden by .with()
        assertThat(actual, sameBeanAs(expected).with("beanInteger", equalTo(42)))
    }

    /**
     * Demonstrates that `.ignoring(String)` compiles and works:
     * the differing field is excluded from comparison.
     */
    @Test
    fun sameBeanAsChainedIgnoringShouldPassWhenDifferingFieldIgnored() {
        val actual = buildBean(beanInt = 42)
        val expected = buildBean(beanInt = 99)
        assertThat(actual, sameBeanAs(expected).ignoring("beanInteger"))
    }

    /**
     * Demonstrates a longer chain: `.ignoring(Class<?>).with(fieldPath, matcher)`.
     */
    @Test
    fun sameBeanAsMultipleChainedCallsShouldCompileAndPass() {
        val actual = buildBean(beanInt = 42)
        val expected = buildBean(beanInt = 99)
        assertThat(actual, sameBeanAs(expected)
            .ignoring("beanInteger")
            .with("beanShort", anything()))
    }

    // --- sameJsonAsApproved chaining ---

    /**
     * Demonstrates that `.ignoring(String)` on JsonMatcher compiles and works.
     */
    @Test
    fun sameJsonAsApprovedChainedIgnoringShouldPassWithApprovedFile() {
        assertThat(
            getBeanWithPrimitivesMaxValues(),
            sameJsonAsApproved<BeanWithPrimitives>().withUniqueId("ignoring").ignoring("beanInteger")
        )
    }

    /**
     * Demonstrates that `.withUniqueId(String).ignoring(String)` chain compiles on JsonMatcher.
     */
    @Test
    fun sameJsonAsApprovedChainedWithUniqueIdAndIgnoringShouldPass() {
        assertThat(
            getBeanWithPrimitivesMaxValues(),
            sameJsonAsApproved<BeanWithPrimitives>().withUniqueId("chained").ignoring(`is`("beanInteger"))
        )
    }

    // --- sameContentAsApproved chaining ---

    /**
     * Demonstrates that `.withUniqueId(String)` on ContentMatcher compiles and works.
     */
    @Test
    fun sameContentAsApprovedChainedWithUniqueIdShouldPassWithApprovedFile() {
        assertThat("extension-function-test-content", sameContentAsApproved<String>().withUniqueId("ext"))
    }

    // --- helpers ---

    private fun buildBean(beanInt: Int): BeanWithPrimitives =
        BeanWithPrimitives.Builder.beanWithPrimitives()
            .beanShort(1)
            .beanBoolean(false)
            .beanByte(1.toByte())
            .beanChar('a')
            .beanFloat(1.0f)
            .beanInt(beanInt)
            .beanDouble(1.0)
            .beanLong(1L)
            .build()

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
