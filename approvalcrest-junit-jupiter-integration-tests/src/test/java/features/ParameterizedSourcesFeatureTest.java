package features;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameContentAsApproved;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import java.util.stream.Stream;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.testdata.TestEnum;
import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

/**
 * Every argument source Jupiter ships leaves the default display name shape {@code [1] ...} in place, and
 * that is the one thing the matcher reads on its own: the {@code TestInfo} overloads pick the leading
 * index up and use it as the unique id, so each case gets its own approved file without the test asking
 * for it. These tests deliberately do <em>not</em> call {@code withUniqueId} — they exist to pin the
 * automatic index, per source kind, for both the JSON and the content matcher.
 */
//312697
public class ParameterizedSourcesFeatureTest {

    //24813a
    @ParameterizedTest
    @ValueSource(strings = {"alpha", "beta"})
    public void valueSourceGetsOneFilePerCaseFromTheAutomaticIndex(String value, TestInfo testInfo) {
        assertThat(beanFor(value), sameJsonAsApproved(testInfo));
    }

    //bd9c2b
    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void valueSourceGetsOneContentFilePerCaseFromTheAutomaticIndex(String value, TestInfo testInfo) {
        assertThat("content of " + value, sameContentAsApproved(testInfo));
    }

    //80d34d
    @ParameterizedTest
    @CsvSource({"case1, val1", "case2, val2"})
    void csvSourceGetsOneFilePerCaseFromTheAutomaticIndex(String name, String value, TestInfo testInfo) {
        assertThat(new BeanOne(name, value), sameJsonAsApproved(testInfo));
    }

    //66fd5f
    @ParameterizedTest
    @EnumSource(value = TestEnum.class, names = {"ONE", "TWO"})
    void enumSourceGetsOneFilePerCaseFromTheAutomaticIndex(TestEnum value, TestInfo testInfo) {
        assertThat(BeanWithGeneric.of("dummy", value), sameJsonAsApproved(testInfo));
    }

    /**
     * {@code null} and empty inputs are about the <em>argument</em>, not the actual object: a null actual
     * goes to the null matcher instead of a file matcher, so the nullable value is wrapped in a bean.
     */
    //7f97e2
    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = "filled")
    void nullAndEmptySourceGetOneFilePerCaseFromTheAutomaticIndex(String value, TestInfo testInfo) {
        assertThat(new BeanOne("dummy", value), sameJsonAsApproved(testInfo));
    }

    //c7c9d0
    @ParameterizedTest
    @ArgumentsSource(TwoBeansProvider.class)
    void argumentsSourceGetsOneFilePerCaseFromTheAutomaticIndex(BeanOne value, TestInfo testInfo) {
        assertThat(value, sameJsonAsApproved(testInfo));
    }

    private static BeanOne beanFor(String value) {
        return new BeanOne("dummy-" + value, "value-" + value);
    }

    static class TwoBeansProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new BeanOne("dummy1", "val1")),
                    Arguments.of(new BeanOne("dummy2", "val2")));
        }
    }
}
