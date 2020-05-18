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

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean.Builder.circularReferenceBean;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Type;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean;
import com.github.karsaig.approvalcrest.testdata.cyclic.Element;
import com.github.karsaig.approvalcrest.testdata.cyclic.Four;
import com.github.karsaig.approvalcrest.testdata.cyclic.One;
import com.github.karsaig.approvalcrest.testdata.cyclic.Two;
import com.google.common.base.Function;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Unit tests which verify circular references are handled automatically.
 */
public class MatcherAssertCircularReferenceTest {

	@Test
	public void doesNothingWhenAutoDetectCircularReferenceIsCalled() {
		CircularReferenceBean expected = circularReferenceBean("parent", "child1", "child2").build();
		CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void shouldNotThrowStackOverFlowExceptionWhenExpectedBeanIsNullAndTheActualNotNull() {
		CircularReferenceBean expected = null;
		CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();

		assertThrows(AssertionFailedError.class, () -> {
			assertThat(actual, sameBeanAs(expected));
		});
	}

	@Test
	public void shouldNotThrowStackOverflowExceptionWhenCircularReferenceExistsInAComplexGraph() {
		Four root = new Four();
		Four child1 = new Four();
		Four child2 = new Four();
		root.setGenericObject(child1);
		child1.setGenericObject(root); // circular
		root.setSubClassField(child2);

		One subRoot = new One();
		One subRootChild = new One();
		subRoot.setGenericObject(subRootChild);
		subRootChild.setGenericObject(subRoot); // circular

		child2.setGenericObject(subRoot);

		assertThat(root, sameBeanAs(root));
	}

	@Test
	public void shouldNotThrowStackOverflowExceptionWhenCircularReferenceExistsIsSkippedButCustomSerialized() {
		Four root = new Four();
		Four child1 = new Four();
		Four child2 = new Four();
		root.setGenericObject(child1);
		root.setSubClassField(child2);

		One subRoot = new One();
		One subRootChild = new One();
		subRoot.setGenericObject(subRootChild);
		subRootChild.setGenericObject(subRoot); // circular
		Function<Object, Boolean> skipper1 = input -> One.class.isInstance(input);
		GsonConfiguration config = new GsonConfiguration();
		config.addTypeAdapter(One.class, new DummyOneJsonSerializer());


		child2.setGenericObject(subRoot);

		assertThat(root, sameBeanAs(root).skipCircularReferenceCheck(skipper1).withGsonConfiguration(config));
	}

	private class DummyOneJsonSerializer implements JsonDeserializer<One>,JsonSerializer<One>  {

		@Override
		public One deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return null;
		}

		@Override
		public JsonElement serialize(One src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive("customSerializedOneCircle");
		}

	}

	@Test
	public void doesNotThrowStackOverflowErrorWhenComparedObjectsHaveDifferentCircularReferences() {
		Object expected = new One();
		One expectedChild = new One();
		((One)expected).setGenericObject(expectedChild);
		expectedChild.setGenericObject(expected);

		Object actual = new Two();
		Two actualChild = new Two();
		((Two)actual).setGenericObject(actualChild);
		actualChild.setGenericObject(actual);

		assertThrows(AssertionFailedError.class, () -> {
			assertThat(actual, sameBeanAs(expected));
		});
	}

	@Test
	public void shouldNotTakeAges() {
		assertTimeout(Duration.ofMillis(150), () -> {
			assertThrows(AssertionFailedError.class, () -> {
				assertThat(Element.ONE, sameBeanAs(Element.TWO));
			});

			return null;
		});
	}

	@Test
	public void doesNotThrowStackOverflowErrorWhenCircularReferenceIsInTheSecondLevelUpperClass() {
		assertThat(new RuntimeException(), sameBeanAs(new RuntimeException()));
	}

	@Test
	public void doesNotThrowStackOverflowExceptionWithAMoreNestedObject() {
		Throwable throwable = new Throwable(new Exception(new RuntimeException(new ClassCastException())));

		assertThat(throwable, sameBeanAs(throwable));
	}

	@Test
	public void doesNotReturn0x1InDiagnosticWhenUnnecessary() {
		try {
			assertThat(Element.ONE, sameBeanAs(Element.TWO));

			fail("expected AssertionFailedError");
		} catch (AssertionFailedError e) {
			assertThat(e.getExpected().getValue().toString(), not(containsString("0x1")));
			assertThat(e.getActual().getValue().toString(), not(containsString("0x1")));
		}
	}
}