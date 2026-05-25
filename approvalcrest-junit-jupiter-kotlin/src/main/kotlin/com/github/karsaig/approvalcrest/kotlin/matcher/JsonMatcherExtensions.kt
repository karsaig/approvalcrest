package com.github.karsaig.approvalcrest.kotlin.matcher

import com.github.karsaig.approvalcrest.matcher.CustomisableMatcher
import com.github.karsaig.approvalcrest.matcher.GsonConfiguration
import com.github.karsaig.approvalcrest.matcher.JsonMatcher
import com.github.karsaig.approvalcrest.matcher.alias.AliasMap
import com.github.karsaig.approvalcrest.matcher.file.ApprovedFileMatcher
import com.github.karsaig.approvalcrest.matcher.sorting.SortField
import org.hamcrest.Matcher
import java.nio.file.Path
import java.util.function.Function

/**
 * Kotlin extension functions for [JsonMatcher] that work around Kotlin's inability
 * to infer F-bounded (recursive) generic return types (KT-5464).
 *
 * Without these, chained calls such as:
 *   `sameJsonAsApproved<MyBean>().ignoring("field").withUniqueId("id")`
 * fail to compile because Kotlin infers the return type as a star-projection.
 */

// --- CustomisableMatcher<T, JsonMatcher<T>> extensions ---

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.ignoring(fieldPath: String): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).ignoring(fieldPath)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.ignoring(vararg fieldPaths: String): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).ignoring(*fieldPaths)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.ignoring(clazz: Class<*>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).ignoring(clazz)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.ignoring(vararg clazz: Class<*>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).ignoring(*clazz)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.ignoring(fieldNamePattern: Matcher<String>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).ignoring(fieldNamePattern)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> JsonMatcher<T>.ignoring(vararg fieldNamePatterns: Matcher<String>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).ignoring(*fieldNamePatterns)

@Suppress("UNCHECKED_CAST")
fun <T, V> JsonMatcher<T>.with(fieldPath: String, matcher: Matcher<V>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).with(fieldPath, matcher)

@Suppress("UNCHECKED_CAST")
fun <T, V> JsonMatcher<T>.withMatcher(fieldNamePattern: Matcher<String>, matcher: Matcher<V>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).withMatcher(fieldNamePattern, matcher)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withGsonConfiguration(configuration: GsonConfiguration): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).withGsonConfiguration(configuration)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.skipCircularReferenceCheck(matcher: Function<Any, Boolean>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).skipCircularReferenceCheck(matcher)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> JsonMatcher<T>.skipCircularReferenceCheck(
    matcher: Function<Any, Boolean>,
    vararg matchers: Function<Any, Boolean>
): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).skipCircularReferenceCheck(matcher, *matchers)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.sortField(fieldNamePattern: Matcher<String>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).sortField(fieldNamePattern)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> JsonMatcher<T>.sortField(vararg fieldNamePatterns: Matcher<String>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).sortField(*fieldNamePatterns)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.sortField(fieldPath: String): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).sortField(fieldPath)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.sortField(vararg fieldPaths: String): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).sortField(*fieldPaths)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.sortFieldMatcher(fieldNamePattern: SortField<Matcher<String>>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).sortFieldMatcher(fieldNamePattern)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> JsonMatcher<T>.sortFieldMatcher(vararg fieldNamePatterns: SortField<Matcher<String>>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).sortFieldMatcher(*fieldNamePatterns)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.sortFieldPath(fieldPath: SortField<String>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).sortFieldPath(fieldPath)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> JsonMatcher<T>.sortFieldPath(vararg fieldPaths: SortField<String>): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).sortFieldPath(*fieldPaths)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withAliasMap(aliasMap: AliasMap): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).withAliasMap(aliasMap)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withAlias(value: String, alias: String): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).withAlias(value, alias)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withAlias(fieldName: String, value: String, alias: String): JsonMatcher<T> =
    (this as CustomisableMatcher<T, JsonMatcher<T>>).withAlias(fieldName, value, alias)

// --- ApprovedFileMatcher<JsonMatcher<T>> extensions ---

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withUniqueId(uniqueId: String): JsonMatcher<T> =
    (this as ApprovedFileMatcher<JsonMatcher<T>>).withUniqueId(uniqueId)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withFileName(customFileName: String): JsonMatcher<T> =
    (this as ApprovedFileMatcher<JsonMatcher<T>>).withFileName(customFileName)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withPathName(pathName: String): JsonMatcher<T> =
    (this as ApprovedFileMatcher<JsonMatcher<T>>).withPathName(pathName)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withRelativePathName(pathName: String): JsonMatcher<T> =
    (this as ApprovedFileMatcher<JsonMatcher<T>>).withRelativePathName(pathName)

@Suppress("UNCHECKED_CAST")
fun <T> JsonMatcher<T>.withPath(path: Path): JsonMatcher<T> =
    (this as ApprovedFileMatcher<JsonMatcher<T>>).withPath(path)
