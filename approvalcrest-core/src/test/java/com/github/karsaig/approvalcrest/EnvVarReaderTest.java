package com.github.karsaig.approvalcrest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.karsaig.approvalcrest.EnvVarReader.getBooleanProperties;
import static com.github.karsaig.approvalcrest.EnvVarReader.getIntProperties;
import static com.github.karsaig.approvalcrest.EnvVarReader.getStringProperties;
import static com.github.karsaig.approvalcrest.EnvVarReader.parseBoolean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void getBooleanPropertiesReturnsDefaultWhenNoneSet() {
        String key1 = "test.getBooleanProperties.key1.absent";
        String key2 = "test.getBooleanProperties.key2.absent";
        System.clearProperty(key1);
        System.clearProperty(key2);

        assertFalse(getBooleanProperties(null, key1, key2));
        assertFalse(getBooleanProperties("false", key1, key2));
        assertTrue(getBooleanProperties("true", key1, key2));
    }

    @Test
    public void getBooleanPropertiesReturnsValueWhenOneKeySet() {
        String key1 = "test.getBooleanProperties.oneSet.canonical";
        String key2 = "test.getBooleanProperties.oneSet.alias";
        System.clearProperty(key1);
        System.clearProperty(key2);
        try {
            System.setProperty(key2, "true");
            assertTrue(getBooleanProperties(null, key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getBooleanPropertiesReturnsSameValueWhenAllKeysAgree() {
        String key1 = "test.getBooleanProperties.agree.canonical";
        String key2 = "test.getBooleanProperties.agree.alias";
        try {
            System.setProperty(key1, "true");
            System.setProperty(key2, "1");
            assertTrue(getBooleanProperties(null, key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getBooleanPropertiesThrowsWhenKeysConflict() {
        String key1 = "test.getBooleanProperties.conflict.canonical";
        String key2 = "test.getBooleanProperties.conflict.alias";
        try {
            System.setProperty(key1, "true");
            System.setProperty(key2, "false");
            assertThrows(IllegalStateException.class,
                    () -> getBooleanProperties(null, key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getStringPropertiesReturnsDefaultWhenNoneSet() {
        String key1 = "test.getStringProperties.absent.canonical";
        String key2 = "test.getStringProperties.absent.alias";
        System.clearProperty(key1);
        System.clearProperty(key2);

        assertEquals("fallback", getStringProperties("fallback", key1, key2));
    }

    @Test
    public void getStringPropertiesReturnsValueWhenOneKeySet() {
        String key1 = "test.getStringProperties.oneSet.canonical";
        String key2 = "test.getStringProperties.oneSet.alias";
        System.clearProperty(key1);
        System.clearProperty(key2);
        try {
            System.setProperty(key2, "chosen");
            assertEquals("chosen", getStringProperties("fallback", key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getStringPropertiesReturnsValueWhenAllKeysAgree() {
        String key1 = "test.getStringProperties.agree.canonical";
        String key2 = "test.getStringProperties.agree.alias";
        try {
            System.setProperty(key1, "same");
            System.setProperty(key2, "same");
            assertEquals("same", getStringProperties("fallback", key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getStringPropertiesThrowsWhenKeysConflict() {
        String key1 = "test.getStringProperties.conflict.canonical";
        String key2 = "test.getStringProperties.conflict.alias";
        try {
            System.setProperty(key1, "one");
            System.setProperty(key2, "two");
            assertThrows(IllegalStateException.class,
                    () -> getStringProperties("fallback", key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getIntPropertiesReturnsDefaultWhenNoneSet() {
        String key1 = "test.getIntProperties.absent.canonical";
        String key2 = "test.getIntProperties.absent.alias";
        System.clearProperty(key1);
        System.clearProperty(key2);

        assertEquals(42, getIntProperties(42, key1, key2));
    }

    @Test
    public void getIntPropertiesReturnsValueWhenOneKeySet() {
        String key1 = "test.getIntProperties.oneSet.canonical";
        String key2 = "test.getIntProperties.oneSet.alias";
        System.clearProperty(key1);
        System.clearProperty(key2);
        try {
            System.setProperty(key2, "7");
            assertEquals(7, getIntProperties(42, key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getIntPropertiesReturnsValueWhenAllKeysAgree() {
        String key1 = "test.getIntProperties.agree.canonical";
        String key2 = "test.getIntProperties.agree.alias";
        try {
            System.setProperty(key1, "5");
            System.setProperty(key2, "5");
            assertEquals(5, getIntProperties(42, key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getIntPropertiesThrowsWhenKeysConflict() {
        String key1 = "test.getIntProperties.conflict.canonical";
        String key2 = "test.getIntProperties.conflict.alias";
        try {
            System.setProperty(key1, "1");
            System.setProperty(key2, "2");
            assertThrows(IllegalStateException.class,
                    () -> getIntProperties(42, key1, key2));
        } finally {
            System.clearProperty(key1);
            System.clearProperty(key2);
        }
    }

    @Test
    public void getIntPropertiesThrowsWhenValueNotNumeric() {
        String key1 = "test.getIntProperties.nonNumeric.canonical";
        try {
            System.setProperty(key1, "not-a-number");
            assertThrows(IllegalStateException.class,
                    () -> getIntProperties(42, key1));
        } finally {
            System.clearProperty(key1);
        }
    }
}
