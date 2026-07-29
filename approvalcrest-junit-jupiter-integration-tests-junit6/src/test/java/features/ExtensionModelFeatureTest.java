package features;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

/**
 * Extensions change how a test method is called: they inject parameters and, in the case of an
 * {@code InvocationInterceptor}, invoke the method themselves. The stack-trace route walks the frames
 * between the engine and the assertion, so it is the part of the library an extension can break.
 */
@ExtendWith(ExtensionModelFeatureTest.BeanResolver.class)
@ExtendWith(ExtensionModelFeatureTest.CountingInterceptor.class)
//1454a7
public class ExtensionModelFeatureTest {

    @RegisterExtension
    final RecordingExtension recording = new RecordingExtension();

    //f8ae58
    @Test
    public void anExtensionInjectedParameterDoesNotMoveTheFile(BeanOne injected, TestInfo testInfo) {
        assertThat(injected, sameJsonAsApproved());
        assertThat(injected, sameJsonAsApproved(testInfo));
    }

    //dcdd2d
    @Test
    void aRegisteredExtensionDoesNotMoveTheFile(TestInfo testInfo) {
        Assertions.assertEquals("aRegisteredExtensionDoesNotMoveTheFile", recording.lastTestMethodName,
                "@RegisterExtension instance field must be called back for this test");

        assertThat(new BeanOne("dummy", "value"), sameJsonAsApproved());
        assertThat(new BeanOne("dummy", "value"), sameJsonAsApproved(testInfo));
    }

    //a41d8b
    @Test
    void anInterceptedInvocationDoesNotMoveTheFile(TestInfo testInfo) {
        Assertions.assertTrue(CountingInterceptor.intercepted > 0, "the interceptor must have run");

        assertThat(new BeanOne("dummy", "value"), sameJsonAsApproved());
        assertThat(new BeanOne("dummy", "value"), sameJsonAsApproved(testInfo));
    }

    static class BeanResolver implements ParameterResolver {

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameterContext.getParameter().getType() == BeanOne.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return new BeanOne("injected", "byExtension");
        }
    }

    static class RecordingExtension implements BeforeEachCallback {

        private String lastTestMethodName;

        @Override
        public void beforeEach(ExtensionContext context) {
            lastTestMethodName = context.getRequiredTestMethod().getName();
        }
    }

    static class CountingInterceptor implements InvocationInterceptor {

        private static int intercepted;

        @Override
        public void interceptTestMethod(Invocation<Void> invocation,
                                       ReflectiveInvocationContext<Method> invocationContext,
                                       ExtensionContext extensionContext) throws Throwable {
            ++intercepted;
            invocation.proceed();
        }
    }
}
