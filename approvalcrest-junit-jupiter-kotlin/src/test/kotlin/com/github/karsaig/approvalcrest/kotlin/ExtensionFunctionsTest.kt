package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers
import com.github.karsaig.approvalcrest.kotlin.matcher.alsoCheck
import com.github.karsaig.approvalcrest.kotlin.matcher.alsoCheckMatching
import com.github.karsaig.approvalcrest.kotlin.matcher.ignoringElementsWhere
import com.github.karsaig.approvalcrest.kotlin.matcher.sortType
import com.github.karsaig.approvalcrest.kotlin.matcher.withMachineReadableOutput
import com.github.karsaig.approvalcrest.kotlin.matcher.withoutSerializingNulls
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

/**
 * Verifies that all Kotlin extension functions for DiagnosingCustomisableMatcher, JsonMatcher,
 * and ContentMatcher compile correctly and can be arbitrarily chained (KT-5464 workaround).
 *
 * The key scenarios:
 *  - Chaining methods that are defined on the F-bounded CustomisableMatcher interface
 *    (withMachineReadableOutput, withoutSerializingNulls, sortType) — these require Kotlin
 *    extension functions to resolve the return type correctly.
 *  - sameBeanAsType<T>(Any) factory — avoids the `as Any` cast for cross-type comparison.
 */
class ExtensionFunctionsTest {

    private data class TestBean(val name: String = "test", val value: Int = 42)

    @Test
    fun alsoCheckCanBeChainedOnBeanMatcher() {
        val expected = TestBean()
        val actual = TestBean()
        val matcher = Matchers.sameBeanAs(expected)
            .alsoCheck("value", equalTo(42))
            .withMachineReadableOutput()
        assertNotNull(matcher)
        assertThat(actual, matcher)
    }

    @Test
    fun alsoCheckMatchingCanBeChainedOnBeanMatcher() {
        val expected = TestBean()
        val actual = TestBean()
        val matcher = Matchers.sameBeanAs(expected)
            .alsoCheckMatching(equalTo("name"), equalTo("test"))
            .withMachineReadableOutput()
        assertNotNull(matcher)
        assertThat(actual, matcher)
    }

    @Test
    fun withMachineReadableOutputCanBeChainedOnBeanMatcher() {
        val expected = TestBean()
        val actual = TestBean()
        val matcher = Matchers.sameBeanAs(expected).withMachineReadableOutput()
        assertNotNull(matcher)
        assertThat(actual, matcher)
    }

    @Test
    fun withoutSerializingNullsCanBeChainedOnBeanMatcher() {
        val expected = TestBean()
        val actual = TestBean()
        val matcher = Matchers.sameBeanAs(expected).withoutSerializingNulls()
        assertNotNull(matcher)
        assertThat(actual, matcher)
    }

    @Test
    fun sortTypeCanBeChainedOnBeanMatcher() {
        val expected = TestBean()
        val actual = TestBean()
        val matcher = Matchers.sameBeanAs(expected).sortType(String::class.java)
        assertNotNull(matcher)
        assertThat(actual, matcher)
    }

    @Test
    fun arbitraryChainOfExtensionsCompilesAndWorksOnBeanMatcher() {
        val expected = TestBean()
        val actual = TestBean()
        // Arbitrary chain: mix of interface-based extensions and direct member calls
        val matcher = Matchers.sameBeanAs(expected)
            .withoutSerializingNulls()
            .withMachineReadableOutput()
            .sortType(String::class.java)
            .ignoring("value")
        assertNotNull(matcher)
        assertThat(actual, matcher)
    }

    @Test
    fun withMachineReadableOutputCanBeChainedOnJsonMatcher() {
        // Compile-time check: extension resolves to JsonMatcher<T>, not a star-projection
        val matcher = Matchers.sameJsonAsApproved<TestBean>().withMachineReadableOutput()
        assertNotNull(matcher)
    }

    @Test
    fun withoutSerializingNullsCanBeChainedOnJsonMatcher() {
        val matcher = Matchers.sameJsonAsApproved<TestBean>().withoutSerializingNulls()
        assertNotNull(matcher)
    }

    @Test
    fun sortTypeCanBeChainedOnJsonMatcher() {
        val matcher = Matchers.sameJsonAsApproved<TestBean>().sortType(String::class.java)
        assertNotNull(matcher)
    }

    @Test
    fun ignoringElementsWhereCanBeChainedOnBeanMatcher() {
        val actual = MetaBean(listOf(Tag("tracking", "flow-1"), Tag("real", "keep")))
        val expected = MetaBean(listOf(Tag("real", "keep")))
        val matcher = Matchers.sameBeanAs(expected)
            .ignoringElementsWhere("tag.system", equalTo("tracking"))
        assertNotNull(matcher)
        assertThat(actual, matcher)
    }

    @Test
    fun ignoringElementsWhereStringFormCanBeChainedOnJsonMatcher() {
        val matcher = Matchers.sameJsonAsApproved<MetaBean>()
            .ignoringElementsWhere("tag.system", "tracking")
        assertNotNull(matcher)
    }

    private data class Tag(val system: String, val code: String)
    private data class MetaBean(val tag: List<Tag>)

    @Test
    fun withMachineReadableOutputCanBeChainedOnContentMatcher() {
        val matcher = Matchers.sameContentAsApproved<TestBean>().withMachineReadableOutput()
        assertNotNull(matcher)
    }

    @Test
    fun sameBeanAsTypeFactoryEliminatesAsAnyCast(testInfo: TestInfo) {
        val input1 = KotlinTestMeta()
        val input2 = KotlinInfoBasedTestMeta(testInfo)

        // Previously required: sameBeanAs(input2 as Any).skipClassComparison() — mutation, no chain
        // Now: sameBeanAsType<Any>(input2) — no `as Any` cast, chains cleanly
        val matcher = Matchers.sameBeanAsType<Any>(input2).skipClassComparison()
        assertNotNull(matcher)
        assertThat(input1, matcher)
    }
}
