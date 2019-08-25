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

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.model.cyclic.CircularReferenceBean.Builder.circularReferenceBean;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.Test.None;

import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.model.cyclic.CircularReferenceBean;
import com.github.karsaig.approvalcrest.model.cyclic.Element;
import com.github.karsaig.approvalcrest.model.cyclic.Four;
import com.github.karsaig.approvalcrest.model.cyclic.One;
import com.github.karsaig.approvalcrest.model.cyclic.Two;
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

    @Test(expected = None.class)
    public void doesNothingWhenAutoDetectCircularReferenceIsCalled() {
        CircularReferenceBean expected = circularReferenceBean("parent", "child1", "child2").build();
        CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();

        assertThat(actual, sameBeanAs(expected));
    }

    @Test(expected = ComparisonFailure.class)
    public void shouldNotThrowStackOverFlowExceptionWhenExpectedBeanIsNullAndTheActualNotNull() {
        CircularReferenceBean expected = null;
        CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();

        assertThat(actual, sameBeanAs(expected));
    }

    @Test(expected = None.class)
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

    @Test(expected = None.class)
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
        Function<Object, Boolean> skipper1 = new Function<Object, Boolean>() {
			
			@Override
			public Boolean apply(Object input) {
				return One.class.isInstance(input);
			}
		};
		GsonConfiguration config = new GsonConfiguration();
		config.addTypeAdapter(One.class, new DummyOneJsonSerializer());
		
        
        child2.setGenericObject(subRoot);

        assertThat(root, sameBeanAs(root).skipCircularReferenceCheck(skipper1).withGsonConfiguration(config));
    }
    
    private class DummyOneJsonSerializer implements JsonDeserializer<One>,JsonSerializer<One>  {

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
    
    @Test(expected = ComparisonFailure.class)
    public void doesNotThrowStackOverflowErrorWhenComparedObjectsHaveDifferentCircularReferences() {
        Object expected = new One();
        One expectedChild = new One();
        ((One)expected).setGenericObject(expectedChild);
        expectedChild.setGenericObject(expected);

        Object actual = new Two();
        Two actualChild = new Two();
        ((Two)actual).setGenericObject(actualChild);
        actualChild.setGenericObject(actual);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test(expected = ComparisonFailure.class, timeout = 150)
    public void shouldNotTakeAges() {
        assertThat(Element.ONE, sameBeanAs(Element.TWO));
    }

    @Test
    public void doesNotThrowStackOverflowErrorWhenCircularReferenceIsInTheSecondLevelUpperClass() {
        assertThat(new RuntimeException(), sameBeanAs(new RuntimeException()));
    }

    @Test
    public void doesNotThrowStackOverflowExceptionWithAMoreNestedObject() {
        final Throwable throwable = new Throwable(new Exception(new RuntimeException(new ClassCastException())));
        
        assertThat(throwable, sameBeanAs(throwable));
    }

    @Test
    public void doesNotReturn0x1InDiagnosticWhenUnnecessary() {
        try {
            assertThat(Element.ONE, sameBeanAs(Element.TWO));

            fail("expected ComparisonFailure");
        } catch (ComparisonFailure e) {
            assertThat(e.getExpected(), not(containsString("0x1")));
            assertThat(e.getActual(), not(containsString("0x1")));
        }
    }
}