package com.github.karsaig.approvalcrest.kotlin.matcher

import com.github.karsaig.approvalcrest.jupiter.matcher.MatchersImpl
import com.github.karsaig.approvalcrest.kotlin.KotlinInfoBasedTestMeta
import com.github.karsaig.approvalcrest.kotlin.KotlinTestMeta
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher
import com.github.karsaig.approvalcrest.matcher.TestMetaInformation
import org.junit.jupiter.api.TestInfo

/**
 * Entry point for the matchers available in Approvalcrest.
 */
object Matchers : MatchersImpl() {
    override fun getTestMetaInformation(): TestMetaInformation = KotlinTestMeta()
    override fun getTestMetaInformation(testInfo: TestInfo): KotlinInfoBasedTestMeta = KotlinInfoBasedTestMeta(testInfo)

    /**
     * Creates a [DiagnosingCustomisableMatcher] for [expected] with the result typed as [T].
     *
     * Use this overload when the actual and expected objects have structurally compatible but
     * different class types (e.g. comparing a map value typed as `Any` to a known domain object).
     * Because the types differ, pair this with [DiagnosingCustomisableMatcher.skipClassComparison]:
     *
     * ```kotlin
     * // Instead of: sameBeanAs(expected as Any).skipClassComparison()
     * sameBeanAsType<Any>(expected).skipClassComparison()
     * ```
     *
     * The unchecked cast of [expected] to [T] is intentional — class comparison is expected to be
     * skipped via [DiagnosingCustomisableMatcher.skipClassComparison].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> sameBeanAsType(expected: Any): DiagnosingCustomisableMatcher<T> =
        sameBeanAs(expected as T)
}