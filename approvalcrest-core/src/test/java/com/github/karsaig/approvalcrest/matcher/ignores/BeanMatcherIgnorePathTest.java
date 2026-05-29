package com.github.karsaig.approvalcrest.matcher.ignores;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.testdata.Bean;
import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.testdata.ChildBean;
import com.github.karsaig.approvalcrest.util.PreBuilt;
import com.google.common.collect.Lists;
import org.hamcrest.DiagnosingMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static java.util.function.Function.identity;

public class BeanMatcherIgnorePathTest extends AbstractBeanMatcherTest {

    @Test
    void ignoreStringKeyInMapShouldNotLeaveEmpty() {
        Map<String, String> inputMap = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
            put("key3", "value3");
            put("key4", "value4");
        }};

        Map<String, String> expectedMap = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
            put("key4", "value4");
        }};

        assertDiagnosingMatcher(inputMap, expectedMap, beanMatcher -> beanMatcher.ignoring("key3").skipClassComparison());
        assertDiagnosingMatcher(inputMap, expectedMap, DiagnosingCustomisableMatcher::skipClassComparison, AssertionFailedError.class, thrown -> {
            Assertions.assertEquals("[]: Expected 3 values but got 4", thrown.getMessage());

            String actual = "[\n" +
                    "  {\n" +
                    "    \"key1\": \"value1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key2\": \"value2\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key3\": \"value3\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key4\": \"value4\"\n" +
                    "  }\n" +
                    "]";

            String expected = "[\n" +
                    "  {\n" +
                    "    \"key1\": \"value1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key2\": \"value2\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key4\": \"value4\"\n" +
                    "  }\n" +
                    "]";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "beanInt shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "beanInt shouldn't be present");
        });
    }

    @Test
    void ignoreEveryStringKeyInMapShouldNotLeaveEmpty() {
        Map<String, String> inputMap = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
            put("key3", "value3");
            put("key4", "value4");
        }};

        Map<String, String> expectedMap = new HashMap<>();

        assertDiagnosingMatcher(inputMap, expectedMap, beanMatcher -> beanMatcher.ignoring("key3", "key1", "key4", "key2"));
        assertDiagnosingMatcher(inputMap, expectedMap, identity(), AssertionFailedError.class, thrown -> {
            Assertions.assertEquals("[]: Expected 0 values but got 4", thrown.getMessage());

            String actual = "[\n" +
                    "  {\n" +
                    "    \"key1\": \"value1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key2\": \"value2\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key3\": \"value3\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key4\": \"value4\"\n" +
                    "  }\n" +
                    "]";

            String expected = "[]";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "beanInt shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "beanInt shouldn't be present");
        });
    }

    @Test
    void ignoreStringKeyInMapShouldNotLeaveEmptyApprovedContainingDifferentValue() {
        Map<String, String> inputMap = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
            put("key3", "value3");
            put("key4", "value4");
        }};

        Map<String, String> expectedMap = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
            put("key3", "value5");
            put("key4", "value4");
        }};

        Map<String, String> expectedMapWithoutIgnoredValue = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
            put("key4", "value4");
        }};

        assertDiagnosingMatcher(inputMap, expectedMap, beanMatcher -> beanMatcher.ignoring("key3").skipClassComparison());
        assertDiagnosingMatcher(inputMap, expectedMapWithoutIgnoredValue, beanMatcher -> beanMatcher.ignoring("key3").skipClassComparison());
        assertDiagnosingMatcher(inputMap, expectedMap, DiagnosingCustomisableMatcher::skipClassComparison, AssertionFailedError.class, thrown -> {
            Assertions.assertEquals("[2].key3\n" +
                    "Expected: value5\n" +
                    "     got: value3\n", thrown.getMessage());

            String actual = "[\n" +
                    "  {\n" +
                    "    \"key1\": \"value1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key2\": \"value2\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key3\": \"value3\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key4\": \"value4\"\n" +
                    "  }\n" +
                    "]";

            String expected = "[\n" +
                    "  {\n" +
                    "    \"key1\": \"value1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key2\": \"value2\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key3\": \"value5\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"key4\": \"value4\"\n" +
                    "  }\n" +
                    "]";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "beanInt shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "beanInt shouldn't be present");
        });
    }


    @Test
    void ignoreStringKeyInMapInCollectionShouldNotLeaveEmpty() {
        List<Map<String, BeanWithGeneric<List<Map<String, String>>>>> input = PreBuilt.getComplexListAndMap();

        Map<String, String> innerMap2 = new HashMap<>();
        innerMap2.put("innerKey24", "innerValue24");
        innerMap2.put("innerKey23", "innerValue23");
        innerMap2.put("innerKey22", "innerValue22");
        innerMap2.put("innerKey21", "innerValue21");

        List<Map<String, String>> innerList1 = new ArrayList<>();
        innerList1.add(innerMap2);


        BeanWithGeneric<List<Map<String, String>>> bean1 = new BeanWithGeneric<>("beanString1", innerList1);

        Map<String, String> innerMap3 = new HashMap<>();
        innerMap3.put("innerKey31", "innerValue31");
        innerMap3.put("innerKey32", "innerValue32");
        innerMap3.put("innerKey33", "innerValue33");
        innerMap3.put("innerKey34", "innerValue34");

        Map<String, String> innerMap4 = new HashMap<>();
        innerMap4.put("innerKey41", "innerValue41");
        innerMap4.put("innerKey42", "innerValue42");
        innerMap4.put("innerKey43", "innerValue43");
        innerMap4.put("innerKey44", "innerValue44");

        List<Map<String, String>> innerList2 = new ArrayList<>();
        innerList2.add(innerMap4);
        innerList2.add(innerMap3);

        BeanWithGeneric<List<Map<String, String>>> bean2 = new BeanWithGeneric<>("beanString2", innerList2);

        Map<String, BeanWithGeneric<List<Map<String, String>>>> outerMap1 = new HashMap<>();
        outerMap1.put("outerKey2", bean2);
        outerMap1.put("outerKey1", bean1);


        BeanWithGeneric<List<Map<String, String>>> bean3 = new BeanWithGeneric<>("beanString3", null);

        Map<String, BeanWithGeneric<List<Map<String, String>>>> outerMap2 = new HashMap<>();
        outerMap2.put("outerKey1", bean3);

        List<Map<String, BeanWithGeneric<List<Map<String, String>>>>> result = new ArrayList<>();
        result.add(outerMap2);
        result.add(outerMap1);


        assertDiagnosingMatcher(input, result, beanMatcher -> beanMatcher.withoutSerializingNulls().ignoring("outerKey1.genericValue.innerKey1", "outerKey1.genericValue.innerKey2", "outerKey1.genericValue.innerKey3", "outerKey1.genericValue.innerKey4"));
        assertDiagnosingMatcher(input, result, beanMatcher -> beanMatcher.withoutSerializingNulls(), AssertionFailedError.class, thrown -> {
            Assertions.assertEquals("[0][0].outerKey1\n" +
                    "Unexpected: genericValue\n" +
                    " ; [1][0].outerKey1.genericValue[]: Expected 1 values but got 2", thrown.getMessage());

            String actual = "[\n" +
                    "  [\n" +
                    "    {\n" +
                    "      \"outerKey1\": {\n" +
                    "        \"dummyString\": \"beanString3\",\n" +
                    "        \"genericValue\": [\n" +
                    "          [\n" +
                    "            {\n" +
                    "              \"innerKey1\": \"innerValue51\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey2\": \"innerValue52\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey3\": \"innerValue53\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey4\": \"innerValue54\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  [\n" +
                    "    {\n" +
                    "      \"outerKey1\": {\n" +
                    "        \"dummyString\": \"beanString1\",\n" +
                    "        \"genericValue\": [\n" +
                    "          [\n" +
                    "            {\n" +
                    "              \"innerKey21\": \"innerValue21\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey22\": \"innerValue22\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey23\": \"innerValue23\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey24\": \"innerValue24\"\n" +
                    "            }\n" +
                    "          ],\n" +
                    "          [\n" +
                    "            {\n" +
                    "              \"innerKey1\": \"innerValue1\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey2\": \"innerValue2\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey3\": \"innerValue3\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey4\": \"innerValue4\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"outerKey2\": {\n" +
                    "        \"dummyString\": \"beanString2\",\n" +
                    "        \"genericValue\": [\n" +
                    "          [\n" +
                    "            {\n" +
                    "              \"innerKey41\": \"innerValue41\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey42\": \"innerValue42\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey43\": \"innerValue43\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey44\": \"innerValue44\"\n" +
                    "            }\n" +
                    "          ],\n" +
                    "          [\n" +
                    "            {\n" +
                    "              \"innerKey31\": \"innerValue31\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey32\": \"innerValue32\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey33\": \"innerValue33\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey34\": \"innerValue34\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "]";

            String expected = "[\n" +
                    "  [\n" +
                    "    {\n" +
                    "      \"outerKey1\": {\n" +
                    "        \"dummyString\": \"beanString3\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  [\n" +
                    "    {\n" +
                    "      \"outerKey1\": {\n" +
                    "        \"dummyString\": \"beanString1\",\n" +
                    "        \"genericValue\": [\n" +
                    "          [\n" +
                    "            {\n" +
                    "              \"innerKey21\": \"innerValue21\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey22\": \"innerValue22\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey23\": \"innerValue23\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey24\": \"innerValue24\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"outerKey2\": {\n" +
                    "        \"dummyString\": \"beanString2\",\n" +
                    "        \"genericValue\": [\n" +
                    "          [\n" +
                    "            {\n" +
                    "              \"innerKey41\": \"innerValue41\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey42\": \"innerValue42\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey43\": \"innerValue43\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey44\": \"innerValue44\"\n" +
                    "            }\n" +
                    "          ],\n" +
                    "          [\n" +
                    "            {\n" +
                    "              \"innerKey31\": \"innerValue31\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey32\": \"innerValue32\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey33\": \"innerValue33\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"innerKey34\": \"innerValue34\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "]";


            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "beanInt shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "beanInt shouldn't be present");
        });
    }

    @Test
    void ignoreFieldInRootList() {
        List<ChildBean> actual = Lists.newArrayList(
                child().childString("banana").childInteger(1).build(),
                child().childString("apple").childInteger(2).build());
        List<ChildBean> expected = Lists.newArrayList(
                child().childString("kiwi").childInteger(1).build(),
                child().childString("grape").childInteger(2).build());

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.ignoring("childString").skipClassComparison());
        assertDiagnosingMatcher(actual, expected, DiagnosingCustomisableMatcher::skipClassComparison,
                AssertionFailedError.class, thrown -> {
                    Assertions.assertTrue(thrown.getMessage().contains("childString"),
                            "failure should report childString diff");
                });
    }

    @Test
    void ignoreFieldInNestedRootList() {
        List<List<ChildBean>> actual = Lists.newArrayList(
                Lists.newArrayList(
                        child().childString("banana").childInteger(1).build(),
                        child().childString("apple").childInteger(2).build()),
                Lists.newArrayList(
                        child().childString("cherry").childInteger(3).build()));
        List<List<ChildBean>> expected = Lists.newArrayList(
                Lists.newArrayList(
                        child().childString("kiwi").childInteger(1).build(),
                        child().childString("grape").childInteger(2).build()),
                Lists.newArrayList(
                        child().childString("plum").childInteger(3).build()));

        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.ignoring("childString").skipClassComparison());
        assertDiagnosingMatcher(actual, expected, DiagnosingCustomisableMatcher::skipClassComparison,
                AssertionFailedError.class, thrown -> {
                    Assertions.assertTrue(thrown.getMessage().contains("childString"),
                            "failure should report childString diff");
                });
    }

    // -----------------------------------------------------------------------
    // Cascade-to-empty: ignoring the only value at each level empties every
    // ancestor up to the root.
    // -----------------------------------------------------------------------

    @Test
    void ignoringDeepestFieldInNestedBeanCascadesToEmpty() {
        NestedCascadeRoot actual   = new NestedCascadeRoot(new NestedCascadeLeaf("actual_value"));
        NestedCascadeRoot expected = new NestedCascadeRoot(new NestedCascadeLeaf("expected_value"));

        // With the leaf ignored both sides cascade to {} and match.
        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.ignoring("nested.leaf").skipClassComparison());
        // Without ignoring the values differ.
        assertDiagnosingMatcher(actual, expected, DiagnosingCustomisableMatcher::skipClassComparison,
                AssertionFailedError.class, thrown ->
                        Assertions.assertTrue(thrown.getMessage().contains("leaf"),
                                "failure should report leaf diff"));
    }

    @Test
    void ignoringOnlyFieldInSingleCollectionElementCascadesToEmpty() {
        List<ChildBean> actual   = Lists.newArrayList(child().childString("foo").childInteger(1).build());
        List<ChildBean> expected = Lists.newArrayList(child().childString("bar").childInteger(2).build());

        // Ignoring all fields: every element → {} → removed → both lists [].
        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.ignoring("childString").ignoring("childInteger").skipClassComparison());
        assertDiagnosingMatcher(actual, expected, DiagnosingCustomisableMatcher::skipClassComparison,
                AssertionFailedError.class, thrown ->
                        Assertions.assertTrue(
                                thrown.getMessage().contains("childString") || thrown.getMessage().contains("childInteger"),
                                "failure should report field diffs"));
    }

    @Test
    void ignoringAllBeanFieldsInNestedCollectionCascadesToEmpty() {
        List<ChildBean> innerActual   = Lists.newArrayList(child().childString("foo").childInteger(1).build());
        List<ChildBean> innerExpected = Lists.newArrayList(child().childString("bar").childInteger(2).build());
        List<List<ChildBean>> actual   = new ArrayList<>();
        actual.add(innerActual);
        List<List<ChildBean>> expected = new ArrayList<>();
        expected.add(innerExpected);

        // bean → {} → removed → inner [] → removed → outer [].
        assertDiagnosingMatcher(actual, expected,
                beanMatcher -> beanMatcher.ignoring("childString").ignoring("childInteger").skipClassComparison());
        assertDiagnosingMatcher(actual, expected, DiagnosingCustomisableMatcher::skipClassComparison,
                AssertionFailedError.class, thrown ->
                        Assertions.assertTrue(
                                thrown.getMessage().contains("childString") || thrown.getMessage().contains("childInteger"),
                                "failure should report field diffs"));
    }

    @SuppressWarnings("unused")
    private static class NestedCascadeLeaf {
        private final String leaf;
        NestedCascadeLeaf(String leaf) { this.leaf = leaf; }
    }

    @SuppressWarnings("unused")
    private static class NestedCascadeRoot {
        private final NestedCascadeLeaf nested;
        NestedCascadeRoot(NestedCascadeLeaf nested) { this.nested = nested; }
    }

    @Test
    void ignoredFieldShouldNotAppearInFailureDiagnostic() {
        Bean expected = bean().string("value1").integer(1).build();
        Bean actual = bean().string("value2").integer(2).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.ignoring("string"),
                AssertionFailedError.class, thrown -> {
                    Assertions.assertFalse(thrown.getExpected().getStringRepresentation().contains("string"),
                            "expected side should not contain ignored field name");
                    Assertions.assertFalse(thrown.getActual().getStringRepresentation().contains("string"),
                            "actual side should not contain ignored field name");
                });
    }

    @Test
    void ignoringNullValuedFieldInCollectionElementRemovesEmptyElement() {
        // Bug repro: a collection element whose ONLY field is the ignored field with a null value
        // was previously kept because Gson (without serializeNulls) omitted the null field first,
        // so ignoring() found nothing and returned false — leaving a spurious {} in the array.
        // With serializeNulls=true (now the default) the null field is present, ignoring() removes
        // it, the element becomes {} and is then removed from the array.
        List<SingleField> expected = Lists.newArrayList(
                new SingleField("a"), new SingleField("b"),
                new SingleField("c"), new SingleField("d")
        );
        List<SingleField> actual = Lists.newArrayList(
                new SingleField("a"), new SingleField("b"),
                new SingleField("c"), new SingleField("d"),
                new SingleField(null)   // extra element whose only field is null
        );

        assertDiagnosingMatcher(actual, expected, m -> m.ignoring("value").skipClassComparison());
    }

    @Test
    void withoutSerializingNullsRestoresLegacyBehaviourForNullFieldInCollection() {
        // With withoutSerializingNulls() the null field is stripped by Gson before ignoring runs,
        // so the element is already {} and ignoring() has nothing to remove — the {} stays,
        // the array keeps 5 elements and the assertion fails.
        List<SingleField> expected = Lists.newArrayList(
                new SingleField("a"), new SingleField("b"),
                new SingleField("c"), new SingleField("d")
        );
        List<SingleField> actual = Lists.newArrayList(
                new SingleField("a"), new SingleField("b"),
                new SingleField("c"), new SingleField("d"),
                new SingleField(null)
        );

        assertDiagnosingMatcher(actual, expected,
                m -> m.ignoring("value").withoutSerializingNulls().skipClassComparison(),
                AssertionFailedError.class,
                thrown -> Assertions.assertTrue(
                        thrown.getMessage().contains("Expected 0 values but got 1"),
                        "should report that one element was not removed: " + thrown.getMessage()));
    }

    @SuppressWarnings("unused")
    private static class SingleField {
        private final String value;
        SingleField(String value) { this.value = value; }
    }

    @Test
    void ignoredFieldIsAbsentFromBothSidesOfFailureOutput() {
        // When a field is ignored, its VALUE must not appear in either the expected or actual
        // side of the failure output (the comparison excludes the field entirely).
        Bean expected = bean().string("value-expected").integer(1).build();
        Bean actual   = bean().string("value-actual").integer(2).build();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher.ignoring("string"),
                AssertionFailedError.class, thrown -> {
                    Assertions.assertFalse(thrown.getExpected().getStringRepresentation().contains("value-expected"),
                            "Ignored field 'string' value must not appear in expected side");
                    Assertions.assertFalse(thrown.getActual().getStringRepresentation().contains("value-actual"),
                            "Ignored field 'string' value must not appear in actual side");
                });
    }
}
