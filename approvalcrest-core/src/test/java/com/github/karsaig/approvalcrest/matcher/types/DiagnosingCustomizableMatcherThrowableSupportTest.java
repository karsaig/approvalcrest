package com.github.karsaig.approvalcrest.matcher.types;

import java.io.IOError;
import java.security.InvalidKeyException;
import java.util.FormatterClosedException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;

public class DiagnosingCustomizableMatcherThrowableSupportTest extends AbstractFileMatcherTest {

    public static Object[][] exceptionHandlingCases() {
        return new Object[][]{

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))), true},


                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new IllegalStateException("Z:")))), false},

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))), true},

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new IllegalStateException()))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))), false},

                {new RuntimeException("X differs:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        false},
                {new RuntimeException("X:", new IllegalStateException("This is bad! differs")),
                        new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        false},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X: differs", new IllegalStateException("This is bad!")),
                        false},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new IllegalStateException("This is bad! differs")),
                        false},


                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new RuntimeException("This is bad!")), false},


                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new IllegalStateException("X:", new IllegalStateException("This is bad!")), false},
                {new Throwable("Y:"), new Throwable("Y:"), true},

                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new Throwable("Z:")), true},

                {new Throwable("Y:", new Throwable("Z:")),
                        new Throwable("Y differs:", new Throwable("Z:")), false},
                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new Throwable("Z differs:")), false},
                {new Throwable("Y:", new Throwable("Z:")), new RuntimeException("Y:", new Throwable("Z:")), false},
                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new RuntimeException("Z:")), false},

                {new Error("Q:"), new Error("Q:"), true},
                {new Error("Q:", new Error("W:")), new Error("Q:", new Error("W:")), true},


                {new IOError(new Throwable("Q:")), new IOError(new Throwable("Q:")), true},


                {new InvalidKeyException(), new InvalidKeyException(), true},
                {new ArrayIndexOutOfBoundsException(), new ArrayIndexOutOfBoundsException(), true},
        };
    }

    @ParameterizedTest
    @MethodSource("exceptionHandlingCases")
    void exceptionHandlingTest(Object input, Object expected, boolean result) {
        MatcherAssert.assertThat(new DiagnosingCustomisableMatcher<>(expected).matches(input), Matchers.is(result));
    }
}
