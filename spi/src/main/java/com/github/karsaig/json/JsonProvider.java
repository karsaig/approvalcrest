/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.json;

import static com.github.karsaig.ServiceLoaders.loadService;

import java.util.List;
import java.util.Set;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

public final class JsonProvider {
    public static @NotNull Json json(List<Class<?>> typesToIgnore, List<Matcher<String>> fieldsToIgnore, Set<Class<?>> circularReferenceTypes) {
        return getDefaultJsonBuilder(typesToIgnore, fieldsToIgnore, circularReferenceTypes).build();
    }

    public static @NotNull Json json(List<Class<?>> typesToIgnore, List<Matcher<String>> fieldsToIgnore, Set<Class<?>> circularReferenceTypes, JsonConfiguration additionalConfig) {
        JsonBuilder jsonBuilder = getDefaultJsonBuilder(typesToIgnore, fieldsToIgnore, circularReferenceTypes);

        if (additionalConfig != null) {
            jsonBuilder.addExtraConfiguration(additionalConfig);
        }

        return jsonBuilder.build();
    }

    private static JsonBuilder getDefaultJsonBuilder(List<Class<?>> typesToIgnore, List<Matcher<String>> fieldsToIgnore, Set<Class<?>> circularReferenceTypes) {
        JsonBuilder builder = loadService(JsonBuilder.class);

        return builder.initialize()
                .setPrettyPrinting()
                .registerTypesToIgnore(typesToIgnore)
                .registerFieldsToIgnore(fieldsToIgnore)
                .registerCircularReferenceTypes(circularReferenceTypes);
    }
}
