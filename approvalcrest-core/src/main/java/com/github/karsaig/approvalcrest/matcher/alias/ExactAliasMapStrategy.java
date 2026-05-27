package com.github.karsaig.approvalcrest.matcher.alias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tier-2 (fast-path) strategy used when every entry in the map has an exact field name,
 * an exact value, a static alias string, and no path constraint.
 *
 * <p>Resolution is O(1): two {@link HashMap} lookups with no predicate evaluation and no iteration.
 * Last-registered-wins is achieved by iterating entries in registration order and overwriting
 * earlier entries with the same (field, value) key.
 */
final class ExactAliasMapStrategy implements ResolveStrategy {

    /** field → value → alias */
    private final Map<String, Map<String, String>> exactMap;
    private final List<AliasEntry> entries;

    ExactAliasMapStrategy(List<AliasEntry> entries) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));

        Map<String, Map<String, String>> building = new HashMap<>();
        for (AliasEntry e : entries) {
            building.computeIfAbsent(e.exactFieldKey, k -> new HashMap<>())
                    .put(e.exactValueKey, e.staticAlias);
        }

        Map<String, Map<String, String>> unmod = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> me : building.entrySet()) {
            unmod.put(me.getKey(), Collections.unmodifiableMap(me.getValue()));
        }
        this.exactMap = Collections.unmodifiableMap(unmod);
    }

    @Override
    public Optional<String> resolve(String path, String fieldName, String coercedValue) {
        Map<String, String> forField = exactMap.get(fieldName);
        if (forField == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(forField.get(coercedValue));
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public List<AliasEntry> getEntries() {
        return entries;
    }
}
