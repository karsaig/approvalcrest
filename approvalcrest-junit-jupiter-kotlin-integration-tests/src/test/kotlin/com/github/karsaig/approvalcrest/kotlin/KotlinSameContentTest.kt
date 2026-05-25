package com.github.karsaig.approvalcrest.kotlin

import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat
import com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameContentAsApproved
import com.github.karsaig.approvalcrest.kotlin.matcher.Matchers.sameJsonAsApproved
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

//4e5799
class KotlinSameContentTest {

    //dac21e
    @Test
    fun runWithDefaultConfigShouldPassWithMatchingApprovedFile() {
        val actual = "Lórem ipsüm dölör sit amet, éu éním íudico lücílíús sit, mel persius vúlpútate pösidoniúm ut. " +
                "Ne próbo lobörtis salútandi ést, in eum paülö suscipiantur, nihil núsqúam árgümentum nam űt. " +
                "Error iudico díssentias sea in, úbíque dignissím vim út. Süas índoctum ut meá, qui módüs ídqué at! " +
                "Te decore menandri vis, has üt éxerci altérüm compréhensám, pr té brüté réfőrmidans.\n\n" +
                "Veniám nöstrüd símíliqúé sit cu, viris mediöcrem eu mei, mei pérfecto disputándo intérpretaris eu! " +
                "Eam ut eűísmód vöcibűs eűripídis, nam libris discére et, iriure eleifend ei mea. " +
                "At iuvaret omnesqűe assueverit sea. Falli euismod ea quo, ad súavitaté torqúatos pro."
        assertThat(actual, sameContentAsApproved<String>())
    }

    //fd51ac
    @Test
    fun runWithDefaultConfigShouldFailWithDifferentContentInApprovedFile() {
        val actual = "Lorem ipsum dolor sit amet, inani nullam oportere no cum."
        assertThrows(sameJsonAsApproved<Any>().withUniqueId("thrown").ignoring(`is`("identityHashCode"))) {
            assertThat(actual, sameContentAsApproved<String>())
        }
    }

    companion object {
        @JvmStatic
        fun parameterizedTestCases(): Array<Array<Any>> = arrayOf(
            arrayOf("Lorem ipsum dolor"),
            arrayOf("Árvízűtűrőtükörfúrógép"),
            arrayOf(" L'apostrophe 用的名字☺\\\\nд1@00000☺☹❤\\\\naA@AA1A猫很可爱\"")
        )
    }

    //cc5ead
    @ParameterizedTest
    @MethodSource("parameterizedTestCases")
    fun parameterizedTest(input: Any, testInfo: TestInfo) {
        assertThat(input, sameContentAsApproved<Any>(testInfo))
    }
}
