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

import static com.github.karsaig.approvalcrest.FieldsIgnorer.MAP_MARKER;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.MARKER;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.MAX_CHAIN_DEPTH;
import static com.github.karsaig.approvalcrest.FieldsIgnorer.truncateAfterLastMapLevel;
import static com.github.karsaig.approvalcrest.JsonElementUtil.anyMatchesFieldName;
import static com.google.common.collect.Sets.newTreeSet;
import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.Matcher;

import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.github.karsaig.approvalcrest.matcher.typeadapters.ClassAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.DateAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.GetterBasedTypeAdapterFactory;
import com.github.karsaig.approvalcrest.matcher.typeadapters.InstantAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.LocalDateAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.LocalDateTimeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.LocalTimeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.OffsetDateTimeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.OffsetTimeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.PathTypeAdapter;
import com.github.karsaig.approvalcrest.matcher.typeadapters.ThrowableTypeAdapterFactory;
import com.github.karsaig.approvalcrest.matcher.typeadapters.UnsafeFieldTypeAdapterFactory;
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
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.graph.GraphAdapterBuilder;
import com.google.gson.reflect.TypeToken;

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

        Set<Class<?>> skipTypes = additionalConfig != null ? additionalConfig.getTypesToSkipInFallbackFactories() : java.util.Collections.emptySet();
        defaultGsonConfiguration(gsonBuilder, matcherConfiguration, circularReferenceTypes, skipTypes);
        if (additionalConfig != null) {
            additionalConfiguration(additionalConfig, gsonBuilder);
        }

        return gsonBuilder.create();
    }

    private static void defaultGsonConfiguration(GsonBuilder gsonBuilder, MatcherConfiguration matcherConfiguration, Set<Class<?>> circularReferenceTypes, Set<Class<?>> additionalSkipTypes) {

        if (matcherConfiguration.isSerializeNulls()) {
            gsonBuilder.serializeNulls();
        }

        if (!circularReferenceTypes.isEmpty()) {
            registerCircularReferenceTypes(circularReferenceTypes, gsonBuilder);
        }

        // Register locked-module fallback factories FIRST. Gson reverses the factory
        // list internally, so first-registered ends up checked LAST — which is what we
        // want: these should only handle types that no other factory claims.
        gsonBuilder.registerTypeAdapterFactory(new UnsafeFieldTypeAdapterFactory(additionalSkipTypes));
        gsonBuilder.registerTypeAdapterFactory(new GetterBasedTypeAdapterFactory(additionalSkipTypes));

        gsonBuilder.registerTypeAdapterFactory(new ThrowableTypeAdapterFactory());
        gsonBuilder.registerTypeAdapter(Optional.class, new OptionalSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(java.util.Optional.class, new JavaOptionalSerializer<>());
        gsonBuilder.registerTypeHierarchyAdapter(java.util.OptionalInt.class, new JavaOptionalIntSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(java.util.OptionalLong.class, new JavaOptionalLongSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(java.util.OptionalDouble.class, new JavaOptionalDoubleSerializer());
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

        registerSetSerialisation(gsonBuilder, matcherConfiguration);

        registerMapSerialisation(gsonBuilder);

        markSortedFields(gsonBuilder, matcherConfiguration.getTypesToSort());

        registerExclusionStrategies(gsonBuilder, matcherConfiguration);
    }

    private static void additionalConfiguration(GsonConfiguration additionalConfig, GsonBuilder gsonBuilder) {
        for (TypeAdapterFactory factory : additionalConfig.getTypeAdapterFactories()) {
            gsonBuilder.registerTypeAdapterFactory(new LenientTypeAdapterFactory(factory));
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
                return anyMatchesFieldName(f.getName(), matcherConfiguration.getPatternsToIgnore());
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return (matcherConfiguration.getTypesToIgnore().contains(clazz));
            }
        });
    }

    private static void markSortedFields(GsonBuilder gsonBuilder, List<Class<?>> typesToSort) {
        gsonBuilder.setFieldNamingStrategy(f -> {
            String ownMarker = ownMarkerOf(f, typesToSort);
            if (ownMarker == null) {
                return f.getName();
            }
            // The field's own marker first, then one sentinel per container level below it, so the sorter
            // can tell a map's [key, value] pair from a nested collection wherever the declared type says
            // so. Appended to today's decision rather than derived from the walk: a field whose generic
            // type says nothing -- Object, raw, a type variable -- must keep the marker it has now, or it
            // silently stops being sorted.
            return ownMarker + markerChainBelow(f.getGenericType()) + f.getName();
        });
    }

    /**
     * The marker the field itself carries, or null when it carries none. A Map gets its own: both queue
     * the field for sorting, but only a Map's array holds [key, value] entries, and the sorter must not
     * reorder a key with its value.
     */
    private static String ownMarkerOf(Field f, List<Class<?>> typesToSort) {
        if (Map.class.isAssignableFrom(f.getType())) {
            return MAP_MARKER;
        }
        if (Set.class.isAssignableFrom(f.getType())) {
            return MARKER;
        }
        if (!typesToSort.isEmpty()) {
            if (Collection.class.isAssignableFrom(f.getType())) {
                Type generic = f.getGenericType();
                if (generic instanceof ParameterizedType) {
                    Type arg = ((ParameterizedType) generic).getActualTypeArguments()[0];
                    if (typesToSort.contains(arg)) {
                        return MARKER;
                    }
                }
            } else if (f.getType().isArray()) {
                if (typesToSort.contains(f.getType().getComponentType())) {
                    return MARKER;
                }
            }
        }
        return null;
    }

    /**
     * Describes the container levels below {@code fieldType}, one sentinel per level: {@code MAP_MARKER}
     * for a map, {@code MARKER} for a collection or array. The field's own level is not included -- that is
     * the marker {@link #ownMarkerOf} returns.
     *
     * <p>The walk stops at the first level the declared type does not describe: a bean, {@code Object}, a
     * raw or wildcard type, a type variable. Stopping at a bean is right, since a bean's own fields carry
     * their own markers and the nesting re-enters through them. Stopping anywhere else costs a fix the
     * sorter cannot make, which is the safe direction -- nothing stops being sorted.
     *
     * <p>Any map or collection is walked, subclasses included, because what a level holds is resolved through
     * its supertypes rather than read off argument 1. Reading positionally needs a fence -- a subtype is free
     * to drop a parameter ({@code class MyMap<V> extends HashMap<String,V>}) or reorder them
     * ({@code class Flipped<V,K> extends HashMap<K,V>}), so argument 1 either does not exist or is the key --
     * and any such fence is a list of known types, which is a list of the types nobody has subclassed.
     */
    private static String markerChainBelow(Type fieldType) {
        StringBuilder chain = new StringBuilder();
        Type current = valueTypeOf(fieldType);
        // Two questions, deliberately separate. A level takes a sentinel because IT is a container; what it
        // holds only decides whether there is another level after it. Conflating them -- stopping because the
        // contents are undescribed -- silently drops the inner level of Map<K, Map<K2, Object>>.
        for (int depth = 0; current != null && isContainerLevel(current) && depth < MAX_CHAIN_DEPTH; depth++) {
            chain.append(isMapLevel(current) ? MAP_MARKER : MARKER);
            current = valueTypeOf(current);
        }
        return truncateAfterLastMapLevel(chain.toString());
    }

    /**
     * Guava's token, not gson's — this file imports that one, so the name is spelled out once here and the
     * rest of the walk goes through this method.
     */
    private static com.google.common.reflect.TypeToken<?> tokenOf(Type type) {
        return com.google.common.reflect.TypeToken.of(type);
    }

    /** Whether this level holds things at all, which is what decides that it takes a sentinel. */
    private static boolean isContainerLevel(Type type) {
        Class<?> raw = tokenOf(type).getRawType();
        return Map.class.isAssignableFrom(raw) || Collection.class.isAssignableFrom(raw)
                || raw == Iterable.class || raw.isArray();
    }

    /** Which sentinel a level takes, asked only of a level {@link #isContainerLevel} has accepted. */
    private static boolean isMapLevel(Type type) {
        return Map.class.isAssignableFrom(tokenOf(type).getRawType());
    }

    /**
     * What a level holds — a map's value type, a collection's element type, an array's component type — or
     * null where the declared type does not say, which is what stops the walk.
     *
     * <p>Memoised because it is the only place a type resolution happens, and that resolution is
     * not cheap: it captures wildcards and walks the whole generic supertype graph, and guava caches the
     * resolver on the token, which a fresh {@code TypeToken.of} reuses none of. The naming strategy runs for
     * every field of every class each time Gson builds an adapter, and this project builds a Gson per
     * assertion and another inside the map and set serialisers, so the same handful of declared types is
     * asked about over and over. The map is static, so it holds the {@code Class}es it has seen for the life
     * of the JVM — bounded by the number of distinct declared field types, but worth knowing where
     * classloaders are reloaded. It is a pure function of the type, and the values are immutable.
     */
    private static Type valueTypeOf(Type type) {
        Type memoised = VALUE_TYPES.computeIfAbsent(type, GsonProvider::resolveValueType);
        return memoised == NOTHING_DESCRIBED ? null : memoised;
    }

    private static Type resolveValueType(Type type) {
        com.google.common.reflect.TypeToken<?> token = tokenOf(type);
        Class<?> raw = token.getRawType();
        if (Map.class.isAssignableFrom(raw)) {
            return resolvedArgument(token, MAP_VALUE);
        }
        if (Collection.class.isAssignableFrom(raw)) {
            return resolvedArgument(token, COLLECTION_ELEMENT);
        }
        // Iterable itself and only itself, as the list this replaces had it. Widening to
        // Iterable.class.isAssignableFrom would newly admit java.nio.file.Path and gson's own JsonArray,
        // both Iterable over themselves, and walk to the depth cap describing levels that are not there.
        if (raw == Iterable.class) {
            return resolvedArgument(token, ITERABLE_ELEMENT);
        }
        if (token.isArray()) {
            return token.getComponentType().getType();
        }
        return NOTHING_DESCRIBED;
    }

    private static Type resolvedArgument(com.google.common.reflect.TypeToken<?> token, TypeVariable<?> parameter) {
        Type resolved = token.resolveType(parameter).getType();
        // An unresolved variable says nothing about what is there, and a wildcard arrives as one: resolution
        // is invariant, so `? extends Map<K,V>` comes back captured as an artificial variable rather than a
        // WildcardType. Object is deliberately not filtered here -- ownMarkerOf tests membership rather than
        // description, and sortType(Object.class) over a List<Object> is a caller saying something real.
        return resolved instanceof TypeVariable ? NOTHING_DESCRIBED : resolved;
    }

    private static final TypeVariable<?> MAP_VALUE = Map.class.getTypeParameters()[1];

    private static final TypeVariable<?> COLLECTION_ELEMENT = Collection.class.getTypeParameters()[0];

    private static final TypeVariable<?> ITERABLE_ELEMENT = Iterable.class.getTypeParameters()[0];

    /** Stands in for null, which a {@link ConcurrentHashMap} cannot hold. */
    private static final Type NOTHING_DESCRIBED = new Type() {
        @Override
        public String toString() {
            return "nothing described";
        }
    };

    private static final ConcurrentHashMap<Type, Type> VALUE_TYPES = new ConcurrentHashMap<>();

    private static void registerMapSerialisation(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeHierarchyAdapter(Map.class, (JsonSerializer<Map>) (map, type, context) -> {
            Gson gson = gsonBuilder.create();

            ArrayListMultimap<String, Object> objects = mapObjectsByTheirJsonRepresentation(map, gson);
            return arrayOfObjectsOrderedByTheirJsonRepresentation(gson, objects, map);
        });
    }

    private static void registerSetSerialisation(GsonBuilder gsonBuilder, MatcherConfiguration matcherConfiguration) {
        boolean legacySetCollapse = matcherConfiguration.isLegacySetCollapse();
        gsonBuilder.registerTypeHierarchyAdapter(Set.class, (JsonSerializer<Set>) (set, type, context) -> {
            Gson gson = gsonBuilder.create();

            List<Object> orderedSet = orderSetByElementsJsonRepresentation(set, gson, legacySetCollapse);
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

    /**
     * Returns the set's elements ordered by their JSON representation, so that the unordered
     * iteration order of a {@code Set} cannot make the output unstable.
     *
     * <p>Sorting is done on a list rather than a {@code TreeSet}: a sorted set treats
     * "comparator returned 0" as "duplicate", so elements that are not {@code equals()} but happen
     * to serialise identically - two instances of a class with no {@code equals()} override and
     * the same field values, for example - were silently dropped. A set that lost or gained such
     * an element then serialised identically either way, so the difference could not fail a test.
     *
     * <p>Each element is serialised once up front rather than inside the comparator, which also
     * avoids re-serialising on every comparison.
     */
    @SuppressWarnings("unchecked")
    private static List<Object> orderSetByElementsJsonRepresentation(Set set, Gson gson, boolean legacySetCollapse) {
        if (legacySetCollapse) {
            Set<Object> collapsed = newTreeSet(Comparator.comparing(gson::toJson));
            collapsed.addAll(set);
            return new ArrayList<>(collapsed);
        }
        List<Map.Entry<String, Object>> decorated = new ArrayList<>(set.size());
        for (Object element : (Set<Object>) set) {
            decorated.add(new AbstractMap.SimpleEntry<>(gson.toJson(element), element));
        }
        decorated.sort(Map.Entry.comparingByKey());
        List<Object> objects = new ArrayList<>(decorated.size());
        for (Map.Entry<String, Object> entry : decorated) {
            objects.add(entry.getValue());
        }
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

    private static JsonArray arrayOfObjectsOrderedByTheirJsonRepresentation(Gson gson, List<Object> objects) {
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
                List<Object> objectsInTheSet = objects.get(jsonRepresentation);
                for (Object objectInTheSet : objectsInTheSet) {
                    JsonArray keyValueArray = new JsonArray();
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
                // A null key has no class to inspect, and it belongs in this branch rather than the pair
                // one: here it renders as the member name "null", which a JSON string input can express and
                // a path can address, whereas a pair would put it at a position that path navigation skips.
                if (object == null) {
                    continue;
                }
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

    private static class JavaOptionalSerializer<T> implements JsonSerializer<java.util.Optional<T>> {

        @Override
        public JsonElement serialize(java.util.Optional<T> src, Type typeOfSrc, JsonSerializationContext context) {
            if (!src.isPresent()) {
                return new JsonObject();
            }
            JsonObject result = new JsonObject();
            Type valueType = getOptionalValueType(typeOfSrc);
            if (valueType != null && isConcrete(valueType) && !isPolymorphic(valueType, src.get())) {
                result.add("value", context.serialize(src.get(), valueType));
            } else {
                result.add("value", context.serialize(src.get()));
            }
            return result;
        }

        private Type getOptionalValueType(Type optionalType) {
            if (optionalType instanceof ParameterizedType) {
                Type[] args = ((ParameterizedType) optionalType).getActualTypeArguments();
                if (args.length == 1) {
                    return args[0];
                }
            }
            return null;
        }

        private boolean isConcrete(Type type) {
            if (type instanceof Class<?>) {
                Class<?> clazz = (Class<?>) type;
                return !clazz.isInterface() && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers());
            }
            if (type instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) type).getRawType();
                return isConcrete(rawType);
            }
            return false;
        }

        private boolean isPolymorphic(Type declaredType, Object value) {
            Class<?> rawDeclared;
            if (declaredType instanceof Class<?>) {
                rawDeclared = (Class<?>) declaredType;
            } else if (declaredType instanceof ParameterizedType) {
                Type raw = ((ParameterizedType) declaredType).getRawType();
                rawDeclared = (raw instanceof Class<?>) ? (Class<?>) raw : null;
            } else {
                return false;
            }
            return rawDeclared != null && rawDeclared != value.getClass();
        }
    }

    private static class JavaOptionalIntSerializer implements JsonSerializer<java.util.OptionalInt> {
        @Override
        public JsonElement serialize(java.util.OptionalInt src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (src.isPresent()) {
                result.addProperty("value", src.getAsInt());
            }
            return result;
        }
    }

    private static class JavaOptionalLongSerializer implements JsonSerializer<java.util.OptionalLong> {
        @Override
        public JsonElement serialize(java.util.OptionalLong src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (src.isPresent()) {
                result.addProperty("value", src.getAsLong());
            }
            return result;
        }
    }

    private static class JavaOptionalDoubleSerializer implements JsonSerializer<java.util.OptionalDouble> {
        @Override
        public JsonElement serialize(java.util.OptionalDouble src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (src.isPresent()) {
                result.addProperty("value", src.getAsDouble());
            }
            return result;
        }
    }

    private static class OptionalSerializer<T> implements JsonSerializer<Optional<T>> {

        @Override
        public JsonElement serialize(Optional<T> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray result = new JsonArray();
            result.add(context.serialize(src.orNull()));
            return result;
        }
    }

    /**
     * Wraps a user-supplied {@link TypeAdapterFactory} to catch {@link IllegalStateException}
     * thrown by factories that cannot handle raw (non-parameterized) types.
     * <p>
     * This is a safety net for factories generated by libraries like Immutables
     * ({@code @Gson.TypeAdapters}) that require actual type parameters in the TypeToken.
     * When such a factory is called with a raw type (e.g., from {@code gson.toJsonTree(object)}),
     * it throws IllegalStateException. This wrapper catches that and returns null, allowing
     * Gson to fall back to reflective serialization.
     */
    static class LenientTypeAdapterFactory implements TypeAdapterFactory {
        private static final Logger LOGGER = Logger.getLogger(LenientTypeAdapterFactory.class.getName());
        private static final Set<String> WARNED_KEYS = ConcurrentHashMap.newKeySet();

        private final TypeAdapterFactory delegate;

        LenientTypeAdapterFactory(TypeAdapterFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            try {
                return delegate.create(gson, type);
            } catch (IllegalStateException e) {
                String key = delegate.getClass().getName() + ":" + type;
                if (WARNED_KEYS.add(key)) {
                    LOGGER.log(Level.WARNING, "TypeAdapterFactory '" + delegate.getClass().getName()
                            + "' threw IllegalStateException for type " + type
                            + ". Falling back to reflective serialization. "
                            + "This usually means the factory received a raw type without generic parameters. "
                            + "Possible causes: (1) a field is declared with a raw type "
                            + "(e.g., MyType instead of MyType<String>), or "
                            + "(2) a custom JsonSerializer calls context.serialize(value) without passing "
                            + "the declared Type — use context.serialize(value, declaredType) instead. "
                            + "Subsequent occurrences for this factory and type combination will only be logged at FINE level.", e);
                } else if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "TypeAdapterFactory '" + delegate.getClass().getName()
                            + "' threw IllegalStateException for type " + type
                            + ". Falling back to reflective serialization (see previous WARNING for details).", e);
                }
                return null;
            }
        }
    }
}
