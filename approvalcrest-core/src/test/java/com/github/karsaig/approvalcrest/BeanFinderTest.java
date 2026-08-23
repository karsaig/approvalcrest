package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.BeanFinder.FanoutResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    static class Containers {
        final Map<String, Person> peopleByName = null;
        final List<Person> people = null;
        final Person[] peopleArray = null;
        final Object anything = null;
        final String text = null;
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

    // -----------------------------------------------------------------------
    // A null half-way along a path: when the declared type settles it, and when it cannot
    // -----------------------------------------------------------------------

    @Test
    void nullIntermediateWithAnUnknownNextSegmentIsRejected() {
        // Person declares no "bogus", so no data could make person.bogus resolve. Reporting this as
        // a null would let the JSON retry answer null for it and a nullValue() matcher pass.
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("person.bogus", new Holder(null));

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathUnresolvableException.class));
        assertThat(result.getLeft().getMessage(), is("person.bogus does not exist"));
    }

    @Test
    void nullIntermediateWithAnUnknownNextSegmentIsRejectedEvenWithMoreSegmentsToGo() {
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("person.bogus.deeper", new Holder(null));

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathUnresolvableException.class));
        assertThat(result.getLeft().getMessage(), is("person.bogus.deeper does not exist"));
    }

    @Test
    void nullIntermediateWithAKnownNextSegmentStaysLenientHoweverDeepThePathGoes() {
        // Only the segment straight after the null is checked. "name" is a field of Person, so this
        // reports a null and lets the JSON retry have its say, even though nothing could follow a
        // String. Checking further would need the generic type arguments erasure has discarded.
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("person.name.deeper", new Holder(null));

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
    }

    @Test
    void nullMapFieldStaysLenientBecauseTheNextSegmentIsAKeyNotAFieldName() {
        // Map entries are addressed by key, so "Alice" is data. Rejecting it would break map-key
        // addressing, which this walk cannot resolve at all and the JSON retry always handles.
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("peopleByName.Alice.name", new Containers());

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
    }

    @Test
    void nullCollectionFieldStaysLenientBecauseTheNextSegmentBelongsToTheElementType() {
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("people.name", new Containers());

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
    }

    @Test
    void nullArrayFieldStaysLenient() {
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("peopleArray.name", new Containers());

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
    }

    @Test
    void nullFieldOfAnUnresolvableTypeStaysLenient() {
        // A T-typed field erases to Object, which declares nothing a path could name.
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("anything.whatever", new Containers());

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
    }

    @Test
    void nullJdkTypedFieldStaysLenient() {
        // String has private fields that differ between JDK releases; they are not something to
        // hand a user a verdict on.
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("text.whatever", new Containers());

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
    }

    @Test
    void nullFieldFollowedByAWildcardStaysLenient() {
        // "*" is never a field name, so it cannot be checked against the declared type.
        Either<RuntimeException, Object> result =
                BeanFinder.findBeanAt("person.*.name", new Holder(null));

        assertTrue(result.isLeft());
        assertThat(result.getLeft(), instanceOf(PathNullPointerException.class));
    }
}
