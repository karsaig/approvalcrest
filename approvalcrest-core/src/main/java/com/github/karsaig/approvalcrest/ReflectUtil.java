package com.github.karsaig.approvalcrest;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides reflective field access with module-opening capability.
 * <p>
 * On Java 9+, uses Unsafe to obtain a trusted {@code MethodHandles.Lookup} and then
 * programmatically opens locked module packages (equivalent to {@code --add-opens} JVM flags).
 * Once a module package is opened, Gson's built-in ReflectiveTypeAdapterFactory works
 * unchanged, producing identical JSON output.
 * </p>
 * <p>
 * Modes (controlled via system property {@code approvalcrestReflection} or alias {@code aCReflection}):
 * <ul>
 *   <li>{@code safe} (default) — opens modules via Unsafe, then standard reflection</li>
 *   <li>{@code force} — uses setAccessible(true) directly (requires --add-opens on Java 9+)</li>
 *   <li>{@code fallback} — skips Unsafe entirely, uses getter-based (for testing/future-proofing)</li>
 * </ul>
 * <p>
 * IMPORTANT: All access to sun.misc.Unsafe, java.lang.Module, and MethodHandles.Lookup is
 * done via reflection. There is NO import of any of these. The code degrades gracefully to
 * getter-based serialization in both of the ways Unsafe goes away:
 * </p>
 * <ul>
 *   <li><b>Disabled</b> — from JDK 26 this is the default (JEP 471/498). The class, its
 *       {@code theUnsafe} field and its {@code Method} objects all still resolve, but every
 *       memory-access call throws {@code UnsupportedOperationException}. Resolution alone is
 *       therefore not evidence that a field can be read, so a probe call is made at
 *       class-initialization time and Unsafe is treated as absent when that probe fails.</li>
 *   <li><b>Removed</b> — in a later release. Class.forName throws ClassNotFoundException
 *       and the Unsafe initialization is skipped.</li>
 * </ul>
 * <p>
 * Either way: zero compile errors, zero runtime errors.
 * </p>
 */
public class ReflectUtil {

    private static final String MODE;
    private static final boolean FORCE_MODE;
    private static final boolean FALLBACK_MODE;

    // Java 9+ Module API via reflection — no compile-time dependency
    private static final Method TRY_SET_ACCESSIBLE;   // AccessibleObject.trySetAccessible()
    private static final Method GET_MODULE;           // Class.getModule()
    private static final Method IS_NAMED;             // Module.isNamed()
    private static final Method GET_PACKAGE_NAME;     // Class.getPackageName()
    private static final Method IS_OPEN;              // Module.isOpen(String, Module)

    // Unsafe access via reflection — no import, no compile-time dependency
    private static final Object UNSAFE;
    private static final Method OBJECT_FIELD_OFFSET;
    private static final Method GET_OBJECT;
    private static final Method GET_BOOLEAN;
    private static final Method GET_BYTE;
    private static final Method GET_CHAR;
    private static final Method GET_SHORT;
    private static final Method GET_INT;
    private static final Method GET_LONG;
    private static final Method GET_FLOAT;
    private static final Method GET_DOUBLE;

    // Module opening — cached MethodHandle (typed as Object) + invocation method
    private static final Object IMPL_ADD_OPENS_MH;    // MethodHandle for Module.implAddOpens
    private static final Method INVOKE_WITH_ARGS;     // MethodHandle.invokeWithArguments(Object[])
    private static final Object OUR_MODULE;           // the module of this class

    // Packages we've already successfully opened (to avoid retrying)
    private static final Set<String> OPENED_PACKAGES = ConcurrentHashMap.newKeySet();

    static {
        String mode = System.getProperty("approvalcrestReflection");
        if (mode == null) {
            mode = System.getProperty("aCReflection", "safe");
        }
        MODE = mode;
        FORCE_MODE = "force".equalsIgnoreCase(MODE);
        FALLBACK_MODE = "fallback".equalsIgnoreCase(MODE);

        // Detect Java 9+ Module API
        Method trySetAccessible = null;
        Method getModule = null;
        Method isNamed = null;
        Method getPackageName = null;
        Method isOpen = null;

        try {
            trySetAccessible = AccessibleObject.class.getMethod("trySetAccessible");
            getModule = Class.class.getMethod("getModule");
            Class<?> moduleClass = getModule.getReturnType();
            isNamed = moduleClass.getMethod("isNamed");
            getPackageName = Class.class.getMethod("getPackageName");
            isOpen = moduleClass.getMethod("isOpen", String.class, moduleClass);
        } catch (NoSuchMethodException e) {
            // Java 8 — Module API not available
            trySetAccessible = null;
            getModule = null;
            isNamed = null;
            getPackageName = null;
            isOpen = null;
        }

        TRY_SET_ACCESSIBLE = trySetAccessible;
        GET_MODULE = getModule;
        IS_NAMED = isNamed;
        GET_PACKAGE_NAME = getPackageName;
        IS_OPEN = isOpen;

        // Initialize Unsafe
        Object unsafe = null;
        Method objectFieldOffset = null;
        Method getObject = null;
        Method getBoolean = null;
        Method getByte = null;
        Method getChar = null;
        Method getShort = null;
        Method getInt = null;
        Method getLong = null;
        Method getFloat = null;
        Method getDouble = null;

        if (!FALLBACK_MODE) {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                unsafe = theUnsafe.get(null);

                objectFieldOffset = unsafeClass.getMethod("objectFieldOffset", Field.class);
                getObject = unsafeClass.getMethod("getObject", Object.class, long.class);
                getBoolean = unsafeClass.getMethod("getBoolean", Object.class, long.class);
                getByte = unsafeClass.getMethod("getByte", Object.class, long.class);
                getChar = unsafeClass.getMethod("getChar", Object.class, long.class);
                getShort = unsafeClass.getMethod("getShort", Object.class, long.class);
                getInt = unsafeClass.getMethod("getInt", Object.class, long.class);
                getLong = unsafeClass.getMethod("getLong", Object.class, long.class);
                getFloat = unsafeClass.getMethod("getFloat", Object.class, long.class);
                getDouble = unsafeClass.getMethod("getDouble", Object.class, long.class);
            } catch (ClassNotFoundException e) {
                // Unsafe completely removed from this JDK
                unsafe = null;
            } catch (Exception e) {
                // Any other failure (security manager, etc.)
                unsafe = null;
            }
        }

        // sun.misc.Unsafe resolving is no longer evidence that it works. From JDK 26 the
        // memory-access methods throw UnsupportedOperationException by default while the class,
        // theUnsafe and the Method objects all still resolve, so prove the field-read path with a
        // probe call. Dropping the reference here is what makes every downstream branch - Tier 2
        // in getFieldValue, getFieldValueViaUnsafe, the module opening below and
        // isUnsafeAvailable() - degrade to the getter-based path instead of failing on first read.
        //
        // Only on Java 9+, keyed off the Module API. Java 8 has no way to disable Unsafe, so there
        // is nothing to detect; probing there could only ever turn a working field-based run into a
        // getter-based one, which would change the output of every Java 8 consumer.
        if (unsafe != null && getModule != null
                && !memoryAccessWorks(unsafe, objectFieldOffset, getInt, getObject)) {
            unsafe = null;
        }

        UNSAFE = unsafe;
        OBJECT_FIELD_OFFSET = objectFieldOffset;
        GET_OBJECT = getObject;
        GET_BOOLEAN = getBoolean;
        GET_BYTE = getByte;
        GET_CHAR = getChar;
        GET_SHORT = getShort;
        GET_INT = getInt;
        GET_LONG = getLong;
        GET_FLOAT = getFloat;
        GET_DOUBLE = getDouble;

        // Initialize module-opening capability via trusted Lookup
        Object implAddOpensMh = null;
        Method invokeWithArgs = null;
        Object ourModule = null;

        if (unsafe != null && getModule != null && objectFieldOffset != null && getObject != null) {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
                Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);

                // Read MethodHandles.Lookup.IMPL_LOOKUP via Unsafe (trusted Lookup with full access)
                Class<?> lookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
                Field implLookupField = lookupClass.getDeclaredField("IMPL_LOOKUP");
                long sOffset = (long) staticFieldOffset.invoke(unsafe, implLookupField);
                Object base = staticFieldBase.invoke(unsafe, implLookupField);
                Object trustedLookup = getObject.invoke(unsafe, base, sOffset);

                // Build MethodType for void(String, Module)
                Class<?> moduleClass = getModule.getReturnType();
                Class<?> methodTypeClass = Class.forName("java.lang.invoke.MethodType");
                Method methodTypeMethod = methodTypeClass.getMethod("methodType", Class.class, Class[].class);
                Object mt = methodTypeMethod.invoke(null, void.class, new Class<?>[]{String.class, moduleClass});

                // Get MethodHandle for Module.implAddOpens(String, Module)
                Method findVirtual = lookupClass.getMethod("findVirtual", Class.class, String.class, methodTypeClass);
                implAddOpensMh = findVirtual.invoke(trustedLookup, moduleClass, "implAddOpens", mt);

                // Get MethodHandle.invokeWithArguments(Object[])
                Class<?> methodHandleClass = Class.forName("java.lang.invoke.MethodHandle");
                invokeWithArgs = methodHandleClass.getMethod("invokeWithArguments", Object[].class);

                // Cache our module reference
                ourModule = getModule.invoke(ReflectUtil.class);
            } catch (Exception e) {
                // Module opening not possible on this JDK
                implAddOpensMh = null;
                invokeWithArgs = null;
                ourModule = null;
            }
        }

        IMPL_ADD_OPENS_MH = implAddOpensMh;
        INVOKE_WITH_ARGS = invokeWithArgs;
        OUR_MODULE = ourModule;

        // Proactively open java.lang to our module. This is needed because
        // Gson's ReflectiveTypeAdapterFactory accesses Throwable fields (detailMessage etc.)
        // even when the serialized class itself is in an unnamed module (e.g. test framework
        // exception classes that extend java.lang.Throwable).
        if (implAddOpensMh != null && invokeWithArgs != null && ourModule != null && getModule != null) {
            try {
                Object javaBaseModule = getModule.invoke(String.class);
                tryOpenModule(javaBaseModule, "java.lang", ourModule);
            } catch (Exception ignored) {
                // Best-effort; if it fails, fallback behavior still works
            }
        }
    }

    /**
     * Instance fields read by {@link #memoryAccessWorks} to prove that Unsafe can still read
     * memory. Deliberately a type we own, so the probe never depends on module access.
     */
    private static final class MemoryAccessProbe {
        static final int INT_VALUE = 0x5A5A5A5A;
        static final String REF_VALUE = "approvalcrest-memory-access-probe";

        private int intField = INT_VALUE;
        private Object refField = REF_VALUE;
    }

    /**
     * Checks that Unsafe's memory-access methods actually read memory, rather than merely
     * resolving. Reads back a known int and a known reference and compares them, so a method that
     * throws and a method that silently returns nothing useful are both reported as unavailable.
     *
     * @return true only if both probe reads returned the expected values
     */
    static boolean memoryAccessWorks(Object unsafe, Method objectFieldOffset, Method getInt, Method getObject) {
        if (unsafe == null || objectFieldOffset == null || getInt == null || getObject == null) {
            return false;
        }
        try {
            MemoryAccessProbe probe = new MemoryAccessProbe();

            Field intField = MemoryAccessProbe.class.getDeclaredField("intField");
            long intOffset = (long) objectFieldOffset.invoke(unsafe, intField);
            if (!Integer.valueOf(MemoryAccessProbe.INT_VALUE).equals(getInt.invoke(unsafe, probe, intOffset))) {
                return false;
            }

            Field refField = MemoryAccessProbe.class.getDeclaredField("refField");
            long refOffset = (long) objectFieldOffset.invoke(unsafe, refField);
            return MemoryAccessProbe.REF_VALUE.equals(getObject.invoke(unsafe, probe, refOffset));
        } catch (Exception e) {
            // Disabled (UnsupportedOperationException on JDK 26+), or otherwise unusable
            return false;
        }
    }

    /**
     * Attempts to make the given AccessibleObject accessible.
     * On Java 9+ in safe/fallback mode, uses trySetAccessible() which returns false
     * for locked modules instead of throwing.
     * On Java 8 or force mode, uses setAccessible(true).
     *
     * @return true if the object is now accessible, false otherwise
     */
    public static boolean makeAccessible(AccessibleObject ao) {
        if (FORCE_MODE) {
            ao.setAccessible(true);
            return true;
        }
        if (TRY_SET_ACCESSIBLE != null) {
            try {
                return (boolean) TRY_SET_ACCESSIBLE.invoke(ao);
            } catch (Exception e) {
                return false;
            }
        }
        // Java 8: setAccessible always works
        ao.setAccessible(true);
        return true;
    }

    /**
     * Checks if the given class is in a module that doesn't allow reflective access
     * to its internals from our module (the unnamed module / classpath).
     * <p>
     * On Java 8 (no Module API), always returns false.
     * In force mode, always returns false.
     * In safe mode with Unsafe available: attempts to open the module on-demand.
     * Arrays, primitives, and types without meaningful packages return false.
     */
    public static boolean isInLockedModule(Class<?> clazz) {
        if (FORCE_MODE) {
            return false;
        }
        if (GET_MODULE == null) {
            // Java 8 — no modules
            return false;
        }
        // Arrays and primitives are not module-locked
        if (clazz.isArray() || clazz.isPrimitive()) {
            return false;
        }
        try {
            Object module = GET_MODULE.invoke(clazz);
            boolean named = (boolean) IS_NAMED.invoke(module);
            if (!named) {
                return false;
            }
            String pkg = (String) GET_PACKAGE_NAME.invoke(clazz);
            if (pkg == null || pkg.isEmpty()) {
                return false;
            }

            // In fallback mode, treat ALL named-module types as locked regardless of
            // Module.isOpen(). On JDK 9-15 with --illegal-access=permit (default),
            // isOpen() returns true even though we want getter-based fallback behavior.
            if (FALLBACK_MODE) {
                return true;
            }

            Object ourMod = OUR_MODULE != null ? OUR_MODULE : GET_MODULE.invoke(ReflectUtil.class);
            boolean open = (boolean) IS_OPEN.invoke(module, pkg, ourMod);
            if (open) {
                return false;
            }

            // Module is locked — try to open it programmatically
            if (tryOpenModule(module, pkg, ourMod)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the given class inherits fields from a locked module, which is not the same
     * question as {@link #isInLockedModule(Class)} asks.
     * <p>
     * A user's own exception class is in the unnamed module, so it is not itself locked, but it
     * carries {@code java.lang.Throwable}'s private fields. When no module can be opened those
     * fields are unreadable, and standard reflection throws rather than returning nothing - so
     * such a type has to be serialized from its getters like a locked type, instead of being left
     * to fail.
     * </p>
     *
     * @return true if any superclass above the given one is in a locked module
     */
    public static boolean inheritsFromLockedModule(Class<?> clazz) {
        if (clazz == null || clazz.isArray() || clazz.isPrimitive()) {
            return false;
        }
        for (Class<?> c = clazz.getSuperclass(); c != null && c != Object.class; c = c.getSuperclass()) {
            if (isInLockedModule(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to open a locked module package to our module using Unsafe + trusted Lookup.
     * This is equivalent to {@code --add-opens module/package=ALL-UNNAMED} but done at runtime.
     *
     * @return true if the module was successfully opened
     */
    private static boolean tryOpenModule(Object targetModule, String pkg, Object ourMod) {
        if (IMPL_ADD_OPENS_MH == null || INVOKE_WITH_ARGS == null) {
            return false;
        }
        String key = System.identityHashCode(targetModule) + "/" + pkg;
        if (!OPENED_PACKAGES.add(key)) {
            // Already attempted — check if it worked
            try {
                return (boolean) IS_OPEN.invoke(targetModule, pkg, ourMod);
            } catch (Exception e) {
                return false;
            }
        }
        try {
            INVOKE_WITH_ARGS.invoke(IMPL_ADD_OPENS_MH, (Object) new Object[]{targetModule, pkg, ourMod});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads a field value using the multi-tier strategy:
     * <ol>
     *   <li>trySetAccessible/setAccessible + get (standard reflection)</li>
     *   <li>Unsafe via reflection (bypasses module system)</li>
     *   <li>Throws InaccessibleFieldException (signals caller to use getter-based approach)</li>
     * </ol>
     */
    public static Object getFieldValue(Field field, Object obj) throws InaccessibleFieldException {
        if (FORCE_MODE) {
            try {
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
                throw new InaccessibleFieldException(field, e);
            }
        }

        // Tier 1: try standard reflection
        if (TRY_SET_ACCESSIBLE != null) {
            try {
                boolean accessible = (boolean) TRY_SET_ACCESSIBLE.invoke(field);
                if (accessible) {
                    return field.get(obj);
                }
            } catch (Exception e) {
                // Fall through to Tier 2
            }
        } else {
            // Java 8: setAccessible always works
            try {
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
                // Fall through to Tier 2
            }
        }

        // Tier 2: Unsafe bypass (if not in fallback mode and Unsafe available)
        if (!FALLBACK_MODE && UNSAFE != null) {
            return getFieldValueViaUnsafe(field, obj);
        }

        // All tiers exhausted
        throw new InaccessibleFieldException(field);
    }

    /**
     * Reads a field value using only Unsafe (bypasses access checks entirely).
     * Returns the value boxed for primitives.
     *
     * @throws InaccessibleFieldException if Unsafe is not available or invocation fails
     */
    public static Object getFieldValueViaUnsafe(Field field, Object obj) throws InaccessibleFieldException {
        if (UNSAFE == null) {
            throw new InaccessibleFieldException(field);
        }
        try {
            long offset = (long) OBJECT_FIELD_OFFSET.invoke(UNSAFE, field);
            Class<?> type = field.getType();
            if (type == boolean.class) return GET_BOOLEAN.invoke(UNSAFE, obj, offset);
            if (type == byte.class) return GET_BYTE.invoke(UNSAFE, obj, offset);
            if (type == char.class) return GET_CHAR.invoke(UNSAFE, obj, offset);
            if (type == short.class) return GET_SHORT.invoke(UNSAFE, obj, offset);
            if (type == int.class) return GET_INT.invoke(UNSAFE, obj, offset);
            if (type == long.class) return GET_LONG.invoke(UNSAFE, obj, offset);
            if (type == float.class) return GET_FLOAT.invoke(UNSAFE, obj, offset);
            if (type == double.class) return GET_DOUBLE.invoke(UNSAFE, obj, offset);
            return GET_OBJECT.invoke(UNSAFE, obj, offset);
        } catch (Exception e) {
            throw new InaccessibleFieldException(field, e);
        }
    }

    /**
     * @return true if in force mode (original setAccessible behavior)
     */
    public static boolean isForceMode() {
        return FORCE_MODE;
    }

    /**
     * @return true if in fallback mode (skip Unsafe, use getter-based)
     */
    public static boolean isFallbackMode() {
        return FALLBACK_MODE;
    }

    /**
     * @return true if Unsafe can actually read fields on this JDK and we are not in fallback mode.
     *         False from JDK 26 onwards unless {@code --sun-misc-unsafe-memory-access=allow} is
     *         set, because the memory-access methods resolve there but throw when called.
     */
    public static boolean isUnsafeAvailable() {
        return UNSAFE != null && !FALLBACK_MODE;
    }

    /**
     * @return the current mode string (safe, force, or fallback)
     */
    public static String getMode() {
        return MODE;
    }
}
