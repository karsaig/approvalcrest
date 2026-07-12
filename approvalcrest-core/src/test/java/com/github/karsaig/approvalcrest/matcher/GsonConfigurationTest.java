package com.github.karsaig.approvalcrest.matcher;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link GsonConfiguration}, the mutable holder that accumulates custom Gson
 * registrations before a {@code Gson} instance is built. The business behaviour under test is
 * that multiple adapters registered for the same key accumulate rather than overwrite, and that
 * the fallback-skip set is exposed as a read-only view.
 */
public class GsonConfigurationTest {

    @Test
    void multipleTypeAdaptersForSameTypeAccumulate() {
        GsonConfiguration configuration = new GsonConfiguration();
        Type key = String.class;
        Object first = new Object();
        Object second = new Object();

        configuration.addTypeAdapter(key, first);
        configuration.addTypeAdapter(key, second);

        assertThat(configuration.getTypeAdapters().get(key), contains(first, second));
    }

    @Test
    void typeAdaptersForDifferentTypesAreKeptSeparate() {
        GsonConfiguration configuration = new GsonConfiguration();
        Object stringAdapter = new Object();
        Object integerAdapter = new Object();

        configuration.addTypeAdapter(String.class, stringAdapter);
        configuration.addTypeAdapter(Integer.class, integerAdapter);

        assertThat(configuration.getTypeAdapters().get(String.class), contains(stringAdapter));
        assertThat(configuration.getTypeAdapters().get(Integer.class), contains(integerAdapter));
    }

    @Test
    void multipleTypeHierarchyAdaptersForSameClassAccumulate() {
        GsonConfiguration configuration = new GsonConfiguration();
        Object first = new Object();
        Object second = new Object();

        configuration.addTypeHierarchyAdapter(Number.class, first);
        configuration.addTypeHierarchyAdapter(Number.class, second);

        assertThat(configuration.getTypeHierarchyAdapter().get(Number.class), contains(first, second));
    }

    @Test
    void typeAdapterFactoriesAccumulateInOrder() {
        GsonConfiguration configuration = new GsonConfiguration();
        TypeAdapterFactory first = nullFactory();
        TypeAdapterFactory second = nullFactory();

        configuration.addTypeAdapterFactory(first);
        configuration.addTypeAdapterFactory(second);

        assertThat(configuration.getTypeAdapterFactories(), contains(first, second));
    }

    private static TypeAdapterFactory nullFactory() {
        return new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return null;
            }
        };
    }

    @Test
    void skipTypesAreRecorded() {
        GsonConfiguration configuration = new GsonConfiguration();

        configuration.addTypeToSkipInFallbackFactories(Iterable.class);

        assertThat(configuration.getTypesToSkipInFallbackFactories(), hasSize(1));
        assertThat(configuration.getTypesToSkipInFallbackFactories().contains(Iterable.class), is(true));
    }

    @Test
    void skipTypesSetIsUnmodifiable() {
        GsonConfiguration configuration = new GsonConfiguration();
        configuration.addTypeToSkipInFallbackFactories(Iterable.class);

        assertThrows(UnsupportedOperationException.class,
                () -> configuration.getTypesToSkipInFallbackFactories().add(Number.class));
    }
}
