package com.github.karsaig.approvalcrest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

public class StringUtil {

    private StringUtil() {
    }

    public static String normalizeNewLines(String input) {
        return Strings.CS.replace(input, "\r\n", "\n");
    }
}
