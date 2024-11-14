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
import org.junit.Assert;
import org.junit.Test;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.ParentBean.Builder.parent;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.assertThat;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.sameBeanAs;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Tests which verify the diagnostic displayed when a custom matcher fails.
 */
public class BeanMatcherCustomMatchingTest {

    @Test
    public void includesDescriptionAndMismatchDescriptionForFailingMatcherOnPrimiteField() {
        ParentBean.Builder expected = parent().childBean(child().childString("apple"));
        ParentBean.Builder actual = parent().childBean(child().childString("banana"));

        AssertionError ae = Assert.assertThrows(AssertionError.class, () -> assertThat(actual, sameBeanAs(expected).with("childBean.childString", equalTo("kiwi"))));
        Assert.assertEquals("\n" +
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

        assertThat(actual, sameBeanAs(expected).with("childBean.childString", equalTo("banana")));
    }
}
