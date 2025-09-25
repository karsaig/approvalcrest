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
                    "     but: parent bean of childString is null", thrown.getMessage());
        });
    }


}
