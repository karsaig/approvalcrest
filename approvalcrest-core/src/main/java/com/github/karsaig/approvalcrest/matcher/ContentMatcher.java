package com.github.karsaig.approvalcrest.matcher;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.hamcrest.Description;

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

    private static final FileStoreMatcherUtils fileStoreMatcherUtils = new FileStoreMatcherUtils(".content");

    private String expectedContent;

    public ContentMatcher(TestMetaInformation testMetaInformation) {
        super(testMetaInformation, fileStoreMatcherUtils);
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
            if (isOverwriteInPlaceEnabled()) {
                overwriteApprovedFile(actualNormalized);
                matches = true;
            } else {
                matches = appendMismatchDescription(mismatchDescription, expectedContent, actualString,
                        getAssertMessage(fileStoreMatcherUtils, "Content does not match!"));
            }
        }
        return matches;
    }

    private void createNotApprovedFileIfNotExists(Object toApprove) {
        createNotApprovedFileIfNotExists(toApprove, () -> {
            if (!String.class.isInstance(toApprove)) {
                throw new IllegalArgumentException("Only String content matcher is supported!");
            }
            return String.class.cast(toApprove);
        });
    }

    private void overwriteApprovedFile(Object actual) {
        overwriteApprovedFile(actual, () -> String.class.cast(actual));
    }

    private void initExpectedFromFile() {
        expectedContent = getExpectedFromFile(Function.identity());
    }
}
