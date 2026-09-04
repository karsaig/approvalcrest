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
import static org.hamcrest.Matchers.not;

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
        Assertions.assertTrue(text.contains("could not compare it"), text);
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
                        err.getMessage().contains("could not compare it"), err.getMessage()));
    }

    @Test
    public void thePatternFormTakesTheSameRoute() {
        // the pattern form reads the serialised tree only, so a whole number is always a Long there
        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7),
                m -> m.alsoCheckMatching(equalTo("value"), greaterThan(0)),
                AssertionError.class,
                err -> Assertions.assertTrue(
                        err.getMessage().contains("could not compare it"), err.getMessage()));
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

    /**
     * The one shape whose verdict changes rather than only its message: a combinator over an {@code int} field
     * with a Long-boxed bound. The bean walk's cast failure used to escape before any verdict existed; now it
     * is an ordinary "no match" there, so the JSON retry runs and settles it -- and the JSON value is a Long,
     * which the matcher does accept.
     */
    @Test
    public void aCombinatorOverAnIntFieldIsNowSettledByTheJsonRetry() {
        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7),
                m -> m.alsoCheck("value", allOf(greaterThan(0L), lessThan(100L))));
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

    // --- the combination that cannot fail, pinned so it cannot change unnoticed ---

    /**
     * Hamcrest answers false for a mismatched boxing rather than raising, so negating it yields a pass whatever
     * the data holds. Nothing outside Hamcrest can see the difference between "false because the value did not
     * satisfy it" and "false because it could not compare" -- the inner catch has already discarded that. This
     * pins the documented limitation; see custom-matching.md.
     */
    @Test
    public void aNegatedOrderingMatcherOnTheWrongBoxingCannotFail() {
        assertDiagnosingMatcher(new LongHolder(42L), new LongHolder(42L),
                m -> m.alsoCheck("value", not(greaterThan(0))));
        // the same configuration passes for a value that plainly does not satisfy it either
        assertDiagnosingMatcher(new LongHolder(-42L), new LongHolder(-42L),
                m -> m.alsoCheck("value", not(greaterThan(0))));
    }

    /** Written with the value's own boxing, the negation discriminates again. */
    @Test
    public void aNegatedOrderingMatcherOnTheRightBoxingStillFails() {
        assertDiagnosingMatcher(new LongHolder(42L), new LongHolder(42L),
                m -> m.alsoCheck("value", not(greaterThan(0L))),
                AssertionError.class,
                err -> Assertions.assertTrue(err.getMessage().contains("value"), err.getMessage()));
    }

    // --- a caller's own mistyped matcher must still say so ---

    /**
     * The cast is swallowed so the JSON retry can run, which would otherwise lose the only evidence that a
     * caller's matcher is mistyped. Before the guard existed this threw and named the bug; it must not now read
     * as an ordinary value mismatch.
     */
    @Test
    public void aMistypedCallerMatcherStillNamesTheCastItFailed() {
        org.hamcrest.Matcher<Object> mistyped = new org.hamcrest.BaseMatcher<Object>() {
            @Override
            public boolean matches(Object item) {
                return ((String) item).length() > 0;
            }

            @Override
            public void describeMismatch(Object item, org.hamcrest.Description description) {
                description.appendText("was ").appendValue(item);
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("a non-empty string");
            }
        };

        assertDiagnosingMatcher(new IntHolder(7), new IntHolder(7),
                m -> m.alsoCheck("value", mistyped),
                AssertionError.class,
                err -> {
                    String text = err.getMessage();
                    Assertions.assertTrue(text.contains("could not compare it"), text);
                    Assertions.assertTrue(text.contains("cannot be cast"), text);
                    Assertions.assertTrue(text.contains("java.lang.String"), text);
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
