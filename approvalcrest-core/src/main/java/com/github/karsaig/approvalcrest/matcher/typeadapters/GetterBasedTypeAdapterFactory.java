package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.github.karsaig.approvalcrest.ReflectUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static java.util.Collections.newSetFromMap;

/**
 * A TypeAdapterFactory that serializes objects from locked modules using their public getter methods.
 * <p>
 * This is the last-resort fallback (Tier 3) for when both standard reflection and Unsafe
 * are unavailable. It produces JSON based on public getX()/isX() methods instead of fields.
 * <p>
 * Active when:
 * <ul>
 *   <li>Mode is "fallback" (forces this factory regardless of Unsafe availability), OR</li>
 *   <li>Mode is "safe" AND type is in a locked module AND Unsafe is NOT available</li>
 * </ul>
 * <p>
 * The JSON format differs from field-based serialization (different property names/structure),
 * but this is expected — it's the price of functioning without --add-opens on future JDKs.
 */
public class GetterBasedTypeAdapterFactory implements TypeAdapterFactory {

    private static final ThreadLocal<Set<Object>> VISITED = ThreadLocal.withInitial(
            () -> newSetFromMap(new IdentityHashMap<>()));

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();

        if (ReflectUtil.isForceMode()) {
            return null;
        }

        // Skip types that Gson handles with built-in adapters
        if (shouldSkip(rawType)) {
            return null;
        }

        if (!ReflectUtil.isInLockedModule(rawType)) {
            return null;
        }

        // In fallback mode, always handle locked types here.
        // In safe mode, only handle if Unsafe is NOT available
        // (if Unsafe is available, UnsafeFieldTypeAdapterFactory handles it first).
        if (!ReflectUtil.isFallbackMode() && ReflectUtil.isUnsafeAvailable()) {
            return null;
        }

        List<GetterInfo> getters = collectGetters(rawType);
        if (getters.isEmpty()) {
            // No getters — use toString representation
            return (TypeAdapter<T>) new ToStringAdapter();
        }

        return (TypeAdapter<T>) new GetterTypeAdapter(gson, getters);
    }

    private boolean shouldSkip(Class<?> type) {
        if (type.isArray()) return true;
        if (type.isPrimitive()) return true;
        if (type.isEnum()) return true;
        if (type == String.class) return true;
        if (Number.class.isAssignableFrom(type)) return true;
        if (Boolean.class == type) return true;
        if (Character.class == type) return true;
        if (Iterable.class.isAssignableFrom(type)) return true;
        if (java.util.Map.class.isAssignableFrom(type)) return true;
        return false;
    }

    private List<GetterInfo> collectGetters(Class<?> type) {
        List<GetterInfo> result = new ArrayList<>();
        for (Method method : type.getMethods()) {
            if (isGetter(method)) {
                String name = derivePropertyName(method);
                result.add(new GetterInfo(method, name));
            }
        }
        return result;
    }

    private boolean isGetter(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }
        if (method.getParameterCount() != 0) {
            return false;
        }
        if (method.getReturnType() == void.class) {
            return false;
        }
        // Skip Object methods
        if (method.getDeclaringClass() == Object.class) {
            return false;
        }
        String name = method.getName();
        return (name.startsWith("get") && name.length() > 3)
                || (name.startsWith("is") && name.length() > 2
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class));
    }

    private String derivePropertyName(Method method) {
        String name = method.getName();
        String property;
        if (name.startsWith("get")) {
            property = name.substring(3);
        } else {
            // "is" prefix
            property = name.substring(2);
        }
        if (property.isEmpty()) {
            return name;
        }
        // Lowercase first character
        return Character.toLowerCase(property.charAt(0)) + property.substring(1);
    }

    private static class GetterInfo {
        final Method method;
        final String propertyName;

        GetterInfo(Method method, String propertyName) {
            this.method = method;
            this.propertyName = propertyName;
        }
    }

    private static class GetterTypeAdapter extends TypeAdapter<Object> {
        private final Gson gson;
        private final List<GetterInfo> getters;

        GetterTypeAdapter(Gson gson, List<GetterInfo> getters) {
            this.gson = gson;
            this.getters = getters;
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            Set<Object> visited = VISITED.get();
            if (!visited.add(value)) {
                // Circular reference — write null to break cycle
                out.nullValue();
                return;
            }
            try {
                out.beginObject();
                for (GetterInfo gi : getters) {
                    try {
                        Object result = gi.method.invoke(value);
                        out.name(gi.propertyName);
                        if (result == null) {
                            out.nullValue();
                        } else {
                            @SuppressWarnings("unchecked")
                            TypeAdapter<Object> adapter = (TypeAdapter<Object>) gson.getAdapter(TypeToken.get(result.getClass()));
                            adapter.write(out, result);
                        }
                    } catch (Exception e) {
                        // Skip getters that throw
                    }
                }
                out.endObject();
            } finally {
                visited.remove(value);
            }
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            // Deserialization is not needed for approvalcrest (serialize-only comparison)
            JsonElement element = gson.getAdapter(JsonElement.class).read(in);
            if (element.isJsonNull()) {
                return null;
            }
            return element;
        }
    }

    private static class ToStringAdapter extends TypeAdapter<Object> {
        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            return in.nextString();
        }
    }
}
