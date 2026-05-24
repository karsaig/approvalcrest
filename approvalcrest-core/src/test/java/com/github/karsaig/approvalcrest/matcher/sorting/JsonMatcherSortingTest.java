package com.github.karsaig.approvalcrest.matcher.sorting;

import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.matcher.ignores.AbstractJsonMatcherIgnoreTest;
import com.github.karsaig.approvalcrest.testdata.BeanWithGenericIterable;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generatePerson;
import static org.hamcrest.Matchers.containsString;
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

    // -------------------------------------------------------------------------
    // sort-gap-1/2/3: sortFieldPath API, real field ignoring, comparison invariant
    // -------------------------------------------------------------------------

    @Test
    public void sortIgnoredPrimitiveFieldIsStillIncludedInComparison() {
        // sort-gap-1: fields in SortField.ignoring() are stripped from the sort key only;
        // the full element (including the ignored field) is still compared against approved.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"25\", \"name\": \"banana\"},\n" +
                "    {\"age\": \"30\", \"name\": \"apple\"}\n" +
                "  ]\n" +
                "}";
        // approved is sorted by name (ignoring age); the age values match the actual values
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"30\", \"name\": \"apple\"},\n" +
                "    {\"age\": \"25\", \"name\": \"banana\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items", "age")), (String) null);
    }

    @Test
    public void sortIgnoredPrimitiveFieldFailsWhenValueDiffers() {
        // sort-gap-1: even though "age" is ignored for sorting, it is still compared;
        // if the age values in approved differ from actual the assertion fails.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"25\", \"name\": \"banana\"},\n" +
                "    {\"age\": \"30\", \"name\": \"apple\"}\n" +
                "  ]\n" +
                "}";
        // approved has the same order as the sort result but different age values
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"99\", \"name\": \"apple\"},\n" +
                "    {\"age\": \"99\", \"name\": \"banana\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items", "age")),
                thrown -> {}, AssertionError.class);
    }

    @Test
    public void sortFieldPathWithRealFieldChangesOrder() {
        // sort-gap-3: sortFieldPath(SortField<String>) API with a real existing field;
        // without ignoring "age", full JSON sort (age < name alphabetically) would keep
        // the original order; with "age" ignored the sort uses only "name" → order inverts.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"25\", \"name\": \"banana\"},\n" +
                "    {\"age\": \"30\", \"name\": \"apple\"}\n" +
                "  ]\n" +
                "}";
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"30\", \"name\": \"apple\"},\n" +
                "    {\"age\": \"25\", \"name\": \"banana\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items", "age")), (String) null);
        // negative: WITHOUT ignoring "age", sort key includes age; "25" < "30" → banana
        // sorts first, but approved (sorted by name) has apple first → FAIL
        assertJsonMatcherWithDummyTestInfo(actual, approved, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items")),
                thrown -> {}, AssertionFailedError.class);
    }

    // -------------------------------------------------------------------------
    // sort-gap-4: matcher-based selector with real field ignoring
    // -------------------------------------------------------------------------

    @Test
    public void sortFieldMatcherWithRealFieldChangesOrder() {
        // sort-gap-4: SortField<Matcher<String>> with a real ignored field — same scenario as
        // sortFieldPathWithRealFieldChangesOrder but using the matcher-based API.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"25\", \"name\": \"banana\"},\n" +
                "    {\"age\": \"30\", \"name\": \"apple\"}\n" +
                "  ]\n" +
                "}";
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"30\", \"name\": \"apple\"},\n" +
                "    {\"age\": \"25\", \"name\": \"banana\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("items"), "age")), (String) null);
        // negative: WITHOUT ignoring "age", sort key includes age → banana sorts first,
        // approved has apple first → FAIL
        assertJsonMatcherWithDummyTestInfo(actual, approved, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("items"))),
                thrown -> {}, AssertionFailedError.class);
    }

    // -------------------------------------------------------------------------
    // sort-gap-5: strict mode for matcher-based sorting
    // -------------------------------------------------------------------------

    @Test
    public void sortFieldMatcherDefaultConfigFailsWhenApprovedIsNotSorted() {
        // sort-gap-5: with the default config (strict, approved not sorted by the framework),
        // the actual side gets sorted by sortFieldMatcher but the approved stays as-is;
        // if approved is not in sorted order the assertion fails.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"name\": \"B\"},\n" +
                "    {\"name\": \"A\"}\n" +
                "  ]\n" +
                "}";
        String unsortedApproved = "{\n" +
                "  \"items\": [\n" +
                "    {\"name\": \"B\"},\n" +
                "    {\"name\": \"A\"}\n" +
                "  ]\n" +
                "}";
        String sortedApproved = "{\n" +
                "  \"items\": [\n" +
                "    {\"name\": \"A\"},\n" +
                "    {\"name\": \"B\"}\n" +
                "  ]\n" +
                "}";
        // default config: actual is sorted to [A, B] but approved stays [B, A] → FAIL
        assertJsonMatcherWithDummyTestInfo(actual, unsortedApproved, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("items"))),
                thrown -> {}, AssertionFailedError.class);
        // approved already in sorted order: actual sorted to [A, B] matches approved [A, B] → PASS
        assertJsonMatcherWithDummyTestInfo(actual, sortedApproved,
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("items"))), (String) null);
    }

    // -------------------------------------------------------------------------
    // sort-gap-8: fan-out — ignored field stripped from every element
    // -------------------------------------------------------------------------

    @Test
    public void sortIgnoringFieldIsAppliedToAllElements() {
        // sort-gap-8: the ignored field is stripped from the sort key of every element,
        // not only the first one; three elements confirm fan-out coverage.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"30\", \"name\": \"cherry\"},\n" +
                "    {\"age\": \"25\", \"name\": \"apple\"},\n" +
                "    {\"age\": \"20\", \"name\": \"banana\"}\n" +
                "  ]\n" +
                "}";
        // sorted by name (ignoring age): apple < banana < cherry
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"age\": \"25\", \"name\": \"apple\"},\n" +
                "    {\"age\": \"20\", \"name\": \"banana\"},\n" +
                "    {\"age\": \"30\", \"name\": \"cherry\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items", "age")), (String) null);
    }

    // -------------------------------------------------------------------------
    // sort-gap-11/13: complex (object) field stripped from sort key
    // -------------------------------------------------------------------------

    @Test
    public void sortIgnoringComplexFieldExcludesItFromSortKey() {
        // sort-gap-11: a whole complex (object) field listed in SortField.ignoring() must be
        // stripped from the sort key, not recursed into unchanged.
        // Without the fix, addr.city ("Alpha" < "Zeta") dominates and B sorts first.
        // With the fix, addr is stripped; sort uses only "name" → A sorts first.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"addr\": {\"city\": \"Alpha\"}, \"name\": \"B\"},\n" +
                "    {\"addr\": {\"city\": \"Zeta\"}, \"name\": \"A\"}\n" +
                "  ]\n" +
                "}";
        // approved: sorted by name, addr values preserved as-is (original, not stripped)
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"addr\": {\"city\": \"Zeta\"}, \"name\": \"A\"},\n" +
                "    {\"addr\": {\"city\": \"Alpha\"}, \"name\": \"B\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items").ignoring("addr")), (String) null);
        // negative: WITHOUT ignoring "addr", the complex field is included in sort key;
        // Alpha < Zeta → B sorts first, but approved has A first → FAIL
        assertJsonMatcherWithDummyTestInfo(actual, approved, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items")),
                thrown -> {}, AssertionFailedError.class);
    }

    @Test
    public void sortFieldMatcherIgnoringComplexFieldExcludesItFromSortKey() {
        // sort-gap-13: same as sortIgnoringComplexFieldExcludesItFromSortKey but using
        // a matcher-based selector — confirms both APIs apply the fix.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"addr\": {\"city\": \"Alpha\"}, \"name\": \"B\"},\n" +
                "    {\"addr\": {\"city\": \"Zeta\"}, \"name\": \"A\"}\n" +
                "  ]\n" +
                "}";
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"addr\": {\"city\": \"Zeta\"}, \"name\": \"A\"},\n" +
                "    {\"addr\": {\"city\": \"Alpha\"}, \"name\": \"B\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("items")).ignoring("addr")), (String) null);
        // negative: WITHOUT ignoring "addr", Alpha < Zeta → B sorts first → FAIL
        assertJsonMatcherWithDummyTestInfo(actual, approved, getDefaultFileMatcherConfig(),
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("items"))),
                thrown -> {}, AssertionFailedError.class);
    }

    // -------------------------------------------------------------------------
    // sort-gap-14: multi-level dot-path ignore
    // -------------------------------------------------------------------------

    @Test
    public void sortIgnoringMultiLevelPathExcludesNestedFieldFromSortKey() {
        // sort-gap-14: a dot-path like "addr.city" strips only the leaf "city" from inside "addr";
        // both elements then have addr={} in their sort keys, so "name" breaks the tie.
        // Without the fix the recursive call looks up the wrong nextLevel key and addr is
        // not filtered at all → city still present → B (Alpha) sorts before A (Zeta).
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"addr\": {\"city\": \"Alpha\"}, \"name\": \"B\"},\n" +
                "    {\"addr\": {\"city\": \"Zeta\"}, \"name\": \"A\"}\n" +
                "  ]\n" +
                "}";
        // approved: sorted by name (addr preserved in full for comparison)
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"addr\": {\"city\": \"Zeta\"}, \"name\": \"A\"},\n" +
                "    {\"addr\": {\"city\": \"Alpha\"}, \"name\": \"B\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items").ignoring("addr.city")), (String) null);
    }

    // -------------------------------------------------------------------------
    // sort-gap-2: sortFieldPath(SortField<String>) basic API (no ignoring)
    // -------------------------------------------------------------------------

    @Test
    public void sortFieldPathApiSortsWithoutIgnoring() {
        // sort-gap-2: sortFieldPath(SortField<String>) without any ignoring must produce the
        // same result as the plain sortField(String) shorthand — verify the SortField API route.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"name\": \"banana\"},\n" +
                "    {\"name\": \"apple\"}\n" +
                "  ]\n" +
                "}";
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"name\": \"apple\"},\n" +
                "    {\"name\": \"banana\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items")), (String) null);
    }

    // -------------------------------------------------------------------------
    // sort-gap-6: root-level Set is sorted (type-based sort configuration)
    // -------------------------------------------------------------------------

    @Test
    public void rootSetIsSortedByTypeBasedConfiguration() {
        // sort-gap-6: Set fields are configured for sorting by their Java type;
        // when the root object is a Set its elements are sorted
        // (exercises the pathsToSort.getOrDefault("", ...) code path).
        Set<String> actual = new LinkedHashSet<>(Arrays.asList("cherry", "apple", "banana"));
        String approved = "[\"apple\",\"banana\",\"cherry\"]";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher, (String) null);
    }

    // -------------------------------------------------------------------------
    // sort-gap-9: matcher-based ignore strips all matching fields
    // -------------------------------------------------------------------------

    @Test
    public void sortIgnoringByMatcherStripsAllMatchingFieldsFromSortKey() {
        // sort-gap-9: a Matcher<String> in ignoring() that matches multiple field names
        // (e.g. containsString("Date")) strips ALL matched fields from the sort key,
        // not just the first.
        String actual = "{\n" +
                "  \"items\": [\n" +
                "    {\"startDate\": \"2023-01\", \"endDate\": \"2023-12\", \"name\": \"B\"},\n" +
                "    {\"startDate\": \"2022-01\", \"endDate\": \"2022-12\", \"name\": \"A\"}\n" +
                "  ]\n" +
                "}";
        // sorted by name only (both *Date fields stripped from sort key): A < B
        String approved = "{\n" +
                "  \"items\": [\n" +
                "    {\"startDate\": \"2022-01\", \"endDate\": \"2022-12\", \"name\": \"A\"},\n" +
                "    {\"startDate\": \"2023-01\", \"endDate\": \"2023-12\", \"name\": \"B\"}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("items").ignoring(containsString("Date"))),
                (String) null);
    }

    // -------------------------------------------------------------------------
    // sort-gap-10: matcher selector applied to multiple nested locations (fan-out)
    // -------------------------------------------------------------------------

    @Test
    public void sortFieldMatcherSelectorAppliedToMultipleNestedLocations() {
        // sort-gap-10: when a field-matcher selector matches the same field name at
        // multiple nested locations (e.g. "tags" inside every group element), sorting and
        // ignoring is applied to each occurrence independently.
        String actual = "{\n" +
                "  \"groups\": [\n" +
                "    {\"tags\": [{\"w\": \"5\", \"name\": \"Z\"}, {\"w\": \"3\", \"name\": \"A\"}]},\n" +
                "    {\"tags\": [{\"w\": \"1\", \"name\": \"Y\"}, {\"w\": \"2\", \"name\": \"B\"}]}\n" +
                "  ]\n" +
                "}";
        // each tags sorted by name (w stripped): first → [A, Z], second → [B, Y]
        String approved = "{\n" +
                "  \"groups\": [\n" +
                "    {\"tags\": [{\"w\": \"3\", \"name\": \"A\"}, {\"w\": \"5\", \"name\": \"Z\"}]},\n" +
                "    {\"tags\": [{\"w\": \"2\", \"name\": \"B\"}, {\"w\": \"1\", \"name\": \"Y\"}]}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldMatcher(SortField.of(is("tags"), "w")),
                (String) null);
    }

    // -------------------------------------------------------------------------
    // sort-gap-12: collection-in-collection sort elements
    // -------------------------------------------------------------------------

    @Test
    public void collectionInCollectionSortingOrdersInnerArraysByTheirContent() {
        // sort-gap-12: sort a collection whose elements are themselves collections;
        // getFilteredStringForSorting hits the isJsonArray branch for inner elements
        // and uses their full JSON as sort key.
        String actual = "{\n" +
                "  \"matrix\": [\n" +
                "    [\"Z\", \"B\"],\n" +
                "    [\"A\", \"C\"]\n" +
                "  ]\n" +
                "}";
        // inner arrays are sorted too (direct fan-out): ["A","C"] and ["B","Z"];
        // outer sort: ["A","C"] < ["B","Z"]
        String approved = "{\n" +
                "  \"matrix\": [\n" +
                "    [\"A\", \"C\"],\n" +
                "    [\"B\", \"Z\"]\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("matrix")), (String) null);
    }

    // -------------------------------------------------------------------------
    // array-of-array-of-beans: configured sort fans out into direct inner arrays;
    // fields inside beans are only sorted when explicitly configured
    // -------------------------------------------------------------------------

    @Test
    public void arrayOfArrayOfBeansSortsInnerArraysAndReordersOuter() {
        // sortField("groups") sorts the outer array. Because the direct elements are
        // themselves arrays, each inner array is also sorted (fan-out applies).
        // The sort key for the outer comparison is computed after inner sorting.
        String actual = "{\n" +
                "  \"groups\": [\n" +
                "    [{\"name\": \"D\"}, {\"name\": \"C\"}],\n" +
                "    [{\"name\": \"B\"}, {\"name\": \"A\"}]\n" +
                "  ]\n" +
                "}";
        // inner[0] sorted → [{name:C},{name:D}]; inner[1] sorted → [{name:A},{name:B}]
        // outer sort keys: {"name":"C"}... vs {"name":"A"}... → A-group first
        String approved = "{\n" +
                "  \"groups\": [\n" +
                "    [{\"name\": \"A\"}, {\"name\": \"B\"}],\n" +
                "    [{\"name\": \"C\"}, {\"name\": \"D\"}]\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("groups")), (String) null);
    }

    @Test
    public void arrayOfArrayOfBeansWithIgnoredFieldSortsByRemainingFields() {
        // Ignoring a field propagates into inner arrays: sort key strips the ignored
        // field from each bean within the inner arrays.
        String actual = "{\n" +
                "  \"groups\": [\n" +
                "    [{\"age\": \"30\", \"name\": \"B\"}, {\"age\": \"25\", \"name\": \"A\"}],\n" +
                "    [{\"age\": \"20\", \"name\": \"D\"}, {\"age\": \"15\", \"name\": \"C\"}]\n" +
                "  ]\n" +
                "}";
        // inner sorted by name (age stripped from sort key): [A,B] and [C,D]
        // outer: A-group first; original age values preserved in output
        String approved = "{\n" +
                "  \"groups\": [\n" +
                "    [{\"age\": \"25\", \"name\": \"A\"}, {\"age\": \"30\", \"name\": \"B\"}],\n" +
                "    [{\"age\": \"15\", \"name\": \"C\"}, {\"age\": \"20\", \"name\": \"D\"}]\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("groups", "age")), (String) null);
    }

    @Test
    public void beanFieldsInsideSortedArrayAreOnlySortedWhenConfigured() {
        // When the direct elements of a sorted array are beans (objects), only fields
        // that were explicitly configured via sortField are sorted inside them.
        // Fields with no sort configuration keep their original order.
        String actual = "{\n" +
                "  \"groups\": [\n" +
                "    {\"name\": \"B\", \"tags\": [\"z\", \"a\"]},\n" +
                "    {\"name\": \"A\", \"tags\": [\"y\", \"b\"]}\n" +
                "  ]\n" +
                "}";
        // outer sorted by full bean JSON: A-bean first; tags arrays left untouched
        String approved = "{\n" +
                "  \"groups\": [\n" +
                "    {\"name\": \"A\", \"tags\": [\"y\", \"b\"]},\n" +
                "    {\"name\": \"B\", \"tags\": [\"z\", \"a\"]}\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(actual, approved,
                jsonMatcher -> jsonMatcher.sortFieldPath(SortField.of("groups")), (String) null);
    }
}
