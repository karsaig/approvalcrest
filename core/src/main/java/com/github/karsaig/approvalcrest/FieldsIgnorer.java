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

import static com.github.karsaig.json.JsonFactoryProvider.jsonFactory;
import static java.lang.Math.max;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.github.karsaig.json.Json;
import com.github.karsaig.json.JsonArray;
import com.github.karsaig.json.JsonElement;
import com.github.karsaig.json.JsonParser;
import com.github.karsaig.json.JsonParserFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Responsible for traversing the Json tree and ignore the specified set of field paths.
 */
public class FieldsIgnorer {
    public static final String MARKER = "!_TO_BE_SORTED_!";

    public static JsonElement findPaths(Json json, Object object, Set<String> pathsToFind) {
        JsonParser jsonParser = JsonParserFactory.jsonParser();
        JsonElement jsonElement = jsonParser.parse(json.toJson(object));

        JsonElement filteredJson = findPaths(jsonElement, pathsToFind);
        if (object != null && (Set.class.isAssignableFrom(object.getClass()) || Map.class.isAssignableFrom(object.getClass()))) {
            return sortArray(filteredJson);
        }
        return filteredJson;
    }

    public static JsonElement findPaths(JsonElement jsonElement, Set<String> pathsToFind) {
        if (pathsToFind.isEmpty()) {
            return jsonElement;
        }

        String pathToFind = headOf(pathsToFind);
        List<String> pathSegments = asList(pathToFind.split(Pattern.quote(".")));
        try {
            findPath(jsonElement, pathToFind, pathSegments);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(pathToFind + " does not exist");
        }
        return findPaths(jsonElement, removePathFromSet(pathsToFind, pathToFind));
    }

    private static Set<String> removePathFromSet(Set<String> setToRemoveFrom, String stringToRemove) {
        Set<String> set = new HashSet<String>(setToRemoveFrom);
        set.remove(stringToRemove);
        return set;
    }

    private static void findPath(JsonElement jsonElement, String pathToFind, final List<String> pathSegments) {
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
        TreeSet<JsonElement> orderedSet = sortIt(jsonElement);
        JsonArray jsonArray = jsonFactory().createJsonArray();
        for (JsonElement element : orderedSet) {
            jsonArray.add(element);
        }
        return jsonArray;
    }

    private static TreeSet<JsonElement> sortIt(JsonElement jsonElement) {
        TreeSet<JsonElement> orderedSet = Sets.newTreeSet(new Comparator<JsonElement>() {
            @Override
            public int compare(JsonElement o1, JsonElement o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        orderedSet.addAll(Lists.newArrayList(jsonElement.getAsJsonArray().iterator()));
        return orderedSet;
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

    private static String headOf(final Collection<String> paths) {
        return paths.iterator().next();
    }
}
