package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.PreBuilt;

/**
 * Abstract class for common methods used by the JsonMatcher tests.
 *
 * @author Andras_Gyuro
 */
public abstract class AbstractJsonMatcherTest {

    protected BeanWithPrimitives getBeanWithPrimitives() {
        return PreBuilt.getBeanWithPrimitives();
    }

    protected String getBeanAsJsonString() {
        return "{ beanLong: 5, beanString: \"dummyString\", beanInt: 10  }";
    }
}
