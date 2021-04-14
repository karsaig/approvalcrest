package com.github.karsaig.approvalcrest.util;

import static com.github.karsaig.approvalcrest.util.TestDataGenerator.generatePerson;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.testdata.Person;

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
}
