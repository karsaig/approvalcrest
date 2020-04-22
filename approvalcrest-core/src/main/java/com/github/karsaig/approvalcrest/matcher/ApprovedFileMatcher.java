package com.github.karsaig.approvalcrest.matcher;

import java.nio.file.Path;

public interface ApprovedFileMatcher<T extends ApprovedFileMatcher<T>> {

    /**
     * Concatenates a unique ID to the generated file name. Useful for test cases
     * containing multiple verifications.
     * <br></br>
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
     * Sets the file name to the given parameter.
     *
     * @param customFileName a {@link Path} object, which will be the base file name. The
     *                       approved/not-approved identifier and file extension will still be
     *                       concatenated to this.
     * @return current instance
     */
    T withFileName(Path customFileName);

    /**
     * Sets the file path to the given parameter.
     * Directory represented by the given path will be used instead of the default directory name (hash code).
     *
     * @param pathName a {@link String} object, which will be the file path.
     * @return current instance
     */
    T withPathName(String pathName);

    /**
     * Sets the file path to the given parameter.
     * Directory represented by the given path will be used instead of the default directory name (hash code).
     *
     * @param path a {@link Path} object, which will be the file path.
     * @return current instance
     */
    T withPath(Path path);
}
