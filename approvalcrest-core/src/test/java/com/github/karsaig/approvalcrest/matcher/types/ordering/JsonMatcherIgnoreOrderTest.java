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
        // Each entry renders key-first, since a complex-key map entry keeps its [key, value] order.
        // Entries themselves are still ordered by content, which is what this test is about: the map's
        // own ordering is unaffected by the pair rule.
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
        // Each entry renders key-first, since a complex-key map entry keeps its [key, value] order.
        // Entries themselves are still ordered by content, which is what this test is about: the map's
        // own ordering is unaffected by the pair rule.
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
    // A complex-key map keeps its key before its value
    //
    // A map with a bean key serialises each entry as a [key, value] array. sortJsonArray used to recurse
    // into that array like any other and reorder it by JSON, which lost the key/value distinction:
    // Map{a -> z} and Map{z -> a} produced the same bytes, so an approved file written for one matched
    // the other. The pair is now left in place while both halves are still descended into.
    //
    // These two tests are the pin from opposite directions: the first would pass even before the fix,
    // because "a" < "z" put the key first by luck; the second is the one that could not.
    // -----------------------------------------------------------------------

    private static Bean singleEntryMap(String key, String value) {
        HashMap<Bean, Bean> m = new LinkedHashMap<>();
        m.put(bean().string(key).build(), bean().string(value).build());
        return bean().map(m).build();
    }

    @Test
    public void complexKeyMapKeepsTheKeyBeforeTheValue() {
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
    public void transposedComplexKeyMapRendersTheOtherWayRound() {
        // The key is "z" and the value is "a", so this renders [z, a] -- the reverse of the test above
        // and the whole point. Before the fix both produced [a, z], so an approved file for one matched
        // the other.
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
                "        \"string\": \"z\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"array\": null,\n" +
                "        \"hashMap\": null,\n" +
                "        \"hashSet\": null,\n" +
                "        \"integer\": 0,\n" +
                "        \"map\": null,\n" +
                "        \"set\": null,\n" +
                "        \"string\": \"a\"\n" +
                "      }\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"set\": null,\n" +
                "  \"string\": null\n" +
                "}";

        assertJsonMatcherWithDummyTestInfo(singleEntryMap("z", "a"), expected, Function.identity(), null);
    }
}
