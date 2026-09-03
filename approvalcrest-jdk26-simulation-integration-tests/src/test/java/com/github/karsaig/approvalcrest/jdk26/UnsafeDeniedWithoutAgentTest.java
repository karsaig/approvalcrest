package com.github.karsaig.approvalcrest.jdk26;

import com.github.karsaig.approvalcrest.ReflectUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Runs with the sun-misc-unsafe-memory-access=deny flag - JDK 26's default - and no agent, which is
 * what a project that has not opted in gets. Locks in the documented degradation: getter-based
 * output, and no exception.
 */
class UnsafeDeniedWithoutAgentTest {

    @Test
    void nothingCanOpenAModule() {
        assertThat(ReflectUtil.isUnsafeAvailable(), is(false));
        assertThat(ReflectUtil.isInstrumentationAvailable(), is(false));
        assertThat(ReflectUtil.isModuleOpeningAvailable(), is(false));
    }

    @Test
    void jdkExceptionFallsBackToGetters() {
        String mismatch = Jdk26Fixtures.describeMismatch(
                Jdk26Fixtures.jdkException("one"), Jdk26Fixtures.jdkException("two"));

        assertThat(mismatch, containsString("localizedMessage"));
        assertThat(mismatch, not(containsString("detailMessage")));
    }

    /**
     * This threw JsonIOException on java.lang.Throwable#detailMessage before types that merely
     * inherit locked-module fields were treated as locked too.
     */
    @Test
    void userDefinedExceptionFallsBackToGettersRatherThanThrowing() {
        // Vary the message, because a mismatch description names only the fields that differ - so
        // this is what makes the property carrying the message visible in the first place.
        String mismatch = Jdk26Fixtures.describeMismatch(
                Jdk26Fixtures.userException("boom", "T-1"), Jdk26Fixtures.userException("bang", "T-1"));

        assertThat(mismatch, containsString("localizedMessage"));
        assertThat(mismatch, not(containsString("detailMessage")));
    }

    @Test
    void userDefinedExceptionStillCoversItsOwnFields() {
        String mismatch = Jdk26Fixtures.describeMismatch(
                Jdk26Fixtures.userException("boom", "T-1"), Jdk26Fixtures.userException("boom", "T-2"));

        assertThat(mismatch, containsString("ticket"));
    }

    @Test
    void equalExceptionsStillCompareEqual() {
        assertThat(Jdk26Fixtures.matches(
                Jdk26Fixtures.userException("boom", "T-1"), Jdk26Fixtures.userException("boom", "T-1")), is(true));
    }
}
