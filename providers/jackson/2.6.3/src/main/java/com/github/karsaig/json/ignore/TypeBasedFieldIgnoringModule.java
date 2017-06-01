package com.github.karsaig.json.ignore;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class TypeBasedFieldIgnoringModule extends AbstractFieldIgnoringModule {
    private static final String MODULE_NAME = "TypeBasedFieldIgnoringModule";

    public TypeBasedFieldIgnoringModule(Class<?>... typesToIgnore) {
        this(ImmutableSet.copyOf(typesToIgnore));
    }

    public TypeBasedFieldIgnoringModule(Set<Class<?>> typesToIgnore) {
        super(new TypeBasedFieldIgnoringBeanSerializerModifier(typesToIgnore), MODULE_NAME);
    }
}
