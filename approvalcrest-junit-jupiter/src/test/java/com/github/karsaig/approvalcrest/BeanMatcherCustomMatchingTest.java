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

import com.github.karsaig.approvalcrest.testdata.ParentBean;
import com.github.karsaig.approvalcrest.util.AssertionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static org.hamcrest.core.IsEqual.equalTo;

public class BeanMatcherCustomMatchingTest {

    @Test
    public void includesDescriptionAndMismatchDescriptionForFailingMatcherOnPrimiteField() {
        ParentBean expected = parent().childBean(child().childString("apple")).build();
        ParentBean actual = parent().childBean(child().childString("banana")).build();

        AssertionError ae = Assertions.assertThrows(AssertionError.class, () -> assertThat(actual, sameBeanAs(expected).with("childBean.childString", equalTo("kiwi"))));
        Assertions.assertEquals("\n" +
                "Expected: {\n" +
                "  \"childBean\": {\n" +
                "    \"childInteger\": 0\n" +
                "  },\n" +
                "  \"childBeanList\": [],\n" +
                "  \"childBeanMap\": []\n" +
                "}\n" +
                "and childBean.childString \"kiwi\"\n" +
                "     but: childBean.childString was \"banana\"", ae.getMessage());
    }

    @Test
    public void matchesPrimitiveWithCustomMatcher() {
        ParentBean.Builder expected = parent().childBean(child().childString("apple"));
        ParentBean.Builder actual = parent().childBean(child().childString("banana"));

        AssertionHelper.assertThat(actual, AssertionHelper.sameBeanAs(expected).with("childBean.childString", equalTo("banana")));
    }
}
