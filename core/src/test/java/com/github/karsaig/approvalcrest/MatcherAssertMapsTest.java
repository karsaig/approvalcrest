package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.model.Bean.Builder.bean;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.github.karsaig.approvalcrest.model.Bean;

/**
 * Tests which verifies maps comparison is not affected by the order of the entries.
 */
@RunWith(Parameterized.class)
public class MatcherAssertMapsTest {
	
	@Parameterized.Parameters
    public static List<Object[]> data() {
        return asList(new Object[20][0]);
    }

    public MatcherAssertMapsTest() {}
	
	@Test
	public void ignoresOrderingInMap() {
		Map<Bean, Bean> expectedMap = newHashMap();
		expectedMap.put(bean().string("key1").build(), bean().string("value1").build());
		expectedMap.put(bean().string("key2").build(), bean().string("value2").build());
		Bean expected = bean().map(expectedMap).build();

		Map<Bean, Bean> actualMap = newHashMap();
		actualMap.put(bean().string("key1").build(), bean().string("value1").build());
		actualMap.put(bean().string("key2").build(), bean().string("value2").build());
		Bean actual = bean().map(actualMap).build();
		
		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void ignoresOrderingInMapImplementations() {
		HashMap<Bean, Bean> expectedMap = newHashMap();
		expectedMap.put(bean().string("key1").build(), bean().string("value1").build());
		expectedMap.put(bean().string("key2").build(), bean().string("value2").build());
		Bean expected = bean().hashMap(expectedMap).build();
		
		HashMap<Bean, Bean> actualMap = newHashMap();
		actualMap.put(bean().string("key1").build(), bean().string("value1").build());
		actualMap.put(bean().string("key2").build(), bean().string("value2").build());
		Bean actual = bean().hashMap(actualMap).build();
		
		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void ignoresOrderingInMapWithTwoEntriesWithSameJsonRepresentationAsKeyButDifferentValues() {
		Map<Bean, Bean> expectedMap = newHashMap();
		expectedMap.put(bean().string("key1").build(), bean().string("value1").build());
		expectedMap.put(bean().string("key1").build(), bean().string("value2").build());
		expectedMap.put(bean().string("key2").build(), bean().string("value3").build());
		Bean expected = bean().map(expectedMap).build();
		
		Map<Bean, Bean> actualMap = newHashMap();
		actualMap.put(bean().string("key1").build(), bean().string("value1").build());
		actualMap.put(bean().string("key1").build(), bean().string("value2").build());
		actualMap.put(bean().string("key2").build(), bean().string("value3").build());
		Bean actual = bean().map(actualMap).build();
		
		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void ignoresOrderingInNestedMap() {
		Map<Bean, Bean> expectedNestedMap1 = newHashMap();
		expectedNestedMap1.put(bean().string("nestedMap1Key1").build(), bean().string("nestedMap1Value1").build());
		expectedNestedMap1.put(bean().string("nestedMap1Key2").build(), bean().string("nestedMap1Value2").build());
		
		Map<Bean, Bean> expectedNestedMap2 = newHashMap();
		expectedNestedMap2.put(bean().string("nestedMap2Key1").build(), bean().string("nestedMap2Value1").build());
		expectedNestedMap2.put(bean().string("nestedMap2Key2").build(), bean().string("nestedMap2Value2").build());
		
		Map<Bean, Bean> expectedMap = newHashMap();
		expectedMap.put(bean().string("key1").build(), bean().string("value1").map(expectedNestedMap1).build());
		expectedMap.put(bean().string("key2").build(), bean().string("value2").map(expectedNestedMap2).build());
		Bean expected = bean().map(expectedMap).build();
		
		Map<Bean, Bean> actualNestedMap1 = newHashMap();
		actualNestedMap1.put(bean().string("nestedMap1Key1").build(), bean().string("nestedMap1Value1").build());
		actualNestedMap1.put(bean().string("nestedMap1Key2").build(), bean().string("nestedMap1Value2").build());
		
		Map<Bean, Bean> actualNestedMap2 = newHashMap();
		actualNestedMap2.put(bean().string("nestedMap2Key1").build(), bean().string("nestedMap2Value1").build());
		actualNestedMap2.put(bean().string("nestedMap2Key2").build(), bean().string("nestedMap2Value2").build());
		
		Map<Bean, Bean> actualMap = newHashMap();
		actualMap.put(bean().string("key1").build(), bean().string("value1").map(actualNestedMap1).build());
		actualMap.put(bean().string("key2").build(), bean().string("value2").map(actualNestedMap2).build());
		Bean actual = bean().map(actualMap).build();
		
		assertThat(actual, sameBeanAs(expected));
	}
}
