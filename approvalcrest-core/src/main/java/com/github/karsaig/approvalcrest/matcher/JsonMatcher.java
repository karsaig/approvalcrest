package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.BeanFinder.findBeanAt;
import static com.github.karsaig.approvalcrest.CyclicReferenceDetector.getClassesWithCircularReferences;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.MARKER;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.findPaths;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.matcher.file.AbstractDiagnosingFileMatcher;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");

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
        description.appendText(filterJson(gson, expected));
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

            String expectedJson = filterJson(gson, expected);

            JsonElement actualJsonElement = getAsJsonElement(gson, actual);

            if (actual == null) {
                matches = appendMismatchDescription(mismatchDescription, expectedJson, "null", "actual was null");
            } else {
                String actualJson = filterJson(gson, actualJsonElement);

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

    private String filterJson(Gson gson, JsonElement jsonElement) {
        Set<String> set = new HashSet<>();
        set.addAll(matcherConfiguration.getPathsToIgnore());

        JsonElement filteredJson = findPaths(jsonElement, set);
        
        if (matcherConfiguration.stabilizeUUIDs()) {
            // Walk the tree to collect all the UUIDs we have in the order they appear 
            // Build a replacement map
            // Walk the tree again to make the replacements
            filteredJson = stabilizeUUIDs(filteredJson);
        }

        return removeSetMarker(gson.toJson(filteredJson));
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
        return MARKER_PATTERN.matcher(json).replaceAll("");
    }

    private boolean createNotApprovedFileIfNotExists(Object toApprove, Gson gson) {
        return createNotApprovedFileIfNotExists(toApprove, () -> serializeToJson(toApprove, gson));
    }

    private void overwriteApprovedFile(Object actual, Gson gson) {
        overwriteApprovedFile(actual, () -> serializeToJson(actual, gson));
    }

    private String serializeToJson(Object toApprove, Gson gson) {
        String content;
        if (String.class.isInstance(toApprove)) {
            JsonElement toApproveJsonElement = JsonParser.parseString(String.class.cast(toApprove));
            if (matcherConfiguration.stabilizeUUIDs()) {
                toApproveJsonElement = stabilizeUUIDs(toApproveJsonElement);
            }
            content = removeSetMarker(gson.toJson(toApproveJsonElement));
        } else {
            String json = gson.toJson(toApprove);
            if (matcherConfiguration.stabilizeUUIDs()) {
                json = gson.toJson(stabilizeUUIDs(JsonParser.parseString(json)));
            }
            content = removeSetMarker(json);
        }
        return content;
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


    /**
     * Replace UUID values with stable replacements. 
     * It's very common to use UUIDs as pointers in a JSON document.
     * 
     * Usually these are randomly generated, which makes comparing them to a static
     * file difficult.
     * 
     * This method replaces all UUIDs with a stable set, preserving them as pointers 
     * but also allowing them to be simply compared to static reference file.
     * 
     * @param filteredJson The original JsonElement to replace UUID values in
     * @return A JsonElement with UUIDs replaced with stable replacements.
     */
    private JsonElement stabilizeUUIDs(JsonElement filteredJson) {
        LinkedHashSet<UUID> uuids = findAllUUIDs(filteredJson);
        Stream<UUID> stableUUIDStream = IntStream
                .iterate(0, i -> i + 1)
                .mapToObj(i -> UUID.nameUUIDFromBytes(("" + i).getBytes()));
        Map<UUID, UUID> replacementUUIDs = Streams.zip(uuids.stream(), stableUUIDStream, Pair::of)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        filteredJson = replaceAllUUIDs(filteredJson, replacementUUIDs);
        return filteredJson;
    }

    /**
     * Returns an ordered set of all the UUIDs that appear in this JSON document.
     * That is, anything which is a string and matches UUID_PATTERN (keys and values)
     */
    private LinkedHashSet<UUID> findAllUUIDs(JsonElement jsonElement) {
        LinkedHashSet<UUID> hs = new LinkedHashSet<>();
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            if (primitive.isString()) {
                String string = primitive.getAsString();
                if (UUID_PATTERN.matcher(string).matches()) {
                    hs.add(UUID.fromString(string));
                    return hs;
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element: jsonArray) {
                hs.addAll(findAllUUIDs(element));
            }
        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            // TODO: MFA - is entrySet a stable order?
            for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                if (UUID_PATTERN.matcher(key).matches()) {
                    hs.add(UUID.fromString(key));
                }
                hs.addAll(findAllUUIDs(entry.getValue()));
            }
        }
        return hs;
    }

    /**
     * Replaces UUIDs in the given JSON document. 
     * Be careful that the order of operation is exacrlt the same as above.
     */
    private JsonElement replaceAllUUIDs(JsonElement jsonElement, Map<UUID, UUID> replacementUUIDs) {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            if (primitive.isString()) {
                String string = primitive.getAsString();
                if (UUID_PATTERN.matcher(string).matches()) {
                    UUID replacement = replacementUUIDs.get(UUID.fromString(string));
                    return new JsonPrimitive(replacement.toString());
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            JsonArray replacement = new JsonArray();
            for (int i=0; i<jsonArray.size(); i++) {
                replacement.add(replaceAllUUIDs(jsonArray.get(i), replacementUUIDs));
            }
            return replacement;
        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject replacement = new JsonObject();
            for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                if (UUID_PATTERN.matcher(key).matches()) {
                    key = replacementUUIDs.get(UUID.fromString(key)).toString();
                }
                JsonElement val = replaceAllUUIDs(entry.getValue(), replacementUUIDs);
                replacement.add(key, val);
            }
            return replacement;
        }
        return jsonElement;
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
    public JsonMatcher<T> stabilizeUUIDs() {
        matcherConfiguration.setStabilizeUUIDs();
        return this;
    }
}
