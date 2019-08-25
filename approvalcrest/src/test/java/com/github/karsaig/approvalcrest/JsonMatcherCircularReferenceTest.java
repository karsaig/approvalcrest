package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.model.cyclic.CircularReferenceBean.Builder.circularReferenceBean;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.sql.SQLException;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.Test.None;

import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.model.ClosableFields;
import com.github.karsaig.approvalcrest.model.IterableFields;
import com.github.karsaig.approvalcrest.model.cyclic.CircularReferenceBean;
import com.github.karsaig.approvalcrest.model.cyclic.Element;
import com.github.karsaig.approvalcrest.model.cyclic.Four;
import com.github.karsaig.approvalcrest.model.cyclic.One;
import com.github.karsaig.approvalcrest.model.cyclic.Two;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonMatcherCircularReferenceTest {
	
	@Test(expected = None.class)
    public void doesNothingWhenAutoDetectCircularReferenceIsCalled() {
        CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();

        assertThat(actual, sameJsonAsApproved());
    }

    @Test(expected = ComparisonFailure.class)
    public void shouldNotThrowStackOverFlowExceptionWhenExpectedBeanIsNullAndTheActualNotNull() {
        CircularReferenceBean actual = circularReferenceBean("parent", "child1", "child2").build();

        assertThat(actual, sameJsonAsApproved());
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

        assertThat(root, sameJsonAsApproved());
    }

    @Test(expected = ComparisonFailure.class)
    public void doesNotThrowStackOverflowErrorWhenComparedObjectsHaveDifferentCircularReferences() {
    	Object actual = new One();
        One actualChild = new One();
        ((One)actual).setGenericObject(actualChild);
        actualChild.setGenericObject(actual);

        assertThat(actual, sameJsonAsApproved());
    }

    @Test(expected = ComparisonFailure.class, timeout = 150)
    public void shouldNotTakeAges() {
        assertThat(Element.TWO, sameJsonAsApproved());
    }

    @Test
    public void doesNotThrowStackOverflowErrorWhenCircularReferenceIsInTheSecondLevelUpperClass() {
        assertThat(new RuntimeException(), sameJsonAsApproved());
    }

    @Test
    public void doesNotThrowStackOverflowExceptionWithAMoreNestedObject() {
        final Throwable throwable = new Throwable(new Exception(new RuntimeException(new ClassCastException())));
        
        assertThat(throwable, sameJsonAsApproved());
    }

    @Test
    public void doesNotReturn0x1InDiagnosticWhenUnnecessary() {
        try {
            assertThat(Element.ONE, sameJsonAsApproved());

            fail("expected ComparisonFailure");
        } catch (ComparisonFailure e) {
            assertThat(e.getExpected(), not(containsString("0x1")));
            assertThat(e.getActual(), not(containsString("0x1")));
        }
    }
    
    @Test
	public void doesNotFailWithClosableFields() {
		ClosableFields input = new ClosableFields();
		input.setInput(new ByteArrayInputStream("DummyInput".getBytes()));
		input.setOutput(new ByteArrayOutputStream());
		
		assertThat(input, sameJsonAsApproved());
	}
    
    @Test
	public void doesNotFailWithIterableFields() {
    	SQLException sqlException = new SQLException("dummy reason");
		IterableFields input = new IterableFields();
		Two dummy1 = new Two();
		dummy1.setGenericObject("Dummy1");
		Two dummy2 = new Two();
		dummy2.setGenericObject("Dummy1");
		input.setTwos(Sets.newHashSet(dummy1,dummy2));
		input.setOnes(sqlException);
		
		assertThat(input, sameJsonAsApproved());
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
        Function<Object, Boolean> skipper1 = new Function<Object, Boolean>() {
			
			@Override
			public Boolean apply(Object input) {
				return One.class.isInstance(input);
			}
		};
		GsonConfiguration config = new GsonConfiguration();
		config.addTypeAdapter(One.class, new DummyOneJsonSerializer());
		
        
        child2.setGenericObject(subRoot);

        assertThat(root, sameJsonAsApproved().skipCircularReferenceCheck(skipper1).withGsonConfiguration(config));
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
}
