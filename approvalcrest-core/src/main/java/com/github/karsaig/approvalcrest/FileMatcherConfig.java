package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.EnvVarReader.getBooleanProperties;

public class FileMatcherConfig {

    private static final String UPDATE_IN_PLACE_OLD_NAME = "jsonMatcherUpdateInPlace";
    private static final String UPDATE_IN_PLACE_NAME = "fileMatcherUpdateInPlace";
    private static final String UPDATE_IN_PLACE_ALIAS = "fMUInPlace";
    private static final String PASS_ON_CREATE = "fileMatcherPassOnCreate";
    private static final String PASS_ON_CREATE_ALIAS = "fMPOnCreate";
    private static final String BUILD_INDEX_NAME = "buildFileIndex";
    private static final String BUILD_INDEX_ALIAS = "bFIndex";
    private static final String APPROVED_DIRECTORY_NAME = "useApprovedDirectory";
    private static final String APPROVED_DIRECTORY_ALIAS = "uADirectory";
    private static final String SORT_INPUT_FILE = "sortInputFile";
    private static final String SORT_INPUT_FILE_ALIAS = "sIFile";
    private static final String STRICT_FILE_MATCHING = "fileMatcherStrictFileMatching";
    private static final String STRICT_FILE_MATCHING_ALIAS = "fMStrictMatching";


    private final boolean overwriteInPlaceEnabled;
    private final boolean passOnCreateEnabled;
    private final boolean buildIndex;
    private final boolean approvedDirectory;
    private final boolean sortInputFile;
    private final boolean strictFileMatching;

    public FileMatcherConfig() {
        overwriteInPlaceEnabled = getBooleanProperties(null, UPDATE_IN_PLACE_OLD_NAME, UPDATE_IN_PLACE_NAME, UPDATE_IN_PLACE_ALIAS);
        passOnCreateEnabled = getBooleanProperties(null, PASS_ON_CREATE, PASS_ON_CREATE_ALIAS);
        buildIndex = getBooleanProperties(null, BUILD_INDEX_NAME, BUILD_INDEX_ALIAS);
        approvedDirectory = getBooleanProperties(null, APPROVED_DIRECTORY_NAME, APPROVED_DIRECTORY_ALIAS);
        sortInputFile = getBooleanProperties(null, SORT_INPUT_FILE, SORT_INPUT_FILE_ALIAS);
        strictFileMatching = getBooleanProperties("true", STRICT_FILE_MATCHING, STRICT_FILE_MATCHING_ALIAS);
    }

    public FileMatcherConfig(boolean overwriteInPlaceEnabled, boolean passOnCreateEnabled, boolean buildIndex, boolean approvedDirectory, boolean sortInputFile, boolean strictMatching) {
        this.overwriteInPlaceEnabled = overwriteInPlaceEnabled;
        this.passOnCreateEnabled = passOnCreateEnabled;
        this.buildIndex = buildIndex;
        this.approvedDirectory = approvedDirectory;
        this.sortInputFile = sortInputFile;
        this.strictFileMatching = strictMatching;
    }



    public boolean isOverwriteInPlaceEnabled() {
        return overwriteInPlaceEnabled;
    }

    public boolean isPassOnCreateEnabled() {
        return passOnCreateEnabled;
    }

    public boolean isBuildIndex() {
        return buildIndex;
    }

    public boolean isApprovedDirectory() {
        return approvedDirectory;
    }

    public boolean isSortInputFile() {
        return sortInputFile;
    }

    public boolean isStrictFileMatching() {
        return strictFileMatching;
    }
}
