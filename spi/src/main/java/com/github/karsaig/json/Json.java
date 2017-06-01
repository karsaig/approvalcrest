package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Very limited Json functionality that we need.
 */
public interface Json {
    @NotNull String toJson(@Nullable Object src);

    @NotNull JsonElement toJsonTree(@Nullable Object src);
}
