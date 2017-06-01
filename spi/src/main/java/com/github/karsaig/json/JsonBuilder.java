package com.github.karsaig.json;

import java.util.List;
import java.util.Set;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

public interface JsonBuilder {
    @NotNull JsonBuilder initialize();

    @NotNull JsonBuilder registerTypesToIgnore(@NotNull List<Class<?>> typesToIgnore);

    @NotNull JsonBuilder registerFieldsToIgnore(@NotNull List<Matcher<String>> fieldsToIgnore);

    @NotNull JsonBuilder registerCircularReferenceTypes(@NotNull Set<Class<?>> circularReferenceTypes);

    @NotNull JsonBuilder setPrettyPrinting();

    @NotNull JsonBuilder addExtraConfiguration(@NotNull JsonConfiguration additionalConfig);

    @NotNull Json build();
}
