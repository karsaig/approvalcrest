package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.matcher.FileStoreMatcherUtils.SEPARATOR;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;

import com.github.karsaig.approvalcrest.ComparisonDescription;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

/**
 * <p>
 * Matcher for asserting expected {@link String}s. Searches for an approved file in
 * the same directory as the test file:
 * <ul>
 * 		<li>If found, the matcher will assert the contents of the file to the actual {@link String}.</li>
 * 		<li>If not found, a non-approved file is created, that must be
 * verified and renamed to "*-approved.content" by the developer. </li>
 * </ul>
 * The files and directories are hashed with SHA-1 algorithm by default to avoid too long file
 * and path names.
 * These are generated in the following way:
 * <ul>
 *   <li> the directory name is the first {@value #NUM_OF_HASH_CHARS} characters of the hashed <b>class name</b>. </li>
 *   <li> the file name is the first {@value #NUM_OF_HASH_CHARS} characters of the hashed <b>test method name</b>. </li>
 * </ul>
 *
 * This default behavior can be overridden by using the {@link #withFileName(String)} for
 * custom file name and {@link #withPathName(String)} for custom path.
 * </p>
 * 
 * @param <T> Only {@link String} is supported at the moment.
 */
public class ContentMatcher<T> extends DiagnosingMatcher<T> implements ApprovedFileMatcher<ContentMatcher<T>> {

	private static final int NUM_OF_HASH_CHARS = 6;

	private String pathName;
	private String fileName;
	private String customFileName;
	private String fileNameWithPath;
	private String uniqueId;
	private String testClassName;
	private String testMethodName;
	private String testClassNameHash;

	private FileStoreMatcherUtils fileStoreMatcherUtils = new FileStoreMatcherUtils(".content");

	private String expectedContent;

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

		if (expectedContent.equals(actualString)) {
			matches = true;
		} else {
			matches = appendMismatchDescription(mismatchDescription, expectedContent, actualString,
					getAssertMessage("Content does not match!"));
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
		this.pathName = pathName;
		return this;
	}

	private void init() {
		testMethodName = fileStoreMatcherUtils.getCallerTestMethodName();
		testClassName = fileStoreMatcherUtils.getCallerTestClassName();

		if (customFileName == null || customFileName.trim().isEmpty()) {
			fileName = hashFileName(testMethodName);
		} else {
			fileName = customFileName;
		}
		if (uniqueId != null) {
			fileName += SEPARATOR + uniqueId;
		}
		if (pathName == null || pathName.trim().isEmpty()) {
			testClassNameHash = hashFileName(testClassName);
			pathName = fileStoreMatcherUtils.getCallerTestClassPath() + File.separator + testClassNameHash;
		}

		fileNameWithPath = pathName + File.separator + fileName;
	}

	private String hashFileName(final String fileName) {
		return Hashing.sha1().hashString(fileName, Charsets.UTF_8).toString().substring(0, NUM_OF_HASH_CHARS);
	}

	private void createNotApprovedFileIfNotExists(final Object toApprove) {
		File approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);

		if (!approvedFile.exists()) {
			try {
				String approvedFileName = approvedFile.getName();
				if (!String.class.isInstance(toApprove)) {
					throw new IllegalArgumentException("Only String content matcher is supported!");
				}
				String content = String.class.cast(toApprove);
				String createdFileName = fileStoreMatcherUtils.createNotApproved(fileNameWithPath, content,
						testClassName + "." + testMethodName);
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

	private void initExpectedFromFile() {
		File approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);
		try {
			expectedContent = fileStoreMatcherUtils.readFile(approvedFile);
		} catch (IOException e) {
			throw new IllegalStateException(
					String.format("Exception while initializing expected from file: %s", approvedFile.toString()), e);
		}
	}

	private boolean appendMismatchDescription(final Description mismatchDescription, final String expected,
			final String actual, final String message) {
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

	private String getAssertMessage(final String message) {
		String result;
		if (testClassNameHash == null) {
			result = "Expected file " + fileNameWithPath + "\n" + message;
		} else {
			result = "Expected file " + testClassNameHash + File.separator
					+ fileStoreMatcherUtils.getFullFileName(fileName, true) + "\n" + message;
		}
		return result;
	}
}
