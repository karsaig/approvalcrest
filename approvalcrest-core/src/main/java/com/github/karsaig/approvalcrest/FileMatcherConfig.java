package com.github.karsaig.approvalcrest;

public class FileMatcherConfig {

    private static final String UPDATE_IN_PLACE_OLD_NAME = "jsonMatcherUpdateInPlace";
    private static final String UPDATE_IN_PLACE_NAME = "fileMatcherUpdateInPlace";
    private static final String PASS_ON_CREATE = "fileMatcherPassOnCreate";
    private static final String BUILD_INDEX_NAME = "buildFileIndex";
    private static final String APPROVED_DIRECTORY_NAME = "useApprovedDirectory";


    private final boolean overwriteInPlaceEnabled;
    private final boolean passOnCreateEnabled;
    private final boolean buildIndex;
    private final boolean approvedDirectory;

    public FileMatcherConfig() {
        overwriteInPlaceEnabled = getBooleanProperty(UPDATE_IN_PLACE_OLD_NAME) || getBooleanProperty(UPDATE_IN_PLACE_NAME);
        passOnCreateEnabled = getBooleanProperty(PASS_ON_CREATE);
        buildIndex = getBooleanProperty(BUILD_INDEX_NAME);
        approvedDirectory = getBooleanProperty(APPROVED_DIRECTORY_NAME);
    }

    public FileMatcherConfig(boolean overwriteInPlaceEnabled, boolean passOnCreateEnabled, boolean buildIndex, boolean approvedDirectory) {
        this.overwriteInPlaceEnabled = overwriteInPlaceEnabled;
        this.passOnCreateEnabled = passOnCreateEnabled;
        this.buildIndex = buildIndex;
        this.approvedDirectory = approvedDirectory;
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
