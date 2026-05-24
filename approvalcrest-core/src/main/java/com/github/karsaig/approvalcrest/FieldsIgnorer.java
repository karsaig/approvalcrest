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
    private static final String PATH_SEPARATOR_PATTERN = Pattern.quote(".");

    public static JsonElement findPaths(JsonElement preComputedJson, Object objectForTypeCheck, Set<String> pathsToFind, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort) {
        JsonElement filteredJson = findPaths(preComputedJson, pathsToFind);
        sortJsonFields(filteredJson, true);
        applySorting(filteredJson, pathsToSort, fieldMatchersToSort, true);
        if (objectForTypeCheck != null && (Set.class.isAssignableFrom(objectForTypeCheck.getClass()) || Map.class.isAssignableFrom(objectForTypeCheck.getClass()))) {
            sortJsonArray(filteredJson.getAsJsonArray(), pathsToSort.getOrDefault("", emptyList()), fieldMatchersToSort);
        }
        return filteredJson;
    }

    public static JsonElement findPaths(Gson gson, Object object, Set<String> pathsToFind, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort) {
        JsonElement jsonElement = JsonParser.parseString(gson.toJson(object));

        JsonElement filteredJson = findPaths(jsonElement, pathsToFind);
        sortJsonFields(filteredJson, true);
        applySorting(filteredJson, pathsToSort, fieldMatchersToSort, true);
        if (object != null && (Set.class.isAssignableFrom(object.getClass()) || Map.class.isAssignableFrom(object.getClass()))) {
            sortJsonArray(filteredJson.getAsJsonArray(),pathsToSort.getOrDefault("", emptyList()), fieldMatchersToSort);
            return filteredJson;
        }
        return filteredJson;
    }

    public static JsonElement findPaths(JsonElement jsonElement, Set<String> pathsToFind) {
        if (jsonElement == null || jsonElement.isJsonNull() || pathsToFind.isEmpty()) {
            return jsonElement;
        }

        String pathToFind = headOf(pathsToFind);
        List<String> pathSegments = asList(pathToFind.split(PATH_SEPARATOR_PATTERN));
        try {
            findPath(jsonElement, pathToFind, pathSegments);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(pathToFind + " does not exist", e);
        }
        return findPaths(jsonElement, removePathFromSet(pathsToFind, pathToFind));
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
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            if (jsonElement.isJsonObject()) {
                Map<String, PathLevel> pathMap = getPathsMap(pathsToSort);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                    JsonElement actualValue = actual.getValue();
                    if (actualValue.isJsonNull() || actualValue.isJsonPrimitive()) {
                        continue;
                    }
                    FieldNamePair fieldNamePair = convertToKeyPair(actual.getKey());
                    PathLevel pathLevel = pathMap.getOrDefault(fieldNamePair.newKey, PathLevel.EMPTY);
                    applySorting(actualValue, pathLevel.nextLevel, fieldMatchersToSort, sortFile);
                    if (actualValue.isJsonArray()) {
                        List<SortField<String>> matchingPathMatchers = anyPathMatch(fieldNamePair.newKey, pathMap, sortFile);
                        List<SortField<Matcher<String>>> matchingFieldMatchers = anyFieldMatcherMatches(fieldNamePair.newKey, fieldMatchersToSort, sortFile);
                        if (fieldNamePair.shouldSortDueToType() || !matchingPathMatchers.isEmpty() || !matchingFieldMatchers.isEmpty()) {
                            sortJsonArray(actualValue.getAsJsonArray(),matchingPathMatchers,matchingFieldMatchers);
                        }
                    }
                }
            } else if (jsonElement.isJsonArray()) {
                Iterator<JsonElement> iter = jsonElement.getAsJsonArray().iterator();
                while (iter.hasNext()) {
                    JsonElement current = iter.next();
                    if (current.isJsonNull() || current.isJsonPrimitive()) {
                        continue;
                    }
                    applySorting(current, pathsToSort, fieldMatchersToSort, sortFile);
                }
            }
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
            boolean removedElement = jsonElement.getAsJsonObject().remove(getLastSegmentOf(pathToIgnore)) != null;
            removedElement |= jsonElement.getAsJsonObject().remove(MARKER + getLastSegmentOf(pathToIgnore)) != null;
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
}
