package com.github.karsaig.approvalcrest;

public class FileMatcherConfig {

    private static final String UPDATE_IN_PLACE_OLD_NAME = "jsonMatcherUpdateInPlace";
    private static final String UPDATE_IN_PLACE_NAME = "fileMatcherUpdateInPlace";
    private static final String PASS_ON_CREATE = "fileMatcherPassOnCreate";
    private static final String BUILD_INDEX_NAME = "buildFileIndex";
    private static final String APPROVED_DIRECTORY_NAME = "useApprovedDirectory";
    private static final String SORT_INPUT_FILE = "sortInputFile";
    private static final String STRICT_MATCHING = "strictMatching";


    private final boolean overwriteInPlaceEnabled;
    private final boolean passOnCreateEnabled;
    private final boolean buildIndex;
    private final boolean approvedDirectory;
    private final boolean sortInputFile;
    private final boolean strictMatching;

    public FileMatcherConfig() {
        overwriteInPlaceEnabled = getBooleanProperty(UPDATE_IN_PLACE_OLD_NAME) || getBooleanProperty(UPDATE_IN_PLACE_NAME);
        passOnCreateEnabled = getBooleanProperty(PASS_ON_CREATE);
        buildIndex = getBooleanProperty(BUILD_INDEX_NAME);
        approvedDirectory = getBooleanProperty(APPROVED_DIRECTORY_NAME);
        sortInputFile = getBooleanProperty(SORT_INPUT_FILE);
        strictMatching = getBooleanProperty(STRICT_MATCHING, "true");
    }

    public FileMatcherConfig(boolean overwriteInPlaceEnabled, boolean passOnCreateEnabled, boolean buildIndex, boolean approvedDirectory, boolean sortInputFile, boolean strictMatching) {
        this.overwriteInPlaceEnabled = overwriteInPlaceEnabled;
        this.passOnCreateEnabled = passOnCreateEnabled;
        this.buildIndex = buildIndex;
        this.approvedDirectory = approvedDirectory;
        this.sortInputFile = sortInputFile;
        this.strictMatching = strictMatching;
    }

    private boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, null);
    }

    private boolean getBooleanProperty(String key, String defaultValue) {
        String value = getProperty(key, defaultValue);
        return "true".equalsIgnoreCase(value) || "t".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value);
    }

    private String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    public boolean isOverwriteInPlaceEnabled() {
        return overwriteInPlaceEnabled;
    }

    public boolean isPassOnCreateEnabled() {
        return passOnCreateEnabled;
    }

    public boolean isSortInputFile() {
        return sortInputFile;
    }

    public boolean isStrictMatching() {
        return strictMatching;
    }
}
