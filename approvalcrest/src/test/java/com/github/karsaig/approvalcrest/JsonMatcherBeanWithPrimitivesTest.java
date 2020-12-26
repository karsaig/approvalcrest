package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import java.lang.reflect.Type;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Unit tests which verify the basic usage of the
 * {@link com.github.karsaig.approvalcrest.matcher.Matchers#sameJsonAsApproved()} method.
 *
 * @author Andras_Gyuro
 */
public class JsonMatcherBeanWithPrimitivesTest extends AbstractJsonMatcherTest {

    private BeanWithPrimitives actual;

    @Before
    public void setUp() {
        actual = getBeanWithPrimitives();
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJson() {
        assertThat(actual, sameJsonAsApproved());
    }

    @Test(expected = ComparisonFailure.class)
    public void shouldThrowAssertionErrorWhenModelDiffersFromApprovedJson() {
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelDiffersFromApprovedJsonButFieldIsIgnored() {
        assertThat(actual, sameJsonAsApproved().ignoring("beanLong").ignoring("beanBoolean"));
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelDiffersFromApprovedJsonButFieldIsIgnoredWithMatcher() {
        Matcher<String> endsWithLongMatcher = Matchers.endsWith("Long");
        Matcher<String> endsWithBooleanMatcher = Matchers.endsWith("Boolean");

        assertThat(actual, sameJsonAsApproved().ignoring(endsWithLongMatcher).ignoring(endsWithBooleanMatcher));
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelDiffersFromApprovedJsonButFieldIsIgnoredWithClass() {
        assertThat(actual, sameJsonAsApproved().ignoring(Long.class).ignoring(Boolean.class));
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJsonWithUniqueId() {
        assertThat(actual, sameJsonAsApproved().withUniqueId("idTest"));
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJsonWithFileName() {
        assertThat(actual, sameJsonAsApproved().withFileName("bean-with-primitive-values"));
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJsonWithFileNameAndPathName() {
        assertThat(actual, sameJsonAsApproved().withPathName("src/test/jsons").withFileName("bean-with-primitive-values-2"));
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelAsStringIsSameAsApprovedJson() {
        String model = getBeanAsJsonString();

        assertThat(model, sameJsonAsApproved());
    }

    @Test
    public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJsonWithGsonConfiguration() {
        GsonConfiguration config = new GsonConfiguration();
        config.addTypeAdapter(Long.class, new DummyStringJsonSerializer());

        assertThat(actual, sameJsonAsApproved().withGsonConfiguration(config));
    }

    private class DummyStringJsonSerializer implements JsonDeserializer<Long>, JsonSerializer<Long> {

        private static final String LONG_SUFFIX = " Long_variable";

        @Override
        public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Long result = null;
            if (!json.isJsonNull()) {
                String asString = json.getAsString();
                result = Long.parseLong(asString.replace(LONG_SUFFIX, ""));
            }
            return result;
        }

        @Override
        public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src + LONG_SUFFIX);
        }

    }

}
