package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.EnvVarReader.getBooleanProperties;
import static com.github.karsaig.approvalcrest.EnvVarReader.getIntProperties;
import static com.github.karsaig.approvalcrest.EnvVarReader.getStringProperties;

public class FileMatcherConfig {

    private static final String UPDATE_IN_PLACE_OLD_NAME = "jsonMatcherUpdateInPlace";
    private static final String UPDATE_IN_PLACE_NAME = "fileMatcherUpdateInPlace";
    private static final String UPDATE_IN_PLACE_ALIAS = "fMUInPlace";
    private static final String PASS_ON_CREATE = "fileMatcherPassOnCreate";
    private static final String PASS_ON_CREATE_ALIAS = "fMPOnCreate";
    private static final String APPROVED_DIRECTORY_NAME = "useApprovedDirectory";
    private static final String APPROVED_DIRECTORY_ALIAS = "uADirectory";
    private static final String SORT_INPUT_FILE = "sortInputFile";
    private static final String SORT_INPUT_FILE_ALIAS = "sIFile";
    private static final String STRICT_FILE_MATCHING = "fileMatcherStrictFileMatching";
    private static final String STRICT_FILE_MATCHING_ALIAS = "fMStrictMatching";
    private static final String SHARED_DIR_NAME = "fileMatcherSharedDir";
    private static final String SHARED_DIR_ALIAS = "fmSharedDir";
    private static final String SHARED_ENABLED_NAME = "fileMatcherSharedEnabled";
    private static final String SHARED_ENABLED_ALIAS = "fmSharedEnabled";
    private static final String SHARED_BUCKET_DEPTH_NAME = "fileMatcherSharedDirBucketDepth";
    private static final String SHARED_BUCKET_DEPTH_ALIAS = "fmSharedDirBucketDepth";

    private static final String DEFAULT_SHARED_DIR = "src/test/java/shared-approvals";
    private static final int DEFAULT_SHARED_BUCKET_DEPTH = 2;

    /**
     * Bucket depth is used as {@code contentKey.substring(0, depth)}, so it has to stay within the
     * documented range. Anything outside it would either fail with an opaque
     * {@link StringIndexOutOfBoundsException} deep inside the matcher, or fan the shared directory
     * out over an absurd number of buckets.
     */
    public static final int MIN_SHARED_BUCKET_DEPTH = 1;
    public static final int MAX_SHARED_BUCKET_DEPTH = 6;

    private final boolean overwriteInPlaceEnabled;
    private final boolean passOnCreateEnabled;
    private final boolean approvedDirectory;
    private final boolean sortInputFile;
    private final boolean strictFileMatching;
    private final String sharedApprovalDirectory;
    private final boolean sharedEnabled;
    private final int sharedBucketDepth;

    public FileMatcherConfig() {
        overwriteInPlaceEnabled = getBooleanProperties(null, UPDATE_IN_PLACE_OLD_NAME, UPDATE_IN_PLACE_NAME, UPDATE_IN_PLACE_ALIAS);
        passOnCreateEnabled = getBooleanProperties(null, PASS_ON_CREATE, PASS_ON_CREATE_ALIAS);
        approvedDirectory = getBooleanProperties(null, APPROVED_DIRECTORY_NAME, APPROVED_DIRECTORY_ALIAS);
        sortInputFile = getBooleanProperties(null, SORT_INPUT_FILE, SORT_INPUT_FILE_ALIAS);
        strictFileMatching = getBooleanProperties("true", STRICT_FILE_MATCHING, STRICT_FILE_MATCHING_ALIAS);
        sharedApprovalDirectory = getStringProperties(DEFAULT_SHARED_DIR, SHARED_DIR_NAME, SHARED_DIR_ALIAS);
        sharedEnabled = getBooleanProperties(null, SHARED_ENABLED_NAME, SHARED_ENABLED_ALIAS);
        sharedBucketDepth = validateBucketDepth(getIntProperties(DEFAULT_SHARED_BUCKET_DEPTH, SHARED_BUCKET_DEPTH_NAME, SHARED_BUCKET_DEPTH_ALIAS));
    }

    private static int validateBucketDepth(int bucketDepth) {
        if (bucketDepth < MIN_SHARED_BUCKET_DEPTH || bucketDepth > MAX_SHARED_BUCKET_DEPTH) {
            throw new IllegalStateException("Invalid value for property " + SHARED_BUCKET_DEPTH_NAME
                    + " (alias " + SHARED_BUCKET_DEPTH_ALIAS + "): " + bucketDepth
                    + ". Must be between " + MIN_SHARED_BUCKET_DEPTH + " and " + MAX_SHARED_BUCKET_DEPTH + ".");
        }
        return bucketDepth;
    }

    public FileMatcherConfig(boolean overwriteInPlaceEnabled, boolean passOnCreateEnabled, boolean approvedDirectory, boolean sortInputFile, boolean strictMatching) {
        this(overwriteInPlaceEnabled, passOnCreateEnabled, approvedDirectory, sortInputFile, strictMatching, DEFAULT_SHARED_DIR, false, DEFAULT_SHARED_BUCKET_DEPTH);
    }

    public FileMatcherConfig(boolean overwriteInPlaceEnabled, boolean passOnCreateEnabled, boolean approvedDirectory, boolean sortInputFile, boolean strictMatching, String sharedApprovalDirectory, boolean sharedEnabled, int sharedBucketDepth) {
        this.overwriteInPlaceEnabled = overwriteInPlaceEnabled;
        this.passOnCreateEnabled = passOnCreateEnabled;
        this.approvedDirectory = approvedDirectory;
        this.sortInputFile = sortInputFile;
        this.strictFileMatching = strictMatching;
        this.sharedApprovalDirectory = sharedApprovalDirectory;
        this.sharedEnabled = sharedEnabled;
        this.sharedBucketDepth = validateBucketDepth(sharedBucketDepth);
    }

    public boolean isOverwriteInPlaceEnabled() {
        return overwriteInPlaceEnabled;
    }

    public boolean isPassOnCreateEnabled() {
        return passOnCreateEnabled;
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

    public String getSharedApprovalDirectory() {
        return sharedApprovalDirectory;
    }

    public boolean isSharedEnabled() {
        return sharedEnabled;
    }

    public int getSharedBucketDepth() {
        return sharedBucketDepth;
    }
}
