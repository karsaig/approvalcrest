package com.github.karsaig.approvalcrest.matcher.ignores;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.util.PreBuilt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        assertDiagnosingMatcher(inputMap, expectedMap, beanMatcher -> beanMatcher.ignoring("key3"));
        assertDiagnosingMatcher(inputMap, expectedMap, identity(), AssertionFailedError.class, thrown -> {
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

        assertDiagnosingMatcher(inputMap, expectedMap, beanMatcher -> beanMatcher.ignoring("key3"));
        assertDiagnosingMatcher(inputMap, expectedMapWithoutIgnoredValue, beanMatcher -> beanMatcher.ignoring("key3"));
        assertDiagnosingMatcher(inputMap, expectedMap, identity(), AssertionFailedError.class, thrown -> {
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


        assertDiagnosingMatcher(input, result, beanMatcher -> beanMatcher.ignoring("outerKey1.genericValue.innerKey1", "outerKey1.genericValue.innerKey2", "outerKey1.genericValue.innerKey3", "outerKey1.genericValue.innerKey4"));
        assertDiagnosingMatcher(input, result, identity(), AssertionFailedError.class, thrown -> {
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
}
