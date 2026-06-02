/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.SortedFieldsTracker;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import com.google.gson.*;
import org.hamcrest.Matcher;

import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Responsible for traversing the Json tree and ignore the specified set of field paths.
 */
public class FieldsIgnorer {
    public static final String MARKER = "!_TO_BE_SORTED_!";
    public static final Pattern MARKER_PATTERN = Pattern.compile(MARKER);
    private static final String PATH_SEPARATOR_PATTERN = Pattern.quote(".");

    public static String removeSetMarker(String json) {
        return MARKER_PATTERN.matcher(json).replaceAll("");
    }

    public static void applyRootCollectionSorting(JsonElement filteredJson, Object objectForTypeCheck, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort) {
        applyRootCollectionSorting(filteredJson, objectForTypeCheck, fieldMatchersToSort, pathsToSort, Collections.<Class<?>>emptyList(), null);
    }

    public static void applyRootCollectionSorting(JsonElement filteredJson, Object objectForTypeCheck, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort, Collection<Class<?>> typesToSort) {
        applyRootCollectionSorting(filteredJson, objectForTypeCheck, fieldMatchersToSort, pathsToSort, typesToSort, null);
    }

    public static void applyRootCollectionSorting(JsonElement filteredJson, Object objectForTypeCheck, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort, Collection<Class<?>> typesToSort, SortedFieldsTracker tracker) {
        if (objectForTypeCheck != null && (Set.class.isAssignableFrom(objectForTypeCheck.getClass()) || Map.class.isAssignableFrom(objectForTypeCheck.getClass()))) {
            // Sets and Maps are always sorted by their root representation (no meaningful order) — NOT tracked
            sortJsonArray(filteredJson.getAsJsonArray(), pathsToSort.getOrDefault("", emptyList()), fieldMatchersToSort);
        } else if (objectForTypeCheck != null && Collection.class.isAssignableFrom(objectForTypeCheck.getClass())) {
            // Other Collections (e.g. List) are sorted only when explicitly configured via "" path
            List<SortField<String>> rootSortFields = pathsToSort.getOrDefault("", emptyList());
            if (!rootSortFields.isEmpty() || !fieldMatchersToSort.isEmpty()) {
                sortJsonArray(filteredJson.getAsJsonArray(), rootSortFields, fieldMatchersToSort);
                if (tracker != null) {
                    recordSortMatches(tracker, "", rootSortFields, fieldMatchersToSort);
                }
            } else if (!typesToSort.isEmpty() && collectionElementMatchesTypesToSort((Collection<?>) objectForTypeCheck, typesToSort)) {
                // Type-based sorting — NOT tracked (same as type-based ignoring)
                sortJsonArray(filteredJson.getAsJsonArray(), emptyList(), fieldMatchersToSort);
            }
        }
    }

    public static JsonElement findPaths(JsonElement preComputedJson, Object objectForTypeCheck, Set<String> pathsToFind, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort) {
        JsonElement filteredJson = findPaths(preComputedJson, pathsToFind);
        applySorting(filteredJson, pathsToSort, fieldMatchersToSort, true);
        if (objectForTypeCheck != null && (Set.class.isAssignableFrom(objectForTypeCheck.getClass()) || Map.class.isAssignableFrom(objectForTypeCheck.getClass()))) {
            // Sets and Maps are always sorted by their root representation (no meaningful order)
            sortJsonArray(filteredJson.getAsJsonArray(), pathsToSort.getOrDefault("", emptyList()), fieldMatchersToSort);
        } else if (objectForTypeCheck != null && Collection.class.isAssignableFrom(objectForTypeCheck.getClass())) {
            // Other Collections (e.g. List) are sorted only when explicitly configured via "" path
            List<SortField<String>> rootSortFields = pathsToSort.getOrDefault("", emptyList());
            if (!rootSortFields.isEmpty() || !fieldMatchersToSort.isEmpty()) {
                sortJsonArray(filteredJson.getAsJsonArray(), rootSortFields, fieldMatchersToSort);
            }
        }
        return filteredJson;
    }

    public static JsonElement findPaths(Gson gson, Object object, Set<String> pathsToFind, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort) {
        JsonElement jsonElement = gson.toJsonTree(object);

        JsonElement filteredJson = findPaths(jsonElement, pathsToFind);
        applySorting(filteredJson, pathsToSort, fieldMatchersToSort, true);
        if (object != null && (Set.class.isAssignableFrom(object.getClass()) || Map.class.isAssignableFrom(object.getClass()))) {
            // Sets and Maps are always sorted by their root representation (no meaningful order)
            sortJsonArray(filteredJson.getAsJsonArray(), pathsToSort.getOrDefault("", emptyList()), fieldMatchersToSort);
            return filteredJson;
        } else if (object != null && Collection.class.isAssignableFrom(object.getClass())) {
            // Other Collections (e.g. List) are sorted only when explicitly configured via "" path
            List<SortField<String>> rootSortFields = pathsToSort.getOrDefault("", emptyList());
            if (!rootSortFields.isEmpty() || !fieldMatchersToSort.isEmpty()) {
                sortJsonArray(filteredJson.getAsJsonArray(), rootSortFields, fieldMatchersToSort);
            }
        }
        return filteredJson;
    }

    public static JsonElement findPaths(JsonElement jsonElement, Set<String> pathsToFind) {
        return findPaths(jsonElement, pathsToFind, null, null);
    }

    /**
     * Tracked version of findPaths. When tracker is non-null, records which fields were actually
     * removed and why. The reasonMap maps each path to its reason (IGNORE_PATH or CUSTOM_MATCHER).
     */
    public static JsonElement findPaths(JsonElement jsonElement, Set<String> pathsToFind,
                                         IgnoredFieldsTracker tracker,
                                         Map<String, IgnoredFieldsTracker.Reason> reasonMap) {
        if (jsonElement == null || jsonElement.isJsonNull() || pathsToFind.isEmpty()) {
            return jsonElement;
        }

        String pathToFind = headOf(pathsToFind);
        List<String> pathSegments = asList(pathToFind.split(PATH_SEPARATOR_PATTERN));
        try {
            boolean removed = findPath(jsonElement, pathToFind, pathSegments);
            if (removed && tracker != null && reasonMap != null) {
                IgnoredFieldsTracker.Reason reason = reasonMap.getOrDefault(pathToFind, IgnoredFieldsTracker.Reason.IGNORE_PATH);
                tracker.recordIgnored(pathToFind, reason);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(pathToFind + " does not exist", e);
        }
        return findPaths(jsonElement, removePathFromSet(pathsToFind, pathToFind), tracker, reasonMap);
    }

    private static Set<String> removePathFromSet(Set<String> setToRemoveFrom, String stringToRemove) {
        Set<String> set = new HashSet<>(setToRemoveFrom);
        set.remove(stringToRemove);
        return set;
    }

    private static boolean findPath(JsonElement jsonElement, String pathToFind, List<String> pathSegments) {
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            Iterator<JsonElement> iterator = jsonArray.iterator();
            boolean result = false;
            while (iterator.hasNext()) {
                JsonElement arrayElement = iterator.next();
                if (arrayElement.isJsonNull() || arrayElement.isJsonPrimitive()) {
                    continue;
                }
                boolean ignoredElement = findPath(arrayElement, pathToFind, pathSegments);
                if (ignoredElement && JsonElementUtil.isEmpty(arrayElement)) {
                    iterator.remove();
                    result |= true;
                }
            }
            // If non-primitive elements were removed and only primitives/nulls remain,
            // those are orphaned map values whose complex key was entirely stripped by
            // ignoring.  Clear them so the inner array becomes empty and the outer
            // loop's existing isEmpty check can remove the whole entry.
            if (result) {
                boolean hasNonPrimitive = false;
                for (JsonElement remaining : jsonArray) {
                    if (!remaining.isJsonNull() && !remaining.isJsonPrimitive()) {
                        hasNonPrimitive = true;
                        break;
                    }
                }
                if (!hasNonPrimitive) {
                    Iterator<JsonElement> cleanup = jsonArray.iterator();
                    while (cleanup.hasNext()) {
                        cleanup.next();
                        cleanup.remove();
                    }
                }
            }
            return result;
        } else {
            String field = headOf(pathSegments);
            if (pathSegments.size() == 1) {
                return ignorePath(jsonElement, pathToFind);
            } else {
                if (jsonElement.isJsonObject()) {
                    JsonObject jo = jsonElement.getAsJsonObject();
                    JsonElement child = jo.get(field);
                    if (child == null) {
                        child = jo.get(MARKER + field);
                        if (child == null) {
                            // Try descending through GraphAdapter envelope keys
                            for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                                if (isGraphAdapterKey(entry.getKey()) && entry.getValue().isJsonObject()) {
                                    boolean changed = findPath(entry.getValue(), pathToFind, pathSegments);
                                    if (changed) {
                                        if (JsonElementUtil.isEmpty(entry.getValue())) {
                                            jo.remove(entry.getKey());
                                        }
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                        List<String> tail = pathSegments.subList(1, pathSegments.size());
                        boolean changed = findPath(child, pathToFind, tail);
                        if (changed && JsonElementUtil.isEmpty(child)) {
                            jo.remove(field);
                            return true;
                        }
                    } else {
                        List<String> tail = pathSegments.subList(1, pathSegments.size());
                        boolean changed = findPath(child, pathToFind, tail);
                        if (changed && JsonElementUtil.isEmpty(child)) {
                            jo.remove(field);
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }

    public static void applySorting(JsonElement jsonElement, Map<String, List<SortField<String>>> pathsToSort, List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile) {
        applySorting(jsonElement, pathsToSort, fieldMatchersToSort, sortFile, null);
    }

    public static void applySorting(JsonElement jsonElement, Map<String, List<SortField<String>>> pathsToSort, List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile, SortedFieldsTracker tracker) {
        if (jsonElement == null || jsonElement.isJsonNull()) return;
        Map<String, PathLevel> pathMap = pathsToSort.isEmpty() ? Collections.emptyMap() : getPathsMap(pathsToSort);
        applySortingInternal(jsonElement, pathMap, pathsToSort, fieldMatchersToSort, sortFile, tracker, "");
    }

    private static void applySortingInternal(JsonElement jsonElement, Map<String, PathLevel> pathMap,
            Map<String, List<SortField<String>>> pathsToSort,
            List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile,
            SortedFieldsTracker tracker, String currentPath) {
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                // Sort object keys inline (#5: replaces separate sortJsonFields pass)
                if (sortFile) {
                    List<FieldNamePair> toSort = getFiledNamePairs(jsonObject);
                    Collections.sort(toSort);
                    for (FieldNamePair pair : toSort) {
                        JsonElement element = jsonObject.remove(pair.originalKey);
                        jsonObject.add(pair.originalKey, element);
                    }
                }
                for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                    JsonElement actualValue = actual.getValue();
                    if (actualValue.isJsonNull() || actualValue.isJsonPrimitive()) {
                        continue;
                    }
                    FieldNamePair fieldNamePair = convertToKeyPair(actual.getKey());
                    if (isGraphAdapterKey(fieldNamePair.newKey)) {
                        // Transparent: descend with same pathMap, don't consume a level
                        applySortingInternal(actualValue, pathMap, pathsToSort, fieldMatchersToSort, sortFile, tracker, currentPath);
                        continue;
                    }
                    PathLevel pathLevel = pathMap.getOrDefault(fieldNamePair.newKey, PathLevel.EMPTY);
                    Map<String, PathLevel> nextPathMap = pathLevel.nextLevel.isEmpty()
                            ? Collections.emptyMap() : getPathsMap(pathLevel.nextLevel);
                    String childPath = currentPath.isEmpty() ? fieldNamePair.newKey : currentPath + "." + fieldNamePair.newKey;
                    applySortingInternal(actualValue, nextPathMap, pathLevel.nextLevel, fieldMatchersToSort, sortFile, tracker, childPath);
                    if (actualValue.isJsonArray()) {
                        List<SortField<String>> matchingPathMatchers = anyPathMatch(fieldNamePair.newKey, pathMap, sortFile);
                        List<SortField<Matcher<String>>> matchingFieldMatchers = anyFieldMatcherMatches(fieldNamePair.newKey, fieldMatchersToSort, sortFile);
                        if (fieldNamePair.shouldSortDueToType() || !matchingPathMatchers.isEmpty() || !matchingFieldMatchers.isEmpty()) {
                            sortJsonArray(actualValue.getAsJsonArray(), matchingPathMatchers, matchingFieldMatchers);
                            if (tracker != null) {
                                recordSortMatches(tracker, childPath, matchingPathMatchers, matchingFieldMatchers);
                            }
                        }
                    }
                }
            } else if (jsonElement.isJsonArray()) {
                // Recurse into each element first so nested arrays are sorted before
                // computing the root sort key (bottom-up ordering).
                Map<String, List<SortField<String>>> innerPathsToSort;
                if (pathsToSort.containsKey("")) {
                    innerPathsToSort = new HashMap<>(pathsToSort);
                    innerPathsToSort.remove("");
                } else {
                    innerPathsToSort = pathsToSort;
                }
                // #3: Build the PathLevel map once for all elements in this array
                Map<String, PathLevel> innerPathMap;
                if (innerPathsToSort == pathsToSort) {
                    innerPathMap = pathMap; // "" was not in map; same pathsToSort, reuse existing map
                } else if (innerPathsToSort.isEmpty()) {
                    innerPathMap = Collections.emptyMap();
                } else {
                    innerPathMap = getPathsMap(innerPathsToSort);
                }
                Iterator<JsonElement> iter = jsonElement.getAsJsonArray().iterator();
                while (iter.hasNext()) {
                    JsonElement current = iter.next();
                    if (current.isJsonNull() || current.isJsonPrimitive()) {
                        continue;
                    }
                    applySortingInternal(current, innerPathMap, innerPathsToSort, fieldMatchersToSort, sortFile, tracker, currentPath);
                }
                // Sort the array itself last (root), so the sort key reflects the
                // already-sorted state of nested elements.
                List<SortField<String>> rootSortFields = pathsToSort.getOrDefault("", emptyList());
                List<SortField<Matcher<String>>> rootFieldMatchers = anyFieldMatcherMatches("", fieldMatchersToSort, sortFile);
                if (!rootSortFields.isEmpty() || !rootFieldMatchers.isEmpty()) {
                    sortJsonArray(jsonElement.getAsJsonArray(), rootSortFields, rootFieldMatchers);
                    if (tracker != null) {
                        recordSortMatches(tracker, currentPath, rootSortFields, rootFieldMatchers);
                    }
                }
            }
        }
    }

    private static void recordSortMatches(SortedFieldsTracker tracker, String path,
            List<SortField<String>> matchingPathMatchers,
            List<SortField<Matcher<String>>> matchingFieldMatchers) {
        if (!matchingPathMatchers.isEmpty()) {
            tracker.recordSortedByPath(path);
        }
        for (SortField<Matcher<String>> fm : matchingFieldMatchers) {
            tracker.recordSortedByPattern(path, fm.getSortFieldSelector().toString());
        }
    }

    private static List<SortField<String>> anyPathMatch(String fieldName, Map<String, PathLevel> pathMap, boolean sortFile) {
        if (sortFile) {
            PathLevel nextLevelSet = pathMap.get(fieldName);
            if(nextLevelSet != null){
                return nextLevelSet.currentLevel;
            }
        }
        return emptyList();
    }

    private static Map<String, PathLevel> getPathsMap(Map<String, List<SortField<String>>> pathsToSort) {
        if (pathsToSort.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, PathLevel> result = new HashMap<>();
        for (Map.Entry<String,List<SortField<String>>> pathEntry : pathsToSort.entrySet()) {
            String path = pathEntry.getKey();
            int indexOfNextLevel = path.indexOf(".");
            if (indexOfNextLevel < 0) {
                result.computeIfAbsent(path, k -> new PathLevel()).addCurrentLevel(pathEntry.getValue());
            } else {
                result.computeIfAbsent(path.substring(0, indexOfNextLevel), k -> new PathLevel()).addNextLevel(path.substring(indexOfNextLevel + 1), pathEntry.getValue());
            }
        }
        return result;
    }

    private static Map<String, PathLevel> getPathsMap(List<SortField<String>> pathsToSort) {
        if (pathsToSort.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, PathLevel> result = new HashMap<>();
        for (SortField<String> pathEntry : pathsToSort) {
            for(String path : pathEntry.getIgnoredPathsForSorting()){
                int indexOfNextLevel = path.indexOf(".");
                if (indexOfNextLevel < 0) {
                    result.computeIfAbsent(path, k -> new PathLevel()).addCurrentLevel(SortField.of(path,emptyList(),pathEntry.getIgnoredFieldMatchersForSorting()));
                } else {
                    String currentLevelKey = path.substring(0, indexOfNextLevel);
                    String nextLevel = path.substring(indexOfNextLevel + 1);
                    result.computeIfAbsent(currentLevelKey, k -> new PathLevel()).addNextLevel(nextLevel, SortField.of(nextLevel,Collections.singletonList(nextLevel),pathEntry.getIgnoredFieldMatchersForSorting()));
                }
            }
        }
        return result;
    }

    private static class PathLevel {
        public static final PathLevel EMPTY = new PathLevel(emptyList(), Collections.emptyMap());

        private final List<SortField<String>> currentLevel;
        private final Map<String, List<SortField<String>>> nextLevel;


        public PathLevel() {
            this(new ArrayList<>(), new HashMap<>());
        }

        public PathLevel(List<SortField<String>> currentLevel, Map<String, List<SortField<String>>> nextLevel) {
            this.currentLevel = currentLevel;
            this.nextLevel = nextLevel;
        }

        public Map<String, List<SortField<String>>> getNextLevel() {
            return nextLevel;
        }

        public void addNextLevel(String input, List<SortField<String>> sortFields) {
            nextLevel.computeIfAbsent(input, k -> new ArrayList<>()).addAll(sortFields);
        }

        public void addNextLevel(String input, SortField<String> sortField) {
            nextLevel.computeIfAbsent(input, k -> new ArrayList<>()).add(sortField);
        }

        public void addCurrentLevel(List<SortField<String>> sortFields) {
            currentLevel.addAll(sortFields);
        }

        public void addCurrentLevel(SortField<String> sortField) {
            currentLevel.add(sortField);
        }

        public boolean isLastPath(){
            return nextLevel.isEmpty();
        }
    }

    private static List<SortField<Matcher<String>>> anyFieldMatcherMatches(String fieldName, List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile) {
        if (sortFile) {
            List<SortField<Matcher<String>>> result = new ArrayList<>();
            for (SortField<Matcher<String>> matcher : fieldMatchersToSort) {
                if (matcher.getSortFieldSelector().matches(fieldName)) {
                    result.add(matcher);
                }
            }
            return result;
        }
        return emptyList();
    }

    private static void sortJsonArray(JsonArray input, List<SortField<String>> matchingPathMatchers, List<SortField<Matcher<String>>> matchingFieldMatchers) {
        List<SortElement> toSort = new ArrayList<>(input.size());
        Iterator<JsonElement> iter = input.iterator();
        while (iter.hasNext()) {
            JsonElement actual = iter.next();
            // When a direct element is itself an array (e.g. List<List<Bean>>), apply
            // the configured sort to it before computing its sort key. The sort was
            // configured for this collection; direct array elements are part of that
            // same collection and fan-out applies.
            // Elements that are objects (beans) are NOT sorted here; only their fields
            // that were explicitly configured via sortField will be sorted via applySorting.
            if (actual.isJsonArray()) {
                sortJsonArray(actual.getAsJsonArray(), matchingPathMatchers, matchingFieldMatchers);
            }
            toSort.add(new SortElement(getFilteredStringForSorting(actual, matchingPathMatchers, matchingFieldMatchers).toString(), actual));
            iter.remove();
        }
        Collections.sort(toSort);
        for (SortElement actual : toSort) {
            input.add(actual.original);
        }
    }

    private static JsonElement getFilteredStringForSorting(JsonElement jsonElement, List<SortField<String>> pathMatchers, List<SortField<Matcher<String>>> fieldMatchers) {
        if (areAllMatchersEmpty(pathMatchers, fieldMatchers)) {
            return jsonElement;
        }

        if (jsonElement.isJsonObject()) {
            JsonObject jsonForSort = new JsonObject();

            // Merge ignored paths/matchers from fieldMatchers into the path map so that
            // SortField<Matcher<String>>.ignoring(String) and .ignoring(Matcher) work for
            // fields inside elements, not just path-based SortField<String>.
            List<SortField<String>> combinedPaths = new ArrayList<>(pathMatchers);
            List<Matcher<String>> innerIgnoredFieldMatchers = new ArrayList<>();
            for (SortField<Matcher<String>> fm : fieldMatchers) {
                for (String ignoredPath : fm.getIgnoredPathsForSorting()) {
                    combinedPaths.add(SortField.of(ignoredPath, Collections.singletonList(ignoredPath), Collections.emptyList()));
                }
                innerIgnoredFieldMatchers.addAll(fm.getIgnoredFieldMatchersForSorting());
            }
            Map<String, PathLevel> pathMap = getPathsMap(combinedPaths);

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                JsonElement actualValue = actual.getValue();
                String actualKey = actual.getKey();
                FieldNamePair fieldNamePair = convertToKeyPair(actualKey);

                if (isGraphAdapterKey(fieldNamePair.newKey)) {
                    // Transparent: recurse with same pathMatchers/fieldMatchers
                    jsonForSort.add(actualKey, getFilteredStringForSorting(actualValue, pathMatchers, fieldMatchers));
                    continue;
                }

                PathLevel matchingPath = pathMap.getOrDefault(fieldNamePair.newKey, PathLevel.EMPTY);

                List<SortField<Matcher<String>>> matchingFieldMatchers = anyFieldMatcherMatches(fieldNamePair.newKey, fieldMatchers, true);
                List<SortField<String>> matchingPathMatchers = anyPathMatch(fieldNamePair.newKey, pathMap, true);
                boolean matchedByInnerFieldMatcher = anyInnerMatcherMatches(fieldNamePair.newKey, innerIgnoredFieldMatchers);

                if (!matchingPathMatchers.isEmpty() || !matchingFieldMatchers.isEmpty() || matchedByInnerFieldMatcher) {
                    // leaf match — strip this field entirely from the sort key (primitive or complex)
                } else if (actualValue.isJsonNull() || actualValue.isJsonPrimitive()) {
                    jsonForSort.add(actualKey, actualValue);
                } else {
                    List<SortField<String>> nextLevelSortFields = new ArrayList<>();
                    for (List<SortField<String>> sortFields : matchingPath.nextLevel.values()) {
                        nextLevelSortFields.addAll(sortFields);
                    }
                    jsonForSort.add(actualKey, getFilteredStringForSorting(actualValue, nextLevelSortFields, fieldMatchers));
                }
            }

            return jsonForSort;
        } else if (jsonElement.isJsonArray()) {
            JsonArray actualArray = jsonElement.getAsJsonArray();
            JsonArray jsonForSort = new JsonArray(actualArray.size());
            for (JsonElement current : actualArray) {
                if (current.isJsonNull() || current.isJsonPrimitive()) {
                    jsonForSort.add(current);
                } else {
                    jsonForSort.add(getFilteredStringForSorting(current, pathMatchers, fieldMatchers));
                }
            }
            return jsonForSort;
        }

        return jsonElement;
    }

    private static boolean anyInnerMatcherMatches(String fieldName, List<Matcher<String>> matchers) {
        for (Matcher<String> matcher : matchers) {
            if (matcher.matches(fieldName)) {
                return true;
            }
        }
        return false;
    }

    static boolean isGraphAdapterKey(String key) {
        if (key == null || !key.startsWith("0x") || key.length() <= 2) return false;
        for (int i = 2; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) return false;
        }
        return true;
    }



    private static boolean areAllMatchersEmpty(List<SortField<String>> matchingPathMatchers, List<SortField<Matcher<String>>> matchingFieldMatchers){
        if(matchingPathMatchers.isEmpty() && matchingFieldMatchers.isEmpty()){
            return true;
        }
        for(SortField<String> matchingPathMatcher : matchingPathMatchers) {
            if(!matchingPathMatcher.isEmpty()){
                return false;
            }
        }
        for(SortField<Matcher<String>> matchingFieldMatcher : matchingFieldMatchers) {
            if(!matchingFieldMatcher.isEmpty()){
                return false;
            }
        }
        return true;
    }

    private static class SortElement implements Comparable<SortElement> {
        private final String value;
        private final JsonElement original;

        public SortElement(String value, JsonElement original) {
            this.value = value;
            this.original = original;
        }

        public JsonElement getOriginal() {
            return original;
        }

        @Override
        public int compareTo(SortElement o) {
            return value.compareTo(o.value);
        }
    }

    private static class FieldNamePair implements Comparable<FieldNamePair> {
        private final String originalKey;
        private final String newKey;

        public FieldNamePair(String originalKey, String newKey) {
            this.originalKey = originalKey;
            this.newKey = newKey;
        }

        public String getOriginalKey() {
            return originalKey;
        }

        public String getNewKey() {
            return newKey;
        }

        public boolean shouldSortDueToType() {
            return !originalKey.equals(newKey);
        }

        @Override
        public int compareTo(FieldNamePair keyPair) {
            return newKey.compareTo(keyPair.newKey);
        }
    }

    public static void sortJsonFields(JsonElement jsonElement, boolean sortFile) {
        if (sortFile) {
            if (jsonElement != null && !jsonElement.isJsonNull()) {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    List<FieldNamePair> toSort = getFiledNamePairs(jsonObject);
                    Collections.sort(toSort);
                    for (FieldNamePair actual : toSort) {
                        JsonElement element = jsonObject.remove(actual.originalKey);
                        jsonObject.add(actual.originalKey, element);
                    }
                    for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                        sortJsonFields(actual.getValue(), sortFile);
                    }
                } else if (jsonElement.isJsonArray()) {
                    Iterator<JsonElement> iter = jsonElement.getAsJsonArray().iterator();
                    while (iter.hasNext()) {
                        JsonElement current = iter.next();
                        if (current.isJsonNull()) {
                            continue;
                        }
                        sortJsonFields(current, sortFile);
                    }
                }
            }
        }
    }

    private static List<FieldNamePair> getFiledNamePairs(JsonObject input) {
        List<FieldNamePair> result = new ArrayList<>(input.size());
        for (String actual : input.keySet()) {
            result.add(convertToKeyPair(actual));
        }
        return result;
    }

    private static FieldNamePair convertToKeyPair(String input) {
        return new FieldNamePair(input, getOriginalFieldName(input));
    }

    private static String getOriginalFieldName(String input) {
        String result = input;
        if (result.startsWith(MARKER)) {
            result = result.substring(MARKER.length());
        }
        return result;
    }

    private static boolean ignorePath(JsonElement jsonElement, String pathToIgnore) {
        if (!jsonElement.isJsonNull()) {
            if (!jsonElement.isJsonObject()) {
                throw new IllegalArgumentException();
            }
            JsonObject jo = jsonElement.getAsJsonObject();
            String lastSegment = getLastSegmentOf(pathToIgnore);
            boolean removedElement = jo.remove(lastSegment) != null;
            removedElement |= jo.remove(MARKER + lastSegment) != null;
            if (!removedElement) {
                // Try descending through GraphAdapter envelope keys
                for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                    if (isGraphAdapterKey(entry.getKey()) && entry.getValue().isJsonObject()) {
                        JsonObject inner = entry.getValue().getAsJsonObject();
                        boolean innerRemoved = inner.remove(lastSegment) != null;
                        innerRemoved |= inner.remove(MARKER + lastSegment) != null;
                        removedElement |= innerRemoved;
                    }
                }
            }
            return removedElement;
        }
        return false;
    }

    private static String getLastSegmentOf(String fieldPath) {
        String[] paths = fieldPath.split(PATH_SEPARATOR_PATTERN);
        if (paths.length == 0) {
            return fieldPath;
        }

        return paths[max(0, paths.length - 1)];
    }

    private static String headOf(Collection<String> paths) {
        return paths.iterator().next();
    }

    private static boolean collectionElementMatchesTypesToSort(Collection<?> collection, Collection<Class<?>> typesToSort) {
        for (Object element : collection) {
            if (element != null) {
                for (Class<?> type : typesToSort) {
                    if (type.isInstance(element)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
