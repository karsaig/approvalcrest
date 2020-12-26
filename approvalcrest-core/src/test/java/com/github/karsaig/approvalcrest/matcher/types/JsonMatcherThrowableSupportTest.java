package com.github.karsaig.approvalcrest.matcher.types;

import java.io.IOError;
import java.security.InvalidKeyException;
import java.util.FormatterClosedException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;

public class JsonMatcherThrowableSupportTest extends AbstractFileMatcherTest {

    public static Object[][] exceptionHandlingCases() {
        return new Object[][]{

                {new RuntimeException("X differs:", new IllegalStateException("This is bad!")), "{\n" +
                        "  \"detailMessage\": \"X:\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"This is bad!\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.IllegalStateException\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.lang.RuntimeException\"\n" +
                        "}",
                        "Expected file 4ac405/11b2ef-approved.json\n" +
                                "detailMessage\n" +
                                "Expected: X:\n" +
                                "     got: X differs:\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad! differs")), "{\n" +
                        "  \"detailMessage\": \"X:\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"This is bad!\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.IllegalStateException\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.lang.RuntimeException\"\n" +
                        "}",
                        "Expected file 4ac405/11b2ef-approved.json\n" +
                                "cause.0x1.detailMessage\n" +
                                "Expected: This is bad!\n" +
                                "     got: This is bad! differs\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")), "{\n" +
                        "  \"detailMessage\": \"X: differs\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"This is bad!\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.IllegalStateException\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.lang.RuntimeException\"\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "detailMessage\n" +
                        "Expected: X: differs\n" +
                        "     got: X:\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")), "{\n" +
                        "  \"detailMessage\": \"X:\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"This is bad! differs\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.IllegalStateException\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.lang.RuntimeException\"\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "cause.0x1.detailMessage\n" +
                        "Expected: This is bad! differs\n" +
                        "     got: This is bad!\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")), "{\n" +
                        "  \"detailMessage\": \"X:\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"This is bad!\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.RuntimeException\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.lang.RuntimeException\"\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "cause.0x1.class\n" +
                        "Expected: java.lang.RuntimeException\n" +
                        "     got: java.lang.IllegalStateException\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")), "{\n" +
                        "  \"detailMessage\": \"X:\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"This is bad!\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.IllegalStateException\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.lang.IllegalStateException\"\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "class\n" +
                        "Expected: java.lang.IllegalStateException\n" +
                        "     got: java.lang.RuntimeException\n"},


                {new Throwable("Y:"), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"detailMessage\": \"Y:\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  }\n" +
                        "}", null},


                {new Throwable("Y:", new Throwable("Z:")), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"detailMessage\": \"Y:\",\n" +
                        "    \"cause\": \"0x2\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  },\n" +
                        "  \"0x2\": {\n" +
                        "    \"detailMessage\": \"Z:\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  }\n" +
                        "}", null},

                {new Throwable("Y:", new Throwable("Z:")), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"detailMessage\": \"Y differs:\",\n" +
                        "    \"cause\": \"0x2\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  },\n" +
                        "  \"0x2\": {\n" +
                        "    \"detailMessage\": \"Z:\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  }\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "0x1.detailMessage\n" +
                        "Expected: Y differs:\n" +
                        "     got: Y:\n"},
                {new Throwable("Y:", new Throwable("Z:")), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"detailMessage\": \"Y:\",\n" +
                        "    \"cause\": \"0x2\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  },\n" +
                        "  \"0x2\": {\n" +
                        "    \"detailMessage\": \"Z differs:\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  }\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "0x2.detailMessage\n" +
                        "Expected: Z differs:\n" +
                        "     got: Z:\n"},
                {new Throwable("Y:", new Throwable("Z:")), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"detailMessage\": \"Y:\",\n" +
                        "    \"cause\": \"0x2\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Differs\"\n" +
                        "  },\n" +
                        "  \"0x2\": {\n" +
                        "    \"detailMessage\": \"Z:\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  }\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "0x1.class\n" +
                        "Expected: java.lang.Differs\n" +
                        "     got: java.lang.Throwable\n"},
                {new Throwable("Y:", new Throwable("Z:")), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"detailMessage\": \"Y:\",\n" +
                        "    \"cause\": \"0x2\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Throwable\"\n" +
                        "  },\n" +
                        "  \"0x2\": {\n" +
                        "    \"detailMessage\": \"Z:\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Differs\"\n" +
                        "  }\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "0x2.class\n" +
                        "Expected: java.lang.Differs\n" +
                        "     got: java.lang.Throwable\n"},

                {new Error("Q:"), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"detailMessage\": \"Q:\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Error\"\n" +
                        "  }\n" +
                        "}", null},
                {new Error("Q:", new Error("W:")), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"detailMessage\": \"Q:\",\n" +
                        "    \"cause\": \"0x2\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Error\"\n" +
                        "  },\n" +
                        "  \"0x2\": {\n" +
                        "    \"detailMessage\": \"W:\",\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.Error\"\n" +
                        "  }\n" +
                        "}", null},


                {new IOError(new Throwable("Q:")), "{\n" +
                        "  \"detailMessage\": \"java.lang.Throwable: Q:\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"Q:\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.Throwable\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.io.IOError\"\n" +
                        "}", null},


                {new InvalidKeyException(), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.security.InvalidKeyException\"\n" +
                        "  }\n" +
                        "}", null},
                {new ArrayIndexOutOfBoundsException(), "{\n" +
                        "  \"0x1\": {\n" +
                        "    \"suppressedExceptions\": [],\n" +
                        "    \"class\": \"java.lang.ArrayIndexOutOfBoundsException\"\n" +
                        "  }\n" +
                        "}", null},


                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))),
                        "{\n" +
                                "  \"0x1\": {\n" +
                                "    \"detailMessage\": \"X:\",\n" +
                                "    \"cause\": {\n" +
                                "      \"detailMessage\": \"This is bad!\",\n" +
                                "      \"cause\": \"0x2\",\n" +
                                "      \"suppressedExceptions\": [],\n" +
                                "      \"class\": \"java.lang.IllegalStateException\"\n" +
                                "    },\n" +
                                "    \"suppressedExceptions\": [],\n" +
                                "    \"class\": \"java.lang.RuntimeException\"\n" +
                                "  },\n" +
                                "  \"0x2\": {\n" +
                                "    \"detailMessage\": \"Y:\",\n" +
                                "    \"cause\": \"0x3\",\n" +
                                "    \"suppressedExceptions\": [],\n" +
                                "    \"class\": \"java.lang.RuntimeException\"\n" +
                                "  },\n" +
                                "  \"0x3\": {\n" +
                                "    \"detailMessage\": \"Z:\",\n" +
                                "    \"suppressedExceptions\": [],\n" +
                                "    \"class\": \"java.lang.RuntimeException\"\n" +
                                "  }\n" +
                                "}", null},


                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new RuntimeException("Z:")))),
                        "{\n" +
                                "  \"0x1\": {\n" +
                                "    \"detailMessage\": \"X:\",\n" +
                                "    \"cause\": {\n" +
                                "      \"detailMessage\": \"This is bad!\",\n" +
                                "      \"cause\": \"0x2\",\n" +
                                "      \"suppressedExceptions\": [],\n" +
                                "      \"class\": \"java.lang.IllegalStateException\"\n" +
                                "    },\n" +
                                "    \"suppressedExceptions\": [],\n" +
                                "    \"class\": \"java.lang.RuntimeException\"\n" +
                                "  },\n" +
                                "  \"0x2\": {\n" +
                                "    \"detailMessage\": \"Y:\",\n" +
                                "    \"cause\": \"0x3\",\n" +
                                "    \"suppressedExceptions\": [],\n" +
                                "    \"class\": \"java.lang.RuntimeExceptionDiffers\"\n" +
                                "  },\n" +
                                "  \"0x3\": {\n" +
                                "    \"detailMessage\": \"Z:\",\n" +
                                "    \"suppressedExceptions\": [],\n" +
                                "    \"class\": \"java.lang.RuntimeException\"\n" +
                                "  }\n" +
                                "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "0x2.class\n" +
                        "Expected: java.lang.RuntimeExceptionDiffers\n" +
                        "     got: java.lang.RuntimeException\n"},

                {new RuntimeException("X:", new IllegalStateException("This is bad!", new RuntimeException("Y:", new FormatterClosedException()))),
                        "{\n" +
                                "  \"detailMessage\": \"X:\",\n" +
                                "  \"cause\": {\n" +
                                "    \"detailMessage\": \"This is bad!\",\n" +
                                "    \"cause\": {\n" +
                                "      \"detailMessage\": \"Y:\",\n" +
                                "      \"cause\": {\n" +
                                "        \"0x1\": {\n" +
                                "          \"suppressedExceptions\": [],\n" +
                                "          \"class\": \"java.util.FormatterClosedException\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"suppressedExceptions\": [],\n" +
                                "      \"class\": \"java.lang.RuntimeException\"\n" +
                                "    },\n" +
                                "    \"suppressedExceptions\": [],\n" +
                                "    \"class\": \"java.lang.IllegalStateException\"\n" +
                                "  },\n" +
                                "  \"suppressedExceptions\": [],\n" +
                                "  \"class\": \"java.lang.RuntimeException\"\n" +
                                "}", null},

        };
    }

    @ParameterizedTest
    @MethodSource("exceptionHandlingCases")
    void exceptionHandlingTest(Object input, String expected, String expectedExceptionMessage) {
        assertJsonMatcherWithDummyTestInfo(input, expected, expectedExceptionMessage);
    }
}
