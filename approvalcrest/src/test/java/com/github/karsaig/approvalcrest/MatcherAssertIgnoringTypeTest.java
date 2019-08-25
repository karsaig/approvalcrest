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

import org.junit.ComparisonFailure;
import org.junit.Test;

import com.github.karsaig.approvalcrest.model.Bean;
import com.github.karsaig.approvalcrest.model.ChildBean;
import com.github.karsaig.approvalcrest.model.ParentBean;

/**
 * Tests for {@link MatcherAssert} which verify that fields of specific object types can be ignored from the comparison.
 */
public class MatcherAssertIgnoringTypeTest {

	@Test
	public void ignoresType() {
		ParentBean expected = parent().childBean(child().childString("value2")).build();
		ParentBean actual = parent().childBean(child().childString("value1")).build();

		assertThat(actual, sameBeanAs(expected).ignoring(ChildBean.class));
	}
	
	@Test(expected = ComparisonFailure.class)
	public void failsWhenBeanDoesNotMatchAfterIgnoringType() {
		Bean expected = bean().string("string").build();
		Bean actual = bean().integer(1).build();

		assertThat(actual, sameBeanAs(expected).ignoring(Boolean.class));
	}
}
