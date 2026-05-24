package com.github.karsaig.approvalcrest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.karsaig.approvalcrest.EnvVarReader.parseBoolean;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvVarReaderTest {

    @ParameterizedTest
    @ValueSource(strings = {"true", "TRUE", "True", "t", "T", "1", "yes", "YES", "Yes", "y", "Y"})
    public void parseBooleanReturnsTrueForTruthyValues(String value) {
        assertTrue(parseBoolean(value), "Expected true for: " + value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "FALSE", "False", "f", "F", "0", "no", "NO", "No", "n", "N", "2", "on", ""})
    public void parseBooleanReturnsFalseForFalsyValues(String value) {
        assertFalse(parseBoolean(value), "Expected false for: " + value);
    }

    @Test
    public void parseBooleanReturnsFalseForNull() {
        assertFalse(parseBoolean(null));
    }
}
