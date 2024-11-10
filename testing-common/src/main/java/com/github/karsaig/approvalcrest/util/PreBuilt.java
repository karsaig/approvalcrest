package com.github.karsaig.approvalcrest.util;

import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generatePerson;

import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.testdata.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreBuilt {

    private PreBuilt() {
    }

    public static BeanWithPrimitives.Builder getBeanWithPrimitivesBuilder() {
        short beanShort = 1;
        boolean beanBoolean = true;
        byte beanByte = 2;
        char beanChar = 'c';
        float beanFloat = 3f;
        int beanInt = 4;
        double beanDouble = 5d;
        long beanLong = 6L;

        return BeanWithPrimitives.Builder.beanWithPrimitives()
                .beanShort(beanShort)
                .beanBoolean(beanBoolean)
                .beanByte(beanByte)
                .beanChar(beanChar)
                .beanFloat(beanFloat)
                .beanInt(beanInt)
                .beanDouble(beanDouble)
                .beanLong(beanLong);
    }

    public static BeanWithPrimitives getBeanWithPrimitives() {
        return getBeanWithPrimitivesBuilder().build();
    }

    public static String getBeanWithPrimitivesAsJsonString() {
        return "{\n" +
                "  \"beanBoolean\": true,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanLong\": 6,\n" +
                "  \"beanShort\": 1\n" +
                "}";
    }

    public static Person getPerson() {
        return generatePerson(1L);
    }

    public static String getPersonAsJsonString() {
        return "{\n" +
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
    }

    public static List<Map<String, BeanWithGeneric<List<Map<String, String>>>>> getComplexListAndMap() {


        Map<String, String> innerMap1 = new HashMap<>();
        innerMap1.put("innerKey4", "innerValue4");
        innerMap1.put("innerKey3", "innerValue3");
        innerMap1.put("innerKey2", "innerValue2");
        innerMap1.put("innerKey1", "innerValue1");

        Map<String, String> innerMap2 = new HashMap<>();
        innerMap2.put("innerKey24", "innerValue24");
        innerMap2.put("innerKey23", "innerValue23");
        innerMap2.put("innerKey22", "innerValue22");
        innerMap2.put("innerKey21", "innerValue21");

        List<Map<String, String>> innerList1 = new ArrayList<>();
        innerList1.add(innerMap2);
        innerList1.add(innerMap1);


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





        Map<String, String> innerMap5 = new HashMap<>();
        innerMap5.put("innerKey1", "innerValue51");
        innerMap5.put("innerKey2", "innerValue52");
        innerMap5.put("innerKey3", "innerValue53");
        innerMap5.put("innerKey4", "innerValue54");

        List<Map<String, String>> innerList3 = new ArrayList<>();
        innerList3.add(innerMap5);

        BeanWithGeneric<List<Map<String, String>>> bean3 = new BeanWithGeneric<>("beanString3", innerList3);

        Map<String, BeanWithGeneric<List<Map<String, String>>>> outerMap2 = new HashMap<>();
        outerMap2.put("outerKey1", bean3);

        List<Map<String, BeanWithGeneric<List<Map<String, String>>>>> result = new ArrayList<>();
        result.add(outerMap2);
        result.add(outerMap1);

        return result;
    }

    public static String getComplexListAndMapAsJsonString() {
        return "[\n" +
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
    }
}
