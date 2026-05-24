package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.testdata.Bean;
import com.github.karsaig.approvalcrest.testdata.ParentBean;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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

    // -----------------------------------------------------------------------
    // with(Matcher<String>, Matcher<V>) — pattern-based custom matchers
    // -----------------------------------------------------------------------

    @Test
    public void matchesNestedFieldWithPatternMatcher() {
        // Pattern "childString" matches childBean.childString — the actual value "banana" passes.
        // Expected has "apple" which would fail structurally; the pattern matcher rescues it.
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.withMatcher(equalTo("childString"), equalTo("banana")));
    }

    @Test
    public void matchesTopLevelFieldWithPatternMatcher() {
        // Pattern "parentString" matches parentString at the top level of ParentBean.
        ParentBean expected = parent().childBean(child().childString("apple")).parentString("hello").build();
        ParentBean actual = parent().childBean(child().childString("apple")).parentString("world").build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.withMatcher(equalTo("parentString"), equalTo("world")));
    }

    @Test
    public void matchesWithChainOfPatternMatchers() {
        // Chain two pattern matchers: one for childString, one for parentString.
        ParentBean expected = parent().childBean(child().childString("apple")).parentString("hello").build();
        ParentBean actual = parent().childBean(child().childString("banana")).parentString("world").build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .withMatcher(equalTo("childString"), equalTo("banana"))
                .withMatcher(equalTo("parentString"), equalTo("world")));
    }

    @Test
    public void patternMatcherCombinedWithPathMatcher() {
        // Path-based matcher handles parentString; pattern-based handles all "childString" fields.
        ParentBean expected = parent().childBean(child().childString("apple")).parentString("hello").build();
        ParentBean actual = parent().childBean(child().childString("banana")).parentString("world").build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .with("parentString", equalTo("world"))
                .withMatcher(equalTo("childString"), equalTo("banana")));
    }

    @Test
    public void patternMatcherMatchesMultipleFieldsAtDifferentLevels() {
        // Pattern "childString" matches childBean.childString AND each element in childBeanList.
        // Both actual values are "banana" → both pass equalTo("banana").
        ParentBean expected = parent()
                .childBean(child().childString("apple"))
                .addToChildBeanList(child().childString("apple"))
                .build();
        ParentBean actual = parent()
                .childBean(child().childString("banana"))
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withMatcher(equalTo("childString"), equalTo("banana")));
    }

    @Test
    public void patternMatcherPassesVacuouslyWhenNoFieldsMatch() {
        // Pattern "nonExistentField" matches nothing → vacuous pass; structural comparison decides.
        ParentBean expected = parent().childBean(child().childString("banana")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withMatcher(equalTo("nonExistentField"), equalTo("anything")));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchOnPrimitive() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean.childString", equalTo("kiwi")), "\n" +
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
    public void matchesWithThreeIndependentCustomMatchers() {
        // expected and actual differ on all three paths.  Removing any one matcher would leave
        // that path in the structural comparison and the differing value would cause a failure.
        // childBean.childInteger also exercises the int/Long JSON-fallback bridge (Integer vs Long).
        ParentBean expected = parent()
                .childBean(child().childString("kiwi").childInteger(5))
                .parentString("apple")
                .build();
        ParentBean actual = parent()
                .childBean(child().childString("banana").childInteger(9))
                .parentString("strawberry")
                .build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .with("childBean.childString", equalTo("banana"))
                .with("childBean.childInteger", equalTo(9L))
                .with("parentString", equalTo("strawberry")));
    }

    @Test
    public void failsWhenCustomMatcherDoesNotMatchOnField() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean", childStringEqualTo("kiwi")), "\n" +
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

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList", hasItem(childStringEqualTo("kiwi"))), "\n" +
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

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanMap", hasEntry(equalTo("key"), childStringEqualTo("kiwi"))), "\n" +
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

        assertDiagnosingErrorMatcher(actual, expected, beanMatcher -> beanMatcher.with("string", startsWith("field")), "\n" +
                "Expected: {\n" +
                "  \"integer\": 0\n" +
                "}\n" +
                "and string a string starting with \"field\"\n" +
                "     but: string was null");
    }

    @Test
    public void matchesIntFieldViaJsonFallback() {
        // Bean path for an int/Integer field returns Integer(0); equalTo(0L) fails because
        // Integer != Long.  The JSON fallback returns Long(0), which passes the matcher.
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("apple")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean.childInteger", equalTo(0L)));
    }

    @Test
    public void matchesPrimitiveWithCustomMatcherRescuingDifferentValue() {
        // expected has "kiwi", actual has "banana".  Without the custom matcher the structural
        // comparison would see kiwi != banana and fail.  With the custom matcher the field is
        // filtered from both sides before comparison, so only the matching parts are compared.
        ParentBean expected = parent().childBean(child().childString("kiwi")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBean.childString", equalTo("banana")));
    }

    @Test
    public void matchesDeeplyNestedFieldPath() {
        // 3-level deep path box.item.value is verified by the custom matcher; box.label and name
        // are compared structurally.
        BeanContainer.BeanBox.BeanItem item = new BeanContainer.BeanBox.BeanItem();
        item.value = "deepValue";
        BeanContainer.BeanBox box = new BeanContainer.BeanBox();
        box.label = "box1";
        box.item = item;
        BeanContainer expected = new BeanContainer();
        expected.name = "container";
        expected.box = box;

        BeanContainer.BeanBox.BeanItem actualItem = new BeanContainer.BeanBox.BeanItem();
        actualItem.value = "deepValue";
        BeanContainer.BeanBox actualBox = new BeanContainer.BeanBox();
        actualBox.label = "box1";
        actualBox.item = actualItem;
        BeanContainer actual = new BeanContainer();
        actual.name = "container";
        actual.box = actualBox;

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("box.item.value", equalTo("deepValue")));
    }

    // 3-level nested data structure used by matchesDeeplyNestedFieldPath
    static class BeanContainer {
        String name;
        BeanBox box;

        static class BeanBox {
            String label;
            BeanItem item;

            static class BeanItem {
                String value;
            }
        }
    }

    @Test
    public void matchesPropertyOfItemInCollectionWithCustomMatcher() {
        // childString values differ between expected and actual but are handled by the custom matcher.
        // Only childInteger is compared structurally, so it must match.
        ParentBean expected = parent().addToChildBeanList(child().childString("kiwi")).addToChildBeanList(child().childString("pear")).build();
        ParentBean actual = parent().addToChildBeanList(child().childString("apple")).addToChildBeanList(child().childString("banana")).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.with("childBeanList.childString", Matchers.oneOf("apple","banana")));
    }

    /**
     * Verifies transparent fanout through TWO nested collection levels.
     * Path "parentBeans.childBeanList.childString" traverses:
     *   List&lt;ParentBean&gt; → List&lt;ChildBean&gt; → childString
     * Every leaf value must satisfy the custom matcher.
     */
    @Test
    public void matchesPathThroughNestedCollections() {
        ParentBean p1 = parent()
                .addToChildBeanList(child().childString("apple"))
                .addToChildBeanList(child().childString("apple"))
                .build();
        ParentBean p2 = parent()
                .addToChildBeanList(child().childString("apple"))
                .build();

        NestedCollectionWrapper expected = new NestedCollectionWrapper(Arrays.asList(p1, p2));
        NestedCollectionWrapper actual   = new NestedCollectionWrapper(Arrays.asList(p1, p2));

        // All leaf childString values are "apple" → all must pass the matcher
        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("parentBeans.childBeanList.childString", equalTo("apple")));
    }

    @Test
    public void matchesPathThroughCollectionContainingNullElement() {
        // childBeanList contains a null element; the custom matcher uses anyOf so both
        // null and "banana" pass — confirming no NPE occurs on the null element.
        ParentBean expected = parent()
                .addToChildBeanList((com.github.karsaig.approvalcrest.testdata.ChildBean) null)
                .addToChildBeanList(child().childString("banana"))
                .build();
        ParentBean actual = parent()
                .addToChildBeanList((com.github.karsaig.approvalcrest.testdata.ChildBean) null)
                .addToChildBeanList(child().childString("banana"))
                .build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.with("childBeanList.childString", org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.nullValue(), equalTo("banana"))));
    }

    static class NestedCollectionWrapper {
        List<ParentBean> parentBeans;
        NestedCollectionWrapper(List<ParentBean> parentBeans) {
            this.parentBeans = parentBeans;
        }
    }

    @Test
    public void patternMatcherPassesVacuouslyWhenNullFieldAbsentFromSerializedJson() {
        // Gson omits null fields during serialisation, so a pattern that names a null field finds
        // nothing in the JSON element and passes vacuously. This is the intended behaviour, and is
        // distinct from the path-based with() which uses bean reflection and can find null fields.
        ParentBean actual   = parent().build();   // childBean == null → omitted from JSON
        ParentBean expected = parent().build();

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.withMatcher(equalTo("childBean"), notNullValue()));
    }

    @Test
    public void toStringReturnsSameBeanAsMatcherForBeanInput() {
        DiagnosingCustomisableMatcher<Object> underTest = MATCHER_FACTORY.beanMatcher(Bean.Builder.bean().build());
        org.junit.jupiter.api.Assertions.assertEquals("SameBeanAs matcher", underTest.toString());
    }

    @Test
    public void toStringReturnsSameBeanAsNullMatcherForNullInput() {
        DiagnosingCustomisableMatcher<Object> underTest = MATCHER_FACTORY.beanMatcher(null);
        org.junit.jupiter.api.Assertions.assertEquals("SameBeanAs null matcher", underTest.toString());
    }

    @Test
    public void toStringReturnsSameBeanAsEqualsMatcherForPrimitiveInput() {
        DiagnosingCustomisableMatcher<Object> underTest = MATCHER_FACTORY.beanMatcher("hello");
        org.junit.jupiter.api.Assertions.assertEquals("SameBeanAs equals matcher", underTest.toString());
    }
}
