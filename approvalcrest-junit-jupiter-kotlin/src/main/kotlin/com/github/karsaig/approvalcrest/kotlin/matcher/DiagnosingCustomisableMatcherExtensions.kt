package com.github.karsaig.approvalcrest.kotlin.matcher

import com.github.karsaig.approvalcrest.matcher.CustomisableMatcher
import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher
import com.github.karsaig.approvalcrest.matcher.GsonConfiguration
import com.github.karsaig.approvalcrest.matcher.alias.AliasMap
import com.github.karsaig.approvalcrest.matcher.sorting.SortField
import org.hamcrest.Matcher
import java.util.function.Function

/**
 * Kotlin extension functions for [DiagnosingCustomisableMatcher] that work around Kotlin's inability
 * to infer F-bounded (recursive) generic return types (KT-5464).
 *
 * Without these, chained calls such as:
 *   `sameBeanAs(expected).with("field", myMatcher)`
 * fail to compile because Kotlin infers the return type as `CustomisableMatcher<*,*>`.
 *
 * Each extension casts `this` to the interface with explicit type parameters so that Kotlin
 * resolves the return type to `DiagnosingCustomisableMatcher<T>` rather than a star-projection.
 */

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.ignoring(fieldPath: String): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).ignoring(fieldPath)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.ignoring(vararg fieldPaths: String): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).ignoring(*fieldPaths)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.ignoring(clazz: Class<*>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).ignoring(clazz)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.ignoring(vararg clazz: Class<*>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).ignoring(*clazz)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.ignoring(fieldNamePattern: Matcher<String>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).ignoring(fieldNamePattern)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> DiagnosingCustomisableMatcher<T>.ignoring(vararg fieldNamePatterns: Matcher<String>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).ignoring(*fieldNamePatterns)

@Suppress("UNCHECKED_CAST")
fun <T, V> DiagnosingCustomisableMatcher<T>.with(fieldPath: String, matcher: Matcher<V>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).with(fieldPath, matcher)

@Suppress("UNCHECKED_CAST")
fun <T, V> DiagnosingCustomisableMatcher<T>.withMatcher(fieldNamePattern: Matcher<String>, matcher: Matcher<V>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).withMatcher(fieldNamePattern, matcher)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.withGsonConfiguration(configuration: GsonConfiguration): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).withGsonConfiguration(configuration)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.skipCircularReferenceCheck(matcher: Function<Any, Boolean>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).skipCircularReferenceCheck(matcher)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> DiagnosingCustomisableMatcher<T>.skipCircularReferenceCheck(
    matcher: Function<Any, Boolean>,
    vararg matchers: Function<Any, Boolean>
): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).skipCircularReferenceCheck(matcher, *matchers)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.sortField(fieldNamePattern: Matcher<String>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortField(fieldNamePattern)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> DiagnosingCustomisableMatcher<T>.sortField(vararg fieldNamePatterns: Matcher<String>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortField(*fieldNamePatterns)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.sortField(fieldPath: String): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortField(fieldPath)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.sortField(vararg fieldPaths: String): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortField(*fieldPaths)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.sortFieldMatcher(fieldNamePattern: SortField<Matcher<String>>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortFieldMatcher(fieldNamePattern)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> DiagnosingCustomisableMatcher<T>.sortFieldMatcher(vararg fieldNamePatterns: SortField<Matcher<String>>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortFieldMatcher(*fieldNamePatterns)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.sortFieldPath(fieldPath: SortField<String>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortFieldPath(fieldPath)

@Suppress("UNCHECKED_CAST", "SpreadOperator")
fun <T> DiagnosingCustomisableMatcher<T>.sortFieldPath(vararg fieldPaths: SortField<String>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortFieldPath(*fieldPaths)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.withAliasMap(aliasMap: AliasMap): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).withAliasMap(aliasMap)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.withAlias(value: String, alias: String): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).withAlias(value, alias)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.withAlias(fieldName: String, value: String, alias: String): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).withAlias(fieldName, value, alias)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.sortType(vararg types: Class<*>): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).sortType(*types)

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.withMachineReadableOutput(): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).withMachineReadableOutput()

@Suppress("UNCHECKED_CAST")
fun <T> DiagnosingCustomisableMatcher<T>.withoutSerializingNulls(): DiagnosingCustomisableMatcher<T> =
    (this as CustomisableMatcher<T, DiagnosingCustomisableMatcher<T>>).withoutSerializingNulls()
