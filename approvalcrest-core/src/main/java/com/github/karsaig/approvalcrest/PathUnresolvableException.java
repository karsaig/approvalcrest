package com.github.karsaig.approvalcrest;

/**
 * Thrown when a path cannot name anything, whatever the data holds — as opposed to a path that is
 * well-formed but runs into a null.
 * <p>
 * The distinction matters because a failure to resolve a path against the object is normally retried
 * against the serialised JSON, which is what lets a path address a map entry by key or descend into an
 * array. A null in parsed JSON carries no type information, so that retry answers "null" for any path
 * below it and a {@code nullValue()} matcher passes — hiding a typo. This exception marks the cases
 * where the declared type is enough to prove the path wrong, so the retry is skipped.
 *
 * @see BeanFinder
 */
public class PathUnresolvableException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    PathUnresolvableException(String message) {
        super(message);
    }
}
