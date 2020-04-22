package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.matcher.FileStoreMatcherUtils.SEPARATOR;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

public abstract class AbstractDiagnosingFileMatcher<T, U extends AbstractDiagnosingFileMatcher<T, U>> extends AbstractDiagnosingMatcher<T> implements ApprovedFileMatcher<U> {

    public static final int NUM_OF_HASH_CHARS = 6;
    private TestMetaInformation testMetaInformation;
    protected String fileName;
    protected String testMethodName;
    protected String testClassName;
    protected String customFileName;
    protected String uniqueId;
    protected Path pathName;
    protected String testClassNameHash;

    protected Path fileNameWithPath;

    public AbstractDiagnosingFileMatcher(TestMetaInformation testMetaInformation) {
        this.testMetaInformation = testMetaInformation;
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
}
