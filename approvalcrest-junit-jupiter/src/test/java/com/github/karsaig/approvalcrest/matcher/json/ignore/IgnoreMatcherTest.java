package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.assertContains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.testdata.Person;
import com.github.karsaig.approvalcrest.util.TestDataGenerator;

public class IgnoreMatcherTest {

    @Test
    public void assertShouldBeSuccessfulWhenPropertyWithDifferenceIsIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.getCurrentAddress().setSince(LocalDate.now());
        input.getPreviousAddresses().get(0).setSince(LocalDate.now());

        assertThat(input, sameJsonAsApproved().ignoring(Matchers.comparesEqualTo("since")));
    }

    // f4-strict-ignored-field-fail: strict mode (the default) does NOT strip ignored fields from
    // the expected (approved file) side, so if the approved file contains "since" and we ignore it,
    // expected has "since" but actual doesn't → the comparison FAILS.
    @Test
    public void strictModeFailsForBeanInputWhenApprovedFileContainsIgnoredField() {
        // generatePerson(1L) produces fixed "since" dates that match the approved file.
        // ignoring("since") removes "since" from actual but strict mode keeps it in expected.
        Person input = TestDataGenerator.generatePerson(1L);

        AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(input, sameJsonAsApproved().ignoring(Matchers.comparesEqualTo("since"))));

        assertContains("since", error.getMessage());
    }

}
