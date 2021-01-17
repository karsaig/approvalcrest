package com.github.karsaig.approvalcrest.matcher.types.ordering;

import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.function.Function.identity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.Bean;


public class JsonMatcherIgnoreOrder extends AbstractFileMatcherTest {

    @Test
    public void ignoresOrderingInNotLinkedSet() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"string\": \"a\",\n" +
                "      \"integer\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"string\": \"b\",\n" +
                "      \"integer\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"string\": \"c\",\n" +
                "      \"integer\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().set(newHashSet(
                bean().string("c").build(),
                bean().string("a").build(),
                bean().string("b").build()))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingInSet() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"string\": \"a\",\n" +
                "      \"integer\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"string\": \"b\",\n" +
                "      \"integer\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"string\": \"c\",\n" +
                "      \"integer\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().set(newLinkedHashSet(
                bean().string("c").build(),
                bean().string("b").build(),
                bean().string("a").build()))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingForSetsImplementations() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"hashSet\": [\n" +
                "    {\n" +
                "      \"string\": \"a\",\n" +
                "      \"integer\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"string\": \"b\",\n" +
                "      \"integer\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"string\": \"c\",\n" +
                "      \"integer\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().hashSet(newLinkedHashSet(
                bean().string("c").build(),
                bean().string("b").build(),
                bean().string("a").build()))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingInNotLinkedNestedSet() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"integer\": 0,\n" +
                "      \"set\": [\n" +
                "        {\n" +
                "          \"string\": \"a\",\n" +
                "          \"integer\": 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"string\": \"b\",\n" +
                "          \"integer\": 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"string\": \"c\",\n" +
                "          \"integer\": 0\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().set(newHashSet(
                bean().set(newHashSet(
                        bean().string("c").build(),
                        bean().string("b").build(),
                        bean().string("a").build())).build(),
                bean().set(newHashSet(
                        bean().string("a").build(),
                        bean().string("c").build(),
                        bean().string("b").build())).build(),
                bean().set(newHashSet(
                        bean().string("a").build(),
                        bean().string("b").build(),
                        bean().string("c").build())).build()))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingInNestedSet() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"integer\": 0,\n" +
                "      \"set\": [\n" +
                "        {\n" +
                "          \"string\": \"a\",\n" +
                "          \"integer\": 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"string\": \"b\",\n" +
                "          \"integer\": 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"string\": \"c\",\n" +
                "          \"integer\": 0\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().set(newLinkedHashSet(
                bean().set(newLinkedHashSet(
                        bean().string("c").build(),
                        bean().string("b").build(),
                        bean().string("a").build())).build(),
                bean().set(newLinkedHashSet(
                        bean().string("a").build(),
                        bean().string("c").build(),
                        bean().string("b").build())).build(),
                bean().set(newLinkedHashSet(
                        bean().string("a").build(),
                        bean().string("b").build(),
                        bean().string("c").build())).build()))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingInNotLinkedMap() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key1\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value1\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key2\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value2\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key3\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value3\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ]\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().map(newHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingInMap() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key1\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value1\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key2\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value2\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key3\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value3\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ]\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().map(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingForMapImplementations() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"hashMap\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key1\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value1\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key2\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value2\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key3\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value3\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ]\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().hashMap(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingInMapWithTwoEntriesWithSameJsonRepresentationAsKeyButDifferentValues() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"hashMap\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key1\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value1\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key1\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value2\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key3\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value3\",\n" +
                "        \"integer\": 0\n" +
                "      }\n" +
                "    ]\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().hashMap(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key1").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingInNotLinkedNestedMap() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key1\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value1\",\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key1\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value1\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key2\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value2\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key3\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value3\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key2\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value2\",\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key1\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value1\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key2\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value2\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key3\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value3\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key3\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value3\",\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key1\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value1\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key2\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value2\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key3\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value3\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().map(newHashMap(
                bean().string("key3").build(), bean().string("value3").map(newHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build()
                )).build(),

                bean().string("key2").build(), bean().string("value2").map(newHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key1").build(), bean().string("value1").build()
                )).build(),
                bean().string("key1").build(), bean().string("value1").map(newHashMap(
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key3").build(), bean().string("value3").build()
                )).build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @Test
    public void ignoresOrderingInNestedMap() {
        String expected = "{\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key1\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value1\",\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key1\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value1\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key2\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value2\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key3\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value3\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key2\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value2\",\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key1\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value1\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key2\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value2\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key3\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value3\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"string\": \"key3\",\n" +
                "        \"integer\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"string\": \"value3\",\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key1\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value1\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key2\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value2\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"string\": \"key3\",\n" +
                "              \"integer\": 0\n" +
                "            },\n" +
                "            {\n" +
                "              \"string\": \"value3\",\n" +
                "              \"integer\": 0\n" +
                "            }\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  ]\n" +
                "}";

        Bean actual = bean().map(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").map(newLinkedHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build()
                )).build(),

                bean().string("key2").build(), bean().string("value2").map(newLinkedHashMap(
                        bean().string("key3").build(), bean().string("value3").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key1").build(), bean().string("value1").build()
                )).build(),
                bean().string("key1").build(), bean().string("value1").map(newLinkedHashMap(
                        bean().string("key1").build(), bean().string("value1").build(),
                        bean().string("key2").build(), bean().string("value2").build(),
                        bean().string("key3").build(), bean().string("value3").build()
                )).build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, identity(), null);
    }

    @SuppressWarnings({"varargs", "unchecked"})
    private <T> HashSet<T> newLinkedHashSet(T... input) {
        return Arrays.stream(input).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private <K, T> HashMap<K, T> newLinkedHashMap(K key1, T value1, K key2, T value2, K key3, T value3) {
        HashMap<K, T> result = new LinkedHashMap<>();
        result.put(key1, value1);
        result.put(key2, value2);
        result.put(key3, value3);
        return result;
    }

    private <K, T> HashMap<K, T> newHashMap(K key1, T value1, K key2, T value2, K key3, T value3) {
        HashMap<K, T> result = new HashMap<>();
        result.put(key1, value1);
        result.put(key2, value2);
        result.put(key3, value3);
        return result;
    }
}
