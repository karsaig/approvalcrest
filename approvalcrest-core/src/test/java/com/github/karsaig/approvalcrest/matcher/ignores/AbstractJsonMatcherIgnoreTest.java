package com.github.karsaig.approvalcrest.matcher.ignores;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.util.PreBuilt;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.util.HashMap;
import java.util.function.Function;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static java.util.function.Function.identity;

public abstract class AbstractJsonMatcherIgnoreTest extends AbstractFileMatcherTest {

    public static Object[][] subpathWithPrimitives() {
        return new Object[][]{
                {"Object input", parent().childBean(child().childString("banana"))},
                {"Json string input", "{\n" +
                        "  \"childBean\": {\n" +
                        "    \"childString\": \"banana\",\n" +
                        "    \"childInteger\": 0\n" +
                        "  },\n" +
                        "  \"childBeanList\": [],\n" +
                        "  \"childBeanMap\": []\n" +
                        "}"}
        };
    }


    public static Object[][] mapCases() {
        return new Object[][]{
                {"Object input", new HashMap<String, String>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                    put("key3", "value3");
                    put("key4", "value4");
                }}},
                {"Json string input", "[{\"key1\": \"value1\"},{\"key2\": \"value2\"},{\"key3\": \"value3\"},{\"key4\": \"value4\"}]"}
        };
    }


    protected void ignoreStringKeyInMapShouldNotLeaveEmpty(String testName, Object input, Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator) {
        String approvedFileContent = "[{\"key1\": \"value1\"},{\"key2\": \"value2\"},{\"key4\": \"value4\"}]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[]: Expected 3 values but got 4"), thrown.getMessage());

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
        }, AssertionFailedError.class);
    }

    protected void ignoreEveryStringKeyInMapShouldNotLeaveEmpty(String testName, Object input, Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator) {
        String approvedFileContent = "[]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[]: Expected 0 values but got 4"), thrown.getMessage());

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
        }, AssertionFailedError.class);
    }

    void ignoreStringKeyInMapShouldNotLeaveEmptyApprovedContainingDifferentValue(String testName, Object input, Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator) {
        String approvedFileContent = "[{\"key1\": \"value1\"},{\"key2\": \"value2\"},{\"key3\": \"value5\"},{\"key4\": \"value4\"}]";
        String approvedFileContentWithoutIgnoredValue = "[{\"key1\": \"value1\"},{\"key2\": \"value2\"},{\"key4\": \"value4\"}]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContentWithoutIgnoredValue, getDefaultFileMatcherConfigWithLenientMatching(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContentWithoutIgnoredValue, getDefaultFileMatcherConfig(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[2].key3\n" +
                    "Expected: value5\n" +
                    "     got: value3\n"), thrown.getMessage());

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
        }, AssertionFailedError.class);
    }


    public static Object[][] mapInCollectionCases() {
        return new Object[][]{
                {"Object input", PreBuilt.getComplexListAndMap()},
                {"Json string input", PreBuilt.getComplexListAndMapAsJsonString()}
        };
    }

    void ignoreStringKeyInMapInCollectionShouldNotLeaveEmpty(String testName, Object input, Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator) {
        String approvedFileContent = "[\n" +
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[0][0].outerKey1\n" +
                    "Unexpected: genericValue\n" +
                    " ; [1][0].outerKey1.genericValue[]: Expected 1 values but got 2"), thrown.getMessage());

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
        }, AssertionFailedError.class);
    }

    protected void ignoreEveryFieldInBeanShouldNotLeaveEmpty(String testName, Object input, Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator) {
        String approvedFileContent = "{\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), configurator, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("\n" +
                    "Unexpected: childBean\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"childBean\": {\n" +
                    "    \"childInteger\": 0,\n" +
                    "    \"childString\": \"banana\"\n" +
                    "  },\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}";

            String expected = "{\n" +
                    "  \"childBeanList\": [],\n" +
                    "  \"childBeanMap\": []\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "beanInt shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "beanInt shouldn't be present");
        }, AssertionFailedError.class);
    }
}
