package com.github.karsaig.approvalcrest.util;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;

public class PreBuilt {

    private PreBuilt() {
    }

    public static BeanWithPrimitives getBeanWithPrimitives() {
        short beanShort = 1;
        boolean beanBoolean = true;
        byte beanByte = 2;
        char beanChar = 'c';
        float beanFloat = 3f;
        int beanInt = 4;
        double beanDouble = 5d;
        long beanLong = 6L;

        BeanWithPrimitives bean = BeanWithPrimitives.Builder.beanWithPrimitives()
                .beanShort(beanShort)
                .beanBoolean(beanBoolean)
                .beanByte(beanByte)
                .beanChar(beanChar)
                .beanFloat(beanFloat)
                .beanInt(beanInt)
                .beanDouble(beanDouble)
                .beanLong(beanLong)
                .build();

        return bean;
    }

    public static String getBeanWithPrimitivesAsJsonString() {
        return "{\n" +
                "  \"beanInteger\": 4,\n" +
                "  \"beanByte\": 2,\n" +
                "  \"beanChar\": \"c\",\n" +
                "  \"beanShort\": 1,\n" +
                "  \"beanLong\": 6,\n" +
                "  \"beanFloat\": 3.0,\n" +
                "  \"beanDouble\": 5.0,\n" +
                "  \"beanBoolean\": true\n" +
                "}";
    }
}
