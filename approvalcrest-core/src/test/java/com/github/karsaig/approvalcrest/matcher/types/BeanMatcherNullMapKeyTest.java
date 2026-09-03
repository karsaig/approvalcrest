package com.github.karsaig.approvalcrest.matcher.types;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;

/** The {@code sameBeanAs} side of a null map key, including what the chosen rendering costs. */
public class BeanMatcherNullMapKeyTest extends AbstractBeanMatcherTest {

    static class Holder {
        Map<Object, Object> m;

        Holder(Object key, Object value) {
            this.m = new LinkedHashMap<>();
            this.m.put(key, value);
        }
    }

    @Test
    public void twoMapsWithANullKeyMatch() {
        assertDiagnosingMatcher(new Holder(null, "a"), new Holder(null, "a"));
    }

    @Test
    public void aMapWithANullKeyDoesNotMatchOneWithAnOrdinaryKey() {
        assertDiagnosingMatcher(new Holder(null, "a"), new Holder("k", "a"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(error.getMessage().contains("m["),
                        "Expected a mismatch inside the map, was: " + error.getMessage()));
    }

    @Test
    public void aNullKeyMatchesTheStringNull() {
        // The cost of rendering a null key as the member name "null": it is indistinguishable from a String
        // key spelling the same thing, so this assertion passes on data that differs. Pinned deliberately —
        // the alternative rendering avoids it but breaks path navigation for every other key in the map.
        assertDiagnosingMatcher(new Holder(null, "a"), new Holder("null", "a"));
    }
}
