package com.github.karsaig.approvalcrest;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    private StringUtil() {
    }

    public static String normalizeNewLines(String input) {
        return StringUtils.replace(input, "\r\n", "\n");
    }
}
