package com.github.karsaig.approvalcrest;

public class EnvVarReader {

    private EnvVarReader() {
    }

    public static boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, null);
    }

    public static boolean getBooleanProperty(String key, String defaultValue) {
        String value = getProperty(key, defaultValue);
        return "true".equalsIgnoreCase(value) || "t".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value);
    }

    private static  String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}
