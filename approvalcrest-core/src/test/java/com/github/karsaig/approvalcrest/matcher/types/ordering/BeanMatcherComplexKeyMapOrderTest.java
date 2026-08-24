package com.github.karsaig.approvalcrest.matcher.types.ordering;

import static com.github.karsaig.approvalcrest.testdata.ChildBean.Builder.child;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.ChildBean;

/**
 * The {@code sameBeanAs} side of the complex-key map pair order. It serialises both sides itself, through
 * the same Gson, so both carry the Map marker and suppression is symmetric — there is no approved file and
 * no strict-matching setting to consult.
 *
 * <p>Also covers a map at the ROOT, which has no field name to mark and is instead recognised from the
 * object's own type in {@code applyRootCollectionSorting}.
 */
public class BeanMatcherComplexKeyMapOrderTest extends AbstractBeanMatcherTest {

    static class MapHolder {
        Map<ChildBean, ChildBean> m = new LinkedHashMap<>();

        MapHolder(String key, String value) {
            m.put(child().childString(key).build(), child().childString(value).build());
        }
    }

    private static Map<ChildBean, ChildBean> rootMap(String key, String value) {
        Map<ChildBean, ChildBean> m = new LinkedHashMap<>();
        m.put(child().childString(key).build(), child().childString(value).build());
        return m;
    }

    @Test
    public void equalMapsStillMatch() {
        assertDiagnosingMatcher(new MapHolder("zk", "av"), new MapHolder("zk", "av"));
    }

    @Test
    public void aMapNoLongerMatchesItsTranspose() {
        // Both sides are serialised here, so before the fix both were normalised to the same bytes and
        // this passed -- a map and its transpose were indistinguishable.
        assertDiagnosingMatcher(new MapHolder("zk", "av"), new MapHolder("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("[0][0].childString")
                                && error.getMessage().contains("[0][1].childString"),
                        "Expected mismatches at both pair positions, was: " + error.getMessage()));
    }

    @Test
    public void equalRootMapsStillMatch() {
        assertDiagnosingMatcher(rootMap("zk", "av"), rootMap("zk", "av"));
    }

    @Test
    public void aRootMapNoLongerMatchesItsTranspose() {
        // A root map is recognised from the object's type, since there is no field name to carry a marker.
        assertDiagnosingMatcher(rootMap("zk", "av"), rootMap("av", "zk"),
                beanMatcher -> beanMatcher, AssertionError.class,
                error -> Assertions.assertTrue(
                        error.getMessage().contains("[0][0].childString")
                                && error.getMessage().contains("[0][1].childString"),
                        "Expected mismatches at both pair positions, was: " + error.getMessage()));
    }
}
