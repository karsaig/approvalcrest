package com.github.karsaig.approvalcrest.matcher.file;

import java.nio.file.Path;

public interface ApprovedFileMatcher<T extends ApprovedFileMatcher<T>> {

    /**
     * Concatenates a unique ID to the generated file name. Useful for test cases
     * containing multiple verifications.
     * <br>
     * Eg: With <b>"id1"</b> as input hash-approved.ext becomes hash-<b>id1</b>-approved.ext
     *
     * @param uniqueId a {@link String} object, that uniquely identifies the file.
     * @return current instance
     */
    T withUniqueId(String uniqueId);

    /**
     * Sets the file name to the given parameter.
     *
     * @param customFileName a {@link String} object, which will be the base file name. The
     *                       approved/not-approved identifier and file extension will still be
     *                       concatenated to this.
     * @return current instance
     */
    T withFileName(String customFileName);

    /**
     * Sets the file path to the given parameter.
     * Directory represented by the given path will be used instead of the default directory name (hash code).
     *
     * Relative to test class' directory by default or to relative path if given.
     *
     * @param pathName a {@link String} object, which will be the file path.
     * @return current instance
     */
    T withPathName(String pathName);

    /**
     * Sets the file path to the given parameter.
     * Directory represented by the given path will be used instead of the default path (test class' directory)
     *
     * Relative to working directory by default.
     *
     *
     * @param pathName a {@link String} object, which will be the file path.
     * @return current instance
     */
    T withRelativePathName(String pathName);

    /**
     * Sets the file path to the given parameter.
     * Directory represented by the given path will be used instead of the default directory name (hash code).
     *
     * Relative to test class' directory by default or to relative path if given.
     *
     * @param path a {@link Path} object, which will be the file path.
     * @return current instance
     */
    T withPath(Path path);
}
