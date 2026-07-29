package features;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import com.github.karsaig.approvalcrest.testdata.classdiff.BeanOne;

/**
 * A {@code @TestTemplate} driven by a custom {@code TestTemplateInvocationContextProvider} runs the same
 * method once per invocation context, with display names the provider chooses. The stack-trace route
 * accepts {@code @TestTemplate} directly, and the invocation display name is not of the {@code [1] ...}
 * shape, so nothing is added to the file name automatically — the unique id has to come from the test.
 */
//4d7fdd
public class TestTemplateFeatureTest {

    //da2988
    @TestTemplate
    @ExtendWith(TwoCasesProvider.class)
    public void testTemplateResolvesTheSameFileFromBothRoutes(String caseName, TestInfo testInfo) {
        BeanOne actual = new BeanOne("dummy-" + caseName, "value-" + caseName);

        assertThat(actual, sameJsonAsApproved().withUniqueId(caseName));
        assertThat(actual, sameJsonAsApproved(testInfo).withUniqueId(caseName));
    }

    static class TwoCasesProvider implements TestTemplateInvocationContextProvider {

        @Override
        public boolean supportsTestTemplate(ExtensionContext context) {
            return true;
        }

        @Override
        public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
            return Stream.of(invocationContext("case1"), invocationContext("case2"));
        }

        private static TestTemplateInvocationContext invocationContext(String caseName) {
            return new TestTemplateInvocationContext() {

                @Override
                public String getDisplayName(int invocationIndex) {
                    return caseName;
                }

                @Override
                public List<Extension> getAdditionalExtensions() {
                    return Collections.<Extension>singletonList(new CaseNameResolver(caseName));
                }
            };
        }
    }

    static class CaseNameResolver implements ParameterResolver {

        private final String caseName;

        CaseNameResolver(String caseName) {
            this.caseName = caseName;
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameterContext.getParameter().getType() == String.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return caseName;
        }
    }
}
