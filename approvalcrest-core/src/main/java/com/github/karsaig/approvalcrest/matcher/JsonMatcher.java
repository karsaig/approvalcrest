package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.matcher.file.AbstractDiagnosingFileMatcher;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.karsaig.approvalcrest.BeanFinder.findBeanAt;
import static com.github.karsaig.approvalcrest.CyclicReferenceDetector.getClassesWithCircularReferences;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.MARKER;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.applySorting;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.findPaths;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.sortJsonFields;

/**
 * <p>
 * Matcher for asserting expected DTOs. Searches for an approved JSON file in
 * the same directory as the test file:
 * <ul>
 * <li>If found, the matcher will assert the contents of the JSON file to the actual object,
 * which is serialized to a JSON String. </li>
 * <li>If not found, a non-approved JSON file is created, that must be
 * verified and renamed to "*-approved.json" by the developer. </li>
 * </ul>
 * The files and directories are hashed with SHA-1 algorithm by default to avoid too long file
 * and path names.
 * These are generated in the following way:
 * <ul>
 * <li> the directory name is the first {@value #NUM_OF_HASH_CHARS} characters of the hashed <b>class name</b>. </li>
 * <li> the file name is the first {@value #NUM_OF_HASH_CHARS} characters of the hashed <b>test method name</b>. </li>
 * </ul>
 * <p>
 * This default behaviour can be overridden by using the {@link #withFileName(String)} for
 * custom file name and {@link #withPathName(String)} for custom path.
 * </p>
 *
 * @author Andras_Gyuro
 */
public class JsonMatcher<T> extends AbstractDiagnosingFileMatcher<T, JsonMatcher<T>> implements CustomisableMatcher<T, JsonMatcher<T>> {
    private static final Pattern MARKER_PATTERN = Pattern.compile(MARKER);

    private final MatcherConfiguration matcherConfiguration = new MatcherConfiguration();
    private final Set<Class<?>> circularReferenceTypes = new HashSet<>();
    private JsonElement expected;

    private GsonConfiguration configuration;

    public JsonMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        super(testMetaInformation, fileMatcherConfig, new FileStoreMatcherUtils("json", fileMatcherConfig));
    }

    @Override
    public void describeTo(Description description) {
        Gson gson = GsonProvider.gson(matcherConfiguration, circularReferenceTypes, configuration);
        description.appendText(filterJson(gson, expected, true));
        for (String fieldPath : matcherConfiguration.getCustomMatchers().keySet()) {
            description.appendText("\nand ").appendText(fieldPath).appendText(" ")
                    .appendDescriptionOf(matcherConfiguration.getCustomMatchers().get(fieldPath));
        }
    }

    @Override
    public JsonMatcher<T> ignoring(String fieldPath) {
        matcherConfiguration.addPathToIgnore(fieldPath);
        return this;
    }

    @Override
    public JsonMatcher<T> ignoring(Class<?> clazz) {
        matcherConfiguration.addTypeToIgnore(clazz);
        return this;
    }

    @Override
    public JsonMatcher<T> ignoring(Matcher<String> fieldNamePattern) {
        matcherConfiguration.addPatternToIgnore(fieldNamePattern);
        return this;
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    @Override
    public final JsonMatcher<T> ignoring(Matcher<String>... fieldNamePatterns) {
        matcherConfiguration.addPatternToIgnore(fieldNamePatterns);
        return this;
    }

    @Override
    public <V> JsonMatcher<T> with(String fieldPath, Matcher<V> matcher) {
        ignoring(fieldPath);
        matcherConfiguration.addCustomMatcher(fieldPath, matcher);
        return this;
    }

    @Override
    public JsonMatcher<T> withGsonConfiguration(GsonConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    protected boolean matches(Object actual, Description mismatchDescription) {
        boolean matches = false;
        circularReferenceTypes.addAll(getClassesWithCircularReferences(actual, matcherConfiguration));
        init();
        Gson gson = GsonProvider.gson(matcherConfiguration, circularReferenceTypes, configuration);
        if (createNotApprovedFileIfNotExists(actual, gson)
                && fileMatcherConfig.isPassOnCreateEnabled()) {
            return true;
        }
        initExpectedFromFile();

        if (areCustomMatchersMatching(actual, mismatchDescription, gson)) {

            String expectedJson = filterJson(gson, expected, fileMatcherConfig.isSortInputFile());

            JsonElement actualJsonElement = getAsJsonElement(gson, actual);

            if (actual == null) {
                matches = appendMismatchDescription(mismatchDescription, expectedJson, "null", "actual was null");
            } else {
                String actualJson = filterJson(gson, actualJsonElement, true);

                matches = assertEquals(expectedJson, actualJson, mismatchDescription);
                if (!matches) {
                    matches = handleInPlaceOverwrite(actual, gson);
                }
            }
        } else {
            matches = handleInPlaceOverwrite(actual, gson);
        }
        return matches;
    }

    @Override
    public JsonMatcher<T> ignoring(String... fieldPaths) {
        matcherConfiguration.addPathToIgnore(fieldPaths);
        return this;
    }

    @Override
    public JsonMatcher<T> ignoring(Class<?>... clazzs) {
        matcherConfiguration.addTypeToIgnore(clazzs);
        return this;
    }

    private boolean handleInPlaceOverwrite(Object actual, Gson gson) {
        if (fileMatcherConfig.isOverwriteInPlaceEnabled()) {
            overwriteApprovedFile(actual, gson);
            return true;
        }
        return false;
    }

    private JsonElement getAsJsonElement(Gson gson, Object object) {
        JsonElement result;
        if (object instanceof String) {
            result = JsonParser.parseString((String) object);
        } else {
            result = gson.toJsonTree(object);
        }
        return result;

    }

    private void initExpectedFromFile() {
        expected = getExpectedFromFile(JsonParser::parseString);
    }

    private String filterJson(Gson gson, JsonElement jsonElement, boolean sortFile) {
        Set<String> set = new HashSet<>(matcherConfiguration.getPathsToIgnore());

        JsonElement filteredJson = findPaths(jsonElement, set);
        filterByFieldMatchers(filteredJson, matcherConfiguration.getPatternsToIgnore());
        sortJsonFields(filteredJson, sortFile);
        applySorting(filteredJson, matcherConfiguration.getPathsToSort(), matcherConfiguration.getPatternsToSort(), sortFile);

        return removeSetMarker(gson.toJson(filteredJson));
    }

    private void filterByFieldMatchers(JsonElement jsonElement, List<Matcher<String>> matchers) {
        if (jsonElement != null && !matchers.isEmpty()) {
            filterFieldsByFieldMatchers(jsonElement, matchers);
        }
    }

    private void filterFieldsByFieldMatchers(JsonElement jsonElement, List<Matcher<String>> matchers) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            List<String> fieldsToRemove = getFieldsToRemove(jsonObject.keySet(), matchers);
            fieldsToRemove.forEach(jsonObject::remove);
            jsonObject.entrySet().forEach(je -> filterFieldsByFieldMatchers(je.getValue(), matchers));
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            Iterator<JsonElement> iterator = jsonArray.iterator();
            while (iterator.hasNext()) {
                filterFieldsByFieldMatchers(iterator.next(), matchers);
            }
        }
    }

    private List<String> getFieldsToRemove(Set<String> fieldNames, List<Matcher<String>> matchers) {
        return fieldNames.stream().filter(fn -> anyMatchesFieldName(fn, matchers)).collect(Collectors.toList());
    }

    private boolean anyMatchesFieldName(String fieldName, List<Matcher<String>> matchers) {
        for (Matcher<String> actual : matchers) {
            if (actual.matches(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private boolean assertEquals(String expectedJson, String actualJson,
                                 Description mismatchDescription) {
        try {
            JSONAssert.assertEquals(expectedJson, actualJson, true);
        } catch (AssertionError e) {
            return appendMismatchDescription(mismatchDescription, expectedJson, actualJson, getAssertMessage(fileStoreMatcherUtils, e));
        } catch (JSONException e) {
            return appendMismatchDescription(mismatchDescription, expectedJson, actualJson, getAssertMessage(fileStoreMatcherUtils, e));
        }

        return true;
    }

    private String removeSetMarker(String json) {
        //return json;
        return MARKER_PATTERN.matcher(json).replaceAll("");
    }

    private boolean createNotApprovedFileIfNotExists(Object toApprove, Gson gson) {
        return createNotApprovedFileIfNotExists(toApprove, () -> serializeToJson(toApprove, gson));
    }

    private void overwriteApprovedFile(Object actual, Gson gson) {
        overwriteApprovedFile(actual, () -> serializeToJson(actual, gson));
    }

    private String serializeToJson(Object toApprove, Gson gson) {
        JsonElement actualJsonElement = getAsJsonElement(gson, toApprove);
        return filterJson(gson, actualJsonElement, true);
    }


    private boolean areCustomMatchersMatching(Object actual, Description mismatchDescription,
                                              Gson gson) {
        boolean result = true;
        Map<Object, Matcher<?>> customMatching = new HashMap<>();
        for (Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
            Object object = actual == null ? null : findBeanAt(entry.getKey(), actual);
            customMatching.put(object, matcherConfiguration.getCustomMatchers().get(entry.getKey()));
        }

        for (Entry<Object, Matcher<?>> entry : customMatching.entrySet()) {
            Matcher<?> matcher = entry.getValue();
            Object object = entry.getKey();
            if (!matcher.matches(object)) {
                appendFieldPath(matcher, mismatchDescription);
                matcher.describeMismatch(object, mismatchDescription);
                appendFieldJsonSnippet(object, mismatchDescription, gson);
                result = false;
            }
        }
        return result;
    }

    private void appendFieldJsonSnippet(Object actual, Description mismatchDescription, Gson gson) {
        JsonElement jsonTree = gson.toJsonTree(actual);
        if (!jsonTree.isJsonPrimitive() && !jsonTree.isJsonNull()) {
            mismatchDescription.appendText("\n" + gson.toJson(actual));
        }
    }

    private void appendFieldPath(Matcher<?> matcher, Description mismatchDescription) {
        for (Entry<String, Matcher<?>> entry : matcherConfiguration.getCustomMatchers().entrySet()) {
            if (entry.getValue().equals(matcher)) {
                mismatchDescription.appendText(entry.getKey()).appendText(" ");
            }
        }
    }

    @Override
    public JsonMatcher<T> skipCircularReferenceCheck(Function<Object, Boolean> matcher) {
        matcherConfiguration.addSkipCircularReferenceChecker(matcher);
        return this;
    }

    @SuppressWarnings({"unchecked", "varargs"})
    @Override
    public final JsonMatcher<T> skipCircularReferenceCheck(Function<Object, Boolean> matcher, Function<Object, Boolean>... matchers) {
        matcherConfiguration.addSkipCircularReferenceChecker(matcher);
        matcherConfiguration.addSkipCircularReferenceChecker(matchers);
        return this;
    }

    @Override
    public JsonMatcher<T> sortField(Matcher<String> fieldNamePattern) {
        matcherConfiguration.addPatternToSort(fieldNamePattern);
        return this;
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    @Override
    public final JsonMatcher<T> sortField(Matcher<String>... fieldNamePatterns) {
        matcherConfiguration.addPatternToSort(fieldNamePatterns);
        return this;
    }

    @Override
    public JsonMatcher<T> sortField(String fieldPath) {
        matcherConfiguration.addPathToSort(fieldPath);
        return this;
    }

    @Override
    public JsonMatcher<T> sortField(String... fieldPaths) {
        matcherConfiguration.addPathToSort(fieldPaths);
        return this;
    }
}
