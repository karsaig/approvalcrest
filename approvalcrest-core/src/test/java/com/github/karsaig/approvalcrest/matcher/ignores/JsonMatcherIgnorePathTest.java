package com.github.karsaig.approvalcrest.matcher.ignores;

import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generatePerson;
import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generateTeam;
import static java.util.function.Function.identity;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.Country;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class JsonMatcherIgnorePathTest extends AbstractFileMatcherTest {

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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("firstName"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("firstName\n" +
                "Expected: Different first name\n" +
                "     got: FirstName1\n"));
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("firstName"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("\n" +
                "Unexpected: firstName\n"));
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

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("firstName"));
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("firstName"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("\n" +
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
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("lead.currentAddress.since"), null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("lead.currentAddress.since\n" +
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
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("members.currentAddress.since"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("members[0].currentAddress.since\n" +
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("members.currentAddress.since"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("members[0].currentAddress\n" +
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("members.currentAddress.since"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("members[0].currentAddress\n" +
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("firstName").ignoring("lastName"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; lastName\n" +
                "Expected: LastName1\n" +
                "     got: Different last name\n"));
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("firstName", "lastName"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("firstName\n" +
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("currentAddress.since").ignoring("currentAddress.country"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("currentAddress.country\n" +
                "Expected: BELGIUM\n" +
                "     got: HUNGARY\n" +
                " ; currentAddress.since\n" +
                "Expected: 2017-04-02\n" +
                "     got: 2020-12-25\n"));
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("currentAddress.since", "currentAddress.country"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("currentAddress.country\n" +
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("members.currentAddress.since", "members.birthCountry"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("members[0].currentAddress.since\n" +
                "Expected: 2019-12-25\n" +
                "     got: 2020-12-25\n" +
                " ; members[1].birthCountry\n" +
                "Expected: FRANCE\n" +
                "     got: HUNGARY\n"));

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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("firstName", "currentAddress.country").ignoring("previousAddresses"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("[0].firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; [1].currentAddress.country\n" +
                "Expected: CANADA\n" +
                "     got: AUSTRIA\n" +
                " ; [2].previousAddresses[]: Expected 3 values but got 0"));
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("firstName", "currentAddress.country").ignoring("previousAddresses"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("[0].firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; [1].currentAddress.country\n" +
                "Expected: CANADA\n" +
                "     got: AUSTRIA\n" +
                " ; [2].previousAddresses[]: Expected 3 values but got 0"));
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("p1.firstName", "p2.currentAddress.country").ignoring("p3.previousAddresses"), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("[0].p1.firstName\n" +
                "Expected: FirstName1\n" +
                "     got: Different first name\n" +
                " ; [1].p2.currentAddress.country\n" +
                "Expected: CANADA\n" +
                "     got: AUSTRIA\n" +
                " ; [2].p3.previousAddresses[]: Expected 3 values but got 0"));
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
                "    \"firstName\": \"FirstName13\",\n" +
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
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
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
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
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

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring("members.currentAddress.since", "members.birthCountry", "name").ignoring("lead.lastName").ignoring("members.email"));
    }
}
