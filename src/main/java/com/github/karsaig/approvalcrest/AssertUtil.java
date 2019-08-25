package com.github.karsaig.approvalcrest;

public class AssertUtil {

    static public void fail(String message) {
        if (message == null) {
            throw new AssertionError();
        }
        throw new AssertionError(message);
    }
}
