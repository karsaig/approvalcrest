/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest.matcher;

import com.github.karsaig.approvalcrest.matcher.alias.AliasMap;
import com.github.karsaig.approvalcrest.matcher.sorting.SortField;
import org.hamcrest.Matcher;

import java.util.function.Function;


/**
 * {@link Matcher} implementation where fields and object types can be skipped from the comparison, or matched with
 * custom matchers.
 *
 * @param <T> type of the object being asserted
 * @param <U> the concrete matcher type, so the fluent methods return the right type when chained
 */
public interface CustomisableMatcher<T, U extends CustomisableMatcher<T, U>> extends Matcher<T> {

    /**
     * Specify the path of the field to be skipped from the matcher comparison.
     * Example:
     * <pre>sameBeanAs(expected).ignoring("beanField.subBeanField")</pre>
     *
     * @param fieldPath the path of the field to be skipped from the comparison.
     * @return the instance of the matcher
     */
    U ignoring(String fieldPath);

    /**
     * Specify the path of the field to be skipped from the matcher comparison.
     * Example:
     * <pre>sameBeanAs(expected).ignoring("beanField.subBeanField","beanField2.subBeanField3")</pre>
     *
     * @param fieldPaths the paths of fields to be skipped from the comparison.
     * @return the instance of the matcher
     */
    U ignoring(String... fieldPaths);

    /**
     * Specify the object type of the fields to be skipped from the matcher comparison.
     * Example:
     * <pre>sameBeanAs(expected).ignoring(Bean.class)</pre>
     *
     * @param clazz the object type to be skipped from the comparison.
     * @return the instance of the matcher
     */
    U ignoring(Class<?> clazz);

    /**
     * Specify the object types of the fields to be skipped from the matcher comparison.
     * Example:
     * <pre>sameBeanAs(expected).ignoring(Bean.class,Bean2.class)</pre>
     *
     * @param clazz the object types to be skipped from the comparison.
     * @return the instance of the matcher
     */
    U ignoring(Class<?>... clazz);

    /**
     * Specify the path of a field to be matched with a specific matcher <em>instead of</em> being compared.
     * The field is removed from the comparison, so it never reaches the approved file and only the matcher
     * speaks for it — which is what makes this the right choice for a value you cannot pin down, such as a
     * generated id or a timestamp.
     * Example:
     * <pre>sameBeanAs(expected).with("beanField.subBeanField", contains("element"))</pre>
     *
     * <p>To assert a field <em>as well as</em> comparing it, use {@link #alsoCheck(String, Matcher)}.
     *
     * @param fieldPath the path of the field to be matched with the provided matcher.
     * @param matcher   the Hamcrest matcher used to match the specified field.
     * @param <V>       type of actual object to match
     * @return the instance of the matcher
     * @see #alsoCheck(String, Matcher)
     *
     * <p><strong>An ordering matcher must be written in the field's own number type.</strong> A whole number
     * reaches a matcher as a {@code Long} whenever it comes from the serialised output, and Hamcrest compares
     * only within one boxing, so {@code greaterThan(0)} against a {@code long} field matches nothing. Negated,
     * it is worse: {@code not(greaterThan(0))} passes whatever the field holds, because Hamcrest answers false
     * for the pairing and the negation turns that into a pass. Nothing here can tell that apart from a genuine
     * false — write {@code greaterThan(0L)}. See {@code docs/custom-matching.md}.
     */
    <V> U with(String fieldPath, Matcher<V> matcher);

    /**
     * Specify the path of a field to be matched with a specific matcher <em>in addition to</em> the normal
     * comparison, rather than instead of it. Where {@link #with(String, Matcher)} takes the field out of the
     * comparison -- so it never reaches the approved file -- this leaves it in and applies the matcher as well.
     * Example:
     * <pre>sameJsonAsApproved().alsoCheck("score", greaterThan(0L))</pre>
     * asserts that {@code score} still equals the value recorded in the approved file <em>and</em> is positive.
     *
     * <p>Use {@link #with(String, Matcher)} for a value you cannot pin down, such as a generated id or a
     * timestamp; use this for a value you can pin down but want a stronger guarantee about.
     *
     * <p>Registering the same path both ways is allowed and the last call wins. An explicit
     * {@code ignoring(fieldPath)} still removes the field, whichever order the two are written in.
     *
     * @param fieldPath the path of the field to be matched with the provided matcher.
     * @param matcher   the Hamcrest matcher used to match the specified field.
     * @param <V>       type of actual object to match
     * @return the instance of the matcher
     * @see #with(String, Matcher)
     * @see #alsoCheckMatching(Matcher, Matcher)
     *
     * <p><strong>An ordering matcher must be written in the field's own number type.</strong> A whole number
     * reaches a matcher as a {@code Long} whenever it comes from the serialised output, and Hamcrest compares
     * only within one boxing, so {@code greaterThan(0)} against a {@code long} field matches nothing. Negated,
     * it is worse: {@code not(greaterThan(0))} passes whatever the field holds, because Hamcrest answers false
     * for the pairing and the negation turns that into a pass. Nothing here can tell that apart from a genuine
     * false — write {@code greaterThan(0L)}. See {@code docs/custom-matching.md}.
     */
    <V> U alsoCheck(String fieldPath, Matcher<V> matcher);

    /**
     * Specify a field name pattern and a custom matcher. All fields at any depth whose name
     * matches the pattern will be matched with the provided matcher <em>instead of</em> being compared: every
     * matched field is removed from the comparison, as {@link #with(String, Matcher)} does for one path. If no
     * fields match the pattern, the matcher passes vacuously.
     * Example:
     * <pre>sameJsonAsApproved().withMatcher(containsString("Date"), notNullValue())</pre>
     *
     * <p>To assert the matched fields <em>as well as</em> comparing them, use
     * {@link #alsoCheckMatching(Matcher, Matcher)}.
     *
     * @param fieldNamePattern the Hamcrest matcher used to match field names.
     * @param matcher          the Hamcrest matcher used to match the field value.
     * @param <V>              type of the field value to match
     * @return the instance of the matcher
     * @see #alsoCheckMatching(Matcher, Matcher)
     */
    <V> U withMatcher(Matcher<String> fieldNamePattern, Matcher<V> matcher);

    /**
     * The additional-assertion counterpart of {@link #withMatcher(Matcher, Matcher)}, as
     * {@link #alsoCheck(String, Matcher)} is to {@link #with(String, Matcher)}. Every field at any depth whose
     * name matches the pattern keeps its place in the comparison and is matched with the provided matcher as
     * well. If no field matches the pattern, the matcher passes vacuously.
     * Example:
     * <pre>sameJsonAsApproved().alsoCheckMatching(containsString("Count"), greaterThan(0L))</pre>
     *
     * <p>Unlike the path form, this cannot undo a {@link #withMatcher(Matcher, Matcher)} registration: patterns
     * accumulate in a list and two matcher instances never compare equal, so a field already made ignorable by
     * {@code withMatcher} stays ignored however it is registered afterwards.
     *
     * @param fieldNamePattern the Hamcrest matcher used to match field names.
     * @param matcher          the Hamcrest matcher used to match the field value.
     * @param <V>              type of the field value to match
     * @return the instance of the matcher
     * @see #withMatcher(Matcher, Matcher)
     * @see #alsoCheck(String, Matcher)
     */
    <V> U alsoCheckMatching(Matcher<String> fieldNamePattern, Matcher<V> matcher);

    /**
     * Remove array elements from the comparison based on the value of a nested field. The path
     * points at a field <em>within each element</em> of an array; the innermost array on the path
     * is the one filtered. Every element whose leaf field value satisfies {@code valueMatcher} is
     * removed before comparison. Intermediate collections are traversed transparently (fan-out),
     * so a path such as {@code entry.resource.meta.tag.system} filters the {@code tag} array of
     * every {@code entry}. Missing, empty or non-array paths are a silent no-op.
     * Example:
     * <pre>sameJsonAsApproved().ignoringElementsWhere("meta.tag.system", equalTo(FLOW_ID_TAG_SYSTEM))</pre>
     *
     * @param elementFieldPath the dot-separated path to the field within each array element.
     * @param valueMatcher     the Hamcrest matcher applied to that field's value to select elements
     *                         for removal.
     * @return the instance of the matcher
     */
    U ignoringElementsWhere(String elementFieldPath, Matcher<?> valueMatcher);

    /**
     * Convenience form of {@link #ignoringElementsWhere(String, Matcher)} that removes elements
     * whose leaf field, coerced to a String, equals {@code value}.
     * Example:
     * <pre>sameJsonAsApproved().ignoringElementsWhere("meta.tag.system", FLOW_ID_TAG_SYSTEM)</pre>
     *
     * @param elementFieldPath the dot-separated path to the field within each array element.
     * @param value            the value to compare the field's coerced String value against.
     * @return the instance of the matcher
     */
    U ignoringElementsWhere(String elementFieldPath, String value);

    /**
     * Specify a custom configuration for the Gson, for example, providing additional TypeAdapters.
     *
     * @param configuration {@link GsonConfiguration} object, containing TypeAdapterFactories, TypeAdapters and
     *                      TypeHierarchyAdapters.
     * @return the instance of the matcher
     */
    U withGsonConfiguration(GsonConfiguration configuration);

    /**
     * Specify the pattern of field names to ignore. Any bean property with a name that
     * matches the supplied pattern will be ignored.
     * Example:
     * <pre>assertThat(myBean, sameBeanAs(myResultBean).ignoring(is("mutationdate")).ignoring(containsString("version")))</pre>
     *
     * @param fieldNamePattern the Hamcrest matcher used to match field names.
     * @return the instance of the matcher
     */
    U ignoring(Matcher<String> fieldNamePattern);

    /**
     * Specify the pattern of field names to ignore. Any bean property with a name that
     * matches the supplied pattern will be ignored.
     * Example:
     * <pre>assertThat(myBean, sameBeanAs(myResultBean).ignoring(is("mutationdate",containsString("version"))))</pre>
     *
     * @param fieldNamePatterns the Hamcrest matchers used to match field names.
     * @return the instance of the matcher
     */
    @SuppressWarnings({"varargs", "unchecked"})
    U ignoring(Matcher<String>... fieldNamePatterns);

    /**
     * Specify function to be applied on fields in order to decide weather to include the field in circular reference check or not.
     *
     * @param matcher The {@link Function} to skip check, if it returns true the matching object the function applies to won't be checked for circular references.
     * @return the instance of the matcher
     */
    U skipCircularReferenceCheck(Function<Object, Boolean> matcher);

    /**
     * Specify function to be applied on fields in order to decide weather to include the field in circular reference check or not.
     *
     * @param matcher  The {@link Function} to skip check, if it returns true the matching object the function applies to won't be checked for circular references.
     * @param matchers The {@link Function}s to skip check
     * @return the instance of the matcher
     */
    @SuppressWarnings({"unchecked", "varargs"})
    U skipCircularReferenceCheck(Function<Object, Boolean> matcher, Function<Object, Boolean>... matchers);

    /**
     * Specify the pattern of field names to sort. Any bean property with a name that
     * matches the supplied pattern will be sorted (if sortable).
     * Example:
     * <pre>assertThat(myBean, sameBeanAs(myResultBean).sortField(is("mutationdate")).sortField(containsString("version")))</pre>
     *
     * @param fieldNamePattern the Hamcrest matcher used to match field names.
     * @return the instance of the matcher
     */
    U sortField(Matcher<String> fieldNamePattern);

    /**
     * Specify the pattern of field names to sort. Any bean property with a name that
     * matches the supplied pattern will be sorted (if sortable).
     * Example:
     * <pre>assertThat(myBean, sameBeanAs(myResultBean).sortField(is("mutationdate",containsString("version"))))</pre>
     *
     * @param fieldNamePatterns the Hamcrest matchers used to match field names.
     * @return the instance of the matcher
     */
    @SuppressWarnings({"varargs", "unchecked"})
    U sortField(Matcher<String>... fieldNamePatterns);

    /**
     * Specify the path of the field to be sorted.
     * Example:
     * <pre>sameBeanAs(expected).sortField("beanField.subBeanField")</pre>
     *
     * @param fieldPath the path of the field to be sorted (if sortable).
     * @return the instance of the matcher
     */
    U sortField(String fieldPath);

    /**
     * Specify the path of the field to be sorted.
     * Example:
     * <pre>sameBeanAs(expected).sortField("beanField.subBeanField","beanField2.subBeanField3")</pre>
     *
     * @param fieldPaths the paths of fields to be sorted (if sortable).
     * @return the instance of the matcher
     */
    U sortField(String... fieldPaths);

    U sortFieldMatcher(SortField<Matcher<String>> fieldNamePattern);

    @SuppressWarnings({"varargs", "unchecked"})
    U sortFieldMatcher(SortField<Matcher<String>>... fieldNamePatterns);

    U sortFieldPath(SortField<String> fieldPath);

    @SuppressWarnings({"varargs", "unchecked"})
    U sortFieldPath(SortField<String>... fieldPaths);

    /**
     * Automatically sort any {@code Collection} or array field whose element type is one of the
     * specified classes, as if it were a {@code Set}. This mirrors the automatic sorting applied
     * to {@code Set} and {@code Map} fields.
     *
     * <pre>sameBeanAs(expected).sortType(Person.class)</pre>
     * <pre>sameBeanAs(expected).sortType(Person.class, Address.class)</pre>
     *
     * @param types the element types whose containing collections should be sorted
     * @return the instance of the matcher
     */
    @SuppressWarnings({"varargs", "unchecked"})
    U sortType(Class<?>... types);

    /**
     * Merge the given {@link AliasMap} into this matcher. Alias entries are appended;
     * when two entries match the same primitive, the last registered wins.
     * Multiple calls to {@code withAliasMap} accumulate — they do not replace earlier maps.
     *
     * @param aliasMap the alias map to merge
     * @return the instance of the matcher
     */
    U withAliasMap(AliasMap aliasMap);

    /**
     * Convenience: add a single alias rule that replaces any primitive whose coerced string
     * value equals {@code value} (regardless of field name or path) with {@code alias}.
     *
     * @param value the raw value to match (coerced to String via {@code getAsString()})
     * @param alias the alias string to substitute
     * @return the instance of the matcher
     */
    U withAlias(String value, String alias);

    /**
     * Convenience: add a single alias rule scoped to a specific field name.
     * Replaces any primitive on a field named {@code fieldName} whose coerced value equals
     * {@code value} with {@code alias}.
     *
     * @param fieldName exact field name to scope the rule
     * @param value     the raw value to match
     * @param alias     the alias string to substitute
     * @return the instance of the matcher
     */
    U withAlias(String fieldName, String value, String alias);

    /**
     * Enable machine-readable output mode for this matcher.
     * When enabled, the failure message will contain the full expected/actual content with
     * structured delimiters (and the approved file path for file-based matchers),
     * instead of the default diff text. Useful for AI agents and CI environments.
     *
     * @return the instance of the matcher
     */
    U withMachineReadableOutput();

    /**
     * Disable null serialization for this matcher. By default, null-valued fields are
     * included in the serialized JSON so that {@code ignoring("field")} works correctly
     * even when the field value is null. Calling this method restores the legacy behaviour
     * where Gson omits null-valued fields from the output.
     * <p>
     * The default can also be changed globally by setting the system property
     * {@code approvalcrestSerializeNulls=false}.
     *
     * @return the instance of the matcher
     */
    U withoutSerializingNulls();
}