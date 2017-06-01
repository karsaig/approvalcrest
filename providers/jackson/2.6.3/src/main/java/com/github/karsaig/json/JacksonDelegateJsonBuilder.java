package com.github.karsaig.json;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import java.util.List;
import java.util.Set;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

import com.github.karsaig.json.ignore.MatcherBasedFieldIgnoringModule;
import com.github.karsaig.json.ignore.TypeBasedFieldIgnoringModule;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableSet;

public class JacksonDelegateJsonBuilder implements JsonBuilder {
    private ObjectMapper delegate;

    private Set<Class<?>> typesToIgnore;
    private List<Matcher<String>> fieldsToIgnore;
    private Set<Class<?>> circularReferenceTypes;
    private JsonConfiguration additionalConfig;

    @Override
    public @NotNull JsonBuilder initialize() {
        delegate = new ObjectMapper();
        delegate.registerModule(new GuavaModule());
        delegate.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        delegate.enable(Feature.ALLOW_COMMENTS);
        return this;
    }

    @Override
    public @NotNull JsonBuilder registerTypesToIgnore(@NotNull List<Class<?>> typesToIgnore) {
        this.typesToIgnore = ImmutableSet.copyOf(typesToIgnore);
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
        delegate.enable(INDENT_OUTPUT);
        return this;
    }

    @Override
    public @NotNull JsonBuilder addExtraConfiguration(@NotNull JsonConfiguration additionalConfig) {
        this.additionalConfig = additionalConfig;
        return this;
    }

    @Override
    public @NotNull Json build() {
        // FIXME: add circular dependency handling

        delegate.registerModule(new TypeBasedFieldIgnoringModule(typesToIgnore));
        delegate.registerModule(new MatcherBasedFieldIgnoringModule(fieldsToIgnore));

        // FIXME: add additional config handling

        return new JacksonDelegateJson(delegate);
    }
}
