package com.github.karsaig.approvalcrest.testdata;

public class DummyBean {

    private int beanInt;
    private String beanString;
    private boolean beanBoolean;
    private DummyBean beanParent;

    public DummyBean(int beanInt, String beanString, boolean beanBoolean, DummyBean beanParent) {
        super();
        this.beanInt = beanInt;
        this.beanString = beanString;
        this.beanBoolean = beanBoolean;
        this.beanParent = beanParent;
    }

    public int getBeanInt() {
        return beanInt;
    }

    public String getBeanString() {
        return beanString;
    }

    public boolean isBeanBoolean() {
        return beanBoolean;
    }

    public DummyBean getBeanParent() {
        return beanParent;
    }

}
