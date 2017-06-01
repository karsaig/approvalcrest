package com.github.karsaig.json.graph;

import java.lang.reflect.Type;

import com.github.karsaig.json.JsonBuilder;

public interface GraphAdapterBuilder {

    GraphAdapterBuilder addType(Type type);

    GraphAdapterBuilder registerOn(JsonBuilder jsonBuilder);
}
