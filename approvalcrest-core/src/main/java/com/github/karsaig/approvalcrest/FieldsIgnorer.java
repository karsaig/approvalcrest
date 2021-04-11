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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static java.lang.Math.max;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Responsible for traversing the Json tree and ignore the specified set of field paths.
 */
public class FieldsIgnorer {
    public static final String MARKER = "!_TO_BE_SORTED_!";

    public static JsonElement findPaths(Gson gson, Object object, Set<String> pathsToFind) {
        JsonElement jsonElement = JsonParser.parseString(gson.toJson(object));

        JsonElement filteredJson = findPaths(jsonElement, pathsToFind);
        sortJsonFields(filteredJson);
        sortPaths(filteredJson);
        if (object != null && (Set.class.isAssignableFrom(object.getClass()) || Map.class.isAssignableFrom(object.getClass()))) {
            return sortArray(filteredJson);
        }
        return filteredJson;
    }

    public static JsonElement findPaths(JsonElement jsonElement, Set<String> pathsToFind) {
        if (jsonElement == null || pathsToFind.isEmpty()) {
            return jsonElement;
        }

        String pathToFind = headOf(pathsToFind);
        List<String> pathSegments = asList(pathToFind.split(Pattern.quote(".")));
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

                    child = sortArray(child);
                    jsonElement.getAsJsonObject().add(MARKER + field, child);
                } else {
                    List<String> tail = pathSegments.subList(1, pathSegments.size());
                    findPath(child, pathToFind, tail);
                }
            }
        }
    }

    private static JsonElement sortArray(JsonElement jsonElement) {
        TreeSet<JsonElement> orderedSet = newTreeSet(new Comparator<JsonElement>() {
            @Override
            public int compare(JsonElement o1, JsonElement o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        orderedSet.addAll(newArrayList(jsonElement.getAsJsonArray().iterator()));
        JsonArray jsonArray = new JsonArray();
        for (JsonElement element : orderedSet) {
            jsonArray.add(element);
        }
        return jsonArray;
    }

    public static void sortPaths(JsonElement jsonElement) {
        //TODO: add sort support
    }

    private static class KeyPair implements Comparable<KeyPair> {
        private final String originalKey;
        private final String newKey;

        public KeyPair(String originalKey, String newKey) {
            this.originalKey = originalKey;
            this.newKey = newKey;
        }

        public String getOriginalKey() {
            return originalKey;
        }

        public String getNewKey() {
            return newKey;
        }

        @Override
        public int compareTo(KeyPair keyPair) {
            return newKey.compareTo(keyPair.newKey);
        }
    }

    public static void sortJsonFields(JsonElement jsonElement) {
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                List<KeyPair> toSort = new ArrayList<>(jsonObject.size());
                for (String actual : jsonObject.keySet()) {
                    String key = actual;
                    if (key.startsWith(MARKER)) {
                        key = key.substring(MARKER.length());
                    }
                    toSort.add(new KeyPair(actual, key));
                }
                Collections.sort(toSort);
                for (KeyPair actual : toSort) {
                    JsonElement element = jsonObject.remove(actual.originalKey);
                    jsonObject.add(actual.newKey, element);
                }
                for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                    sortJsonFields(actual.getValue());
                }
            } else if (jsonElement.isJsonArray()) {
                Iterator<JsonElement> iter = jsonElement.getAsJsonArray().iterator();
                while (iter.hasNext()) {
                    JsonElement current = iter.next();
                    if (current.isJsonNull()) {
                        continue;
                    }
                    sortJsonFields(current);
                }
            }
        }
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
        String[] paths = fieldPath.split(Pattern.quote("."));
        if (paths.length == 0) {
            return fieldPath;
        }

        return paths[max(0, paths.length - 1)];
    }

    private static String headOf(Collection<String> paths) {
        return paths.iterator().next();
    }
}
