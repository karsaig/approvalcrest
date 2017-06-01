package com.github.karsaig.json.ignore;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

public class MatcherBasedFieldIgnoringModuleTest {
    @Test
    public void shouldNotSerializePropertiesWhenNameIsMatching() throws JsonProcessingException {
        // GIVEN
        ObjectMapper mapper = new ObjectMapper().registerModule(new MatcherBasedFieldIgnoringModule(ImmutableList.of(endsWith("Bar"))));

        // WHEN
        String json = mapper.writeValueAsString(new A(new Foo("foo"), new Bar("bar"), "test", "testBar"));

        // THEN
        assertThat(json, is("{\"testFoo\":{\"foo\":\"foo\"},\"test\":\"test\"}"));
    }

    private static class A {
        private final Foo testFoo;
        private final Bar fooBar;
        private final String test;
        private final String testBar;

        private A(Foo testFoo, Bar fooBar, String test, String testBar) {
            this.testFoo = testFoo;
            this.fooBar = fooBar;
            this.test = test;
            this.testBar = testBar;
        }

        public Foo getTestFoo() {
            return testFoo;
        }

        public Bar getFooBar() {
            return fooBar;
        }

        public String getTest() {
            return test;
        }

        public String getTestBar() {
            return testBar;
        }
    }

    private static class Foo {
        private final String foo;

        private Foo(String foo) {
            this.foo = foo;
        }

        public String getFoo() {
            return foo;
        }
    }

    private static class Bar {
        private final String bar;

        private Bar(String bar) {
            this.bar = bar;
        }

        public String getBar() {
            return bar;
        }
    }
}