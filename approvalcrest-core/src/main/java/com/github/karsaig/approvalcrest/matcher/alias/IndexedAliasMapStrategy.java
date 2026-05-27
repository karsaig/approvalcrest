package com.github.karsaig.approvalcrest.matcher.alias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tier-1 (general) strategy used when the map contains entries with regex field matchers,
 * path constraints, function resolvers, or value-only entries (no field constraint).
 *
 * <p>At construction time, entries are split into two buckets:
 * <ul>
 *   <li><b>exactFieldIndex</b> — entries with an exact-string field key, indexed by that key.
 *       Each bucket stores entry indices in descending order (last-registered first).</li>
 *   <li><b>wildcardIndices</b> — all other entries (null field matcher, regex, or path constraint),
 *       also in descending index order.</li>
 * </ul>
 *
 * <p>{@link #resolve} merges both descending index arrays with a two-pointer scan, visiting
 * candidates from highest to lowest insertion index and stopping at the first match.
 * This preserves last-registered-wins ordering across both buckets, while skipping all
 * exact-field entries that don't match the queried field name.
 */
final class IndexedAliasMapStrategy implements ResolveStrategy {

    private static final int[] EMPTY_INDICES = new int[0];

    private final List<AliasEntry> entries;
    /** fieldName → entry indices into {@link #entries}, sorted descending (last-registered first). */
    private final Map<String, int[]> exactFieldIndex;
    /** Indices of wildcard/regex/path entries, sorted descending. */
    private final int[] wildcardIndices;

    IndexedAliasMapStrategy(List<AliasEntry> entries) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));

        Map<String, List<Integer>> fieldBuckets = new HashMap<>();
        List<Integer> wildcardBucket = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            AliasEntry e = entries.get(i);
            if (e.exactFieldKey != null) {
                fieldBuckets.computeIfAbsent(e.exactFieldKey, k -> new ArrayList<>()).add(i);
            } else {
                wildcardBucket.add(i);
            }
        }

        Map<String, int[]> fieldIndex = new HashMap<>();
        for (Map.Entry<String, List<Integer>> me : fieldBuckets.entrySet()) {
            fieldIndex.put(me.getKey(), toDescendingArray(me.getValue()));
        }
        this.exactFieldIndex = Collections.unmodifiableMap(fieldIndex);
        this.wildcardIndices = toDescendingArray(wildcardBucket);
    }

    /** Converts a list of ascending insertion indices into a descending int array. */
    private static int[] toDescendingArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(list.size() - 1 - i);
        }
        return arr;
    }

    /**
     * Two-pointer descending merge: at each step, pick the candidate with the higher insertion
     * index (= later-registered), check if it matches, and return on first match.
     */
    @Override
    public Optional<String> resolve(String path, String fieldName, String coercedValue) {
        int[] exactCandidates = exactFieldIndex.getOrDefault(fieldName, EMPTY_INDICES);
        int ei = 0;
        int wi = 0;

        while (ei < exactCandidates.length || wi < wildcardIndices.length) {
            int ev = ei < exactCandidates.length ? exactCandidates[ei] : -1;
            int wv = wi < wildcardIndices.length ? wildcardIndices[wi] : -1;

            int idx;
            if (ev >= wv) {
                idx = ev;
                ei++;
            } else {
                idx = wv;
                wi++;
            }

            AliasEntry entry = entries.get(idx);
            if (entry.matches(path, fieldName, coercedValue)) {
                return Optional.of(entry.resolve(coercedValue));
            }
        }
        return Optional.empty();
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
