/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest;

import static java.util.Arrays.asList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;


/**
 * Returns the object corresponding to the path specified
 */
public class BeanFinder {

    private final static String PATH_REGEX = Pattern.quote(".");

    /**
     * Tagged list returned when a path segment traverses through a {@link Collection}.
     * Each element is the value found in the corresponding collection element.
     * Callers that need to distinguish "the field IS a collection" from "we fanned out
     * through a collection" can use {@code instanceof FanoutResult}.
     */
    public static class FanoutResult extends ArrayList<Object> {
        private static final long serialVersionUID = 1L;
        public FanoutResult() {
            super();
        }
    }

    public static Either<RuntimeException,Object> findBeanAt(String fieldPath, Object object) {
            return findBeanAt(fieldPath, asList(fieldPath.split(PATH_REGEX)), object);
    }

    private static Either<RuntimeException,Object> findBeanAt(String fullPath, List<String> fields, Object object) {
        try {
            if (object == null) {
                return Either.left(new PathNullPointerException(fields.get(0)));
            }
            // Transparent collection traversal: fan out into each element, mirroring
            // FieldsIgnorer's array-traversal behaviour.
            if (object instanceof Collection) {
                Collection<?> coll = (Collection<?>) object;
                FanoutResult fanout = new FanoutResult();
                Either<RuntimeException, Object> lastError = null;
                for (Object element : coll) {
                    if (element == null) {
                        fanout.add(null);
                        continue;
                    }
                    Either<RuntimeException, Object> r = findBeanAt(fullPath, fields, element);
                    if (r.isLeft()) {
                        lastError = r;
                    } else {
                        fanout.add(r.getRight());
                    }
                }
                return (!fanout.isEmpty() || lastError == null) ? Either.right(fanout) : lastError;
            }
            if (JsonElementUtil.WILDCARD.equals(headOf(fields)) && fields.size() > 1) {
                return fanOutOverNamedChildren(fullPath, fields.subList(1, fields.size()), object);
            }
            for (Field field : getEveryField(object.getClass())) {
                if (headOf(fields).equals(field.getName())) {
                    try {
                        Object value = ReflectUtil.getFieldValue(field, object);
                        if (fields.size() == 1) {
                            return Either.right(value);
                        } else {
                            if (value == null) {
                                return Either.left(new PathNullPointerException(field.getName()));
                            }
                            return findBeanAt(fullPath, fields.subList(1, fields.size()), value);
                        }
                    } catch (InaccessibleFieldException e) {
                        return Either.left(new IllegalArgumentException(
                                "Cannot access field '" + field.getName() + "' in locked module type "
                                        + field.getDeclaringClass().getName() + " for path: " + fullPath));
                    }
                }
            }

            return Either.left(new IllegalArgumentException(fullPath + " does not exist"));
        } catch (Exception e) {
            return Either.left(new IllegalArgumentException("Error searching for: " + fullPath,e));
        }
    }

    /**
     * Resolves {@code tail} against every named child of {@code object}, for a
     * {@link JsonElementUtil#WILDCARD} segment: a {@code Map}'s values, or a bean's field values.
     * A child the rest of the path cannot be traversed through — a null, or a scalar with segments
     * still to go — is skipped, and the whole call fails only when no child resolves {@code tail}.
     * That is deliberately unlike the collection branch above, which admits a null element as a null
     * value: a collection's elements are all intended by the path, whereas the wildcard selects
     * children by pattern, so one that leads nowhere is not a result of null but an irrelevance. Were
     * it admitted, a wildcard at the root of any bean with a null field could never match.
     * <p>
     * Resolving here rather than falling through to the serialised JSON means the values keep their
     * real types, so a matcher written against the element's own class matches and a numeric field is
     * not widened to {@code Long}. A {@code Collection} needs no branch: the transparent traversal
     * above already fans out without consuming a segment.
     */
    private static Either<RuntimeException, Object> fanOutOverNamedChildren(String fullPath, List<String> tail,
                                                                           Object object) {
        FanoutResult fanout = new FanoutResult();
        Either<RuntimeException, Object> lastError = null;
        if (object instanceof Map) {
            for (Object value : ((Map<?, ?>) object).values()) {
                if (value == null) {
                    continue;
                }
                Either<RuntimeException, Object> r = findBeanAt(fullPath, tail, value);
                if (r.isLeft()) {
                    lastError = r;
                } else if (!isEmptyFanout(r.getRight())) {
                    fanout.add(r.getRight());
                }
            }
        } else {
            for (Field field : getEveryField(object.getClass())) {
                if (isNotSerialised(field)) {
                    continue;
                }
                Object value;
                try {
                    value = ReflectUtil.getFieldValue(field, object);
                } catch (InaccessibleFieldException e) {
                    continue;
                }
                if (value == null) {
                    continue;
                }
                Either<RuntimeException, Object> r = findBeanAt(fullPath, tail, value);
                if (r.isLeft()) {
                    lastError = r;
                } else if (!isEmptyFanout(r.getRight())) {
                    fanout.add(r.getRight());
                }
            }
        }
        return (!fanout.isEmpty() || lastError == null) ? Either.right(fanout) : lastError;
    }

    /**
     * True for a field the serialiser leaves out, so a wildcard does not reach further than any named
     * path or the JSON form could.
     * <p>
     * This matters only for the wildcard: a named path can reach such a field, but only because
     * someone named it. {@code *} means "every named child", and the children a reader can see are the
     * ones that get serialised. Without the filter a wildcard walks {@code static} constants — on an
     * enum, the other constants of the same enum — and the synthetic {@code this$0} of a non-static
     * inner class, which resolves against the *enclosing* instance and so lets an assertion pass on
     * data that is not part of the object under comparison at all.
     */
    private static boolean isNotSerialised(Field field) {
        return field.isSynthetic()
                || Modifier.isStatic(field.getModifiers())
                || Modifier.isTransient(field.getModifiers());
    }

    /**
     * True when a child resolved to an <em>empty</em> fan-out, which under a wildcard counts as not
     * having resolved at all.
     * <p>
     * An empty fan-out is otherwise a deliberate failure — it stops {@code list.x} passing vacuously
     * over an empty list — but a wildcard's children are selected by pattern, and one that yields
     * nothing is an irrelevance rather than a result. Without this, a wildcard at the root of any bean
     * with an empty collection field could never match. If every child yields nothing the outer fan-out
     * is itself empty, so the vacuous pass stays closed.
     */
    private static boolean isEmptyFanout(Object value) {
        return value instanceof FanoutResult && ((FanoutResult) value).isEmpty();
    }

    private static String headOf(Collection<String> paths) {
        return paths.iterator().next();
    }

    private static List<Field> getEveryField(Class<?> type) {
        List<Field> result = new LinkedList<>();
        for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
            for (Field currentField : clazz.getDeclaredFields()) {
                result.add(currentField);
            }
        }
        return result;
    }
}
