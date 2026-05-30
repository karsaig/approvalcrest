package com.github.karsaig.approvalcrest.testng;

import org.hamcrest.Matcher;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.matcher.CustomisableMatcher;

/**
 * Modified version of {@link org.hamcrest.MatcherAssert}. If the matcher doesn't match, determine if a {@link AssertionFailedError} should be
 * thrown. The exception is thrown instead of {@link AssertionError}, so that IDE like eclipse and IntelliJ can display a
 * pop-up window highlighting the String differences.
 */
public class MatcherAssert {

    private static final AssertImpl ASSERT_IMPL = new AssertImpl();

    /**
     * @param actual  the object that will be matched against the matcher
     * @param matcher defines the condition the object have to fulfill in order to match
     * @param <T>     type of actual object
     * @see org.hamcrest.MatcherAssert#assertThat(Object, Matcher)
     */
    public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
        assertThat("", actual, matcher);
    }

    /**
     * Checks if the object matches the condition defined by the matcher provided.
     *
     * @param reason  describes the assertion
     * @param actual  the object that will be matched against the matcher
     * @param matcher defines the condition the object have to fulfill in order to match
     * @param <T>     type of actual object
     */
    public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
        ASSERT_IMPL.assertThat(reason, actual, matcher, (message, comparisonDescription) -> {
            throw new AssertionFailedError(
                    message,
                    comparisonDescription.getExpected(),
                    comparisonDescription.getActual()
            );
        });
    }

    /**
     * Asserts that {@code Executable} throws an exception when executed.
     * If it does, the exception object is asserted with {@link MatcherAssert#assertThat(String, Object, Matcher)} then returned.
     * If it does not throw an exception, an {@link AssertionFailedError} is thrown.
     *
     * @param matcher    defines the condition the exception have to fulfill in order to match
     * @param executable the executable which supposed to throw the exception
     * @return the exception thrown
     */
    @SuppressWarnings({"ProhibitedExceptionCaught", "ThrowInsideCatchBlockWhichIgnoresCaughtException", "rawtypes", "unchecked", "ThrowableNotThrown"})
    public static Throwable assertThrows(CustomisableMatcher matcher, Executable executable) {
        return assertThrows(null, matcher, executable);
    }

    /**
     * Asserts that {@code Executable} throws an exception when executed.
     * If it does, the exception object is asserted with {@link MatcherAssert#assertThat(String, Object, Matcher)} then returned.
     * If it does not throw an exception, an {@link AssertionFailedError} is thrown.
     *
     * @param reason     describes the assertion
     * @param matcher    defines the condition the exception have to fulfill in order to match
     * @param executable the executable which supposed to throw the exception
     * @return the exception thrown
     */
    @SuppressWarnings({"ProhibitedExceptionCaught", "ThrowInsideCatchBlockWhichIgnoresCaughtException", "rawtypes", "unchecked", "ThrowableNotThrown"})
    public static Throwable assertThrows(String reason, CustomisableMatcher matcher, Executable executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            if (throwable instanceof OutOfMemoryError) {
                throw new RuntimeException(throwable);
            }
            assertThat(reason, throwable, matcher);
            return throwable;
        }
        throw new AssertionFailedError("Expected exception but no exception was thrown!");
    }
}
