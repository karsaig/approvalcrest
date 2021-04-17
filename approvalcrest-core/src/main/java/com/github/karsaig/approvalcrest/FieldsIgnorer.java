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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.Math.max;
import static java.util.Arrays.asList;

/**
 * Responsible for traversing the Json tree and ignore the specified set of field paths.
 */
public class FieldsIgnorer {
    public static final String MARKER = "!_TO_BE_SORTED_!";
    private static final String PATH_SEPARATOR_PATTERN = Pattern.quote(".");

    public static JsonElement findPaths(Gson gson, Object object, Set<String> pathsToFind, List<Matcher<String>> fieldMatchersToSort, Set<String> pathsToSort) {
        JsonElement jsonElement = JsonParser.parseString(gson.toJson(object));

        JsonElement filteredJson = findPaths(jsonElement, pathsToFind);
        sortJsonFields(filteredJson, true);
        applySorting(filteredJson, pathsToSort, fieldMatchersToSort, true);
        if (object != null && (Set.class.isAssignableFrom(object.getClass()) || Map.class.isAssignableFrom(object.getClass()))) {
            //return sortArray(filteredJson);
            sortJsonArray(filteredJson.getAsJsonArray());
            return filteredJson;
        }
        return filteredJson;
    }

    public static JsonElement findPaths(JsonElement jsonElement, Set<String> pathsToFind) {
        if (jsonElement == null || pathsToFind.isEmpty()) {
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

    private static void findPath(JsonElement jsonElement, String pathToFind, List<String> pathSegments) {
        String field = headOf(pathSegments);

        if (jsonElement.isJsonArray()) {
            Iterator<JsonElement> iterator = jsonElement.getAsJsonArray().iterator();
            while (iterator.hasNext()) {
                JsonElement arrayElement = iterator.next();
                if (arrayElement.isJsonNull()) {
                    continue;
                }
                findPath(arrayElement, pathToFind, pathSegments);
            }
        } else {
            if (pathSegments.size() == 1) {
                ignorePath(jsonElement, pathToFind);
            } else {
                JsonElement child = jsonElement.getAsJsonObject().get(field);
                if (child == null) {
                    child = jsonElement.getAsJsonObject().get(MARKER + field);
                    if (child == null) {
                        return;
                    }
                    List<String> tail = pathSegments.subList(1, pathSegments.size());
                    findPath(child, pathToFind, tail);
                } else {
                    List<String> tail = pathSegments.subList(1, pathSegments.size());
                    findPath(child, pathToFind, tail);
                }
            }
        }
    }

    public static void applySorting(JsonElement jsonElement, Set<String> pathsToSort, List<Matcher<String>> fieldMatchersToSort, boolean sortFile) {
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
                        if (fieldNamePair.shouldSortDueToType() || anyPathMatch(fieldNamePair.newKey, pathMap, sortFile) || anyFieldMatcherMatches(fieldNamePair.newKey, fieldMatchersToSort, sortFile)) {
                            sortJsonArray(actualValue.getAsJsonArray());
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

    private static boolean anyPathMatch(String fieldName, Map<String, PathLevel> pathMap, boolean sortFile) {
        if (sortFile) {
            PathLevel nextLevelSet = pathMap.get(fieldName);
            return nextLevelSet != null && nextLevelSet.isMatchCurrentLevel();
        }
        return false;
    }

    private static Map<String, PathLevel> getPathsMap(Set<String> pathsToSort) {
        if (pathsToSort.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, PathLevel> result = new HashMap<>();
        for (String path : pathsToSort) {
            int indexOfNextLevel = path.indexOf(".");
            if (indexOfNextLevel < 0) {
                result.computeIfAbsent(path, k -> new PathLevel()).setMatchCurrentLevel();
            } else {
                result.computeIfAbsent(path.substring(0, indexOfNextLevel), k -> new PathLevel()).addNextLevel(path.substring(indexOfNextLevel + 1));
            }
        }
        return result;
    }

    private static class PathLevel {
        public static final PathLevel EMPTY = new PathLevel(false, Collections.emptySet());

        private boolean matchCurrentLevel;
        private final Set<String> nextLevel;


        public PathLevel() {
            this(false, new HashSet<>());
        }

        public PathLevel(boolean matchCurrentLevel, Set<String> nextLevel) {
            this.matchCurrentLevel = matchCurrentLevel;
            this.nextLevel = nextLevel;
        }

        public Set<String> getNextLevel() {
            return nextLevel;
        }

        public boolean addNextLevel(String input) {
            return nextLevel.add(input);
        }

        public void setMatchCurrentLevel() {
            this.matchCurrentLevel = true;
        }

        public boolean isMatchCurrentLevel() {
            return matchCurrentLevel;
        }
    }

    private static boolean anyFieldMatcherMatches(String fieldName, List<Matcher<String>> fieldMatchersToSort, boolean sortFile) {
        if (sortFile) {
            for (Matcher<String> matcher : fieldMatchersToSort) {
                if (matcher.matches(fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void sortJsonArray(JsonArray input) {
        List<SortElement> toSort = new ArrayList<>(input.size());
        Iterator<JsonElement> iter = input.iterator();
        while (iter.hasNext()) {
            JsonElement actual = iter.next();
            toSort.add(new SortElement(actual.toString(), actual));
            iter.remove();
        }
        Collections.sort(toSort);
        for (SortElement actual : toSort) {
            input.add(actual.original);
        }
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

    private static void ignorePath(JsonElement jsonElement, String pathToIgnore) {
        if (!jsonElement.isJsonNull()) {
            if (!jsonElement.isJsonObject()) {
                throw new IllegalArgumentException();
            }
            jsonElement.getAsJsonObject().remove(getLastSegmentOf(pathToIgnore));
            jsonElement.getAsJsonObject().remove(MARKER + getLastSegmentOf(pathToIgnore));
        }
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
