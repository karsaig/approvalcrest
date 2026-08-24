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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
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
