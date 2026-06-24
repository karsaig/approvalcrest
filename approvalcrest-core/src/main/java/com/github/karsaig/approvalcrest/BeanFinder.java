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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
