package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.assertContains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.testdata.Person;
import com.github.karsaig.approvalcrest.testdata.TestDataGenerator;

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
}
