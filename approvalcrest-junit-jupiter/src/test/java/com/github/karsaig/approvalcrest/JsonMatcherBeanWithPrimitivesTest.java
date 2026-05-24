package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;

/**
 * Smoke tests for {@link com.github.karsaig.approvalcrest.matcher.Matchers#sameJsonAsApproved()}.
 * Logic variants are covered in approvalcrest-core.
 */
//5f9b80
public class JsonMatcherBeanWithPrimitivesTest extends AbstractJsonMatcherTest {

    private BeanWithPrimitives actual;

    @BeforeEach
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
        AssertionFailedError ae = assertThrows(AssertionFailedError.class, () -> assertThat(actual, sameJsonAsApproved()));
        assertThat(ae.getMessage(), Matchers.stringContainsInOrder("Expected file 5f9b80/964370-approved.json", "Expected: 3.1", "got: 3.0"));
    }

    @ParameterizedTest
    @ValueSource(strings = "@ParameterizedTest annotation present")
    public void shouldNotThrowAssertionErrorWhenAnnotationIsTestTemplate(String input) {
        assertThat(actual, sameJsonAsApproved());
    }
}
