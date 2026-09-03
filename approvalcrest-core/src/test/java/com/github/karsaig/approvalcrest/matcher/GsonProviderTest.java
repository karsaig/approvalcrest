package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.FieldsIgnorer;
import com.github.karsaig.approvalcrest.MatcherConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link GsonProvider}'s configuration assembly — specifically the
 * {@code additionalConfiguration} registration loops (custom factories, type adapters and
 * type-hierarchy adapters supplied via {@link GsonConfiguration}) and the {@code markSortedFields}
 * field-naming strategy that prefixes Set/Map/typesToSort fields with the sort marker.
 */
public class GsonProviderTest {

    private static final Set<Class<?>> NO_CIRCULAR = Collections.emptySet();

    static class MyMap<V> extends HashMap<String, V> {
        private static final long serialVersionUID = 1L;
    }

    /** Two arguments in the other order, so reading argument 1 would take the key. */
    static class Flipped<V, K> extends HashMap<K, V> {
        private static final long serialVersionUID = 1L;
    }

    /** No arguments at all to read: what it holds is only knowable from its supertype. */
    static class Registry extends HashMap<String, Map<String, String>> {
        private static final long serialVersionUID = 1L;
    }

    static class Bag<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;
    }

    static class Pouch<E> extends LinkedHashSet<E> {
        private static final long serialVersionUID = 1L;
    }

    static class KeyOnly<K> extends HashMap<K, Map<String, String>> {
        private static final long serialVersionUID = 1L;
    }

    /** The commonest subclass idiom there is, and one this cannot describe: the value is Object. */
    static class Payload extends HashMap<String, Object> {
        private static final long serialVersionUID = 1L;
    }

    static class SelfMap extends HashMap<String, SelfMap> {
        private static final long serialVersionUID = 1L;
    }

    interface Lookup<V> extends Map<String, V> {
    }

    static class TypeVariableHolder<T extends Map<String, String>> {
        T data;
    }

    /**
     * Every field here is marked today; what the chain adds is what comes after the first sentinel. The
     * assertions compare whole keys, because containsString cannot tell a chain from an over-long one.
     */
    @SuppressWarnings("unused")
    static class ChainHolder {
        Map<String, String> plainMap = new LinkedHashMap<>();
        Map<String, Map<String, String>> nestedMap = new LinkedHashMap<>();
        Set<Map<String, String>> setOfMaps = new LinkedHashSet<>();
        Set<Map<String, Set<Map<String, String>>>> setOfMapOfSetOfMap = new LinkedHashSet<>();
        Map<String, List<List<Map<String, String>>>> mapUnderTwoCollections = new LinkedHashMap<>();
        Map<String, Map<String, String>[]> mapOfArrayOfMaps = new LinkedHashMap<>();
        Set<List<String>> setOfLists = new LinkedHashSet<>();
        Map<String, SortMe> mapToBean = new LinkedHashMap<>();
        MyMap<String> mapSubtypeWithOneArgument = new MyMap<>();
        TypeVariableHolder<Map<String, String>> typeVariable = new TypeVariableHolder<>();
    }

    /**
     * Subtypes and shapes a fixed list of known types cannot describe. Every one of these is a field
     * declaration someone can write, and each needs the value type resolved through the supertype chain
     * rather than read off argument 1.
     */
    @SuppressWarnings("unused")
    static class SubtypeHolder {
        MyMap<Map<String, String>> valueOnlyParameter = new MyMap<>();
        Flipped<Map<String, String>, String> reorderedParameters = new Flipped<>();
        Registry noParameters = new Registry();
        KeyOnly<String> keyOnlyParameter = new KeyOnly<>();
        Lookup<Map<String, String>> mapInterface = null;
        // A List subtype carries no marker of its own, exactly as a plain List does — collections are only
        // marked when sortType names their element type — so there is no chain to hang off it either.
        Bag<Map<String, String>> listSubtype = new Bag<>();
        Pouch<Map<String, String>> setSubtype = new Pouch<>();
        com.google.common.collect.ImmutableMap<String, Map<String, String>> guavaMap =
                com.google.common.collect.ImmutableMap.of();
        java.util.EnumMap<java.time.DayOfWeek, Map<String, String>> enumMap =
                new java.util.EnumMap<>(java.time.DayOfWeek.class);
        Map<String, Iterable<Map<String, String>>> throughAnIterable = new LinkedHashMap<>();
        Map<String, Map<String, Object>> containerHoldingTheUndescribed = new LinkedHashMap<>();
        Payload objectValued = new Payload();
        java.util.Properties properties = new java.util.Properties();
        Map<String, ? extends Map<String, String>> wildcardValue = new LinkedHashMap<>();
        Map<String, List<Object>> collectionOfTheUndescribed = new LinkedHashMap<>();
        SelfMap selfReferential = new SelfMap();
    }

    static class SortMe {
        final int v;
        SortMe(int v) { this.v = v; }
    }

    @SuppressWarnings("unused")
    static class Holder {
        Set<String> aSet = new LinkedHashSet<>(Arrays.asList("x"));
        Map<String, String> aMap = new LinkedHashMap<>();
        List<SortMe> sortList = Arrays.asList(new SortMe(1));
        SortMe[] sortArr = new SortMe[]{new SortMe(2)};
        String plain = "p";
    }

    @Test
    void markSortedFieldsDescribesTheContainerLevelsBelowAMarkedField() {
        Gson gson = GsonProvider.gson(new MatcherConfiguration(), NO_CIRCULAR);
        String map = FieldsIgnorer.MAP_MARKER;
        String collection = FieldsIgnorer.MARKER;

        List<String> keys = new ArrayList<>(gson.toJsonTree(new ChainHolder()).getAsJsonObject().keySet());

        assertThat(keys, contains(
                map + "plainMap",
                map + map + "nestedMap",
                collection + map + "setOfMaps",
                collection + map + collection + map + "setOfMapOfSetOfMap",
                map + collection + collection + map + "mapUnderTwoCollections",
                map + collection + map + "mapOfArrayOfMaps",
                // A chain with no map level below the field describes nothing the sorter does differently,
                // so it is dropped and the field is marked exactly as it is today.
                collection + "setOfLists",
                // A bean stops the walk: its own fields carry their own markers.
                map + "mapToBean",
                // One type argument where a Map has two, so the walk cannot say which is the value.
                map + "mapSubtypeWithOneArgument",
                "typeVariable"));
    }

    @Test
    void markSortedFieldsDescribesSubtypesAndStopsWhereTheDeclarationSaysNothing() {
        Gson gson = GsonProvider.gson(new MatcherConfiguration(), NO_CIRCULAR);
        String map = FieldsIgnorer.MAP_MARKER;
        String collection = FieldsIgnorer.MARKER;
        StringBuilder eightDeep = new StringBuilder();
        for (int level = 0; level < FieldsIgnorer.MAX_CHAIN_DEPTH; level++) {
            eightDeep.append(map);
        }

        List<String> keys = new ArrayList<>(gson.toJsonTree(new SubtypeHolder()).getAsJsonObject().keySet());

        assertThat(keys, contains(
                map + map + "valueOnlyParameter",
                // Reading argument 1 would take K here, so this is the case that says the value is resolved
                // rather than read by position.
                map + map + "reorderedParameters",
                map + map + "noParameters",
                map + map + "keyOnlyParameter",
                map + map + "mapInterface",
                "listSubtype",
                collection + map + "setSubtype",
                map + map + "guavaMap",
                map + map + "enumMap",
                // Iterable is not a Collection, and dropping it from the walk would silently leave the map
                // below it undescribed.
                map + collection + map + "throughAnIterable",
                // A level is described because it IS a container, not because what it holds is known.
                map + map + "containerHoldingTheUndescribed",
                // Object says nothing, so there is no level below either of these.
                map + "objectValued",
                map + "properties",
                // Resolution is invariant, so the wildcard comes back captured and the walk stops.
                map + "wildcardValue",
                // A collection level with no map below it describes nothing the sorter does differently.
                map + "collectionOfTheUndescribed",
                // Maps all the way down: the field's own marker, then the depth cap's worth of levels.
                map + eightDeep + "selfReferential"));
    }

    @Test
    void markSortedFieldsKeepsMarkingAFieldWhoseGenericTypeSaysNothing() {
        // The chain is appended to today's decision rather than derived from the walk. Derived, a field
        // declared by a type variable would lose its marker and stop being sorted.
        Gson gson = GsonProvider.gson(new MatcherConfiguration(), NO_CIRCULAR);
        TypeVariableHolder<Map<String, String>> holder = new TypeVariableHolder<>();
        holder.data = new LinkedHashMap<>();

        assertThat(gson.toJson(holder), containsString(FieldsIgnorer.MAP_MARKER + "data"));
    }

    @Test
    void markSortedFieldsPrefixesSetMapAndTypesToSortFields() {
        MatcherConfiguration config = new MatcherConfiguration().addTypeToSort(SortMe.class);
        Gson gson = GsonProvider.gson(config, NO_CIRCULAR);

        String json = gson.toJson(new Holder());

        // Set, Map, Collection<SortMe> and SortMe[] fields are marked; plain is not. A Map carries its
        // own marker, because only a Map's array holds [key, value] entries.
        assertThat(json, containsString(FieldsIgnorer.MARKER + "aSet"));
        assertThat(json, containsString(FieldsIgnorer.MAP_MARKER + "aMap"));
        assertThat(json, containsString(FieldsIgnorer.MARKER + "sortList"));
        assertThat(json, containsString(FieldsIgnorer.MARKER + "sortArr"));
        assertThat(json, containsString("\"plain\""));
    }

    @Test
    void markSortedFieldsWithoutTypesToSortOnlyMarksSetAndMap() {
        MatcherConfiguration config = new MatcherConfiguration();
        Gson gson = GsonProvider.gson(config, NO_CIRCULAR);

        String json = gson.toJson(new Holder());

        assertThat(json, containsString(FieldsIgnorer.MARKER + "aSet"));
        assertThat(json, containsString(FieldsIgnorer.MAP_MARKER + "aMap"));
        // no typesToSort → Collection/array fields keep their plain names
        assertThat(json, containsString("\"sortList\""));
        assertThat(json, containsString("\"sortArr\""));
    }

    static class Custom {
        final String value;
        Custom(String value) { this.value = value; }
    }

    @Test
    void additionalConfigurationRegistersTypeAdapter() {
        MatcherConfiguration config = new MatcherConfiguration();
        GsonConfiguration additional = new GsonConfiguration();
        JsonSerializer<Custom> serializer = (src, type, ctx) -> new JsonPrimitive("custom:" + src.value);
        additional.addTypeAdapter(Custom.class, serializer);

        Gson gson = GsonProvider.gson(config, NO_CIRCULAR, additional);

        assertThat(gson.toJson(new Custom("x")), is("\"custom:x\""));
    }

    @Test
    void additionalConfigurationRegistersTypeHierarchyAdapter() {
        MatcherConfiguration config = new MatcherConfiguration();
        GsonConfiguration additional = new GsonConfiguration();
        JsonSerializer<Number> serializer = (src, type, ctx) -> new JsonPrimitive("num:" + src);
        additional.addTypeHierarchyAdapter(Number.class, serializer);

        Gson gson = GsonProvider.gson(config, NO_CIRCULAR, additional);

        assertThat(gson.toJson(42), containsString("num:42"));
    }

    @Test
    void additionalConfigurationRegistersTypeAdapterFactoryLeniently() {
        MatcherConfiguration config = new MatcherConfiguration();
        GsonConfiguration additional = new GsonConfiguration();
        // A factory that declines every type; wrapped in LenientTypeAdapterFactory and
        // consulted during serialization without breaking default handling.
        additional.addTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null;
            }
        });

        Gson gson = GsonProvider.gson(config, NO_CIRCULAR, additional);

        assertThat(gson.toJson(new Custom("y")), containsString("value"));
    }

    @Test
    void additionalFactoryProvidingAdapterIsUsedLeniently() {
        MatcherConfiguration config = new MatcherConfiguration();
        GsonConfiguration additional = new GsonConfiguration();
        additional.addTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() != Custom.class) {
                    return null;
                }
                return (TypeAdapter<T>) new TypeAdapter<Custom>() {
                    @Override
                    public void write(JsonWriter out, Custom value) throws IOException {
                        out.value("lenient:" + value.value);
                    }

                    @Override
                    public Custom read(JsonReader in) throws IOException {
                        return new Custom(in.nextString());
                    }
                };
            }
        });

        Gson gson = GsonProvider.gson(config, NO_CIRCULAR, additional);

        // The custom adapter (wrapped in LenientTypeAdapterFactory) drives serialization.
        assertThat(gson.toJson(new Custom("z")), is("\"lenient:z\""));
    }
}
