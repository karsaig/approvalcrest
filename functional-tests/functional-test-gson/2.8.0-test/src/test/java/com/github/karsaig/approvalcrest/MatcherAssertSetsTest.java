package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.model.Bean.Builder.bean;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.github.karsaig.approvalcrest.model.Bean;

/**
 * Tests which verifies sets comparison is not affected by the order of the elements.
 */
@RunWith(Parameterized.class)
public class MatcherAssertSetsTest {
	
	@Parameterized.Parameters
    public static List<Object[]> data() {
        return asList(new Object[20][0]);
    }

    public MatcherAssertSetsTest() {}
	
	@Test
	public void ignoresOrderingInSet() {
		Bean expected = bean().set(newHashSet(
				bean().string("a").build(), 
				bean().string("b").build()))
				.build(); 
		
		Bean actual = bean().set(newHashSet(
				bean().string("a").build(), 
				bean().string("b").build()))
				.build(); 
		
		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void ignoresOrderingInNestedSet() {
		Bean expected = bean().set(newHashSet(
				bean().set(newHashSet(
						bean().string("a").build(), 
						bean().string("b").build())).build(),
				bean().set(newHashSet(
						bean().string("a").build(), 
						bean().string("b").build())).build()))
				.build();

		Bean actual = bean().set(newHashSet(
				bean().set(newHashSet(
						bean().string("a").build(), 
						bean().string("b").build())).build(),
				bean().set(newHashSet(
						bean().string("a").build(), 
						bean().string("b").build())).build()))
				.build();
		
		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void ignoresOrderingForSetsImplementations() {
		Bean expected = bean().hashSet(newHashSet(
				bean().string("a").build(), 
				bean().string("b").build()))
				.build(); 
		
		Bean actual = bean().hashSet(newHashSet(
				bean().string("a").build(), 
				bean().string("b").build()))
				.build(); 
		
		assertThat(actual, sameBeanAs(expected));
	}
}
