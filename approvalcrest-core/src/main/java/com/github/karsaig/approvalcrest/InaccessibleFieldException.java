package com.github.karsaig.approvalcrest;

import java.lang.reflect.Field;

/**
 * Thrown when a field cannot be read via any available access strategy
 * (trySetAccessible failed and Unsafe is not available).
 * Signals to callers that they should use an alternative approach (e.g. getter-based serialization).
 */
public class InaccessibleFieldException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final transient Field field;

    public InaccessibleFieldException(Field field) {
        super("Cannot access field: " + field.getDeclaringClass().getName() + "." + field.getName());
        this.field = field;
    }

    public Field getField() {
        return field;
    }
}
