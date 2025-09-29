package com.github.karsaig.approvalcrest;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class Junit4DesciptionWatcher extends TestWatcher {
    protected Description description;

    public Description getDescription() {
        return description;
    }

    @Override
    protected void starting(Description description) {
        this.description = description;
    }
}
