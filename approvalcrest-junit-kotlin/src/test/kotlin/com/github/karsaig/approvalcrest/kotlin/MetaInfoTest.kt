package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert
import com.github.karsaig.approvalcrest.jupiter.matcher.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import java.nio.file.Paths

class MetaInfoTest {
    @Test
    fun testKotlinMetaWithSameBeanAsMatcher() {
        val underTest = KotlinTestMeta()
        val expected = KotlinTestMeta(
            Paths.get("src/test/kotlin/com/github/karsaig/approvalcrest/kotlin"),
            "com.github.karsaig.approvalcrest.kotlin.MetaInfoTest",
            "testKotlinMetaWithSameBeanAsMatcher",
            Paths.get("src/test/resources/approvalcrest")
        )
        MatcherAssert.assertThat(underTest, Matchers.sameBeanAs(expected))
    }

    @Test
    fun testKotlinInfoBasedMetaWithSameBeanAsMatcher(testInfo: TestInfo) {
        val underTest = KotlinInfoBasedTestMeta(testInfo)
        val expected = KotlinInfoBasedTestMeta(
            Paths.get("src/test/kotlin/com/github/karsaig/approvalcrest/kotlin"),
            "com.github.karsaig.approvalcrest.kotlin.MetaInfoTest",
            "testKotlinInfoBasedMetaWithSameBeanAsMatcher",
            Paths.get("src/test/resources/approvalcrest")
        )
        MatcherAssert.assertThat(underTest, Matchers.sameBeanAs(expected))
    }
}