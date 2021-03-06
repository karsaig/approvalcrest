package samecontent;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThrows;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameContentAsApproved;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


//f89d26
public class SameContentTest {


    //dac21e
    @Test
    public void runWithDefaultConfigShouldPassWithMatchingApprovedFile() {
        String actual = "Lórem ipsüm dölör sit amet, éu énim íudico lücílíús sit, mel persius vúlpútate pösidoniúm ut. Ne próbo lobörtis salútandi ést, in eum paülö suscipiantur, nihil núsqúam árgümentum nam űt. Error iudico díssentias sea in, úbíque dignissím vim út. Süas índoctum ut meá, qui módüs ídqué at! Te decore menandri vis, has üt éxerci altérüm compréhensám, prő té brüté réfőrmidans.\n" +
                "\n" +
                "Veniám nöstrüd símíliqúé sit cu, viris mediöcrem eu mei, mei pérfecto disputándo intérpretaris eu! Eam ut eűísmód vöcibűs eűripídis, nam libris discére et, iriure eleifend ei mea. At iuvaret omnesqűe assueverit sea. Falli euismod ea quo, ad súavitaté torqúatos pro.";

        assertThat(actual, sameContentAsApproved());
    }

    //fd51ac
    @Test
    public void runWithDefaultConfigShouldFailWithDifferentContentInApprovedFile() {
        String actual = "Lorem ipsum dolor sit amet, inani nullam oportere no cum.";

        assertThrows(sameJsonAsApproved().withUniqueId("thrown").ignoring(is("identityHashCode")), () -> assertThat(actual, sameContentAsApproved()));
    }

    public static Object[][] parameterizedTestCases() {
        return new Object[][]{
                {"Lorem ipsum dolor"},
                {"Árvízűtűrőtükörfúrógép"},
                {" L’apostrophe 用的名字☺\\\\nд1@00000☺☹❤\\\\naA@AA1A猫很可爱\""}
        };
    }

    @ParameterizedTest
    @MethodSource("parameterizedTestCases")
    void parameterizedTest(Object input, TestInfo testInfo) {
        assertThat(input, sameContentAsApproved(testInfo));
    }
}
