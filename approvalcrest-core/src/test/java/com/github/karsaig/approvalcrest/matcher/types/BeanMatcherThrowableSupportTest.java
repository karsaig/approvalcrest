package com.github.karsaig.approvalcrest.matcher.types;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOError;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.FormatterClosedException;
import java.util.function.Function;

import static java.util.function.Function.identity;

public class BeanMatcherThrowableSupportTest extends AbstractBeanMatcherTest {

    public static Object[][] exceptionHandlingCases() {
        return new Object[][]{

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))), null, identity()},


                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new IllegalStateException("Z:")))), "0x4.class\n" +
                        "Expected: java.lang.IllegalStateException\n" +
                        "     got: java.lang.RuntimeException\n", identity()},

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))), null, identity()},

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new IllegalStateException()))),
                        new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))), "cause.0x2.class\n" +
                        "Expected: java.util.FormatterClosedException\n" +
                        "     got: java.lang.IllegalStateException\n", identity()},

                {new RuntimeException("X differs:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        "detailMessage\n" +
                                "Expected: X:\n" +
                                "     got: X differs:\n", identity()},
                {new RuntimeException("X:", new IllegalStateException("This is bad! differs")),
                        new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        "cause.0x1.detailMessage\n" +
                                "Expected: This is bad!\n" +
                                "     got: This is bad! differs\n", identity()},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X: differs", new IllegalStateException("This is bad!")),
                        "detailMessage\n" +
                                "Expected: X: differs\n" +
                                "     got: X:\n", identity()},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new IllegalStateException("This is bad! differs")),
                        "cause.0x1.detailMessage\n" +
                                "Expected: This is bad! differs\n" +
                                "     got: This is bad!\n", identity()},


                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new RuntimeException("X:", new RuntimeException("This is bad!")), "0x2.class\n" +
                        "Expected: java.lang.RuntimeException\n" +
                        "     got: java.lang.IllegalStateException\n", identity()},


                {new RuntimeException("X:", new IllegalStateException("This is bad!")),
                        new IllegalStateException("X:", new IllegalStateException("This is bad!")), "\n" +
                        "Expected: \n" +
                        "     but: Actual type [class java.lang.RuntimeException] is not an instance of expected type [class java.lang.IllegalStateException]!\n" +
                        "This can be ignored with skipClassComparison or\n" +
                        "setting beanMatcherSkipClassComparison env variable to true", identity()},
                {new Throwable("Y:"), new Throwable("Y:"), null, identity()},

                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new Throwable("Z:")), null, identity()},

                {new Throwable("Y:", new Throwable("Z:")),
                        new Throwable("Y differs:", new Throwable("Z:")), "0x1.detailMessage\n" +
                        "Expected: Y differs:\n" +
                        "     got: Y:\n", identity()},
                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new Throwable("Z differs:")), "0x2.detailMessage\n" +
                        "Expected: Z differs:\n" +
                        "     got: Z:\n", identity()},
                {new Throwable("Y:", new Throwable("Z:")), new RuntimeException("Y:", new Throwable("Z:")), "\n" +
                        "Expected: \n" +
                        "     but: Actual type [class java.lang.Throwable] is not an instance of expected type [class java.lang.RuntimeException]!\n" +
                        "This can be ignored with skipClassComparison or\n" +
                        "setting beanMatcherSkipClassComparison env variable to true", identity()},
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
                        "Unexpected: 0x2\n", (Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>>) DiagnosingCustomisableMatcher::skipClassComparison},
                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new RuntimeException("Z:")), "0x2.class\n" +
                        "Expected: java.lang.RuntimeException\n" +
                        "     got: java.lang.Throwable\n", identity()},

                {new Throwable("Y:", new Throwable("Z:")), new Throwable("Y:", new RuntimeException("Z:")), "0x2.class\n" +
                        "Expected: java.lang.RuntimeException\n" +
                        "     got: java.lang.Throwable\n", (Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>>) DiagnosingCustomisableMatcher::skipClassComparison},

                {new Error("Q:"), new Error("Q:"), null, identity()},
                {new Error("Q:", new Error("W:")), new Error("Q:", new Error("W:")), null, identity()},


                {new IOError(new Throwable("Q:")), new IOError(new Throwable("Q:")), null, identity()},


                {new InvalidKeyException(), new InvalidKeyException(), null, identity()},
                {new ArrayIndexOutOfBoundsException(), new ArrayIndexOutOfBoundsException(), null, identity()},


                {new RuntimeException("Runtime message", new GeneralSecurityException("message")), new RuntimeException("Runtime message", new GeneralSecurityException("message")), null, identity()},
        };
    }

    @ParameterizedTest
    @MethodSource("exceptionHandlingCases")
    void exceptionHandlingTest(Object input, Object expected,String expectedExceptionMessage, Function<DiagnosingCustomisableMatcher<Object>, DiagnosingCustomisableMatcher<Object>> configurator) {
        assertDiagnosingErrorMatcher(input, expected, configurator,expectedExceptionMessage);
    }
}
