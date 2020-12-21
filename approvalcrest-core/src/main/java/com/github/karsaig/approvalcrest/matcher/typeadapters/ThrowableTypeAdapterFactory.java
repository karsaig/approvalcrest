package com.github.karsaig.approvalcrest.matcher.typeadapters;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


public class ThrowableTypeAdapterFactory extends CustomizedTypeAdapterFactory<Throwable> {

    private static final String STACK_TRACE_NAME = "stackTrace";
    private static final Pattern OBJECT_REFERENCE_PATTERN = Pattern.compile("0x(?:\\d)+");

    public ThrowableTypeAdapterFactory() {
        super(Throwable.class);
    }

    @Override
    protected void beforeWrite(Throwable source, JsonElement toSerialize) {
        if (toSerialize.isJsonObject()) {
            JsonObject jsonObject = toSerialize.getAsJsonObject();
            if (jsonObject.has(STACK_TRACE_NAME)) {
                addClass(source, jsonObject);
                jsonObject.remove(STACK_TRACE_NAME);
            } else {
                Map<String, Object> refToObjectmap = buildSourceObjectMap(source, jsonObject);
                for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                    JsonObject actualObject = actual.getValue().getAsJsonObject();
                    if (actualObject.has(STACK_TRACE_NAME)) {
                        addClass(refToObjectmap.get(actual.getKey()), actualObject);
                        actualObject.remove(STACK_TRACE_NAME);
                    }
                }
            }
        }
    }

    private void addClass(Object source, JsonObject jsonObject) {
        jsonObject.add("class", new JsonPrimitive(source.getClass().getCanonicalName()));
    }

    private Map<String, Object> buildSourceObjectMap(Throwable source, JsonObject jsonObject) {
        Optional<String> firstRef = jsonObject.keySet().stream().filter(OBJECT_REFERENCE_PATTERN.asPredicate()).findFirst();
        if (!firstRef.isPresent()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        JsonElement currentElement = jsonObject.get(firstRef.get());
        result.put(firstRef.get(), source);

        doBuildSourceObjectMap(result, source, jsonObject, currentElement.getAsJsonObject());
        return result;
    }

    private void doBuildSourceObjectMap(Map<String, Object> result, Object o, JsonObject originalObject, JsonObject currentObject) {
        List<Map.Entry<String, JsonElement>> jsonPrimitiveRefs = currentObject.entrySet().stream()
                .filter(e -> e.getValue().isJsonPrimitive()).filter(e -> OBJECT_REFERENCE_PATTERN.asPredicate().test(e.getValue().getAsJsonPrimitive().getAsString()))
                .collect(Collectors.toList());
        Map<String, Field> fieldMap = getEveryField(o);
        if (!jsonPrimitiveRefs.isEmpty()) {
            for (Map.Entry<String, JsonElement> entry : jsonPrimitiveRefs) {
                try {
                    Field field = fieldMap.get(entry.getKey());
                    field.setAccessible(true);
                    Object current = field.get(o);
                    String key = entry.getValue().getAsJsonPrimitive().getAsString();
                    result.put(key, current);
                    JsonElement jsonElementForCurrent = originalObject.get(key);
                    if (jsonElementForCurrent != null && jsonElementForCurrent.isJsonObject()) {
                        doBuildSourceObjectMap(result, current, originalObject, jsonElementForCurrent.getAsJsonObject());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        List<Map.Entry<String, JsonElement>> jsonObjectRefs = currentObject.entrySet().stream()
                .filter(e -> e.getValue().isJsonObject()).collect(Collectors.toList());
        if (!jsonObjectRefs.isEmpty()) {
            for (Map.Entry<String, JsonElement> entry : jsonObjectRefs) {
                try {
                    Field field = fieldMap.get(entry.getKey());
                    field.setAccessible(true);
                    Object current = field.get(o);
                    doBuildSourceObjectMap(result, current, originalObject, entry.getValue().getAsJsonObject());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Map<String, Field> getEveryField(Object o) {
        Map<String, Field> result = new HashMap<>();
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field classField : fields) {
                result.put(classField.getName(), classField);
            }
        }
        return result;
    }
}
