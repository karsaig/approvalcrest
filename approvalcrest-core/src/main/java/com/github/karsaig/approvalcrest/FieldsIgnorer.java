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

import com.github.karsaig.approvalcrest.matcher.machinereadable.IgnoredFieldsTracker;
import com.github.karsaig.approvalcrest.matcher.machinereadable.SortedFieldsTracker;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import com.google.gson.*;
import org.hamcrest.Matcher;

import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Responsible for traversing the Json tree and ignore the specified set of field paths.
 */
public class FieldsIgnorer {
    public static final String MARKER = "!_TO_BE_SORTED_!";
    /**
     * Marks a {@code Map}-typed field, where {@link #MARKER} marks a {@code Set} or a type-selected
     * collection. Both queue the field for sorting; only this one says the field's array elements are
     * {@code [key, value]} entries rather than a collection, which is what stops the sorter reordering a
     * key with its value. Like {@link #MARKER} it is stripped before anything is written, so it never
     * reaches a file.
     *
     * <p>Either sentinel is written again for each container level below the field that its declared type
     * describes — this one for a map level, {@link #MARKER} for a collection or array. That sequence is a
     * <em>marker chain</em>, and it is the only thing that can say which arrays hold entries rather than
     * ordinary elements, since JSON alone cannot. An empty chain means nothing is known about a level,
     * which is what every level had before chains existed.
     */
    public static final String MAP_MARKER = "!_TO_BE_SORTED_MAP_!";
    /** Matches either marker. Neither is a prefix of the other, so the alternation order is irrelevant. */
    public static final Pattern MARKER_PATTERN = Pattern.compile(MAP_MARKER + "|" + MARKER);
    private static final String PATH_SEPARATOR_PATTERN = Pattern.quote(".");

    /**
     * How many levels a marker chain may describe. Reaching it truncates the chain, so the levels below are
     * undescribed and keep the behaviour they had before chains existed. No real declaration or object graph
     * comes close; this only stops a self-referential one walking for ever.
     */
    public static final int MAX_CHAIN_DEPTH = 8;

    public static String removeSetMarker(String json) {
        return MARKER_PATTERN.matcher(json).replaceAll("");
    }

    private static boolean chainStartsAtMapLevel(String chain) {
        return chain.startsWith(MAP_MARKER);
    }

    /** Neither sentinel is a prefix of the other, as {@link #MARKER_PATTERN} relies on, so one test does. */
    private static boolean chainStartsAtCollectionLevel(String chain) {
        return chain.startsWith(MARKER);
    }

    /**
     * The marker chain describing {@code root} itself, for the routes where the root has no field name to
     * carry one. Derived from the object rather than a declared type, so it sees what generics cannot -- a
     * {@code Map<K, Object>} whose values are all maps included.
     *
     * <p>A level is appended only while every value or element at that level is the same kind of container,
     * so nothing is claimed that is not true of every entry. As with the declared-type walk, a chain with no
     * map level in it describes nothing the sorter does differently and is dropped.
     */
    public static String runtimeMarkerChain(Object root) {
        return truncateAfterLastMapLevel(describeLevels(root, 0));
    }

    /**
     * The chain {@code value} describes: its own level, then whatever every child of it agrees on.
     * Descending into one representative child would be cheaper, but the chain would then be a function of
     * iteration order -- {@code Map.values()} has none to speak of -- so two maps that are equal could be
     * described differently and compare unequal, with no file to regenerate out of it.
     */
    private static String describeLevels(Object value, int depth) {
        if (depth >= MAX_CHAIN_DEPTH) {
            return "";
        }
        if (value instanceof Map) {
            return MAP_MARKER + levelEveryChildAgreesOn(((Map<?, ?>) value).values(), depth + 1);
        }
        if (value instanceof Collection) {
            return MARKER + levelEveryChildAgreesOn((Collection<?>) value, depth + 1);
        }
        if (value instanceof Object[]) {
            return MARKER + levelEveryChildAgreesOn(Arrays.asList((Object[]) value), depth + 1);
        }
        return "";
    }

    /**
     * What every child describes, or empty as soon as two of them differ. An empty container describes
     * nothing below it either: there is no entry to be right about.
     */
    private static String levelEveryChildAgreesOn(Collection<?> children, int depth) {
        String agreed = null;
        for (Object child : children) {
            String childChain = describeLevels(child, depth);
            if (childChain.isEmpty()) {
                return "";
            }
            if (agreed == null) {
                agreed = childChain;
            } else if (!agreed.equals(childChain)) {
                return "";
            }
        }
        return agreed == null ? "" : agreed;
    }

    /**
     * Drops everything after the last map level: only a map level changes what the sorter does, so a
     * trailing collection token describes nothing and would lengthen a chain for no reason.
     */
    public static String truncateAfterLastMapLevel(String chain) {
        int lastMapLevel = chain.lastIndexOf(MAP_MARKER);
        return lastMapLevel < 0 ? "" : chain.substring(0, lastMapLevel + MAP_MARKER.length());
    }

    /** The chain describing the level below the one {@code chain} starts at, empty when there is none. */
    private static String chainTail(String chain) {
        if (chain.startsWith(MAP_MARKER)) {
            return chain.substring(MAP_MARKER.length());
        }
        if (chain.startsWith(MARKER)) {
            return chain.substring(MARKER.length());
        }
        return "";
    }

    public static void applyRootCollectionSorting(JsonElement filteredJson, Object objectForTypeCheck, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort) {
        applyRootCollectionSorting(filteredJson, objectForTypeCheck, fieldMatchersToSort, pathsToSort, Collections.<Class<?>>emptyList(), null);
    }

    public static void applyRootCollectionSorting(JsonElement filteredJson, Object objectForTypeCheck, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort, Collection<Class<?>> typesToSort) {
        applyRootCollectionSorting(filteredJson, objectForTypeCheck, fieldMatchersToSort, pathsToSort, typesToSort, null);
    }

    public static void applyRootCollectionSorting(JsonElement filteredJson, Object objectForTypeCheck, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort, Collection<Class<?>> typesToSort, SortedFieldsTracker tracker) {
        applyRootCollectionSorting(filteredJson, objectForTypeCheck, fieldMatchersToSort, pathsToSort, typesToSort, tracker, false);
    }

    /**
     * @param keepMapEntryOrder see {@link #applySorting(JsonElement, Map, List, boolean, SortedFieldsTracker, boolean)}.
     *        A root map has no field name to mark, so its entries are recognised from the object's own type.
     */
    public static void applyRootCollectionSorting(JsonElement filteredJson, Object objectForTypeCheck, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort, Collection<Class<?>> typesToSort, SortedFieldsTracker tracker, boolean keepMapEntryOrder) {
        if (objectForTypeCheck != null && (Set.class.isAssignableFrom(objectForTypeCheck.getClass()) || Map.class.isAssignableFrom(objectForTypeCheck.getClass()))) {
            // Sets and Maps are always sorted by their root representation (no meaningful order) — NOT tracked
            sortJsonArray(filteredJson.getAsJsonArray(), pathsToSort.getOrDefault("", emptyList()), fieldMatchersToSort,
                    runtimeMarkerChain(objectForTypeCheck), keepMapEntryOrder);
        } else if (objectForTypeCheck != null && Collection.class.isAssignableFrom(objectForTypeCheck.getClass())) {
            // Other Collections (e.g. List) are sorted only when explicitly configured via "" path
            List<SortField<String>> rootSortFields = pathsToSort.getOrDefault("", emptyList());
            if (!rootSortFields.isEmpty() || !fieldMatchersToSort.isEmpty()) {
                sortJsonArray(filteredJson.getAsJsonArray(), rootSortFields, fieldMatchersToSort, "", keepMapEntryOrder);
                if (tracker != null) {
                    recordSortMatches(tracker, "", rootSortFields, fieldMatchersToSort);
                }
            } else if (!typesToSort.isEmpty() && collectionElementMatchesTypesToSort((Collection<?>) objectForTypeCheck, typesToSort)) {
                // Type-based sorting — NOT tracked (same as type-based ignoring)
                sortJsonArray(filteredJson.getAsJsonArray(), emptyList(), fieldMatchersToSort, "", keepMapEntryOrder);
            }
        }
    }

    public static JsonElement findPaths(JsonElement preComputedJson, Object objectForTypeCheck, Set<String> pathsToFind, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort) {
        JsonElement filteredJson = findPaths(preComputedJson, pathsToFind);
        String rootChain = runtimeMarkerChain(objectForTypeCheck);
        applySorting(filteredJson, pathsToSort, fieldMatchersToSort, true, null, true, rootChain);
        if (objectForTypeCheck != null && (Set.class.isAssignableFrom(objectForTypeCheck.getClass()) || Map.class.isAssignableFrom(objectForTypeCheck.getClass()))) {
            // Sets and Maps are always sorted by their root representation (no meaningful order). A root map
            // has no field name, so its entries are recognised from the type, as applyRootCollectionSorting does.
            sortJsonArray(filteredJson.getAsJsonArray(), pathsToSort.getOrDefault("", emptyList()), fieldMatchersToSort,
                    rootChain, true);
        } else if (objectForTypeCheck != null && Collection.class.isAssignableFrom(objectForTypeCheck.getClass())) {
            // Other Collections (e.g. List) are sorted only when explicitly configured via "" path
            List<SortField<String>> rootSortFields = pathsToSort.getOrDefault("", emptyList());
            if (!rootSortFields.isEmpty() || !fieldMatchersToSort.isEmpty()) {
                // true, as everywhere else on this route: with no chain to suppress anything the flag has
                // no effect here, and two spellings of one meaning read as though they differed.
                sortJsonArray(filteredJson.getAsJsonArray(), rootSortFields, fieldMatchersToSort, "", true);
            }
        }
        return filteredJson;
    }

    public static JsonElement findPaths(Gson gson, Object object, Set<String> pathsToFind, List<SortField<Matcher<String>>> fieldMatchersToSort, Map<String, List<SortField<String>>> pathsToSort) {
        JsonElement jsonElement = gson.toJsonTree(object);

        JsonElement filteredJson = findPaths(jsonElement, pathsToFind);
        String rootChain = runtimeMarkerChain(object);
        applySorting(filteredJson, pathsToSort, fieldMatchersToSort, true, null, true, rootChain);
        if (object != null && (Set.class.isAssignableFrom(object.getClass()) || Map.class.isAssignableFrom(object.getClass()))) {
            // Sets and Maps are always sorted by their root representation (no meaningful order). A root map
            // has no field name, so its entries are recognised from the type, as applyRootCollectionSorting does.
            sortJsonArray(filteredJson.getAsJsonArray(), pathsToSort.getOrDefault("", emptyList()), fieldMatchersToSort,
                    rootChain, true);
            return filteredJson;
        } else if (object != null && Collection.class.isAssignableFrom(object.getClass())) {
            // Other Collections (e.g. List) are sorted only when explicitly configured via "" path
            List<SortField<String>> rootSortFields = pathsToSort.getOrDefault("", emptyList());
            if (!rootSortFields.isEmpty() || !fieldMatchersToSort.isEmpty()) {
                // true, as everywhere else on this route: with no chain to suppress anything the flag has
                // no effect here, and two spellings of one meaning read as though they differed.
                sortJsonArray(filteredJson.getAsJsonArray(), rootSortFields, fieldMatchersToSort, "", true);
            }
        }
        return filteredJson;
    }

    public static JsonElement findPaths(JsonElement jsonElement, Set<String> pathsToFind) {
        return findPaths(jsonElement, pathsToFind, null, null);
    }

    /**
     * Tracked version of findPaths. When tracker is non-null, records which fields were actually
     * removed and why. The reasonMap maps each path to its reason (IGNORE_PATH or CUSTOM_MATCHER).
     */
    public static JsonElement findPaths(JsonElement jsonElement, Set<String> pathsToFind,
                                         IgnoredFieldsTracker tracker,
                                         Map<String, IgnoredFieldsTracker.Reason> reasonMap) {
        if (jsonElement == null || jsonElement.isJsonNull() || pathsToFind.isEmpty()) {
            return jsonElement;
        }

        String pathToFind = headOf(pathsToFind);
        List<String> pathSegments = asList(pathToFind.split(PATH_SEPARATOR_PATTERN));
        try {
            // Two facts, not one. The return value means "a direct child of mine went", which is
            // what the cascade logic needs and must not be widened -- widening it would delete
            // parents that still hold other fields. But it is the wrong question for the tracker:
            // .ignoring("a.b.c") where a.b keeps other fields removes c and returns false all the
            // way up, so the rule would go unrecorded. The accumulator answers "did anything change".
            boolean[] changedAnywhere = new boolean[1];
            findPath(jsonElement, pathToFind, pathSegments, false, changedAnywhere);
            if (changedAnywhere[0] && tracker != null && reasonMap != null) {
                IgnoredFieldsTracker.Reason reason = reasonMap.getOrDefault(pathToFind, IgnoredFieldsTracker.Reason.IGNORE_PATH);
                tracker.recordIgnoredRule(pathToFind, reason);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(pathToFind + " does not exist", e);
        }
        return findPaths(jsonElement, removePathFromSet(pathsToFind, pathToFind), tracker, reasonMap);
    }

    private static Set<String> removePathFromSet(Set<String> setToRemoveFrom, String stringToRemove) {
        Set<String> set = new HashSet<>(setToRemoveFrom);
        set.remove(stringToRemove);
        return set;
    }

    /**
     * @param parentIsArray whether {@code jsonElement} is itself an element of an array. Only the
     *                      orphan cleanup below needs it, and only to tell a complex-key map's
     *                      {@code [key, value]} pair — always an array inside an array — from a
     *                      collection held by an object property.
     * @param changedAnywhere single-element accumulator set whenever a removal actually happens,
     *                      anywhere below this call. Distinct from the return value, which reports
     *                      only whether a direct child of {@code jsonElement} went.
     */
    private static boolean findPath(JsonElement jsonElement, String pathToFind, List<String> pathSegments,
                                    boolean parentIsArray, boolean[] changedAnywhere) {
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            int sizeBeforeRemoval = jsonArray.size();
            Iterator<JsonElement> iterator = jsonArray.iterator();
            boolean result = false;
            while (iterator.hasNext()) {
                JsonElement arrayElement = iterator.next();
                if (arrayElement.isJsonNull() || arrayElement.isJsonPrimitive()) {
                    continue;
                }
                boolean ignoredElement = findPath(arrayElement, pathToFind, pathSegments, true, changedAnywhere);
                if (ignoredElement && JsonElementUtil.isEmpty(arrayElement)) {
                    iterator.remove();
                    result |= true;
                }
            }
            // If non-primitive elements were removed and only primitives/nulls remain,
            // those are orphaned map values whose complex key was entirely stripped by
            // ignoring.  Clear them so the inner array becomes empty and the outer
            // loop's existing isEmpty check can remove the whole entry.
            //
            // Only for something shaped like a map entry: an array of exactly two elements that is
            // itself an element of an array. A complex-key map serialises each entry as [key, value]
            // inside the map's array, so it always matches; a collection held by an object property
            // never does, and neither does a nested collection of any other length.
            //
            // Clearing anything else discards values the caller never mentioned. Ignoring "list.a"
            // over {"list":[{"a":1},"keep-me",42]} empties the object, leaves only primitives, and
            // deleting those too drops the whole field. The two-element test is what extends that
            // protection past the outermost level: without it, the same loss happens one level
            // down, in {"ll":[[{"a":1},"keep",7]]}.
            //
            // A nested collection of exactly two elements is still indistinguishable from a map
            // entry and is still cleared. That residue is accepted.
            if (result && parentIsArray && sizeBeforeRemoval == 2) {
                boolean hasNonPrimitive = false;
                for (JsonElement remaining : jsonArray) {
                    if (!remaining.isJsonNull() && !remaining.isJsonPrimitive()) {
                        hasNonPrimitive = true;
                        break;
                    }
                }
                if (!hasNonPrimitive) {
                    Iterator<JsonElement> cleanup = jsonArray.iterator();
                    while (cleanup.hasNext()) {
                        cleanup.next();
                        cleanup.remove();
                    }
                }
            }
            return result;
        } else {
            String field = headOf(pathSegments);
            if (pathSegments.size() == 1) {
                // Every removal anywhere cascades from a leaf removal here, so this is the one place
                // the accumulator has to be set.
                boolean removedLeaf = ignorePath(jsonElement, pathToFind);
                if (removedLeaf) {
                    changedAnywhere[0] = true;
                }
                return removedLeaf;
            } else {
                if (jsonElement.isJsonObject()) {
                    JsonObject jo = jsonElement.getAsJsonObject();
                    if (JsonElementUtil.WILDCARD.equals(field)) {
                        return ignoreUnderEveryNamedChild(jo, pathToFind, pathSegments, changedAnywhere);
                    }
                    // The field naming strategy prefixes Set-typed fields with MARKER and Map-typed fields
                    // with MAP_MARKER, and repeats a sentinel per container level it can describe below the
                    // field, so the child may sit under the bare name or any such sequence. Take the key as
                    // well as the value: removing an emptied child by the bare name silently does nothing
                    // when it was found under a prefixed one, leaving an empty husk whose prefix
                    // removeSetMarker strips, so the file shows an empty collection where the field
                    // should have gone.
                    // Prefers the bare name when an object somehow holds both. That is reachable —
                    // a subclass widening an inherited field to a Set or Map gets both names, since
                    // Gson's duplicate-name check runs after the naming strategy — and in that shape
                    // only the bare twin is descended into, whereas the leaf branch below removes
                    // both names. Not worth resolving here: such an object is written with two
                    // identical JSON keys once the prefix is stripped, which cannot be read back at
                    // all, so the shape is broken well before this line sees it.
                    String childKey = childKeyOf(jo, field);
                    JsonElement child = childKey == null ? null : jo.get(childKey);
                    if (child == null) {
                        // Try descending through GraphAdapter envelope keys. Every envelope has
                        // to be visited: one graph can hold several objects carrying the same
                        // field, and stopping at the first leaves the rest in place. Since this
                        // runs separately over the actual and the expected side, that filters the
                        // two differently and fails the comparison on data rather than on the
                        // ignore rule. Keys are snapshotted because entrySet() is a live view and
                        // an emptied envelope is removed below.
                        boolean anyEnvelopeChanged = false;
                        for (String envelopeKey : new ArrayList<>(jo.keySet())) {
                            if (!isGraphAdapterKey(envelopeKey)) {
                                continue;
                            }
                            JsonElement envelope = jo.get(envelopeKey);
                            if (envelope == null || !envelope.isJsonObject()) {
                                continue;
                            }
                            if (findPath(envelope, pathToFind, pathSegments, false, changedAnywhere)) {
                                anyEnvelopeChanged = true;
                                if (JsonElementUtil.isEmpty(envelope)) {
                                    jo.remove(envelopeKey);
                                }
                            }
                        }
                        return anyEnvelopeChanged;
                    }
                    List<String> tail = pathSegments.subList(1, pathSegments.size());
                    if (findPath(child, pathToFind, tail, false, changedAnywhere) && JsonElementUtil.isEmpty(child)) {
                        jo.remove(childKey);
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * Remove array elements matching the given rules. Each rule's path points at a field within
     * an array's elements; the innermost array on the path is filtered. Intermediate arrays are
     * traversed transparently (fan-out), so a path such as {@code entry.resource.meta.tag.system}
     * filters the {@code tag} array of every {@code entry}. Missing, empty or non-array paths are
     * a silent no-op; an element without the leaf field, or that is not a JSON object, is kept.
     */
    public static void removeMatchingElements(JsonElement root, List<ElementIgnoreRule> rules, IgnoredFieldsTracker tracker) {
        if (root == null || root.isJsonNull() || rules == null || rules.isEmpty()) {
            return;
        }
        for (ElementIgnoreRule rule : rules) {
            List<String> segments = asList(rule.getPath().split(PATH_SEPARATOR_PATTERN));
            if (segments.isEmpty()) {
                continue;
            }
            String leafField = segments.get(segments.size() - 1);
            List<String> prefix = segments.subList(0, segments.size() - 1);
            removeMatchingElements(root, prefix, leafField, rule, tracker, "");
        }
    }

    private static void removeMatchingElements(JsonElement element, List<String> prefix, String leafField,
                                               ElementIgnoreRule rule, IgnoredFieldsTracker tracker, String currentPath) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            if (prefix.isEmpty()) {
                filterArray(element.getAsJsonArray(), leafField, rule, tracker, currentPath);
            } else {
                for (JsonElement child : element.getAsJsonArray()) {
                    removeMatchingElements(child, prefix, leafField, rule, tracker, currentPath);
                }
            }
            return;
        }
        if (prefix.isEmpty() || !element.isJsonObject()) {
            return;
        }
        JsonObject jo = element.getAsJsonObject();
        String field = headOf(prefix);
        List<String> tail = prefix.subList(1, prefix.size());
        if (JsonElementUtil.WILDCARD.equals(field)) {
            // Every named child, so the rule reaches the array under each map value rather than
            // under one named key. Reached only from the prefix, so a * that ends the whole path is
            // the leaf field name instead and keeps its literal meaning.
            for (String key : new ArrayList<>(jo.keySet())) {
                JsonElement value = jo.get(key);
                if (value == null || value.isJsonNull()) {
                    continue;
                }
                boolean envelope = isGraphAdapterKey(key) && value.isJsonObject();
                String wildcardPath = currentPath.isEmpty() ? key : currentPath + "." + key;
                removeMatchingElements(value, envelope ? prefix : tail, leafField, rule, tracker,
                        envelope ? currentPath : wildcardPath);
            }
            return;
        }
        String childPath = currentPath.isEmpty() ? field : currentPath + "." + field;
        JsonElement child = getChild(jo, field);
        if (child != null) {
            removeMatchingElements(child, tail, leafField, rule, tracker, childPath);
        } else {
            // Descend through GraphAdapter envelope keys transparently.
            for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                if (isGraphAdapterKey(entry.getKey()) && entry.getValue().isJsonObject()) {
                    removeMatchingElements(entry.getValue(), prefix, leafField, rule, tracker, currentPath);
                }
            }
        }
    }

    private static void filterArray(JsonArray array, String leafField, ElementIgnoreRule rule,
                                    IgnoredFieldsTracker tracker, String arrayPath) {
        Iterator<JsonElement> iterator = array.iterator();
        int idx = 0;
        while (iterator.hasNext()) {
            JsonElement arrayElement = iterator.next();
            if (arrayElement.isJsonObject() && rule.matches(getChild(arrayElement.getAsJsonObject(), leafField))) {
                iterator.remove();
                if (tracker != null) {
                    tracker.recordIgnored(arrayPath + "[" + idx + "]", IgnoredFieldsTracker.Reason.IGNORE_ELEMENT_MATCH);
                }
            }
            idx++;
        }
    }

    /**
     * Applies the rest of the path under every named child of {@code jo}, for a
     * {@link JsonElementUtil#WILDCARD} segment. Reached only when something follows the wildcard: a
     * trailing one is handled by the leaf branch above, where it keeps its ordinary meaning of a key
     * literally named {@code *}.
     * <p>
     * A graph-adapter envelope key is descended through rather than consumed, so the wildcard applies
     * to the real fields underneath and never to the envelope itself.
     * <p>
     * Returns true only when a direct key of {@code jo} was removed, matching the contract the object
     * branch uses — the caller relies on it to cascade the removal of emptied parents.
     */
    private static boolean ignoreUnderEveryNamedChild(JsonObject jo, String pathToFind, List<String> pathSegments,
                                                      boolean[] changedAnywhere) {
        List<String> tail = pathSegments.subList(1, pathSegments.size());
        boolean removedOwnKey = false;
        // Keys are snapshotted: emptied children are removed from the map below.
        for (String key : new ArrayList<>(jo.keySet())) {
            JsonElement child = jo.get(key);
            // Skip nulls and scalars, as the array fan-out above does: a scalar child has no field
            // for the rest of the path to name, and descending into one reaches ignorePath, which
            // rejects a non-object. Without this a wildcard would be unusable at any position with
            // a scalar sibling -- which is the ordinary shape of a bean.
            if (child == null || child.isJsonNull() || child.isJsonPrimitive()) {
                continue;
            }
            boolean envelope = isGraphAdapterKey(key) && child.isJsonObject();
            boolean changed = findPath(child, pathToFind, envelope ? pathSegments : tail, false, changedAnywhere);
            if (changed && JsonElementUtil.isEmpty(child)) {
                jo.remove(key);
                removedOwnKey = true;
            }
        }
        return removedOwnKey;
    }

    /**
     * Returns the key under which {@code field} is present, tolerating whatever sentinel sequence the field
     * naming strategy put in front of it, or null if no form is present. The bare name wins when more than
     * one is present, which is the long-standing behaviour.
     */
    private static String childKeyOf(JsonObject jo, String field) {
        if (jo.has(field)) {
            return field;
        }
        if (jo.has(MARKER + field)) {
            return MARKER + field;
        }
        if (jo.has(MAP_MARKER + field)) {
            return MAP_MARKER + field;
        }
        // A field below which the naming strategy described further container levels carries one
        // sentinel per level, so the single-prefix probes above miss it. The probes stay in front of
        // this scan: they are what makes the bare name, and then MARKER, win a tie.
        for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
            if (field.equals(getOriginalFieldName(entry.getKey()))) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns the child of {@code jo} named {@code field}, tolerating whatever sentinel sequence the field
     * naming strategy put in front of it, or null if no form is present. Package-private so the JSON path
     * resolver can apply the same rule.
     */
    static JsonElement getChild(JsonObject jo, String field) {
        String key = childKeyOf(jo, field);
        return key == null ? null : jo.get(key);
    }

    public static void applySorting(JsonElement jsonElement, Map<String, List<SortField<String>>> pathsToSort, List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile) {
        applySorting(jsonElement, pathsToSort, fieldMatchersToSort, sortFile, null);
    }

    public static void applySorting(JsonElement jsonElement, Map<String, List<SortField<String>>> pathsToSort, List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile, SortedFieldsTracker tracker) {
        applySorting(jsonElement, pathsToSort, fieldMatchersToSort, sortFile, tracker, false);
    }

    /**
     * @param keepMapEntryOrder whether a complex-key map's {@code [key, value]} pair keeps its order
     *        instead of being sorted like a collection. Pass the caller's strict-file-matching setting:
     *        under strict matching the expected side is parsed and never sorted, so suppressing here
     *        cannot disagree with it. Callers that serialise both sides themselves, such as
     *        {@code sameBeanAs}, pass true unconditionally.
     */
    public static void applySorting(JsonElement jsonElement, Map<String, List<SortField<String>>> pathsToSort, List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile, SortedFieldsTracker tracker, boolean keepMapEntryOrder) {
        applySorting(jsonElement, pathsToSort, fieldMatchersToSort, sortFile, tracker, keepMapEntryOrder, "");
    }

    /**
     * @param rootChain the marker chain describing {@code jsonElement} itself, since a root map or
     *        collection has no field name to carry one and only the caller has the object to derive it
     *        from. Getting this wrong reorders a root map's keys with its values: this sort runs before
     *        {@code applyRootCollectionSorting}, so that method cannot make up for it.
     */
    public static void applySorting(JsonElement jsonElement, Map<String, List<SortField<String>>> pathsToSort, List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile, SortedFieldsTracker tracker, boolean keepMapEntryOrder, String rootChain) {
        if (jsonElement == null || jsonElement.isJsonNull()) return;
        Map<String, PathLevel> pathMap = pathsToSort.isEmpty() ? Collections.emptyMap() : getPathsMap(pathsToSort);
        applySortingInternal(jsonElement, pathMap, pathsToSort, fieldMatchersToSort, sortFile, tracker, "",
                false, rootChain, keepMapEntryOrder);
    }

    /**
     * @param isMapEntry whether {@code jsonElement} is itself a complex-key map's {@code [key, value]}
     *        pair, in which case its own element order must be preserved.
     * @param chain what is known about the levels here, meaning one thing per kind of node:
     *        <ul>
     *        <li>an array: the head describes this array -- a map level, so its elements are entries, or a
     *            collection level, so the tail describes its elements;</li>
     *        <li>a pair ({@code isMapEntry}): the chain describing its value half, the key half getting
     *            none;</li>
     *        <li>an object: the chain describing its members' values, which is non-empty only for a
     *            simple-keyed map's entry object, where the member name is a map key and carries no
     *            marker of its own.</li>
     *        </ul>
     *        Empty means nothing is known, which is what every level had before chains existed.
     */
    private static void applySortingInternal(JsonElement jsonElement, Map<String, PathLevel> pathMap,
            Map<String, List<SortField<String>>> pathsToSort,
            List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile,
            SortedFieldsTracker tracker, String currentPath,
            boolean isMapEntry, String chain, boolean keepMapEntryOrder) {
        // With suppression off both sides are sorted alike and a pair is an ordinary array, so a chain
        // would describe a level that is no longer treated as a level at all.
        String levels = keepMapEntryOrder ? chain : "";
        // From here `levels` is the chain; the parameter is not read again.
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                // Sort object keys inline (#5: replaces separate sortJsonFields pass)
                if (sortFile) {
                    List<FieldNamePair> toSort = getFiledNamePairs(jsonObject);
                    Collections.sort(toSort);
                    for (FieldNamePair pair : toSort) {
                        JsonElement element = jsonObject.remove(pair.originalKey);
                        jsonObject.add(pair.originalKey, element);
                    }
                }
                PathLevel wildcardLevel = pathMap.get(JsonElementUtil.WILDCARD);
                for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                    JsonElement actualValue = actual.getValue();
                    if (actualValue.isJsonNull() || actualValue.isJsonPrimitive()) {
                        continue;
                    }
                    FieldNamePair fieldNamePair = convertToKeyPair(actual.getKey());
                    if (isGraphAdapterKey(fieldNamePair.newKey)) {
                        // Transparent: descend with same pathMap, don't consume a level
                        // No chain: an envelope only ever wraps a bean, because the cyclic-reference
                        // detector excludes Maps and Iterables from the graph types, and the chain walk
                        // stops at a bean. So nothing can be pending here.
                        applySortingInternal(actualValue, pathMap, pathsToSort, fieldMatchersToSort, sortFile, tracker, currentPath,
                                false, "", keepMapEntryOrder);
                        continue;
                    }
                    PathLevel pathLevel = pathMap.getOrDefault(fieldNamePair.newKey, PathLevel.EMPTY);
                    Map<String, List<SortField<String>>> nextLevel =
                            mergeWildcardLevel(pathLevel.nextLevel, wildcardLevel);
                    Map<String, PathLevel> nextPathMap = nextLevel.isEmpty()
                            ? Collections.emptyMap() : getPathsMap(nextLevel);
                    String childPath = currentPath.isEmpty() ? fieldNamePair.newKey : currentPath + "." + fieldNamePair.newKey;
                    // A member of a simple-keyed map's entry object is named by the map key, which carries
                    // no marker, so the chain has to come from the level above -- and such an object has
                    // exactly one member. Everywhere else the field's own marker is the only source, and
                    // must not be overridden: a bean whose own fields are marked would lose them.
                    String childChain = !levels.isEmpty() && jsonObject.size() == 1
                            ? levels : fieldNamePair.markerChain();
                    applySortingInternal(actualValue, nextPathMap, nextLevel, fieldMatchersToSort, sortFile, tracker, childPath,
                            false, childChain, keepMapEntryOrder);
                    if (actualValue.isJsonArray()) {
                        List<SortField<String>> matchingPathMatchers = anyPathMatch(fieldNamePair.newKey, pathMap, sortFile);
                        List<SortField<Matcher<String>>> matchingFieldMatchers = anyFieldMatcherMatches(fieldNamePair.newKey, fieldMatchersToSort, sortFile);
                        if (fieldNamePair.shouldSortDueToType() || !matchingPathMatchers.isEmpty() || !matchingFieldMatchers.isEmpty()) {
                            sortJsonArray(actualValue.getAsJsonArray(), matchingPathMatchers, matchingFieldMatchers,
                                    childChain, keepMapEntryOrder);
                            if (tracker != null) {
                                recordSortMatches(tracker, childPath, matchingPathMatchers, matchingFieldMatchers);
                            }
                        }
                    }
                }
            } else if (jsonElement.isJsonArray()) {
                // Recurse into each element first so nested arrays are sorted before
                // computing the root sort key (bottom-up ordering).
                Map<String, List<SortField<String>>> innerPathsToSort;
                if (pathsToSort.containsKey("")) {
                    innerPathsToSort = new HashMap<>(pathsToSort);
                    innerPathsToSort.remove("");
                } else {
                    innerPathsToSort = pathsToSort;
                }
                // #3: Build the PathLevel map once for all elements in this array
                Map<String, PathLevel> innerPathMap;
                if (innerPathsToSort == pathsToSort) {
                    innerPathMap = pathMap; // "" was not in map; same pathsToSort, reuse existing map
                } else if (innerPathsToSort.isEmpty()) {
                    innerPathMap = Collections.emptyMap();
                } else {
                    innerPathMap = getPathsMap(innerPathsToSort);
                }
                // Indexed rather than iterated: a pair's value half is at index 1, and the skip below
                // means a loop counter is not the array index -- a String or null key is skipped, so the
                // value would otherwise be the first element seen.
                JsonArray array = jsonElement.getAsJsonArray();
                for (int index = 0; index < array.size(); index++) {
                    JsonElement current = array.get(index);
                    if (current.isJsonNull() || current.isJsonPrimitive()) {
                        continue;
                    }
                    boolean currentIsMapEntry = false;
                    String currentChain = "";
                    if (isMapEntry) {
                        // This node is a pair, so its elements are the key and the value halves. A map at
                        // the value half renders as its entry array, so anything else there contradicts what
                        // the type said -- a custom adapter can do that -- and the chain is dropped.
                        currentChain = isValueHalf(array, index) && current.isJsonArray() ? levels : "";
                    } else if (chainStartsAtMapLevel(levels)) {
                        currentIsMapEntry = isPair(current);
                        if (currentIsMapEntry || isSingleMemberObject(current)) {
                            currentChain = chainTail(levels);
                        }
                    } else if (chainStartsAtCollectionLevel(levels) && current.isJsonArray()) {
                        // Anything else at a collection level contradicts what the type said, so the tail
                        // would describe something that is not there.
                        currentChain = chainTail(levels);
                    }
                    applySortingInternal(current, innerPathMap, innerPathsToSort, fieldMatchersToSort, sortFile, tracker, currentPath,
                            currentIsMapEntry, currentChain, keepMapEntryOrder);
                }
                // Sort the array itself last (root), so the sort key reflects the
                // already-sorted state of nested elements.
                List<SortField<String>> rootSortFields = pathsToSort.getOrDefault("", emptyList());
                List<SortField<Matcher<String>>> rootFieldMatchers = anyFieldMatcherMatches("", fieldMatchersToSort, sortFile);
                if ((!rootSortFields.isEmpty() || !rootFieldMatchers.isEmpty())
                        && !(isMapEntry && keepMapEntryOrder)) {
                    sortJsonArray(jsonElement.getAsJsonArray(), rootSortFields, rootFieldMatchers,
                            isMapEntry ? "" : levels, keepMapEntryOrder);
                    if (tracker != null) {
                        recordSortMatches(tracker, currentPath, rootSortFields, rootFieldMatchers);
                    }
                }
            }
        }
    }

    private static void recordSortMatches(SortedFieldsTracker tracker, String path,
            List<SortField<String>> matchingPathMatchers,
            List<SortField<Matcher<String>>> matchingFieldMatchers) {
        if (!matchingPathMatchers.isEmpty()) {
            tracker.recordSortedByPath(path);
        }
        for (SortField<Matcher<String>> fm : matchingFieldMatchers) {
            tracker.recordSortedByPattern(path, fm.getSortFieldSelector().toString());
        }
    }

    private static List<SortField<String>> anyPathMatch(String fieldName, Map<String, PathLevel> pathMap, boolean sortFile) {
        if (sortFile) {
            PathLevel nextLevelSet = pathMap.get(fieldName);
            if(nextLevelSet != null){
                return nextLevelSet.currentLevel;
            }
        }
        return emptyList();
    }

    private static Map<String, PathLevel> getPathsMap(Map<String, List<SortField<String>>> pathsToSort) {
        if (pathsToSort.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, PathLevel> result = new HashMap<>();
        for (Map.Entry<String,List<SortField<String>>> pathEntry : pathsToSort.entrySet()) {
            String path = pathEntry.getKey();
            int indexOfNextLevel = path.indexOf(".");
            if (indexOfNextLevel < 0) {
                result.computeIfAbsent(path, k -> new PathLevel()).addCurrentLevel(pathEntry.getValue());
            } else {
                result.computeIfAbsent(path.substring(0, indexOfNextLevel), k -> new PathLevel()).addNextLevel(path.substring(indexOfNextLevel + 1), pathEntry.getValue());
            }
        }
        return result;
    }

    /**
     * Folds a {@link JsonElementUtil#WILDCARD} level into the level reached by a named key, so a
     * sort path written with {@code *} applies to every named child at that position — the same
     * meaning the wildcard has when ignoring or custom matching.
     * <p>
     * The two are merged per key rather than one replacing the other, so a wildcard and a literal
     * key naming the same child both take effect: {@code sortField("map.*.tags", "map.k1.other")}
     * sorts {@code tags} under every entry and {@code other} under {@code k1}.
     */
    private static Map<String, List<SortField<String>>> mergeWildcardLevel(
            Map<String, List<SortField<String>>> namedLevel, PathLevel wildcard) {
        if (wildcard == null || wildcard.nextLevel.isEmpty()) {
            return namedLevel;
        }
        if (namedLevel.isEmpty()) {
            return wildcard.nextLevel;
        }
        Map<String, List<SortField<String>>> merged = new HashMap<>(namedLevel);
        for (Map.Entry<String, List<SortField<String>>> entry : wildcard.nextLevel.entrySet()) {
            List<SortField<String>> existing = merged.get(entry.getKey());
            if (existing == null) {
                merged.put(entry.getKey(), entry.getValue());
            } else {
                List<SortField<String>> combined = new ArrayList<>(existing);
                combined.addAll(entry.getValue());
                merged.put(entry.getKey(), combined);
            }
        }
        return merged;
    }

    private static Map<String, PathLevel> getPathsMap(List<SortField<String>> pathsToSort) {
        if (pathsToSort.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, PathLevel> result = new HashMap<>();
        for (SortField<String> pathEntry : pathsToSort) {
            for(String path : pathEntry.getIgnoredPathsForSorting()){
                int indexOfNextLevel = path.indexOf(".");
                if (indexOfNextLevel < 0) {
                    result.computeIfAbsent(path, k -> new PathLevel()).addCurrentLevel(SortField.of(path,emptyList(),pathEntry.getIgnoredFieldMatchersForSorting()));
                } else {
                    String currentLevelKey = path.substring(0, indexOfNextLevel);
                    String nextLevel = path.substring(indexOfNextLevel + 1);
                    result.computeIfAbsent(currentLevelKey, k -> new PathLevel()).addNextLevel(nextLevel, SortField.of(nextLevel,Collections.singletonList(nextLevel),pathEntry.getIgnoredFieldMatchersForSorting()));
                }
            }
        }
        return result;
    }

    private static class PathLevel {
        public static final PathLevel EMPTY = new PathLevel(emptyList(), Collections.emptyMap());

        private final List<SortField<String>> currentLevel;
        private final Map<String, List<SortField<String>>> nextLevel;


        public PathLevel() {
            this(new ArrayList<>(), new HashMap<>());
        }

        public PathLevel(List<SortField<String>> currentLevel, Map<String, List<SortField<String>>> nextLevel) {
            this.currentLevel = currentLevel;
            this.nextLevel = nextLevel;
        }

        public Map<String, List<SortField<String>>> getNextLevel() {
            return nextLevel;
        }

        public void addNextLevel(String input, List<SortField<String>> sortFields) {
            nextLevel.computeIfAbsent(input, k -> new ArrayList<>()).addAll(sortFields);
        }

        public void addNextLevel(String input, SortField<String> sortField) {
            nextLevel.computeIfAbsent(input, k -> new ArrayList<>()).add(sortField);
        }

        public void addCurrentLevel(List<SortField<String>> sortFields) {
            currentLevel.addAll(sortFields);
        }

        public void addCurrentLevel(SortField<String> sortField) {
            currentLevel.add(sortField);
        }

        public boolean isLastPath(){
            return nextLevel.isEmpty();
        }
    }

    /**
     * Whether {@code element} has the shape of a complex-key map's entry: a two-position {@code [key, value]}
     * array. Ignoring can take a half away, and the type says nothing about what a custom adapter emitted,
     * so a map level is confirmed against the element before its tail is applied. Failing the check falls
     * back to knowing nothing, which is what every level did before chains existed.
     */
    private static boolean isPair(JsonElement element) {
        return element.isJsonArray() && element.getAsJsonArray().size() == 2;
    }

    /**
     * Whether {@code index} is the value half of {@code pair}. Index 1 by construction, but only where the
     * pair still has both halves: ignoring can empty one and take it away, and a chain must not then be
     * handed to whatever is left at that position.
     */
    private static boolean isValueHalf(JsonArray pair, int index) {
        return index == 1 && pair.size() == 2;
    }

    /** Whether {@code element} is a simple-keyed map's entry: one member, named by the key. */
    private static boolean isSingleMemberObject(JsonElement element) {
        return element.isJsonObject() && element.getAsJsonObject().size() == 1;
    }

    private static List<SortField<Matcher<String>>> anyFieldMatcherMatches(String fieldName, List<SortField<Matcher<String>>> fieldMatchersToSort, boolean sortFile) {
        if (sortFile) {
            List<SortField<Matcher<String>>> result = new ArrayList<>();
            for (SortField<Matcher<String>> matcher : fieldMatchersToSort) {
                if (matcher.getSortFieldSelector().matches(fieldName)) {
                    result.add(matcher);
                }
            }
            return result;
        }
        return emptyList();
    }

    /**
     * @param chain what is known about this array's levels: a map level means its direct elements are a
     *        complex-key map's {@code [key, value]} pairs, and the tail then describes each entry's value
     *        half; a collection level means the tail describes its elements. A pair is two positions, not
     *        a collection: reordering it swaps the key with the value, losing which half was which. Its
     *        halves are still descended into, so a list-valued half is sorted as before.
     * @param keepMapEntryOrder whether that suppression is active. The caller is responsible for passing
     *        it only when both sides of its comparison are treated alike: {@code sameBeanAs} serialises
     *        both itself and passes true unconditionally, while a file matcher passes its strict-matching
     *        setting, since only under strict matching is the approved content left unsorted. Suppressing
     *        on one side while the other still reorders would fail with no way to regenerate out of it.
     */
    private static void sortJsonArray(JsonArray input, List<SortField<String>> matchingPathMatchers,
                                      List<SortField<Matcher<String>>> matchingFieldMatchers,
                                      String chain, boolean keepMapEntryOrder) {
        // With suppression off a pair is an ordinary array, so what the chain describes no longer holds.
        String levels = keepMapEntryOrder ? chain : "";
        boolean elementsAreMapEntries = chainStartsAtMapLevel(levels);
        String belowThisLevel = chainTail(levels);
        List<SortElement> toSort = new ArrayList<>(input.size());
        Iterator<JsonElement> iter = input.iterator();
        while (iter.hasNext()) {
            JsonElement actual = iter.next();
            // When a direct element is itself an array (e.g. List<List<Bean>>), apply
            // the configured sort to it before computing its sort key. The sort was
            // configured for this collection; direct array elements are part of that
            // same collection and fan-out applies.
            // Elements that are objects (beans) are NOT sorted here; only their fields
            // that were explicitly configured via sortField will be sorted via applySorting.
            if (actual.isJsonArray()) {
                JsonArray element = actual.getAsJsonArray();
                // isPair as well as the chain: the type saying "map" is not enough on its own, and an
                // element that is not two positions cannot be an entry whatever the type said. Getting this
                // wrong leaves an ordinary collection unsorted, which is the one outcome that is not allowed.
                if (elementsAreMapEntries && isPair(actual)) {
                    // Descend into the key and the value, but leave the pair's own order alone. Only the
                    // value half continues the chain, and only where ignoring has not taken a half away.
                    for (int half = 0; half < element.size(); half++) {
                        JsonElement halfElement = element.get(half);
                        if (halfElement.isJsonArray()) {
                            sortJsonArray(halfElement.getAsJsonArray(), matchingPathMatchers, matchingFieldMatchers,
                                    isValueHalf(element, half) ? belowThisLevel : "", keepMapEntryOrder);
                        }
                    }
                } else {
                    sortJsonArray(element, matchingPathMatchers, matchingFieldMatchers,
                            chainStartsAtCollectionLevel(levels) ? belowThisLevel : "", keepMapEntryOrder);
                }
            }
            toSort.add(new SortElement(getFilteredStringForSorting(actual, matchingPathMatchers, matchingFieldMatchers).toString(), actual));
            iter.remove();
        }
        Collections.sort(toSort);
        for (SortElement actual : toSort) {
            input.add(actual.original);
        }
    }

    private static JsonElement getFilteredStringForSorting(JsonElement jsonElement, List<SortField<String>> pathMatchers, List<SortField<Matcher<String>>> fieldMatchers) {
        if (areAllMatchersEmpty(pathMatchers, fieldMatchers)) {
            return jsonElement;
        }

        if (jsonElement.isJsonObject()) {
            JsonObject jsonForSort = new JsonObject();

            // Merge ignored paths/matchers from fieldMatchers into the path map so that
            // SortField<Matcher<String>>.ignoring(String) and .ignoring(Matcher) work for
            // fields inside elements, not just path-based SortField<String>.
            List<SortField<String>> combinedPaths = new ArrayList<>(pathMatchers);
            List<Matcher<String>> innerIgnoredFieldMatchers = new ArrayList<>();
            for (SortField<Matcher<String>> fm : fieldMatchers) {
                for (String ignoredPath : fm.getIgnoredPathsForSorting()) {
                    combinedPaths.add(SortField.of(ignoredPath, Collections.singletonList(ignoredPath), Collections.emptyList()));
                }
                innerIgnoredFieldMatchers.addAll(fm.getIgnoredFieldMatchersForSorting());
            }
            Map<String, PathLevel> pathMap = getPathsMap(combinedPaths);
            PathLevel wildcardLevel = pathMap.get(JsonElementUtil.WILDCARD);

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                JsonElement actualValue = actual.getValue();
                String actualKey = actual.getKey();
                FieldNamePair fieldNamePair = convertToKeyPair(actualKey);

                if (isGraphAdapterKey(fieldNamePair.newKey)) {
                    // Transparent: recurse with same pathMatchers/fieldMatchers
                    jsonForSort.add(actualKey, getFilteredStringForSorting(actualValue, pathMatchers, fieldMatchers));
                    continue;
                }

                PathLevel matchingPath = pathMap.getOrDefault(fieldNamePair.newKey, PathLevel.EMPTY);

                List<SortField<Matcher<String>>> matchingFieldMatchers = anyFieldMatcherMatches(fieldNamePair.newKey, fieldMatchers, true);
                List<SortField<String>> matchingPathMatchers = anyPathMatch(fieldNamePair.newKey, pathMap, true);
                boolean matchedByInnerFieldMatcher = anyInnerMatcherMatches(fieldNamePair.newKey, innerIgnoredFieldMatchers);

                if (!matchingPathMatchers.isEmpty() || !matchingFieldMatchers.isEmpty() || matchedByInnerFieldMatcher) {
                    // leaf match — strip this field entirely from the sort key (primitive or complex)
                } else if (actualValue.isJsonNull() || actualValue.isJsonPrimitive()) {
                    jsonForSort.add(actualKey, actualValue);
                } else {
                    List<SortField<String>> nextLevelSortFields = new ArrayList<>();
                    for (List<SortField<String>> sortFields
                            : mergeWildcardLevel(matchingPath.nextLevel, wildcardLevel).values()) {
                        nextLevelSortFields.addAll(sortFields);
                    }
                    jsonForSort.add(actualKey, getFilteredStringForSorting(actualValue, nextLevelSortFields, fieldMatchers));
                }
            }

            return jsonForSort;
        } else if (jsonElement.isJsonArray()) {
            JsonArray actualArray = jsonElement.getAsJsonArray();
            JsonArray jsonForSort = new JsonArray(actualArray.size());
            for (JsonElement current : actualArray) {
                if (current.isJsonNull() || current.isJsonPrimitive()) {
                    jsonForSort.add(current);
                } else {
                    jsonForSort.add(getFilteredStringForSorting(current, pathMatchers, fieldMatchers));
                }
            }
            return jsonForSort;
        }

        return jsonElement;
    }

    private static boolean anyInnerMatcherMatches(String fieldName, List<Matcher<String>> matchers) {
        for (Matcher<String> matcher : matchers) {
            if (matcher.matches(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given string is an object-graph reference id as produced by
     * {@code GraphAdapterBuilder}, i.e. {@code "0x"} followed by one or more lowercase hex digits.
     *
     * <p>This is the single definition of the reference format; any code that needs to recognise
     * these ids must use this method rather than its own pattern, so that ids from the tenth
     * object onwards (which contain the hex letters a-f) are not missed.
     */
    public static boolean isGraphAdapterKey(String key) {
        if (key == null || !key.startsWith("0x") || key.length() <= 2) return false;
        for (int i = 2; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) return false;
        }
        return true;
    }



    private static boolean areAllMatchersEmpty(List<SortField<String>> matchingPathMatchers, List<SortField<Matcher<String>>> matchingFieldMatchers){
        if(matchingPathMatchers.isEmpty() && matchingFieldMatchers.isEmpty()){
            return true;
        }
        for(SortField<String> matchingPathMatcher : matchingPathMatchers) {
            if(!matchingPathMatcher.isEmpty()){
                return false;
            }
        }
        for(SortField<Matcher<String>> matchingFieldMatcher : matchingFieldMatchers) {
            if(!matchingFieldMatcher.isEmpty()){
                return false;
            }
        }
        return true;
    }

    private static class SortElement implements Comparable<SortElement> {
        private final String value;
        private final JsonElement original;

        public SortElement(String value, JsonElement original) {
            this.value = value;
            this.original = original;
        }

        public JsonElement getOriginal() {
            return original;
        }

        @Override
        public int compareTo(SortElement o) {
            return value.compareTo(o.value);
        }
    }

    private static class FieldNamePair implements Comparable<FieldNamePair> {
        private final String originalKey;
        private final String newKey;

        public FieldNamePair(String originalKey, String newKey) {
            this.originalKey = originalKey;
            this.newKey = newKey;
        }

        public String getOriginalKey() {
            return originalKey;
        }

        public String getNewKey() {
            return newKey;
        }

        /**
         * The sentinel sequence the field naming strategy put in front of the name, describing this field's
         * own level and every container level below it that the declared type could describe. Empty for an
         * unmarked field.
         */
        public String markerChain() {
            return originalKey.substring(0, originalKey.length() - newKey.length());
        }

        public boolean shouldSortDueToType() {
            return !originalKey.equals(newKey);
        }

        @Override
        public int compareTo(FieldNamePair keyPair) {
            return newKey.compareTo(keyPair.newKey);
        }
    }

    public static void sortJsonFields(JsonElement jsonElement, boolean sortFile) {
        if (sortFile) {
            if (jsonElement != null && !jsonElement.isJsonNull()) {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    List<FieldNamePair> toSort = getFiledNamePairs(jsonObject);
                    Collections.sort(toSort);
                    for (FieldNamePair actual : toSort) {
                        JsonElement element = jsonObject.remove(actual.originalKey);
                        jsonObject.add(actual.originalKey, element);
                    }
                    for (Map.Entry<String, JsonElement> actual : jsonObject.entrySet()) {
                        sortJsonFields(actual.getValue(), sortFile);
                    }
                } else if (jsonElement.isJsonArray()) {
                    Iterator<JsonElement> iter = jsonElement.getAsJsonArray().iterator();
                    while (iter.hasNext()) {
                        JsonElement current = iter.next();
                        if (current.isJsonNull()) {
                            continue;
                        }
                        sortJsonFields(current, sortFile);
                    }
                }
            }
        }
    }

    private static List<FieldNamePair> getFiledNamePairs(JsonObject input) {
        List<FieldNamePair> result = new ArrayList<>(input.size());
        for (String actual : input.keySet()) {
            result.add(convertToKeyPair(actual));
        }
        return result;
    }

    private static FieldNamePair convertToKeyPair(String input) {
        return new FieldNamePair(input, getOriginalFieldName(input));
    }

    /**
     * The field's own name, with every sentinel the naming strategy wrote in front of it stripped. Package
     * visible because the pattern-matching side of {@link JsonElementUtil} has to test a user's
     * {@code Matcher<String>} against the name the user knows, not the marked key.
     */
    static String getOriginalFieldName(String input) {
        String result = input;
        // Every prefix, not only the first: one sentinel per container level means a name can carry several.
        // MAP_MARKER first: it is not a prefix of MARKER, but checking the longer sentinel first keeps this
        // correct if either string ever changes. Stripping matters beyond cosmetics -- the result feeds path
        // matching and every tracker string, neither of which removeSetMarker ever sees.
        while (true) {
            if (result.startsWith(MAP_MARKER)) {
                result = result.substring(MAP_MARKER.length());
            } else if (result.startsWith(MARKER)) {
                result = result.substring(MARKER.length());
            } else {
                return result;
            }
        }
    }

    /**
     * Removes {@code field} from {@code jo} whatever sentinel sequence the field naming strategy gave it,
     * and reports whether anything went. Every form is removed, not the first found.
     */
    private static boolean removeChildInAnyMarkedForm(JsonObject jo, String field) {
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
            if (field.equals(getOriginalFieldName(entry.getKey()))) {
                keysToRemove.add(entry.getKey());
            }
        }
        boolean removed = false;
        for (String key : keysToRemove) {
            removed |= jo.remove(key) != null;
        }
        return removed;
    }

    private static boolean ignorePath(JsonElement jsonElement, String pathToIgnore) {
        if (!jsonElement.isJsonNull()) {
            if (!jsonElement.isJsonObject()) {
                throw new IllegalArgumentException();
            }
            JsonObject jo = jsonElement.getAsJsonObject();
            String lastSegment = getLastSegmentOf(pathToIgnore);
            boolean removedElement = removeChildInAnyMarkedForm(jo, lastSegment);
            if (!removedElement) {
                // Try descending through GraphAdapter envelope keys
                for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                    if (isGraphAdapterKey(entry.getKey()) && entry.getValue().isJsonObject()) {
                        removedElement |= removeChildInAnyMarkedForm(entry.getValue().getAsJsonObject(), lastSegment);
                    }
                }
            }
            return removedElement;
        }
        return false;
    }

    private static String getLastSegmentOf(String fieldPath) {
        String[] paths = fieldPath.split(PATH_SEPARATOR_PATTERN);
        if (paths.length == 0) {
            return fieldPath;
        }

        return paths[max(0, paths.length - 1)];
    }

    private static String headOf(Collection<String> paths) {
        return paths.iterator().next();
    }

    private static boolean collectionElementMatchesTypesToSort(Collection<?> collection, Collection<Class<?>> typesToSort) {
        for (Object element : collection) {
            if (element != null) {
                for (Class<?> type : typesToSort) {
                    if (type.isInstance(element)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
