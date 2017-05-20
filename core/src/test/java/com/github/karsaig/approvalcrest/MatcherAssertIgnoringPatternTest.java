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
import static com.github.karsaig.approvalcrest.model.Bean.Builder.bean;
import static com.github.karsaig.approvalcrest.model.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.model.ParentBean.Builder.parent;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.ComparisonFailure;
import org.junit.Test;

import com.github.karsaig.approvalcrest.MatcherAssert;
import com.github.karsaig.approvalcrest.model.Bean;
import com.github.karsaig.approvalcrest.model.ParentBean;

/**
 * Tests for {@link MatcherAssert} which verify that fields that match supplied pattern are ignored.
 */
public class MatcherAssertIgnoringPatternTest {

	@Test
	public void ignoresByExactName() {
	    ParentBean expected = parent().childBean("value", 123).build();
	    ParentBean actual = parent().childBean("eulav", 123).build();

		assertThat(actual, sameBeanAs(expected).ignoring(is("childString")));
	}
	
    @Test
    public void ignoresAnyMatchingFieldNames() {
        ParentBean expected = parent().childBean("value", 123).addToChildBeanList(child().childString("child").build()).build();
        ParentBean actual = parent().childBean("value", 321).addToChildBeanList(child().childString("dlihc").build()).build();

        assertThat(actual, sameBeanAs(expected).ignoring(containsString("child")));
    }
    
    @Test(expected = ComparisonFailure.class)
    public void throwsComparisonFailureWhenNonIgnoredFieldsMismatch() {
    	Bean expected = bean().string("1234").integer(12345).build();
    	Bean actual = bean().string("1234").integer(54321).build();
    	
    	assertThat(actual, sameBeanAs(expected).ignoring(containsString("string")));
    }
}
