package com.github.karsaig.approvalcrest;

import java.lang.reflect.Field;

/**
 * Thrown when a field cannot be read via any available access strategy
 * (trySetAccessible failed and no module could be opened, so Unsafe is not available either).
 * Signals to callers that they should use an alternative approach (e.g. getter-based serialization).
 */
public class InaccessibleFieldException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final transient Field field;

    public InaccessibleFieldException(Field field) {
        super(message(field));
        this.field = field;
    }

    /**
     * Keeps the underlying reflection failure. Without it an {@code IllegalAccessException}, an
     * {@code InvocationTargetException}, a {@code SecurityException} and a bad-offset
     * {@code IllegalArgumentException} all collapse into the same message, which makes the advice
     * this exception eventually turns into impossible to check.
     */
    public InaccessibleFieldException(Field field, Throwable cause) {
        super(message(field), cause);
        this.field = field;
    }

    private static String message(Field field) {
        return "Cannot access field: " + field.getDeclaringClass().getName() + "." + field.getName();
    }

    public Field getField() {
        return field;
    }
}
