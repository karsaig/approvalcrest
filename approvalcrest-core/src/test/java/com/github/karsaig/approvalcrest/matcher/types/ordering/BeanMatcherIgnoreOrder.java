package com.github.karsaig.approvalcrest.matcher.types.ordering;

import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractTest;
import com.github.karsaig.approvalcrest.matcher.TestMatcherFactory;
import com.github.karsaig.approvalcrest.testdata.Bean;


public class BeanMatcherIgnoreOrder extends AbstractTest {

    private TestMatcherFactory matcherFactory = new TestMatcherFactory();

    @Test
    public void ignoresOrderingInNotLinkedSet() {
        Bean expected = bean().set(newHashSet(
                bean().string("a").build(),
                bean().string("b").build(),
                bean().string("c").build()))
                .build();

        Bean actual = bean().set(newHashSet(
                bean().string("c").build(),
                bean().string("a").build(),
                bean().string("b").build()))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingInSet() {
        Bean expected = bean().set(newLinkedHashSet(
                bean().string("a").build(),
                bean().string("b").build(),
                bean().string("c").build()))
                .build();

        Bean actual = bean().set(newLinkedHashSet(
                bean().string("c").build(),
                bean().string("b").build(),
                bean().string("a").build()))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingForSetsImplementations() {
        Bean expected = bean().hashSet(newLinkedHashSet(
                bean().string("a").build(),
                bean().string("b").build(),
                bean().string("c").build()))
                .build();

        Bean actual = bean().hashSet(newLinkedHashSet(
                bean().string("c").build(),
                bean().string("b").build(),
                bean().string("a").build()))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingInNotLinkedNestedSet() {
        Bean expected = bean().set(newHashSet(
                bean().set(newHashSet(
                        bean().string("a").build(),
                        bean().string("b").build(),
                        bean().string("c").build())).build(),
                bean().set(newHashSet(
                        bean().string("b").build(),
                        bean().string("a").build(),
                        bean().string("c").build())).build(),
                bean().set(newHashSet(
                        bean().string("c").build(),
                        bean().string("b").build(),
                        bean().string("a").build())).build()))
                .build();

        Bean actual = bean().set(newHashSet(
                bean().set(newHashSet(
                        bean().string("c").build(),
                        bean().string("b").build(),
                        bean().string("a").build())).build(),
                bean().set(newHashSet(
                        bean().string("a").build(),
                        bean().string("c").build(),
                        bean().string("b").build())).build(),
                bean().set(newHashSet(
                        bean().string("a").build(),
                        bean().string("b").build(),
                        bean().string("c").build())).build()))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingInNestedSet() {
        Bean expected = bean().set(newLinkedHashSet(
                bean().set(newLinkedHashSet(
                        bean().string("a").build(),
                        bean().string("b").build(),
                        bean().string("c").build())).build(),
                bean().set(newLinkedHashSet(
                        bean().string("b").build(),
                        bean().string("a").build(),
                        bean().string("c").build())).build(),
                bean().set(newLinkedHashSet(
                        bean().string("c").build(),
                        bean().string("b").build(),
                        bean().string("a").build())).build()))
                .build();

        Bean actual = bean().set(newLinkedHashSet(
                bean().set(newLinkedHashSet(
                        bean().string("c").build(),
                        bean().string("b").build(),
                        bean().string("a").build())).build(),
                bean().set(newLinkedHashSet(
                        bean().string("a").build(),
                        bean().string("c").build(),
                        bean().string("b").build())).build(),
                bean().set(newLinkedHashSet(
                        bean().string("a").build(),
                        bean().string("b").build(),
                        bean().string("c").build())).build()))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingInNotLinkedMap() {
        Bean expected = bean().map(newHashMap(
                bean().string("key1").build(), bean().string("value1").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key3").build(), bean().string("value3").build()
        ))
                .build();

        Bean actual = bean().map(newHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingInMap() {
        Bean expected = bean().map(newLinkedHashMap(
                bean().string("key1").build(), bean().string("value1").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key3").build(), bean().string("value3").build()
        ))
                .build();

        Bean actual = bean().map(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingForMapImplementations() {
        Bean expected = bean().hashMap(newLinkedHashMap(
                bean().string("key1").build(), bean().string("value1").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key3").build(), bean().string("value3").build()
        ))
                .build();

        Bean actual = bean().hashMap(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingInMapWithTwoEntriesWithSameJsonRepresentationAsKeyButDifferentValues() {
        Bean expected = bean().hashMap(newLinkedHashMap(
                bean().string("key1").build(), bean().string("value1").build(),
                bean().string("key1").build(), bean().string("value2").build(),
                bean().string("key3").build(), bean().string("value3").build()
        ))
                .build();

        Bean actual = bean().hashMap(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key1").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingInNotLinkedNestedMap() {
        Bean expected = bean().map(newHashMap(
                bean().string("key1").build(), bean().string("value1").map(newHashMap(
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key3").build(), bean().string("value3").build()
                )).build(),
                bean().string("key2").build(), bean().string("value2").map(newHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key1").build(), bean().string("value1").build()
                )).build(),
                bean().string("key3").build(), bean().string("value3").map(newHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build()
                )).build()
        ))
                .build();

        Bean actual = bean().map(newHashMap(
                bean().string("key3").build(), bean().string("value3").map(newHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build()
                )).build(),

                bean().string("key2").build(), bean().string("value2").map(newHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key1").build(), bean().string("value1").build()
                )).build(),
                bean().string("key1").build(), bean().string("value1").map(newHashMap(
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key3").build(), bean().string("value3").build()
                )).build()
        ))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @Test
    public void ignoresOrderingInNestedMap() {
        Bean expected = bean().map(newLinkedHashMap(
                bean().string("key1").build(), bean().string("value1").map(newLinkedHashMap(
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key3").build(), bean().string("value3").build()
                )).build(),
                bean().string("key2").build(), bean().string("value2").map(newLinkedHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key1").build(), bean().string("value1").build()
                )).build(),
                bean().string("key3").build(), bean().string("value3").map(newLinkedHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build()
                )).build()
        ))
                .build();

        Bean actual = bean().map(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").map(newLinkedHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build()
                )).build(),

                bean().string("key2").build(), bean().string("value2").map(newLinkedHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key1").build(), bean().string("value1").build()
                )).build(),
                bean().string("key1").build(), bean().string("value1").map(newLinkedHashMap(
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key3").build(), bean().string("value3").build()
                )).build()
        ))
                .build();

        assertThat(actual, matcherFactory.beanMatcher(expected));
    }

    @SuppressWarnings({"varargs", "unchecked"})
    private <T> HashSet<T> newLinkedHashSet(T... input) {
        return Arrays.stream(input).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private <K, T> HashMap<K, T> newLinkedHashMap(K key1, T value1, K key2, T value2, K key3, T value3) {
        HashMap<K, T> result = new LinkedHashMap<>();
        result.put(key1, value1);
        result.put(key2, value2);
        result.put(key3, value3);
        return result;
    }

    private <K, T> HashMap<K, T> newHashMap(K key1, T value1, K key2, T value2, K key3, T value3) {
        HashMap<K, T> result = new HashMap<>();
        result.put(key1, value1);
        result.put(key2, value2);
        result.put(key3, value3);
        return result;
    }
}
