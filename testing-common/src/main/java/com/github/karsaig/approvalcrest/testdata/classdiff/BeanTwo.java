package com.github.karsaig.approvalcrest.testdata.classdiff;

import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;

public class BeanTwo extends BeanWithGeneric<String> {
    public BeanTwo(String dummyString, String genericValue) {
        super(dummyString, genericValue);
    }
}
