package com.github.karsaig.approvalcrest.matcher.ignores;

import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generatePerson;
import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generateTeam;
import static java.util.function.Function.identity;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.testdata.BeanWithGenericIterable;
import com.github.karsaig.approvalcrest.testdata.Country;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class JsonMatcherIgnoreMatcherTest extends AbstractFileMatcherTest {


    public static Object[][] simpleDiffCases() {
        return new Object[][]{
                {"Object input", generatePerson(1L)},
                {"Json string input", "{\n" +
                        "  \"firstName\": \"FirstName1\",\n" +
                        "  \"lastName\": \"LastName1\",\n" +
                        "  \"email\": \"e1@e.mail\",\n" +
                        "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "  \"birthCountry\": \"BELGIUM\",\n" +
                        "  \"currentAddress\": {\n" +
                        "    \"country\": \"BELGIUM\",\n" +
                        "    \"city\": \"CityName1\",\n" +
                        "    \"streetName\": \"StreetName60\",\n" +
                        "    \"streetNumber\": 43,\n" +
                        "    \"postCode\": \"PostCode64\",\n" +
                        "    \"since\": \"2017-04-02\"\n" +
                        "  },\n" +
                        "  \"previousAddresses\": [\n" +
                        "    {\n" +
                        "      \"country\": \"EGYPT\",\n" +
                        "      \"city\": \"CityName11\",\n" +
                        "      \"streetName\": \"StreetName70\",\n" +
                        "      \"streetNumber\": 53,\n" +
                        "      \"postCode\": \"PostCode74\",\n" +
                        "      \"since\": \"2017-04-12\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffCases")
    public void assertShouldBeSuccessfulWhenSimplePathWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"firstName\": \"Different first name\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("firstName\n" +
                    "Expected: Different first name\n" +
                    "     got: FirstName1\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"birthCountry\": \"BELGIUM\",\n" +
                    "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "  \"currentAddress\": {\n" +
                    "    \"city\": \"CityName1\",\n" +
                    "    \"country\": \"BELGIUM\",\n" +
                    "    \"postCode\": \"PostCode64\",\n" +
                    "    \"since\": \"2017-04-02\",\n" +
                    "    \"streetName\": \"StreetName60\",\n" +
                    "    \"streetNumber\": 43\n" +
                    "  },\n" +
                    "  \"email\": \"e1@e.mail\",\n" +
                    "  \"firstName\": \"FirstName1\",\n" +
                    "  \"lastName\": \"LastName1\",\n" +
                    "  \"previousAddresses\": [\n" +
                    "    {\n" +
                    "      \"city\": \"CityName11\",\n" +
                    "      \"country\": \"EGYPT\",\n" +
                    "      \"postCode\": \"PostCode74\",\n" +
                    "      \"since\": \"2017-04-12\",\n" +
                    "      \"streetName\": \"StreetName70\",\n" +
                    "      \"streetNumber\": 53\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation());
            Assertions.assertEquals(approvedFileContent, thrown.getExpected().getStringRepresentation());
        }, AssertionFailedError.class);
    }


    public static Object[][] simpleDiff2Cases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    return p;
                })},
                {"Json string input", "{\n" +
                        "  \"firstName\": \"Different first name\",\n" +
                        "  \"lastName\": \"LastName1\",\n" +
                        "  \"email\": \"e1@e.mail\",\n" +
                        "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "  \"birthCountry\": \"BELGIUM\",\n" +
                        "  \"currentAddress\": {\n" +
                        "    \"country\": \"BELGIUM\",\n" +
                        "    \"city\": \"CityName1\",\n" +
                        "    \"streetName\": \"StreetName60\",\n" +
                        "    \"streetNumber\": 43,\n" +
                        "    \"postCode\": \"PostCode64\",\n" +
                        "    \"since\": \"2017-04-02\"\n" +
                        "  },\n" +
                        "  \"previousAddresses\": [\n" +
                        "    {\n" +
                        "      \"country\": \"EGYPT\",\n" +
                        "      \"city\": \"CityName11\",\n" +
                        "      \"streetName\": \"StreetName70\",\n" +
                        "      \"streetNumber\": 53,\n" +
                        "      \"postCode\": \"PostCode74\",\n" +
                        "      \"since\": \"2017-04-12\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiff2Cases")
    public void assertShouldBeSuccessfulWhenSimplePathWithDifferenceIsIgnoredAndMissingFromExpected(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(is("firstName")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("\n" +
                    "Unexpected: firstName\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"birthCountry\": \"BELGIUM\",\n" +
                    "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "  \"currentAddress\": {\n" +
                    "    \"city\": \"CityName1\",\n" +
                    "    \"country\": \"BELGIUM\",\n" +
                    "    \"postCode\": \"PostCode64\",\n" +
                    "    \"since\": \"2017-04-02\",\n" +
                    "    \"streetName\": \"StreetName60\",\n" +
                    "    \"streetNumber\": 43\n" +
                    "  },\n" +
                    "  \"email\": \"e1@e.mail\",\n" +
                    "  \"firstName\": \"Different first name\",\n" +
                    "  \"lastName\": \"LastName1\",\n" +
                    "  \"previousAddresses\": [\n" +
                    "    {\n" +
                    "      \"city\": \"CityName11\",\n" +
                    "      \"country\": \"EGYPT\",\n" +
                    "      \"postCode\": \"PostCode74\",\n" +
                    "      \"since\": \"2017-04-12\",\n" +
                    "      \"streetName\": \"StreetName70\",\n" +
                    "      \"streetNumber\": 53\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation());
            Assertions.assertEquals(approvedFileContent, thrown.getExpected().getStringRepresentation());
        }, AssertionFailedError.class);
    }

    public static Object[][] notApprovedIgnoreCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Should not see this in not approved file");
                    return p;
                })},
                {"Json string input", "{\n" +
                        "  \"firstName\": \"Should not see this in not approved file\",\n" +
                        "  \"lastName\": \"LastName1\",\n" +
                        "  \"email\": \"e1@e.mail\",\n" +
                        "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "  \"birthCountry\": \"BELGIUM\",\n" +
                        "  \"currentAddress\": {\n" +
                        "    \"country\": \"BELGIUM\",\n" +
                        "    \"city\": \"CityName1\",\n" +
                        "    \"streetName\": \"StreetName60\",\n" +
                        "    \"streetNumber\": 43,\n" +
                        "    \"postCode\": \"PostCode64\",\n" +
                        "    \"since\": \"2017-04-02\"\n" +
                        "  },\n" +
                        "  \"previousAddresses\": [\n" +
                        "    {\n" +
                        "      \"country\": \"EGYPT\",\n" +
                        "      \"city\": \"CityName11\",\n" +
                        "      \"streetName\": \"StreetName70\",\n" +
                        "      \"streetNumber\": 53,\n" +
                        "      \"postCode\": \"PostCode74\",\n" +
                        "      \"since\": \"2017-04-12\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("notApprovedIgnoreCases")
    public void notApprovedFileCreatedWithIgnoredPathsNotPresentInIt(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43\n" +
                "  },\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(is("firstName")));
    }


    public static Object[][] simplePathIgnoreWithNullCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName(null);
                    return p;
                })},
                {"Json string input", "{\n" +
                        "  \"lastName\": \"LastName1\",\n" +
                        "  \"email\": \"e1@e.mail\",\n" +
                        "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "  \"birthCountry\": \"BELGIUM\",\n" +
                        "  \"currentAddress\": {\n" +
                        "    \"country\": \"BELGIUM\",\n" +
                        "    \"city\": \"CityName1\",\n" +
                        "    \"streetName\": \"StreetName60\",\n" +
                        "    \"streetNumber\": 43,\n" +
                        "    \"postCode\": \"PostCode64\",\n" +
                        "    \"since\": \"2017-04-02\"\n" +
                        "  },\n" +
                        "  \"previousAddresses\": [\n" +
                        "    {\n" +
                        "      \"country\": \"EGYPT\",\n" +
                        "      \"city\": \"CityName11\",\n" +
                        "      \"streetName\": \"StreetName70\",\n" +
                        "      \"streetNumber\": 53,\n" +
                        "      \"postCode\": \"PostCode74\",\n" +
                        "      \"since\": \"2017-04-12\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simplePathIgnoreWithNullCases")
    public void assertShouldBeSuccessfulWhenSimplePathWithNullDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"firstName\": \"NOT NULL\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("\n" +
                "Expected: firstName\n" +
                "     but none found\n"));
    }


    public static Object[][] multiLevelPathCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.getLead().getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    return t;
                })},
                {"Json string input", "{\n" +
                        "  \"name\": \"TeamName2\",\n" +
                        "  \"lead\": {\n" +
                        "    \"firstName\": \"FirstName13\",\n" +
                        "    \"lastName\": \"LastName13\",\n" +
                        "    \"email\": \"e13@e.mail\",\n" +
                        "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"HUNGARY\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"HUNGARY\",\n" +
                        "      \"city\": \"CityName13\",\n" +
                        "      \"streetName\": \"StreetName72\",\n" +
                        "      \"streetNumber\": 55,\n" +
                        "      \"postCode\": \"PostCode76\",\n" +
                        "      \"since\": \"2020-12-25\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"CANADA\",\n" +
                        "        \"city\": \"CityName23\",\n" +
                        "        \"streetName\": \"StreetName82\",\n" +
                        "        \"streetNumber\": 65,\n" +
                        "        \"postCode\": \"PostCode86\",\n" +
                        "        \"since\": \"2017-04-24\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName24\",\n" +
                        "        \"streetName\": \"StreetName83\",\n" +
                        "        \"streetNumber\": 66,\n" +
                        "        \"postCode\": \"PostCode87\",\n" +
                        "        \"since\": \"2017-04-25\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName25\",\n" +
                        "        \"streetName\": \"StreetName84\",\n" +
                        "        \"streetNumber\": 67,\n" +
                        "        \"postCode\": \"PostCode88\",\n" +
                        "        \"since\": \"2017-04-26\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"members\": [\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName102\",\n" +
                        "      \"lastName\": \"LastName102\",\n" +
                        "      \"email\": \"e102@e.mail\",\n" +
                        "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"EGYPT\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName102\",\n" +
                        "        \"streetName\": \"StreetName161\",\n" +
                        "        \"streetNumber\": 144,\n" +
                        "        \"postCode\": \"PostCode165\",\n" +
                        "        \"since\": \"2017-07-12\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"city\": \"CityName112\",\n" +
                        "          \"streetName\": \"StreetName171\",\n" +
                        "          \"streetNumber\": 154,\n" +
                        "          \"postCode\": \"PostCode175\",\n" +
                        "          \"since\": \"2017-07-22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName103\",\n" +
                        "      \"lastName\": \"LastName103\",\n" +
                        "      \"email\": \"e103@e.mail\",\n" +
                        "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"FRANCE\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"FRANCE\",\n" +
                        "        \"city\": \"CityName103\",\n" +
                        "        \"streetName\": \"StreetName162\",\n" +
                        "        \"streetNumber\": 145,\n" +
                        "        \"postCode\": \"PostCode166\",\n" +
                        "        \"since\": \"2017-07-13\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"CANADA\",\n" +
                        "          \"city\": \"CityName114\",\n" +
                        "          \"streetName\": \"StreetName173\",\n" +
                        "          \"streetNumber\": 156,\n" +
                        "          \"postCode\": \"PostCode177\",\n" +
                        "          \"since\": \"2017-07-24\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"city\": \"CityName115\",\n" +
                        "          \"streetName\": \"StreetName174\",\n" +
                        "          \"streetNumber\": 157,\n" +
                        "          \"postCode\": \"PostCode178\",\n" +
                        "          \"since\": \"2017-07-25\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multiLevelPathCases")
    public void assertShouldBeSuccessfulWhenMultiLevelPathWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"name\": \"TeamName2\",\n" +
                "  \"lead\": {\n" +
                "    \"firstName\": \"FirstName13\",\n" +
                "    \"lastName\": \"LastName13\",\n" +
                "    \"email\": \"e13@e.mail\",\n" +
                "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"HUNGARY\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"HUNGARY\",\n" +
                "      \"city\": \"CityName13\",\n" +
                "      \"streetName\": \"StreetName72\",\n" +
                "      \"streetNumber\": 55,\n" +
                "      \"postCode\": \"PostCode76\",\n" +
                "      \"since\": \"2020-04-21\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"city\": \"CityName23\",\n" +
                "        \"streetName\": \"StreetName82\",\n" +
                "        \"streetNumber\": 65,\n" +
                "        \"postCode\": \"PostCode86\",\n" +
                "        \"since\": \"2017-04-24\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName24\",\n" +
                "        \"streetName\": \"StreetName83\",\n" +
                "        \"streetNumber\": 66,\n" +
                "        \"postCode\": \"PostCode87\",\n" +
                "        \"since\": \"2017-04-25\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName25\",\n" +
                "        \"streetName\": \"StreetName84\",\n" +
                "        \"streetNumber\": 67,\n" +
                "        \"postCode\": \"PostCode88\",\n" +
                "        \"since\": \"2017-04-26\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName102\",\n" +
                "      \"lastName\": \"LastName102\",\n" +
                "      \"email\": \"e102@e.mail\",\n" +
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"EGYPT\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName102\",\n" +
                "        \"streetName\": \"StreetName161\",\n" +
                "        \"streetNumber\": 144,\n" +
                "        \"postCode\": \"PostCode165\",\n" +
                "        \"since\": \"2017-07-12\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"city\": \"CityName112\",\n" +
                "          \"streetName\": \"StreetName171\",\n" +
                "          \"streetNumber\": 154,\n" +
                "          \"postCode\": \"PostCode175\",\n" +
                "          \"since\": \"2017-07-22\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName103\",\n" +
                "      \"lastName\": \"LastName103\",\n" +
                "      \"email\": \"e103@e.mail\",\n" +
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"FRANCE\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"city\": \"CityName103\",\n" +
                "        \"streetName\": \"StreetName162\",\n" +
                "        \"streetNumber\": 145,\n" +
                "        \"postCode\": \"PostCode166\",\n" +
                "        \"since\": \"2017-07-13\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName114\",\n" +
                "          \"streetName\": \"StreetName173\",\n" +
                "          \"streetNumber\": 156,\n" +
                "          \"postCode\": \"PostCode177\",\n" +
                "          \"since\": \"2017-07-24\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"city\": \"CityName115\",\n" +
                "          \"streetName\": \"StreetName174\",\n" +
                "          \"streetNumber\": 157,\n" +
                "          \"postCode\": \"PostCode178\",\n" +
                "          \"since\": \"2017-07-25\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since")), null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("lead.currentAddress.since\n" +
                "Expected: 2020-04-21\n" +
                "     got: 2020-12-25\n"));
    }


    public static Object[][] multiLevelPathInCollectionCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.getMembers().get(0).getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    return t;
                })},
                {"Json string input", "{\n" +
                        "  \"name\": \"TeamName2\",\n" +
                        "  \"lead\": {\n" +
                        "    \"firstName\": \"FirstName13\",\n" +
                        "    \"lastName\": \"LastName13\",\n" +
                        "    \"email\": \"e13@e.mail\",\n" +
                        "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"HUNGARY\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"HUNGARY\",\n" +
                        "      \"city\": \"CityName13\",\n" +
                        "      \"streetName\": \"StreetName72\",\n" +
                        "      \"streetNumber\": 55,\n" +
                        "      \"postCode\": \"PostCode76\",\n" +
                        "      \"since\": \"2017-04-14\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"CANADA\",\n" +
                        "        \"city\": \"CityName23\",\n" +
                        "        \"streetName\": \"StreetName82\",\n" +
                        "        \"streetNumber\": 65,\n" +
                        "        \"postCode\": \"PostCode86\",\n" +
                        "        \"since\": \"2017-04-24\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName24\",\n" +
                        "        \"streetName\": \"StreetName83\",\n" +
                        "        \"streetNumber\": 66,\n" +
                        "        \"postCode\": \"PostCode87\",\n" +
                        "        \"since\": \"2017-04-25\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName25\",\n" +
                        "        \"streetName\": \"StreetName84\",\n" +
                        "        \"streetNumber\": 67,\n" +
                        "        \"postCode\": \"PostCode88\",\n" +
                        "        \"since\": \"2017-04-26\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"members\": [\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName102\",\n" +
                        "      \"lastName\": \"LastName102\",\n" +
                        "      \"email\": \"e102@e.mail\",\n" +
                        "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"EGYPT\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName102\",\n" +
                        "        \"streetName\": \"StreetName161\",\n" +
                        "        \"streetNumber\": 144,\n" +
                        "        \"postCode\": \"PostCode165\",\n" +
                        "        \"since\": \"2020-12-25\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"city\": \"CityName112\",\n" +
                        "          \"streetName\": \"StreetName171\",\n" +
                        "          \"streetNumber\": 154,\n" +
                        "          \"postCode\": \"PostCode175\",\n" +
                        "          \"since\": \"2017-07-22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName103\",\n" +
                        "      \"lastName\": \"LastName103\",\n" +
                        "      \"email\": \"e103@e.mail\",\n" +
                        "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"FRANCE\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"FRANCE\",\n" +
                        "        \"city\": \"CityName103\",\n" +
                        "        \"streetName\": \"StreetName162\",\n" +
                        "        \"streetNumber\": 145,\n" +
                        "        \"postCode\": \"PostCode166\",\n" +
                        "        \"since\": \"2017-07-13\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"CANADA\",\n" +
                        "          \"city\": \"CityName114\",\n" +
                        "          \"streetName\": \"StreetName173\",\n" +
                        "          \"streetNumber\": 156,\n" +
                        "          \"postCode\": \"PostCode177\",\n" +
                        "          \"since\": \"2017-07-24\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"city\": \"CityName115\",\n" +
                        "          \"streetName\": \"StreetName174\",\n" +
                        "          \"streetNumber\": 157,\n" +
                        "          \"postCode\": \"PostCode178\",\n" +
                        "          \"since\": \"2017-07-25\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multiLevelPathInCollectionCases")
    public void assertShouldBeSuccessfulWhenMultiLevelPathInCollectionWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"name\": \"TeamName2\",\n" +
                "  \"lead\": {\n" +
                "    \"firstName\": \"FirstName13\",\n" +
                "    \"lastName\": \"LastName13\",\n" +
                "    \"email\": \"e13@e.mail\",\n" +
                "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"HUNGARY\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"HUNGARY\",\n" +
                "      \"city\": \"CityName13\",\n" +
                "      \"streetName\": \"StreetName72\",\n" +
                "      \"streetNumber\": 55,\n" +
                "      \"postCode\": \"PostCode76\",\n" +
                "      \"since\": \"2017-04-14\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"city\": \"CityName23\",\n" +
                "        \"streetName\": \"StreetName82\",\n" +
                "        \"streetNumber\": 65,\n" +
                "        \"postCode\": \"PostCode86\",\n" +
                "        \"since\": \"2017-04-24\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName24\",\n" +
                "        \"streetName\": \"StreetName83\",\n" +
                "        \"streetNumber\": 66,\n" +
                "        \"postCode\": \"PostCode87\",\n" +
                "        \"since\": \"2017-04-25\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName25\",\n" +
                "        \"streetName\": \"StreetName84\",\n" +
                "        \"streetNumber\": 67,\n" +
                "        \"postCode\": \"PostCode88\",\n" +
                "        \"since\": \"2017-04-26\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName102\",\n" +
                "      \"lastName\": \"LastName102\",\n" +
                "      \"email\": \"e102@e.mail\",\n" +
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"EGYPT\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName102\",\n" +
                "        \"streetName\": \"StreetName161\",\n" +
                "        \"streetNumber\": 144,\n" +
                "        \"postCode\": \"PostCode165\",\n" +
                "        \"since\": \"2019-12-25\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"city\": \"CityName112\",\n" +
                "          \"streetName\": \"StreetName171\",\n" +
                "          \"streetNumber\": 154,\n" +
                "          \"postCode\": \"PostCode175\",\n" +
                "          \"since\": \"2017-07-22\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName103\",\n" +
                "      \"lastName\": \"LastName103\",\n" +
                "      \"email\": \"e103@e.mail\",\n" +
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"FRANCE\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"city\": \"CityName103\",\n" +
                "        \"streetName\": \"StreetName162\",\n" +
                "        \"streetNumber\": 145,\n" +
                "        \"postCode\": \"PostCode166\",\n" +
                "        \"since\": \"2017-07-13\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName114\",\n" +
                "          \"streetName\": \"StreetName173\",\n" +
                "          \"streetNumber\": 156,\n" +
                "          \"postCode\": \"PostCode177\",\n" +
                "          \"since\": \"2017-07-24\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"city\": \"CityName115\",\n" +
                "          \"streetName\": \"StreetName174\",\n" +
                "          \"streetNumber\": 157,\n" +
                "          \"postCode\": \"PostCode178\",\n" +
                "          \"since\": \"2017-07-25\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("members[0].currentAddress.since\n" +
                "Expected: 2019-12-25\n" +
                "     got: 2020-12-25\n"));

    }

    public static Object[][] multiLevelPathInCollectionWithNullCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.getMembers().get(0).getCurrentAddress().setSince(null);
                    return t;
                })},
                {"Json string input", "{\n" +
                        "  \"name\": \"TeamName2\",\n" +
                        "  \"lead\": {\n" +
                        "    \"firstName\": \"FirstName13\",\n" +
                        "    \"lastName\": \"LastName13\",\n" +
                        "    \"email\": \"e13@e.mail\",\n" +
                        "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"HUNGARY\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"HUNGARY\",\n" +
                        "      \"city\": \"CityName13\",\n" +
                        "      \"streetName\": \"StreetName72\",\n" +
                        "      \"streetNumber\": 55,\n" +
                        "      \"postCode\": \"PostCode76\",\n" +
                        "      \"since\": \"2017-04-14\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"CANADA\",\n" +
                        "        \"city\": \"CityName23\",\n" +
                        "        \"streetName\": \"StreetName82\",\n" +
                        "        \"streetNumber\": 65,\n" +
                        "        \"postCode\": \"PostCode86\",\n" +
                        "        \"since\": \"2017-04-24\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName24\",\n" +
                        "        \"streetName\": \"StreetName83\",\n" +
                        "        \"streetNumber\": 66,\n" +
                        "        \"postCode\": \"PostCode87\",\n" +
                        "        \"since\": \"2017-04-25\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName25\",\n" +
                        "        \"streetName\": \"StreetName84\",\n" +
                        "        \"streetNumber\": 67,\n" +
                        "        \"postCode\": \"PostCode88\",\n" +
                        "        \"since\": \"2017-04-26\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"members\": [\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName102\",\n" +
                        "      \"lastName\": \"LastName102\",\n" +
                        "      \"email\": \"e102@e.mail\",\n" +
                        "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"EGYPT\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName102\",\n" +
                        "        \"streetName\": \"StreetName161\",\n" +
                        "        \"streetNumber\": 144,\n" +
                        "        \"postCode\": \"PostCode165\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"city\": \"CityName112\",\n" +
                        "          \"streetName\": \"StreetName171\",\n" +
                        "          \"streetNumber\": 154,\n" +
                        "          \"postCode\": \"PostCode175\",\n" +
                        "          \"since\": \"2017-07-22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName103\",\n" +
                        "      \"lastName\": \"LastName103\",\n" +
                        "      \"email\": \"e103@e.mail\",\n" +
                        "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"FRANCE\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"FRANCE\",\n" +
                        "        \"city\": \"CityName103\",\n" +
                        "        \"streetName\": \"StreetName162\",\n" +
                        "        \"streetNumber\": 145,\n" +
                        "        \"postCode\": \"PostCode166\",\n" +
                        "        \"since\": \"2017-07-13\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"CANADA\",\n" +
                        "          \"city\": \"CityName114\",\n" +
                        "          \"streetName\": \"StreetName173\",\n" +
                        "          \"streetNumber\": 156,\n" +
                        "          \"postCode\": \"PostCode177\",\n" +
                        "          \"since\": \"2017-07-24\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"city\": \"CityName115\",\n" +
                        "          \"streetName\": \"StreetName174\",\n" +
                        "          \"streetNumber\": 157,\n" +
                        "          \"postCode\": \"PostCode178\",\n" +
                        "          \"since\": \"2017-07-25\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multiLevelPathInCollectionWithNullCases")
    public void assertShouldBeSuccessfulWhenMultiLevelPathInCollectionWithNullDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"name\": \"TeamName2\",\n" +
                "  \"lead\": {\n" +
                "    \"firstName\": \"FirstName13\",\n" +
                "    \"lastName\": \"LastName13\",\n" +
                "    \"email\": \"e13@e.mail\",\n" +
                "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"HUNGARY\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"HUNGARY\",\n" +
                "      \"city\": \"CityName13\",\n" +
                "      \"streetName\": \"StreetName72\",\n" +
                "      \"streetNumber\": 55,\n" +
                "      \"postCode\": \"PostCode76\",\n" +
                "      \"since\": \"2017-04-14\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"city\": \"CityName23\",\n" +
                "        \"streetName\": \"StreetName82\",\n" +
                "        \"streetNumber\": 65,\n" +
                "        \"postCode\": \"PostCode86\",\n" +
                "        \"since\": \"2017-04-24\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName24\",\n" +
                "        \"streetName\": \"StreetName83\",\n" +
                "        \"streetNumber\": 66,\n" +
                "        \"postCode\": \"PostCode87\",\n" +
                "        \"since\": \"2017-04-25\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName25\",\n" +
                "        \"streetName\": \"StreetName84\",\n" +
                "        \"streetNumber\": 67,\n" +
                "        \"postCode\": \"PostCode88\",\n" +
                "        \"since\": \"2017-04-26\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName102\",\n" +
                "      \"lastName\": \"LastName102\",\n" +
                "      \"email\": \"e102@e.mail\",\n" +
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"EGYPT\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName102\",\n" +
                "        \"streetName\": \"StreetName161\",\n" +
                "        \"streetNumber\": 144,\n" +
                "        \"postCode\": \"PostCode165\",\n" +
                "        \"since\": \"2019-12-25\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"city\": \"CityName112\",\n" +
                "          \"streetName\": \"StreetName171\",\n" +
                "          \"streetNumber\": 154,\n" +
                "          \"postCode\": \"PostCode175\",\n" +
                "          \"since\": \"2017-07-22\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName103\",\n" +
                "      \"lastName\": \"LastName103\",\n" +
                "      \"email\": \"e103@e.mail\",\n" +
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"FRANCE\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"city\": \"CityName103\",\n" +
                "        \"streetName\": \"StreetName162\",\n" +
                "        \"streetNumber\": 145,\n" +
                "        \"postCode\": \"PostCode166\",\n" +
                "        \"since\": \"2017-07-13\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName114\",\n" +
                "          \"streetName\": \"StreetName173\",\n" +
                "          \"streetNumber\": 156,\n" +
                "          \"postCode\": \"PostCode177\",\n" +
                "          \"since\": \"2017-07-24\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"city\": \"CityName115\",\n" +
                "          \"streetName\": \"StreetName174\",\n" +
                "          \"streetNumber\": 157,\n" +
                "          \"postCode\": \"PostCode178\",\n" +
                "          \"since\": \"2017-07-25\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("members[0].currentAddress\n" +
                "Expected: since\n" +
                "     but none found\n"));
    }

    public static Object[][] multiLevelPathInCollectionWithNullDiffCases() {
        return new Object[][]{
                {"Object input", generateTeam(2L)},
                {"Json string input", "{\n" +
                        "  \"name\": \"TeamName2\",\n" +
                        "  \"lead\": {\n" +
                        "    \"firstName\": \"FirstName13\",\n" +
                        "    \"lastName\": \"LastName13\",\n" +
                        "    \"email\": \"e13@e.mail\",\n" +
                        "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"HUNGARY\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"HUNGARY\",\n" +
                        "      \"city\": \"CityName13\",\n" +
                        "      \"streetName\": \"StreetName72\",\n" +
                        "      \"streetNumber\": 55,\n" +
                        "      \"postCode\": \"PostCode76\",\n" +
                        "      \"since\": \"2017-04-14\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"CANADA\",\n" +
                        "        \"city\": \"CityName23\",\n" +
                        "        \"streetName\": \"StreetName82\",\n" +
                        "        \"streetNumber\": 65,\n" +
                        "        \"postCode\": \"PostCode86\",\n" +
                        "        \"since\": \"2017-04-24\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName24\",\n" +
                        "        \"streetName\": \"StreetName83\",\n" +
                        "        \"streetNumber\": 66,\n" +
                        "        \"postCode\": \"PostCode87\",\n" +
                        "        \"since\": \"2017-04-25\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName25\",\n" +
                        "        \"streetName\": \"StreetName84\",\n" +
                        "        \"streetNumber\": 67,\n" +
                        "        \"postCode\": \"PostCode88\",\n" +
                        "        \"since\": \"2017-04-26\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"members\": [\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName102\",\n" +
                        "      \"lastName\": \"LastName102\",\n" +
                        "      \"email\": \"e102@e.mail\",\n" +
                        "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"EGYPT\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName102\",\n" +
                        "        \"streetName\": \"StreetName161\",\n" +
                        "        \"streetNumber\": 144,\n" +
                        "        \"postCode\": \"PostCode165\",\n" +
                        "        \"since\": \"2017-07-12\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"city\": \"CityName112\",\n" +
                        "          \"streetName\": \"StreetName171\",\n" +
                        "          \"streetNumber\": 154,\n" +
                        "          \"postCode\": \"PostCode175\",\n" +
                        "          \"since\": \"2017-07-22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName103\",\n" +
                        "      \"lastName\": \"LastName103\",\n" +
                        "      \"email\": \"e103@e.mail\",\n" +
                        "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"FRANCE\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"FRANCE\",\n" +
                        "        \"city\": \"CityName103\",\n" +
                        "        \"streetName\": \"StreetName162\",\n" +
                        "        \"streetNumber\": 145,\n" +
                        "        \"postCode\": \"PostCode166\",\n" +
                        "        \"since\": \"2017-07-13\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"CANADA\",\n" +
                        "          \"city\": \"CityName114\",\n" +
                        "          \"streetName\": \"StreetName173\",\n" +
                        "          \"streetNumber\": 156,\n" +
                        "          \"postCode\": \"PostCode177\",\n" +
                        "          \"since\": \"2017-07-24\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"city\": \"CityName115\",\n" +
                        "          \"streetName\": \"StreetName174\",\n" +
                        "          \"streetNumber\": 157,\n" +
                        "          \"postCode\": \"PostCode178\",\n" +
                        "          \"since\": \"2017-07-25\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multiLevelPathInCollectionWithNullDiffCases")
    public void assertShouldBeSuccessfulWhenMultiLevelPathInCollectionWithNullDifferenceInFileIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"name\": \"TeamName2\",\n" +
                "  \"lead\": {\n" +
                "    \"firstName\": \"FirstName13\",\n" +
                "    \"lastName\": \"LastName13\",\n" +
                "    \"email\": \"e13@e.mail\",\n" +
                "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"HUNGARY\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"HUNGARY\",\n" +
                "      \"city\": \"CityName13\",\n" +
                "      \"streetName\": \"StreetName72\",\n" +
                "      \"streetNumber\": 55,\n" +
                "      \"postCode\": \"PostCode76\",\n" +
                "      \"since\": \"2017-04-14\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"city\": \"CityName23\",\n" +
                "        \"streetName\": \"StreetName82\",\n" +
                "        \"streetNumber\": 65,\n" +
                "        \"postCode\": \"PostCode86\",\n" +
                "        \"since\": \"2017-04-24\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName24\",\n" +
                "        \"streetName\": \"StreetName83\",\n" +
                "        \"streetNumber\": 66,\n" +
                "        \"postCode\": \"PostCode87\",\n" +
                "        \"since\": \"2017-04-25\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName25\",\n" +
                "        \"streetName\": \"StreetName84\",\n" +
                "        \"streetNumber\": 67,\n" +
                "        \"postCode\": \"PostCode88\",\n" +
                "        \"since\": \"2017-04-26\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName102\",\n" +
                "      \"lastName\": \"LastName102\",\n" +
                "      \"email\": \"e102@e.mail\",\n" +
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"EGYPT\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName102\",\n" +
                "        \"streetName\": \"StreetName161\",\n" +
                "        \"streetNumber\": 144,\n" +
                "        \"postCode\": \"PostCode165\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"city\": \"CityName112\",\n" +
                "          \"streetName\": \"StreetName171\",\n" +
                "          \"streetNumber\": 154,\n" +
                "          \"postCode\": \"PostCode175\",\n" +
                "          \"since\": \"2017-07-22\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName103\",\n" +
                "      \"lastName\": \"LastName103\",\n" +
                "      \"email\": \"e103@e.mail\",\n" +
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"FRANCE\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"city\": \"CityName103\",\n" +
                "        \"streetName\": \"StreetName162\",\n" +
                "        \"streetNumber\": 145,\n" +
                "        \"postCode\": \"PostCode166\",\n" +
                "        \"since\": \"2017-07-13\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName114\",\n" +
                "          \"streetName\": \"StreetName173\",\n" +
                "          \"streetNumber\": 156,\n" +
                "          \"postCode\": \"PostCode177\",\n" +
                "          \"since\": \"2017-07-24\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"city\": \"CityName115\",\n" +
                "          \"streetName\": \"StreetName174\",\n" +
                "          \"streetNumber\": 157,\n" +
                "          \"postCode\": \"PostCode178\",\n" +
                "          \"since\": \"2017-07-25\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("members[0].currentAddress\n" +
                "Unexpected: since\n"));
    }

    public static Object[][] multipleSimplePathDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    p.setLastName("Different last name");
                    return p;
                })},
                {"Json string input", "{\n" +
                        "  \"firstName\": \"Different first name\",\n" +
                        "  \"lastName\": \"Different last name\",\n" +
                        "  \"email\": \"e1@e.mail\",\n" +
                        "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "  \"birthCountry\": \"BELGIUM\",\n" +
                        "  \"currentAddress\": {\n" +
                        "    \"country\": \"BELGIUM\",\n" +
                        "    \"city\": \"CityName1\",\n" +
                        "    \"streetName\": \"StreetName60\",\n" +
                        "    \"streetNumber\": 43,\n" +
                        "    \"postCode\": \"PostCode64\",\n" +
                        "    \"since\": \"2017-04-02\"\n" +
                        "  },\n" +
                        "  \"previousAddresses\": [\n" +
                        "    {\n" +
                        "      \"country\": \"EGYPT\",\n" +
                        "      \"city\": \"CityName11\",\n" +
                        "      \"streetName\": \"StreetName70\",\n" +
                        "      \"streetNumber\": 53,\n" +
                        "      \"postCode\": \"PostCode74\",\n" +
                        "      \"since\": \"2017-04-12\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleSimplePathDiffCases")
    public void assertShouldBeSuccessfulWhenMultipleSimplePathWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"firstName\": \"FirstName1\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName")).ignoring(is("lastName")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; lastName\n" +
                "Expected: LastName1\n" +
                "     got: Different last name\n"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("lastName\n" +
                    "Expected: LastName1\n" +
                    "     got: Different last name\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"birthCountry\": \"BELGIUM\",\n" +
                    "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "  \"currentAddress\": {\n" +
                    "    \"city\": \"CityName1\",\n" +
                    "    \"country\": \"BELGIUM\",\n" +
                    "    \"postCode\": \"PostCode64\",\n" +
                    "    \"since\": \"2017-04-02\",\n" +
                    "    \"streetName\": \"StreetName60\",\n" +
                    "    \"streetNumber\": 43\n" +
                    "  },\n" +
                    "  \"email\": \"e1@e.mail\",\n" +
                    "  \"lastName\": \"Different last name\",\n" +
                    "  \"previousAddresses\": [\n" +
                    "    {\n" +
                    "      \"city\": \"CityName11\",\n" +
                    "      \"country\": \"EGYPT\",\n" +
                    "      \"postCode\": \"PostCode74\",\n" +
                    "      \"since\": \"2017-04-12\",\n" +
                    "      \"streetName\": \"StreetName70\",\n" +
                    "      \"streetNumber\": 53\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"lastName\": \"LastName1\",\n" +
                    "  \"email\": \"e1@e.mail\",\n" +
                    "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "  \"birthCountry\": \"BELGIUM\",\n" +
                    "  \"currentAddress\": {\n" +
                    "    \"country\": \"BELGIUM\",\n" +
                    "    \"city\": \"CityName1\",\n" +
                    "    \"streetName\": \"StreetName60\",\n" +
                    "    \"streetNumber\": 43,\n" +
                    "    \"postCode\": \"PostCode64\",\n" +
                    "    \"since\": \"2017-04-02\"\n" +
                    "  },\n" +
                    "  \"previousAddresses\": [\n" +
                    "    {\n" +
                    "      \"country\": \"EGYPT\",\n" +
                    "      \"city\": \"CityName11\",\n" +
                    "      \"streetName\": \"StreetName70\",\n" +
                    "      \"streetNumber\": 53,\n" +
                    "      \"postCode\": \"PostCode74\",\n" +
                    "      \"since\": \"2017-04-12\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "firstName shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "firstName shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] multipleSimpleDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    p.setLastName("Different last name");
                    return p;
                })},
                {"Json string input", "{\n" +
                        "  \"firstName\": \"Different first name\",\n" +
                        "  \"lastName\": \"Different last name\",\n" +
                        "  \"email\": \"e1@e.mail\",\n" +
                        "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "  \"birthCountry\": \"BELGIUM\",\n" +
                        "  \"currentAddress\": {\n" +
                        "    \"country\": \"BELGIUM\",\n" +
                        "    \"city\": \"CityName1\",\n" +
                        "    \"streetName\": \"StreetName60\",\n" +
                        "    \"streetNumber\": 43,\n" +
                        "    \"postCode\": \"PostCode64\",\n" +
                        "    \"since\": \"2017-04-02\"\n" +
                        "  },\n" +
                        "  \"previousAddresses\": [\n" +
                        "    {\n" +
                        "      \"country\": \"EGYPT\",\n" +
                        "      \"city\": \"CityName11\",\n" +
                        "      \"streetName\": \"StreetName70\",\n" +
                        "      \"streetNumber\": 53,\n" +
                        "      \"postCode\": \"PostCode74\",\n" +
                        "      \"since\": \"2017-04-12\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleSimpleDiffCases")
    public void assertShouldBeSuccessfulWhenMultipleSimplePathSingleIgnoreWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"firstName\": \"FirstName1\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName"), is("lastName")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; lastName\n" +
                "Expected: LastName1\n" +
                "     got: Different last name\n"));
    }

    public static Object[][] multipleMultiLevelPathWithDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    p.getCurrentAddress().setCountry(Country.HUNGARY);
                    return p;
                })},
                {"Json string input", "{\n" +
                        "  \"firstName\": \"FirstName1\",\n" +
                        "  \"lastName\": \"LastName1\",\n" +
                        "  \"email\": \"e1@e.mail\",\n" +
                        "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "  \"birthCountry\": \"BELGIUM\",\n" +
                        "  \"currentAddress\": {\n" +
                        "    \"country\": \"HUNGARY\",\n" +
                        "    \"city\": \"CityName1\",\n" +
                        "    \"streetName\": \"StreetName60\",\n" +
                        "    \"streetNumber\": 43,\n" +
                        "    \"postCode\": \"PostCode64\",\n" +
                        "    \"since\": \"2020-12-25\"\n" +
                        "  },\n" +
                        "  \"previousAddresses\": [\n" +
                        "    {\n" +
                        "      \"country\": \"EGYPT\",\n" +
                        "      \"city\": \"CityName11\",\n" +
                        "      \"streetName\": \"StreetName70\",\n" +
                        "      \"streetNumber\": 53,\n" +
                        "      \"postCode\": \"PostCode74\",\n" +
                        "      \"since\": \"2017-04-12\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleMultiLevelPathWithDiffCases")
    public void assertShouldBeSuccessfulWhenMultipleMultiLevelPathWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"firstName\": \"FirstName1\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since")).ignoring(is("country")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("currentAddress.country\n" +
                "Expected: BELGIUM\n" +
                "     got: HUNGARY\n" +
                " ; currentAddress.since\n" +
                "Expected: 2017-04-02\n" +
                "     got: 2020-12-25\n"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"birthCountry\": \"BELGIUM\",\n" +
                    "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "  \"currentAddress\": {\n" +
                    "    \"city\": \"CityName1\",\n" +
                    "    \"country\": \"HUNGARY\",\n" +
                    "    \"postCode\": \"PostCode64\",\n" +
                    "    \"streetName\": \"StreetName60\",\n" +
                    "    \"streetNumber\": 43\n" +
                    "  },\n" +
                    "  \"email\": \"e1@e.mail\",\n" +
                    "  \"firstName\": \"FirstName1\",\n" +
                    "  \"lastName\": \"LastName1\",\n" +
                    "  \"previousAddresses\": [\n" +
                    "    {\n" +
                    "      \"city\": \"CityName11\",\n" +
                    "      \"country\": \"EGYPT\",\n" +
                    "      \"postCode\": \"PostCode74\",\n" +
                    "      \"streetName\": \"StreetName70\",\n" +
                    "      \"streetNumber\": 53\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"firstName\": \"FirstName1\",\n" +
                    "  \"lastName\": \"LastName1\",\n" +
                    "  \"email\": \"e1@e.mail\",\n" +
                    "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "  \"birthCountry\": \"BELGIUM\",\n" +
                    "  \"currentAddress\": {\n" +
                    "    \"country\": \"BELGIUM\",\n" +
                    "    \"city\": \"CityName1\",\n" +
                    "    \"streetName\": \"StreetName60\",\n" +
                    "    \"streetNumber\": 43,\n" +
                    "    \"postCode\": \"PostCode64\"\n" +
                    "  },\n" +
                    "  \"previousAddresses\": [\n" +
                    "    {\n" +
                    "      \"country\": \"EGYPT\",\n" +
                    "      \"city\": \"CityName11\",\n" +
                    "      \"streetName\": \"StreetName70\",\n" +
                    "      \"streetNumber\": 53,\n" +
                    "      \"postCode\": \"PostCode74\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "since shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "since shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] multipleMultiLevelPathSingleIngoreWithDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    p.getCurrentAddress().setCountry(Country.HUNGARY);
                    return p;
                })},
                {"Json string input", "{\n" +
                        "  \"firstName\": \"FirstName1\",\n" +
                        "  \"lastName\": \"LastName1\",\n" +
                        "  \"email\": \"e1@e.mail\",\n" +
                        "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "  \"birthCountry\": \"BELGIUM\",\n" +
                        "  \"currentAddress\": {\n" +
                        "    \"country\": \"HUNGARY\",\n" +
                        "    \"city\": \"CityName1\",\n" +
                        "    \"streetName\": \"StreetName60\",\n" +
                        "    \"streetNumber\": 43,\n" +
                        "    \"postCode\": \"PostCode64\",\n" +
                        "    \"since\": \"2020-12-25\"\n" +
                        "  },\n" +
                        "  \"previousAddresses\": [\n" +
                        "    {\n" +
                        "      \"country\": \"EGYPT\",\n" +
                        "      \"city\": \"CityName11\",\n" +
                        "      \"streetName\": \"StreetName70\",\n" +
                        "      \"streetNumber\": 53,\n" +
                        "      \"postCode\": \"PostCode74\",\n" +
                        "      \"since\": \"2017-04-12\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleMultiLevelPathSingleIngoreWithDiffCases")
    public void assertShouldBeSuccessfulWhenMultipleMultiLevelPathSingleIgnoreWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"firstName\": \"FirstName1\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since"), is("country")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("currentAddress.country\n" +
                "Expected: BELGIUM\n" +
                "     got: HUNGARY\n" +
                " ; currentAddress.since\n" +
                "Expected: 2017-04-02\n" +
                "     got: 2020-12-25\n"));
    }


    public static Object[][] multipleMultiLevelPathInCollectionWithDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.getMembers().get(0).getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    t.getMembers().get(1).setBirthCountry(Country.HUNGARY);
                    return t;
                })},
                {"Json string input", "{\n" +
                        "  \"name\": \"TeamName2\",\n" +
                        "  \"lead\": {\n" +
                        "    \"firstName\": \"FirstName13\",\n" +
                        "    \"lastName\": \"LastName13\",\n" +
                        "    \"email\": \"e13@e.mail\",\n" +
                        "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"HUNGARY\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"HUNGARY\",\n" +
                        "      \"city\": \"CityName13\",\n" +
                        "      \"streetName\": \"StreetName72\",\n" +
                        "      \"streetNumber\": 55,\n" +
                        "      \"postCode\": \"PostCode76\",\n" +
                        "      \"since\": \"2017-04-14\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"CANADA\",\n" +
                        "        \"city\": \"CityName23\",\n" +
                        "        \"streetName\": \"StreetName82\",\n" +
                        "        \"streetNumber\": 65,\n" +
                        "        \"postCode\": \"PostCode86\",\n" +
                        "        \"since\": \"2017-04-24\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName24\",\n" +
                        "        \"streetName\": \"StreetName83\",\n" +
                        "        \"streetNumber\": 66,\n" +
                        "        \"postCode\": \"PostCode87\",\n" +
                        "        \"since\": \"2017-04-25\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName25\",\n" +
                        "        \"streetName\": \"StreetName84\",\n" +
                        "        \"streetNumber\": 67,\n" +
                        "        \"postCode\": \"PostCode88\",\n" +
                        "        \"since\": \"2017-04-26\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"members\": [\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName102\",\n" +
                        "      \"lastName\": \"LastName102\",\n" +
                        "      \"email\": \"e102@e.mail\",\n" +
                        "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"EGYPT\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName102\",\n" +
                        "        \"streetName\": \"StreetName161\",\n" +
                        "        \"streetNumber\": 144,\n" +
                        "        \"postCode\": \"PostCode165\",\n" +
                        "        \"since\": \"2020-12-25\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"city\": \"CityName112\",\n" +
                        "          \"streetName\": \"StreetName171\",\n" +
                        "          \"streetNumber\": 154,\n" +
                        "          \"postCode\": \"PostCode175\",\n" +
                        "          \"since\": \"2017-07-22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName103\",\n" +
                        "      \"lastName\": \"LastName103\",\n" +
                        "      \"email\": \"e103@e.mail\",\n" +
                        "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"HUNGARY\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"FRANCE\",\n" +
                        "        \"city\": \"CityName103\",\n" +
                        "        \"streetName\": \"StreetName162\",\n" +
                        "        \"streetNumber\": 145,\n" +
                        "        \"postCode\": \"PostCode166\",\n" +
                        "        \"since\": \"2017-07-13\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"CANADA\",\n" +
                        "          \"city\": \"CityName114\",\n" +
                        "          \"streetName\": \"StreetName173\",\n" +
                        "          \"streetNumber\": 156,\n" +
                        "          \"postCode\": \"PostCode177\",\n" +
                        "          \"since\": \"2017-07-24\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"city\": \"CityName115\",\n" +
                        "          \"streetName\": \"StreetName174\",\n" +
                        "          \"streetNumber\": 157,\n" +
                        "          \"postCode\": \"PostCode178\",\n" +
                        "          \"since\": \"2017-07-25\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleMultiLevelPathInCollectionWithDiffCases")
    public void assertShouldBeSuccessfulWhenMultipleMultiLevelPathInCollectionSingleIgnoreWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"name\": \"TeamName2\",\n" +
                "  \"lead\": {\n" +
                "    \"firstName\": \"FirstName13\",\n" +
                "    \"lastName\": \"LastName13\",\n" +
                "    \"email\": \"e13@e.mail\",\n" +
                "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"HUNGARY\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"HUNGARY\",\n" +
                "      \"city\": \"CityName13\",\n" +
                "      \"streetName\": \"StreetName72\",\n" +
                "      \"streetNumber\": 55,\n" +
                "      \"postCode\": \"PostCode76\",\n" +
                "      \"since\": \"2017-04-14\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"city\": \"CityName23\",\n" +
                "        \"streetName\": \"StreetName82\",\n" +
                "        \"streetNumber\": 65,\n" +
                "        \"postCode\": \"PostCode86\",\n" +
                "        \"since\": \"2017-04-24\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName24\",\n" +
                "        \"streetName\": \"StreetName83\",\n" +
                "        \"streetNumber\": 66,\n" +
                "        \"postCode\": \"PostCode87\",\n" +
                "        \"since\": \"2017-04-25\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName25\",\n" +
                "        \"streetName\": \"StreetName84\",\n" +
                "        \"streetNumber\": 67,\n" +
                "        \"postCode\": \"PostCode88\",\n" +
                "        \"since\": \"2017-04-26\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName102\",\n" +
                "      \"lastName\": \"LastName102\",\n" +
                "      \"email\": \"e102@e.mail\",\n" +
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"EGYPT\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName102\",\n" +
                "        \"streetName\": \"StreetName161\",\n" +
                "        \"streetNumber\": 144,\n" +
                "        \"postCode\": \"PostCode165\",\n" +
                "        \"since\": \"2019-12-25\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"city\": \"CityName112\",\n" +
                "          \"streetName\": \"StreetName171\",\n" +
                "          \"streetNumber\": 154,\n" +
                "          \"postCode\": \"PostCode175\",\n" +
                "          \"since\": \"2017-07-22\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName103\",\n" +
                "      \"lastName\": \"LastName103\",\n" +
                "      \"email\": \"e103@e.mail\",\n" +
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"FRANCE\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"city\": \"CityName103\",\n" +
                "        \"streetName\": \"StreetName162\",\n" +
                "        \"streetNumber\": 145,\n" +
                "        \"postCode\": \"PostCode166\",\n" +
                "        \"since\": \"2017-07-13\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"since\": \"2017-07-23\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName114\",\n" +
                "          \"streetName\": \"StreetName173\",\n" +
                "          \"streetNumber\": 156,\n" +
                "          \"postCode\": \"PostCode177\",\n" +
                "          \"since\": \"2017-07-24\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"city\": \"CityName115\",\n" +
                "          \"streetName\": \"StreetName174\",\n" +
                "          \"streetNumber\": 157,\n" +
                "          \"postCode\": \"PostCode178\",\n" +
                "          \"since\": \"2017-07-25\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since"), is("birthCountry")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("members[0].currentAddress.since\n" +
                "Expected: 2019-12-25\n" +
                "     got: 2020-12-25\n" +
                " ; members[1].birthCountry\n" +
                "Expected: FRANCE\n" +
                "     got: HUNGARY\n"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("since")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("members[1].birthCountry\n" +
                    "Expected: FRANCE\n" +
                    "     got: HUNGARY\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"lead\": {\n" +
                    "    \"birthCountry\": \"HUNGARY\",\n" +
                    "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName13\",\n" +
                    "      \"country\": \"HUNGARY\",\n" +
                    "      \"postCode\": \"PostCode76\",\n" +
                    "      \"streetName\": \"StreetName72\",\n" +
                    "      \"streetNumber\": 55\n" +
                    "    },\n" +
                    "    \"email\": \"e13@e.mail\",\n" +
                    "    \"firstName\": \"FirstName13\",\n" +
                    "    \"lastName\": \"LastName13\",\n" +
                    "    \"previousAddresses\": [\n" +
                    "      {\n" +
                    "        \"city\": \"CityName23\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode86\",\n" +
                    "        \"streetName\": \"StreetName82\",\n" +
                    "        \"streetNumber\": 65\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"city\": \"CityName24\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode87\",\n" +
                    "        \"streetName\": \"StreetName83\",\n" +
                    "        \"streetNumber\": 66\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"city\": \"CityName25\",\n" +
                    "        \"country\": \"EGYPT\",\n" +
                    "        \"postCode\": \"PostCode88\",\n" +
                    "        \"streetName\": \"StreetName84\",\n" +
                    "        \"streetNumber\": 67\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"members\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"EGYPT\",\n" +
                    "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName102\",\n" +
                    "        \"country\": \"EGYPT\",\n" +
                    "        \"postCode\": \"PostCode165\",\n" +
                    "        \"streetName\": \"StreetName161\",\n" +
                    "        \"streetNumber\": 144\n" +
                    "      },\n" +
                    "      \"email\": \"e102@e.mail\",\n" +
                    "      \"firstName\": \"FirstName102\",\n" +
                    "      \"lastName\": \"LastName102\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName112\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode175\",\n" +
                    "          \"streetName\": \"StreetName171\",\n" +
                    "          \"streetNumber\": 154\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName113\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode176\",\n" +
                    "          \"streetName\": \"StreetName172\",\n" +
                    "          \"streetNumber\": 155\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName103\",\n" +
                    "        \"country\": \"FRANCE\",\n" +
                    "        \"postCode\": \"PostCode166\",\n" +
                    "        \"streetName\": \"StreetName162\",\n" +
                    "        \"streetNumber\": 145\n" +
                    "      },\n" +
                    "      \"email\": \"e103@e.mail\",\n" +
                    "      \"firstName\": \"FirstName103\",\n" +
                    "      \"lastName\": \"LastName103\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName113\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode176\",\n" +
                    "          \"streetName\": \"StreetName172\",\n" +
                    "          \"streetNumber\": 155\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName114\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode177\",\n" +
                    "          \"streetName\": \"StreetName173\",\n" +
                    "          \"streetNumber\": 156\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName115\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode178\",\n" +
                    "          \"streetName\": \"StreetName174\",\n" +
                    "          \"streetNumber\": 157\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"name\": \"TeamName2\"\n" +
                    "}";

            String expected = "{\n" +
                    "  \"name\": \"TeamName2\",\n" +
                    "  \"lead\": {\n" +
                    "    \"firstName\": \"FirstName13\",\n" +
                    "    \"lastName\": \"LastName13\",\n" +
                    "    \"email\": \"e13@e.mail\",\n" +
                    "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                    "    \"birthCountry\": \"HUNGARY\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"country\": \"HUNGARY\",\n" +
                    "      \"city\": \"CityName13\",\n" +
                    "      \"streetName\": \"StreetName72\",\n" +
                    "      \"streetNumber\": 55,\n" +
                    "      \"postCode\": \"PostCode76\"\n" +
                    "    },\n" +
                    "    \"previousAddresses\": [\n" +
                    "      {\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"city\": \"CityName23\",\n" +
                    "        \"streetName\": \"StreetName82\",\n" +
                    "        \"streetNumber\": 65,\n" +
                    "        \"postCode\": \"PostCode86\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"city\": \"CityName24\",\n" +
                    "        \"streetName\": \"StreetName83\",\n" +
                    "        \"streetNumber\": 66,\n" +
                    "        \"postCode\": \"PostCode87\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"country\": \"EGYPT\",\n" +
                    "        \"city\": \"CityName25\",\n" +
                    "        \"streetName\": \"StreetName84\",\n" +
                    "        \"streetNumber\": 67,\n" +
                    "        \"postCode\": \"PostCode88\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"members\": [\n" +
                    "    {\n" +
                    "      \"firstName\": \"FirstName102\",\n" +
                    "      \"lastName\": \"LastName102\",\n" +
                    "      \"email\": \"e102@e.mail\",\n" +
                    "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"EGYPT\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"EGYPT\",\n" +
                    "        \"city\": \"CityName102\",\n" +
                    "        \"streetName\": \"StreetName161\",\n" +
                    "        \"streetNumber\": 144,\n" +
                    "        \"postCode\": \"PostCode165\"\n" +
                    "      },\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"city\": \"CityName112\",\n" +
                    "          \"streetName\": \"StreetName171\",\n" +
                    "          \"streetNumber\": 154,\n" +
                    "          \"postCode\": \"PostCode175\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"city\": \"CityName113\",\n" +
                    "          \"streetName\": \"StreetName172\",\n" +
                    "          \"streetNumber\": 155,\n" +
                    "          \"postCode\": \"PostCode176\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"firstName\": \"FirstName103\",\n" +
                    "      \"lastName\": \"LastName103\",\n" +
                    "      \"email\": \"e103@e.mail\",\n" +
                    "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"FRANCE\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"FRANCE\",\n" +
                    "        \"city\": \"CityName103\",\n" +
                    "        \"streetName\": \"StreetName162\",\n" +
                    "        \"streetNumber\": 145,\n" +
                    "        \"postCode\": \"PostCode166\"\n" +
                    "      },\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"city\": \"CityName113\",\n" +
                    "          \"streetName\": \"StreetName172\",\n" +
                    "          \"streetNumber\": 155,\n" +
                    "          \"postCode\": \"PostCode176\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"city\": \"CityName114\",\n" +
                    "          \"streetName\": \"StreetName173\",\n" +
                    "          \"streetNumber\": 156,\n" +
                    "          \"postCode\": \"PostCode177\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"city\": \"CityName115\",\n" +
                    "          \"streetName\": \"StreetName174\",\n" +
                    "          \"streetNumber\": 157,\n" +
                    "          \"postCode\": \"PostCode178\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "since shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "since shouldn't be present");
        }, AssertionFailedError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleMultiLevelPathInCollectionWithDiffCases")
    public void assertShouldBeSuccessfulWhenMultipleMultiLevelPathInCollectionSingleIgnoreWithDifferenceIsIgnoredInStrictMode(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"lead\": {\n" +
                "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"city\": \"CityName13\",\n" +
                "      \"country\": \"HUNGARY\",\n" +
                "      \"postCode\": \"PostCode76\",\n" +
                "      \"streetName\": \"StreetName72\",\n" +
                "      \"streetNumber\": 55\n" +
                "    },\n" +
                "    \"email\": \"e13@e.mail\",\n" +
                "    \"firstName\": \"FirstName13\",\n" +
                "    \"lastName\": \"LastName13\",\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"city\": \"CityName23\",\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"postCode\": \"PostCode86\",\n" +
                "        \"streetName\": \"StreetName82\",\n" +
                "        \"streetNumber\": 65\n" +
                "      },\n" +
                "      {\n" +
                "        \"city\": \"CityName24\",\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"postCode\": \"PostCode87\",\n" +
                "        \"streetName\": \"StreetName83\",\n" +
                "        \"streetNumber\": 66\n" +
                "      },\n" +
                "      {\n" +
                "        \"city\": \"CityName25\",\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"postCode\": \"PostCode88\",\n" +
                "        \"streetName\": \"StreetName84\",\n" +
                "        \"streetNumber\": 67\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName102\",\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"postCode\": \"PostCode165\",\n" +
                "        \"streetName\": \"StreetName161\",\n" +
                "        \"streetNumber\": 144\n" +
                "      },\n" +
                "      \"email\": \"e102@e.mail\",\n" +
                "      \"firstName\": \"FirstName102\",\n" +
                "      \"lastName\": \"LastName102\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName112\",\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"postCode\": \"PostCode175\",\n" +
                "          \"streetName\": \"StreetName171\",\n" +
                "          \"streetNumber\": 154\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName103\",\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"postCode\": \"PostCode166\",\n" +
                "        \"streetName\": \"StreetName162\",\n" +
                "        \"streetNumber\": 145\n" +
                "      },\n" +
                "      \"email\": \"e103@e.mail\",\n" +
                "      \"firstName\": \"FirstName103\",\n" +
                "      \"lastName\": \"LastName103\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName114\",\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"postCode\": \"PostCode177\",\n" +
                "          \"streetName\": \"StreetName173\",\n" +
                "          \"streetNumber\": 156\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName115\",\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"postCode\": \"PostCode178\",\n" +
                "          \"streetName\": \"StreetName174\",\n" +
                "          \"streetNumber\": 157\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\": \"TeamName2\"\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,getDefaultFileMatcherConfig(), jsonMatcher -> jsonMatcher.ignoring(is("since"), is("birthCountry")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,getDefaultFileMatcherConfig(), identity(), getExcceptionMessageForDummyTestInfo("lead.currentAddress\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[2]\n" +
                "Unexpected: since\n" +
                " ; lead\n" +
                "Unexpected: birthCountry\n" +
                " ; members[0].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[0]\n" +
                "Unexpected: birthCountry\n" +
                " ; members[1].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[2]\n" +
                "Unexpected: since\n" +
                " ; members[1]\n" +
                "Unexpected: birthCountry\n"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent,getDefaultFileMatcherConfig(), jsonMatcher -> jsonMatcher.ignoring(is("since")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo(
                    "lead\n" +
                            "Unexpected: birthCountry\n" +
                            " ; members[0]\n" +
                            "Unexpected: birthCountry\n" +
                            " ; members[1]\n" +
                            "Unexpected: birthCountry\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"lead\": {\n" +
                    "    \"birthCountry\": \"HUNGARY\",\n" +
                    "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName13\",\n" +
                    "      \"country\": \"HUNGARY\",\n" +
                    "      \"postCode\": \"PostCode76\",\n" +
                    "      \"streetName\": \"StreetName72\",\n" +
                    "      \"streetNumber\": 55\n" +
                    "    },\n" +
                    "    \"email\": \"e13@e.mail\",\n" +
                    "    \"firstName\": \"FirstName13\",\n" +
                    "    \"lastName\": \"LastName13\",\n" +
                    "    \"previousAddresses\": [\n" +
                    "      {\n" +
                    "        \"city\": \"CityName23\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode86\",\n" +
                    "        \"streetName\": \"StreetName82\",\n" +
                    "        \"streetNumber\": 65\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"city\": \"CityName24\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode87\",\n" +
                    "        \"streetName\": \"StreetName83\",\n" +
                    "        \"streetNumber\": 66\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"city\": \"CityName25\",\n" +
                    "        \"country\": \"EGYPT\",\n" +
                    "        \"postCode\": \"PostCode88\",\n" +
                    "        \"streetName\": \"StreetName84\",\n" +
                    "        \"streetNumber\": 67\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"members\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"EGYPT\",\n" +
                    "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName102\",\n" +
                    "        \"country\": \"EGYPT\",\n" +
                    "        \"postCode\": \"PostCode165\",\n" +
                    "        \"streetName\": \"StreetName161\",\n" +
                    "        \"streetNumber\": 144\n" +
                    "      },\n" +
                    "      \"email\": \"e102@e.mail\",\n" +
                    "      \"firstName\": \"FirstName102\",\n" +
                    "      \"lastName\": \"LastName102\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName112\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode175\",\n" +
                    "          \"streetName\": \"StreetName171\",\n" +
                    "          \"streetNumber\": 154\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName113\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode176\",\n" +
                    "          \"streetName\": \"StreetName172\",\n" +
                    "          \"streetNumber\": 155\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName103\",\n" +
                    "        \"country\": \"FRANCE\",\n" +
                    "        \"postCode\": \"PostCode166\",\n" +
                    "        \"streetName\": \"StreetName162\",\n" +
                    "        \"streetNumber\": 145\n" +
                    "      },\n" +
                    "      \"email\": \"e103@e.mail\",\n" +
                    "      \"firstName\": \"FirstName103\",\n" +
                    "      \"lastName\": \"LastName103\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName113\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode176\",\n" +
                    "          \"streetName\": \"StreetName172\",\n" +
                    "          \"streetNumber\": 155\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName114\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode177\",\n" +
                    "          \"streetName\": \"StreetName173\",\n" +
                    "          \"streetNumber\": 156\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName115\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode178\",\n" +
                    "          \"streetName\": \"StreetName174\",\n" +
                    "          \"streetNumber\": 157\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"name\": \"TeamName2\"\n" +
                    "}";

            String expected = "{\n" +
                    "  \"lead\": {\n" +
                    "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName13\",\n" +
                    "      \"country\": \"HUNGARY\",\n" +
                    "      \"postCode\": \"PostCode76\",\n" +
                    "      \"streetName\": \"StreetName72\",\n" +
                    "      \"streetNumber\": 55\n" +
                    "    },\n" +
                    "    \"email\": \"e13@e.mail\",\n" +
                    "    \"firstName\": \"FirstName13\",\n" +
                    "    \"lastName\": \"LastName13\",\n" +
                    "    \"previousAddresses\": [\n" +
                    "      {\n" +
                    "        \"city\": \"CityName23\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode86\",\n" +
                    "        \"streetName\": \"StreetName82\",\n" +
                    "        \"streetNumber\": 65\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"city\": \"CityName24\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode87\",\n" +
                    "        \"streetName\": \"StreetName83\",\n" +
                    "        \"streetNumber\": 66\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"city\": \"CityName25\",\n" +
                    "        \"country\": \"EGYPT\",\n" +
                    "        \"postCode\": \"PostCode88\",\n" +
                    "        \"streetName\": \"StreetName84\",\n" +
                    "        \"streetNumber\": 67\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"members\": [\n" +
                    "    {\n" +
                    "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName102\",\n" +
                    "        \"country\": \"EGYPT\",\n" +
                    "        \"postCode\": \"PostCode165\",\n" +
                    "        \"streetName\": \"StreetName161\",\n" +
                    "        \"streetNumber\": 144\n" +
                    "      },\n" +
                    "      \"email\": \"e102@e.mail\",\n" +
                    "      \"firstName\": \"FirstName102\",\n" +
                    "      \"lastName\": \"LastName102\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName112\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode175\",\n" +
                    "          \"streetName\": \"StreetName171\",\n" +
                    "          \"streetNumber\": 154\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName113\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode176\",\n" +
                    "          \"streetName\": \"StreetName172\",\n" +
                    "          \"streetNumber\": 155\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName103\",\n" +
                    "        \"country\": \"FRANCE\",\n" +
                    "        \"postCode\": \"PostCode166\",\n" +
                    "        \"streetName\": \"StreetName162\",\n" +
                    "        \"streetNumber\": 145\n" +
                    "      },\n" +
                    "      \"email\": \"e103@e.mail\",\n" +
                    "      \"firstName\": \"FirstName103\",\n" +
                    "      \"lastName\": \"LastName103\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName113\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode176\",\n" +
                    "          \"streetName\": \"StreetName172\",\n" +
                    "          \"streetNumber\": 155\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName114\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode177\",\n" +
                    "          \"streetName\": \"StreetName173\",\n" +
                    "          \"streetNumber\": 156\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName115\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode178\",\n" +
                    "          \"streetName\": \"StreetName174\",\n" +
                    "          \"streetNumber\": 157\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"name\": \"TeamName2\"\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "birthCountry shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "birthCountry shouldn't be present");
        }, AssertionFailedError.class);

    }

    public static Object[][] simpleDiffInList() {
        return new Object[][]{
                {"Object input", Lists.newArrayList(modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    return p;
                }), modifyObject(generatePerson(2L), p2 -> {
                    p2.getCurrentAddress().setCountry(Country.AUSTRIA);
                    return p2;
                }), modifyObject(generatePerson(3L), p3 -> {
                    p3.setPreviousAddresses(Collections.emptyList());
                    return p3;
                }))},
                {"Json string input", "[\n" +
                        "  {\n" +
                        "    \"firstName\": \"Different first name\",\n" +
                        "    \"lastName\": \"LastName1\",\n" +
                        "    \"email\": \"e1@e.mail\",\n" +
                        "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"BELGIUM\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"BELGIUM\",\n" +
                        "      \"city\": \"CityName1\",\n" +
                        "      \"streetName\": \"StreetName60\",\n" +
                        "      \"streetNumber\": 43,\n" +
                        "      \"postCode\": \"PostCode64\",\n" +
                        "      \"since\": \"2017-04-02\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName11\",\n" +
                        "        \"streetName\": \"StreetName70\",\n" +
                        "        \"streetNumber\": 53,\n" +
                        "        \"postCode\": \"PostCode74\",\n" +
                        "        \"since\": \"2017-04-12\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"firstName\": \"FirstName2\",\n" +
                        "    \"lastName\": \"LastName2\",\n" +
                        "    \"email\": \"e2@e.mail\",\n" +
                        "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"CANADA\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"AUSTRIA\",\n" +
                        "      \"city\": \"CityName2\",\n" +
                        "      \"streetName\": \"StreetName61\",\n" +
                        "      \"streetNumber\": 44,\n" +
                        "      \"postCode\": \"PostCode65\",\n" +
                        "      \"since\": \"2017-04-03\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"FRANCE\",\n" +
                        "        \"city\": \"CityName12\",\n" +
                        "        \"streetName\": \"StreetName71\",\n" +
                        "        \"streetNumber\": 54,\n" +
                        "        \"postCode\": \"PostCode75\",\n" +
                        "        \"since\": \"2017-04-13\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"HUNGARY\",\n" +
                        "        \"city\": \"CityName13\",\n" +
                        "        \"streetName\": \"StreetName72\",\n" +
                        "        \"streetNumber\": 55,\n" +
                        "        \"postCode\": \"PostCode76\",\n" +
                        "        \"since\": \"2017-04-14\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"firstName\": \"FirstName3\",\n" +
                        "    \"lastName\": \"LastName3\",\n" +
                        "    \"email\": \"e3@e.mail\",\n" +
                        "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"DENMARK\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"DENMARK\",\n" +
                        "      \"city\": \"CityName3\",\n" +
                        "      \"streetName\": \"StreetName62\",\n" +
                        "      \"streetNumber\": 45,\n" +
                        "      \"postCode\": \"PostCode66\",\n" +
                        "      \"since\": \"2017-04-04\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": []\n" +
                        "  }\n" +
                        "]"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInList")
    public void simpleDifferenceInListTest(String testName, Object input) {
        String approvedFileContent = "[\n" +
                "  {\n" +
                "    \"firstName\": \"FirstName1\",\n" +
                "    \"lastName\": \"LastName1\",\n" +
                "    \"email\": \"e1@e.mail\",\n" +
                "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"BELGIUM\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"BELGIUM\",\n" +
                "      \"city\": \"CityName1\",\n" +
                "      \"streetName\": \"StreetName60\",\n" +
                "      \"streetNumber\": 43,\n" +
                "      \"postCode\": \"PostCode64\",\n" +
                "      \"since\": \"2017-04-02\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName11\",\n" +
                "        \"streetName\": \"StreetName70\",\n" +
                "        \"streetNumber\": 53,\n" +
                "        \"postCode\": \"PostCode74\",\n" +
                "        \"since\": \"2017-04-12\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"firstName\": \"FirstName2\",\n" +
                "    \"lastName\": \"LastName2\",\n" +
                "    \"email\": \"e2@e.mail\",\n" +
                "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"CANADA\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"CANADA\",\n" +
                "      \"city\": \"CityName2\",\n" +
                "      \"streetName\": \"StreetName61\",\n" +
                "      \"streetNumber\": 44,\n" +
                "      \"postCode\": \"PostCode65\",\n" +
                "      \"since\": \"2017-04-03\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"city\": \"CityName12\",\n" +
                "        \"streetName\": \"StreetName71\",\n" +
                "        \"streetNumber\": 54,\n" +
                "        \"postCode\": \"PostCode75\",\n" +
                "        \"since\": \"2017-04-13\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"city\": \"CityName13\",\n" +
                "        \"streetName\": \"StreetName72\",\n" +
                "        \"streetNumber\": 55,\n" +
                "        \"postCode\": \"PostCode76\",\n" +
                "        \"since\": \"2017-04-14\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"firstName\": \"FirstName3\",\n" +
                "    \"lastName\": \"LastName3\",\n" +
                "    \"email\": \"e3@e.mail\",\n" +
                "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"DENMARK\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"DENMARK\",\n" +
                "      \"city\": \"CityName3\",\n" +
                "      \"streetName\": \"StreetName62\",\n" +
                "      \"streetNumber\": 45,\n" +
                "      \"postCode\": \"PostCode66\",\n" +
                "      \"since\": \"2017-04-04\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"city\": \"CityName13\",\n" +
                "        \"streetName\": \"StreetName72\",\n" +
                "        \"streetNumber\": 55,\n" +
                "        \"postCode\": \"PostCode76\",\n" +
                "        \"since\": \"2017-04-14\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"city\": \"CityName14\",\n" +
                "        \"streetName\": \"StreetName73\",\n" +
                "        \"streetNumber\": 56,\n" +
                "        \"postCode\": \"PostCode77\",\n" +
                "        \"since\": \"2017-04-15\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"city\": \"CityName15\",\n" +
                "        \"streetName\": \"StreetName74\",\n" +
                "        \"streetNumber\": 57,\n" +
                "        \"postCode\": \"PostCode78\",\n" +
                "        \"since\": \"2017-04-16\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName"), is("country")).ignoring(is("previousAddresses")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("[0].firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; [1].currentAddress.country\n" +
                "Expected: CANADA\n" +
                "     got: AUSTRIA\n" +
                " ; [2].previousAddresses[]: Expected 3 values but got 0"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("previousAddresses")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[0].firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: Different first name\n" +
                    " ; [1].currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: AUSTRIA\n"), thrown.getMessage());

            String actual = "[\n" +
                    "  {\n" +
                    "    \"birthCountry\": \"BELGIUM\",\n" +
                    "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName1\",\n" +
                    "      \"country\": \"BELGIUM\",\n" +
                    "      \"postCode\": \"PostCode64\",\n" +
                    "      \"since\": \"2017-04-02\",\n" +
                    "      \"streetName\": \"StreetName60\",\n" +
                    "      \"streetNumber\": 43\n" +
                    "    },\n" +
                    "    \"email\": \"e1@e.mail\",\n" +
                    "    \"firstName\": \"Different first name\",\n" +
                    "    \"lastName\": \"LastName1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"birthCountry\": \"CANADA\",\n" +
                    "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName2\",\n" +
                    "      \"country\": \"AUSTRIA\",\n" +
                    "      \"postCode\": \"PostCode65\",\n" +
                    "      \"since\": \"2017-04-03\",\n" +
                    "      \"streetName\": \"StreetName61\",\n" +
                    "      \"streetNumber\": 44\n" +
                    "    },\n" +
                    "    \"email\": \"e2@e.mail\",\n" +
                    "    \"firstName\": \"FirstName2\",\n" +
                    "    \"lastName\": \"LastName2\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"birthCountry\": \"DENMARK\",\n" +
                    "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName3\",\n" +
                    "      \"country\": \"DENMARK\",\n" +
                    "      \"postCode\": \"PostCode66\",\n" +
                    "      \"since\": \"2017-04-04\",\n" +
                    "      \"streetName\": \"StreetName62\",\n" +
                    "      \"streetNumber\": 45\n" +
                    "    },\n" +
                    "    \"email\": \"e3@e.mail\",\n" +
                    "    \"firstName\": \"FirstName3\",\n" +
                    "    \"lastName\": \"LastName3\"\n" +
                    "  }\n" +
                    "]";

            String expected = "[\n" +
                    "  {\n" +
                    "    \"firstName\": \"FirstName1\",\n" +
                    "    \"lastName\": \"LastName1\",\n" +
                    "    \"email\": \"e1@e.mail\",\n" +
                    "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "    \"birthCountry\": \"BELGIUM\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"country\": \"BELGIUM\",\n" +
                    "      \"city\": \"CityName1\",\n" +
                    "      \"streetName\": \"StreetName60\",\n" +
                    "      \"streetNumber\": 43,\n" +
                    "      \"postCode\": \"PostCode64\",\n" +
                    "      \"since\": \"2017-04-02\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"firstName\": \"FirstName2\",\n" +
                    "    \"lastName\": \"LastName2\",\n" +
                    "    \"email\": \"e2@e.mail\",\n" +
                    "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "    \"birthCountry\": \"CANADA\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"country\": \"CANADA\",\n" +
                    "      \"city\": \"CityName2\",\n" +
                    "      \"streetName\": \"StreetName61\",\n" +
                    "      \"streetNumber\": 44,\n" +
                    "      \"postCode\": \"PostCode65\",\n" +
                    "      \"since\": \"2017-04-03\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"firstName\": \"FirstName3\",\n" +
                    "    \"lastName\": \"LastName3\",\n" +
                    "    \"email\": \"e3@e.mail\",\n" +
                    "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "    \"birthCountry\": \"DENMARK\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"country\": \"DENMARK\",\n" +
                    "      \"city\": \"CityName3\",\n" +
                    "      \"streetName\": \"StreetName62\",\n" +
                    "      \"streetNumber\": 45,\n" +
                    "      \"postCode\": \"PostCode66\",\n" +
                    "      \"since\": \"2017-04-04\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "previousAddresses shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "previousAddresses shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] simpleDiffInSet() {
        return new Object[][]{
                {"Object input", Sets.newHashSet(modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    return p;
                }), modifyObject(generatePerson(2L), p2 -> {
                    p2.getCurrentAddress().setCountry(Country.AUSTRIA);
                    return p2;
                }), modifyObject(generatePerson(3L), p3 -> {
                    p3.setPreviousAddresses(Collections.emptyList());
                    return p3;
                }))},
                {"Json string input", "[\n" +
                        "  {\n" +
                        "    \"firstName\": \"Different first name\",\n" +
                        "    \"lastName\": \"LastName1\",\n" +
                        "    \"email\": \"e1@e.mail\",\n" +
                        "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"BELGIUM\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"BELGIUM\",\n" +
                        "      \"city\": \"CityName1\",\n" +
                        "      \"streetName\": \"StreetName60\",\n" +
                        "      \"streetNumber\": 43,\n" +
                        "      \"postCode\": \"PostCode64\",\n" +
                        "      \"since\": \"2017-04-02\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName11\",\n" +
                        "        \"streetName\": \"StreetName70\",\n" +
                        "        \"streetNumber\": 53,\n" +
                        "        \"postCode\": \"PostCode74\",\n" +
                        "        \"since\": \"2017-04-12\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"firstName\": \"FirstName2\",\n" +
                        "    \"lastName\": \"LastName2\",\n" +
                        "    \"email\": \"e2@e.mail\",\n" +
                        "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"CANADA\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"AUSTRIA\",\n" +
                        "      \"city\": \"CityName2\",\n" +
                        "      \"streetName\": \"StreetName61\",\n" +
                        "      \"streetNumber\": 44,\n" +
                        "      \"postCode\": \"PostCode65\",\n" +
                        "      \"since\": \"2017-04-03\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"FRANCE\",\n" +
                        "        \"city\": \"CityName12\",\n" +
                        "        \"streetName\": \"StreetName71\",\n" +
                        "        \"streetNumber\": 54,\n" +
                        "        \"postCode\": \"PostCode75\",\n" +
                        "        \"since\": \"2017-04-13\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"HUNGARY\",\n" +
                        "        \"city\": \"CityName13\",\n" +
                        "        \"streetName\": \"StreetName72\",\n" +
                        "        \"streetNumber\": 55,\n" +
                        "        \"postCode\": \"PostCode76\",\n" +
                        "        \"since\": \"2017-04-14\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"firstName\": \"FirstName3\",\n" +
                        "    \"lastName\": \"LastName3\",\n" +
                        "    \"email\": \"e3@e.mail\",\n" +
                        "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"DENMARK\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"DENMARK\",\n" +
                        "      \"city\": \"CityName3\",\n" +
                        "      \"streetName\": \"StreetName62\",\n" +
                        "      \"streetNumber\": 45,\n" +
                        "      \"postCode\": \"PostCode66\",\n" +
                        "      \"since\": \"2017-04-04\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": []\n" +
                        "  }\n" +
                        "]"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInSet")
    public void simpleDifferenceInSetTest(String testName, Object input) {
        String approvedFileContent = "[\n" +
                "  {\n" +
                "    \"firstName\": \"FirstName1\",\n" +
                "    \"lastName\": \"LastName1\",\n" +
                "    \"email\": \"e1@e.mail\",\n" +
                "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"BELGIUM\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"BELGIUM\",\n" +
                "      \"city\": \"CityName1\",\n" +
                "      \"streetName\": \"StreetName60\",\n" +
                "      \"streetNumber\": 43,\n" +
                "      \"postCode\": \"PostCode64\",\n" +
                "      \"since\": \"2017-04-02\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"city\": \"CityName11\",\n" +
                "        \"streetName\": \"StreetName70\",\n" +
                "        \"streetNumber\": 53,\n" +
                "        \"postCode\": \"PostCode74\",\n" +
                "        \"since\": \"2017-04-12\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"firstName\": \"FirstName2\",\n" +
                "    \"lastName\": \"LastName2\",\n" +
                "    \"email\": \"e2@e.mail\",\n" +
                "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"CANADA\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"CANADA\",\n" +
                "      \"city\": \"CityName2\",\n" +
                "      \"streetName\": \"StreetName61\",\n" +
                "      \"streetNumber\": 44,\n" +
                "      \"postCode\": \"PostCode65\",\n" +
                "      \"since\": \"2017-04-03\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"city\": \"CityName12\",\n" +
                "        \"streetName\": \"StreetName71\",\n" +
                "        \"streetNumber\": 54,\n" +
                "        \"postCode\": \"PostCode75\",\n" +
                "        \"since\": \"2017-04-13\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"city\": \"CityName13\",\n" +
                "        \"streetName\": \"StreetName72\",\n" +
                "        \"streetNumber\": 55,\n" +
                "        \"postCode\": \"PostCode76\",\n" +
                "        \"since\": \"2017-04-14\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"firstName\": \"FirstName3\",\n" +
                "    \"lastName\": \"LastName3\",\n" +
                "    \"email\": \"e3@e.mail\",\n" +
                "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "    \"birthCountry\": \"DENMARK\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"country\": \"DENMARK\",\n" +
                "      \"city\": \"CityName3\",\n" +
                "      \"streetName\": \"StreetName62\",\n" +
                "      \"streetNumber\": 45,\n" +
                "      \"postCode\": \"PostCode66\",\n" +
                "      \"since\": \"2017-04-04\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"city\": \"CityName13\",\n" +
                "        \"streetName\": \"StreetName72\",\n" +
                "        \"streetNumber\": 55,\n" +
                "        \"postCode\": \"PostCode76\",\n" +
                "        \"since\": \"2017-04-14\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"city\": \"CityName14\",\n" +
                "        \"streetName\": \"StreetName73\",\n" +
                "        \"streetNumber\": 56,\n" +
                "        \"postCode\": \"PostCode77\",\n" +
                "        \"since\": \"2017-04-15\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"city\": \"CityName15\",\n" +
                "        \"streetName\": \"StreetName74\",\n" +
                "        \"streetNumber\": 57,\n" +
                "        \"postCode\": \"PostCode78\",\n" +
                "        \"since\": \"2017-04-16\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName"), is("country")).ignoring(is("previousAddresses")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("[0].firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; [1].currentAddress.country\n" +
                "Expected: CANADA\n" +
                "     got: AUSTRIA\n" +
                " ; [2].previousAddresses[]: Expected 3 values but got 0"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("previousAddresses")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[0].firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: Different first name\n" +
                    " ; [1].currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: AUSTRIA\n"), thrown.getMessage());

            String actual = "[\n" +
                    "  {\n" +
                    "    \"birthCountry\": \"BELGIUM\",\n" +
                    "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName1\",\n" +
                    "      \"country\": \"BELGIUM\",\n" +
                    "      \"postCode\": \"PostCode64\",\n" +
                    "      \"since\": \"2017-04-02\",\n" +
                    "      \"streetName\": \"StreetName60\",\n" +
                    "      \"streetNumber\": 43\n" +
                    "    },\n" +
                    "    \"email\": \"e1@e.mail\",\n" +
                    "    \"firstName\": \"Different first name\",\n" +
                    "    \"lastName\": \"LastName1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"birthCountry\": \"CANADA\",\n" +
                    "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName2\",\n" +
                    "      \"country\": \"AUSTRIA\",\n" +
                    "      \"postCode\": \"PostCode65\",\n" +
                    "      \"since\": \"2017-04-03\",\n" +
                    "      \"streetName\": \"StreetName61\",\n" +
                    "      \"streetNumber\": 44\n" +
                    "    },\n" +
                    "    \"email\": \"e2@e.mail\",\n" +
                    "    \"firstName\": \"FirstName2\",\n" +
                    "    \"lastName\": \"LastName2\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"birthCountry\": \"DENMARK\",\n" +
                    "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"city\": \"CityName3\",\n" +
                    "      \"country\": \"DENMARK\",\n" +
                    "      \"postCode\": \"PostCode66\",\n" +
                    "      \"since\": \"2017-04-04\",\n" +
                    "      \"streetName\": \"StreetName62\",\n" +
                    "      \"streetNumber\": 45\n" +
                    "    },\n" +
                    "    \"email\": \"e3@e.mail\",\n" +
                    "    \"firstName\": \"FirstName3\",\n" +
                    "    \"lastName\": \"LastName3\"\n" +
                    "  }\n" +
                    "]";

            String expected = "[\n" +
                    "  {\n" +
                    "    \"firstName\": \"FirstName1\",\n" +
                    "    \"lastName\": \"LastName1\",\n" +
                    "    \"email\": \"e1@e.mail\",\n" +
                    "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "    \"birthCountry\": \"BELGIUM\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"country\": \"BELGIUM\",\n" +
                    "      \"city\": \"CityName1\",\n" +
                    "      \"streetName\": \"StreetName60\",\n" +
                    "      \"streetNumber\": 43,\n" +
                    "      \"postCode\": \"PostCode64\",\n" +
                    "      \"since\": \"2017-04-02\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"firstName\": \"FirstName2\",\n" +
                    "    \"lastName\": \"LastName2\",\n" +
                    "    \"email\": \"e2@e.mail\",\n" +
                    "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "    \"birthCountry\": \"CANADA\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"country\": \"CANADA\",\n" +
                    "      \"city\": \"CityName2\",\n" +
                    "      \"streetName\": \"StreetName61\",\n" +
                    "      \"streetNumber\": 44,\n" +
                    "      \"postCode\": \"PostCode65\",\n" +
                    "      \"since\": \"2017-04-03\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"firstName\": \"FirstName3\",\n" +
                    "    \"lastName\": \"LastName3\",\n" +
                    "    \"email\": \"e3@e.mail\",\n" +
                    "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "    \"birthCountry\": \"DENMARK\",\n" +
                    "    \"currentAddress\": {\n" +
                    "      \"country\": \"DENMARK\",\n" +
                    "      \"city\": \"CityName3\",\n" +
                    "      \"streetName\": \"StreetName62\",\n" +
                    "      \"streetNumber\": 45,\n" +
                    "      \"postCode\": \"PostCode66\",\n" +
                    "      \"since\": \"2017-04-04\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "previousAddresses shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "previousAddresses shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] simpleDiffInSetAsGenericProperty() {
        return new Object[][]{
                {"Object input", BeanWithGeneric.of("String1", Sets.newHashSet(modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    return p;
                }), modifyObject(generatePerson(2L), p2 -> {
                    p2.getCurrentAddress().setCountry(Country.AUSTRIA);
                    return p2;
                }), modifyObject(generatePerson(3L), p3 -> {
                    p3.setPreviousAddresses(Collections.emptyList());
                    return p3;
                })))},
                {"Json string input", "{\n" +
                        "  \"dummyString\": \"String1\",\n" +
                        "  \"genericValue\": [\n" +
                        "    {\n" +
                        "      \"firstName\": \"Different first name\",\n" +
                        "      \"lastName\": \"LastName1\",\n" +
                        "      \"email\": \"e1@e.mail\",\n" +
                        "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"BELGIUM\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"BELGIUM\",\n" +
                        "        \"city\": \"CityName1\",\n" +
                        "        \"streetName\": \"StreetName60\",\n" +
                        "        \"streetNumber\": 43,\n" +
                        "        \"postCode\": \"PostCode64\",\n" +
                        "        \"since\": \"2017-04-02\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"city\": \"CityName11\",\n" +
                        "          \"streetName\": \"StreetName70\",\n" +
                        "          \"streetNumber\": 53,\n" +
                        "          \"postCode\": \"PostCode74\",\n" +
                        "          \"since\": \"2017-04-12\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName2\",\n" +
                        "      \"lastName\": \"LastName2\",\n" +
                        "      \"email\": \"e2@e.mail\",\n" +
                        "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"CANADA\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"AUSTRIA\",\n" +
                        "        \"city\": \"CityName2\",\n" +
                        "        \"streetName\": \"StreetName61\",\n" +
                        "        \"streetNumber\": 44,\n" +
                        "        \"postCode\": \"PostCode65\",\n" +
                        "        \"since\": \"2017-04-03\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"city\": \"CityName12\",\n" +
                        "          \"streetName\": \"StreetName71\",\n" +
                        "          \"streetNumber\": 54,\n" +
                        "          \"postCode\": \"PostCode75\",\n" +
                        "          \"since\": \"2017-04-13\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"city\": \"CityName13\",\n" +
                        "          \"streetName\": \"StreetName72\",\n" +
                        "          \"streetNumber\": 55,\n" +
                        "          \"postCode\": \"PostCode76\",\n" +
                        "          \"since\": \"2017-04-14\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName3\",\n" +
                        "      \"lastName\": \"LastName3\",\n" +
                        "      \"email\": \"e3@e.mail\",\n" +
                        "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"DENMARK\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName3\",\n" +
                        "        \"streetName\": \"StreetName62\",\n" +
                        "        \"streetNumber\": 45,\n" +
                        "        \"postCode\": \"PostCode66\",\n" +
                        "        \"since\": \"2017-04-04\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": []\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInSetAsGenericProperty")
    public void simpleDifferenceInSetAsGenericPropertyTest(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"genericValue\": [\n" +
                "    {\n" +
                "      \"firstName\": \"Different first name\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43,\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"since\": \"2018-04-02\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53,\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"since\": \"2019-04-13\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"streetNumber\": 44,\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54,\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"since\": \"2017-04-13\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55,\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"streetNumber\": 45,\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\"\n" +
                "      },\n" +
                "      \"previousAddresses\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName"), is("since")).ignoring(is("previousAddresses")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("genericValue[0].currentAddress.since\n" +
                "Expected: 2018-04-02\n" +
                "     got: 2017-04-02\n" +
                " ; genericValue[0].previousAddresses[0].since\n" +
                "Expected: 2019-04-13\n" +
                "     got: 2017-04-12\n" +
                " ; genericValue[1].previousAddresses[1].country\n" +
                "Expected: FRANCE\n" +
                "     got: HUNGARY\n"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName")).ignoring(is("previousAddresses")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("genericValue[0].currentAddress.since\n" +
                    "Expected: 2018-04-02\n" +
                    "     got: 2017-04-02\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"genericValue\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"lastName\": \"LastName1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"lastName\": \"LastName2\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"lastName\": \"LastName3\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"genericValue\": [\n" +
                    "    {\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43,\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2018-04-02\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44,\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45,\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "firstName and previousAddresses shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "firstName and previousAddresses shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] simpleDiffInSetAsProperty() {
        return new Object[][]{
                {"Object input", BeanWithGenericIterable.Builder.bean().dummyString("String1").set(Sets.newHashSet(modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    return p;
                }), modifyObject(generatePerson(2L), p2 -> {
                    p2.getCurrentAddress().setCountry(Country.AUSTRIA);
                    return p2;
                }), modifyObject(generatePerson(3L), p3 -> {
                    p3.setPreviousAddresses(Collections.emptyList());
                    return p3;
                })))},
                {"Json string input", "{\n" +
                        "  \"dummyString\": \"String1\",\n" +
                        "  \"set\": [\n" +
                        "    {\n" +
                        "      \"firstName\": \"Different first name\",\n" +
                        "      \"lastName\": \"LastName1\",\n" +
                        "      \"email\": \"e1@e.mail\",\n" +
                        "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"BELGIUM\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"BELGIUM\",\n" +
                        "        \"city\": \"CityName1\",\n" +
                        "        \"streetName\": \"StreetName60\",\n" +
                        "        \"streetNumber\": 43,\n" +
                        "        \"postCode\": \"PostCode64\",\n" +
                        "        \"since\": \"2017-04-02\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"city\": \"CityName11\",\n" +
                        "          \"streetName\": \"StreetName70\",\n" +
                        "          \"streetNumber\": 53,\n" +
                        "          \"postCode\": \"PostCode74\",\n" +
                        "          \"since\": \"2017-04-12\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName2\",\n" +
                        "      \"lastName\": \"LastName2\",\n" +
                        "      \"email\": \"e2@e.mail\",\n" +
                        "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"CANADA\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"AUSTRIA\",\n" +
                        "        \"city\": \"CityName2\",\n" +
                        "        \"streetName\": \"StreetName61\",\n" +
                        "        \"streetNumber\": 44,\n" +
                        "        \"postCode\": \"PostCode65\",\n" +
                        "        \"since\": \"2017-04-03\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"city\": \"CityName12\",\n" +
                        "          \"streetName\": \"StreetName71\",\n" +
                        "          \"streetNumber\": 54,\n" +
                        "          \"postCode\": \"PostCode75\",\n" +
                        "          \"since\": \"2017-04-13\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"city\": \"CityName13\",\n" +
                        "          \"streetName\": \"StreetName72\",\n" +
                        "          \"streetNumber\": 55,\n" +
                        "          \"postCode\": \"PostCode76\",\n" +
                        "          \"since\": \"2017-04-14\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName3\",\n" +
                        "      \"lastName\": \"LastName3\",\n" +
                        "      \"email\": \"e3@e.mail\",\n" +
                        "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"DENMARK\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName3\",\n" +
                        "        \"streetName\": \"StreetName62\",\n" +
                        "        \"streetNumber\": 45,\n" +
                        "        \"postCode\": \"PostCode66\",\n" +
                        "        \"since\": \"2017-04-04\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": []\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInSetAsProperty")
    public void simpleDifferenceInSetAsPropertyTest(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"firstName\": \"Different first name\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43,\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"since\": \"2018-04-02\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53,\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"since\": \"2019-04-13\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"streetNumber\": 44,\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54,\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"since\": \"2017-04-13\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55,\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"streetNumber\": 45,\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\"\n" +
                "      },\n" +
                "      \"previousAddresses\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName"), is("since")).ignoring(is("previousAddresses")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("set[0].currentAddress.since\n" +
                "Expected: 2018-04-02\n" +
                "     got: 2017-04-02\n" +
                " ; set[0].previousAddresses[0].since\n" +
                "Expected: 2019-04-13\n" +
                "     got: 2017-04-12\n" +
                " ; set[1].previousAddresses[1].country\n" +
                "Expected: FRANCE\n" +
                "     got: HUNGARY\n"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName")).ignoring(is("previousAddresses")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("set[0].currentAddress.since\n" +
                    "Expected: 2018-04-02\n" +
                    "     got: 2017-04-02\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"lastName\": \"LastName1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"lastName\": \"LastName2\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"lastName\": \"LastName3\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43,\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2018-04-02\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44,\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45,\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "firstName and previousAddresses shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "firstName and previousAddresses shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] simpleDiffInMap() {
        return new Object[][]{
                {"Object input", ImmutableMap.of("p1", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    return p;
                }), "p2", modifyObject(generatePerson(2L), p2 -> {
                    p2.getCurrentAddress().setCountry(Country.AUSTRIA);
                    return p2;
                }), "p3", modifyObject(generatePerson(3L), p3 -> {
                    p3.setPreviousAddresses(Collections.emptyList());
                    return p3;
                }))},
                {"Json string input", "[\n" +
                        "  {\n" +
                        "    \"p1\": {\n" +
                        "      \"firstName\": \"Different first name\",\n" +
                        "      \"lastName\": \"LastName1\",\n" +
                        "      \"email\": \"e1@e.mail\",\n" +
                        "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"BELGIUM\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"BELGIUM\",\n" +
                        "        \"city\": \"CityName1\",\n" +
                        "        \"streetName\": \"StreetName60\",\n" +
                        "        \"streetNumber\": 43,\n" +
                        "        \"postCode\": \"PostCode64\",\n" +
                        "        \"since\": \"2017-04-02\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"city\": \"CityName11\",\n" +
                        "          \"streetName\": \"StreetName70\",\n" +
                        "          \"streetNumber\": 53,\n" +
                        "          \"postCode\": \"PostCode74\",\n" +
                        "          \"since\": \"2017-04-12\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"p2\": {\n" +
                        "      \"firstName\": \"FirstName2\",\n" +
                        "      \"lastName\": \"LastName2\",\n" +
                        "      \"email\": \"e2@e.mail\",\n" +
                        "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"CANADA\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"AUSTRIA\",\n" +
                        "        \"city\": \"CityName2\",\n" +
                        "        \"streetName\": \"StreetName61\",\n" +
                        "        \"streetNumber\": 44,\n" +
                        "        \"postCode\": \"PostCode65\",\n" +
                        "        \"since\": \"2017-04-03\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"city\": \"CityName12\",\n" +
                        "          \"streetName\": \"StreetName71\",\n" +
                        "          \"streetNumber\": 54,\n" +
                        "          \"postCode\": \"PostCode75\",\n" +
                        "          \"since\": \"2017-04-13\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"city\": \"CityName13\",\n" +
                        "          \"streetName\": \"StreetName72\",\n" +
                        "          \"streetNumber\": 55,\n" +
                        "          \"postCode\": \"PostCode76\",\n" +
                        "          \"since\": \"2017-04-14\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"p3\": {\n" +
                        "      \"firstName\": \"FirstName3\",\n" +
                        "      \"lastName\": \"LastName3\",\n" +
                        "      \"email\": \"e3@e.mail\",\n" +
                        "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"DENMARK\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName3\",\n" +
                        "        \"streetName\": \"StreetName62\",\n" +
                        "        \"streetNumber\": 45,\n" +
                        "        \"postCode\": \"PostCode66\",\n" +
                        "        \"since\": \"2017-04-04\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": []\n" +
                        "    }\n" +
                        "  }\n" +
                        "]"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInMap")
    public void simpleDifferenceInMapTest(String testName, Object input) {
        String approvedFileContent = "[\n" +
                "  {\n" +
                "    \"p1\": {\n" +
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43,\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"since\": \"2017-04-02\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53,\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"since\": \"2017-04-12\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"p2\": {\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"streetNumber\": 44,\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54,\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"since\": \"2017-04-13\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55,\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"p3\": {\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"streetNumber\": 45,\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55,\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"streetNumber\": 56,\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57,\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"since\": \"2017-04-16\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName"), is("country")).ignoring(is("previousAddresses")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("[0].p1.firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; [1].p2.currentAddress.country\n" +
                "Expected: CANADA\n" +
                "     got: AUSTRIA\n" +
                " ; [2].p3.previousAddresses[]: Expected 3 values but got 0"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("previousAddresses")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("[0].p1.firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: Different first name\n" +
                    " ; [1].p2.currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: AUSTRIA\n"), thrown.getMessage());

            String actual = "[\n" +
                    "  {\n" +
                    "    \"p1\": {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"firstName\": \"Different first name\",\n" +
                    "      \"lastName\": \"LastName1\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"p2\": {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"p3\": {\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]";

            String expected = "[\n" +
                    "  {\n" +
                    "    \"p1\": {\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43,\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"since\": \"2017-04-02\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"p2\": {\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44,\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"p3\": {\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"streetNumber\": 45,\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "]";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "previousAddresses shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "previousAddresses shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] simpleDiffInMapAsGenericProperty() {
        return new Object[][]{
                {"Object input", BeanWithGeneric.of("String1", ImmutableMap.of("p1", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    return p;
                }), "p2", modifyObject(generatePerson(2L), p2 -> {
                    p2.getCurrentAddress().setCountry(Country.AUSTRIA);
                    return p2;
                }), "p3", modifyObject(generatePerson(3L), p3 -> {
                    p3.setPreviousAddresses(Collections.emptyList());
                    return p3;
                })))},
                {"Json string input", "{\n" +
                        "  \"dummyString\": \"String1\",\n" +
                        "  \"genericValue\": [\n" +
                        "    {\n" +
                        "      \"p1\": {\n" +
                        "        \"firstName\": \"Different first name\",\n" +
                        "        \"lastName\": \"LastName1\",\n" +
                        "        \"email\": \"e1@e.mail\",\n" +
                        "        \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "        \"birthCountry\": \"BELGIUM\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName1\",\n" +
                        "          \"streetName\": \"StreetName60\",\n" +
                        "          \"streetNumber\": 43,\n" +
                        "          \"postCode\": \"PostCode64\",\n" +
                        "          \"since\": \"2017-04-02\"\n" +
                        "        },\n" +
                        "        \"previousAddresses\": [\n" +
                        "          {\n" +
                        "            \"country\": \"EGYPT\",\n" +
                        "            \"city\": \"CityName11\",\n" +
                        "            \"streetName\": \"StreetName70\",\n" +
                        "            \"streetNumber\": 53,\n" +
                        "            \"postCode\": \"PostCode74\",\n" +
                        "            \"since\": \"2017-04-12\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"p2\": {\n" +
                        "        \"firstName\": \"FirstName2\",\n" +
                        "        \"lastName\": \"LastName2\",\n" +
                        "        \"email\": \"e2@e.mail\",\n" +
                        "        \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "        \"birthCountry\": \"CANADA\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"city\": \"CityName2\",\n" +
                        "          \"streetName\": \"StreetName61\",\n" +
                        "          \"streetNumber\": 44,\n" +
                        "          \"postCode\": \"PostCode65\",\n" +
                        "          \"since\": \"2017-04-03\"\n" +
                        "        },\n" +
                        "        \"previousAddresses\": [\n" +
                        "          {\n" +
                        "            \"country\": \"FRANCE\",\n" +
                        "            \"city\": \"CityName12\",\n" +
                        "            \"streetName\": \"StreetName71\",\n" +
                        "            \"streetNumber\": 54,\n" +
                        "            \"postCode\": \"PostCode75\",\n" +
                        "            \"since\": \"2017-04-13\"\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"country\": \"HUNGARY\",\n" +
                        "            \"city\": \"CityName13\",\n" +
                        "            \"streetName\": \"StreetName72\",\n" +
                        "            \"streetNumber\": 55,\n" +
                        "            \"postCode\": \"PostCode76\",\n" +
                        "            \"since\": \"2017-04-14\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"p3\": {\n" +
                        "        \"firstName\": \"FirstName3\",\n" +
                        "        \"lastName\": \"LastName3\",\n" +
                        "        \"email\": \"e3@e.mail\",\n" +
                        "        \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                        "        \"birthCountry\": \"DENMARK\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"city\": \"CityName3\",\n" +
                        "          \"streetName\": \"StreetName62\",\n" +
                        "          \"streetNumber\": 45,\n" +
                        "          \"postCode\": \"PostCode66\",\n" +
                        "          \"since\": \"2017-04-04\"\n" +
                        "        },\n" +
                        "        \"previousAddresses\": []\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInMapAsGenericProperty")
    public void simpleDifferenceInMapAsGenericPropertyTest(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"genericValue\": [\n" +
                "  {\n" +
                "    \"p1\": {\n" +
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43,\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"since\": \"2017-04-02\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53,\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"since\": \"2017-04-12\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"p2\": {\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"streetNumber\": 44,\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54,\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"since\": \"2017-04-13\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55,\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"p3\": {\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"streetNumber\": 45,\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55,\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"streetNumber\": 56,\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57,\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"since\": \"2017-04-16\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName"), is("country")).ignoring(is("previousAddresses")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("genericValue[0].p1.firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; genericValue[1].p2.currentAddress.country\n" +
                "Expected: CANADA\n" +
                "     got: AUSTRIA\n" +
                " ; genericValue[2].p3.previousAddresses[]: Expected 3 values but got 0"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("previousAddresses")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("genericValue[0].p1.firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: Different first name\n" +
                    " ; genericValue[1].p2.currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: AUSTRIA\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"genericValue\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"BELGIUM\",\n" +
                    "        \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName1\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode64\",\n" +
                    "          \"since\": \"2017-04-02\",\n" +
                    "          \"streetName\": \"StreetName60\",\n" +
                    "          \"streetNumber\": 43\n" +
                    "        },\n" +
                    "        \"email\": \"e1@e.mail\",\n" +
                    "        \"firstName\": \"Different first name\",\n" +
                    "        \"lastName\": \"LastName1\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"CANADA\",\n" +
                    "        \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName2\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode65\",\n" +
                    "          \"since\": \"2017-04-03\",\n" +
                    "          \"streetName\": \"StreetName61\",\n" +
                    "          \"streetNumber\": 44\n" +
                    "        },\n" +
                    "        \"email\": \"e2@e.mail\",\n" +
                    "        \"firstName\": \"FirstName2\",\n" +
                    "        \"lastName\": \"LastName2\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p3\": {\n" +
                    "        \"birthCountry\": \"DENMARK\",\n" +
                    "        \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName3\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode66\",\n" +
                    "          \"since\": \"2017-04-04\",\n" +
                    "          \"streetName\": \"StreetName62\",\n" +
                    "          \"streetNumber\": 45\n" +
                    "        },\n" +
                    "        \"email\": \"e3@e.mail\",\n" +
                    "        \"firstName\": \"FirstName3\",\n" +
                    "        \"lastName\": \"LastName3\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"genericValue\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"firstName\": \"FirstName1\",\n" +
                    "        \"lastName\": \"LastName1\",\n" +
                    "        \"email\": \"e1@e.mail\",\n" +
                    "        \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"BELGIUM\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"city\": \"CityName1\",\n" +
                    "          \"streetName\": \"StreetName60\",\n" +
                    "          \"streetNumber\": 43,\n" +
                    "          \"postCode\": \"PostCode64\",\n" +
                    "          \"since\": \"2017-04-02\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"firstName\": \"FirstName2\",\n" +
                    "        \"lastName\": \"LastName2\",\n" +
                    "        \"email\": \"e2@e.mail\",\n" +
                    "        \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"CANADA\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"city\": \"CityName2\",\n" +
                    "          \"streetName\": \"StreetName61\",\n" +
                    "          \"streetNumber\": 44,\n" +
                    "          \"postCode\": \"PostCode65\",\n" +
                    "          \"since\": \"2017-04-03\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p3\": {\n" +
                    "        \"firstName\": \"FirstName3\",\n" +
                    "        \"lastName\": \"LastName3\",\n" +
                    "        \"email\": \"e3@e.mail\",\n" +
                    "        \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"DENMARK\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"city\": \"CityName3\",\n" +
                    "          \"streetName\": \"StreetName62\",\n" +
                    "          \"streetNumber\": 45,\n" +
                    "          \"postCode\": \"PostCode66\",\n" +
                    "          \"since\": \"2017-04-04\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "previousAddresses shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "previousAddresses shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] simpleDiffInMapAsProperty() {
        return new Object[][]{
                {"Object input", BeanWithGenericIterable.Builder.bean().dummyString("String1").map(ImmutableMap.of("p1", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Different first name");
                    return p;
                }), "p2", modifyObject(generatePerson(2L), p2 -> {
                    p2.getCurrentAddress().setCountry(Country.AUSTRIA);
                    return p2;
                }), "p3", modifyObject(generatePerson(3L), p3 -> {
                    p3.setPreviousAddresses(Collections.emptyList());
                    return p3;
                })))},
                {"Json string input", "{\n" +
                        "  \"dummyString\": \"String1\",\n" +
                        "  \"map\": [\n" +
                        "    {\n" +
                        "      \"p1\": {\n" +
                        "        \"firstName\": \"Different first name\",\n" +
                        "        \"lastName\": \"LastName1\",\n" +
                        "        \"email\": \"e1@e.mail\",\n" +
                        "        \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                        "        \"birthCountry\": \"BELGIUM\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName1\",\n" +
                        "          \"streetName\": \"StreetName60\",\n" +
                        "          \"streetNumber\": 43,\n" +
                        "          \"postCode\": \"PostCode64\",\n" +
                        "          \"since\": \"2017-04-02\"\n" +
                        "        },\n" +
                        "        \"previousAddresses\": [\n" +
                        "          {\n" +
                        "            \"country\": \"EGYPT\",\n" +
                        "            \"city\": \"CityName11\",\n" +
                        "            \"streetName\": \"StreetName70\",\n" +
                        "            \"streetNumber\": 53,\n" +
                        "            \"postCode\": \"PostCode74\",\n" +
                        "            \"since\": \"2017-04-12\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"p2\": {\n" +
                        "        \"firstName\": \"FirstName2\",\n" +
                        "        \"lastName\": \"LastName2\",\n" +
                        "        \"email\": \"e2@e.mail\",\n" +
                        "        \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "        \"birthCountry\": \"CANADA\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"city\": \"CityName2\",\n" +
                        "          \"streetName\": \"StreetName61\",\n" +
                        "          \"streetNumber\": 44,\n" +
                        "          \"postCode\": \"PostCode65\",\n" +
                        "          \"since\": \"2017-04-03\"\n" +
                        "        },\n" +
                        "        \"previousAddresses\": [\n" +
                        "          {\n" +
                        "            \"country\": \"FRANCE\",\n" +
                        "            \"city\": \"CityName12\",\n" +
                        "            \"streetName\": \"StreetName71\",\n" +
                        "            \"streetNumber\": 54,\n" +
                        "            \"postCode\": \"PostCode75\",\n" +
                        "            \"since\": \"2017-04-13\"\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"country\": \"HUNGARY\",\n" +
                        "            \"city\": \"CityName13\",\n" +
                        "            \"streetName\": \"StreetName72\",\n" +
                        "            \"streetNumber\": 55,\n" +
                        "            \"postCode\": \"PostCode76\",\n" +
                        "            \"since\": \"2017-04-14\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"p3\": {\n" +
                        "        \"firstName\": \"FirstName3\",\n" +
                        "        \"lastName\": \"LastName3\",\n" +
                        "        \"email\": \"e3@e.mail\",\n" +
                        "        \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                        "        \"birthCountry\": \"DENMARK\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"city\": \"CityName3\",\n" +
                        "          \"streetName\": \"StreetName62\",\n" +
                        "          \"streetNumber\": 45,\n" +
                        "          \"postCode\": \"PostCode66\",\n" +
                        "          \"since\": \"2017-04-04\"\n" +
                        "        },\n" +
                        "        \"previousAddresses\": []\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInMapAsProperty")
    public void simpleDifferenceInMapAsPropertyTest(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"map\": [\n" +
                "  {\n" +
                "    \"p1\": {\n" +
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43,\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"since\": \"2017-04-02\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53,\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"since\": \"2017-04-12\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"p2\": {\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"streetNumber\": 44,\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54,\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"since\": \"2017-04-13\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55,\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"p3\": {\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"streetNumber\": 45,\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55,\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"streetNumber\": 56,\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57,\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"since\": \"2017-04-16\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("firstName"), is("country")).ignoring(is("previousAddresses")), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), identity(), getExcceptionMessageForDummyTestInfo("map[0].p1.firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; map[1].p2.currentAddress.country\n" +
                "Expected: CANADA\n" +
                "     got: AUSTRIA\n" +
                " ; map[2].p3.previousAddresses[]: Expected 3 values but got 0"));

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, getDefaultFileMatcherConfigWithLenientMatching(), jsonMatcher -> jsonMatcher.ignoring(is("previousAddresses")), thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("map[0].p1.firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: Different first name\n" +
                    " ; map[1].p2.currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: AUSTRIA\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"map\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"BELGIUM\",\n" +
                    "        \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName1\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode64\",\n" +
                    "          \"since\": \"2017-04-02\",\n" +
                    "          \"streetName\": \"StreetName60\",\n" +
                    "          \"streetNumber\": 43\n" +
                    "        },\n" +
                    "        \"email\": \"e1@e.mail\",\n" +
                    "        \"firstName\": \"Different first name\",\n" +
                    "        \"lastName\": \"LastName1\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"CANADA\",\n" +
                    "        \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName2\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode65\",\n" +
                    "          \"since\": \"2017-04-03\",\n" +
                    "          \"streetName\": \"StreetName61\",\n" +
                    "          \"streetNumber\": 44\n" +
                    "        },\n" +
                    "        \"email\": \"e2@e.mail\",\n" +
                    "        \"firstName\": \"FirstName2\",\n" +
                    "        \"lastName\": \"LastName2\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p3\": {\n" +
                    "        \"birthCountry\": \"DENMARK\",\n" +
                    "        \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName3\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode66\",\n" +
                    "          \"since\": \"2017-04-04\",\n" +
                    "          \"streetName\": \"StreetName62\",\n" +
                    "          \"streetNumber\": 45\n" +
                    "        },\n" +
                    "        \"email\": \"e3@e.mail\",\n" +
                    "        \"firstName\": \"FirstName3\",\n" +
                    "        \"lastName\": \"LastName3\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"map\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"firstName\": \"FirstName1\",\n" +
                    "        \"lastName\": \"LastName1\",\n" +
                    "        \"email\": \"e1@e.mail\",\n" +
                    "        \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"BELGIUM\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"city\": \"CityName1\",\n" +
                    "          \"streetName\": \"StreetName60\",\n" +
                    "          \"streetNumber\": 43,\n" +
                    "          \"postCode\": \"PostCode64\",\n" +
                    "          \"since\": \"2017-04-02\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"firstName\": \"FirstName2\",\n" +
                    "        \"lastName\": \"LastName2\",\n" +
                    "        \"email\": \"e2@e.mail\",\n" +
                    "        \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"CANADA\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"city\": \"CityName2\",\n" +
                    "          \"streetName\": \"StreetName61\",\n" +
                    "          \"streetNumber\": 44,\n" +
                    "          \"postCode\": \"PostCode65\",\n" +
                    "          \"since\": \"2017-04-03\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p3\": {\n" +
                    "        \"firstName\": \"FirstName3\",\n" +
                    "        \"lastName\": \"LastName3\",\n" +
                    "        \"email\": \"e3@e.mail\",\n" +
                    "        \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"DENMARK\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"city\": \"CityName3\",\n" +
                    "          \"streetName\": \"StreetName62\",\n" +
                    "          \"streetNumber\": 45,\n" +
                    "          \"postCode\": \"PostCode66\",\n" +
                    "          \"since\": \"2017-04-04\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "previousAddresses shouldn't be present");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "previousAddresses shouldn't be present");
        }, AssertionFailedError.class);
    }

    public static Object[][] multipleMultiLevelPathInCollectionWithDiffNotApprovedCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.setName("Should not see this in not approved file!");
                    t.getLead().setLastName("Should not see this in not approved file!");
                    t.getMembers().get(0).getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    t.getMembers().get(1).setBirthCountry(Country.HUNGARY);
                    t.getMembers().get(1).setEmail("Should not see this in not approved file!");
                    return t;
                })},
                {"Json string input", "{\n" +
                        "  \"name\": \"Should not see this in not approved file!\",\n" +
                        "  \"lead\": {\n" +
                        "    \"firstName\": \"FirstName13\",\n" +
                        "    \"lastName\": \"Should not see this in not approved file!\",\n" +
                        "    \"email\": \"e13@e.mail\",\n" +
                        "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                        "    \"birthCountry\": \"HUNGARY\",\n" +
                        "    \"currentAddress\": {\n" +
                        "      \"country\": \"HUNGARY\",\n" +
                        "      \"city\": \"CityName13\",\n" +
                        "      \"streetName\": \"StreetName72\",\n" +
                        "      \"streetNumber\": 55,\n" +
                        "      \"postCode\": \"PostCode76\",\n" +
                        "      \"since\": \"2017-04-14\"\n" +
                        "    },\n" +
                        "    \"previousAddresses\": [\n" +
                        "      {\n" +
                        "        \"country\": \"CANADA\",\n" +
                        "        \"city\": \"CityName23\",\n" +
                        "        \"streetName\": \"StreetName82\",\n" +
                        "        \"streetNumber\": 65,\n" +
                        "        \"postCode\": \"PostCode86\",\n" +
                        "        \"since\": \"2017-04-24\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"DENMARK\",\n" +
                        "        \"city\": \"CityName24\",\n" +
                        "        \"streetName\": \"StreetName83\",\n" +
                        "        \"streetNumber\": 66,\n" +
                        "        \"postCode\": \"PostCode87\",\n" +
                        "        \"since\": \"2017-04-25\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName25\",\n" +
                        "        \"streetName\": \"StreetName84\",\n" +
                        "        \"streetNumber\": 67,\n" +
                        "        \"postCode\": \"PostCode88\",\n" +
                        "        \"since\": \"2017-04-26\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"members\": [\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName102\",\n" +
                        "      \"lastName\": \"LastName102\",\n" +
                        "      \"email\": \"e102@e.mail\",\n" +
                        "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"EGYPT\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"EGYPT\",\n" +
                        "        \"city\": \"CityName102\",\n" +
                        "        \"streetName\": \"StreetName161\",\n" +
                        "        \"streetNumber\": 144,\n" +
                        "        \"postCode\": \"PostCode165\",\n" +
                        "        \"since\": \"2020-12-25\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"city\": \"CityName112\",\n" +
                        "          \"streetName\": \"StreetName171\",\n" +
                        "          \"streetNumber\": 154,\n" +
                        "          \"postCode\": \"PostCode175\",\n" +
                        "          \"since\": \"2017-07-22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"firstName\": \"FirstName103\",\n" +
                        "      \"lastName\": \"LastName103\",\n" +
                        "      \"email\": \"Should not see this in not approved file!\",\n" +
                        "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                        "      \"birthCountry\": \"HUNGARY\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"country\": \"FRANCE\",\n" +
                        "        \"city\": \"CityName103\",\n" +
                        "        \"streetName\": \"StreetName162\",\n" +
                        "        \"streetNumber\": 145,\n" +
                        "        \"postCode\": \"PostCode166\",\n" +
                        "        \"since\": \"2017-07-13\"\n" +
                        "      },\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"city\": \"CityName113\",\n" +
                        "          \"streetName\": \"StreetName172\",\n" +
                        "          \"streetNumber\": 155,\n" +
                        "          \"postCode\": \"PostCode176\",\n" +
                        "          \"since\": \"2017-07-23\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"CANADA\",\n" +
                        "          \"city\": \"CityName114\",\n" +
                        "          \"streetName\": \"StreetName173\",\n" +
                        "          \"streetNumber\": 156,\n" +
                        "          \"postCode\": \"PostCode177\",\n" +
                        "          \"since\": \"2017-07-24\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"city\": \"CityName115\",\n" +
                        "          \"streetName\": \"StreetName174\",\n" +
                        "          \"streetNumber\": 157,\n" +
                        "          \"postCode\": \"PostCode178\",\n" +
                        "          \"since\": \"2017-07-25\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleMultiLevelPathInCollectionWithDiffNotApprovedCases")
    public void notApprovedFileCreatedWithIgnoredPathsNotPresentInItForMultipleMultiLevelIgnoreWithCollection(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"lead\": {\n" +
                "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                "    \"currentAddress\": {\n" +
                "      \"city\": \"CityName13\",\n" +
                "      \"country\": \"HUNGARY\",\n" +
                "      \"postCode\": \"PostCode76\",\n" +
                "      \"streetName\": \"StreetName72\",\n" +
                "      \"streetNumber\": 55\n" +
                "    },\n" +
                "    \"firstName\": \"FirstName13\",\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"city\": \"CityName23\",\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"postCode\": \"PostCode86\",\n" +
                "        \"streetName\": \"StreetName82\",\n" +
                "        \"streetNumber\": 65\n" +
                "      },\n" +
                "      {\n" +
                "        \"city\": \"CityName24\",\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"postCode\": \"PostCode87\",\n" +
                "        \"streetName\": \"StreetName83\",\n" +
                "        \"streetNumber\": 66\n" +
                "      },\n" +
                "      {\n" +
                "        \"city\": \"CityName25\",\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"postCode\": \"PostCode88\",\n" +
                "        \"streetName\": \"StreetName84\",\n" +
                "        \"streetNumber\": 67\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName102\",\n" +
                "        \"country\": \"EGYPT\",\n" +
                "        \"postCode\": \"PostCode165\",\n" +
                "        \"streetName\": \"StreetName161\",\n" +
                "        \"streetNumber\": 144\n" +
                "      },\n" +
                "      \"firstName\": \"FirstName102\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName112\",\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"postCode\": \"PostCode175\",\n" +
                "          \"streetName\": \"StreetName171\",\n" +
                "          \"streetNumber\": 154\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName103\",\n" +
                "        \"country\": \"FRANCE\",\n" +
                "        \"postCode\": \"PostCode166\",\n" +
                "        \"streetName\": \"StreetName162\",\n" +
                "        \"streetNumber\": 145\n" +
                "      },\n" +
                "      \"firstName\": \"FirstName103\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"postCode\": \"PostCode176\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName114\",\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"postCode\": \"PostCode177\",\n" +
                "          \"streetName\": \"StreetName173\",\n" +
                "          \"streetNumber\": 156\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName115\",\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"postCode\": \"PostCode178\",\n" +
                "          \"streetName\": \"StreetName174\",\n" +
                "          \"streetNumber\": 157\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(is("since"), is("birthCountry"), is("name")).ignoring(is("lastName")).ignoring(is("email")));
    }
}
