package com.github.karsaig.approvalcrest;

import java.util.Arrays;

public class EnvVarReader {

    private EnvVarReader() {
    }

    public static boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, null);
    }

    public static boolean getBooleanProperty(String key, String defaultValue) {
        String value = getProperty(key, defaultValue);
        return parseBoolean(value);
    }

    /**
     * Resolves a boolean property from multiple alternative key names (e.g. canonical name + shorthand alias).
     * <ul>
     *   <li>If none of the keys are explicitly set, returns {@code parseBoolean(defaultValue)}.</li>
     *   <li>If exactly one key is set, returns its parsed value.</li>
     *   <li>If multiple keys are set to the <em>same</em> value, returns that value.</li>
     *   <li>If multiple keys are set to <em>different</em> values, throws {@link IllegalStateException}.</li>
     * </ul>
     */
    public static boolean getBooleanProperties(String defaultValue, String... keys) {
        Boolean result = null;
        for (String key : keys) {
            String value = System.getProperty(key);
            if (value != null) {
                boolean parsed = parseBoolean(value);
                if (result == null) {
                    result = parsed;
                } else if (result != parsed) {
                    throw new IllegalStateException(
                            "Ambiguous configuration: conflicting values set for properties: "
                                    + Arrays.toString(keys));
                }
            }
        }
        return result != null ? result : parseBoolean(defaultValue);
    }

    static boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "t".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value);
    }

    private static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}
