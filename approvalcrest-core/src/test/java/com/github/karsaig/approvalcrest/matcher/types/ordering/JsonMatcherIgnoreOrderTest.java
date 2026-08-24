package com.github.karsaig.approvalcrest.matcher.types.ordering;

import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.Bean;


public class JsonMatcherIgnoreOrderTest extends AbstractFileMatcherTest {


    @Test
    public void ignoresOrderingInNotLinkedSet() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": null,\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"a\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"b\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"c\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"string\": null\n" +
                "}";

        Bean actual = bean().set(newHashSet(
                bean().string("c").build(),
                bean().string("a").build(),
                bean().string("b").build()))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingInSet() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": null,\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"a\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"b\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"c\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"string\": null\n" +
                "}";

        Bean actual = bean().set(newLinkedHashSet(
                bean().string("c").build(),
                bean().string("b").build(),
                bean().string("a").build()))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingForSetsImplementations() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": [\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"a\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"b\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": null,\n" +
                "      \"string\": \"c\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": null,\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
                "}";

        Bean actual = bean().hashSet(newLinkedHashSet(
                bean().string("c").build(),
                bean().string("b").build(),
                bean().string("a").build()))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingInNotLinkedNestedSet() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": null,\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": [\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"a\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"b\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"c\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"string\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": [\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"a\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"b\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"c\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"string\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": [\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"a\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"b\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"c\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"string\": null\n" +
                "    }\n" +
                "  ],\n" +
                "  \"string\": null\n" +
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

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingInNestedSet() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": null,\n" +
                "  \"set\": [\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": [\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"a\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"b\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"c\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"string\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": [\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"a\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"b\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"c\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"string\": null\n" +
                "    },\n" +
                "    {\n" +
                "      \"array\": null,\n" +
                "      \"hashMap\": null,\n" +
                "      \"hashSet\": null,\n" +
                "      \"integer\": 0,\n" +
                "      \"map\": null,\n" +
                "      \"set\": [\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"a\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"b\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"array\": null,\n" +
                "          \"hashMap\": null,\n" +
                "          \"hashSet\": null,\n" +
                "          \"integer\": 0,\n" +
                "          \"map\": null,\n" +
                "          \"set\": null,\n" +
                "          \"string\": \"c\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"string\": null\n" +
                "    }\n" +
                "  ],\n" +
                "  \"string\": null\n" +
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

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingInNotLinkedMap() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value1\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key2\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value2\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key3\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value3\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
                "}";

        Bean actual = bean().map(newHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingInMap() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value1\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key2\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value2\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key3\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value3\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
                "}";

        Bean actual = bean().map(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingForMapImplementations() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value1\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key2\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value2\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key3\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value3\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": null,\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
                "}";

        Bean actual = bean().hashMap(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key2").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingInMapWithTwoEntriesWithSameJsonRepresentationAsKeyButDifferentValues() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value1\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value2\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key3\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value3\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": null,\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
                "}";

        Bean actual = bean().hashMap(newLinkedHashMap(
                bean().string("key3").build(), bean().string("value3").build(),
                bean().string("key1").build(), bean().string("value2").build(),
                bean().string("key1").build(), bean().string("value1").build()
        ))
                .build();

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingInNotLinkedNestedMap() {
        // Each entry renders value-first. A complex-key map entry serialises as a [key, value]
        // array, and sortJsonArray recurses into that array like any other, so whichever side's
        // JSON sorts first comes first. The comparison runs on the marker-prefixed compact form
        // with members already key-sorted, so "map" precedes "string": every value here carries a
        // nested map, and "map": [ sorts ahead of the key's "map": null.
        //
        // This is a defect, not a presentation quirk, and it predates this class being collected.
        // Sorting the pair destroys the key/value distinction, so a map {a: z} and its transpose
        // {z: a} serialise byte-identically and an approved file written for one is matched by the
        // other. The file side is never re-sorted, so nothing cancels it out. Not fixed here --
        // fixing it rewrites every approved file holding a complex-key map -- but do not read these
        // expectations as endorsing it.
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key1\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value1\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key2\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value2\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key3\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value3\"\n" +
                "            }\n" +
                "          ]\n" +
                "        ],\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key1\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key1\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value1\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key2\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value2\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key3\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value3\"\n" +
                "            }\n" +
                "          ]\n" +
                "        ],\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value2\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key2\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key1\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value1\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key2\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value2\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key3\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value3\"\n" +
                "            }\n" +
                "          ]\n" +
                "        ],\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value3\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key3\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
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

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
    }

    @Test
    public void ignoresOrderingInNestedMap() {
        // Each entry renders value-first. A complex-key map entry serialises as a [key, value]
        // array, and sortJsonArray recurses into that array like any other, so whichever side's
        // JSON sorts first comes first. The comparison runs on the marker-prefixed compact form
        // with members already key-sorted, so "map" precedes "string": every value here carries a
        // nested map, and "map": [ sorts ahead of the key's "map": null.
        //
        // This is a defect, not a presentation quirk, and it predates this class being collected.
        // Sorting the pair destroys the key/value distinction, so a map {a: z} and its transpose
        // {z: a} serialise byte-identically and an approved file written for one is matched by the
        // other. The file side is never re-sorted, so nothing cancels it out. Not fixed here --
        // fixing it rewrites every approved file holding a complex-key map -- but do not read these
        // expectations as endorsing it.
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key1\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value1\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key2\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value2\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key3\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value3\"\n" +
                "            }\n" +
                "          ]\n" +
                "        ],\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key1\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key1\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value1\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key2\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value2\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key3\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value3\"\n" +
                "            }\n" +
                "          ]\n" +
                "        ],\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value2\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key2\"\n" +
                "      }\n" +
                "    ],\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key1\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value1\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key2\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value2\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"key3\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"array\": null,\n" +
                "              \"hashMap\": null,\n" +
                "              \"hashSet\": null,\n" +
                "              \"integer\": 0,\n" +
                "              \"map\": null,\n" +
                "              \"set\": null,\n" +
                "              \"string\": \"value3\"\n" +
                "            }\n" +
                "          ]\n" +
                "        ],\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"value3\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"key3\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
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

        assertJsonMatcherWithDummyTestInfo(actual, expected, Function.identity(), null);
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

    // -----------------------------------------------------------------------
    // Known limitation: a complex-key map and its transpose are indistinguishable
    //
    // A map with a bean key serialises each entry as a [key, value] array, and sortJsonArray recurses
    // into that array like any other, so the pair is reordered by JSON. The key/value distinction is
    // lost with it: Map{a -> z} and Map{z -> a} produce the same bytes, and the approved file written
    // for one is matched by the other. The file side is never re-sorted, so nothing cancels it out.
    //
    // Pre-existing and not fixed here: stopping the sort at a pair array changes the written form of
    // every approved file holding such a map. These two tests are the pin -- when that is fixed, they
    // are what should change, deliberately.
    // -----------------------------------------------------------------------

    private static Bean singleEntryMap(String key, String value) {
        HashMap<Bean, Bean> m = new LinkedHashMap<>();
        m.put(bean().string(key).build(), bean().string(value).build());
        return bean().map(m).build();
    }

    @Test
    public void complexKeyMapRendersThePairInSortedOrder() {
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"a\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"z\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(singleEntryMap("a", "z"), expected, Function.identity(), null);
    }

    @Test
    public void transposedComplexKeyMapRendersIdentically() {
        // Same bytes as above, from a map whose key and value are swapped.
        String expected = "{\n" +
                "  \"array\": null,\n" +
                "  \"hashMap\": null,\n" +
                "  \"hashSet\": null,\n" +
                "  \"integer\": 0,\n" +
                "  \"map\": [\n" +
                "    [\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"a\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"z\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(singleEntryMap("z", "a"), expected, Function.identity(), null);
    }
}
