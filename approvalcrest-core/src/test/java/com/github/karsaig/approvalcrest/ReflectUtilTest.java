package com.github.karsaig.approvalcrest;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // --- module-opening mechanisms ---
    //
    // Both routes are invoked reflectively, so a stub with a matching method signature stands in
    // for a JVM arrangement that cannot be produced from inside this one: an attached agent, or a
    // JDK 26 where Unsafe is dead. The stubs avoid naming java.lang.Module, which does not exist
    // in the release 8 API this module compiles against.

    /** Stands in for Instrumentation, recording what it was asked to open. */
    public static class RecordingInstrumentation {
        Object target;
        Map<String, Set<Object>> opens;

        public void redefineModule(Object module, Set<Object> extraReads,
                                   Map<String, Set<Object>> extraExports,
                                   Map<String, Set<Object>> extraOpens,
                                   Set<Class<?>> extraUses,
                                   Map<Class<?>, List<Class<?>>> extraProvides) {
            this.target = module;
            this.opens = extraOpens;
        }
    }

    /** Stands in for an Instrumentation that refuses, as an unmodifiable module would. */
    public static class RefusingInstrumentation {
        public void redefineModule(Object module, Set<Object> extraReads,
                                   Map<String, Set<Object>> extraExports,
                                   Map<String, Set<Object>> extraOpens,
                                   Set<Class<?>> extraUses,
                                   Map<Class<?>, List<Class<?>>> extraProvides) {
            throw new IllegalArgumentException("package not in module");
        }
    }

    /** Stands in for MethodHandle.invokeWithArguments. */
    public static class RefusingHandleInvoker {
        public Object invokeWithArguments(Object[] arguments) {
            throw new UnsupportedOperationException("implAddOpens");
        }
    }

    private static Method redefineModuleOf(Class<?> stubType) throws NoSuchMethodException {
        return stubType.getMethod("redefineModule", Object.class, Set.class, Map.class, Map.class,
                Set.class, Map.class);
    }

    /**
     * Every module that performs reflection has to be named in the open, not just this class's.
     * They are the same module in an ordinary run, but the agent jar joins the system class path
     * and a suite could resolve approvalcrest and Gson from different loaders - in which case an
     * open naming only one of them would leave the other unable to read the field.
     */
    @Test
    void instrumentationOpenNamesEveryModuleItWasGiven() throws Exception {
        RecordingInstrumentation stub = new RecordingInstrumentation();
        Object moduleA = new Object();
        Object moduleB = new Object();
        Object target = new Object();

        boolean called = ReflectUtil.openViaInstrumentation(stub, redefineModuleOf(RecordingInstrumentation.class),
                target, "java.lang", new Object[]{moduleA, moduleB});

        assertThat(called, is(true));
        assertThat(stub.target, is(target));
        assertThat(stub.opens.keySet(), is(java.util.Collections.singleton("java.lang")));
        assertThat(stub.opens.get("java.lang"), is((Set<Object>) new java.util.LinkedHashSet<Object>(
                java.util.Arrays.asList(moduleA, moduleB))));
    }

    @Test
    void instrumentationOpenReportsFailureWhenTheCallIsRefused() throws Exception {
        assertThat(ReflectUtil.openViaInstrumentation(new RefusingInstrumentation(),
                redefineModuleOf(RefusingInstrumentation.class), new Object(), "java.lang",
                new Object[]{new Object()}), is(false));
    }

    @Test
    void instrumentationOpenReportsFailureWhenAnythingIsMissing() throws Exception {
        Method redefine = redefineModuleOf(RecordingInstrumentation.class);
        Object[] modules = new Object[]{new Object()};

        assertThat(ReflectUtil.openViaInstrumentation(null, redefine, new Object(), "p", modules), is(false));
        assertThat(ReflectUtil.openViaInstrumentation(new RecordingInstrumentation(), null, new Object(), "p", modules), is(false));
        assertThat(ReflectUtil.openViaInstrumentation(new RecordingInstrumentation(), redefine, new Object(), "p", null), is(false));
        assertThat(ReflectUtil.openViaInstrumentation(new RecordingInstrumentation(), redefine, new Object(), "p", new Object[0]), is(false));
    }

    @Test
    void unsafeOpenReportsFailureWhenTheHandleThrowsOrIsMissing() throws Exception {
        Method invoker = RefusingHandleInvoker.class.getMethod("invokeWithArguments", Object[].class);

        assertThat(ReflectUtil.openViaUnsafe(new RefusingHandleInvoker(), invoker, new Object(), "p", new Object()), is(false));
        assertThat(ReflectUtil.openViaUnsafe(null, invoker, new Object(), "p", new Object()), is(false));
        assertThat(ReflectUtil.openViaUnsafe(new Object(), null, new Object(), "p", new Object()), is(false));
        assertThat(ReflectUtil.openViaUnsafe(new Object(), invoker, new Object(), "p", null), is(false));
    }

    /**
     * Only the implication that actually holds. The reverse does not: Unsafe reading fields is not
     * the same condition as the Unsafe opening route having resolved, and on Java 8 Unsafe works
     * while there is no module to open at all.
     */
    @Test
    void theAgentRouteAlwaysCountsAsAWayToOpenAModule() {
        if (ReflectUtil.isInstrumentationAvailable()) {
            assertThat(ReflectUtil.isModuleOpeningAvailable(), is(true));
        }
    }

    /**
     * Nothing may report an open route while no route exists, which is the direction that would
     * let a locked type be claimed by a factory that cannot then read it.
     */
    @Test
    void noRouteIsReportedWhenNeitherMechanismResolved() {
        if (!ReflectUtil.isModuleOpeningAvailable()) {
            assertThat(ReflectUtil.isInstrumentationAvailable(), is(false));
        }
    }

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
