package com.github.karsaig.approvalcrest.matcher;


import static com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean.Builder.circularReferenceBean;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.testdata.ClosableFields;
import com.github.karsaig.approvalcrest.testdata.IterableFields;
import com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean;
import com.github.karsaig.approvalcrest.testdata.cyclic.Element;
import com.github.karsaig.approvalcrest.testdata.cyclic.Four;
import com.github.karsaig.approvalcrest.testdata.cyclic.One;
import com.github.karsaig.approvalcrest.testdata.cyclic.Two;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonMatcherCircularReferenceTest extends AbstractFileMatcherTest {

    @Test
    public void doesNothingWhenAutoDetectCircularReferenceIsCalled() {
        CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();
        String approvedFileContent = "/*com.github.karsaig.approvalcrest.matcher.JsonMatcherCircularReferenceTest.doesNothingWhenAutoDetectCircularReferenceIsCalled*/\n" +
                "{\n" +
                "  \"parent\": {\n" +
                "    \"0x1\": {\n" +
                "      \"children\": [\n" +
                "        {\n" +
                "          \"parent\": \"0x1\",\n" +
                "          \"childAttribute\": \"child1\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"parent\": \"0x1\",\n" +
                "          \"childAttribute\": \"child2\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"parentAttribute\": \"parent\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        inMemoryFsWithDummyTestInfo(actual, approvedFileContent, null);
    }

    @Test
    public void shouldNotThrowStackOverFlowExceptionWhenExpectedBeanIsNullAndTheActualNotNull() {
        CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();
        String approvedFileContent = "/*com.github.karsaig.approvalcrest.JsonMatcherCircularReferenceTest.shouldNotThrowStackOverFlowExceptionWhenExpectedBeanIsNullAndTheActualNotNull*/\n" +
                "{\n" +
                "  \"parent\": null\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherCircularReferenceTest", "shouldNotThrowStackOverFlowExceptionWhenExpectedBeanIsNullAndTheActualNotNull");
            JsonMatcher<CircularReferenceBean> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("b16968").resolve("cb2b1f-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {}\n" +
                    "     but: Expected file b16968/cb2b1f-approved.json\n" +
                    "\n" +
                    "Unexpected: parent\n", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("b16968/cb2b1f-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowStackOverflowExceptionWhenCircularReferenceExistsInAComplexGraph() {
        Four actual = new Four();
        Four child1 = new Four();
        Four child2 = new Four();
        actual.setGenericObject(child1);
        child1.setGenericObject(actual); // circular
        actual.setSubClassField(child2);

        One subRoot = new One();
        One subRootChild = new One();
        subRoot.setGenericObject(subRootChild);
        subRootChild.setGenericObject(subRoot); // circular

        child2.setGenericObject(subRoot);

        String approvedFileContent = "/*com.github.karsaig.approvalcrest.JsonMatcherCircularReferenceTest.shouldNotThrowStackOverflowExceptionWhenCircularReferenceExistsInAComplexGraph*/\n" +
                "{\n" +
                "  \"0x1\": {\n" +
                "    \"subClassField\": \"0x2\",\n" +
                "    \"threeObject\": \"0x3\"\n" +
                "  },\n" +
                "  \"0x2\": {\n" +
                "    \"threeObject\": \"0x4\"\n" +
                "  },\n" +
                "  \"0x3\": {\n" +
                "    \"threeObject\": \"0x1\"\n" +
                "  },\n" +
                "  \"0x4\": {\n" +
                "    \"oneObject\": \"0x5\"\n" +
                "  },\n" +
                "  \"0x5\": {\n" +
                "    \"oneObject\": \"0x4\"\n" +
                "  }\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherCircularReferenceTest", "shouldNotThrowStackOverflowExceptionWhenCircularReferenceExistsInAComplexGraph");
            JsonMatcher<Four> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("b16968").resolve("b47cca-approved.json"), approvedFileContent);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("b16968/b47cca-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void doesNotThrowStackOverflowErrorWhenComparedObjectsHaveDifferentCircularReferences() {
        Object actual = new One();
        One actualChild = new One();
        ((One) actual).setGenericObject(actualChild);
        actualChild.setGenericObject(actual);

        String approvedFileContent = "/*com.github.karsaig.approvalcrest.matcher.JsonMatcherCircularReferenceTest.doesNotThrowStackOverflowErrorWhenComparedObjectsHaveDifferentCircularReferences*/\n" +
                "{\n" +
                "  \"0x1\": {\n" +
                "    \"twoObject\": \"0x2\"\n" +
                "  },\n" +
                "  \"0x2\": {\n" +
                "    \"twoObject\": \"0x1\"\n" +
                "  }\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherCircularReferenceTest", "doesNotThrowStackOverflowErrorWhenComparedObjectsHaveDifferentCircularReferences");
            JsonMatcher<Object> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("b16968").resolve("d75e15-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"0x1\": {\n" +
                    "    \"twoObject\": \"0x2\"\n" +
                    "  },\n" +
                    "  \"0x2\": {\n" +
                    "    \"twoObject\": \"0x1\"\n" +
                    "  }\n" +
                    "}\n" +
                    "     but: Expected file b16968/d75e15-approved.json\n" +
                    "0x1\n" +
                    "Expected: twoObject\n" +
                    "     but none found\n" +
                    " ; 0x1\n" +
                    "Unexpected: oneObject\n" +
                    " ; 0x2\n" +
                    "Expected: twoObject\n" +
                    "     but none found\n" +
                    " ; 0x2\n" +
                    "Unexpected: oneObject\n", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("b16968/d75e15-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotTakeAges() {
        Element actual = Element.TWO;
        String approvedFileContent = "/*com.github.karsaig.approvalcrest.matcher.JsonMatcherCircularReferenceTest.shouldNotTakeAges*/\n" +
                "{\n" +
                "  \"element\": \"one\"\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherCircularReferenceTest", "shouldNotTakeAges");
            writeFile(imfsi.getTestPath().resolve("b16968").resolve("492db7-approved.json"), approvedFileContent);
            assertTimeout(Duration.ofMillis(150), () -> {
                JsonMatcher<Element> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());
                AssertionError actualError = assertThrows(AssertionError.class,
                        () -> MatcherAssert.assertThat(actual, underTest));

                Assertions.assertEquals("\n" +
                        "Expected: {\n" +
                        "  \"element\": \"one\"\n" +
                        "}\n" +
                        "     but: Expected file b16968/492db7-approved.json\n" +
                        "element\n" +
                        "Expected: one\n" +
                        "     got: two\n", actualError.getMessage());

            });
            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("b16968/492db7-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void doesNotThrowStackOverflowErrorWhenCircularReferenceIsInTheSecondLevelUpperClass() {
        RuntimeException actual = new RuntimeException();
        String approvedFileContent = "{\n" +
                "  \"0x1\": {\n" +
                "    \"suppressedExceptions\": [],\n" +
                "    \"class\": \"java.lang.RuntimeException\"\n" +
                "  }\n" +
                "}";
        inMemoryFsWithDummyTestInfo(actual, approvedFileContent, null);
    }

    @Test
    public void doesNotThrowStackOverflowExceptionWithAMoreNestedObject() {
        Throwable actual = new Throwable(new Exception(new RuntimeException(new ClassCastException())));
        String approvedFileContent = "{\n" +
                "  \"detailMessage\": \"java.lang.Exception: java.lang.RuntimeException: java.lang.ClassCastException\",\n" +
                "  \"cause\": {\n" +
                "    \"detailMessage\": \"java.lang.RuntimeException: java.lang.ClassCastException\",\n" +
                "    \"cause\": {\n" +
                "      \"detailMessage\": \"java.lang.ClassCastException\",\n" +
                "      \"cause\": {\n" +
                "        \"0x1\": {\n" +
                "          \"suppressedExceptions\": [],\n" +
                "          \"class\": \"java.lang.ClassCastException\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"suppressedExceptions\": [],\n" +
                "      \"class\": \"java.lang.RuntimeException\"\n" +
                "    },\n" +
                "    \"suppressedExceptions\": [],\n" +
                "    \"class\": \"java.lang.Exception\"\n" +
                "  },\n" +
                "  \"suppressedExceptions\": [],\n" +
                "  \"class\": \"java.lang.Throwable\"\n" +
                "}";
        inMemoryFsWithDummyTestInfo(actual, approvedFileContent, null);
    }

    @Test
    public void doesNotReturn0x1InDiagnosticWhenUnnecessary() {
        Element actual = Element.ONE;
        String approvedFileContent = "/*com.github.karsaig.approvalcrest.JsonMatcherCircularReferenceTest.doesNotReturn0x1InDiagnosticWhenUnnecessary*/\n" +
                "{\n" +
                "  \"element\": null\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherCircularReferenceTest", "doesNotReturn0x1InDiagnosticWhenUnnecessary");
            JsonMatcher<Element> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("b16968").resolve("d52e02-approved.json"), approvedFileContent);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals("\n" +
                    "Expected: {}\n" +
                    "     but: Expected file b16968/d52e02-approved.json\n" +
                    "\n" +
                    "Unexpected: element\n", actualError.getMessage());

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("b16968/d52e02-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void doesNotFailWithClosableFields() {
        ClosableFields actual = new ClosableFields();
        actual.setInput(new ByteArrayInputStream("DummyInput".getBytes()));
        actual.setOutput(new ByteArrayOutputStream());

        String approvedFileContent = "/*com.github.karsaig.approvalcrest.matcher.JsonMatcherCircularReferenceTest.doesNotFailWithClosableFields*/\n" +
                "{\n" +
                "  \"input\": {\n" +
                "    \"buf\": [\n" +
                "      68,\n" +
                "      117,\n" +
                "      109,\n" +
                "      109,\n" +
                "      121,\n" +
                "      73,\n" +
                "      110,\n" +
                "      112,\n" +
                "      117,\n" +
                "      116\n" +
                "    ],\n" +
                "    \"pos\": 0,\n" +
                "    \"mark\": 0,\n" +
                "    \"count\": 10\n" +
                "  },\n" +
                "  \"output\": {\n" +
                "    \"buf\": [\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0\n" +
                "    ],\n" +
                "    \"count\": 0\n" +
                "  }\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherCircularReferenceTest", "doesNotFailWithClosableFields");
            JsonMatcher<ClosableFields> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("b16968").resolve("d75f36-approved.json"), approvedFileContent);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("b16968/d75f36-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void doesNotFailWithIterableFields() {
        SQLException sqlException = new SQLException("dummy reason");
        IterableFields actual = new IterableFields();
        Two dummy1 = new Two();
        dummy1.setGenericObject("Dummy1");
        Two dummy2 = new Two();
        dummy2.setGenericObject("Dummy1");
        actual.setTwos(Sets.newHashSet(dummy1, dummy2));
        actual.setOnes(sqlException);

        String approvedFileContent = "/*com.github.karsaig.approvalcrest.matcher.JsonMatcherCircularReferenceTest.doesNotFailWithIterableFields*/\n" +
                "{\n" +
                "  \"ones\": {},\n" +
                "  \"twos\": [\n" +
                "    {\n" +
                "      \"twoObject\": \"Dummy1\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"twoObject\": \"Dummy1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherCircularReferenceTest", "doesNotFailWithIterableFields");
            JsonMatcher<IterableFields> underTest = new JsonMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            writeFile(imfsi.getTestPath().resolve("b16968").resolve("57143d-approved.json"), approvedFileContent);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("b16968/57143d-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    @Test
    public void shouldNotThrowStackOverflowExceptionWhenCircularReferenceExistsIsSkippedButCustomSerialized() {
        Four actual = new Four();
        Four child1 = new Four();
        Four child2 = new Four();
        actual.setGenericObject(child1);
        actual.setSubClassField(child2);

        One subRoot = new One();
        One subRootChild = new One();
        subRoot.setGenericObject(subRootChild);
        subRootChild.setGenericObject(subRoot); // circular
        Function<Object, Boolean> skipper1 = One.class::isInstance;
        GsonConfiguration config = new GsonConfiguration();
        config.addTypeAdapter(One.class, new DummyOneJsonSerializer());


        child2.setGenericObject(subRoot);

        String approvedFileContent = "/*com.github.karsaig.approvalcrest.matcher.JsonMatcherCircularReferenceTest.shouldNotThrowStackOverflowExceptionWhenCircularReferenceExistsIsSkippedButCustomSerialized*/\n" +
                "{\n" +
                "  \"subClassField\": {\n" +
                "    \"threeObject\": \"customSerializedOneCircle\"\n" +
                "  },\n" +
                "  \"threeObject\": {}\n" +
                "}";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi, "JsonMatcherCircularReferenceTest", "shouldNotThrowStackOverflowExceptionWhenCircularReferenceExistsIsSkippedButCustomSerialized");
            JsonMatcher<Four> underTest = new JsonMatcher<Four>(dummyTestInfo, getDefaultFileMatcherConfig()).skipCircularReferenceCheck(skipper1).withGsonConfiguration(config);

            writeFile(imfsi.getTestPath().resolve("b16968").resolve("60ec6f-approved.json"), approvedFileContent);

            MatcherAssert.assertThat(actual, underTest);

            List<InMemoryFiles> actualFiles = getFiles(imfsi);
            InMemoryFiles expected = new InMemoryFiles("b16968/60ec6f-approved.json", approvedFileContent);

            assertIterableEquals(singletonList(expected), actualFiles);
        });
    }

    private class DummyOneJsonSerializer implements JsonDeserializer<One>, JsonSerializer<One> {

        private static final String LONG_SUFFIX = " Long_variable";

        @Override
        public One deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            return null;
        }

        @Override
        public JsonElement serialize(final One src, final Type typeOfSrc, final JsonSerializationContext context) {
            return new JsonPrimitive("customSerializedOneCircle");
        }

    }
}
