package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.Bean;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.karsaig.approvalcrest.matchers.ChildBeanMatchers.childStringEqualTo;
import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class BeanMatcherCustomSuccessTest extends AbstractBeanMatcherTest {

    @Test
    public void matchesPrimitiveWithCustomMatcher() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean.childString", equalTo("banana")));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchOnPrimitive() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcherError(actual, expected, beanMatcher -> beanMatcher.with("childBean.childString", equalTo("kiwi")), "\n" +
                "Expected: {\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}\n" +
                "and childBean.childString \"kiwi\"\n" +
                "     but: childBean.childString was \"banana\"");
    }

    @Test
    public void matchesFieldWithCustomMatcher() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana").childInteger(2)).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean", childStringEqualTo("banana")));
    }

    @Test
    public void matchesFieldWithChainOfCustomMatchers() {
        ParentBean expected = parent().childBean(child().childString("apple")).parentString("kiwi").build();
        ParentBean actual = parent().childBean(child().childString("banana").childInteger(2)).parentString("strawberry").build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean", childStringEqualTo("banana")).with("parentString", equalTo("strawberry")));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchOnField() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcherError(actual, expected, beanMatcher -> beanMatcher.with("childBean", childStringEqualTo("kiwi")), "\n" +
                "Expected: {\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}\n" +
                "and childBean having string field \"kiwi\"\n" +
                "     but: childBean string field was \"banana\"\n" +
                "{\n" +
                "  \"childString\": \"banana\",\n" +
                "  \"childInteger\": 0\n" +
                "}");
    }

    @Test
    public void matchesItemInCollectionWithCustomMatcher() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple")).addToChildBeanList(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", hasItem(childStringEqualTo("banana"))));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchACollection() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple")).addToChildBeanList(child().childString("banana")).build();

        assertDiagnosingMatcherError(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", hasItem(childStringEqualTo("kiwi"))), "\n" +
                "Expected: {\n" +
                "  \"childBeanMap\": []\n" +
                "}\n" +
                "and childBeanList a collection containing having string field \"kiwi\"\n" +
                "     but: childBeanList mismatches were: [string field was \"apple\", string field was \"banana\"]\n" +
                "[\n" +
                "  {\n" +
                "    \"childString\": \"apple\",\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  {\n" +
                "    \"childString\": \"banana\",\n" +
                "    \"childInteger\": 0\n" +
                "  }\n" +
                "]");
    }

    @Test
    public void matchesItemInMap() {
        ParentBean expected = parent().putToChildBeanMap("key", child().childString("apple")).build();
        ParentBean actual = parent().putToChildBeanMap("key", child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanMap", hasEntry(equalTo("key"), childStringEqualTo("banana"))));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchAMap() {
        ParentBean expected = parent().putToChildBeanMap("key", child().childString("apple")).build();
        ParentBean actual = parent().putToChildBeanMap("key", child().childString("banana")).build();

        assertDiagnosingMatcherError(actual, expected, beanMatcher -> beanMatcher.with("childBeanMap", hasEntry(equalTo("key"), childStringEqualTo("kiwi"))), "\n" +
                "Expected: {\n" +
                "  \"childBeanList\": []\n" +
                "}\n" +
                "and childBeanMap map containing [\"key\"->having string field \"kiwi\"]\n" +
                "     but: childBeanMap map was [<key=ChildBean{childString='banana', childInteger=0}>]\n" +
                "[\n" +
                "  {\n" +
                "    \"key\": {\n" +
                "      \"childString\": \"banana\",\n" +
                "      \"childInteger\": 0\n" +
                "    }\n" +
                "  }\n" +
                "]");
    }

    @Test
    public void failsWhenActualIsNull() {
        Bean expected = bean().build();
        Bean actual = null;

        assertDiagnosingMatcherError(actual, expected, beanMatcher -> beanMatcher.with("string", startsWith("field")), "\n" +
                "Expected: {\n" +
                "  \"integer\": 0\n" +
                "}\n" +
                "and string a string starting with \"field\"\n" +
                "     but: string was null");
    }

    @Disabled
    @Test
    public void matchesPropertyOfItemInCollectionWithCustomMatcher() {
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi").childInteger(5)).addToChildBeanList(child().childString("pear").childInteger(8)).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple").childInteger(6)).addToChildBeanList(child().childString("banana").childInteger(7)).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList.childString", Matchers.oneOf("apple","banana")));
    }
}
