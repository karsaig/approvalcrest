package samejson;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests change shape over their lifetime: a `TestInfo` parameter gets added, visibility is reduced,
 * a display name is introduced. None of that should move the approved file — otherwise a harmless
 * refactor silently orphans a golden master and the next run writes a second one.
 *
 * <p>Each test asserts through both routes against a single approved file. A divergence makes the
 * second assertion look for a file that does not exist, so it fails rather than quietly passing.
 */
public class RouteEvolutionTest {

    private static BeanOne bean() {
        return new BeanOne("dummy", "value");
    }

    /** A public test that has since gained a TestInfo parameter keeps its file. */
    @Test
    public void publicTestUsesOneFileForBothRoutes(TestInfo testInfo) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    /** The same, after the method was reduced to package-private visibility. */
    @Test
    void packagePrivateTestUsesOneFileForBothRoutes(TestInfo testInfo) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    /** Adding a display name must not move the file. */
    @Test
    @DisplayName("a display name that looks nothing like the method name")
    void displayNameDoesNotMoveTheFile(TestInfo testInfo) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    @Nested
    class NestedTests {

        @Test
        void nestedTestUsesOneFileForBothRoutes(TestInfo testInfo) {
            assertThat(bean(), sameJsonAsApproved());
            assertThat(bean(), sameJsonAsApproved(testInfo));
        }
    }
}
