package com.github.karsaig.json;

import static com.google.common.collect.Sets.newTreeSet;
import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

import com.github.karsaig.approvalcrest.FieldsIgnorer;
import com.github.karsaig.json.graph.GraphAdapterBuilder;
import com.github.karsaig.json.graph.GsonGraphAdapterBuilder;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapterFactory;

public class GsonDelegateJsonBuilder implements JsonBuilder {
    private GsonBuilder delegate;

    private List<Class<?>> typesToIgnore;
    private List<Matcher<String>> fieldsToIgnore;
    private Set<Class<?>> circularReferenceTypes;
    private JsonConfiguration additionalConfig;

    @Override
    public @NotNull JsonBuilder initialize() {
        delegate = new GsonBuilder();
        delegate.registerTypeAdapter(Optional.class, new OptionalSerializer());
        registerSetSerialisation(delegate);
        registerMapSerialisation(delegate);
        markSetAndMapFields(delegate);
        return this;
    }

    @Override
    public @NotNull JsonBuilder registerTypesToIgnore(@NotNull List<Class<?>> typesToIgnore) {
        this.typesToIgnore = typesToIgnore;
        return this;
    }

    @Override
    public @NotNull JsonBuilder registerFieldsToIgnore(@NotNull List<Matcher<String>> fieldsToIgnore) {
        this.fieldsToIgnore = fieldsToIgnore;
        return this;
    }

    @Override
    public @NotNull JsonBuilder registerCircularReferenceTypes(@NotNull Set<Class<?>> circularReferenceTypes) {
        this.circularReferenceTypes = circularReferenceTypes;
        return this;
    }

    @Override
    public @NotNull JsonBuilder setPrettyPrinting() {
        delegate.setPrettyPrinting();
        return this;
    }

    @Override
    public @NotNull JsonBuilder addExtraConfiguration(@NotNull JsonConfiguration additionalConfig) {
        this.additionalConfig = additionalConfig;
        return this;
    }

    @Override
    public @NotNull Json build() {
        if (!circularReferenceTypes.isEmpty()) {
            registerCircularReferenceTypes(circularReferenceTypes, this);
        }

        if (!typesToIgnore.isEmpty() || !fieldsToIgnore.isEmpty()) {
            delegate.setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    for (Matcher<String> matcher : fieldsToIgnore) {
                        if (matcher.matches(fieldAttributes.getName())) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return (typesToIgnore.contains(clazz));
                }
            });
        }

        if (additionalConfig != null) {
            Map<Type, List<Object>> typeAdapterMap = additionalConfig.getTypeAdapters();
            for (Type type : typeAdapterMap.keySet()) {
                if (typeAdapterMap.get(type) != null) {
                    for (Object o : typeAdapterMap.get(type)) {
                        delegate.registerTypeAdapter(type, o);
                    }
                }
            }
        }

        return new GsonDelegateJson(delegate.create());
    }

    public void registerTypeAdapterFactory(TypeAdapterFactory factory) {
        delegate.registerTypeAdapterFactory(factory);
    }

    public void registerTypeAdapter(Type key, TypeAdapterFactory factory) {
        delegate.registerTypeAdapter(key, factory);
    }

    private static void registerSetSerialisation(final GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeHierarchyAdapter(Set.class, new JsonSerializer<Set>() {
            @Override
            public com.google.gson.JsonElement serialize(Set set, Type type, JsonSerializationContext context) {
                Gson gson = gsonBuilder.create();

                Set<Object> orderedSet = orderSetByElementsJsonRepresentation(set, gson);
                return arrayOfObjectsOrderedByTheirJsonRepresentation(gson, orderedSet);
            }
        });
    }

    private static void registerMapSerialisation(final GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeHierarchyAdapter(Map.class, new JsonSerializer<Map>() {
            @Override
            public com.google.gson.JsonElement serialize(Map map, Type type, JsonSerializationContext context) {
                Gson gson = gsonBuilder.create();

                ArrayListMultimap<String, Object> objects = mapObjectsByTheirJsonRepresentation(map, gson);
                return arrayOfObjectsOrderedByTheirJsonRepresentation(gson, objects, map);
            }
        });
    }

    private static void markSetAndMapFields(final GsonBuilder gsonBuilder) {
        gsonBuilder.setFieldNamingStrategy(new FieldNamingStrategy() {
            @Override
            public String translateName(Field f) {
                if (Set.class.isAssignableFrom(f.getType()) || Map.class.isAssignableFrom(f.getType())) {
                    return FieldsIgnorer.MARKER + f.getName();
                }
                return f.getName();
            }
        });
    }

    private static void registerCircularReferenceTypes(Set<Class<?>> circularReferenceTypes, JsonBuilder jsonBuilder) {
        GraphAdapterBuilder graphAdapterBuilder = new GsonGraphAdapterBuilder();
        for (Class<?> circularReferenceType : circularReferenceTypes) {
            graphAdapterBuilder.addType(circularReferenceType);
        }
        graphAdapterBuilder.registerOn(jsonBuilder);
    }

    @SuppressWarnings("unchecked")
    private static Set<Object> orderSetByElementsJsonRepresentation(Set set, final Gson gson) {
        Set<Object> objects = newTreeSet(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return gson.toJson(o1).compareTo(gson.toJson(o2));
            }
        });
        objects.addAll(set);
        return objects;
    }

    @SuppressWarnings("unchecked")
    private static ArrayListMultimap<String, Object> mapObjectsByTheirJsonRepresentation(Map map, Gson gson) {
        ArrayListMultimap<String, Object> objects = ArrayListMultimap.create();
        for (Entry<Object, Object> mapEntry : (Set<Map.Entry<Object, Object>>) map.entrySet()) {
            objects.put(gson.toJson(mapEntry.getKey()).concat(gson.toJson(mapEntry.getValue())), mapEntry.getKey());
        }
        return objects;
    }

    private static com.google.gson.JsonArray arrayOfObjectsOrderedByTheirJsonRepresentation(Gson gson, Set<Object> objects) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        for (Object object : objects) {
            array.add(gson.toJsonTree(object));
        }
        return array;
    }

    private static com.google.gson.JsonElement arrayOfObjectsOrderedByTheirJsonRepresentation(Gson gson, ArrayListMultimap<String, Object> objects, Map map) {
        ImmutableList<String> sortedMapKeySet = Ordering.natural().immutableSortedCopy(objects.keySet());
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        if (allKeysArePrimitiveOrStringOrEnum(sortedMapKeySet, objects)) {
            for (String jsonRepresentation : sortedMapKeySet) {
                List<Object> objectsInTheSet = objects.get(jsonRepresentation);
                for (Object objectInTheSet : objectsInTheSet) {
                    com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
                    jsonObject.add(String.valueOf(objectInTheSet), gson.toJsonTree(map.get(objectInTheSet)));
                    array.add(jsonObject);
                }
            }
        } else {
            for (String jsonRepresentation : sortedMapKeySet) {
                com.google.gson.JsonArray keyValueArray = new com.google.gson.JsonArray();
                List<Object> objectsInTheSet = objects.get(jsonRepresentation);
                for (Object objectInTheSet : objectsInTheSet) {
                    keyValueArray.add(gson.toJsonTree(objectInTheSet));
                    keyValueArray.add(gson.toJsonTree(map.get(objectInTheSet)));
                    array.add(keyValueArray);
                }
            }
        }

        return array;
    }

    private static boolean allKeysArePrimitiveOrStringOrEnum(ImmutableList<String> sortedMapKeySet, ArrayListMultimap<String, Object> objects) {
        for (String jsonRepresentation : sortedMapKeySet) {
            List<Object> mapKeys = objects.get(jsonRepresentation);
            for (Object object : mapKeys) {
                if (!(isPrimitiveOrWrapper(object.getClass()) || object.getClass() == String.class || object.getClass().isEnum())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class OptionalSerializer<T> implements JsonSerializer<Optional<T>> {
        @Override
        public com.google.gson.JsonElement serialize(Optional<T> src, Type typeOfSrc, JsonSerializationContext context) {
            com.google.gson.JsonArray result = new com.google.gson.JsonArray();
            result.add(context.serialize(src.orNull()));
            return result;
        }
    }
}
