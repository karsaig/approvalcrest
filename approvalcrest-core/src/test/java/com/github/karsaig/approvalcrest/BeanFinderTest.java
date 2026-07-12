package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.BeanFinder.FanoutResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BeanFinder}, the engine that resolves a dot-separated field path against
 * a Java object graph. The interesting business behaviour is the transparent fan-out through
 * collections and the distinct error signals for "path is null half-way" versus "path does not
 * exist" — these are the paths a user hits when configuring {@code .ignoring("a.b.c")} or a
 * custom matcher on a nested field.
 */
public class BeanFinderTest {

    static class Person {
        final String name;

        Person(String name) {
            this.name = name;
        }
    }

    static class Holder {
        final Person person;

        Holder(Person person) {
            this.person = person;
        }
    }

    static class Parent {
        final String base = "inherited";
    }

    static class Child extends Parent {
        final String own = "own";
    }

    @Test
    void findsSimpleField() {
        Either<RuntimeException, Object> result = BeanFinder.findBeanAt("name", new Person("Alice"));

        assertTrue(result.isRight());
        assertThat(result.getRight(), is("Alice"));
    }

    @Test
    void findsNestedField() {
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("person.name", new Holder(new Person("Bob")));

        assertTrue(result.isRight());
        assertThat(result.getRight(), is("Bob"));
    }

    @Test
    void findsInheritedField() {
        Either<RuntimeException, Object> result = BeanFinder.findBeanAt("base", new Child());

        assertTrue(result.isRight());
        assertThat(result.getRight(), is("inherited"));
    }

    @Test
    void nullRootYieldsPathNullPointerException() {
        Either<RuntimeException, Object> result = BeanFinder.findBeanAt("name", null);

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
    }

    @Test
    void nullIntermediateYieldsPathNullPointerException() {
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("person.name", new Holder(null));

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
        assertThat(((PathNullPointerException) result.getLeft()).getPath(), is("person"));
    }

    @Test
    void missingFieldYieldsIllegalArgumentException() {
        Either<RuntimeException, Object> result = BeanFinder.findBeanAt("missing", new Person("x"));

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(IllegalArgumentException.class));
        assertThat(result.getLeft().getMessage(), is("missing does not exist"));
    }

    @Test
    void fansOutThroughCollection() {
        List<Person> people = Arrays.asList(new Person("a"), new Person("b"), new Person("c"));

        Either<RuntimeException, Object> result = BeanFinder.findBeanAt("name", people);

        assertTrue(result.isRight());
        assertThat(result.getRight(), instanceOf(FanoutResult.class));
        assertThat((FanoutResult) result.getRight(), contains("a", "b", "c"));
    }

    @Test
    void emptyCollectionFansOutToEmptyResult() {
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("name", Collections.emptyList());

        assertTrue(result.isRight());
        assertThat(result.getRight(), instanceOf(FanoutResult.class));
        assertThat((FanoutResult) result.getRight(), hasSize(0));
    }

    @Test
    void nullElementIsPreservedInFanout() {
        List<Person> people = Arrays.asList(new Person("a"), null);

        Either<RuntimeException, Object> result = BeanFinder.findBeanAt("name", people);

        assertTrue(result.isRight());
        FanoutResult fanout = (FanoutResult) result.getRight();
        assertThat(fanout, contains(is("a"), is((Object) null)));
    }

    @Test
    void collectionWhereEveryElementFailsReturnsLastError() {
        List<Object> objects = new ArrayList<>();
        objects.add(new Object());
        objects.add(new Object());

        Either<RuntimeException, Object> result = BeanFinder.findBeanAt("name", objects);

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(IllegalArgumentException.class));
    }

    @Test
    void heterogeneousCollectionKeepsResolvableElements() {
        List<Object> objects = new ArrayList<>();
        objects.add(new Person("a"));
        objects.add(new Object()); // no "name" field

        Either<RuntimeException, Object> result = BeanFinder.findBeanAt("name", objects);

        assertTrue(result.isRight());
        assertThat((FanoutResult) result.getRight(), contains("a"));
    }
}
