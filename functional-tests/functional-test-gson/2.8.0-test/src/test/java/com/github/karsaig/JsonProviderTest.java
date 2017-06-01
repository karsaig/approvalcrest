package com.github.karsaig;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.github.karsaig.json.JsonProvider;

import com.github.karsaig.json.Json;

public class JsonProviderTest {
    @Test
    public void shouldWhen() {
        // GIVEN
        List<Class<?>> typesToIgnore = emptyList();
        List<Matcher<String>> fieldsToIgnore = Collections.emptyList();
        Set<Class<?>> circularReferenceTypes = Collections.emptySet();

        // WHEN
        Json actualJson = JsonProvider.json(typesToIgnore, fieldsToIgnore, circularReferenceTypes);

        // THEN
        assertNotNull(actualJson);
    }
}
