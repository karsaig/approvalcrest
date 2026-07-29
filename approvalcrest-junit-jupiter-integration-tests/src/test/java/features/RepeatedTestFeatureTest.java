package features;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInfo;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

/**
 * {@code @RepeatedTest} runs one method several times, so a single class and method name has to map to
 * several approved files. {@code RepetitionInfo} plus {@code withUniqueId} is the way to get one file per
 * repetition; the repetition number never reaches the matcher on its own, because the default display
 * name is {@code repetition 1 of 3} and the automatic index only fires for the {@code [1] ...} shape.
 *
 * <p>{@code @RepeatedTest} is meta-annotated with {@code @TestTemplate}, so the stack-trace route
 * recognises it without a {@code TestInfo} parameter.
 */
//f3fedc
public class RepeatedTestFeatureTest {

    //05b2e1
    @RepeatedTest(3)
    public void repeatedTestGetsOneFilePerRepetition(RepetitionInfo repetitionInfo) {
        int repetition = repetitionInfo.getCurrentRepetition();

        assertThat(beanFor(repetition), sameJsonAsApproved().withUniqueId("rep" + repetition));
    }

    //8864b8
    @RepeatedTest(2)
    void repeatedTestResolvesTheSameFileFromBothRoutes(RepetitionInfo repetitionInfo, TestInfo testInfo) {
        int repetition = repetitionInfo.getCurrentRepetition();
        String uniqueId = "rep" + repetition;

        assertThat(beanFor(repetition), sameJsonAsApproved().withUniqueId(uniqueId));
        assertThat(beanFor(repetition), sameJsonAsApproved(testInfo).withUniqueId(uniqueId));
    }

    private static BeanOne beanFor(int repetition) {
        return new BeanOne("dummy" + repetition, "value" + repetition);
    }
}
