package com.github.karsaig.approvalcrest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonPrimitive;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * A rule is applied deep inside the matcher, long after it is constructed, so a null passed to a
 * factory would otherwise surface as an NPE at comparison time naming neither the rule nor its path.
 */
public class ElementIgnoreRuleTest {

    @Test
    public void ofValueRejectsNullAtTheCallSite() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> ElementIgnoreRule.ofValue("entry.resource.meta.tag.system", null));

        assertTrue(e.getMessage().contains("entry.resource.meta.tag.system"),
                "the error must name the path, was: " + e.getMessage());
    }

    @Test
    public void ofRejectsANullMatcherAtTheCallSite() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> ElementIgnoreRule.of("entry.resource.meta.tag.system", null));

        assertTrue(e.getMessage().contains("entry.resource.meta.tag.system"),
                "the error must name the path, was: " + e.getMessage());
    }

    @Test
    public void valueRuleStillMatchesAndRejectsAsBefore() {
        ElementIgnoreRule rule = ElementIgnoreRule.ofValue("a.b", "tracking");

        assertEquals("a.b", rule.getPath());
        assertTrue(rule.matches(new JsonPrimitive("tracking")));
        assertFalse(rule.matches(new JsonPrimitive("other")));
        assertFalse(rule.matches(null), "a null leaf is simply not a match");
    }

    @Test
    public void matcherRuleStillMatchesAsBefore() {
        ElementIgnoreRule rule = ElementIgnoreRule.of("a.b", Matchers.is("tracking"));

        assertTrue(rule.matches(new JsonPrimitive("tracking")));
        assertFalse(rule.matches(new JsonPrimitive("other")));
    }
}
