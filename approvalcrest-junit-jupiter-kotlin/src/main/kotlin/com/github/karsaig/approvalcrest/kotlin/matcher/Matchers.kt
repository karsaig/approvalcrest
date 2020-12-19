package com.github.karsaig.approvalcrest.kotlin.matcher

import com.github.karsaig.approvalcrest.jupiter.matcher.MatchersImpl
import com.github.karsaig.approvalcrest.kotlin.KotlinInfoBasedTestMeta
import com.github.karsaig.approvalcrest.kotlin.KotlinTestMeta
import com.github.karsaig.approvalcrest.matcher.TestMetaInformation
import org.junit.jupiter.api.TestInfo

/**
 * Entry point for the matchers available in Approvalcrest.
 */
object Matchers : MatchersImpl() {
    override fun getTestMetaInformation(): TestMetaInformation = KotlinTestMeta()
    override fun getTestMetaInformation(testInfo: TestInfo): KotlinInfoBasedTestMeta = KotlinInfoBasedTestMeta(testInfo)
}