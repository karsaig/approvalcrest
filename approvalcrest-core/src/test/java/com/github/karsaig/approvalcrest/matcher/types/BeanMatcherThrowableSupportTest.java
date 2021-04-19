package com.github.karsaig.approvalcrest.matcher.types;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOError;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.FormatterClosedException;

public class BeanMatcherThrowableSupportTest extends AbstractBeanMatcherTest {

    public static Object[][] exceptionHandlingCases() {
        return new Object[][]{

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))), null},


                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new IllegalStateException("Z:")))), "0x4.class\n" +
                        "Expected: java.lang.IllegalStateException\n" +
                        "     got: java.lang.RuntimeException\n"},

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))), null},

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new IllegalStateException()))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))), "cause.0x2.class\n" +
                        "Expected: java.util.FormatterClosedException\n" +
                        "     got: java.lang.IllegalStateException\n"},

                {new RuntimeException("X differs:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        "detailMessage\n" +
                                "Expected: X:\n" +
                                "     got: X differs:\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad! differs")),
                        new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        "cause.0x1.detailMessage\n" +
                                "Expected: This is bad!\n" +
                                "     got: This is bad! differs\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X: differs", new IllegalStateException("This is bad!")),
                        "detailMessage\n" +
                                "Expected: X: differs\n" +
                                "     got: X:\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new IllegalStateException("This is bad! differs")),
                        "cause.0x1.detailMessage\n" +
                                "Expected: This is bad! differs\n" +
                                "     got: This is bad!\n"},


                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new RuntimeException("This is bad!")), "0x2.class\n" +
                        "Expected: java.lang.RuntimeException\n" +
                        "     got: java.lang.IllegalStateException\n"},


                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new IllegalStateException("X:", new IllegalStateException("This is bad!")), "\n" +
                        "Expected: 0x1\n" +
                        "     but none found\n" +
                        " ; \n" +
                        "Expected: 0x2\n" +
                        "     but none found\n" +
                        " ; \n" +
                        "Unexpected: cause\n" +
                        " ; \n" +
                        "Unexpected: class\n" +
                        " ; \n" +
                        "Unexpected: detailMessage\n" +
                        " ; \n" +
                        "Unexpected: suppressedExceptions\n"},
                {new Throwable("Y:"), new Throwable("Y:"), null},

                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new Throwable("Z:")), null},

                {new Throwable("Y:", new Throwable("Z:")),
                        new Throwable("Y differs:", new Throwable("Z:")), "0x1.detailMessage\n" +
                        "Expected: Y differs:\n" +
                        "     got: Y:\n"},
                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new Throwable("Z differs:")), "0x2.detailMessage\n" +
                        "Expected: Z differs:\n" +
                        "     got: Z:\n"},
                {new Throwable("Y:", new Throwable("Z:")), new RuntimeException("Y:", new Throwable("Z:")), "\n" +
                        "Expected: cause\n" +
                        "     but none found\n" +
                        " ; \n" +
                        "Expected: class\n" +
                        "     but none found\n" +
                        " ; \n" +
                        "Expected: detailMessage\n" +
                        "     but none found\n" +
                        " ; \n" +
                        "Expected: suppressedExceptions\n" +
                        "     but none found\n" +
                        " ; \n" +
                        "Unexpected: 0x1\n" +
                        " ; \n" +
                        "Unexpected: 0x2\n"},
                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new RuntimeException("Z:")), "0x2.class\n" +
                        "Expected: java.lang.RuntimeException\n" +
                        "     got: java.lang.Throwable\n"},

                {new Error("Q:"), new Error("Q:"), null},
                {new Error("Q:", new Error("W:")), new Error("Q:", new Error("W:")), null},


                {new IOError(new Throwable("Q:")), new IOError(new Throwable("Q:")), null},


                {new InvalidKeyException(), new InvalidKeyException(), null},
                {new ArrayIndexOutOfBoundsException(), new ArrayIndexOutOfBoundsException(), null},


                {new RuntimeException("Runtime message", new GeneralSecurityException("message")), new RuntimeException("Runtime message", new GeneralSecurityException("message")), null},
        };
    }

    @ParameterizedTest
    @MethodSource("exceptionHandlingCases")
    void exceptionHandlingTest(Object input, Object expected, String expectedExceptionMessage) {
        assertDiagnosingMatcher(input, expected, expectedExceptionMessage);
    }
}
