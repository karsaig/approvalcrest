package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.AssertUtil.fail;
import static com.github.karsaig.approvalcrest.matcher.FileStoreMatcherUtils.SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;

import com.github.karsaig.approvalcrest.ComparisonDescription;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

/**
 * <p>
 * Matcher for asserting expected {@link String}s. Searches for an approved file
 * in the same directory as the test file:
 * <ul>
 * <li>If found, the matcher will assert the contents of the file to the actual
 * {@link String}.</li>
 * <li>If not found, a non-approved file is created, that must be verified and
 * renamed to "*-approved.content" by the developer.</li>
 * </ul>
 * The files and directories are hashed with SHA-1 algorithm by default to avoid
 * too long file and path names. These are generated in the following way:
 * <ul>
 * <li>the directory name is the first {@value #NUM_OF_HASH_CHARS} characters of
 * the hashed <b>class name</b>.</li>
 * <li>the file name is the first {@value #NUM_OF_HASH_CHARS} characters of the
 * hashed <b>test method name</b>.</li>
 * </ul>
 * <p>
 * This default behavior can be overridden by using the
 * {@link #withFileName(String)} for custom file name and
 * {@link #withPathName(String)} for custom path.
 * </p>
 *
 * @param <T> Only {@link String} is supported at the moment.
 */
public class ContentMatcher<T> extends DiagnosingMatcher<T> implements ApprovedFileMatcher<ContentMatcher<T>> {

    private static final int NUM_OF_HASH_CHARS = 6;
    private static final String UPDATE_IN_PLACE_NAME = "jsonMatcherUpdateInPlace";
    private static final Pattern WINDOWS_NEWLINE_PATTERN = Pattern.compile("\r\n");

    private Path pathName;
    private String fileName;
    private String customFileName;
    private Path fileNameWithPath;
    private String uniqueId;
    private TestMetaInformation testMetaInformation;
    private String testClassName;
    private String testMethodName;
    private String testClassNameHash;

    private FileStoreMatcherUtils fileStoreMatcherUtils = new FileStoreMatcherUtils(".content");

    private String expectedContent;

    public ContentMatcher(TestMetaInformation testMetaInformation) {
        this.testMetaInformation = testMetaInformation;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expectedContent);
    }

    @Override
    protected boolean matches(Object actual, Description mismatchDescription) {
        boolean matches = false;
        init();
        createNotApprovedFileIfNotExists(actual);
        initExpectedFromFile();
        String actualString = String.class.cast(actual);


        String expectedNormalited = WINDOWS_NEWLINE_PATTERN.matcher(expectedContent).replaceAll("\n");
        String actualNormalized = WINDOWS_NEWLINE_PATTERN.matcher(actualString).replaceAll("\n");
        if (expectedNormalited.equals(actualNormalized)) {
            matches = true;
        } else {
            if ("true".equals(System.getProperty(UPDATE_IN_PLACE_NAME))) {
                overwriteApprovedFile(actualNormalized);
                matches = true;
            } else {
                matches = appendMismatchDescription(mismatchDescription, expectedContent, actualString,
                        getAssertMessage("Content does not match!"));
            }
        }
        return matches;
    }

    @Override
    public ContentMatcher<T> withUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    @Override
    public ContentMatcher<T> withFileName(String customFileName) {
        this.customFileName = customFileName;
        return this;
    }

    @Override
    public ContentMatcher<T> withPathName(String pathName) {
        this.pathName = Paths.get(pathName);
        return this;
    }

    private void init() {
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

    private String hashFileName(String fileName) {
        return Hashing.sha1().hashString(fileName, Charsets.UTF_8).toString().substring(0, NUM_OF_HASH_CHARS);
    }

    private void createNotApprovedFileIfNotExists(Object toApprove) {
        Path approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);

        if (Files.notExists(approvedFile)) {
            try {
                String approvedFileName = approvedFile.getFileName().toString();
                if (!String.class.isInstance(toApprove)) {
                    throw new IllegalArgumentException("Only String content matcher is supported!");
                }
                String content = String.class.cast(toApprove);
                String createdFileName = fileStoreMatcherUtils.createNotApproved(fileNameWithPath, content,
                        getCommentLine());
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

    private void overwriteApprovedFile(Object actual) {
        Path approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);
        if (Files.exists(approvedFile)) {
            try {
                String content = String.class.cast(actual);
                fileStoreMatcherUtils.overwriteApprovedFile(fileNameWithPath, content, getCommentLine());
            } catch (IOException e) {
                throw new IllegalStateException(
                        String.format("Exception while overwriting approved file %s", actual.toString()), e);
            }
        } else {
            throw new IllegalStateException(
                    "Approved file " + fileNameWithPath + " must exist in order to overwrite it! ");
        }
    }

    private String getCommentLine() {
        return testClassName + "." + testMethodName;
    }

    private void initExpectedFromFile() {
        Path approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);
        try {
            expectedContent = fileStoreMatcherUtils.readFile(approvedFile);
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Exception while initializing expected from file: %s", approvedFile.toString()), e);
        }
    }

    private boolean appendMismatchDescription(Description mismatchDescription, String expected, String actual, String message) {
        if (mismatchDescription instanceof ComparisonDescription) {
            ComparisonDescription shazamMismatchDescription = (ComparisonDescription) mismatchDescription;
            shazamMismatchDescription.setComparisonFailure(true);
            shazamMismatchDescription.setExpected(expected);
            shazamMismatchDescription.setActual(actual);
            shazamMismatchDescription.setDifferencesMessage(message);
        }
        mismatchDescription.appendText(message);
        return false;
    }

    private String getAssertMessage(String message) {
        String result;
        if (testClassNameHash == null) {
            result = "Expected file " + fileNameWithPath + "\n" + message;
        } else {
            result = "Expected file " + testClassNameHash + File.separator
                    + fileStoreMatcherUtils.getFullFileName(Paths.get(fileName), true) + "\n" + message;
        }
        return result;
    }
}
