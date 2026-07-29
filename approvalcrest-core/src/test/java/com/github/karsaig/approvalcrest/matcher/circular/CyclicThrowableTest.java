package com.github.karsaig.approvalcrest.matcher.circular;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;

import org.junit.jupiter.api.Test;

/**
 * Cycles between throwables must terminate, the way cycles between ordinary beans already do in
 * {@link BeanMatcherCircularReferenceTest}.
 *
 * <p>Throwables take a different path: {@code ThrowableTypeAdapterFactory} post-processes the
 * serialised graph to name the concrete type of each nested throwable, and that walk follows
 * {@code 0x} references across the graph rather than descending the JSON tree, so a ring leads back
 * to a reference it has already seen.
 */
public class CyclicThrowableTest extends AbstractBeanMatcherTest {

    public static class Looping extends RuntimeException {
        private static final long serialVersionUID = 1L;
        Looping other;

        Looping(String message) {
            super(message);
        }
    }

    private static Looping selfReferencing() {
        Looping node = new Looping("self");
        node.other = node;
        return node;
    }

    private static Looping ring(int size) {
        Looping[] nodes = new Looping[size];
        for (int i = 0; i < size; i++) {
            nodes[i] = new Looping("node " + i);
        }
        for (int i = 0; i < size; i++) {
            nodes[i].other = nodes[(i + 1) % size];
        }
        return nodes[0];
    }

    @Test
    public void handlesSelfReferencingThrowable() {
        assertDiagnosingMatcher(selfReferencing(), selfReferencing());
    }

    @Test
    public void handlesTwoNodeThrowableRing() {
        assertDiagnosingMatcher(ring(2), ring(2));
    }

    /**
     * Fourteen nodes so the ring closes through a reference id containing a hex letter, which is
     * only followed at all since the reference grammar was corrected.
     */
    @Test
    public void handlesThrowableRingLargeEnoughToUseHexReferenceIds() {
        assertDiagnosingMatcher(ring(14), ring(14));
    }

    /** A difference inside a cyclic throwable graph must still be reported rather than swallowed. */
    @Test
    public void reportsDifferenceInsideACyclicThrowableGraph() {
        Looping actual = ring(3);
        Looping expected = ring(3);
        expected.other.other = new Looping("different");
        expected.other.other.other = expected;

        assertDiagnosingMatcher(actual, expected, Function.identity(), AssertionError.class,
                error -> assertTrue(error.getMessage().contains("different"),
                        "the difference must be reported, message was: " + error.getMessage()));
    }
}
