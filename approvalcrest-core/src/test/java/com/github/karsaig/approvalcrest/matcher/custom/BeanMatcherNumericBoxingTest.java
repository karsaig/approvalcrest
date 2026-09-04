package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;

/**
 * An ordering matcher written with the wrong kind of number must report a mismatch, not crash.
 *
 * <p>Hamcrest's ordering matchers resolve their type parameter to {@code Object}, so nothing is rejected on the
 * type check and the cast inside {@code compareTo} is what fails. A bare ordering matcher catches that itself and
 * answers false, but its {@code describeMismatch} does not — so an assertion that had already correctly decided
 * "no match" died while building the message. Several combinators are worse: {@code allOf}, {@code both().and()},
 * {@code hasItem} and {@code everyItem} call the inner {@code describeMismatch} from inside their own
 * {@code matches}, so the exception escaped before any verdict was reached.
 *
 * <p>The verdict is deliberately unchanged: requiring the matcher's number to be written in the same form as the
 * value's is the documented contract, and approved files depend on it. Only the crash is fixed.
 */
public class BeanMatcherNumericBoxingTest extends AbstractBeanMatcherTest {

    static class IntHolder {
        int value;

        IntHolder(int value) {
            this.value = value;
        }
    }

    static class LongHolder {
        long value;

        LongHolder(long value) {
            this.value = value;
        }
    }

    static class ScoreHolder {
        List<Long> scores = new ArrayList<>(Arrays.asList(3L, 1L, 2L));
    }

    private static void assertExplainsTheBoxing(AssertionError err) {
        String text = err.getMessage();
        Assertions.assertTrue(text.contains("value"), text);
        Assertions.assertTrue(text.contains("which this matcher cannot compare"), text);
        Assertions.assertTrue(text.contains("java.lang.Long"), text);
        Assertions.assertTrue(text.contains("cannot be cast"), text);
        Assertions.assertTrue(text.contains("docs/custom-matching.md"), text);
        // the scratch buffer must not leave a stranded fragment from the matcher's own aborted description
        Assertions.assertFalse(text.contains("was was"), text);
    }

    // --- a bare ordering matcher: the crash was in describeMismatch ---

    @Test
    public void aLongFieldAgainstAnIntMatcherExplainsTheBoxingInsteadOfThrowing() {
        assertDiagnosingMatcher(new LongHolder(7L), new LongHolder(7L),
                m -> m.alsoCheck("value", greaterThan(100)),
                AssertionError.class,
                BeanMatcherNumericBoxingTest::assertExplainsTheBoxing);
    }

    @Test
    public void aLongFieldAgainstASatisfiableIntMatcherAlsoExplainsTheBoxing() {
        assertDiagnosingMatcher(new LongHolder(7L), new LongHolder(7L),
                m -> m.alsoCheck("value", greaterThan(0)),
                AssertionError.class,
                BeanMatcherNumericBoxingTest::assertExplainsTheBoxing);
    }

    @Test
    public void theReplacingModeTakesTheSameRoute() {
        assertDiagnosingMatcher(new LongHolder(7L), new LongHolder(9L),
                m -> m.with("value", lessThan(100)),
                AssertionError.class,
                err -> Assertions.assertTrue(
                        err.getMessage().contains("which this matcher cannot compare"), err.getMessage()));
    }

    @Test
    public void thePatternFormTakesTheSameRoute() {
        // the pattern form reads the serialised tree only, so a whole number is always a Long there
        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7),
                m -> m.alsoCheckMatching(equalTo("value"), greaterThan(0)),
                AssertionError.class,
                err -> Assertions.assertTrue(
                        err.getMessage().contains("which this matcher cannot compare"), err.getMessage()));
    }

    // --- combinators: the crash was in matches, before any verdict ---

    @Test
    public void allOfOverAnOrderingMatcherReportsAMismatchRatherThanThrowing() {
        assertDiagnosingMatcher(new LongHolder(7L), new LongHolder(7L),
                m -> m.alsoCheck("value", allOf(greaterThan(0), lessThan(100))),
                AssertionError.class,
                err -> Assertions.assertTrue(err.getMessage().contains("value"), err.getMessage()));
    }

    @Test
    public void bothAndOverAnOrderingMatcherReportsAMismatchRatherThanThrowing() {
        assertDiagnosingMatcher(new LongHolder(7L), new LongHolder(7L),
                m -> m.alsoCheck("value", both(greaterThan(0)).and(lessThan(100))),
                AssertionError.class,
                err -> Assertions.assertTrue(err.getMessage().contains("value"), err.getMessage()));
    }

    @Test
    public void everyItemOverAnOrderingMatcherReportsAMismatchRatherThanThrowing() {
        assertDiagnosingMatcher(new ScoreHolder(), new ScoreHolder(),
                m -> m.alsoCheck("scores", everyItem(greaterThan(0))),
                AssertionError.class,
                err -> Assertions.assertTrue(err.getMessage().contains("scores"), err.getMessage()));
    }

    @Test
    public void hasItemOverAnOrderingMatcherReportsAMismatchRatherThanThrowing() {
        assertDiagnosingMatcher(new ScoreHolder(), new ScoreHolder(),
                m -> m.alsoCheck("scores", hasItem(greaterThan(0))),
                AssertionError.class,
                err -> Assertions.assertTrue(err.getMessage().contains("scores"), err.getMessage()));
    }

    // --- the matching boxing is unaffected ---

    @Test
    public void anIntFieldAgainstAnIntMatcherStillMatches() {
        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7), m -> m.alsoCheck("value", greaterThan(0)));
    }

    @Test
    public void aLongFieldAgainstALongMatcherStillMatches() {
        assertDiagnosingMatcher(new LongHolder(7L), new LongHolder(7L),
                m -> m.alsoCheck("value", greaterThan(0L)));
    }

    @Test
    public void allOfOnTheMatchingBoxingStillMatches() {
        assertDiagnosingMatcher(new LongHolder(7L), new LongHolder(7L),
                m -> m.alsoCheck("value", allOf(greaterThan(0L), lessThan(100L))));
    }

    @Test
    public void everyItemOnTheMatchingBoxingStillMatches() {
        assertDiagnosingMatcher(new ScoreHolder(), new ScoreHolder(),
                m -> m.alsoCheck("scores", everyItem(greaterThan(0L))));
    }

    /**
     * An {@code int} field resolves to an Integer on the bean walk and to a Long on the JSON retry, so a
     * Long-boxed matcher is answered by the second attempt. The crash needed the value to be a Long on both.
     */
    @Test
    public void anIntFieldAgainstALongMatcherIsRescuedByTheJsonRetry() {
        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7), m -> m.alsoCheck("value", greaterThan(0L)));
    }

    // --- a correctly written matcher that simply does not hold must say so ---

    /**
     * The bean walk cannot describe an Integer against a Long-boxed matcher, so the value that settled the
     * verdict -- the Long from the JSON retry -- has to be the one reported. Keeping the bean value would
     * replace the real reason with a cast complaint about a boxing the caller never wrote.
     */
    @Test
    public void aGenuineMismatchIsNotReportedAsABoxingProblem() {
        assertDiagnosingMatcher(new IntHolder(-5), new IntHolder(-5),
                m -> m.alsoCheck("value", greaterThan(0L)),
                AssertionError.class,
                err -> {
                    String text = err.getMessage();
                    Assertions.assertTrue(text.contains("value <-5L> was less than <0L>"), text);
                    Assertions.assertFalse(text.contains("cannot compare"), text);
                });
    }

    /** An ordinary mismatch on the right boxing keeps Hamcrest's own wording. */
    @Test
    public void anOrdinaryMismatchIsUnchanged() {
        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7),
                m -> m.alsoCheck("value", greaterThan(100)),
                AssertionError.class,
                err -> {
                    String text = err.getMessage();
                    Assertions.assertTrue(text.contains("value <7> was less than <100>"), text);
                    Assertions.assertFalse(text.contains("cannot compare"), text);
                });
    }

    // --- equalTo never threw and is untouched ---

    @Test
    public void equalToIsUnchangedAcrossBoxings() {
        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7), m -> m.alsoCheck("value", equalTo(7L)));
    }

    @Test
    public void equalToStillFailsOnAGenuinelyDifferentValue() {
        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7),
                m -> m.alsoCheck("value", equalTo(9L)),
                AssertionError.class,
                err -> {
                    String text = err.getMessage();
                    Assertions.assertTrue(text.contains("value was <7>"), text);
                    Assertions.assertFalse(text.contains("cannot compare"), text);
                });
    }
}
