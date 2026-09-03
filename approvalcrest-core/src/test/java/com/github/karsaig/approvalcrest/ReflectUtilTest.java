package com.github.karsaig.approvalcrest;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    // --- memory-access probe ---
    //
    // From JDK 26 sun.misc.Unsafe is disabled rather than removed: Class.forName, the theUnsafe
    // field and every getMethod call still succeed, but invoking a memory-access method throws
    // UnsupportedOperationException. Resolution is therefore no longer evidence that a field can
    // be read, and treating it as such made safe mode claim locked-module types it could not
    // serialize. These stubs stand in for that JDK, which cannot be reproduced from inside a JVM
    // that was started with Unsafe working.

    /** Behaves as JDK 26+ does by default: resolves fine, throws on every call. */
    public static class DisabledUnsafe {
        public long objectFieldOffset(Field field) {
            throw new UnsupportedOperationException("objectFieldOffset");
        }

        public int getInt(Object obj, long offset) {
            throw new UnsupportedOperationException("getInt");
        }

        public Object getObject(Object obj, long offset) {
            throw new UnsupportedOperationException("getObject");
        }
    }

    /** Resolves and returns, but hands back values that were never in those fields. */
    public static class LyingUnsafe {
        public long objectFieldOffset(Field field) {
            return 0L;
        }

        public int getInt(Object obj, long offset) {
            return 0;
        }

        public Object getObject(Object obj, long offset) {
            return "not the probe value";
        }
    }

    private static Method stub(Class<?> stubType, String name, Class<?>... params) throws NoSuchMethodException {
        return stubType.getMethod(name, params);
    }

    private static Method[] stubMethods(Class<?> stubType) throws NoSuchMethodException {
        return new Method[]{
                stub(stubType, "objectFieldOffset", Field.class),
                stub(stubType, "getInt", Object.class, long.class),
                stub(stubType, "getObject", Object.class, long.class)
        };
    }

    @Test
    void memoryAccessProbePassesWithTheRealUnsafe() throws Exception {
        assumeTrue(ReflectUtil.isUnsafeAvailable(), "requires a JDK where Unsafe still reads memory");

        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);

        assertThat(ReflectUtil.memoryAccessWorks(
                theUnsafe.get(null),
                unsafeClass.getMethod("objectFieldOffset", Field.class),
                unsafeClass.getMethod("getInt", Object.class, long.class),
                unsafeClass.getMethod("getObject", Object.class, long.class)), is(true));
    }

    @Test
    void memoryAccessProbeFailsWhenTheMethodsThrow() throws Exception {
        Method[] methods = stubMethods(DisabledUnsafe.class);

        assertThat(ReflectUtil.memoryAccessWorks(new DisabledUnsafe(), methods[0], methods[1], methods[2]), is(false));
    }

    /**
     * A probe that only checked for an absent exception would pass here and let safe mode go on
     * claiming locked-module types, so the read-back values are compared too.
     */
    @Test
    void memoryAccessProbeFailsWhenTheValuesReadBackWrong() throws Exception {
        Method[] methods = stubMethods(LyingUnsafe.class);

        assertThat(ReflectUtil.memoryAccessWorks(new LyingUnsafe(), methods[0], methods[1], methods[2]), is(false));
    }

    @Test
    void memoryAccessProbeFailsWhenAnythingIsMissing() throws Exception {
        Method[] methods = stubMethods(LyingUnsafe.class);

        assertThat(ReflectUtil.memoryAccessWorks(null, methods[0], methods[1], methods[2]), is(false));
        assertThat(ReflectUtil.memoryAccessWorks(new Object(), null, methods[1], methods[2]), is(false));
        assertThat(ReflectUtil.memoryAccessWorks(new Object(), methods[0], null, methods[2]), is(false));
        assertThat(ReflectUtil.memoryAccessWorks(new Object(), methods[0], methods[1], null), is(false));
    }

    /**
     * isUnsafeAvailable() is what UnsafeFieldTypeAdapterFactory uses to decide whether to claim a
     * locked-module type, so a true answer has to mean a read will actually succeed. Reporting
     * availability on the strength of the class merely resolving is what produced empty JSON.
     */
    @Test
    void unsafeAvailabilityMatchesWhetherAReadSucceeds() throws Exception {
        boolean readSucceeded;
        try {
            readSucceeded = Integer.valueOf(42)
                    .equals(ReflectUtil.getFieldValueViaUnsafe(field("intField"), new AllTypes()));
        } catch (InaccessibleFieldException e) {
            readSucceeded = false;
        }

        assertThat(ReflectUtil.isUnsafeAvailable(), is(readSucceeded));
    }

    // --- locked-module detection guard rails ---

    // --- inherited locked-module fields ---
    //
    // A user's own exception class is in the unnamed module, so isInLockedModule is false for it
    // and neither fallback factory claimed it - but it still carries Throwable's private fields.
    // Where no module can be opened, Gson's reflective adapter threw JsonIOException on
    // detailMessage instead of the type degrading to getters.

    /** Public on purpose: getter-based serialization has to be able to invoke the accessors. */
    public static class UserDefinedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final String ticket;

        public UserDefinedException(String message, String ticket) {
            super(message);
            this.ticket = ticket;
        }

        public String getTicket() {
            return ticket;
        }
    }

    /**
     * Asserted as a relationship rather than a fixed value, because both sides depend on whether
     * this JDK could open java.lang - which is exactly the condition the guard exists for.
     */
    @Test
    void aSubclassInheritsWhateverItsSuperclassLockedStateIs() {
        assertThat(ReflectUtil.inheritsFromLockedModule(UserDefinedException.class),
                is(ReflectUtil.isInLockedModule(RuntimeException.class)));
    }

    @Test
    void theSubclassItselfIsNeverLocked() {
        // It lives in the unnamed module; only what it inherits can be locked.
        assertThat(ReflectUtil.isInLockedModule(UserDefinedException.class), is(false));
    }

    @Test
    void aClassExtendingOnlyObjectInheritsNothingLocked() {
        assertThat(ReflectUtil.inheritsFromLockedModule(AllTypes.class), is(false));
    }

    @Test
    void inheritanceCheckHandlesArraysPrimitivesAndNull() {
        assertThat(ReflectUtil.inheritsFromLockedModule(int[].class), is(false));
        assertThat(ReflectUtil.inheritsFromLockedModule(int.class), is(false));
        assertThat(ReflectUtil.inheritsFromLockedModule(null), is(false));
    }

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
