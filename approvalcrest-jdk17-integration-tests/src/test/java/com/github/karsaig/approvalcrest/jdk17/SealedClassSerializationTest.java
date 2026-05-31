package com.github.karsaig.approvalcrest.jdk17;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests that approvalcrest correctly serializes and matches sealed class hierarchies.
 */
public class SealedClassSerializationTest {

    sealed interface Shape permits Circle, Rectangle, Triangle {}

    record Circle(double radius) implements Shape {}

    record Rectangle(double width, double height) implements Shape {}

    record Triangle(double base, double height) implements Shape {}

    sealed interface Result<T> permits Success, Failure {}

    record Success<T>(T value) implements Result<T> {}

    record Failure<T>(String error) implements Result<T> {}

    static sealed class Vehicle permits Car, Truck {
        final String make;
        final int year;

        Vehicle(String make, int year) {
            this.make = make;
            this.year = year;
        }
    }

    static final class Car extends Vehicle {
        final int doors;

        Car(String make, int year, int doors) {
            super(make, year);
            this.doors = doors;
        }
    }

    static final class Truck extends Vehicle {
        final double payloadTons;

        Truck(String make, int year, double payloadTons) {
            super(make, year);
            this.payloadTons = payloadTons;
        }
    }

    record ShapeContainer(String name, Shape shape) {}

    @Test
    public void sealedRecordCircleMatchesIdenticalInstance() {
        Shape actual = new Circle(5.0);
        Shape expected = new Circle(5.0);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void sealedRecordRectangleMatchesIdenticalInstance() {
        Shape actual = new Rectangle(3.0, 4.0);
        Shape expected = new Rectangle(3.0, 4.0);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void sealedClassCarMatchesIdenticalInstance() {
        Vehicle actual = new Car("Toyota", 2023, 4);
        Vehicle expected = new Car("Toyota", 2023, 4);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void sealedClassTruckMatchesIdenticalInstance() {
        Vehicle actual = new Truck("Ford", 2022, 2.5);
        Vehicle expected = new Truck("Ford", 2022, 2.5);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void genericSealedSuccessMatchesIdenticalInstance() {
        Result<String> actual = new Success<>("done");
        Result<String> expected = new Success<>("done");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void genericSealedFailureMatchesIdenticalInstance() {
        Result<String> actual = new Failure<>("oops");
        Result<String> expected = new Failure<>("oops");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void containerWithSealedRecordMatchesIdenticalInstance() {
        ShapeContainer actual = new ShapeContainer("my circle", new Circle(2.5));
        ShapeContainer expected = new ShapeContainer("my circle", new Circle(2.5));
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void sealedRecordCircleMatchesApprovedJson() {
        Shape actual = new Circle(5.0);
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void sealedRecordRectangleMatchesApprovedJson() {
        Shape actual = new Rectangle(3.0, 4.0);
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void sealedClassCarMatchesApprovedJson() {
        Vehicle actual = new Car("Toyota", 2023, 4);
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void sealedClassTruckMatchesApprovedJson() {
        Vehicle actual = new Truck("Ford", 2022, 2.5);
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void containerWithSealedRecordMatchesApprovedJson() {
        ShapeContainer actual = new ShapeContainer("my circle", new Circle(2.5));
        assertThat(actual, sameJsonAsApproved());
    }

    // ---- Negative cases: mismatch detection ----

    @Test
    public void sealedRecordMismatchDetected() {
        Shape actual = new Circle(5.0);
        Shape expected = new Circle(10.0);
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("radius"), "Should report radius mismatch");
    }

    @Test
    public void sealedClassMismatchDetected() {
        Vehicle actual = new Car("Toyota", 2023, 4);
        Vehicle expected = new Car("Honda", 2024, 2);
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("make"), "Should report make mismatch");
        assertTrue(error.getMessage().contains("year"), "Should report year mismatch");
        assertTrue(error.getMessage().contains("doors"), "Should report doors mismatch");
    }

    @Test
    public void differentSealedSubtypesMismatchDetected() {
        Shape actual = new Circle(5.0);
        Shape expected = new Rectangle(3.0, 4.0);
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("is not an instance of expected type"),
                "Should report type mismatch between different sealed subtypes");
    }

    @Test
    public void containerWithDifferentSealedContentMismatchDetected() {
        ShapeContainer actual = new ShapeContainer("shape1", new Circle(5.0));
        ShapeContainer expected = new ShapeContainer("shape1", new Circle(10.0));
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("radius"), "Should report nested radius mismatch");
    }
}
