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

    Path getApprovedDirectory();

    Path workingDirectory();
}
