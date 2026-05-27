package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.BeanFinder;
import com.github.karsaig.approvalcrest.ComparisonDescription;
import com.github.karsaig.approvalcrest.Either;
import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.PathNullPointerException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.github.karsaig.approvalcrest.BeanFinder.findBeanAt;
import static com.github.karsaig.approvalcrest.JsonElementUtil.collectValuesByFieldNamePattern;
import static com.github.karsaig.approvalcrest.JsonElementUtil.findJsonValueAt;
import static com.github.karsaig.approvalcrest.JsonElementUtil.jsonElementToJavaValue;
import static com.github.karsaig.approvalcrest.EnvVarReader.getBooleanProperty;

public abstract class AbstractDiagnosingMatcher<T> extends DiagnosingMatcher<T> {

    // Class name string — keeps this file compilable on Java 8 since InaccessibleObjectException
    // was introduced in Java 9.
    private static final String INACCESSIBLE_OBJECT_EXCEPTION =
            "java.lang.reflect.InaccessibleObjectException";
    private static final Pattern ADD_OPENS_PATTERN =
            Pattern.compile("module (\\S+) does not \"opens (\\S+)\"");

    private boolean comparisonDescriptionNeeded = false;
    protected boolean machineReadableOutput = getBooleanProperty("fileMatcherMachineReadable");

    /**
     * Template-method entry point. Subclasses implement their matching logic here.
     * This is called by {@link #matches(Object, Description)}, which provides the
     * single catch-point for {@code InaccessibleObjectException}.
     */
    protected abstract boolean doMatches(Object actual, Description mismatchDescription);

    /**
     * Dispatches to {@link #doMatches} and intercepts any {@code InaccessibleObjectException}
     * (checked by class name so this compiles on Java 8) buried anywhere in the cause chain.
     * When found it throws an {@link IllegalStateException} with a human-readable explanation
     * and the exact {@code --add-opens} JVM flag the developer needs to configure.
     */
    @Override
    protected final boolean matches(Object actual, Description mismatchDescription) {
        try {
            return doMatches(actual, mismatchDescription);
        } catch (RuntimeException e) {
            Throwable inaccessible = findCause(e, INACCESSIBLE_OBJECT_EXCEPTION);
            if (inaccessible != null) {
                throw new IllegalStateException(buildAddOpensMessage(inaccessible), e);
            }
            throw e;
        }
    }

    private static Throwable findCause(Throwable t, String className) {
        for (Throwable cause = t; cause != null; cause = cause.getCause()) {
            if (className.equals(cause.getClass().getName())) {
                return cause;
            }
        }
        return null;
    }

    private static String buildAddOpensMessage(Throwable inaccessible) {
        String flag = extractAddOpensFlag(inaccessible.getMessage());
        String placeholder = "--add-opens <module>/<package>=ALL-UNNAMED";
        String example = flag != null ? flag : placeholder;
        return "approvalcrest could not access a field via reflection.\n"
                + "This happens on Java 9+ when the required '--add-opens' JVM argument is missing.\n"
                + (flag != null ? "\nAdd the following JVM argument:\n  " + flag + "\n" : "")
                + "\nFor Maven (maven-surefire-plugin), add to pom.xml:\n"
                + "  <configuration>\n"
                + "    <argLine>" + example + "</argLine>\n"
                + "  </configuration>\n"
                + "\nFor Gradle, add to your test block:\n"
                + "  jvmArgs '" + example + "'\n"
                + "\nFor IDEs (IntelliJ / Eclipse), add to 'VM options' in the run/test configuration.\n"
                + "\nOriginal error: " + inaccessible.getMessage();
    }

    private static String extractAddOpensFlag(String message) {
        if (message == null) {
            return null;
        }
        java.util.regex.Matcher m = ADD_OPENS_PATTERN.matcher(message);
        return m.find() ? "--add-opens " + m.group(1) + "/" + m.group(2) + "=ALL-UNNAMED" : null;
    }

    protected boolean assertJsonEquals(String expectedJson, String actualJson, Description mismatchDescription, Function<Throwable, String> messageExtractor) {
        try {
            JSONAssert.assertEquals(expectedJson, actualJson, true);
        } catch (AssertionError | JSONException e) {
            return appendMismatchDescription(mismatchDescription, expectedJson, actualJson, messageExtractor.apply(e));
        }
        return true;
    }

    protected boolean appendMismatchDescription(Description mismatchDescription, String expected, String actual, String message) {
        if (comparisonDescriptionNeeded && ComparisonDescription.class.isInstance(mismatchDescription)) {
            ComparisonDescription shazamMismatchDescription = (ComparisonDescription) mismatchDescription;
            shazamMismatchDescription.setComparisonFailure(true);
            shazamMismatchDescription.setExpected(expected);
            shazamMismatchDescription.setActual(actual);
            shazamMismatchDescription.setDifferencesMessage(message);
            shazamMismatchDescription.setMachineReadable(machineReadableOutput);
        }
        mismatchDescription.appendText(message);
        return false;
    }

    protected void setComparisonDescriptionNeeded(boolean comparisonDescriptionNeeded) {
        this.comparisonDescriptionNeeded = comparisonDescriptionNeeded;
    }

    protected boolean isComparisonDescriptionNeeded() {
        return comparisonDescriptionNeeded;
    }

    protected boolean areCustomMatchersMatchingBeanOrJson(Object actual, JsonElement actualAsJsonElement, Description mismatchDescription, Gson gson, MatcherConfiguration matcherConfiguration) {
        boolean hasCustomMatchers = !matcherConfiguration.getCustomMatchers().isEmpty();
        boolean hasCustomMatcherPatterns = !matcherConfiguration.getCustomMatcherPatterns().isEmpty();

        if (!hasCustomMatchers && !hasCustomMatcherPatterns) {
            return true;
        }

        if (hasCustomMatchers) {
            if (actual == null) {
                for (Map.Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
                    Matcher<?> matcher = entry.getValue();
                    if (!matcher.matches(null)) {
                        appendFieldPath(matcher, mismatchDescription, matcherConfiguration);
                        matcher.describeMismatch(null, mismatchDescription);
                        appendFieldJsonSnippet(null, mismatchDescription, gson);
                        return false;
                    }
                }
            } else {
                List<FailEntry> retryList = new ArrayList<>();

                for (Map.Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
                    String path = entry.getKey();
                    Matcher<?> matcher = entry.getValue();
                    Either<RuntimeException, Object> beanResult = findBeanAt(path, actual);
                    if (beanResult.isRight()) {
                        Object beanValue = beanResult.getRight();
                        if (!matcherPassesOnValue(matcher, beanValue)) {
                            retryList.add(FailEntry.matcherFailed(path, matcher, reportValueFor(matcher, beanValue)));
                        }
                    } else {
                        retryList.add(FailEntry.beanPath(path, matcher, beanResult.getLeft()));
                    }
                }

                if (!retryList.isEmpty()) {
                    List<FailEntry> finalFailures = new ArrayList<>();

                    for (FailEntry retryEntry : retryList) {
                        Either<RuntimeException, Object> jsonResult = findJsonValueAt(retryEntry.path, actualAsJsonElement);
                        if (jsonResult.isRight()) {
                            Object jsonValue = jsonResult.getRight();
                            if (!matcherPassesOnValue(retryEntry.matcher, jsonValue)) {
                                if (retryEntry.kind == FailEntry.Kind.MATCHER_FAILED) {
                                    finalFailures.add(retryEntry);
                                } else {
                                    finalFailures.add(FailEntry.jsonFailed(retryEntry.path, retryEntry.matcher, reportValueFor(retryEntry.matcher, jsonValue)));
                                }
                            }
                        } else {
                            finalFailures.add(retryEntry);
                        }
                    }

                    if (!finalFailures.isEmpty()) {
                        FailEntry first = finalFailures.get(0);
                        if (first.kind == FailEntry.Kind.BEAN_PATH) {
                            RuntimeException e = first.exception;
                            if (e instanceof PathNullPointerException) {
                                mismatchDescription.appendText(String.format("%s is null", ((PathNullPointerException) e).getPath()));
                                return false;
                            }
                            throw e;
                        }
                        appendFieldPath(first.matcher, mismatchDescription, matcherConfiguration);
                        first.matcher.describeMismatch(first.value, mismatchDescription);
                        appendFieldJsonSnippet(first.value, mismatchDescription, gson);
                        return false;
                    }
                }
            }
        }

        if (hasCustomMatcherPatterns && actual != null
                && actualAsJsonElement != null && !actualAsJsonElement.isJsonNull()) {
            for (AbstractMap.SimpleEntry<Matcher<String>, Matcher<?>> entry : matcherConfiguration.getCustomMatcherPatterns()) {
                Matcher<String> fieldNamePattern = entry.getKey();
                Matcher<?> valueMatcher = entry.getValue();
                List<JsonElement> matchingValues = collectValuesByFieldNamePattern(actualAsJsonElement, fieldNamePattern);
                for (JsonElement je : matchingValues) {
                    Object value = jsonElementToJavaValue(je);
                    if (!valueMatcher.matches(value)) {
                        mismatchDescription.appendDescriptionOf(fieldNamePattern).appendText(" ");
                        valueMatcher.describeMismatch(value, mismatchDescription);
                        appendFieldJsonSnippet(value, mismatchDescription, gson);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Returns true when the matcher passes on {@code value}.
     * For a {@link BeanFinder.FanoutResult} every element must pass (recursively).
     * An <em>empty</em> FanoutResult returns {@code false}: there are no elements to validate
     * against, so the matcher cannot be considered satisfied (avoids vacuous-truth false positives
     * when the collection at the path is empty).
     */
    private static boolean matcherPassesOnValue(Matcher<?> matcher, Object value) {
        if (value instanceof BeanFinder.FanoutResult) {
            BeanFinder.FanoutResult fanout = (BeanFinder.FanoutResult) value;
            if (fanout.isEmpty()) {
                return false;
            }
            for (Object element : fanout) {
                if (!matcherPassesOnValue(matcher, element)) {
                    return false;
                }
            }
            return true;
        }
        return matcher.matches(value);
    }

    /**
     * Returns the value to store in a {@link FailEntry} for error reporting.
     * For a {@link BeanFinder.FanoutResult} we descend to the first failing leaf
     * so the mismatch description names the concrete bad value, not the whole list.
     */
    private static Object reportValueFor(Matcher<?> matcher, Object value) {
        if (value instanceof BeanFinder.FanoutResult) {
            for (Object element : (BeanFinder.FanoutResult) value) {
                if (!matcherPassesOnValue(matcher, element)) {
                    return reportValueFor(matcher, element);
                }
            }
        }
        return value;
    }

    protected void appendFieldPath(Matcher<?> matcher, Description mismatchDescription, MatcherConfiguration matcherConfiguration) {
        for (Map.Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
            if (entry.getValue().equals(matcher)) {
                mismatchDescription.appendText(entry.getKey()).appendText(" ");
            }
        }
    }

    protected void appendFieldJsonSnippet(Object actual, Description mismatchDescription, Gson gson) {
        JsonElement jsonTree = gson.toJsonTree(actual);
        if (!jsonTree.isJsonPrimitive() && !jsonTree.isJsonNull()) {
            mismatchDescription.appendText("\n" + gson.toJson(actual));
        }
    }

    private static class FailEntry {
        enum Kind { BEAN_PATH, MATCHER_FAILED, JSON_FAILED }

        final Kind kind;
        final String path;
        final Matcher<?> matcher;
        final Object value;
        final RuntimeException exception;

        static FailEntry beanPath(String path, Matcher<?> matcher, RuntimeException e) {
            return new FailEntry(Kind.BEAN_PATH, path, matcher, null, e);
        }

        static FailEntry matcherFailed(String path, Matcher<?> matcher, Object value) {
            return new FailEntry(Kind.MATCHER_FAILED, path, matcher, value, null);
        }

        static FailEntry jsonFailed(String path, Matcher<?> matcher, Object value) {
            return new FailEntry(Kind.JSON_FAILED, path, matcher, value, null);
        }

        private FailEntry(Kind kind, String path, Matcher<?> matcher, Object value, RuntimeException exception) {
            this.kind = kind;
            this.path = path;
            this.matcher = matcher;
            this.value = value;
            this.exception = exception;
        }
    }
}
