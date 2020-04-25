package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.AssertUtil.fail;
import static com.github.karsaig.approvalcrest.matcher.FileStoreMatcherUtils.SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.hash.Hashing;

public abstract class AbstractDiagnosingFileMatcher<T, U extends AbstractDiagnosingFileMatcher<T, U>> extends AbstractDiagnosingMatcher<T> implements ApprovedFileMatcher<U> {

    private static final String UPDATE_IN_PLACE_NAME = "jsonMatcherUpdateInPlace";
    public static final int NUM_OF_HASH_CHARS = 6;
    private boolean overwriteInPlaceEnabled = "true".equals(System.getProperty(UPDATE_IN_PLACE_NAME));
    private FileStoreMatcherUtils fileStoreMatcherUtils;
    private TestMetaInformation testMetaInformation;
    protected String fileName;
    protected String testMethodName;
    protected String testClassName;
    protected String customFileName;
    protected String uniqueId;
    protected Path pathName;
    protected String testClassNameHash;

    protected Path fileNameWithPath;

    public AbstractDiagnosingFileMatcher(TestMetaInformation testMetaInformation, FileStoreMatcherUtils fileStoreMatcherUtils) {
        this.testMetaInformation = testMetaInformation;
        this.fileStoreMatcherUtils = fileStoreMatcherUtils;
    }

    protected void init() {
        testMethodName = testMetaInformation.testMethodName();
        testClassName = testMetaInformation.testClassName();

        if (customFileName == null || customFileName.trim().isEmpty()) {
            fileName = hashFileName(testMethodName);
        } else {
            fileName = customFileName;
        }
        if (uniqueId != null) {
            fileName += SEPARATOR + uniqueId;
        }
        if (pathName == null) {
            testClassNameHash = hashFileName(testClassName);
            pathName = testMetaInformation.getTestClassPath().resolve(testClassNameHash);
        }

        fileNameWithPath = pathName.resolve(fileName);
    }

    @Override
    public U withUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return (U) this;
    }

    @Override
    public U withFileName(String customFileName) {
        this.customFileName = customFileName;
        return (U) this;
    }

    @Override
    public U withPathName(String pathName) {
        this.pathName = Paths.get(pathName);
        return (U) this;
    }

    @Override
    public U withPath(Path path) {
        this.pathName = path;
        return (U) this;
    }

    private String hashFileName(String fileName) {
        return Hashing.sha1().hashString(fileName, Charsets.UTF_8).toString().substring(0, NUM_OF_HASH_CHARS);
    }

    protected String getAssertMessage(FileStoreMatcherUtils fileStoreMatcherUtils, String message) {
        String result;
        if (testClassNameHash == null) {
            result = "Expected file " + fileNameWithPath + "\n" + message;
        } else {
            result = "Expected file " + testClassNameHash + File.separator
                    + fileStoreMatcherUtils.getFullFileName(Paths.get(fileName), true) + "\n" + message;
        }
        return result;
    }

    protected String getAssertMessage(FileStoreMatcherUtils fileStoreMatcherUtils, Throwable t) {
        return getAssertMessage(fileStoreMatcherUtils, t.getMessage());
    }

    protected boolean isOverwriteInPlaceEnabled() {
        return overwriteInPlaceEnabled;
    }

    @VisibleForTesting
    void setOverwriteInPlaceEnabled(boolean overwriteInPlaceEnabled) {
        this.overwriteInPlaceEnabled = overwriteInPlaceEnabled;
    }

    protected void createNotApprovedFileIfNotExists(Object toApprove, Supplier<String> content) {
        Path approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);

        if (Files.notExists(approvedFile)) {
            try {
                String approvedFileName = approvedFile.getFileName().toString();
                String createdFileName = fileStoreMatcherUtils.createNotApproved(fileNameWithPath, content.get(), getCommentLine());
                String message;
                if (testClassNameHash == null) {
                    message = "Not approved file created: '" + createdFileName
                            + "';\n please verify its contents and rename it to '" + approvedFileName + "'.";
                } else {
                    message = "Not approved file created: '" + testClassNameHash + File.separator + createdFileName
                            + "';\n please verify its contents and rename it to '" + approvedFileName + "'.";
                }
                fail(message);

            } catch (IOException e) {
                throw new IllegalStateException(
                        String.format("Exception while creating not approved file %s", toApprove.toString()), e);
            }
        }
    }

    protected String getCommentLine() {
        return testClassName + "." + testMethodName;
    }

    @VisibleForTesting
    void setJsonMatcherUtils(FileStoreMatcherUtils jsonMatcherUtils) {
        this.fileStoreMatcherUtils = jsonMatcherUtils;
    }
}
