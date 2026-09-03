package com.github.karsaig.approvalcrest.jdk26;

import com.github.karsaig.approvalcrest.jupiter.matcher.Matchers;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/**
 * Shared fixtures for the two executions of this module. Both serialise the same objects with the
 * sun-misc-unsafe-memory-access=deny flag, which is JDK 26's default; they differ only in whether
 * the approvalcrest agent is attached, and therefore in the JSON they expect.
 * <p>
 * The serialised form is read out of a mismatch description rather than by calling the serialiser,
 * so these tests go through the same public API a user does.
 * </p>
 */
final class Jdk26Fixtures {

    private Jdk26Fixtures() {
    }

    /**
     * A user's own exception. Its module is not locked, so nothing claims it on the way in - but it
     * carries {@code java.lang.Throwable}'s private fields, which is what needs java.lang opened.
     */
    public static class UserDefinedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final String ticket;

        public UserDefinedException(String message, String ticket) {
            super(message);
            this.ticket = ticket;
        }

        public String getTicket() {
            return ticket;
        }
    }

    static RuntimeException jdkException(String message) {
        RuntimeException thrown = new RuntimeException(message);
        thrown.setStackTrace(new StackTraceElement[0]);
        return thrown;
    }

    static UserDefinedException userException(String message, String ticket) {
        UserDefinedException thrown = new UserDefinedException(message, ticket);
        thrown.setStackTrace(new StackTraceElement[0]);
        return thrown;
    }

    /**
     * Compares two deliberately different objects and returns the diagnostic, which carries the
     * serialised form of both. The property names in it are what distinguish field-based output
     * from getter-based.
     */
    static String describeMismatch(Object expected, Object actual) {
        Matcher<Object> matcher = Matchers.sameBeanAs(expected);
        StringDescription description = new StringDescription();
        matcher.describeMismatch(actual, description);
        return description.toString();
    }

    /** True when the two objects compare equal through the public matcher. */
    static boolean matches(Object expected, Object actual) {
        return Matchers.sameBeanAs(expected).matches(actual);
    }
}
