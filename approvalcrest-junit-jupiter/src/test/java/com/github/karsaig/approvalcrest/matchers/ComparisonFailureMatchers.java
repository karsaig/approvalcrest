/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest.matchers;

import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.opentest4j.AssertionFailedError;

/**
 * AssertionFailedError hamcrest matchers.
 */
public class ComparisonFailureMatchers {
	public static Matcher<AssertionFailedError> expected(final Matcher<String> expectedMatcher) {
		return new FeatureMatcher<AssertionFailedError, String>(expectedMatcher, "AssertionFailedError with expected string", "expected string") {
			@Override
			protected String featureValueOf(final AssertionFailedError actual) {
				return actual.getExpected().getValue().toString();
			}
		};
	}

	public static Matcher<AssertionFailedError> actual(final Matcher<String> actualMatcher) {
		return new FeatureMatcher<AssertionFailedError, String>(actualMatcher, "AssertionFailedError with actual string", "actual string") {
			@Override
			protected String featureValueOf(final AssertionFailedError actual) {
				return actual.getActual().getValue().toString();
			}
		};
	}

	public static Matcher<AssertionFailedError> message(final Matcher<String> messageMatcher) {
		return new FeatureMatcher<AssertionFailedError, String>(messageMatcher, "AssertionFailedError with message string", "message string") {
			@Override
			protected String featureValueOf(final AssertionFailedError actual) {
				return actual.getMessage();
			}
		};
	}

	public static void checkThat(final AssertionFailedError e, final Matcher<AssertionFailedError>... matchers) {
		org.hamcrest.MatcherAssert.assertThat(e, allOf(matchers));
	}
}
