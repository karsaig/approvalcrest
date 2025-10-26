package com.github.karsaig.approvalcrest.matcher.sorting;

import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.matcher.ignores.AbstractJsonMatcherIgnoreTest;
import com.github.karsaig.approvalcrest.testdata.BeanWithGenericIterable;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.util.HashMap;
import java.util.function.Function;

import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generatePerson;
import static org.hamcrest.Matchers.is;

public class JsonMatcherSortingTest extends AbstractJsonMatcherIgnoreTest  {


    public static Object[][] expectedFileSortingTestcases() {
        return new Object[][]{
                {"Object input", BeanWithGenericIterable.Builder.bean().dummyString("String1")
                        .set(Sets.newHashSet(generatePerson(1L), generatePerson(2L), generatePerson(3L)))
                        .hashMap(new HashMap<Object, Object>() {{
                            put("p1", generatePerson(4L));
                            put("p2", generatePerson(5L));
                        }})
                        .array(new Object[]{generatePerson(6L), generatePerson(7L), generatePerson(8L)})
                        .build()},
                {"Json string input", "{\n" +
                        "  \"array\": [\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"HUNGARY\",\n" +
                        "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName6\",\n" +
                        "        \"country\": \"HUNGARY\",\n" +
                        "        \"postCode\": \"PostCode69\",\n" +
                        "        \"since\": \"2017-04-07\",\n" +
                        "        \"streetName\": \"StreetName65\",\n" +
                        "        \"streetNumber\": 48\n" +
                        "      },\n" +
                        "      \"email\": \"e6@e.mail\",\n" +
                        "      \"firstName\": \"FirstName6\",\n" +
                        "      \"lastName\": \"LastName6\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName16\",\n" +
                        "          \"country\": \"CANADA\",\n" +
                        "          \"postCode\": \"PostCode79\",\n" +
                        "          \"since\": \"2017-04-17\",\n" +
                        "          \"streetName\": \"StreetName75\",\n" +
                        "          \"streetNumber\": 58\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"AUSTRIA\",\n" +
                        "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName7\",\n" +
                        "        \"country\": \"AUSTRIA\",\n" +
                        "        \"postCode\": \"PostCode70\",\n" +
                        "        \"since\": \"2017-04-08\",\n" +
                        "        \"streetName\": \"StreetName66\",\n" +
                        "        \"streetNumber\": 49\n" +
                        "      },\n" +
                        "      \"email\": \"e7@e.mail\",\n" +
                        "      \"firstName\": \"FirstName7\",\n" +
                        "      \"lastName\": \"LastName7\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName17\",\n" +
                        "          \"country\": \"DENMARK\",\n" +
                        "          \"postCode\": \"PostCode80\",\n" +
                        "          \"since\": \"2017-04-18\",\n" +
                        "          \"streetName\": \"StreetName76\",\n" +
                        "          \"streetNumber\": 59\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName18\",\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"postCode\": \"PostCode81\",\n" +
                        "          \"since\": \"2017-04-19\",\n" +
                        "          \"streetName\": \"StreetName77\",\n" +
                        "          \"streetNumber\": 60\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"BELGIUM\",\n" +
                        "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName8\",\n" +
                        "        \"country\": \"BELGIUM\",\n" +
                        "        \"postCode\": \"PostCode71\",\n" +
                        "        \"since\": \"2017-04-09\",\n" +
                        "        \"streetName\": \"StreetName67\",\n" +
                        "        \"streetNumber\": 50\n" +
                        "      },\n" +
                        "      \"email\": \"e8@e.mail\",\n" +
                        "      \"firstName\": \"FirstName8\",\n" +
                        "      \"lastName\": \"LastName8\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName18\",\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"postCode\": \"PostCode81\",\n" +
                        "          \"since\": \"2017-04-19\",\n" +
                        "          \"streetName\": \"StreetName77\",\n" +
                        "          \"streetNumber\": 60\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName19\",\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"postCode\": \"PostCode82\",\n" +
                        "          \"since\": \"2017-04-20\",\n" +
                        "          \"streetName\": \"StreetName78\",\n" +
                        "          \"streetNumber\": 61\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName20\",\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"postCode\": \"PostCode83\",\n" +
                        "          \"since\": \"2017-04-21\",\n" +
                        "          \"streetName\": \"StreetName79\",\n" +
                        "          \"streetNumber\": 62\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"dummyString\": \"String1\",\n" +
                        "  \"hashMap\": [\n" +
                        "    {\n" +
                        "      \"p1\": {\n" +
                        "        \"birthCountry\": \"EGYPT\",\n" +
                        "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"city\": \"CityName4\",\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"postCode\": \"PostCode67\",\n" +
                        "          \"since\": \"2017-04-05\",\n" +
                        "          \"streetName\": \"StreetName63\",\n" +
                        "          \"streetNumber\": 46\n" +
                        "        },\n" +
                        "        \"email\": \"e4@e.mail\",\n" +
                        "        \"firstName\": \"FirstName4\",\n" +
                        "        \"lastName\": \"LastName4\",\n" +
                        "        \"previousAddresses\": [\n" +
                        "          {\n" +
                        "            \"city\": \"CityName14\",\n" +
                        "            \"country\": \"AUSTRIA\",\n" +
                        "            \"postCode\": \"PostCode77\",\n" +
                        "            \"since\": \"2017-04-15\",\n" +
                        "            \"streetName\": \"StreetName73\",\n" +
                        "            \"streetNumber\": 56\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"city\": \"CityName15\",\n" +
                        "            \"country\": \"BELGIUM\",\n" +
                        "            \"postCode\": \"PostCode78\",\n" +
                        "            \"since\": \"2017-04-16\",\n" +
                        "            \"streetName\": \"StreetName74\",\n" +
                        "            \"streetNumber\": 57\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"city\": \"CityName16\",\n" +
                        "            \"country\": \"CANADA\",\n" +
                        "            \"postCode\": \"PostCode79\",\n" +
                        "            \"since\": \"2017-04-17\",\n" +
                        "            \"streetName\": \"StreetName75\",\n" +
                        "            \"streetNumber\": 58\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"city\": \"CityName17\",\n" +
                        "            \"country\": \"DENMARK\",\n" +
                        "            \"postCode\": \"PostCode80\",\n" +
                        "            \"since\": \"2017-04-18\",\n" +
                        "            \"streetName\": \"StreetName76\",\n" +
                        "            \"streetNumber\": 59\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"p2\": {\n" +
                        "        \"birthCountry\": \"FRANCE\",\n" +
                        "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                        "        \"currentAddress\": {\n" +
                        "          \"city\": \"CityName5\",\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"postCode\": \"PostCode68\",\n" +
                        "          \"since\": \"2017-04-06\",\n" +
                        "          \"streetName\": \"StreetName64\",\n" +
                        "          \"streetNumber\": 47\n" +
                        "        },\n" +
                        "        \"email\": \"e5@e.mail\",\n" +
                        "        \"firstName\": \"FirstName5\",\n" +
                        "        \"lastName\": \"LastName5\",\n" +
                        "        \"previousAddresses\": []\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ],\n" +
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
                        "      \"firstName\": \"FirstName1\",\n" +
                        "      \"lastName\": \"LastName1\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName11\",\n" +
                        "          \"country\": \"EGYPT\",\n" +
                        "          \"postCode\": \"PostCode74\",\n" +
                        "          \"since\": \"2017-04-12\",\n" +
                        "          \"streetName\": \"StreetName70\",\n" +
                        "          \"streetNumber\": 53\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"birthCountry\": \"CANADA\",\n" +
                        "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                        "      \"currentAddress\": {\n" +
                        "        \"city\": \"CityName2\",\n" +
                        "        \"country\": \"CANADA\",\n" +
                        "        \"postCode\": \"PostCode65\",\n" +
                        "        \"since\": \"2017-04-03\",\n" +
                        "        \"streetName\": \"StreetName61\",\n" +
                        "        \"streetNumber\": 44\n" +
                        "      },\n" +
                        "      \"email\": \"e2@e.mail\",\n" +
                        "      \"firstName\": \"FirstName2\",\n" +
                        "      \"lastName\": \"LastName2\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName12\",\n" +
                        "          \"country\": \"FRANCE\",\n" +
                        "          \"postCode\": \"PostCode75\",\n" +
                        "          \"since\": \"2017-04-13\",\n" +
                        "          \"streetName\": \"StreetName71\",\n" +
                        "          \"streetNumber\": 54\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName13\",\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"postCode\": \"PostCode76\",\n" +
                        "          \"since\": \"2017-04-14\",\n" +
                        "          \"streetName\": \"StreetName72\",\n" +
                        "          \"streetNumber\": 55\n" +
                        "        }\n" +
                        "      ]\n" +
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
                        "      \"firstName\": \"FirstName3\",\n" +
                        "      \"lastName\": \"LastName3\",\n" +
                        "      \"previousAddresses\": [\n" +
                        "        {\n" +
                        "          \"city\": \"CityName13\",\n" +
                        "          \"country\": \"HUNGARY\",\n" +
                        "          \"postCode\": \"PostCode76\",\n" +
                        "          \"since\": \"2017-04-14\",\n" +
                        "          \"streetName\": \"StreetName72\",\n" +
                        "          \"streetNumber\": 55\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName14\",\n" +
                        "          \"country\": \"AUSTRIA\",\n" +
                        "          \"postCode\": \"PostCode77\",\n" +
                        "          \"since\": \"2017-04-15\",\n" +
                        "          \"streetName\": \"StreetName73\",\n" +
                        "          \"streetNumber\": 56\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"city\": \"CityName15\",\n" +
                        "          \"country\": \"BELGIUM\",\n" +
                        "          \"postCode\": \"PostCode78\",\n" +
                        "          \"since\": \"2017-04-16\",\n" +
                        "          \"streetName\": \"StreetName74\",\n" +
                        "          \"streetNumber\": 57\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"}
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitSortingByPathOfExpectedFile(String testName, Object input) {
        testingExplicitSortingByPathOfExpectedFile(input,
                jsonMatcher -> jsonMatcher.sortField("array", "array.previousAddresses", "set.previousAddresses", "hashMap.p1.previousAddresses", "hashMap", "set"),
                jsonMatcher -> jsonMatcher.sortField("array", "array.previousAddresses", "set.previousAddresses", "hashMap.p1.previousAddresses", "hashMap", "set"));
    }

    public void testingExplicitSortingByPathOfExpectedFile(Object input,Function<JsonMatcher<Object>, JsonMatcher<Object>> passConfigurator,Function<JsonMatcher<Object>, JsonMatcher<Object>> failConfigurator) {
        String approvedFileContent = "{\n" +
                "  \"array\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"AUSTRIA\",\n" +
                "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName7\",\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"postCode\": \"PostCode70\",\n" +
                "        \"since\": \"2017-04-08\",\n" +
                "        \"streetName\": \"StreetName66\",\n" +
                "        \"streetNumber\": 49\n" +
                "      },\n" +
                "      \"email\": \"e7@e.mail\",\n" +
                "      \"firstName\": \"FirstName7\",\n" +
                "      \"lastName\": \"LastName7\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName17\",\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"postCode\": \"PostCode80\",\n" +
                "          \"since\": \"2017-04-18\",\n" +
                "          \"streetName\": \"StreetName76\",\n" +
                "          \"streetNumber\": 59\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"streetNumber\": 60\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"HUNGARY\",\n" +
                "      \"lastName\": \"LastName6\",\n" +
                "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName6\",\n" +
                "        \"since\": \"2017-04-07\",\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"postCode\": \"PostCode69\",\n" +
                "        \"streetName\": \"StreetName65\",\n" +
                "        \"streetNumber\": 48\n" +
                "      },\n" +
                "      \"email\": \"e6@e.mail\",\n" +
                "      \"firstName\": \"FirstName6\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName16\",\n" +
                "          \"postCode\": \"PostCode79\",\n" +
                "          \"since\": \"2017-04-17\",\n" +
                "          \"streetName\": \"StreetName75\",\n" +
                "          \"streetNumber\": 58\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                "      \"firstName\": \"FirstName8\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName8\",\n" +
                "        \"postCode\": \"PostCode71\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"since\": \"2017-04-09\",\n" +
                "        \"streetName\": \"StreetName67\",\n" +
                "        \"streetNumber\": 50\n" +
                "      },\n" +
                "      \"email\": \"e8@e.mail\",\n" +
                "      \"lastName\": \"LastName8\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName19\",\n" +
                "          \"since\": \"2017-04-20\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode82\",\n" +
                "          \"streetName\": \"StreetName78\",\n" +
                "          \"streetNumber\": 61\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetNumber\": 60\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName20\",\n" +
                "          \"postCode\": \"PostCode83\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-21\",\n" +
                "          \"streetName\": \"StreetName79\",\n" +
                "          \"streetNumber\": 62\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"hashMap\": [\n" +
                "    {\n" +
                "      \"p2\": {\n" +
                "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                "        \"birthCountry\": \"FRANCE\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName5\",\n" +
                "          \"streetName\": \"StreetName64\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode68\",\n" +
                "          \"since\": \"2017-04-06\",\n" +
                "          \"streetNumber\": 47\n" +
                "        },\n" +
                "        \"firstName\": \"FirstName5\",\n" +
                "        \"email\": \"e5@e.mail\",\n" +
                "        \"lastName\": \"LastName5\",\n" +
                "        \"previousAddresses\": []\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"p1\": {\n" +
                "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                "        \"birthCountry\": \"EGYPT\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName4\",\n" +
                "          \"since\": \"2017-04-05\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode67\",\n" +
                "          \"streetName\": \"StreetName63\",\n" +
                "          \"streetNumber\": 46\n" +
                "        },\n" +
                "        \"email\": \"e4@e.mail\",\n" +
                "        \"lastName\": \"LastName4\",\n" +
                "        \"firstName\": \"FirstName4\",\n" +
                "        \"previousAddresses\": [\n" +
                "          {\n" +
                "            \"city\": \"CityName14\",\n" +
                "            \"postCode\": \"PostCode77\",\n" +
                "            \"country\": \"AUSTRIA\",\n" +
                "            \"since\": \"2017-04-15\",\n" +
                "            \"streetName\": \"StreetName73\",\n" +
                "            \"streetNumber\": 56\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName16\",\n" +
                "            \"since\": \"2017-04-17\",\n" +
                "            \"country\": \"CANADA\",\n" +
                "            \"postCode\": \"PostCode79\",\n" +
                "            \"streetName\": \"StreetName75\",\n" +
                "            \"streetNumber\": 58\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName15\",\n" +
                "            \"country\": \"BELGIUM\",\n" +
                "            \"streetName\": \"StreetName74\",\n" +
                "            \"postCode\": \"PostCode78\",\n" +
                "            \"since\": \"2017-04-16\",\n" +
                "            \"streetNumber\": 57\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName17\",\n" +
                "            \"country\": \"DENMARK\",\n" +
                "            \"postCode\": \"PostCode80\",\n" +
                "            \"streetName\": \"StreetName76\",\n" +
                "            \"since\": \"2017-04-18\",\n" +
                "            \"streetNumber\": 59\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\",\n" +
                "        \"streetNumber\": 44\n" +
                "      },\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"since\": \"2017-04-13\",\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"since\": \"2017-04-02\",\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43\n" +
                "      },\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"since\": \"2017-04-12\",\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\",\n" +
                "        \"streetNumber\": 45\n" +
                "      },\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\",\n" +
                "          \"streetNumber\": 56\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"since\": \"2017-04-16\",\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(), passConfigurator, null, null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, failConfigurator, thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("array[1].birthCountry\n" +
                    "Expected: HUNGARY\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].birthDate\n" +
                    "Expected: 2011-04-01T13:42:11\n" +
                    "     got: 2009-04-01T13:42:11\n" +
                    " ; array[1].currentAddress.city\n" +
                    "Expected: CityName6\n" +
                    "     got: CityName8\n" +
                    " ; array[1].currentAddress.country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].currentAddress.postCode\n" +
                    "Expected: PostCode69\n" +
                    "     got: PostCode71\n" +
                    " ; array[1].currentAddress.since\n" +
                    "Expected: 2017-04-07\n" +
                    "     got: 2017-04-09\n" +
                    " ; array[1].currentAddress.streetName\n" +
                    "Expected: StreetName65\n" +
                    "     got: StreetName67\n" +
                    " ; array[1].currentAddress.streetNumber\n" +
                    "Expected: 48\n" +
                    "     got: 50\n" +
                    " ; array[1].email\n" +
                    "Expected: e6@e.mail\n" +
                    "     got: e8@e.mail\n" +
                    " ; array[1].firstName\n" +
                    "Expected: FirstName6\n" +
                    "     got: FirstName8\n" +
                    " ; array[1].lastName\n" +
                    "Expected: LastName6\n" +
                    "     got: LastName8\n" +
                    " ; array[1].previousAddresses[]: Expected 1 values but got 3 ; array[2].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].birthDate\n" +
                    "Expected: 2009-04-01T13:42:11\n" +
                    "     got: 2011-04-01T13:42:11\n" +
                    " ; array[2].currentAddress.city\n" +
                    "Expected: CityName8\n" +
                    "     got: CityName6\n" +
                    " ; array[2].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].currentAddress.postCode\n" +
                    "Expected: PostCode71\n" +
                    "     got: PostCode69\n" +
                    " ; array[2].currentAddress.since\n" +
                    "Expected: 2017-04-09\n" +
                    "     got: 2017-04-07\n" +
                    " ; array[2].currentAddress.streetName\n" +
                    "Expected: StreetName67\n" +
                    "     got: StreetName65\n" +
                    " ; array[2].currentAddress.streetNumber\n" +
                    "Expected: 50\n" +
                    "     got: 48\n" +
                    " ; array[2].email\n" +
                    "Expected: e8@e.mail\n" +
                    "     got: e6@e.mail\n" +
                    " ; array[2].firstName\n" +
                    "Expected: FirstName8\n" +
                    "     got: FirstName6\n" +
                    " ; array[2].lastName\n" +
                    "Expected: LastName8\n" +
                    "     got: LastName6\n" +
                    " ; array[2].previousAddresses[]: Expected 3 values but got 1 ; hashMap[0]\n" +
                    "Expected: p2\n" +
                    "     but none found\n" +
                    " ; hashMap[0]\n" +
                    "Unexpected: p1\n" +
                    " ; hashMap[1]\n" +
                    "Expected: p1\n" +
                    "     but none found\n" +
                    " ; hashMap[1]\n" +
                    "Unexpected: p2\n" +
                    " ; set[0].birthCountry\n" +
                    "Expected: CANADA\n" +
                    "     got: BELGIUM\n" +
                    " ; set[0].birthDate\n" +
                    "Expected: 2015-04-01T13:42:11\n" +
                    "     got: 2016-04-01T13:42:11\n" +
                    " ; set[0].currentAddress.city\n" +
                    "Expected: CityName2\n" +
                    "     got: CityName1\n" +
                    " ; set[0].currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: BELGIUM\n" +
                    " ; set[0].currentAddress.postCode\n" +
                    "Expected: PostCode65\n" +
                    "     got: PostCode64\n" +
                    " ; set[0].currentAddress.since\n" +
                    "Expected: 2017-04-03\n" +
                    "     got: 2017-04-02\n" +
                    " ; set[0].currentAddress.streetName\n" +
                    "Expected: StreetName61\n" +
                    "     got: StreetName60\n" +
                    " ; set[0].currentAddress.streetNumber\n" +
                    "Expected: 44\n" +
                    "     got: 43\n" +
                    " ; set[0].email\n" +
                    "Expected: e2@e.mail\n" +
                    "     got: e1@e.mail\n" +
                    " ; set[0].firstName\n" +
                    "Expected: FirstName2\n" +
                    "     got: FirstName1\n" +
                    " ; set[0].lastName\n" +
                    "Expected: LastName2\n" +
                    "     got: LastName1\n" +
                    " ; set[0].previousAddresses[]: Expected 2 values but got 1 ; set[1].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: CANADA\n" +
                    " ; set[1].birthDate\n" +
                    "Expected: 2016-04-01T13:42:11\n" +
                    "     got: 2015-04-01T13:42:11\n" +
                    " ; set[1].currentAddress.city\n" +
                    "Expected: CityName1\n" +
                    "     got: CityName2\n" +
                    " ; set[1].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: CANADA\n" +
                    " ; set[1].currentAddress.postCode\n" +
                    "Expected: PostCode64\n" +
                    "     got: PostCode65\n" +
                    " ; set[1].currentAddress.since\n" +
                    "Expected: 2017-04-02\n" +
                    "     got: 2017-04-03\n" +
                    " ; set[1].currentAddress.streetName\n" +
                    "Expected: StreetName60\n" +
                    "     got: StreetName61\n" +
                    " ; set[1].currentAddress.streetNumber\n" +
                    "Expected: 43\n" +
                    "     got: 44\n" +
                    " ; set[1].email\n" +
                    "Expected: e1@e.mail\n" +
                    "     got: e2@e.mail\n" +
                    " ; set[1].firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: FirstName2\n" +
                    " ; set[1].lastName\n" +
                    "Expected: LastName1\n" +
                    "     got: LastName2\n" +
                    " ; set[1].previousAddresses[]: Expected 1 values but got 2 ; set[2].previousAddresses[0].city\n" +
                    "Expected: CityName14\n" +
                    "     got: CityName13\n" +
                    " ; set[2].previousAddresses[0].country\n" +
                    "Expected: AUSTRIA\n" +
                    "     got: HUNGARY\n" +
                    " ; set[2].previousAddresses[0].postCode\n" +
                    "Expected: PostCode77\n" +
                    "     got: PostCode76\n" +
                    " ; set[2].previousAddresses[0].since\n" +
                    "Expected: 2017-04-15\n" +
                    "     got: 2017-04-14\n" +
                    " ; set[2].previousAddresses[0].streetName\n" +
                    "Expected: StreetName73\n" +
                    "     got: StreetName72\n" +
                    " ; set[2].previousAddresses[0].streetNumber\n" +
                    "Expected: 56\n" +
                    "     got: 55\n" +
                    " ; set[2].previousAddresses[1].city\n" +
                    "Expected: CityName13\n" +
                    "     got: CityName14\n" +
                    " ; set[2].previousAddresses[1].country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: AUSTRIA\n" +
                    " ; set[2].previousAddresses[1].postCode\n" +
                    "Expected: PostCode76\n" +
                    "     got: PostCode77\n" +
                    " ; set[2].previousAddresses[1].since\n" +
                    "Expected: 2017-04-14\n" +
                    "     got: 2017-04-15\n" +
                    " ; set[2].previousAddresses[1].streetName\n" +
                    "Expected: StreetName72\n" +
                    "     got: StreetName73\n" +
                    " ; set[2].previousAddresses[1].streetNumber\n" +
                    "Expected: 55\n" +
                    "     got: 56\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
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
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
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
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "no explicit sorting applied");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "no explicit sorting applied");
        }, AssertionFailedError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitSortingByPathOfExpectedFileOneByOne(String testName, Object input) {
        testingExplicitSortingByPathOfExpectedFile(input,
                jsonMatcher -> jsonMatcher.sortField("array").sortField( "array.previousAddresses").sortField( "set.previousAddresses").sortField( "hashMap.p1.previousAddresses").sortField( "hashMap").sortField( "set"),
                jsonMatcher -> jsonMatcher.sortField("array").sortField( "array.previousAddresses").sortField("set.previousAddresses").sortField( "hashMap.p1.previousAddresses").sortField( "hashMap").sortField( "set")
                );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitSortingByMatcherOfExpectedFile(String testName, Object input) {
        testingExplicitSortingByMatcherOfExpectedFile(input,
                jsonMatcher -> jsonMatcher.sortField(is("array"), is("previousAddresses"), is("hashMap"), is("set")),
                jsonMatcher -> jsonMatcher.sortField(is("array"), is("previousAddresses"), is("hashMap"), is("set")));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitSortingByMatcherOfExpectedFileOneByOne(String testName, Object input) {
        testingExplicitSortingByMatcherOfExpectedFile(input,
                jsonMatcher -> jsonMatcher.sortField(is("array")).sortField( is("previousAddresses")).sortField(  is("hashMap")).sortField(  is("set")),
                jsonMatcher -> jsonMatcher.sortField(is("array")).sortField(  is("previousAddresses")).sortField(  is("hashMap")).sortField(  is("set")));
    }

    private void testingExplicitSortingByMatcherOfExpectedFile(Object input,Function<JsonMatcher<Object>, JsonMatcher<Object>> passConfigurator,Function<JsonMatcher<Object>, JsonMatcher<Object>> failConfigurator) {
        String approvedFileContent = "{\n" +
                "  \"array\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"AUSTRIA\",\n" +
                "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName7\",\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"postCode\": \"PostCode70\",\n" +
                "        \"since\": \"2017-04-08\",\n" +
                "        \"streetName\": \"StreetName66\",\n" +
                "        \"streetNumber\": 49\n" +
                "      },\n" +
                "      \"email\": \"e7@e.mail\",\n" +
                "      \"firstName\": \"FirstName7\",\n" +
                "      \"lastName\": \"LastName7\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName17\",\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"postCode\": \"PostCode80\",\n" +
                "          \"since\": \"2017-04-18\",\n" +
                "          \"streetName\": \"StreetName76\",\n" +
                "          \"streetNumber\": 59\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"streetNumber\": 60\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"HUNGARY\",\n" +
                "      \"lastName\": \"LastName6\",\n" +
                "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName6\",\n" +
                "        \"since\": \"2017-04-07\",\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"postCode\": \"PostCode69\",\n" +
                "        \"streetName\": \"StreetName65\",\n" +
                "        \"streetNumber\": 48\n" +
                "      },\n" +
                "      \"email\": \"e6@e.mail\",\n" +
                "      \"firstName\": \"FirstName6\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"city\": \"CityName16\",\n" +
                "          \"postCode\": \"PostCode79\",\n" +
                "          \"since\": \"2017-04-17\",\n" +
                "          \"streetName\": \"StreetName75\",\n" +
                "          \"streetNumber\": 58\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                "      \"firstName\": \"FirstName8\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName8\",\n" +
                "        \"postCode\": \"PostCode71\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"since\": \"2017-04-09\",\n" +
                "        \"streetName\": \"StreetName67\",\n" +
                "        \"streetNumber\": 50\n" +
                "      },\n" +
                "      \"email\": \"e8@e.mail\",\n" +
                "      \"lastName\": \"LastName8\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName19\",\n" +
                "          \"since\": \"2017-04-20\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode82\",\n" +
                "          \"streetName\": \"StreetName78\",\n" +
                "          \"streetNumber\": 61\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetNumber\": 60\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName20\",\n" +
                "          \"postCode\": \"PostCode83\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-21\",\n" +
                "          \"streetName\": \"StreetName79\",\n" +
                "          \"streetNumber\": 62\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"hashMap\": [\n" +
                "    {\n" +
                "      \"p2\": {\n" +
                "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                "        \"birthCountry\": \"FRANCE\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName5\",\n" +
                "          \"streetName\": \"StreetName64\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode68\",\n" +
                "          \"since\": \"2017-04-06\",\n" +
                "          \"streetNumber\": 47\n" +
                "        },\n" +
                "        \"firstName\": \"FirstName5\",\n" +
                "        \"email\": \"e5@e.mail\",\n" +
                "        \"lastName\": \"LastName5\",\n" +
                "        \"previousAddresses\": []\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"p1\": {\n" +
                "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                "        \"birthCountry\": \"EGYPT\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName4\",\n" +
                "          \"since\": \"2017-04-05\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode67\",\n" +
                "          \"streetName\": \"StreetName63\",\n" +
                "          \"streetNumber\": 46\n" +
                "        },\n" +
                "        \"email\": \"e4@e.mail\",\n" +
                "        \"lastName\": \"LastName4\",\n" +
                "        \"firstName\": \"FirstName4\",\n" +
                "        \"previousAddresses\": [\n" +
                "          {\n" +
                "            \"city\": \"CityName14\",\n" +
                "            \"postCode\": \"PostCode77\",\n" +
                "            \"country\": \"AUSTRIA\",\n" +
                "            \"since\": \"2017-04-15\",\n" +
                "            \"streetName\": \"StreetName73\",\n" +
                "            \"streetNumber\": 56\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName16\",\n" +
                "            \"since\": \"2017-04-17\",\n" +
                "            \"country\": \"CANADA\",\n" +
                "            \"postCode\": \"PostCode79\",\n" +
                "            \"streetName\": \"StreetName75\",\n" +
                "            \"streetNumber\": 58\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName15\",\n" +
                "            \"country\": \"BELGIUM\",\n" +
                "            \"streetName\": \"StreetName74\",\n" +
                "            \"postCode\": \"PostCode78\",\n" +
                "            \"since\": \"2017-04-16\",\n" +
                "            \"streetNumber\": 57\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName17\",\n" +
                "            \"country\": \"DENMARK\",\n" +
                "            \"postCode\": \"PostCode80\",\n" +
                "            \"streetName\": \"StreetName76\",\n" +
                "            \"since\": \"2017-04-18\",\n" +
                "            \"streetNumber\": 59\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\",\n" +
                "        \"streetNumber\": 44\n" +
                "      },\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"since\": \"2017-04-13\",\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName1\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"since\": \"2017-04-02\",\n" +
                "        \"postCode\": \"PostCode64\",\n" +
                "        \"streetName\": \"StreetName60\",\n" +
                "        \"streetNumber\": 43\n" +
                "      },\n" +
                "      \"email\": \"e1@e.mail\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"since\": \"2017-04-12\",\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                "      \"birthCountry\": \"DENMARK\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName3\",\n" +
                "        \"streetName\": \"StreetName62\",\n" +
                "        \"country\": \"DENMARK\",\n" +
                "        \"postCode\": \"PostCode66\",\n" +
                "        \"since\": \"2017-04-04\",\n" +
                "        \"streetNumber\": 45\n" +
                "      },\n" +
                "      \"email\": \"e3@e.mail\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\",\n" +
                "          \"streetNumber\": 56\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"since\": \"2017-04-16\",\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(), passConfigurator, null, null);

        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, failConfigurator, thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("array[1].birthCountry\n" +
                    "Expected: HUNGARY\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].birthDate\n" +
                    "Expected: 2011-04-01T13:42:11\n" +
                    "     got: 2009-04-01T13:42:11\n" +
                    " ; array[1].currentAddress.city\n" +
                    "Expected: CityName6\n" +
                    "     got: CityName8\n" +
                    " ; array[1].currentAddress.country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].currentAddress.postCode\n" +
                    "Expected: PostCode69\n" +
                    "     got: PostCode71\n" +
                    " ; array[1].currentAddress.since\n" +
                    "Expected: 2017-04-07\n" +
                    "     got: 2017-04-09\n" +
                    " ; array[1].currentAddress.streetName\n" +
                    "Expected: StreetName65\n" +
                    "     got: StreetName67\n" +
                    " ; array[1].currentAddress.streetNumber\n" +
                    "Expected: 48\n" +
                    "     got: 50\n" +
                    " ; array[1].email\n" +
                    "Expected: e6@e.mail\n" +
                    "     got: e8@e.mail\n" +
                    " ; array[1].firstName\n" +
                    "Expected: FirstName6\n" +
                    "     got: FirstName8\n" +
                    " ; array[1].lastName\n" +
                    "Expected: LastName6\n" +
                    "     got: LastName8\n" +
                    " ; array[1].previousAddresses[]: Expected 1 values but got 3 ; array[2].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].birthDate\n" +
                    "Expected: 2009-04-01T13:42:11\n" +
                    "     got: 2011-04-01T13:42:11\n" +
                    " ; array[2].currentAddress.city\n" +
                    "Expected: CityName8\n" +
                    "     got: CityName6\n" +
                    " ; array[2].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].currentAddress.postCode\n" +
                    "Expected: PostCode71\n" +
                    "     got: PostCode69\n" +
                    " ; array[2].currentAddress.since\n" +
                    "Expected: 2017-04-09\n" +
                    "     got: 2017-04-07\n" +
                    " ; array[2].currentAddress.streetName\n" +
                    "Expected: StreetName67\n" +
                    "     got: StreetName65\n" +
                    " ; array[2].currentAddress.streetNumber\n" +
                    "Expected: 50\n" +
                    "     got: 48\n" +
                    " ; array[2].email\n" +
                    "Expected: e8@e.mail\n" +
                    "     got: e6@e.mail\n" +
                    " ; array[2].firstName\n" +
                    "Expected: FirstName8\n" +
                    "     got: FirstName6\n" +
                    " ; array[2].lastName\n" +
                    "Expected: LastName8\n" +
                    "     got: LastName6\n" +
                    " ; array[2].previousAddresses[]: Expected 3 values but got 1 ; hashMap[0]\n" +
                    "Expected: p2\n" +
                    "     but none found\n" +
                    " ; hashMap[0]\n" +
                    "Unexpected: p1\n" +
                    " ; hashMap[1]\n" +
                    "Expected: p1\n" +
                    "     but none found\n" +
                    " ; hashMap[1]\n" +
                    "Unexpected: p2\n" +
                    " ; set[0].birthCountry\n" +
                    "Expected: CANADA\n" +
                    "     got: BELGIUM\n" +
                    " ; set[0].birthDate\n" +
                    "Expected: 2015-04-01T13:42:11\n" +
                    "     got: 2016-04-01T13:42:11\n" +
                    " ; set[0].currentAddress.city\n" +
                    "Expected: CityName2\n" +
                    "     got: CityName1\n" +
                    " ; set[0].currentAddress.country\n" +
                    "Expected: CANADA\n" +
                    "     got: BELGIUM\n" +
                    " ; set[0].currentAddress.postCode\n" +
                    "Expected: PostCode65\n" +
                    "     got: PostCode64\n" +
                    " ; set[0].currentAddress.since\n" +
                    "Expected: 2017-04-03\n" +
                    "     got: 2017-04-02\n" +
                    " ; set[0].currentAddress.streetName\n" +
                    "Expected: StreetName61\n" +
                    "     got: StreetName60\n" +
                    " ; set[0].currentAddress.streetNumber\n" +
                    "Expected: 44\n" +
                    "     got: 43\n" +
                    " ; set[0].email\n" +
                    "Expected: e2@e.mail\n" +
                    "     got: e1@e.mail\n" +
                    " ; set[0].firstName\n" +
                    "Expected: FirstName2\n" +
                    "     got: FirstName1\n" +
                    " ; set[0].lastName\n" +
                    "Expected: LastName2\n" +
                    "     got: LastName1\n" +
                    " ; set[0].previousAddresses[]: Expected 2 values but got 1 ; set[1].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: CANADA\n" +
                    " ; set[1].birthDate\n" +
                    "Expected: 2016-04-01T13:42:11\n" +
                    "     got: 2015-04-01T13:42:11\n" +
                    " ; set[1].currentAddress.city\n" +
                    "Expected: CityName1\n" +
                    "     got: CityName2\n" +
                    " ; set[1].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: CANADA\n" +
                    " ; set[1].currentAddress.postCode\n" +
                    "Expected: PostCode64\n" +
                    "     got: PostCode65\n" +
                    " ; set[1].currentAddress.since\n" +
                    "Expected: 2017-04-02\n" +
                    "     got: 2017-04-03\n" +
                    " ; set[1].currentAddress.streetName\n" +
                    "Expected: StreetName60\n" +
                    "     got: StreetName61\n" +
                    " ; set[1].currentAddress.streetNumber\n" +
                    "Expected: 43\n" +
                    "     got: 44\n" +
                    " ; set[1].email\n" +
                    "Expected: e1@e.mail\n" +
                    "     got: e2@e.mail\n" +
                    " ; set[1].firstName\n" +
                    "Expected: FirstName1\n" +
                    "     got: FirstName2\n" +
                    " ; set[1].lastName\n" +
                    "Expected: LastName1\n" +
                    "     got: LastName2\n" +
                    " ; set[1].previousAddresses[]: Expected 1 values but got 2 ; set[2].previousAddresses[0].city\n" +
                    "Expected: CityName14\n" +
                    "     got: CityName13\n" +
                    " ; set[2].previousAddresses[0].country\n" +
                    "Expected: AUSTRIA\n" +
                    "     got: HUNGARY\n" +
                    " ; set[2].previousAddresses[0].postCode\n" +
                    "Expected: PostCode77\n" +
                    "     got: PostCode76\n" +
                    " ; set[2].previousAddresses[0].since\n" +
                    "Expected: 2017-04-15\n" +
                    "     got: 2017-04-14\n" +
                    " ; set[2].previousAddresses[0].streetName\n" +
                    "Expected: StreetName73\n" +
                    "     got: StreetName72\n" +
                    " ; set[2].previousAddresses[0].streetNumber\n" +
                    "Expected: 56\n" +
                    "     got: 55\n" +
                    " ; set[2].previousAddresses[1].city\n" +
                    "Expected: CityName13\n" +
                    "     got: CityName14\n" +
                    " ; set[2].previousAddresses[1].country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: AUSTRIA\n" +
                    " ; set[2].previousAddresses[1].postCode\n" +
                    "Expected: PostCode76\n" +
                    "     got: PostCode77\n" +
                    " ; set[2].previousAddresses[1].since\n" +
                    "Expected: 2017-04-14\n" +
                    "     got: 2017-04-15\n" +
                    " ; set[2].previousAddresses[1].streetName\n" +
                    "Expected: StreetName72\n" +
                    "     got: StreetName73\n" +
                    " ; set[2].previousAddresses[1].streetNumber\n" +
                    "Expected: 55\n" +
                    "     got: 56\n"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
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
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
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
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"set\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"2016-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName1\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"since\": \"2017-04-02\",\n" +
                    "        \"postCode\": \"PostCode64\",\n" +
                    "        \"streetName\": \"StreetName60\",\n" +
                    "        \"streetNumber\": 43\n" +
                    "      },\n" +
                    "      \"email\": \"e1@e.mail\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthDate\": \"2014-04-01T13:42:11\",\n" +
                    "      \"birthCountry\": \"DENMARK\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName3\",\n" +
                    "        \"streetName\": \"StreetName62\",\n" +
                    "        \"country\": \"DENMARK\",\n" +
                    "        \"postCode\": \"PostCode66\",\n" +
                    "        \"since\": \"2017-04-04\",\n" +
                    "        \"streetNumber\": 45\n" +
                    "      },\n" +
                    "      \"email\": \"e3@e.mail\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "no explicit sorting applied");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "no explicit sorting applied");
        }, AssertionFailedError.class);
    }


    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitSortingByPathOfExpectedFileShouldThrowWhenExpectedFileDiffersAndInStrictMode(String testName, Object input) {
        String approvedFileContent = "{\n" +
                "  \"array\": [\n" +
                "    {\n" +
                "      \"birthCountry\": \"HUNGARY\",\n" +
                "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName6\",\n" +
                "        \"country\": \"HUNGARY\",\n" +
                "        \"postCode\": \"PostCode69\",\n" +
                "        \"since\": \"2017-04-07\",\n" +
                "        \"streetName\": \"StreetName65\",\n" +
                "        \"streetNumber\": 48\n" +
                "      },\n" +
                "      \"email\": \"e6@e.mail\",\n" +
                "      \"firstName\": \"FirstName6\",\n" +
                "      \"lastName\": \"LastName6\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName16\",\n" +
                "          \"country\": \"CANADA\",\n" +
                "          \"postCode\": \"PostCode79\",\n" +
                "          \"since\": \"2017-04-17\",\n" +
                "          \"streetName\": \"StreetName75\",\n" +
                "          \"streetNumber\": 58\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"AUSTRIA\",\n" +
                "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName7\",\n" +
                "        \"country\": \"AUSTRIA\",\n" +
                "        \"postCode\": \"PostCode70\",\n" +
                "        \"since\": \"2017-04-08\",\n" +
                "        \"streetName\": \"StreetName66\",\n" +
                "        \"streetNumber\": 49\n" +
                "      },\n" +
                "      \"email\": \"e7@e.mail\",\n" +
                "      \"firstName\": \"FirstName7\",\n" +
                "      \"lastName\": \"LastName7\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName17\",\n" +
                "          \"country\": \"DENMARK\",\n" +
                "          \"postCode\": \"PostCode80\",\n" +
                "          \"since\": \"2017-04-18\",\n" +
                "          \"streetName\": \"StreetName76\",\n" +
                "          \"streetNumber\": 59\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"streetNumber\": 60\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"BELGIUM\",\n" +
                "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName8\",\n" +
                "        \"country\": \"BELGIUM\",\n" +
                "        \"postCode\": \"PostCode71\",\n" +
                "        \"since\": \"2017-04-09\",\n" +
                "        \"streetName\": \"StreetName67\",\n" +
                "        \"streetNumber\": 50\n" +
                "      },\n" +
                "      \"email\": \"e8@e.mail\",\n" +
                "      \"firstName\": \"FirstName8\",\n" +
                "      \"lastName\": \"LastName8\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName18\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode81\",\n" +
                "          \"since\": \"2017-04-19\",\n" +
                "          \"streetName\": \"StreetName77\",\n" +
                "          \"streetNumber\": 60\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName19\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode82\",\n" +
                "          \"since\": \"2017-04-20\",\n" +
                "          \"streetName\": \"StreetName78\",\n" +
                "          \"streetNumber\": 61\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName20\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"postCode\": \"PostCode83\",\n" +
                "          \"since\": \"2017-04-21\",\n" +
                "          \"streetName\": \"StreetName79\",\n" +
                "          \"streetNumber\": 62\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"dummyString\": \"String1\",\n" +
                "  \"hashMap\": [\n" +
                "    {\n" +
                "      \"p1\": {\n" +
                "        \"birthCountry\": \"EGYPT\",\n" +
                "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName4\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode67\",\n" +
                "          \"since\": \"2017-04-05\",\n" +
                "          \"streetName\": \"StreetName63\",\n" +
                "          \"streetNumber\": 46\n" +
                "        },\n" +
                "        \"email\": \"e4@e.mail\",\n" +
                "        \"firstName\": \"FirstName4\",\n" +
                "        \"lastName\": \"LastName4\",\n" +
                "        \"previousAddresses\": [\n" +
                "          {\n" +
                "            \"city\": \"CityName14\",\n" +
                "            \"country\": \"AUSTRIA\",\n" +
                "            \"postCode\": \"PostCode77\",\n" +
                "            \"since\": \"2017-04-15\",\n" +
                "            \"streetName\": \"StreetName73\",\n" +
                "            \"streetNumber\": 56\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName15\",\n" +
                "            \"country\": \"BELGIUM\",\n" +
                "            \"postCode\": \"PostCode78\",\n" +
                "            \"since\": \"2017-04-16\",\n" +
                "            \"streetName\": \"StreetName74\",\n" +
                "            \"streetNumber\": 57\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName16\",\n" +
                "            \"country\": \"CANADA\",\n" +
                "            \"postCode\": \"PostCode79\",\n" +
                "            \"since\": \"2017-04-17\",\n" +
                "            \"streetName\": \"StreetName75\",\n" +
                "            \"streetNumber\": 58\n" +
                "          },\n" +
                "          {\n" +
                "            \"city\": \"CityName17\",\n" +
                "            \"country\": \"DENMARK\",\n" +
                "            \"postCode\": \"PostCode80\",\n" +
                "            \"since\": \"2017-04-18\",\n" +
                "            \"streetName\": \"StreetName76\",\n" +
                "            \"streetNumber\": 59\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"p2\": {\n" +
                "        \"birthCountry\": \"FRANCE\",\n" +
                "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                "        \"currentAddress\": {\n" +
                "          \"city\": \"CityName5\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode68\",\n" +
                "          \"since\": \"2017-04-06\",\n" +
                "          \"streetName\": \"StreetName64\",\n" +
                "          \"streetNumber\": 47\n" +
                "        },\n" +
                "        \"email\": \"e5@e.mail\",\n" +
                "        \"firstName\": \"FirstName5\",\n" +
                "        \"lastName\": \"LastName5\",\n" +
                "        \"previousAddresses\": []\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
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
                "      \"firstName\": \"FirstName1\",\n" +
                "      \"lastName\": \"LastName1\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName11\",\n" +
                "          \"country\": \"EGYPT\",\n" +
                "          \"postCode\": \"PostCode74\",\n" +
                "          \"since\": \"2017-04-12\",\n" +
                "          \"streetName\": \"StreetName70\",\n" +
                "          \"streetNumber\": 53\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"birthCountry\": \"CANADA\",\n" +
                "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                "      \"currentAddress\": {\n" +
                "        \"city\": \"CityName2\",\n" +
                "        \"country\": \"CANADA\",\n" +
                "        \"postCode\": \"PostCode65\",\n" +
                "        \"since\": \"2017-04-03\",\n" +
                "        \"streetName\": \"StreetName61\",\n" +
                "        \"streetNumber\": 44\n" +
                "      },\n" +
                "      \"email\": \"e2@e.mail\",\n" +
                "      \"firstName\": \"FirstName2\",\n" +
                "      \"lastName\": \"LastName2\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName12\",\n" +
                "          \"country\": \"FRANCE\",\n" +
                "          \"postCode\": \"PostCode75\",\n" +
                "          \"since\": \"2017-04-13\",\n" +
                "          \"streetName\": \"StreetName71\",\n" +
                "          \"streetNumber\": 54\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        }\n" +
                "      ]\n" +
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
                "      \"firstName\": \"FirstName3\",\n" +
                "      \"lastName\": \"LastName3\",\n" +
                "      \"previousAddresses\": [\n" +
                "        {\n" +
                "          \"city\": \"CityName13\",\n" +
                "          \"country\": \"HUNGARY\",\n" +
                "          \"postCode\": \"PostCode76\",\n" +
                "          \"since\": \"2017-04-14\",\n" +
                "          \"streetName\": \"StreetName72\",\n" +
                "          \"streetNumber\": 55\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName14\",\n" +
                "          \"country\": \"AUSTRIA\",\n" +
                "          \"postCode\": \"PostCode77\",\n" +
                "          \"since\": \"2017-04-15\",\n" +
                "          \"streetName\": \"StreetName73\",\n" +
                "          \"streetNumber\": 56\n" +
                "        },\n" +
                "        {\n" +
                "          \"city\": \"CityName15\",\n" +
                "          \"country\": \"BELGIUM\",\n" +
                "          \"postCode\": \"PostCode78\",\n" +
                "          \"since\": \"2017-04-16\",\n" +
                "          \"streetName\": \"StreetName74\",\n" +
                "          \"streetNumber\": 57\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Function<JsonMatcher<Object>, JsonMatcher<Object>> configurator = jsonMatcher -> jsonMatcher.sortField("array", "array.previousAddresses", "set.previousAddresses", "hashMap.p1.previousAddresses", "hashMap", "set");
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSortingWithLenientMatching(), configurator, null, null);
        assertJsonMatcherWithDummyTestInfo(input, approvedFileContent, enableExpectedFileSorting(), configurator, thrown -> {
            Assertions.assertEquals(getExcceptionMessageForDummyTestInfo("array[0].birthCountry\n" +
                    "Expected: HUNGARY\n" +
                    "     got: AUSTRIA\n" +
                    " ; array[0].birthDate\n" +
                    "Expected: 2011-04-01T13:42:11\n" +
                    "     got: 2010-04-01T13:42:11\n" +
                    " ; array[0].currentAddress.city\n" +
                    "Expected: CityName6\n" +
                    "     got: CityName7\n" +
                    " ; array[0].currentAddress.country\n" +
                    "Expected: HUNGARY\n" +
                    "     got: AUSTRIA\n" +
                    " ; array[0].currentAddress.postCode\n" +
                    "Expected: PostCode69\n" +
                    "     got: PostCode70\n" +
                    " ; array[0].currentAddress.since\n" +
                    "Expected: 2017-04-07\n" +
                    "     got: 2017-04-08\n" +
                    " ; array[0].currentAddress.streetName\n" +
                    "Expected: StreetName65\n" +
                    "     got: StreetName66\n" +
                    " ; array[0].currentAddress.streetNumber\n" +
                    "Expected: 48\n" +
                    "     got: 49\n" +
                    " ; array[0].email\n" +
                    "Expected: e6@e.mail\n" +
                    "     got: e7@e.mail\n" +
                    " ; array[0].firstName\n" +
                    "Expected: FirstName6\n" +
                    "     got: FirstName7\n" +
                    " ; array[0].lastName\n" +
                    "Expected: LastName6\n" +
                    "     got: LastName7\n" +
                    " ; array[0].previousAddresses[]: Expected 1 values but got 2 ; array[1].birthCountry\n" +
                    "Expected: AUSTRIA\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].birthDate\n" +
                    "Expected: 2010-04-01T13:42:11\n" +
                    "     got: 2009-04-01T13:42:11\n" +
                    " ; array[1].currentAddress.city\n" +
                    "Expected: CityName7\n" +
                    "     got: CityName8\n" +
                    " ; array[1].currentAddress.country\n" +
                    "Expected: AUSTRIA\n" +
                    "     got: BELGIUM\n" +
                    " ; array[1].currentAddress.postCode\n" +
                    "Expected: PostCode70\n" +
                    "     got: PostCode71\n" +
                    " ; array[1].currentAddress.since\n" +
                    "Expected: 2017-04-08\n" +
                    "     got: 2017-04-09\n" +
                    " ; array[1].currentAddress.streetName\n" +
                    "Expected: StreetName66\n" +
                    "     got: StreetName67\n" +
                    " ; array[1].currentAddress.streetNumber\n" +
                    "Expected: 49\n" +
                    "     got: 50\n" +
                    " ; array[1].email\n" +
                    "Expected: e7@e.mail\n" +
                    "     got: e8@e.mail\n" +
                    " ; array[1].firstName\n" +
                    "Expected: FirstName7\n" +
                    "     got: FirstName8\n" +
                    " ; array[1].lastName\n" +
                    "Expected: LastName7\n" +
                    "     got: LastName8\n" +
                    " ; array[1].previousAddresses[]: Expected 2 values but got 3 ; array[2].birthCountry\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].birthDate\n" +
                    "Expected: 2009-04-01T13:42:11\n" +
                    "     got: 2011-04-01T13:42:11\n" +
                    " ; array[2].currentAddress.city\n" +
                    "Expected: CityName8\n" +
                    "     got: CityName6\n" +
                    " ; array[2].currentAddress.country\n" +
                    "Expected: BELGIUM\n" +
                    "     got: HUNGARY\n" +
                    " ; array[2].currentAddress.postCode\n" +
                    "Expected: PostCode71\n" +
                    "     got: PostCode69\n" +
                    " ; array[2].currentAddress.since\n" +
                    "Expected: 2017-04-09\n" +
                    "     got: 2017-04-07\n" +
                    " ; array[2].currentAddress.streetName\n" +
                    "Expected: StreetName67\n" +
                    "     got: StreetName65\n" +
                    " ; array[2].currentAddress.streetNumber\n" +
                    "Expected: 50\n" +
                    "     got: 48\n" +
                    " ; array[2].email\n" +
                    "Expected: e8@e.mail\n" +
                    "     got: e6@e.mail\n" +
                    " ; array[2].firstName\n" +
                    "Expected: FirstName8\n" +
                    "     got: FirstName6\n" +
                    " ; array[2].lastName\n" +
                    "Expected: LastName8\n" +
                    "     got: LastName6\n" +
                    " ; array[2].previousAddresses[]: Expected 3 values but got 1"), thrown.getMessage());

            String actual = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
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
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
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
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String expected = "{\n" +
                    "  \"array\": [\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"HUNGARY\",\n" +
                    "      \"birthDate\": \"2011-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName6\",\n" +
                    "        \"country\": \"HUNGARY\",\n" +
                    "        \"postCode\": \"PostCode69\",\n" +
                    "        \"since\": \"2017-04-07\",\n" +
                    "        \"streetName\": \"StreetName65\",\n" +
                    "        \"streetNumber\": 48\n" +
                    "      },\n" +
                    "      \"email\": \"e6@e.mail\",\n" +
                    "      \"firstName\": \"FirstName6\",\n" +
                    "      \"lastName\": \"LastName6\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName16\",\n" +
                    "          \"country\": \"CANADA\",\n" +
                    "          \"postCode\": \"PostCode79\",\n" +
                    "          \"since\": \"2017-04-17\",\n" +
                    "          \"streetName\": \"StreetName75\",\n" +
                    "          \"streetNumber\": 58\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"AUSTRIA\",\n" +
                    "      \"birthDate\": \"2010-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName7\",\n" +
                    "        \"country\": \"AUSTRIA\",\n" +
                    "        \"postCode\": \"PostCode70\",\n" +
                    "        \"since\": \"2017-04-08\",\n" +
                    "        \"streetName\": \"StreetName66\",\n" +
                    "        \"streetNumber\": 49\n" +
                    "      },\n" +
                    "      \"email\": \"e7@e.mail\",\n" +
                    "      \"firstName\": \"FirstName7\",\n" +
                    "      \"lastName\": \"LastName7\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName17\",\n" +
                    "          \"country\": \"DENMARK\",\n" +
                    "          \"postCode\": \"PostCode80\",\n" +
                    "          \"since\": \"2017-04-18\",\n" +
                    "          \"streetName\": \"StreetName76\",\n" +
                    "          \"streetNumber\": 59\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"BELGIUM\",\n" +
                    "      \"birthDate\": \"2009-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName8\",\n" +
                    "        \"country\": \"BELGIUM\",\n" +
                    "        \"postCode\": \"PostCode71\",\n" +
                    "        \"since\": \"2017-04-09\",\n" +
                    "        \"streetName\": \"StreetName67\",\n" +
                    "        \"streetNumber\": 50\n" +
                    "      },\n" +
                    "      \"email\": \"e8@e.mail\",\n" +
                    "      \"firstName\": \"FirstName8\",\n" +
                    "      \"lastName\": \"LastName8\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName18\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode81\",\n" +
                    "          \"since\": \"2017-04-19\",\n" +
                    "          \"streetName\": \"StreetName77\",\n" +
                    "          \"streetNumber\": 60\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName19\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode82\",\n" +
                    "          \"since\": \"2017-04-20\",\n" +
                    "          \"streetName\": \"StreetName78\",\n" +
                    "          \"streetNumber\": 61\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName20\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode83\",\n" +
                    "          \"since\": \"2017-04-21\",\n" +
                    "          \"streetName\": \"StreetName79\",\n" +
                    "          \"streetNumber\": 62\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"dummyString\": \"String1\",\n" +
                    "  \"hashMap\": [\n" +
                    "    {\n" +
                    "      \"p1\": {\n" +
                    "        \"birthCountry\": \"EGYPT\",\n" +
                    "        \"birthDate\": \"2013-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName4\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode67\",\n" +
                    "          \"since\": \"2017-04-05\",\n" +
                    "          \"streetName\": \"StreetName63\",\n" +
                    "          \"streetNumber\": 46\n" +
                    "        },\n" +
                    "        \"email\": \"e4@e.mail\",\n" +
                    "        \"firstName\": \"FirstName4\",\n" +
                    "        \"lastName\": \"LastName4\",\n" +
                    "        \"previousAddresses\": [\n" +
                    "          {\n" +
                    "            \"city\": \"CityName14\",\n" +
                    "            \"country\": \"AUSTRIA\",\n" +
                    "            \"postCode\": \"PostCode77\",\n" +
                    "            \"since\": \"2017-04-15\",\n" +
                    "            \"streetName\": \"StreetName73\",\n" +
                    "            \"streetNumber\": 56\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName15\",\n" +
                    "            \"country\": \"BELGIUM\",\n" +
                    "            \"postCode\": \"PostCode78\",\n" +
                    "            \"since\": \"2017-04-16\",\n" +
                    "            \"streetName\": \"StreetName74\",\n" +
                    "            \"streetNumber\": 57\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName16\",\n" +
                    "            \"country\": \"CANADA\",\n" +
                    "            \"postCode\": \"PostCode79\",\n" +
                    "            \"since\": \"2017-04-17\",\n" +
                    "            \"streetName\": \"StreetName75\",\n" +
                    "            \"streetNumber\": 58\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"city\": \"CityName17\",\n" +
                    "            \"country\": \"DENMARK\",\n" +
                    "            \"postCode\": \"PostCode80\",\n" +
                    "            \"since\": \"2017-04-18\",\n" +
                    "            \"streetName\": \"StreetName76\",\n" +
                    "            \"streetNumber\": 59\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"p2\": {\n" +
                    "        \"birthCountry\": \"FRANCE\",\n" +
                    "        \"birthDate\": \"2012-04-01T13:42:11\",\n" +
                    "        \"currentAddress\": {\n" +
                    "          \"city\": \"CityName5\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode68\",\n" +
                    "          \"since\": \"2017-04-06\",\n" +
                    "          \"streetName\": \"StreetName64\",\n" +
                    "          \"streetNumber\": 47\n" +
                    "        },\n" +
                    "        \"email\": \"e5@e.mail\",\n" +
                    "        \"firstName\": \"FirstName5\",\n" +
                    "        \"lastName\": \"LastName5\",\n" +
                    "        \"previousAddresses\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
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
                    "      \"firstName\": \"FirstName1\",\n" +
                    "      \"lastName\": \"LastName1\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName11\",\n" +
                    "          \"country\": \"EGYPT\",\n" +
                    "          \"postCode\": \"PostCode74\",\n" +
                    "          \"since\": \"2017-04-12\",\n" +
                    "          \"streetName\": \"StreetName70\",\n" +
                    "          \"streetNumber\": 53\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"birthCountry\": \"CANADA\",\n" +
                    "      \"birthDate\": \"2015-04-01T13:42:11\",\n" +
                    "      \"currentAddress\": {\n" +
                    "        \"city\": \"CityName2\",\n" +
                    "        \"country\": \"CANADA\",\n" +
                    "        \"postCode\": \"PostCode65\",\n" +
                    "        \"since\": \"2017-04-03\",\n" +
                    "        \"streetName\": \"StreetName61\",\n" +
                    "        \"streetNumber\": 44\n" +
                    "      },\n" +
                    "      \"email\": \"e2@e.mail\",\n" +
                    "      \"firstName\": \"FirstName2\",\n" +
                    "      \"lastName\": \"LastName2\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName12\",\n" +
                    "          \"country\": \"FRANCE\",\n" +
                    "          \"postCode\": \"PostCode75\",\n" +
                    "          \"since\": \"2017-04-13\",\n" +
                    "          \"streetName\": \"StreetName71\",\n" +
                    "          \"streetNumber\": 54\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        }\n" +
                    "      ]\n" +
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
                    "      \"firstName\": \"FirstName3\",\n" +
                    "      \"lastName\": \"LastName3\",\n" +
                    "      \"previousAddresses\": [\n" +
                    "        {\n" +
                    "          \"city\": \"CityName13\",\n" +
                    "          \"country\": \"HUNGARY\",\n" +
                    "          \"postCode\": \"PostCode76\",\n" +
                    "          \"since\": \"2017-04-14\",\n" +
                    "          \"streetName\": \"StreetName72\",\n" +
                    "          \"streetNumber\": 55\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName14\",\n" +
                    "          \"country\": \"AUSTRIA\",\n" +
                    "          \"postCode\": \"PostCode77\",\n" +
                    "          \"since\": \"2017-04-15\",\n" +
                    "          \"streetName\": \"StreetName73\",\n" +
                    "          \"streetNumber\": 56\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"city\": \"CityName15\",\n" +
                    "          \"country\": \"BELGIUM\",\n" +
                    "          \"postCode\": \"PostCode78\",\n" +
                    "          \"since\": \"2017-04-16\",\n" +
                    "          \"streetName\": \"StreetName74\",\n" +
                    "          \"streetNumber\": 57\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Assertions.assertEquals(actual, thrown.getActual().getStringRepresentation(), "no explicit sorting applied");
            Assertions.assertEquals(expected, thrown.getExpected().getStringRepresentation(), "no explicit sorting applied");
        }, AssertionFailedError.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitPartialSortingByMatcherIgnoredAndFullSortOfExpectedFile(String testName, Object input) {
        testingExplicitSortingByMatcherOfExpectedFile(input,
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("array"),"notExisting2", is("notexisting1")),SortField.of(is("previousAddresses"),"ne3",is("ne4"))).sortFieldMatcher(SortField.of(  is("hashMap"),"ne5",is("ne6")),SortField.of(  is("set"),"ne7", is("ne8"))),
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("array"),"notExisting2", is("notexisting1")),SortField.of(is("previousAddresses"),"ne3",is("ne4")),SortField.of(  is("hashMap"),"ne5",is("ne6")),SortField.of(  is("set"),"ne7", is("ne8"))));
    }
    
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expectedFileSortingTestcases")
    public void testingExplicitPartialSortingByMatcherIgnoredAndFullSortOfExpectedFileOneByOne(String testName, Object input) {
        testingExplicitSortingByMatcherOfExpectedFile(input,
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("array"),"notExisting2", is("notexisting1"))).sortFieldMatcher(SortField.of(is("previousAddresses"),"ne3",is("ne4"))).sortFieldMatcher(SortField.of(  is("hashMap"),"ne5",is("ne6"))).sortFieldMatcher(SortField.of(  is("set"),"ne7", is("ne8"))),
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("array"),"notExisting2", is("notexisting1"))).sortFieldMatcher(SortField.of(is("previousAddresses"),"ne3",is("ne4"))).sortFieldMatcher(SortField.of(  is("hashMap"),"ne5",is("ne6"))).sortFieldMatcher(SortField.of(  is("set"),"ne7", is("ne8"))));
    }
}
