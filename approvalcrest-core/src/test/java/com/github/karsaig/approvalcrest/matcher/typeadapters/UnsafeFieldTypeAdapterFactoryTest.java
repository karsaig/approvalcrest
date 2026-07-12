package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit tests for {@link UnsafeFieldTypeAdapterFactory}.
 * <p>
 * This fallback factory only produces an adapter for types that live in a <em>locked</em> module
 * (one whose package cannot be opened for reflective access). On a JDK where runtime module
 * opening succeeds — the normal case, including the environment coverage is measured in — the
 * factory correctly declines every type, letting Gson's own reflective adapter serialise it.
 * The business contract verified here is that the factory never intercepts types Gson already
 * handles natively, nor types the caller explicitly registered to be skipped.
 */
public class UnsafeFieldTypeAdapterFactoryTest {

    private final Gson gson = new Gson();

    static class Base {
    }

    static class Sub extends Base {
    }

    static class Pojo {
        @SuppressWarnings("unused")
        int value = 1;
    }

    private <T> TypeAdapter<T> create(UnsafeFieldTypeAdapterFactory factory, Class<T> type) {
        return factory.create(gson, TypeToken.get(type));
    }

    @Test
    void forceModeIndependentSkipTypesAreDeclined() {
        UnsafeFieldTypeAdapterFactory factory = new UnsafeFieldTypeAdapterFactory();

        // Each of these exercises a distinct branch of shouldSkip(...).
        assertThat(create(factory, int[].class), nullValue());          // array
        assertThat(create(factory, int.class), nullValue());            // primitive
        assertThat(create(factory, TimeUnit.class), nullValue());       // enum
        assertThat(create(factory, String.class), nullValue());         // String
        assertThat(create(factory, Integer.class), nullValue());        // Number
        assertThat(create(factory, Boolean.class), nullValue());        // Boolean
        assertThat(create(factory, Character.class), nullValue());      // Character
        assertThat(create(factory, ArrayList.class), nullValue());      // Iterable
        assertThat(create(factory, List.class), nullValue());           // Iterable (interface)
        assertThat(create(factory, HashMap.class), nullValue());        // Map
        assertThat(create(factory, Map.class), nullValue());            // Map (interface)
        assertThat(create(factory, Optional.class), nullValue());       // java.util.Optional
    }

    @Test
    void guavaOptionalIsDeclined() {
        UnsafeFieldTypeAdapterFactory factory = new UnsafeFieldTypeAdapterFactory();

        assertThat(create(factory, com.google.common.base.Optional.class), nullValue());
    }

    @Test
    void customAdditionalSkipTypeIsDeclined() {
        UnsafeFieldTypeAdapterFactory factory =
                new UnsafeFieldTypeAdapterFactory(Collections.singleton(Base.class));

        // Sub is assignable to the registered skip type Base → declined.
        assertThat(create(factory, Sub.class), nullValue());
    }

    @Test
    void plainPojoIsNotInterceptedWhenModuleIsOpen() {
        UnsafeFieldTypeAdapterFactory factory = new UnsafeFieldTypeAdapterFactory();

        // A classpath POJO is not skipped and — because its (unnamed) module is open — is
        // not in a locked module either, so the factory declines and Gson handles it.
        assertThat(create(factory, Pojo.class), nullValue());
    }
}
