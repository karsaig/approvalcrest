package com.github.karsaig.approvalcrest.testng;

/**
 * A block of code expected to throw, passed to {@code MatcherAssert.assertThrows} in the TestNG
 * integration.
 */
@FunctionalInterface
public interface Executable {

    /**
     * Runs the code under test.
     *
     * @throws Throwable whatever the code under test throws; the caller matches it against the
     *                   expected exception
     */
    void execute() throws Throwable;
}
