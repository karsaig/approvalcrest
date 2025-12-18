package com.github.karsaig.approvalcrest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.hamcrest.Matcher;

import java.lang.reflect.Field;
import java.util.List;

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
}
