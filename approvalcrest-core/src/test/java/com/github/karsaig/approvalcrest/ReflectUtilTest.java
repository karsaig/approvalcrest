package com.github.karsaig.approvalcrest;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link ReflectUtil}. The default (unconfigured) test run executes in "safe"
 * mode with Unsafe available, which is exactly the environment these assertions target. The
 * meaty, previously-uncovered logic is the Unsafe-based field reader that boxes every primitive
 * type, plus the locked-module detection guard rails.
 */
public class ReflectUtilTest {

    @SuppressWarnings("unused")
    static class AllTypes {
        private boolean boolField = true;
        private byte byteField = (byte) 7;
        private char charField = 'Z';
        private short shortField = (short) 12345;
        private int intField = 42;
        private long longField = 9_000_000_000L;
        private float floatField = 3.5f;
        private double doubleField = 2.75d;
        private String refField = "hello";
        private String nullField = null;
    }

    private static Field field(String name) throws NoSuchFieldException {
        return AllTypes.class.getDeclaredField(name);
    }

    @SuppressWarnings("unused")
    static class WithStaticField {
        private static String staticField = "value";
    }

    /**
     * The underlying reflection failure has to survive. Without it an IllegalAccessException, an
     * InvocationTargetException and a bad-offset IllegalArgumentException all collapse into the
     * same "Cannot access field" message, which is what the eventual --add-opens guidance is built
     * from - leaving no way to tell whether that advice even applies.
     *
     * <p>A static field is used as the trigger because Unsafe.objectFieldOffset rejects it
     * outright, which exercises the failure path without any memory-unsafe access.
     */
    @Test
    void inaccessibleFieldExceptionKeepsTheUnderlyingCause() throws Exception {
        assumeTrue(ReflectUtil.isUnsafeAvailable(), "requires Unsafe");
        Field staticField = WithStaticField.class.getDeclaredField("staticField");

        InaccessibleFieldException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                InaccessibleFieldException.class,
                () -> ReflectUtil.getFieldValueViaUnsafe(staticField, new WithStaticField()));

        assertThat("the reflection failure must be preserved", thrown.getCause(), is(notNullValue()));
        assertThat(thrown.getField(), is(staticField));
    }

    // --- mode detection ---

    @Test
    void modeFlagsAreConsistentWithMode() {
        // The core test suite is executed in several reflection modes (safe/force/fallback),
        // so assert the boolean accessors stay consistent with getMode() rather than pinning
        // a specific mode.
        String mode = ReflectUtil.getMode();
        assertThat(mode, is(notNullValue()));
        assertThat(ReflectUtil.isForceMode(), is("force".equalsIgnoreCase(mode)));
        assertThat(ReflectUtil.isFallbackMode(), is("fallback".equalsIgnoreCase(mode)));
    }

    // --- standard-reflection tier ---

    @Test
    void getFieldValueReadsAccessibleField() throws Exception {
        Object value = ReflectUtil.getFieldValue(field("intField"), new AllTypes());
        assertThat(value, is(42));
    }

    @Test
    void makeAccessibleReturnsTrueForOwnField() throws Exception {
        assertThat(ReflectUtil.makeAccessible(field("intField")), is(true));
    }

    // --- Unsafe tier: every primitive type is boxed correctly ---

    @Test
    void getFieldValueViaUnsafeReadsAllPrimitiveTypes() throws Exception {
        assumeTrue(ReflectUtil.isUnsafeAvailable(), "Unsafe not available on this JDK");
        AllTypes obj = new AllTypes();

        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("boolField"), obj), is(true));
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("byteField"), obj), is((byte) 7));
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("charField"), obj), is('Z'));
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("shortField"), obj), is((short) 12345));
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("intField"), obj), is(42));
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("longField"), obj), is(9_000_000_000L));
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("floatField"), obj), is(3.5f));
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("doubleField"), obj), is(2.75d));
    }

    @Test
    void getFieldValueViaUnsafeReadsReferenceField() throws Exception {
        assumeTrue(ReflectUtil.isUnsafeAvailable(), "Unsafe not available on this JDK");
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("refField"), new AllTypes()), is("hello"));
    }

    @Test
    void getFieldValueViaUnsafeReadsNullReferenceField() throws Exception {
        assumeTrue(ReflectUtil.isUnsafeAvailable(), "Unsafe not available on this JDK");
        assertThat(ReflectUtil.getFieldValueViaUnsafe(field("nullField"), new AllTypes()), is((Object) null));
    }

    // --- locked-module detection guard rails ---

    @Test
    void arraysAndPrimitivesAreNeverLocked() {
        assertThat(ReflectUtil.isInLockedModule(int[].class), is(false));
        assertThat(ReflectUtil.isInLockedModule(int.class), is(false));
    }

    @Test
    void classpathTypesAreNotLocked() {
        // Types on our own (unnamed) module are never locked.
        assertThat(ReflectUtil.isInLockedModule(AllTypes.class), is(false));
    }
}
