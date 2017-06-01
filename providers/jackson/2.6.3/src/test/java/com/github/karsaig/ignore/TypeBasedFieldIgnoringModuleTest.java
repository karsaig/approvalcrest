package com.github.karsaig.ignore;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;

import com.github.karsaig.json.ignore.TypeBasedFieldIgnoringModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TypeBasedFieldIgnoringModuleTest {

    @Test
    public void shouldNotSerializeFieldWhenItsTypeMarkedToBeIgnored() throws JsonProcessingException {
        // GIVEN
        ObjectMapper mapper = new ObjectMapper().registerModule(new TypeBasedFieldIgnoringModule(B.class));

        // WHEN
        String json = mapper.writeValueAsString(new A(new B("Lorem", "Impsum"), "baar"));

        // THEN
        assertThat(json, is("{\"fooo\":\"baar\"}"));
    }

    @Test
    public void shouldNotSerializeFieldsWhenTypesMarkedToBeIgnored() throws JsonProcessingException {
        // GIVEN
        ObjectMapper mapper = new ObjectMapper().registerModule(new TypeBasedFieldIgnoringModule(B.class, String.class));

        // WHEN
        String json = mapper.writeValueAsString(new A(new B("Lorem", "Impsum"), "baar"));

        // THEN
        assertThat(json, is("{}"));
    }

    private class A {
        private B foo;
        private String fooo;

        public A(B foo, String fooo) {
            this.foo = foo;
            this.fooo = fooo;
        }

        public B getFoo() {
            return foo;
        }

        public String getFooo() {
            return fooo;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("foo", foo).append("fooo", fooo).toString();
        }
    }

    private class B {
        private String bar;
        private String baar;

        private B(String bar, String baar) {
            this.bar = bar;
            this.baar = baar;
        }

        public String getBar() {
            return bar;
        }

        public String getBaar() {
            return baar;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("bar", bar).append("baar", baar).toString();
        }
    }
}
