package com.github.karsaig.approvalcrest.testdata;

public class BeanWithGeneric<T> {

    private String dummyString;
    private T genericValue;

    public static <T> BeanWithGeneric<T> of(String dummyString, T genericValue) {
        return new BeanWithGeneric<>(dummyString, genericValue);
    }

    public BeanWithGeneric(String dummyString, T genericValu) {
        this.dummyString = dummyString;
        this.genericValue = genericValu;
    }
}
