package com.github.karsaig.approvalcrest;

import com.google.gson.JsonElement;
import org.hamcrest.Matcher;

import java.lang.reflect.Field;
import java.util.List;

public class JsonElementUtil {

    private JsonElementUtil() {
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
