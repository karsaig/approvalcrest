package com.github.karsaig.approvalcrest.matcher.circular;


import com.github.karsaig.approvalcrest.StringUtil;
import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.testdata.ClosableFields;
import com.github.karsaig.approvalcrest.testdata.IterableFields;
import com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean;
import com.github.karsaig.approvalcrest.testdata.cyclic.Element;
import com.github.karsaig.approvalcrest.testdata.cyclic.Four;
import com.github.karsaig.approvalcrest.testdata.cyclic.One;
import com.github.karsaig.approvalcrest.testdata.cyclic.Two;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.function.Function;

import static com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean.Builder.circularReferenceBean;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class BeanMatcherCircularReferenceTest extends AbstractBeanMatcherTest {

    @Test
    public void doesNothingWhenAutoDetectCircularReferenceIsCalled() {
        CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();
        CircularReferenceBean expected = circularReferenceBean("parent", "child1", "child2").build();

        assertDiagnosingMatcher(actual, expected);
    }

    @Test
    public void shouldNotThrowStackOverFlowExceptionWhenExpectedBeanIsNullAndTheActualNotNull() {
        CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();
        CircularReferenceBean expected = null;

        assertDiagnosingMatcher(actual, expected, "actual is not null");
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


        assertDiagnosingMatcher(actual, actual);
    }

    @Test
    public void doesNotThrowStackOverflowErrorWhenComparedObjectsHaveDifferentCircularReferences() {
        Object actual = new One();
        One actualChild = new One();
        ((One) actual).setGenericObject(actualChild);
        actualChild.setGenericObject(actual);

        Object expected = new Two();
        Two expectedChild = new Two();
        ((Two) expected).setGenericObject(expectedChild);
        expectedChild.setGenericObject(expected);

        assertDiagnosingMatcher(actual, expected, "0x1\n" +
                "Expected: twoObject\n" +
                "     but none found\n" +
                " ; 0x1\n" +
                "Unexpected: oneObject\n" +
                " ; 0x2\n" +
                "Expected: twoObject\n" +
                "     but none found\n" +
                " ; 0x2\n" +
                "Unexpected: oneObject\n");
    }

    @Test
    public void shouldNotTakeAges() {
        Element actual = Element.TWO;
        Element expected = Element.ONE;

        assertDiagnosingMatcher(actual, expected, "element\n" +
                "Expected: one\n" +
                "     got: two\n");
    }

    @Test
    public void doesNotThrowStackOverflowErrorWhenCircularReferenceIsInTheSecondLevelUpperClass() {
        RuntimeException actual = new RuntimeException();
        RuntimeException expected = new RuntimeException();
        assertDiagnosingMatcher(actual, expected);
    }

    @Test
    public void doesNotThrowStackOverflowExceptionWithAMoreNestedObject() {
        Throwable actual = new Throwable(new Exception(new RuntimeException(new ClassCastException())));
        assertDiagnosingMatcher(actual, actual);
    }

    @Test
    public void doesNotReturn0x1InDiagnosticWhenUnnecessary() {
        Element actual = Element.ONE;
        Element expected = Element.TWO;

        DiagnosingCustomisableMatcher<Object> diagnosingMatcher = MATCHER_FACTORY.beanMatcher(expected);
        AssertionError actualError = assertThrows(AssertionError.class,
                () -> MatcherAssert.assertThat(actual, diagnosingMatcher));


        String expectedExceptionMessage = "\n" +
                "Expected: {\n" +
                "  \"element\": \"two\"\n" +
                "}\n" +
                "     but: element\n" +
                "Expected: two\n" +
                "     got: one\n";

        Assertions.assertEquals(expectedExceptionMessage, StringUtil.normalizeNewLines(actualError.getMessage()));

    }

    @Test
    public void doesNotFailWithClosableFields() {
        ClosableFields actual = new ClosableFields();
        actual.setInput(new ByteArrayInputStream("DummyInput".getBytes()));
        actual.setOutput(new ByteArrayOutputStream());

        ClosableFields expected = new ClosableFields();
        expected.setInput(new ByteArrayInputStream("DummyInput".getBytes()));
        expected.setOutput(new ByteArrayOutputStream());


        assertDiagnosingMatcher(actual, expected);
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


        SQLException sqlException2 = new SQLException("dummy reason");
        IterableFields expected = new IterableFields();
        Two dummy3 = new Two();
        dummy3.setGenericObject("Dummy1");
        Two dummy4 = new Two();
        dummy4.setGenericObject("Dummy1");
        expected.setTwos(Sets.newHashSet(dummy3, dummy4));
        expected.setOnes(sqlException2);


        assertDiagnosingMatcher(actual, expected);
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

        assertDiagnosingErrorMatcher(actual, actual, beanMatcher -> beanMatcher.skipCircularReferenceCheck(skipper1).withGsonConfiguration(config), null);
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
