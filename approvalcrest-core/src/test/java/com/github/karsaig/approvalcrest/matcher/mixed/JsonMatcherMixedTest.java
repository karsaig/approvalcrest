package com.github.karsaig.approvalcrest.matcher.mixed;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.function.Function.identity;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

public class JsonMatcherMixedTest extends AbstractFileMatcherTest {

    private static Map<String, Object> MAP_CASE = new HashMap<String, Object>() {{
        put("aud", new String[]{"afca9c84-787d-44d6-a537-6560f1712d0f"});
        put("auth_time", 1760351400);
        put("sub", "45d48351-44d6-42b7-82dc-744e2e7b3f7a");
        put("person_public_id", "9fdca323-5873-42d5-8672-0bb513d0f443");
        put("azp", "afca9c84-787d-44d6-a537-6560f1712d0f");
        put("exp", "2025-10-13T11:00:00.000Z");
        put("iat", "2025-10-13T10:30:00.000Z");
        put("iss", "https://t.oauth2.x.com");
        put("fhirUser", "Patient/asd123");
        put("jti", 1782298044);
    }};

    
    private static String MAP_AS_JSON_CASE = "[ {\n" +
            "  \"aud\" : [ \"afca9c84-787d-44d6-a537-6560f1712d0f\" ]\n" +
            "}, {\n" +
            "  \"auth_time\" : 1760351400\n" +
            "}, {\n" +
            "  \"azp\" : \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
            "}, {\n" +
            "  \"exp\" : \"2025-10-13T11:00:00.000Z\"\n" +
            "}, {\n" +
            "  \"fhirUser\" : \"Patient/asd123\"\n" +
            "}, {\n" +
            "  \"iat\" : \"2025-10-13T10:30:00.000Z\"\n" +
            "}, {\n" +
            "  \"iss\" : \"https://t.oauth2.x.com\"\n" +
            "}, {\n" +
            "  \"jti\" : 1782298044\n" +
            "}, {\n" +
            "  \"person_public_id\" : \"9fdca323-5873-42d5-8672-0bb513d0f443\"\n" +
            "}, {\n" +
            "  \"sub\" : \"45d48351-44d6-42b7-82dc-744e2e7b3f7a\"\n" +
            "} ]";

    private static final List<Object> LIST_OF_MAPS_CASE = Collections.singletonList(MAP_CASE);

    private static final String LIST_OF_MAPS_AS_JSON_CASE = "[ " + MAP_AS_JSON_CASE + " ]";

    private static Object[][] mapCases() {
        return new Object[][]{
                {"Object input", MAP_CASE},
                {"Json string input", MAP_AS_JSON_CASE}
        };
    }

    private static Object[][] listCases() {
        return new Object[][]{
                {"List<Map> Object input", LIST_OF_MAPS_CASE},
                {"List<Map> Json string input", LIST_OF_MAPS_AS_JSON_CASE}
        };
    }

    private static Object[][] allCases() {
        return new Object[][]{
                {"Object input", MAP_CASE},
                {"Json string input", MAP_AS_JSON_CASE},
                {"List<Map> Object input", LIST_OF_MAPS_CASE},
                {"List<Map> Json string input", LIST_OF_MAPS_AS_JSON_CASE}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("mapCases")
    void mixedFeaturesTest(String testName, Object input) {
        mixedTestImpl(testName,input, jsonMatcher -> jsonMatcher
                .with("fhirUser", matchesPattern("Patient/[a-z0-9-]+"))
                .ignoring("jti")
                .with("person_public_id", matchesPattern("[a-z0-9-]+"))
                .with("sub", matchesPattern("[a-z0-9-]+"))
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("mapCases")
    void mixedFeaturesTwoTest(String testName, Object input) {
        mixedTestImpl(testName,input, jsonMatcher -> jsonMatcher
                .withMatcher(equalTo("fhirUser"), matchesPattern("Patient/[a-z0-9-]+"))
                .ignoring(equalTo("jti"))
                .withMatcher(equalTo("person_public_id"), Matchers.matchesPattern("[a-z0-9-]+"))
                .withMatcher(equalTo("sub"), Matchers.matchesPattern("[a-z0-9-]+"))
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("listCases")
    void mixedFeaturesCollectionTest(String testName, Object input) {
        listTestImpl(testName, input, jsonMatcher -> jsonMatcher
                .with("fhirUser", matchesPattern("Patient/[a-z0-9-]+"))
                .ignoring("jti")
                .with("person_public_id", matchesPattern("[a-z0-9-]+"))
                .with("sub", matchesPattern("[a-z0-9-]+"))
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("listCases")
    void mixedFeaturesCollectionTwoTest(String testName, Object input) {
        listTestImpl(testName, input, jsonMatcher -> jsonMatcher
                .withMatcher(equalTo("fhirUser"), matchesPattern("Patient/[a-z0-9-]+"))
                .ignoring(equalTo("jti"))
                .withMatcher(equalTo("person_public_id"), Matchers.matchesPattern("[a-z0-9-]+"))
                .withMatcher(equalTo("sub"), Matchers.matchesPattern("[a-z0-9-]+"))
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("allCases")
    void pathAbsentFromAllElementsThrowsException(String testName, Object input) {
        assertJsonMatcherWithDummyTestInfo(input, "null", getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.with("nonExistentField", equalTo("anything")),
                thrown -> Assertions.assertEquals("nonExistentField does not exist", thrown.getMessage()),
                IllegalArgumentException.class);
    }

    private void listTestImpl(String testName, Object input, Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator) {
        String approvedFileContent = "[\n" +
                "  [\n" +
                "    {\n" +
                "      \"aud\": [\n" +
                "        \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"auth_time\": 1760351400\n" +
                "    },\n" +
                "    {\n" +
                "      \"azp\": \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"exp\": \"2025-10-13T11:00:00.000Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iat\": \"2025-10-13T10:30:00.000Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iss\": \"https://t.oauth2.x.com\"\n" +
                "    }\n" +
                "  ]\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), configurator, null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[0][]: Expected 6 values but got 10"), thrown.getMessage());

            String actual = "[\n" +
                    "  [\n" +
                    "    {\n" +
                    "      \"aud\": [\n" +
                    "        \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"auth_time\": 1760351400\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"azp\": \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"exp\": \"2025-10-13T11:00:00.000Z\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"fhirUser\": \"Patient/asd123\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"iat\": \"2025-10-13T10:30:00.000Z\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"iss\": \"https://t.oauth2.x.com\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"jti\": 1782298044\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"person_public_id\": \"9fdca323-5873-42d5-8672-0bb513d0f443\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"sub\": \"45d48351-44d6-42b7-82dc-744e2e7b3f7a\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "]";

            String expected = "[\n" +
                    "  [\n" +
                    "    {\n" +
                    "      \"aud\": [\n" +
                    "        \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"auth_time\": 1760351400\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"azp\": \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"exp\": \"2025-10-13T11:00:00.000Z\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"iat\": \"2025-10-13T10:30:00.000Z\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"iss\": \"https://t.oauth2.x.com\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "]";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "should show full list contents");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "should show filtered list contents");
        }, AssertionFailedError.class);
    }

    private void mixedTestImpl(String testName, Object input, Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator) {
        String approvedFileContent = "[\n" +
                "  {\n" +
                "    \"aud\": [\n" +
                "      \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"auth_time\": 1760351400\n" +
                "  },\n" +
                "  {\n" +
                "    \"azp\": \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"exp\": \"2025-10-13T11:00:00.000Z\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"iat\": \"2025-10-13T10:30:00.000Z\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"iss\": \"https://t.oauth2.x.com\"\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), configurator, null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfig(), identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[]: Expected 6 values but got 10"), thrown.getMessage());

            String actual = "[\n" +
                    "  {\n" +
                    "    \"aud\": [\n" +
                    "      \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"auth_time\": 1760351400\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"azp\": \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"exp\": \"2025-10-13T11:00:00.000Z\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"fhirUser\": \"Patient/asd123\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"iat\": \"2025-10-13T10:30:00.000Z\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"iss\": \"https://t.oauth2.x.com\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"jti\": 1782298044\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"person_public_id\": \"9fdca323-5873-42d5-8672-0bb513d0f443\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"sub\": \"45d48351-44d6-42b7-82dc-744e2e7b3f7a\"\n" +
                    "  }\n" +
                    "]";

            String expected = "[\n" +
                    "  {\n" +
                    "    \"aud\": [\n" +
                    "      \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"auth_time\": 1760351400\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"azp\": \"afca9c84-787d-44d6-a537-6560f1712d0f\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"exp\": \"2025-10-13T11:00:00.000Z\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"iat\": \"2025-10-13T10:30:00.000Z\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"iss\": \"https://t.oauth2.x.com\"\n" +
                    "  }\n" +
                    "]";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "beanInt shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "beanInt shouldn't be present");
        }, AssertionFailedError.class);
    }
}
