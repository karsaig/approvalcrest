package com.github.karsaig.approvalcrest.matcher.alias;

import java.util.List;
import java.util.Optional;

/**
 * Internal strategy for resolving alias entries. Two implementations exist:
 * {@link ExactAliasMapStrategy} (O(1) for all-static maps) and
 * {@link IndexedAliasMapStrategy} (general case).
 */
interface ResolveStrategy {

    Optional<String> resolve(String path, String fieldName, String coercedValue);

    boolean isEmpty();

    List<AliasEntry> getEntries();
}
