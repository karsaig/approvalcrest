package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.AssertUtil.fail;
import static com.github.karsaig.approvalcrest.BeanFinder.findBeanAt;
import static com.github.karsaig.approvalcrest.CyclicReferenceDetector.getClassesWithCircularReferences;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.MARKER;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.findPaths;
import static com.github.karsaig.approvalcrest.matcher.FileStoreMatcherUtils.SEPARATOR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.github.karsaig.approvalcrest.ComparisonDescription;
import com.github.karsaig.approvalcrest.MatcherConfiguration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * <p>
 * Matcher for asserting expected DTOs. Searches for an approved JSON file in
 * the same directory as the test file:
 * <ul>
 * 		<li>If found, the matcher will assert the contents of the JSON file to the actual object,
 * which is serialized to a JSON String. </li>
 * 		<li>If not found, a non-approved JSON file is created, that must be
 * verified and renamed to "*-approved.json" by the developer. </li>
 * </ul>
 * The files and directories are hashed with SHA-1 algorithm by default to avoid too long file
 * and path names.
 * These are generated in the following way:
 * <ul>
 *   <li> the directory name is the first {@value #NUM_OF_HASH_CHARS} characters of the hashed <b>class name</b>. </li>
 *   <li> the file name is the first {@value #NUM_OF_HASH_CHARS} characters of the hashed <b>test method name</b>. </li>
 * </ul>
 * <p>
 * This default behaviour can be overridden by using the {@link #withFileName(String)} for
 * custom file name and {@link #withPathName(String)} for custom path.
 * </p>
 *
 * @author Andras_Gyuro
 */
public class JsonMatcher<T> extends DiagnosingMatcher<T> implements CustomisableMatcher<T>, ApprovedFileMatcher<JsonMatcher<T>> {

    private static final int NUM_OF_HASH_CHARS = 6;
    private static final String UPDATE_IN_PLACE_NAME = "jsonMatcherUpdateInPlace";
    private static final Pattern MARKER_PATTERN = Pattern.compile(MARKER);

    private String pathName;
    private String fileName;
    private String customFileName;
    private String fileNameWithPath;
    private String uniqueId;
    private TestMetaInformation testMetaInformation;
    private String testClassName;
    private String testMethodName;
    private String testClassNameHash;

    private MatcherConfiguration matcherConfiguration = new MatcherConfiguration();
    private final Set<Class<?>> circularReferenceTypes = new HashSet<>();
    private JsonElement expected;
    private FileStoreMatcherUtils fileStoreMatcherUtils = new FileStoreMatcherUtils(".json");

    private GsonConfiguration configuration;

    public JsonMatcher(TestMetaInformation testMetaInformation) {
        this.testMetaInformation = testMetaInformation;
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
    public JsonMatcher<T> withUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    @Override
    public JsonMatcher<T> withFileName(String customFileName) {
        this.customFileName = customFileName;
        return this;
    }

    @Override
    public JsonMatcher<T> withPathName(String pathName) {
        this.pathName = pathName;
        return this;
    }

    @Override
    protected boolean matches(Object actual, Description mismatchDescription) {
        boolean matches = false;
        circularReferenceTypes.addAll(getClassesWithCircularReferences(actual, matcherConfiguration));
        init();
        Gson gson = GsonProvider.gson(matcherConfiguration, circularReferenceTypes, configuration);
        createNotApprovedFileIfNotExists(actual, gson);
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
    public CustomisableMatcher<T> ignoring(String... fieldPaths) {
        matcherConfiguration.addPathToIgnore(fieldPaths);
        return this;
    }

    @Override
    public CustomisableMatcher<T> ignoring(Class<?>... clazzs) {
        matcherConfiguration.addTypeToIgnore(clazzs);
        return this;
    }

    private boolean handleInPlaceOverwrite(Object actual, Gson gson) {
        if ("true".equals(System.getProperty(UPDATE_IN_PLACE_NAME))) {
            overwriteApprovedFile(actual, gson);
            return true;
        }
        return false;
    }

    private void init() {
        testMethodName = testMetaInformation.testMethodName();
        testClassName = testMetaInformation.testClassName();

        if (customFileName == null || customFileName.trim().isEmpty()) {
            fileName = hashFileName(testMethodName);
        } else {
            fileName = customFileName;
        }
        if (uniqueId != null) {
            fileName += SEPARATOR + uniqueId;
        }
        if (pathName == null || pathName.trim().isEmpty()) {
            testClassNameHash = hashFileName(testClassName);
            pathName = testMetaInformation.getTestClassPath() + File.separator + testClassNameHash;
        }

        fileNameWithPath = pathName + File.separator + fileName;
    }

    private String hashFileName(String fileName) {
        return Hashing.sha1().hashString(fileName, Charsets.UTF_8).toString().substring(0, NUM_OF_HASH_CHARS);
    }

    private JsonElement getAsJsonElement(Gson gson, Object object) {
        JsonElement result;
        if (object instanceof String) {
            JsonParser jsonParser = new JsonParser();
            result = jsonParser.parse((String) object);
        } else {
            result = gson.toJsonTree(object);
        }
        return result;

    }

    private void initExpectedFromFile() {
        File approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);

        try {
            String approvedJsonStr = readFile(approvedFile);
            JsonParser jsonParser = new JsonParser();
            expected = jsonParser.parse(approvedJsonStr);
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Exception while initializing expected from file: %s", approvedFile.toString()), e);
        }
    }

    private String readFile(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
    }

    private String filterJson(Gson gson, JsonElement jsonElement) {
        Set<String> set = new HashSet<>();
        set.addAll(matcherConfiguration.getPathsToIgnore());

        JsonElement filteredJson = findPaths(jsonElement, set);

        return removeSetMarker(gson.toJson(filteredJson));
    }

    private boolean assertEquals(String expectedJson, String actualJson,
                                 Description mismatchDescription) {
        try {
            JSONAssert.assertEquals(expectedJson, actualJson, true);
        } catch (AssertionError e) {
            return appendMismatchDescription(mismatchDescription, expectedJson, actualJson, getAssertMessage(e));
        } catch (JSONException e) {
            return appendMismatchDescription(mismatchDescription, expectedJson, actualJson, getAssertMessage(e));
        }

        return true;
    }

    private String getAssertMessage(Throwable t) {
        String result;
        if (testClassNameHash == null) {
            result = "Expected file " + fileNameWithPath + "\n" + t.getMessage();
        } else {
            result = "Expected file " + testClassNameHash + File.separator + fileStoreMatcherUtils.getFullFileName(fileName, true)
                    + "\n" + t.getMessage();
        }
        return result;
    }

    private String removeSetMarker(String json) {
        return MARKER_PATTERN.matcher(json).replaceAll("");
    }

    private void createNotApprovedFileIfNotExists(Object toApprove, Gson gson) {
        File approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);
        if (!approvedFile.exists()) {
            try {
                String approvedFileName = approvedFile.getName();
                String content = serializeToJson(toApprove, gson);
                String createdFileName = fileStoreMatcherUtils.createNotApproved(fileNameWithPath, content, getCommentLine());
                String message;
                if (testClassNameHash == null) {
                    message = "Not approved file created: '" + createdFileName
                            + "'; please verify its contents and rename it to '" + approvedFileName + "'.";
                } else {
                    message = "Not approved file created: '" + testClassNameHash + File.separator + createdFileName
                            + "'; please verify its contents and rename it to '" + approvedFileName + "'.";
                }
                fail(message);

            } catch (IOException e) {
                throw new IllegalStateException(
                        String.format("Exception while creating not approved file %s", toApprove.toString()), e);
            }
        }
    }

    private void overwriteApprovedFile(Object actual, Gson gson) {
        File approvedFile = fileStoreMatcherUtils.getApproved(fileNameWithPath);
        if (approvedFile.exists()) {
            try {
                String content = serializeToJson(actual, gson);
                fileStoreMatcherUtils.overwriteApprovedFile(fileNameWithPath, content, getCommentLine());
            } catch (IOException e) {
                throw new IllegalStateException(
                        String.format("Exception while overwriting approved file %s", actual.toString()), e);
            }
        } else {
            throw new IllegalStateException("Approved file " + fileNameWithPath + " must exist in order to overwrite it! ");
        }
    }

    private String getCommentLine() {
        return testClassName + "." + testMethodName;
    }

    private String serializeToJson(Object toApprove, Gson gson) {
        String content;
        if (String.class.isInstance(toApprove)) {
            JsonParser jsonParser = new JsonParser();
            JsonElement toApproveJsonElement = jsonParser.parse(String.class.cast(toApprove));
            content = removeSetMarker(gson.toJson(toApproveJsonElement));
        } else {
            content = removeSetMarker(gson.toJson(toApprove));
        }
        return content;
    }

    private boolean appendMismatchDescription(Description mismatchDescription, String expectedJson,
                                              String actualJson, String message) {
        if (mismatchDescription instanceof ComparisonDescription) {
            ComparisonDescription shazamMismatchDescription = (ComparisonDescription) mismatchDescription;
            shazamMismatchDescription.setComparisonFailure(true);
            shazamMismatchDescription.setExpected(expectedJson);
            shazamMismatchDescription.setActual(actualJson);
            shazamMismatchDescription.setDifferencesMessage(message);
        }
        mismatchDescription.appendText(message);
        return false;
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

    @VisibleForTesting
    void setJsonMatcherUtils(FileStoreMatcherUtils jsonMatcherUtils) {
        this.fileStoreMatcherUtils = jsonMatcherUtils;
    }

    @Override
    public CustomisableMatcher<T> skipCircularReferenceCheck(Function<Object, Boolean> matcher) {
        matcherConfiguration.addSkipCircularReferenceChecker(matcher);
        return this;
    }

    @Override
    public CustomisableMatcher<T> skipCircularReferenceCheck(Function<Object, Boolean>... matchers) {
        matcherConfiguration.addSkipCircularReferenceChecker(matchers);
        return this;
    }
}
