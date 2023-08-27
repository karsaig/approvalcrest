package com.github.karsaig.approvalcrest.matcher.file;

import static com.github.karsaig.approvalcrest.AssertUtil.fail;
import static com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils.SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.AbstractDiagnosingMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMetaInformation;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.hash.Hashing;

public abstract class AbstractDiagnosingFileMatcher<T, U extends AbstractDiagnosingFileMatcher<T, U>> extends AbstractDiagnosingMatcher<T> implements ApprovedFileMatcher<U> {

    public static final int NUM_OF_HASH_CHARS = 6;
    protected final FileStoreMatcherUtils fileStoreMatcherUtils;
    protected final FileMatcherConfig fileMatcherConfig;
    private final TestMetaInformation testMetaInformation;
    protected String fileName;
    protected String testMethodName;
    protected String testClassName;
    protected String customFileName;
    protected String uniqueId;
    protected Path pathName;
    protected String relativePathName;
    protected String testClassNameHash;

    protected Path fileNameWithPath;

    public AbstractDiagnosingFileMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig, FileStoreMatcherUtils fileStoreMatcherUtils) {
        this.testMetaInformation = Objects.requireNonNull(testMetaInformation, "TestMetaInformation must not be null!");
        this.fileStoreMatcherUtils = Objects.requireNonNull(fileStoreMatcherUtils, "FileStoreMatcherUtils must not be null!");
        this.fileMatcherConfig = Objects.requireNonNull(fileMatcherConfig, "FileMatcherConfig must not be null!");
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
            if(relativePathName == null) {
                testClassNameHash = hashFileName(testClassName);
                pathName = testMetaInformation.getTestClassPath().resolve(testClassNameHash);
            } else {
                pathName = testMetaInformation.getTestClassPath().resolve(relativePathName);
            }
        }

        fileNameWithPath = pathName.resolve(fileName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public U withUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return (U) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public U withFileName(String customFileName) {
        this.customFileName = customFileName;
        return (U) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public U withPathName(String pathName) {
        this.pathName = Paths.get(pathName);
        return (U) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public U withRelativePathName(String relativePathName) {
        this.relativePathName = relativePathName;
        return (U) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public U withPath(Path path) {
        this.pathName = path;
        return (U) this;
    }

    @SuppressWarnings("deprecation")
    private String hashFileName(String fileName) {
        return Hashing.sha1().hashString(fileName, Charsets.UTF_8).toString().substring(0, NUM_OF_HASH_CHARS);
    }

    protected String getAssertMessage(FileStoreMatcherUtils fileStoreMatcherUtils, String message) {
        String result;
        if (testClassNameHash == null) {
            result = "Expected file " + fileNameWithPath.toString().replace(File.separator, "/") + "\n" + message;
        } else {
            result = "Expected file " + testClassNameHash + "/"
                    + fileStoreMatcherUtils.getFullFileName(Paths.get(fileName), true).toString().replace(File.separator, "/") + "\n" + message;
        }
        return result;
    }

    protected String getAssertMessage(FileStoreMatcherUtils fileStoreMatcherUtils, Throwable t) {
        return getAssertMessage(fileStoreMatcherUtils, t.getMessage());
    }

    /**
     * Creates a file suffixed with -not-approved for the developer to verify, and rename.
     *
     * @param toApprove ?
     * @param content The content to be added to the -not-approved file.
     * @return true if the not-approved file was created, false otherwise.
     */
    protected boolean createNotApprovedFileIfNotExists(Object toApprove, Supplier<String> content) {
        Path approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);

        if (Files.notExists(approvedFile)) {
            try {
                String approvedFileName = approvedFile.getFileName().toString();
                String createdFileName = fileStoreMatcherUtils.createNotApproved(fileNameWithPath, content.get(), getCommentLine());
                if (!fileMatcherConfig.isPassOnCreateEnabled()) {
                    String message;
                    if (testClassNameHash == null) {
                        message = "Not approved file created: '" + createdFileName
                                + "';\n please verify its contents and rename it to '" + approvedFileName + "'.";
                    } else {
                        message = "Not approved file created: '" + testClassNameHash + File.separator + createdFileName
                                + "';\n please verify its contents and rename it to '" + approvedFileName + "'.";
                    }
                    fail(message);
                }
                return true;
            } catch (IOException e) {
                throw new IllegalStateException(
                        String.format("Exception while creating not approved file %s", toApprove.toString()), e);
            }
        }
        return false;
    }

    protected String getCommentLine() {
        return testClassName + "." + testMethodName;
    }

    protected void overwriteApprovedFile(Object actual, Supplier<String> content) {
        Path approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);
        if (Files.exists(approvedFile)) {
            try {
                fileStoreMatcherUtils.overwriteApprovedFile(fileNameWithPath, content.get(), getCommentLine());
            } catch (IOException e) {
                throw new IllegalStateException(
                        String.format("Exception while overwriting approved file %s", actual.toString()), e);
            }
        } else {
            throw new IllegalStateException("Approved file " + fileNameWithPath + " must exist in order to overwrite it! ");
        }
    }

    protected <V> V getExpectedFromFile(Function<String, V> processorAfterRead) {
        Path approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);
        try {
            String fileContent = fileStoreMatcherUtils.readFile(approvedFile);
            return processorAfterRead.apply(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Exception while initializing expected from file: %s", approvedFile.toString()), e);
        }
    }
}
