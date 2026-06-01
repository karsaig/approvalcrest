package com.github.karsaig.approvalcrest.jdk17;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Tests that approvalcrest correctly serializes and matches Java Records.
 */
public class RecordSerializationTest {

    record SimpleRecord(String name, int age) {}

    record NestedRecord(String label, SimpleRecord inner) {}

    record GenericRecord<T>(T value, String description) {}

    record RecordWithOptional(String id, Optional<String> nickname) {}

    record RecordWithList(String name, List<String> tags) {}

    @Test
    public void simpleRecordMatchesIdenticalInstance() {
        SimpleRecord actual = new SimpleRecord("Alice", 30);
        SimpleRecord expected = new SimpleRecord("Alice", 30);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void nestedRecordMatchesIdenticalInstance() {
        NestedRecord actual = new NestedRecord("parent", new SimpleRecord("child", 5));
        NestedRecord expected = new NestedRecord("parent", new SimpleRecord("child", 5));
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void genericRecordMatchesIdenticalInstance() {
        GenericRecord<String> actual = new GenericRecord<>("hello", "a string value");
        GenericRecord<String> expected = new GenericRecord<>("hello", "a string value");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void genericRecordWithComplexTypeMatchesIdenticalInstance() {
        GenericRecord<SimpleRecord> actual = new GenericRecord<>(new SimpleRecord("Bob", 25), "person record");
        GenericRecord<SimpleRecord> expected = new GenericRecord<>(new SimpleRecord("Bob", 25), "person record");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void recordWithOptionalPresentMatchesIdenticalInstance() {
        RecordWithOptional actual = new RecordWithOptional("id1", Optional.of("Nick"));
        RecordWithOptional expected = new RecordWithOptional("id1", Optional.of("Nick"));
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void recordWithOptionalEmptyMatchesIdenticalInstance() {
        RecordWithOptional actual = new RecordWithOptional("id2", Optional.empty());
        RecordWithOptional expected = new RecordWithOptional("id2", Optional.empty());
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void recordWithListMatchesIdenticalInstance() {
        RecordWithList actual = new RecordWithList("tagged", List.of("a", "b", "c"));
        RecordWithList expected = new RecordWithList("tagged", List.of("a", "b", "c"));
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void simpleRecordMatchesApprovedJson() {
        SimpleRecord actual = new SimpleRecord("Alice", 30);
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void nestedRecordMatchesApprovedJson() {
        NestedRecord actual = new NestedRecord("parent", new SimpleRecord("child", 5));
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void genericRecordMatchesApprovedJson() {
        GenericRecord<String> actual = new GenericRecord<>("hello", "a string value");
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void recordWithOptionalPresentMatchesApprovedJson() {
        RecordWithOptional actual = new RecordWithOptional("id1", Optional.of("Nick"));
        assertThat(actual, sameJsonAsApproved());
    }

    @Test
    public void recordWithOptionalEmptyMatchesApprovedJson() {
        RecordWithOptional actual = new RecordWithOptional("id2", Optional.empty());
        assertThat(actual, sameJsonAsApproved());
    }

    // ---- Negative cases: mismatch detection ----

    @Test
    public void simpleRecordMismatchDetected() {
        SimpleRecord actual = new SimpleRecord("Alice", 30);
        SimpleRecord expected = new SimpleRecord("Bob", 25);
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("name"), "Should report name mismatch");
        assertTrue(error.getMessage().contains("age"), "Should report age mismatch");
    }

    @Test
    public void nestedRecordMismatchDetected() {
        NestedRecord actual = new NestedRecord("parent", new SimpleRecord("Alice", 30));
        NestedRecord expected = new NestedRecord("parent", new SimpleRecord("Bob", 25));
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("name"), "Should report nested name mismatch");
    }

    @Test
    public void genericRecordMismatchDetected() {
        GenericRecord<String> actual = new GenericRecord<>("hello", "desc1");
        GenericRecord<String> expected = new GenericRecord<>("world", "desc2");
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("value"), "Should report value mismatch");
        assertTrue(error.getMessage().contains("description"), "Should report description mismatch");
    }

    @Test
    public void recordWithOptionalMismatchDetected() {
        RecordWithOptional actual = new RecordWithOptional("id1", Optional.of("Nick"));
        RecordWithOptional expected = new RecordWithOptional("id1", Optional.empty());
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("nickname"), "Should report nickname mismatch");
    }

    // --- Record inside Optional ---

    record Address(String street, String city, int zip) {}

    record PersonWithOptionalAddress(String name, Optional<Address> address) {}

    @Test
    public void recordInsideOptionalMatchesIdenticalInstance() {
        PersonWithOptionalAddress actual = new PersonWithOptionalAddress("Alice",
                Optional.of(new Address("123 Main St", "Springfield", 62704)));
        PersonWithOptionalAddress expected = new PersonWithOptionalAddress("Alice",
                Optional.of(new Address("123 Main St", "Springfield", 62704)));
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void recordInsideOptionalEmptyMatchesIdenticalInstance() {
        PersonWithOptionalAddress actual = new PersonWithOptionalAddress("Bob", Optional.empty());
        PersonWithOptionalAddress expected = new PersonWithOptionalAddress("Bob", Optional.empty());
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void recordInsideOptionalMismatchDetected() {
        PersonWithOptionalAddress actual = new PersonWithOptionalAddress("Alice",
                Optional.of(new Address("123 Main St", "Springfield", 62704)));
        PersonWithOptionalAddress expected = new PersonWithOptionalAddress("Alice",
                Optional.of(new Address("456 Oak Ave", "Shelbyville", 62705)));
        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, sameBeanAs(expected)));
        assertTrue(error.getMessage().contains("street"), "Should report street mismatch");
    }
}
