/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest;

import com.google.gson.JsonElement;
import org.hamcrest.Matcher;

/**
 * A rule describing array elements to remove before comparison. The {@code path} points at a
 * field <em>within each element</em> of an array; the innermost array on the path is the one
 * filtered. An element is removed when the value of that leaf field satisfies the rule.
 *
 * @see FieldsIgnorer#removeMatchingElements
 */
public class ElementIgnoreRule {

    private final String path;
    private final Matcher<?> valueMatcher;
    private final String value;

    private ElementIgnoreRule(String path, Matcher<?> valueMatcher, String value) {
        this.path = path;
        this.valueMatcher = valueMatcher;
        this.value = value;
    }

    /**
     * Remove elements whose leaf field value (coerced to a Java value) satisfies the matcher.
     */
    public static ElementIgnoreRule of(String path, Matcher<?> valueMatcher) {
        return new ElementIgnoreRule(path, valueMatcher, null);
    }

    /**
     * Remove elements whose leaf field, coerced to a String, equals {@code value}.
     */
    public static ElementIgnoreRule ofValue(String path, String value) {
        return new ElementIgnoreRule(path, null, value);
    }

    public String getPath() {
        return path;
    }

    /**
     * Whether the given leaf field element satisfies this rule and its containing element should
     * therefore be removed.
     */
    public boolean matches(JsonElement leaf) {
        if (leaf == null) {
            return false;
        }
        if (valueMatcher != null) {
            return valueMatcher.matches(JsonElementUtil.jsonElementToJavaValue(leaf));
        }
        return leaf.isJsonPrimitive() && value.equals(leaf.getAsString());
    }
}
