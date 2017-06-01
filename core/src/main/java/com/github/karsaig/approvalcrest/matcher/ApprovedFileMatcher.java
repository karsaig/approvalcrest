package com.github.karsaig.approvalcrest.matcher;

public interface ApprovedFileMatcher<T extends ApprovedFileMatcher<T>> {
	
	/**
	 * <pre>
	 * Concatenates a unique ID to the generated file name. Useful for test cases
	 * containing multiple verifications.
	 * Eg: With <b>"id1"</b> as input hash-approved.ext becomes hash-<b>id1</b>-approved.ext
	 * </pre>
	 * @param uniqueId
	 *          a {@link String} object, that uniquely identifies the file.
	 * @return current instance
	 */
	T withUniqueId(final String uniqueId);
	
	/**
	 * Sets the file name to the given parameter.
	 *
	 * @param customFileName
	 *          a {@link String} object, which will be the base file name. The
	 *          approved/not-approved identifier and file extension will still be
	 *          concatenated to this.
	 * @return current instance
	 */
	T withFileName(final String customFileName);
	
	/**
	 * Sets the file path to the given parameter.
	 * Directory represented by the given path will be used instead of the default directory name (hash code).
	 *
	 * @param pathName
	 *          a {@link String} object, which will be the file path.
	 * @return current instance
	 */
	T withPathName(final String pathName);
}
