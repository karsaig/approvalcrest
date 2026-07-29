/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest;

import org.hamcrest.Matcher;
import org.junit.AssumptionViolatedException;
import org.junit.ComparisonFailure;

import com.github.karsaig.approvalcrest.matcher.CustomisableMatcher;

import junit.framework.AssertionFailedError;

/**
 * Modified version of {@link org.hamcrest.MatcherAssert}. If the matcher doesn't match, determine if a {@link ComparisonFailure} should be
 * thrown. The exception is thrown instead of {@link AssertionError}, so that IDE like eclipse and IntelliJ can display a
 * pop-up window highlighting the String differences.
 */
public class MatcherAssert {

    private static final AssertImplProxy ASSERT_IMPL = new AssertImplProxy();

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
            if (comparisonDescription.isMachineReadable()) {
                throw new CleanMessageComparisonFailure(
                        message,
                        comparisonDescription.getExpected(),
                        comparisonDescription.getActual()
                );
            }
            throw new ComparisonFailure(
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
            rethrowIfNotTheExceptionUnderTest(throwable);
            assertThat(reason, throwable, matcher);
            return throwable;
        }
        throw new AssertionFailedError("Expected exception but no exception was thrown!");
    }

    /**
     * Rethrows anything that cannot be the exception under test, so it is not matched against the
     * expected one.
     *
     * <p>Two categories qualify. A {@link VirtualMachineError} means the JVM is in trouble, and
     * comparing it to a business exception buries that; it is rethrown as-is rather than wrapped,
     * which also avoids allocating while the heap may be exhausted. A {@link AssumptionViolatedException} is the
     * framework's signal to skip the test, so matching it would turn a skip into a pass or a
     * confusing failure.
     *
     * <p>{@code AssertionError} is deliberately NOT rethrown: asserting that a matcher fails is a
     * supported use of this method, and a matcher failure is an AssertionError.
     */
    private static void rethrowIfNotTheExceptionUnderTest(Throwable throwable) {
        if (throwable instanceof VirtualMachineError) {
            throw (VirtualMachineError) throwable;
        }
        if (throwable instanceof AssumptionViolatedException) {
            throw (AssumptionViolatedException) throwable;
        }
    }
}
