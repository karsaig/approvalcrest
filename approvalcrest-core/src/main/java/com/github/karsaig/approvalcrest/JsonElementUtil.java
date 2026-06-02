package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
import com.github.karsaig.approvalcrest.matcher.machinereadable.AliasTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.hamcrest.Matcher;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonElementUtil {

    private JsonElementUtil() {
    }

    public static Either<RuntimeException, Object> findJsonValueAt(String path, JsonElement root) {
        if (root == null || root.isJsonNull()) {
            return Either.left(new IllegalArgumentException(path + " not found"));
        }
        String[] segments = path.split("\\.");
        return findJsonValueAt(path, segments, 0, root);
    }

    private static Either<RuntimeException, Object> findJsonValueAt(String path, String[] segments, int segIdx, JsonElement current) {
        if (current == null || current.isJsonNull()) {
            return Either.right(null);
        }
        if (segIdx == segments.length) {
            return Either.right(jsonElementToJavaValue(current));
        }
        if (current.isJsonArray()) {
            // Transparent array traversal: fan out into each element, mirroring
            // FieldsIgnorer's array-traversal behaviour and BeanFinder's collection fanout.
            BeanFinder.FanoutResult fanout = new BeanFinder.FanoutResult();
            for (JsonElement elem : current.getAsJsonArray()) {
                Either<RuntimeException, Object> r = findJsonValueAt(path, segments, segIdx, elem);
                if (r.isLeft()) {
                    return r;
                }
                fanout.add(r.getRight());
            }
            return Either.right(fanout);
        }
        if (!current.isJsonObject()) {
            return Either.left(new IllegalArgumentException(segments[segIdx] + " not navigable"));
        }
        JsonObject obj = current.getAsJsonObject();
        String segment = segments[segIdx];
        if (!obj.has(segment)) {
            // Try transparent descent through graph-adapter envelope keys
            for (java.util.Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                if (FieldsIgnorer.isGraphAdapterKey(entry.getKey()) && entry.getValue().isJsonObject()) {
                    Either<RuntimeException, Object> result = findJsonValueAt(path, segments, segIdx, entry.getValue());
                    if (result.isRight()) return result;
                }
            }
            return Either.left(new IllegalArgumentException(path + " not found"));
        }
        return findJsonValueAt(path, segments, segIdx + 1, obj.get(segment));
    }

    public static Object jsonElementToJavaValue(JsonElement el) {
        if (el.isJsonNull()) {
            return null;
        }
        if (el.isJsonPrimitive()) {
            JsonPrimitive p = el.getAsJsonPrimitive();
            if (p.isString()) {
                return p.getAsString();
            }
            if (p.isBoolean()) {
                return p.getAsBoolean();
            }
            if (p.isNumber()) {
                double d = p.getAsDouble();
                long l = p.getAsLong();
                if (d == (double) l) {
                    return l;
                }
                return d;
            }
        }
        return el;
    }

    public static void filterByCustomMatcherPatterns(JsonElement json, MatcherConfiguration matcherConfiguration) {
        filterByCustomMatcherPatterns(json, matcherConfiguration, null);
    }

    public static void filterByCustomMatcherPatterns(JsonElement json, MatcherConfiguration matcherConfiguration,
                                                      IgnoredFieldsTracker tracker) {
        List<AbstractMap.SimpleEntry<Matcher<String>, Matcher<?>>> patterns = matcherConfiguration.getCustomMatcherPatterns();
        if (!patterns.isEmpty()) {
            List<Matcher<String>> patternKeys = new ArrayList<>();
            for (AbstractMap.SimpleEntry<Matcher<String>, Matcher<?>> entry : patterns) {
                patternKeys.add(entry.getKey());
            }
            filterByFieldMatchers(json, patternKeys, tracker, IgnoredFieldsTracker.Reason.CUSTOM_MATCHER_PATTERN);
        }
    }

    public static boolean isEmpty(JsonElement jsonElement) {
        if (jsonElement.isJsonNull() || jsonElement.isJsonPrimitive()) {
            return true;
        } else {
            if (jsonElement.isJsonArray()) {
                if (jsonElement.getAsJsonArray().isEmpty()) {
                    return true;
                }
            } else if (jsonElement.isJsonObject()) {
                if (jsonElement.getAsJsonObject().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean anyMatchesFieldName(Field field, List<Matcher<String>> patternsToIgnore) {
        return anyMatchesFieldName(field.getName(), patternsToIgnore);
    }

    public static boolean anyMatchesFieldName(String fieldName, List<Matcher<String>> matchers) {
        for (Matcher<String> actual : matchers) {
            if (actual.matches(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static void filterByFieldMatchers(JsonElement jsonElement, List<Matcher<String>> matchers) {
        filterByFieldMatchers(jsonElement, matchers, null, null);
    }

    public static void filterByFieldMatchers(JsonElement jsonElement, List<Matcher<String>> matchers,
                                              IgnoredFieldsTracker tracker, IgnoredFieldsTracker.Reason reason) {
        if (jsonElement != null && !matchers.isEmpty() && !jsonElement.isJsonNull()) {
            filterFieldsByFieldMatchers(jsonElement, matchers, tracker, reason, "");
        }
    }

    private static boolean filterFieldsByFieldMatchers(JsonElement jsonElement, List<Matcher<String>> matchers,
                                                        IgnoredFieldsTracker tracker, IgnoredFieldsTracker.Reason reason,
                                                        String currentPath) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean changes = false;
            Iterator<Map.Entry<String, JsonElement>> iter = jsonObject.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, JsonElement> entry = iter.next();
                Matcher<String> matchedPattern = findMatchingPattern(entry.getKey(), matchers);
                if (matchedPattern != null) {
                    iter.remove();
                    changes = true;
                    if (tracker != null && reason != null) {
                        String childPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();
                        tracker.recordIgnoredPattern(childPath, reason, matchedPattern.toString());
                    }
                } else {
                    JsonElement je = entry.getValue();
                    String childPath = tracker != null ? (currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey()) : "";
                    boolean changed = filterFieldsByFieldMatchers(je, matchers, tracker, reason, childPath);
                    if (changed && isEmpty(je)) {
                        iter.remove();
                        changes = true;
                        if (tracker != null) {
                            tracker.recordRemovedEmpty(childPath, collectChildPaths(tracker, childPath));
                        }
                    }
                }
            }
            return changes;
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            Iterator<JsonElement> iterator = jsonArray.iterator();
            boolean changes = false;
            int idx = 0;
            while (iterator.hasNext()) {
                JsonElement je = iterator.next();
                String elemPath = tracker != null ? currentPath + "[" + idx + "]" : "";
                boolean changed = filterFieldsByFieldMatchers(je, matchers, tracker, reason, elemPath);
                if (changed && isEmpty(je)) {
                    iterator.remove();
                    changes = true;
                } else {
                    idx++;
                }
            }
            return changes;
        }
        return false;
    }

    private static Matcher<String> findMatchingPattern(String fieldName, List<Matcher<String>> matchers) {
        for (Matcher<String> matcher : matchers) {
            if (matcher.matches(fieldName)) {
                return matcher;
            }
        }
        return null;
    }

    private static List<String> collectChildPaths(IgnoredFieldsTracker tracker, String parentPath) {
        List<String> causes = new ArrayList<>();
        String prefix = parentPath + ".";
        for (IgnoredFieldsTracker.IgnoredField field : tracker.getFields()) {
            if (field.getPath().startsWith(prefix)) {
                String desc = field.getPath() + " (" + field.getReason() + ")";
                causes.add(desc);
            }
        }
        return causes;
    }

    public static List<JsonElement> collectValuesByFieldNamePattern(JsonElement root, Matcher<String> fieldNamePattern) {
        List<JsonElement> result = new ArrayList<>();
        collectValuesRecursive(root, fieldNamePattern, result);
        return result;
    }

    private static void collectValuesRecursive(JsonElement element, Matcher<String> fieldNamePattern, List<JsonElement> result) {
        if (element == null || element.isJsonNull() || element.isJsonPrimitive()) {
            return;
        }
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (fieldNamePattern.matches(entry.getKey())) {
                    result.add(entry.getValue());
                }
                collectValuesRecursive(entry.getValue(), fieldNamePattern, result);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                collectValuesRecursive(child, fieldNamePattern, result);
            }
        }
    }

    /**
     * Walks {@code root} recursively and replaces every non-boolean, non-null JSON primitive
     * that matches an entry in {@code aliases} with the alias string (in-place mutation).
     * The last registered matching entry in the map wins.
     */
    public static void applyAliases(JsonElement root, AliasMap aliases) {
        applyAliasesRecursive(root, aliases, "", null, null);
    }

    public static void applyAliases(JsonElement root, AliasMap aliases, AliasTracker tracker) {
        applyAliasesRecursive(root, aliases, "", null, tracker);
    }

    private static void applyAliasesRecursive(JsonElement element, AliasMap aliases,
                                               String currentPath, String fieldName, AliasTracker tracker) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : new ArrayList<>(obj.entrySet())) {
                String childField = entry.getKey();
                String childPath = currentPath.isEmpty() ? childField : currentPath + "." + childField;
                JsonElement child = entry.getValue();
                if (child.isJsonPrimitive()) {
                    JsonPrimitive prim = child.getAsJsonPrimitive();
                    if (!prim.isBoolean()) {
                        String coerced = prim.getAsString();
                        Optional<String> alias = aliases.resolve(childPath, childField, coerced);
                        if (alias.isPresent()) {
                            obj.addProperty(childField, alias.get());
                            if (tracker != null) {
                                tracker.recordAlias(childPath, coerced, alias.get());
                            }
                        }
                    }
                } else {
                    applyAliasesRecursive(child, aliases, childPath, childField, tracker);
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonElement child = arr.get(i);
                if (child.isJsonPrimitive()) {
                    JsonPrimitive prim = child.getAsJsonPrimitive();
                    if (!prim.isBoolean()) {
                        String coerced = prim.getAsString();
                        Optional<String> alias = aliases.resolve(currentPath, fieldName != null ? fieldName : "", coerced);
                        if (alias.isPresent()) {
                            arr.set(i, new JsonPrimitive(alias.get()));
                            if (tracker != null) {
                                tracker.recordAlias(currentPath + "[" + i + "]", coerced, alias.get());
                            }
                        }
                    }
                } else {
                    applyAliasesRecursive(child, aliases, currentPath, fieldName, tracker);
                }
            }
        }
    }
}

