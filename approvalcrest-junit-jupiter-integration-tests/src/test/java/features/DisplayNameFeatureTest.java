package features;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

/**
 * Display names are for humans and must not decide where an approved file lives — renaming a test in the
 * report would otherwise orphan its golden master. Each test here asserts through both routes against a
 * single file, so a divergence fails instead of quietly creating a second file.
 *
 * <p>The exception is the automatic index: it is read off the display name, so a custom
 * {@code @ParameterizedTest} name pattern that does not start with {@code [1]} switches it off, and per
 * case files then need an explicit unique id.
 */
@DisplayName("a class display name that looks nothing like the class name")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
//6d39fa
public class DisplayNameFeatureTest {

    //d239b8
    @Test
    @DisplayName("a method display name that looks nothing like the method name")
    public void an_explicit_display_name_does_not_move_the_file(TestInfo testInfo) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    //af086d
    @Test
    void a_generated_display_name_does_not_move_the_file(TestInfo testInfo) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    //d31a0f
    @ParameterizedTest(name = "case {0}")
    @ValueSource(strings = {"alpha", "beta"})
    void a_custom_name_pattern_switches_the_automatic_index_off(String value, TestInfo testInfo) {
        BeanOne actual = new BeanOne("dummy-" + value, "value-" + value);

        assertThat(actual, sameJsonAsApproved(testInfo).withUniqueId(value));
        assertThat(actual, sameJsonAsApproved().withUniqueId(value));
    }

    private static BeanOne bean() {
        return new BeanOne("dummy", "value");
    }
}
