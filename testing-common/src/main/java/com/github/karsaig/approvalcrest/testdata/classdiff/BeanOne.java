package com.github.karsaig.approvalcrest.testdata.classdiff;

import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;

public class BeanOne extends BeanWithGeneric<String> {
    public BeanOne(String dummyString, String genericValue) {
        super(dummyString, genericValue);
    }
}
