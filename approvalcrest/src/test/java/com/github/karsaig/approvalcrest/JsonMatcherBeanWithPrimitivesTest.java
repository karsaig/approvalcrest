package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

/**
 * Smoke tests for {@link com.github.karsaig.approvalcrest.matcher.Matchers#sameJsonAsApproved()}.
 * Logic variants are covered in approvalcrest-core.
 */
//5f9b80
public class JsonMatcherBeanWithPrimitivesTest extends AbstractJsonMatcherTest {

    private BeanWithPrimitives actual;

    @Before
    public void setUp() {
        actual = getBeanWithPrimitives();
    }

    //78b1d8
    @Test
    public void shouldNotThrowAssertionErrorWhenModelIsSameAsApprovedJson() {
        assertThat(actual, sameJsonAsApproved());
    }

    //964370
    @Test
    public void shouldThrowAssertionErrorWhenModelDiffersFromApprovedJson() {
        ComparisonFailure ex = Assert.assertThrows(ComparisonFailure.class, () -> assertThat(actual, sameJsonAsApproved()));
        assertThat(ex.getMessage(), Matchers.stringContainsInOrder("Expected file 5f9b80/964370-approved.json", "Expected: 3.1", "got: 3.0"));
    }
}
