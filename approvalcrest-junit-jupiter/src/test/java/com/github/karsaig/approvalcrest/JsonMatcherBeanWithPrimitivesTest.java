package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Type;
import java.util.GregorianCalendar;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.model.BeanWithPrimitives;
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
 * @author Andras_Gyuro
 *
 */
public class JsonMatcherBeanWithPrimitivesTest extends AbstractJsonMatcherTest {

	private BeanWithPrimitives actual;

	@BeforeEach
	public void setUp(){
		actual = getBeanWithPrimitives();
	}

	@Test
	public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJson(){
		assertThat(actual, sameJsonAsApproved());
	}

	@Test
	public void shouldThrowAssertionErrorWhenModelDiffersFromApprovedJson(){
		assertThrows(AssertionFailedError.class, () -> {
			assertThat(actual, sameJsonAsApproved());
		});
	}

	@Test
	public void shouldNotThrowAssertionErrorWhenModelDiffersFromApprovedJsonButFieldIsIgnored(){
		assertThat(actual, sameJsonAsApproved().ignoring("beanLong").ignoring("beanBoolean"));
	}

	@Test
	public void shouldNotThrowAssertionErrorWhenModelDiffersFromApprovedJsonButFieldIsIgnoredWithMatcher(){
		Matcher<String> endsWithLongMatcher = Matchers.endsWith("Long");
		Matcher<String> endsWithBooleanMatcher = Matchers.endsWith("Boolean");

		assertThat(actual, sameJsonAsApproved().ignoring(endsWithLongMatcher).ignoring(endsWithBooleanMatcher));
	}

	@Test
	public void shouldNotThrowAssertionErrorWhenModelDiffersFromApprovedJsonButFieldIsIgnoredWithClass(){
		Matchers.endsWith("Long");
		Matchers.endsWith("Boolean");

		assertThat(actual, sameJsonAsApproved().ignoring(Long.class).ignoring(Boolean.class));
	}

	@Test
	public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJsonWithUniqueId(){
		assertThat(actual, sameJsonAsApproved().withUniqueId("idTest"));
	}

	@Test
	public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJsonWithFileName(){
		assertThat(actual, sameJsonAsApproved().withFileName("bean-with-primitive-values"));
	}

	@Test
	public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJsonWithFileNameAndPathName(){
		assertThat(actual, sameJsonAsApproved().withPathName("src/test/jsons").withFileName("bean-with-primitive-values-2"));
	}

	@Test
	public void shouldNotThrowAssertionErrorWhenModelAsStringIsSameAsApprovedJson(){
		String model = getBeanAsJsonString();

		assertThat(model, sameJsonAsApproved());
	}

    @Test
    public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJsonWithGsonConfiguration(){
        GsonConfiguration config = new GsonConfiguration();
        config.addTypeAdapter(Long.class, new DummyStringJsonSerializer());

        assertThat(actual, sameJsonAsApproved().withGsonConfiguration(config));
    }

    @ParameterizedTest
    @ValueSource(strings = "@ParameterizedTest annotation present")
    public void shouldNotThrowAssertionErrorWhenAnnotationIsTestTemplate(String input){
        assertThat(actual, sameJsonAsApproved());
    }

	private class DummyStringJsonSerializer implements JsonDeserializer<Long>,JsonSerializer<Long>  {

		private static final String LONG_SUFFIX = " Long_variable";

		@Override
		public Long deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		        Long result = null;
		        if (!json.isJsonNull()) {
		            String asString = json.getAsString();
		            result = Long.parseLong(asString.replace(LONG_SUFFIX, ""));
		        }
		        return result;
		    }

		@Override
		public JsonElement serialize(final Long src, final Type typeOfSrc, final JsonSerializationContext context) {
		    return new JsonPrimitive(src+LONG_SUFFIX);
		}

}

}
