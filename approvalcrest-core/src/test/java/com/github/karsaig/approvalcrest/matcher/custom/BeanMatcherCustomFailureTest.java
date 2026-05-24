package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.karsaig.approvalcrest.matchers.ChildBeanMatchers.childStringEqualTo;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.core.IsEqual.equalTo;

public class BeanMatcherCustomFailureTest extends AbstractBeanMatcherTest {

    @Test
    public void includesDescriptionAndMismatchDescriptionForFailingMatcherOnPrimiteField() {
        ParentBean expectedBean = parent().childBean(child().childString("apple")).build();
        ParentBean inputBean = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(inputBean, expectedBean, sameBeanAs -> sameBeanAs.with("childBean.childString", equalTo("kiwi")), AssertionError.class, thrown -> {
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"childBean\": {\n" +
                    "    \"childInteger\": 0\n" +
                    "  },\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}\n" +
                    "and childBean.childString \"kiwi\"\n" +
                    "     but: childBean.childString was \"banana\"", thrown.getMessage());
        });
    }


    @Test
    public void includesJsonSnippetOfNonPrimitiveFieldOnMatchFailure() {
        ParentBean expectedBean = parent().childBean(child().childString("apple")).build();
        ParentBean inputBean = parent().childBean(child().childString("banana").childInteger(1)).build();

        assertDiagnosingMatcher(inputBean, expectedBean, sameBeanAs -> sameBeanAs.with("childBean", childStringEqualTo("kiwi")), AssertionError.class, thrown -> {
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}\n" +
                    "and childBean having string field \"kiwi\"\n" +
                    "     but: childBean string field was \"banana\"\n" +
                    "{\n" +
                    "  \"childString\": \"banana\",\n" +
                    "  \"childInteger\": 1\n" +
                    "}", thrown.getMessage());
        });
    }


    @Test
    public void doesNotIncludeJsonSnippetOnNullField() {
        ParentBean expectedBean = parent().childBean(child().childString("apple")).build();
        ParentBean inputBean = parent().build();

        assertDiagnosingMatcher(inputBean, expectedBean, sameBeanAs -> sameBeanAs.with("childBean", childStringEqualTo("kiwi")), AssertionError.class, thrown -> {
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}\n" +
                    "and childBean having string field \"kiwi\"\n" +
                    "     but: childBean was null", thrown.getMessage());
        });
    }

    @Test
    public void throwsIllegalArgumentExceptionWhenFieldPathDoesNotExist() {
        ParentBean expectedBean = parent().childBean(child().childString("apple")).build();
        ParentBean inputBean = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(inputBean, expectedBean, sameBeanAs -> sameBeanAs.with("childBean.nonExistingField", equalTo("kiwi")), IllegalArgumentException.class, thrown -> {
            Assertions.assertEquals("childBean.nonExistingField does not exist", thrown.getMessage());
        });
    }


    @Test
    public void failsWhenIntFieldMatcherDoesNotMatchValueEvenViaJsonFallback() {
        // Bean path: Integer(0), equalTo(5L) → false.  JSON fallback: Long(0), equalTo(5L) → still false.
        // The failure report preserves the original bean-path value (Integer 0) for the mismatch message.
        ParentBean expectedBean = parent().childBean(child().childString("apple")).build();
        ParentBean inputBean = parent().childBean(child().childString("apple")).build();

        assertDiagnosingMatcher(inputBean, expectedBean, sameBeanAs -> sameBeanAs.with("childBean.childInteger", equalTo(5L)), AssertionError.class, thrown -> {
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"childBean\": {\n" +
                    "    \"childString\": \"apple\"\n" +
                    "  },\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}\n" +
                    "and childBean.childInteger <5L>\n" +
                    "     but: childBean.childInteger was <0>", thrown.getMessage());
        });
    }

    @Test
    public void doesNotIncludeParentBeanFromFieldPath() {
        ParentBean expectedBean = parent().childBean(child().childString("apple")).build();
        ParentBean inputBean = parent().build();


        assertDiagnosingMatcher(inputBean, expectedBean, sameBeanAs -> sameBeanAs.with("childBean.childString", equalTo("apple")), AssertionError.class, thrown -> {
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"childBean\": {\n" +
                    "    \"childInteger\": 0\n" +
                    "  },\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}\n" +
                    "and childBean.childString \"apple\"\n" +
                    "     but: childBean is null", thrown.getMessage());
        });
    }

    @Test
    public void failsWhenPathThroughEmptyCollectionIsUsed() {
        // When childBeanList is empty there are no values at childBeanList.childString to
        // validate against.  The custom matcher must NOT silently pass (vacuous truth).
        ParentBean expectedBean = parent().build();
        ParentBean inputBean   = parent().build();

        assertDiagnosingMatcher(inputBean, expectedBean, sameBeanAs -> sameBeanAs.with("childBeanList.childString", equalTo("apple")), AssertionError.class, thrown -> {
            Assertions.assertTrue(
                    thrown.getMessage().contains("childBeanList.childString"),
                    "Expected mismatch mentioning the path, got: " + thrown.getMessage());
        });
    }

    @Test
    public void reportsOnlyFirstFailureWhenBothMatchersFail() {
        // Both matchers fail; only the first (HashMap iteration order) should appear in mismatch.
        ParentBean actual   = parent().childBean(child().childString("banana").childInteger(0)).build();
        ParentBean expected = parent().childBean(child().childString("kiwi").childInteger(99)).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher
                        .with("childBean.childString", equalTo("kiwi"))
                        .with("childBean.childInteger", equalTo(99L)),
                AssertionError.class, error -> {
                    String msg = error.getMessage();
                    String mismatchSection = msg.contains("but:") ? msg.substring(msg.lastIndexOf("but:")) : msg;
                    Assertions.assertFalse(
                            mismatchSection.contains("childBean.childString") && mismatchSection.contains("childBean.childInteger"),
                            "Only first failure should be reported in mismatch section, was: " + msg);
                });
    }

    // -----------------------------------------------------------------------
    // with(Matcher<String>, Matcher<V>) — pattern-based failures
    // -----------------------------------------------------------------------

    @Test
    public void patternMatcherFailsWhenMatchedFieldValueDoesNotMatch() {
        // Pattern "childString" matches childBean.childString; actual is "banana" but matcher expects "kiwi".
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withMatcher(equalTo("childString"), equalTo("kiwi")),
                AssertionError.class, error -> {
                    String msg = error.getMessage();
                    Assertions.assertTrue(
                            msg.contains("was \"banana\""),
                            "Expected mismatch mentioning actual value, was: " + msg);
                });
    }

    @Test
    public void patternMatcherFailsOnFirstNonMatchingFieldWhenMultipleMatch() {
        // Pattern "childString" matches both childBean.childString and childBeanList[0].childString.
        // First matched value is "banana", matcher expects "kiwi" → fails on first match.
        ParentBean expected = parent()
                .childBean(child().childString("apple"))
                .addToChildBeanList(child().childString("apple"))
                .build();
        ParentBean actual = parent()
                .childBean(child().childString("banana"))
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withMatcher(equalTo("childString"), equalTo("kiwi")),
                AssertionError.class, error -> {
                    String msg = error.getMessage();
                    Assertions.assertTrue(
                            msg.contains("was \"banana\""),
                            "Expected mismatch on matched field value, was: " + msg);
                });
    }

    @Test
    public void patternMatcherMessageIncludesPatternDescription() {
        // The pattern's description (from appendDescriptionOf) must appear in the failure message.
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withMatcher(equalTo("childString"), equalTo("kiwi")),
                AssertionError.class, error -> {
                    String msg = error.getMessage();
                    Assertions.assertTrue(
                            msg.contains("childString"),
                            "Expected pattern description 'childString' in message, was: " + msg);
                    Assertions.assertTrue(
                            msg.contains("was \"banana\""),
                            "Expected actual value in message, was: " + msg);
                });
    }

}
