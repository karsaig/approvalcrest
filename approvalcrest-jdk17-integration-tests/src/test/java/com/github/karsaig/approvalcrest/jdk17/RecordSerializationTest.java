package com.github.karsaig.approvalcrest.jdk17;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;

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
}
