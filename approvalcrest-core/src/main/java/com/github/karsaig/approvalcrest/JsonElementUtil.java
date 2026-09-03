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
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonElementUtil {

    /**
     * Path segment standing for "every named child at this position" — a JSON object property, a
     * {@code Map} key, or a bean field.
     * <p>
     * It is only a wildcard in a non-final segment. As the final segment it keeps its ordinary
     * meaning, a key literally named {@code *}, which a JSON document or a {@code Map<String,?>} may
     * genuinely have.
     * <p>
     * Arrays, collections and sets are not covered, and need no covering: they are traversed
     * transparently without consuming a segment — collections in the object walk
     * ({@code BeanFinder}), arrays and collections in the JSON walk — so {@code list.x} already means
     * "x in every element".
     */
    public static final String WILDCARD = "*";

    private JsonElementUtil() {
    }

    /** True when {@code segments[segIdx]} is the wildcard and something follows it to descend into. */
    private static boolean isWildcardSegment(String[] segments, int segIdx) {
        return WILDCARD.equals(segments[segIdx]) && segIdx + 1 < segments.length;
    }

    /**
     * Returns a read-only {@link List} view of {@code array}.
     * <p>
     * Gson's {@link JsonArray} is {@code Iterable<JsonElement>} but not a {@link java.util.Collection},
     * so Hamcrest matchers typed on {@code Collection} — {@code hasSize}, {@code empty} — cannot
     * evaluate against one: they answer false on the type check without ever inspecting the content,
     * which makes their negations vacuously true. This view lets them answer for real.
     * <p>
     * Nothing is copied: {@code size()} and {@code get(int)} delegate to the live array. Elements are
     * coerced on read by {@link #jsonElementToJavaValue(JsonElement)}, so a JSON scalar arrives as the
     * {@code String}, {@code Long}, {@code Double} or {@code Boolean} it represents and an element
     * matcher written against the value can match it. This mirrors what the fan-out path already does
     * at a path leaf. A {@code JsonObject} or nested {@code JsonArray} is handed back as it is: JSON
     * carries no type information, so an element matcher written against the field's own class could
     * not match it either way.
     */
    public static List<Object> asList(JsonArray array) {
        return new AbstractList<Object>() {
            @Override
            public Object get(int index) {
                return jsonElementToJavaValue(array.get(index));
            }

            @Override
            public int size() {
                return array.size();
            }
        };
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
            // Deliberately yields null whatever segments remain, rather than failing to resolve:
            // findJsonValueThroughNullMidPathReturnsNull pins it, and the "<path> is null" diagnostic
            // that doesNotIncludeParentBeanFromFieldPath asserts depends on it.
            return Either.right(null);
        }
        if (segIdx == segments.length) {
            return Either.right(jsonElementToJavaValue(current));
        }
        if (current.isJsonArray()) {
            // Transparent array traversal: fan out into each element, mirroring
            // FieldsIgnorer's array-traversal behaviour and BeanFinder's collection fanout.
            // Lenient: collect from elements that have the field; fail only if none do.
            // This handles Map-as-array-of-single-entry-objects where only one element
            // holds any given key (e.g. Gson's Map serialisation).
            BeanFinder.FanoutResult fanout = new BeanFinder.FanoutResult();
            Either<RuntimeException, Object> lastError = null;
            for (JsonElement elem : current.getAsJsonArray()) {
                Either<RuntimeException, Object> r = findJsonValueAt(path, segments, segIdx, elem);
                if (r.isLeft()) {
                    lastError = r;
                } else {
                    fanout.add(r.getRight());
                }
            }
            // lastError == null: empty array or all succeeded — preserve right(emptyFanout) for empty arrays
            return (!fanout.isEmpty() || lastError == null) ? Either.right(fanout) : lastError;
        }
        if (!current.isJsonObject()) {
            return Either.left(new IllegalArgumentException(segments[segIdx] + " not navigable"));
        }
        JsonObject obj = current.getAsJsonObject();
        String segment = segments[segIdx];
        if (isWildcardSegment(segments, segIdx)) {
            return fanOutOverNamedChildren(path, segments, segIdx, obj);
        }
        // getChild tolerates the sentinels the field naming strategy adds: MARKER for a Set-typed field,
        // MAP_MARKER for a Map-typed one. Without that no path could cross such a field on the JSON
        // fallback.
        JsonElement child = FieldsIgnorer.getChild(obj, segment);
        if (child == null) {
            // Try transparent descent through graph-adapter envelope keys
            for (java.util.Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                if (FieldsIgnorer.isGraphAdapterKey(entry.getKey()) && entry.getValue().isJsonObject()) {
                    Either<RuntimeException, Object> result = findJsonValueAt(path, segments, segIdx, entry.getValue());
                    if (result.isRight()) return result;
                }
            }
            return Either.left(new IllegalArgumentException(path + " not found"));
        }
        return findJsonValueAt(path, segments, segIdx + 1, child);
    }

    /**
     * Resolves the rest of {@code segments} against every named child of {@code obj}, for a
     * {@link #WILDCARD} segment. A graph-adapter envelope key is descended through rather than
     * consumed, so the wildcard applies to the real fields underneath it and never to the envelope.
     * <p>
     * A child the rest of the path cannot be traversed through — a null, or a scalar with segments still
     * to go — is skipped. An empty object yields an empty {@code FanoutResult}, which the caller
     * deliberately treats as a failure rather than a vacuous pass, and a non-empty object where nothing
     * resolves is an error, so a mistyped path cannot pass by matching nothing.
     */
    private static Either<RuntimeException, Object> fanOutOverNamedChildren(String path, String[] segments,
                                                                           int segIdx, JsonObject obj) {
        BeanFinder.FanoutResult fanout = new BeanFinder.FanoutResult();
        for (String key : new ArrayList<>(obj.keySet())) {
            JsonElement value = obj.get(key);
            // A null child cannot be traversed further, and the wildcard selects children by pattern,
            // so it is an irrelevance rather than a null result. Skipped, as a scalar child is.
            if (value == null || value.isJsonNull()) {
                continue;
            }
            boolean envelope = FieldsIgnorer.isGraphAdapterKey(key) && value.isJsonObject();
            Either<RuntimeException, Object> r =
                    findJsonValueAt(path, segments, envelope ? segIdx : segIdx + 1, value);
            // An empty fan-out means the child resolved to nothing. Elsewhere that is a deliberate
            // failure, stopping list.x passing vacuously over an empty list, but a wildcard's children
            // are selected by pattern: one that yields nothing is an irrelevance, not a result. Keeping
            // it would make a wildcard unsatisfiable beside any empty collection.
            if (r.isRight() && !isEmptyFanout(r.getRight())) {
                fanout.add(r.getRight());
            }
        }
        if (fanout.isEmpty() && !obj.keySet().isEmpty()) {
            return Either.left(new IllegalArgumentException(path + " not found"));
        }
        return Either.right(fanout);
    }

    private static boolean isEmptyFanout(Object value) {
        return value instanceof BeanFinder.FanoutResult && ((BeanFinder.FanoutResult) value).isEmpty();
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
                // The name the caller declared, not the key: a Set-, Map- or type-selected field is held
                // under a name the field naming strategy has prefixed. Matching the key lets a pattern
                // match only the internal form and take such a field away -- and the path recorded for
                // it then carries the sentinel into machine-readable output, which nothing strips.
                String fieldName = FieldsIgnorer.getOriginalFieldName(entry.getKey());
                Matcher<String> matchedPattern = findMatchingPattern(fieldName, matchers);
                if (matchedPattern != null) {
                    iter.remove();
                    changes = true;
                    if (tracker != null && reason != null) {
                        String childPath = currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName;
                        tracker.recordIgnoredPattern(childPath, reason, matchedPattern.toString());
                    }
                } else {
                    JsonElement je = entry.getValue();
                    String childPath = tracker != null ? (currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName) : "";
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
                // The stripped name, not the key: a Set-, Map- or type-selected field is written with one
                // or more sentinels in front of it, and a pattern is written against the name the caller
                // declared. Matching the raw key leaves such a field unmatched, and withMatcher's
                // "no field matched, so nothing to check" then makes the assertion one that cannot fail.
                if (fieldNamePattern.matches(FieldsIgnorer.getOriginalFieldName(entry.getKey()))) {
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
                // The declared name, as the ignore pass above uses: an alias is written against the name
                // the caller knows, and a recorded path is read by whoever reads the report.
                String childField = FieldsIgnorer.getOriginalFieldName(entry.getKey());
                String childPath = currentPath.isEmpty() ? childField : currentPath + "." + childField;
                JsonElement child = entry.getValue();
                if (child.isJsonPrimitive()) {
                    JsonPrimitive prim = child.getAsJsonPrimitive();
                    if (!prim.isBoolean()) {
                        String coerced = prim.getAsString();
                        Optional<String> alias = aliases.resolve(childPath, childField, coerced);
                        if (alias.isPresent()) {
                            obj.addProperty(entry.getKey(), alias.get());
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

