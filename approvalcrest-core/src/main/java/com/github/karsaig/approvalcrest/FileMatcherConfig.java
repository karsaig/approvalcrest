package com.github.karsaig.approvalcrest;

public class FileMatcherConfig {

    private static final String UPDATE_IN_PLACE_OLD_NAME = "jsonMatcherUpdateInPlace";
    private static final String UPDATE_IN_PLACE_NAME = "fileMatcherUpdateInPlace";
    private static final String PASS_ON_CREATE = "fileMatcherPassOnCreate";

    private final boolean overwriteInPlaceEnabled;
    private final boolean passOnCreateEnabled;

    public FileMatcherConfig() {
        overwriteInPlaceEnabled = getBooleanProperty(UPDATE_IN_PLACE_OLD_NAME) || getBooleanProperty(UPDATE_IN_PLACE_NAME);
        passOnCreateEnabled = getBooleanProperty(PASS_ON_CREATE);
    }

    public FileMatcherConfig(boolean overwriteInPlaceEnabled, boolean passOnCreateEnabled) {
        this.overwriteInPlaceEnabled = overwriteInPlaceEnabled;
        this.passOnCreateEnabled = passOnCreateEnabled;
    }

    private boolean getBooleanProperty(String key) {
        return "true".equals(getProperty(key));
    }

    String getProperty(String key) {
        return System.getProperty(key);
    }

    public boolean isOverwriteInPlaceEnabled() {
        return overwriteInPlaceEnabled;
    }

    public boolean isPassOnCreateEnabled() {
        return passOnCreateEnabled;
    }
}
