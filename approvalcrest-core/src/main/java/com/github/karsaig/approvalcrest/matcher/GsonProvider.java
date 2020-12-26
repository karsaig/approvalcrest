/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.FieldsIgnorer.MARKER;
import static com.google.common.collect.Sets.newTreeSet;
import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hamcrest.Matcher;

import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.matcher.typeadapters.ClassAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.DateAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.InstantAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.LocalDateAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.LocalDateTimeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.LocalTimeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.OffsetDateTimeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.OffsetTimeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.PathTypeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.ThrowableTypeAdapterFactory;
import com.github.karsaig.approvalcrest.matcher.typeadapters.ZonedDateTimeAdapter;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.graph.GraphAdapterBuilder;

/**
 * Provides an instance of {@link Gson}. If any class type has been ignored on the matcher, the {@link Gson} provided
 * will include an {@link ExclusionStrategy} which will skip the serialisation of fields for that type.
 */
@SuppressWarnings("rawtypes")
class GsonProvider {
    /**
     * Returns a {@link Gson} instance containing {@link ExclusionStrategy} based on the object types to ignore during
     * serialisation.
     *
     * @param matcherConfiguration
     * @param circularReferenceTypes cater for circular referenced objects
     * @return an instance of {@link Gson}
     */
    public static Gson gson(MatcherConfiguration matcherConfiguration, Set<Class<?>> circularReferenceTypes) {
        return gson(matcherConfiguration, circularReferenceTypes, null);
    }

    /**
     * Returns a {@link Gson} instance containing {@link ExclusionStrategy} based on the object types to ignore during
     * serialisation.
     *
     * @param matcherConfiguration
     * @param circularReferenceTypes cater for circular referenced objects
     * @param additionalConfig       provides additional gson configuration
     * @return an instance of {@link Gson}
     */
    public static Gson gson(MatcherConfiguration matcherConfiguration, Set<Class<?>> circularReferenceTypes, GsonConfiguration additionalConfig) {
        GsonBuilder gsonBuilder = initGson();

        defaultGsonConfiguration(gsonBuilder, matcherConfiguration, circularReferenceTypes);
        if (additionalConfig != null) {
            additionalConfiguration(additionalConfig, gsonBuilder);
        }

        return gsonBuilder.create();
    }

    private static void defaultGsonConfiguration(GsonBuilder gsonBuilder, MatcherConfiguration matcherConfiguration, Set<Class<?>> circularReferenceTypes) {

        if (!circularReferenceTypes.isEmpty()) {
            registerCircularReferenceTypes(circularReferenceTypes, gsonBuilder);
        }

        gsonBuilder.registerTypeAdapterFactory(new ThrowableTypeAdapterFactory());
        gsonBuilder.registerTypeAdapter(Optional.class, new OptionalSerializer());
        gsonBuilder.registerTypeAdapterFactory(DateAdapter.FACTORY);
        gsonBuilder.registerTypeAdapterFactory(ClassAdapter.FACTORY);
        gsonBuilder.registerTypeAdapter(InstantAdapter.INSTANT_TYPE, new InstantAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateAdapter.LOCAL_DATE_TYPE, new LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTimeAdapter.LOCAL_DATE_TIME_TYPE, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(LocalTimeAdapter.LOCAL_TIME_TYPE, new LocalTimeAdapter());
        gsonBuilder.registerTypeAdapter(OffsetDateTimeAdapter.OFFSET_DATE_TIME_TYPE, new OffsetDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(OffsetTimeAdapter.OFFSET_TIME_TYPE, new OffsetTimeAdapter());
        gsonBuilder.registerTypeAdapter(ZonedDateTimeAdapter.ZONED_DATE_TIME_TYPE, new ZonedDateTimeAdapter());

        gsonBuilder.registerTypeAdapterFactory(PathTypeAdapter.FACTORY);

        registerSetSerialisation(gsonBuilder);

        registerMapSerialisation(gsonBuilder);

        markSetAndMapFields(gsonBuilder);

        registerExclusionStrategies(gsonBuilder, matcherConfiguration);
    }

    private static void additionalConfiguration(GsonConfiguration additionalConfig, GsonBuilder gsonBuilder) {
        for (TypeAdapterFactory factory : additionalConfig.getTypeAdapterFactories()) {
            gsonBuilder.registerTypeAdapterFactory(factory);
        }
        Map<Type, List<Object>> typeAdapterMap = additionalConfig.getTypeAdapters();
        for (Type type : typeAdapterMap.keySet()) {
            if (typeAdapterMap.get(type) != null) {
                for (Object o : typeAdapterMap.get(type)) {
                    gsonBuilder.registerTypeAdapter(type, o);

                }
            }
        }
        Map<Class<?>, List<Object>> hierarchyTypeAdapterMap = additionalConfig.getTypeHierarchyAdapter();
        for (Class<?> clazz : hierarchyTypeAdapterMap.keySet()) {
            if (hierarchyTypeAdapterMap.get(clazz) != null) {
                for (Object o : hierarchyTypeAdapterMap.get(clazz)) {
                    gsonBuilder.registerTypeHierarchyAdapter(clazz, o);
                }
            }
        }

    }

    private static void registerExclusionStrategies(GsonBuilder gsonBuilder, MatcherConfiguration matcherConfiguration) {
        if (matcherConfiguration.getTypesToIgnore().isEmpty() && matcherConfiguration.getPatternsToIgnore().isEmpty()) {
            return;
        }

        gsonBuilder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                for (Matcher<String> p : matcherConfiguration.getPatternsToIgnore()) {
                    if (p.matches(f.getName())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return (matcherConfiguration.getTypesToIgnore().contains(clazz));
            }
        });
    }

    private static void markSetAndMapFields(GsonBuilder gsonBuilder) {
        gsonBuilder.setFieldNamingStrategy(f -> {
            if (Set.class.isAssignableFrom(f.getType()) || Map.class.isAssignableFrom(f.getType())) {
                return MARKER + f.getName();
            }
            return f.getName();
        });
    }

    private static void registerMapSerialisation(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeHierarchyAdapter(Map.class, (JsonSerializer<Map>) (map, type, context) -> {
            Gson gson = gsonBuilder.create();

            ArrayListMultimap<String, Object> objects = mapObjectsByTheirJsonRepresentation(map, gson);
            return arrayOfObjectsOrderedByTheirJsonRepresentation(gson, objects, map);
        });
    }

    private static void registerSetSerialisation(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeHierarchyAdapter(Set.class, (JsonSerializer<Set>) (set, type, context) -> {
            Gson gson = gsonBuilder.create();

            Set<Object> orderedSet = orderSetByElementsJsonRepresentation(set, gson);
            return arrayOfObjectsOrderedByTheirJsonRepresentation(gson, orderedSet);
        });
    }

    private static void registerCircularReferenceTypes(Set<Class<?>> circularReferenceTypes, GsonBuilder gsonBuilder) {
        GraphAdapterBuilder graphAdapterBuilder = new GraphAdapterBuilder();
        for (Class<?> circularReferenceType : circularReferenceTypes) {
            graphAdapterBuilder.addType(circularReferenceType);
        }
        graphAdapterBuilder.registerOn(gsonBuilder);
    }

    @SuppressWarnings("unchecked")
    private static Set<Object> orderSetByElementsJsonRepresentation(Set set, Gson gson) {
        Set<Object> objects = newTreeSet(Comparator.comparing(gson::toJson));
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

    private static JsonArray arrayOfObjectsOrderedByTheirJsonRepresentation(Gson gson, Set<Object> objects) {
        JsonArray array = new JsonArray();
        for (Object object : objects) {
            array.add(gson.toJsonTree(object));
        }
        return array;
    }

    private static JsonArray arrayOfObjectsOrderedByTheirJsonRepresentation(Gson gson, ArrayListMultimap<String, Object> objects, Map map) {
        ImmutableList<String> sortedMapKeySet = Ordering.natural().immutableSortedCopy(objects.keySet());
        JsonArray array = new JsonArray();
        if (allKeysArePrimitiveOrStringOrEnum(sortedMapKeySet, objects)) {
            for (String jsonRepresentation : sortedMapKeySet) {
                List<Object> objectsInTheSet = objects.get(jsonRepresentation);
                for (Object objectInTheSet : objectsInTheSet) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add(String.valueOf(objectInTheSet), gson.toJsonTree(map.get(objectInTheSet)));
                    array.add(jsonObject);
                }
            }
        } else {
            for (String jsonRepresentation : sortedMapKeySet) {
                JsonArray keyValueArray = new JsonArray();
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

    private static GsonBuilder initGson() {
        return new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting();
    }

    private static class OptionalSerializer<T> implements JsonSerializer<Optional<T>> {

        @Override
        public JsonElement serialize(Optional<T> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray result = new JsonArray();
            result.add(context.serialize(src.orNull()));
            return result;
        }
    }
}
