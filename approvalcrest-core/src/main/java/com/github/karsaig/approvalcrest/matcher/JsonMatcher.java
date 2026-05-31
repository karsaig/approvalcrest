package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.JsonElementUtil;
import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
import com.github.karsaig.approvalcrest.matcher.file.AbstractDiagnosingFileMatcher;
import com.github.karsaig.approvalcrest.matcher.file.FileStoreMatcherUtils;
import com.github.karsaig.approvalcrest.matcher.machinereadable.AliasTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker.Reason;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import com.google.gson.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.*;
import java.util.function.Function;

import static com.github.karsaig.approvalcrest.CyclicReferenceDetector.getClassesWithCircularReferences;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

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

    private final MatcherConfiguration matcherConfiguration = new MatcherConfiguration();
    private final Set<Class<?>> circularReferenceTypes = new HashSet<>();
    private Either expected;

    private GsonConfiguration configuration;

    public JsonMatcher(TestMetaInformation testMetaInformation, FileMatcherConfig fileMatcherConfig) {
        super(testMetaInformation, fileMatcherConfig, new FileStoreMatcherUtils("json", fileMatcherConfig));
    }

    @Override
    public void describeTo(Description description) {
        Gson gson = GsonProvider.gson(matcherConfiguration, circularReferenceTypes, configuration);
        if (expected.isParsedJson()) {
            description.appendText(filterJson(gson, expected.getParsedContent(), true, false, false, null, null));
        } else {
            description.appendText(expected.getOriginalContent());
        }
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
    public <V> JsonMatcher<T> withMatcher(Matcher<String> fieldNamePattern, Matcher<V> matcher) {
        matcherConfiguration.addCustomMatcherPattern(fieldNamePattern, matcher);
        return this;
    }

    @Override
    public JsonMatcher<T> withGsonConfiguration(GsonConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    protected boolean doMatches(Object actual, Description mismatchDescription) {
        boolean matches = false;
        circularReferenceTypes.addAll(getClassesWithCircularReferences(actual, matcherConfiguration));
        init();
        Gson gson = GsonProvider.gson(matcherConfiguration, circularReferenceTypes, configuration);
        if (createNotApprovedFileIfNotExists(actual, gson) && fileMatcherConfig.isPassOnCreateEnabled()) {
            return true;
        }
        initExpectedFromFile();

        JsonElement actualJsonElement = getAsJsonElement(gson, actual);

        if (areCustomMatchersMatchingBeanOrJson(actual, actualJsonElement, mismatchDescription, gson, matcherConfiguration)) {

            IgnoredFieldsTracker ignoredTracker = machineReadableOutput ? new IgnoredFieldsTracker() : null;
            AliasTracker aliasTracker = machineReadableOutput ? new AliasTracker() : null;
            String untrackedNote = buildUntrackedNote();

            String expectedJson = expected.getOriginalContent();
            if (expected.isParsedJson()) {
                expectedJson = filterJson(gson, expected.getParsedContent(), fileMatcherConfig.isSortInputFile(), fileMatcherConfig.isStrictFileMatching(), fileMatcherConfig.isStrictFileMatching(), ignoredTracker, aliasTracker);
            }

            if (actual == null) {
                matches = appendMismatchDescriptionWithNote(mismatchDescription, expectedJson, "null", "actual was null",
                        ignoredTracker, aliasTracker, untrackedNote);
            } else {
                String actualJson = filterJson(gson, actualJsonElement, true, false, false, ignoredTracker, aliasTracker);

                matches = assertJsonEquals(expectedJson, actualJson, mismatchDescription, e -> getAssertMessage(fileStoreMatcherUtils, e),
                        ignoredTracker, aliasTracker, untrackedNote);
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
        expected = getExpectedFromFile(fileContent -> {
            try {
                return new Either(JsonParser.parseString(fileContent));
            } catch (Exception e) {
                return new Either(fileContent);
            }
        });
    }

    private String filterJson(Gson gson, JsonElement jsonElement, boolean sortFile, boolean skipIgnores, boolean skipCustomSortings,
                              IgnoredFieldsTracker ignoredTracker, AliasTracker aliasTracker) {
        Set<String> set = skipIgnores ? emptySet() : new HashSet<>(matcherConfiguration.getPathsToIgnore());

        Map<String, Reason> reasonMap = new HashMap<>();
        if (!skipIgnores) {
            for (String p : matcherConfiguration.getPathsToIgnore()) {
                reasonMap.put(p, Reason.IGNORE_PATH);
            }
            for (String p : matcherConfiguration.getCustomMatchers().keySet()) {
                reasonMap.put(p, Reason.CUSTOM_MATCHER);
                set.add(p);
            }
        }

        JsonElement filteredJson = findPaths(jsonElement, set, ignoredTracker, reasonMap);
        JsonElementUtil.filterByFieldMatchers(filteredJson, skipIgnores ? emptyList() : matcherConfiguration.getPatternsToIgnore(), ignoredTracker, Reason.IGNORE_PATTERN);
        if (!skipIgnores) {
            JsonElementUtil.filterByCustomMatcherPatterns(filteredJson, matcherConfiguration, ignoredTracker);
        }
        AliasMap aliasMap = matcherConfiguration.getAliasMap();
        if (!aliasMap.isEmpty()) {
            JsonElementUtil.applyAliases(filteredJson, aliasMap, aliasTracker);
        }
        applySorting(filteredJson, skipCustomSortings ? emptyMap() : matcherConfiguration.getPathsToSort(), skipCustomSortings ? emptyList() : matcherConfiguration.getPatternsToSort(), sortFile);

        return removeSetMarker(gson.toJson(filteredJson));
    }

    private String buildUntrackedNote() {
        return buildUntrackedNote(matcherConfiguration);
    }

    private boolean createNotApprovedFileIfNotExists(Object toApprove, Gson gson) {
        return createNotApprovedFileIfNotExists(toApprove, () -> serializeToJson(toApprove, gson));
    }

    private void overwriteApprovedFile(Object actual, Gson gson) {
        overwriteApprovedFile(actual, () -> serializeToJson(actual, gson));
    }

    private String serializeToJson(Object toApprove, Gson gson) {
        JsonElement actualJsonElement = getAsJsonElement(gson, toApprove);
        return filterJson(gson, actualJsonElement, true, false, false, null, null);
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

    @Override
    public JsonMatcher<T> sortFieldMatcher(SortField<Matcher<String>> fieldNamePattern) {
        matcherConfiguration.addPatternToSort(fieldNamePattern);
        return this;
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    @Override
    public final JsonMatcher<T> sortFieldMatcher(SortField<Matcher<String>>... fieldNamePatterns) {
        matcherConfiguration.addPatternToSort(fieldNamePatterns);
        return this;
    }

    @Override
    public JsonMatcher<T> sortFieldPath(SortField<String> fieldPath) {
        matcherConfiguration.addPathToSort(fieldPath);
        return this;
    }

    @SuppressWarnings({"varargs", "unchecked"})
    @SafeVarargs
    @Override
    public final JsonMatcher<T> sortFieldPath(SortField<String>... fieldPaths) {
        matcherConfiguration.addPathToSort(fieldPaths);
        return this;
    }

    @Override
    public JsonMatcher<T> sortType(Class<?>... types) {
        matcherConfiguration.addTypeToSort(types);
        return this;
    }

    @Override
    public JsonMatcher<T> withAliasMap(AliasMap aliasMap) {
        matcherConfiguration.addAliasMap(aliasMap);
        return this;
    }

    @Override
    public JsonMatcher<T> withAlias(String value, String alias) {
        matcherConfiguration.addAlias(value, alias);
        return this;
    }

    @Override
    public JsonMatcher<T> withAlias(String fieldName, String value, String alias) {
        matcherConfiguration.addAlias(fieldName, value, alias);
        return this;
    }

    @Override
    public JsonMatcher<T> withoutSerializingNulls() {
        matcherConfiguration.setSerializeNulls(false);
        return this;
    }

    @Override
    public String toString() {
        if (fileNameWithPath == null) {
            return "JsonMatcher";
        }
        return "JsonMatcher for " + fileStoreMatcherUtils.getApproved(fileNameWithPath,filenameWithRelativePath).getFileNameWithRelativePath();
    }

    private static class Either {
        private JsonElement parsedContent;
        private String originalContent;

        public Either(JsonElement parsedContent) {
            this.parsedContent = parsedContent;
            this.originalContent = null;
        }

        public Either(String originalContent) {
            this.originalContent = originalContent;
            this.parsedContent = null;
        }

        boolean isParsedJson() {
            return originalContent == null;
        }

        public JsonElement getParsedContent() {
            return parsedContent;
        }

        public String getOriginalContent() {
            return originalContent;
        }
    }
}
