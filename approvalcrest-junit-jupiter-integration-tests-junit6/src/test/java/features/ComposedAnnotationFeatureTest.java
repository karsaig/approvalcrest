package features;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

/**
 * Jupiter treats an annotation that is itself annotated with {@code @Test} as a test annotation, however
 * many hops away it is. The stack-trace route has to do the same crawl, otherwise a project that hides
 * {@code @Test} behind its own annotation cannot use the no-argument matchers at all.
 */
//edfcfa
public class ComposedAnnotationFeatureTest {

    //2efde6
    @Test
    void aPlainTestIsTheBaselineForTheComposedOnes(TestInfo testInfo) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    //86ba93
    @ApprovedJsonTest
    void aComposedAnnotationIsResolvedByBothRoutes(TestInfo testInfo) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    //f107d1
    @ThoroughApprovedJsonTest
    void aTwoHopComposedAnnotationIsResolvedByBothRoutes(TestInfo testInfo) {
        assertThat(bean(), sameJsonAsApproved());
        assertThat(bean(), sameJsonAsApproved(testInfo));
    }

    private static BeanOne bean() {
        return new BeanOne("dummy", "value");
    }

    /** One hop away from {@code @Test}. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Test
    @interface ApprovedJsonTest {
    }

    /** Two hops away from {@code @Test}, through {@link ApprovedJsonTest}. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @ApprovedJsonTest
    @interface ThoroughApprovedJsonTest {
    }
}
