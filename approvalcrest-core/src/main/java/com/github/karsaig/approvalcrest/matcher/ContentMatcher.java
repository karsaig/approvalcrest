package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.file.AbstractDiagnosingFileMatcher;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;
import org.hamcrest.Description;

import java.util.function.Function;
import java.util.regex.Pattern;

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
public class ContentMatcher<T> extends AbstractDiagnosingFileMatcher<T, ContentMatcher<T>> {

    private static final Pattern WINDOWS_NEWLINE_PATTERN = Pattern.compile("\r\n");

    private String expectedContent;

    public ContentMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        super(testMetaInformation, fileMatcherConfig, new FileStoreMatcherUtils("content", fileMatcherConfig));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expectedContent);
    }

    @Override
    protected boolean matches(Object actual, Description mismatchDescription) {
        if (!String.class.isInstance(actual)) {
            throw new IllegalArgumentException("Only String content matcher is supported!");
        }
        boolean matches = false;
        init();
        String actualString = String.class.cast(actual);
        if (createNotApprovedFileIfNotExists(actualString)
                && fileMatcherConfig.isPassOnCreateEnabled()) {
            return true;
        }
        initExpectedFromFile();


        String actualNormalized = normalize(actualString);
        if (expectedContent.equals(actualNormalized)) {
            matches = true;
        } else {
            if (fileMatcherConfig.isOverwriteInPlaceEnabled()) {
                overwriteApprovedFile(actualNormalized);
                matches = true;
            } else {
                matches = appendMismatchDescription(mismatchDescription, expectedContent, actualNormalized,
                        getAssertMessage(fileStoreMatcherUtils, "Content does not match!"));
            }
        }
        return matches;
    }

    private String normalize(String input) {
        return input == null ? null : WINDOWS_NEWLINE_PATTERN.matcher(input).replaceAll("\n");
    }

    private boolean createNotApprovedFileIfNotExists(String toApprove) {
        return createNotApprovedFileIfNotExists(toApprove, () -> normalize(toApprove));
    }

    private void overwriteApprovedFile(Object actual) {
        overwriteApprovedFile(actual, () -> String.class.cast(actual));
    }

    private void initExpectedFromFile() {
        expectedContent = normalize(getExpectedFromFile(Function.identity()));
    }

    @Override
    public String toString() {
        if (fileNameWithPath == null) {
            return "ContentMatcher";
        }
        return "ContentMatcher for " + fileStoreMatcherUtils.getApproved(fileNameWithPath,filenameWithRelativePath).getFileNameWithRelativePath();
    }
}
