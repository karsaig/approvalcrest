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
import static java.util.Arrays.stream;

import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.Streams;

/**
 * Tests that the matcher works as expected with primitives, Strings and Enums
 */
public class MatcherAssertIsEqualTest {
	public static Stream<Object[]> data() {
		Object[][] data = new Object[][] { 
				{ "banana", "banana" }, 
				{ true, true },
				{ 'a', 'a' },
				{ (byte)1, (byte)1 },
				{ (short)1, (short)1 },
				{ 10, 10 },
				{ 20l, 20l },
				{ 30.0f, 30.0f },
				{ 40.0d, 40.0d },
				{ EnumTest.ENUM, EnumTest.ENUM }
		};
		return stream(data);
	}

	@ParameterizedTest
    @MethodSource("data")
	public void matchesExpectation(Object actual, Object expected) {
		assertThat(actual, sameBeanAs(expected));
	}
	
	private enum EnumTest {
		ENUM
	}

}
