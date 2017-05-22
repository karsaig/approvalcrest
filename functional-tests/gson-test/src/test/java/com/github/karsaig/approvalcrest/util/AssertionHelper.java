/*
 * Copyright 2013 Shazam Entertainment Limited
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/
package com.github.karsaig.approvalcrest.util;

import com.github.karsaig.approvalcrest.MatcherAssert;
import com.github.karsaig.approvalcrest.matcher.CustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.Matchers;
import com.github.karsaig.approvalcrest.model.Bean;
import com.github.karsaig.approvalcrest.model.ChildBean;
import com.github.karsaig.approvalcrest.model.ParentBean;

/**
 * Provides helper methods to reduce the noise in the test classes
 */
public class AssertionHelper {
	public static CustomisableMatcher<ChildBean> sameBeanAs(ChildBean.Builder expected) {
		return Matchers.sameBeanAs(expected.build());
	}
	
	public static void assertThat(ChildBean.Builder actual, CustomisableMatcher<ChildBean> matcher) {
		MatcherAssert.assertThat(actual.build(), matcher);
	}
	
	public static CustomisableMatcher<ParentBean> sameBeanAs(ParentBean.Builder expected) {
		return Matchers.sameBeanAs(expected.build());
	}
	
	public static void assertThat(ParentBean.Builder actual, CustomisableMatcher<ParentBean> matcher) {
		MatcherAssert.assertThat(actual.build(), matcher);
	}
	
	public static CustomisableMatcher<Bean> sameBeanAs(Bean.Builder expected) {
		return Matchers.sameBeanAs(expected.build());
	}
	
	public static void assertThat(Bean.Builder actual, CustomisableMatcher<Bean> matcher) {
		MatcherAssert.assertThat(actual.build(), matcher);
	}
}
