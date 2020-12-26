package com.github.karsaig.approvalcrest.matcher.ignores;

import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generatePerson;
import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generateTeam;
import static java.util.function.Function.identity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.Address;
import com.github.karsaig.approvalcrest.testdata.Country;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class JsonMatcherIgnoreClassTest extends AbstractFileMatcherTest {

    public static Object[][] simpleDiffCases() {
        return new Object[][]{
                {"Object input", generatePerson(1L)}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffCases")
    public void assertShouldBeSuccessfulWhenStringIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(String.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo("currentAddress\n" +
                "Unexpected: city\n" +
                " ; currentAddress\n" +
                "Unexpected: postCode\n" +
                " ; currentAddress\n" +
                "Unexpected: streetName\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: city\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: postCode\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: streetName\n" +
                " ; \n" +
                "Unexpected: email\n" +
                " ; \n" +
                "Unexpected: firstName\n" +
                " ; \n" +
                "Unexpected: lastName\n"));
    }


    public static Object[][] notApprovedIgnoreCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName("Should not see this in not approved file");
                    return p;
                })}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("notApprovedIgnoreCases")
    public void notApprovedFileCreatedWithIgnoredClassNotPresentInIt(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(String.class));
    }


    public static Object[][] simplePathIgnoreWithNullCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setFirstName(null);
                    return p;
                })}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simplePathIgnoreWithNullCases")
    public void assertShouldBeSuccessfulWhenStringWithNullValueIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "  \"birthCountry\": \"BELGIUM\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"country\": \"BELGIUM\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"country\": \"EGYPT\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(String.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "currentAddress\n" +
                "Unexpected: city\n" +
                " ; currentAddress\n" +
                "Unexpected: postCode\n" +
                " ; currentAddress\n" +
                "Unexpected: streetName\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: city\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: postCode\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: streetName\n" +
                " ; \n" +
                "Unexpected: email\n" +
                " ; \n" +
                "Unexpected: lastName\n"));
    }


    public static Object[][] multiLevelPathCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.getLead().getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    return t;
                })}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multiLevelPathCases")
    public void assertShouldBeSuccessfulWhenMultiLevelPathWithClassIsIgnored(String testName, Object input) {
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
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDate.class), null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "lead.currentAddress\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[2]\n" +
                "Unexpected: since\n" +
                " ; members[0].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[1].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[2]\n" +
                "Unexpected: since\n"));
    }


    public static Object[][] multiLevelPathInCollectionCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.getMembers().get(0).getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    return t;
                })}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multiLevelPathInCollectionCases")
    public void assertShouldBeSuccessfulWhenMultiLevelPathInCollectionWithClassIsIgnored(String testName, Object input) {
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
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDate.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "lead.currentAddress\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[2]\n" +
                "Unexpected: since\n" +
                " ; members[0].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[1].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[2]\n" +
                "Unexpected: since\n"));

    }

    public static Object[][] multiLevelPathInCollectionWithNullCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.getMembers().get(0).getCurrentAddress().setSince(null);
                    return t;
                })}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multiLevelPathInCollectionWithNullCases")
    public void assertShouldBeSuccessfulWhenMultiLevelPathInCollectionWithNullClassIsIgnored(String testName, Object input) {
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

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDate.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "lead.currentAddress\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[2]\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[1].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[2]\n" +
                "Unexpected: since\n"));
    }


    public static Object[][] multipleSimplePathDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setBirthCountry(Country.HUNGARY);
                    p.setBirthDate(LocalDateTime.of(2021, 1, 2, 17, 51, 11, 13));
                    return p;
                })}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleSimplePathDiffCases")
    public void assertShouldBeSuccessfulWhenMultipleSimplePathWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"firstName\": \"FirstName1\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDateTime.class).ignoring(Country.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "currentAddress\n" +
                "Unexpected: country\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: country\n" +
                " ; \n" +
                "Unexpected: birthCountry\n" +
                " ; \n" +
                "Unexpected: birthDate\n"));
    }

    public static Object[][] multipleSimpleDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.setBirthCountry(Country.HUNGARY);
                    p.setBirthDate(LocalDateTime.of(2021, 1, 2, 17, 51, 11, 13));
                    return p;
                })}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleSimpleDiffCases")
    public void assertShouldBeSuccessfulWhenMultipleSimplePathSingleIgnoreWithDifferenceIsIgnored(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"firstName\": \"FirstName1\",\n" +
                "  \"lastName\": \"LastName1\",\n" +
                "  \"email\": \"e1@e.mail\",\n" +
                "  \"currentAddress\": {\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\",\n" +
                "    \"since\": \"2017-04-02\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\",\n" +
                "      \"since\": \"2017-04-12\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDateTime.class, Country.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "currentAddress\n" +
                "Unexpected: country\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: country\n" +
                " ; \n" +
                "Unexpected: birthCountry\n" +
                " ; \n" +
                "Unexpected: birthDate\n"));
    }

    public static Object[][] multipleMultiLevelPathWithDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    p.getCurrentAddress().setCountry(Country.HUNGARY);
                    return p;
                })}
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
                "  \"currentAddress\": {\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDate.class).ignoring(Country.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "currentAddress\n" +
                "Unexpected: country\n" +
                " ; currentAddress\n" +
                "Unexpected: since\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: country\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; \n" +
                "Unexpected: birthCountry\n"));
    }

    public static Object[][] multipleMultiLevelPathSingleIngoreWithDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generatePerson(1L), p -> {
                    p.getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    p.getCurrentAddress().setCountry(Country.HUNGARY);
                    return p;
                })}
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
                "  \"currentAddress\": {\n" +
                "    \"city\": \"CityName1\",\n" +
                "    \"streetName\": \"StreetName60\",\n" +
                "    \"streetNumber\": 43,\n" +
                "    \"postCode\": \"PostCode64\"\n" +
                "  },\n" +
                "  \"previousAddresses\": [\n" +
                "    {\n" +
                "      \"city\": \"CityName11\",\n" +
                "      \"streetName\": \"StreetName70\",\n" +
                "      \"streetNumber\": 53,\n" +
                "      \"postCode\": \"PostCode74\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDate.class, Country.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "currentAddress\n" +
                "Unexpected: country\n" +
                " ; currentAddress\n" +
                "Unexpected: since\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: country\n" +
                " ; previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; \n" +
                "Unexpected: birthCountry\n"));
    }


    public static Object[][] multipleMultiLevelPathInCollectionWithDiffCases() {
        return new Object[][]{
                {"Object input", modifyObject(generateTeam(2L), t -> {
                    t.getMembers().get(0).getCurrentAddress().setSince(LocalDate.of(2020, 12, 25));
                    t.getMembers().get(1).setBirthCountry(Country.HUNGARY);
                    return t;
                })}
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
                "    \"currentAddress\": {\n" +
                "      \"city\": \"CityName13\",\n" +
                "      \"streetName\": \"StreetName72\",\n" +
                "      \"streetNumber\": 55,\n" +
                "      \"postCode\": \"PostCode76\"\n" +
                "    },\n" +
                "    \"previousAddresses\": [\n" +
                "      {\n" +
                "        \"city\": \"CityName23\",\n" +
                "        \"streetName\": \"StreetName82\",\n" +
                "        \"streetNumber\": 65,\n" +
                "        \"postCode\": \"PostCode86\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"city\": \"CityName24\",\n" +
                "        \"streetName\": \"StreetName83\",\n" +
                "        \"streetNumber\": 66,\n" +
                "        \"postCode\": \"PostCode87\"\n" +
                "      },\n" +
                "      {\n" +
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
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName102\",\n" +
                "        \"streetName\": \"StreetName161\",\n" +
                "        \"streetNumber\": 144,\n" +
                "        \"postCode\": \"PostCode165\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName112\",\n" +
                "          \"streetName\": \"StreetName171\",\n" +
                "          \"streetNumber\": 154,\n" +
                "          \"postCode\": \"PostCode175\"\n" +
                "        },\n" +
                "        {\n" +
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
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName103\",\n" +
                "        \"streetName\": \"StreetName162\",\n" +
                "        \"streetNumber\": 145,\n" +
                "        \"postCode\": \"PostCode166\"\n" +
                "      },\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName113\",\n" +
                "          \"streetName\": \"StreetName172\",\n" +
                "          \"streetNumber\": 155,\n" +
                "          \"postCode\": \"PostCode176\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName114\",\n" +
                "          \"streetName\": \"StreetName173\",\n" +
                "          \"streetNumber\": 156,\n" +
                "          \"postCode\": \"PostCode177\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName115\",\n" +
                "          \"streetName\": \"StreetName174\",\n" +
                "          \"streetNumber\": 157,\n" +
                "          \"postCode\": \"PostCode178\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDate.class, Country.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "lead.currentAddress\n" +
                "Unexpected: country\n" +
                " ; lead.currentAddress\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[0]\n" +
                "Unexpected: country\n" +
                " ; lead.previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[1]\n" +
                "Unexpected: country\n" +
                " ; lead.previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; lead.previousAddresses[2]\n" +
                "Unexpected: country\n" +
                " ; lead.previousAddresses[2]\n" +
                "Unexpected: since\n" +
                " ; lead\n" +
                "Unexpected: birthCountry\n" +
                " ; members[0].currentAddress\n" +
                "Unexpected: country\n" +
                " ; members[0].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[0]\n" +
                "Unexpected: country\n" +
                " ; members[0].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[0].previousAddresses[1]\n" +
                "Unexpected: country\n" +
                " ; members[0].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[0]\n" +
                "Unexpected: birthCountry\n" +
                " ; members[1].currentAddress\n" +
                "Unexpected: country\n" +
                " ; members[1].currentAddress\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[0]\n" +
                "Unexpected: country\n" +
                " ; members[1].previousAddresses[0]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[1]\n" +
                "Unexpected: country\n" +
                " ; members[1].previousAddresses[1]\n" +
                "Unexpected: since\n" +
                " ; members[1].previousAddresses[2]\n" +
                "Unexpected: country\n" +
                " ; members[1].previousAddresses[2]\n" +
                "Unexpected: since\n" +
                " ; members[1]\n" +
                "Unexpected: birthCountry\n"));

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
                }))}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInList")
    public void simpleDifferenceInListTest(String testName, Object input) {
        String approvedFileContent = "[\n" +
                "  {\n" +
                "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "    \"previousAddresses\": [\n" +
                "      null\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "    \"previousAddresses\": [\n" +
                "      null,\n" +
                "      null\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "    \"previousAddresses\": []\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(String.class, Country.class).ignoring(Address.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "[0].previousAddresses[0]\n" +
                "Expected: null\n" +
                "     got: a JSON object\n" +
                " ; [0]\n" +
                "Unexpected: birthCountry\n" +
                " ; [0]\n" +
                "Unexpected: currentAddress\n" +
                " ; [0]\n" +
                "Unexpected: email\n" +
                " ; [0]\n" +
                "Unexpected: firstName\n" +
                " ; [0]\n" +
                "Unexpected: lastName\n" +
                " ; [1].previousAddresses[0]\n" +
                "Expected: null\n" +
                "     got: a JSON object\n" +
                " ; [1].previousAddresses[1]\n" +
                "Expected: null\n" +
                "     got: a JSON object\n" +
                " ; [1]\n" +
                "Unexpected: birthCountry\n" +
                " ; [1]\n" +
                "Unexpected: currentAddress\n" +
                " ; [1]\n" +
                "Unexpected: email\n" +
                " ; [1]\n" +
                "Unexpected: firstName\n" +
                " ; [1]\n" +
                "Unexpected: lastName\n" +
                " ; [2]\n" +
                "Unexpected: birthCountry\n" +
                " ; [2]\n" +
                "Unexpected: currentAddress\n" +
                " ; [2]\n" +
                "Unexpected: email\n" +
                " ; [2]\n" +
                "Unexpected: firstName\n" +
                " ; [2]\n" +
                "Unexpected: lastName\n"));
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
                }))}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInSet")
    public void simpleDifferenceInSetTest(String testName, Object input) {
        String approvedFileContent = "[\n" +
                "  {\n" +
                "    \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "    \"previousAddresses\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "    \"previousAddresses\": [\n" +
                "      null,\n" +
                "      null\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "    \"previousAddresses\": [\n" +
                "      null\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(String.class, Country.class).ignoring(Address.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "[0].birthDate\n" +
                "Expected: 2014-04-01T13:42:11\n" +
                "     got: 2016-04-01T13:42:11\n" +
                " ; [0].previousAddresses[]: Expected 0 values but got 1 ; [0]\n" +
                "Unexpected: birthCountry\n" +
                " ; [0]\n" +
                "Unexpected: currentAddress\n" +
                " ; [0]\n" +
                "Unexpected: email\n" +
                " ; [0]\n" +
                "Unexpected: firstName\n" +
                " ; [0]\n" +
                "Unexpected: lastName\n" +
                " ; [1].previousAddresses[0]\n" +
                "Expected: null\n" +
                "     got: a JSON object\n" +
                " ; [1].previousAddresses[1]\n" +
                "Expected: null\n" +
                "     got: a JSON object\n" +
                " ; [1]\n" +
                "Unexpected: birthCountry\n" +
                " ; [1]\n" +
                "Unexpected: currentAddress\n" +
                " ; [1]\n" +
                "Unexpected: email\n" +
                " ; [1]\n" +
                "Unexpected: firstName\n" +
                " ; [1]\n" +
                "Unexpected: lastName\n" +
                " ; [2].birthDate\n" +
                "Expected: 2016-04-01T13:42:11\n" +
                "     got: 2014-04-01T13:42:11\n" +
                " ; [2].previousAddresses[]: Expected 1 values but got 0 ; [2]\n" +
                "Unexpected: birthCountry\n" +
                " ; [2]\n" +
                "Unexpected: currentAddress\n" +
                " ; [2]\n" +
                "Unexpected: email\n" +
                " ; [2]\n" +
                "Unexpected: firstName\n" +
                " ; [2]\n" +
                "Unexpected: lastName\n"));
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
                }))}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("simpleDiffInMap")
    public void simpleDifferenceInMapTest(String testName, Object input) {
        String approvedFileContent = "[\n" +
                "  {\n" +
                "    \"p3\": {\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"previousAddresses\": []\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"p2\": {\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"previousAddresses\": [\n" +
                "        null,\n" +
                "        null\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"p1\": {\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"previousAddresses\": [\n" +
                "        null\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(String.class, Country.class).ignoring(Address.class), null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, identity(), getExcceptionMessageForDummyTestInfo( "[0]\n" +
                "Expected: p3\n" +
                "     but none found\n" +
                " ; [0]\n" +
                "Unexpected: p1\n" +
                " ; [1].p2.previousAddresses[0]\n" +
                "Expected: null\n" +
                "     got: a JSON object\n" +
                " ; [1].p2.previousAddresses[1]\n" +
                "Expected: null\n" +
                "     got: a JSON object\n" +
                " ; [1].p2\n" +
                "Unexpected: birthCountry\n" +
                " ; [1].p2\n" +
                "Unexpected: currentAddress\n" +
                " ; [1].p2\n" +
                "Unexpected: email\n" +
                " ; [1].p2\n" +
                "Unexpected: firstName\n" +
                " ; [1].p2\n" +
                "Unexpected: lastName\n" +
                " ; [2]\n" +
                "Expected: p1\n" +
                "     but none found\n" +
                " ; [2]\n" +
                "Unexpected: p3\n"));
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
                })}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("multipleMultiLevelPathInCollectionWithDiffNotApprovedCases")
    public void notApprovedFileCreatedWithIgnoredPathsNotPresentInItForMultipleMultiLevelIgnoreWithCollection(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"lead\": {\n" +
                "    \"birthDate\": \"2004-04-01T13:42:11\",\n" +
                "    \"previousAddresses\": [\n" +
                "      null,\n" +
                "      null,\n" +
                "      null\n" +
                "    ]\n" +
                "  },\n" +
                "  \"members\": [\n" +
                "    {\n" +
                "      \"birthDate\": \"1915-04-01T13:42:11\",\n" +
                "      \"previousAddresses\": [\n" +
                "        null,\n" +
                "        null\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"1914-04-01T13:42:11\",\n" +
                "      \"previousAddresses\": [\n" +
                "        null,\n" +
                "        null,\n" +
                "        null\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfoForNotApprovedFile(input, approvedFileContent, jsonMatcher -> jsonMatcher.ignoring(LocalDate.class, Country.class, String.class).ignoring(Address.class));
    }
}
