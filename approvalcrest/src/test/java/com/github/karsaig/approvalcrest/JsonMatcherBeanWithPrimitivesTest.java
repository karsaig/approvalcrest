package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.google.gson.*;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.lang.reflect.Type;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

/**
 * Unit tests which verify the basic usage of the
 * {@link com.github.karsaig.approvalcrest.matcher.Matchers#sameJsonAsApproved()} method.
 *
 * @author Andras_Gyuro
 */
//5f9b80
public class JsonMatcherBeanWithPrimitivesTest extends AbstractJsonMatcherTest {

    private BeanWithPrimitives actual;

    @Before
    public void setUp() {
        actual = getBeanWithPrimitives();
    }

    //78b1d8
    @Test
    public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJson() {
        assertThat(actual, sameJsonAsApproved());
    }

    //964370
    @Test
    public void shouldThrowAssertionErrorWhenModelDiffersFromApprovedJson() {
        ComparisonFailure ex = Assert.assertThrows(ComparisonFailure.class, () -> assertThat(actual, sameJsonAsApproved()));
        assertThat(ex.getMessage(), Matchers.stringContainsInOrder("Expected file 5f9b80/964370-approved.json", "Expected: 3.1", "got: 3.0"));
    }

    //10b21f
    @Test
    public void shouldNotThrowAssertionErrorWhenModelDiffersFromApprovedJsonButFieldIsIgnored() {
        assertThat(actual, sameJsonAsApproved().ignoring("beanLong").ignoring("beanBoolean"));
    }

    //e32435
    @Test
    public void shouldThrowAssertionErrorWhenModelDiffersFromApprovedJsonButFieldIsIgnoredAndIgnoredFieldsPresentInApprovedFile() {
        ComparisonFailure ex = Assert.assertThrows(ComparisonFailure.class, () -> assertThat(actual, sameJsonAsApproved().ignoring("beanLong").ignoring("beanBoolean")));
        assertThat(ex.getMessage(), Matchers.stringContainsInOrder("Expected file 5f9b80/e32435-approved.json", "Expected: beanBoolean", "but none found"));
    }

    //6057b5
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
        assertThat(actual, sameJsonAsApproved().withRelativePathName("src/test").withPathName("jsons").withFileName("bean-with-primitive-values-2"));
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
