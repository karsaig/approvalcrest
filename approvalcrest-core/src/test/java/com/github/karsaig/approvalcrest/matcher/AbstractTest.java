package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.ComparisonDescription;
import com.github.karsaig.approvalcrest.TestAssertImpl;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.opentest4j.AssertionFailedError;

import java.util.function.BiConsumer;

public abstract class AbstractTest {

    protected static final TestAssertImpl TEST_ASSERT_IMPl = new TestAssertImpl();
    protected static final String AI_TIP_SUFFIX = "\n[AI tip] Re-run with system property fmAI=true for structured, machine-readable output.";

    protected static String removeAiTip(String message) {
        if (message != null && message.endsWith(AI_TIP_SUFFIX)) {
            return message.substring(0, message.length() - AI_TIP_SUFFIX.length());
        }
        return message;
    }

    protected <T> void assertThat(T input, Matcher<? super T> matcher) {
        TEST_ASSERT_IMPl.assertThat(null, input, matcher, comparisonDescriptionHandler());
    }

    protected BiConsumer<String, ComparisonDescription> comparisonDescriptionHandler() {
        return (s, cd) -> {
            throw new AssertionFailedError(
                    removeAiTip(s),
                    cd.getExpected(),
                    cd.getActual()
            );
        };
    }

    public enum EnumTest {
        ENUM1, ENUM2
    }

}
