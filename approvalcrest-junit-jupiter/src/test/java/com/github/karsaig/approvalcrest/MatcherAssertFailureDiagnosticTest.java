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
import static com.github.karsaig.approvalcrest.matchers.ComparisonFailureMatchers.actual;
import static com.github.karsaig.approvalcrest.matchers.ComparisonFailureMatchers.checkThat;
import static com.github.karsaig.approvalcrest.matchers.ComparisonFailureMatchers.expected;
import static com.github.karsaig.approvalcrest.matchers.ComparisonFailureMatchers.message;
import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.testdata.Bean;

/**
 * Smoke tests for {@link com.github.karsaig.approvalcrest.matcher.Matchers#sameBeanAs(Object)}.
 * Logic variants are covered in approvalcrest-core.
 */
@SuppressWarnings("unchecked")
public class MatcherAssertFailureDiagnosticTest {

    @Test
    public void prettyPrintsTheJson() {
        Bean expected = bean().string("value1").integer(1).build();
        Bean actual = bean().string("value2").integer(2).build();

        try {
            assertThat(actual, sameBeanAs(expected));
            fail("Exceptionexpected");
        } catch (AssertionFailedError e) {
            checkThat(e,
                    expected(containsString("{\n" +
                            "  \"integer\": 1,\n" +
                            "  \"string\": \"value1\"\n" +
                            "}")),
                    actual(containsString("{\n" +
                            "  \"integer\": 2,\n" +
                            "  \"string\": \"value2\"\n" +
                            "}")));
        }
    }

    @Test
    public void includesAssertDescriptionInDiagnostic() {
        Bean expected = bean().string("value1").integer(1).build();
        Bean actual = bean().string("value2").integer(2).build();

        try {
            assertThat("assertion description", actual, sameBeanAs(expected));
            fail("Exceptionexpected");
        } catch (AssertionFailedError e) {
            checkThat(e, message(startsWith("assertion description\n")));
        }
    }
}
