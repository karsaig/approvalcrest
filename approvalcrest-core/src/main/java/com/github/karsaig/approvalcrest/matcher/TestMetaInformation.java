package com.github.karsaig.approvalcrest.matcher;

import java.nio.file.Path;

public interface TestMetaInformation {

    /**
     * Returns the absolute path of the test class in which the call was
     * originated from.
     *
     * @return test method name in String
     */
    Path getTestClassPath();

    /**
     * Returns the name of the test class file which the call was originated
     * from.
     *
     * @return test method's class name
     */
    String testClassName();

    /**
     * Returns the name of the test method, in which the call was originated
     * from.
     *
     * @return test method name in String
     */
    String testMethodName();

    /**
     * Returns the directory approved files are stored under when the
     * {@code useApprovedDirectory} property is enabled, instead of alongside the test class.
     *
     * @return the approved-file directory, relative to the project root
     */
    Path getApprovedDirectory();

    /**
     * Returns the directory that relative approved-file paths are resolved against, normally the
     * project root the tests were started from.
     *
     * @return the working directory, as an absolute path
     */
    Path workingDirectory();
}
