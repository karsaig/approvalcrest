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
import static com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean.Builder.circularReferenceBean;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.Test.None;

import com.github.karsaig.approvalcrest.matcher.GsonConfiguration;
import com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean;
import com.github.karsaig.approvalcrest.testdata.cyclic.Element;
import com.github.karsaig.approvalcrest.testdata.cyclic.Four;
import com.github.karsaig.approvalcrest.testdata.cyclic.One;
import com.github.karsaig.approvalcrest.testdata.cyclic.Two;

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