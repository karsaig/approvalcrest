package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.assertContains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

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

    @Test
    public void assertShouldFailWhenPropertyWithDifferenceIsNotIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.getCurrentAddress().setSince(LocalDate.now());
        input.getPreviousAddresses().get(0).setSince(LocalDate.now());

        AssertionFailedError exception = assertThrows(AssertionFailedError.class, () -> assertThat(input, sameJsonAsApproved()));

        assertContains("previousAddresses[0].since", exception.getMessage());
    }

    @Test
    public void assertShouldBeSuccessfulWhenMultiplePropertyWithDifferenceIsIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.getCurrentAddress().setSince(LocalDate.now());
        input.getPreviousAddresses().get(0).setSince(LocalDate.now());
        input.setFirstName("Different first name");
        input.setLastName("Different last name");

        assertThat(input, sameJsonAsApproved().ignoring(Matchers.comparesEqualTo("since")).ignoring(Matchers.containsString("Name")));
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
