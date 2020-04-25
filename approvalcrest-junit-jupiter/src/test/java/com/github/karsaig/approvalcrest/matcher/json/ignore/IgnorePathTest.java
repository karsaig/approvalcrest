package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.assertContains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.github.karsaig.approvalcrest.testdata.Country;
import com.github.karsaig.approvalcrest.testdata.Person;
import com.github.karsaig.approvalcrest.testdata.Team;
import com.github.karsaig.approvalcrest.util.TestDataGenerator;

public class IgnorePathTest {
    @Test
    public void assertShouldBeSuccessfulWhenSimplePathWithDifferenceIsIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.setFirstName("Different first name");

        assertThat(input, sameJsonAsApproved().ignoring("firstName"));
    }

    @Test
    public void assertShouldFailWhenSimplePathWithDifferenceIsNotIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.setFirstName("Different first name");

        AssertionFailedError exception = assertThrows(AssertionFailedError.class,
                () -> assertThat(input, sameJsonAsApproved()));

        assertContains("Expected: FirstName1\n     got: Different first name", exception.getMessage());
    }

    @Test
    public void assertShouldBeSuccessfulWhenSimplePathWithNullDifferenceIsIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.setFirstName(null);

        assertThat(input, sameJsonAsApproved().ignoring("firstName"));
    }

    @Test
    public void assertShouldFailWhenSimplePathWithNullDifferenceIsNotIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.setFirstName(null);

        AssertionFailedError exception = assertThrows(AssertionFailedError.class,
                () -> assertThat(input, sameJsonAsApproved()));

        assertContains("Expected: firstName\n     but none found", exception.getMessage());
    }

    @Test
    public void assertShouldBeSuccessfulWhenSimplePathWithNullDifferenceInFileIsIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.setFirstName("Different first name");

        assertThat(input, sameJsonAsApproved().ignoring("firstName"));
    }

    @Test
    public void assertShouldBeSuccessfulWhenSimplePathWithNullDifferenceInFileIsNotIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.setFirstName("Different first name");

        AssertionFailedError exception = assertThrows(AssertionFailedError.class,
                () -> assertThat(input, sameJsonAsApproved()));

        assertContains("Unexpected: firstName", exception.getMessage());
    }

    @Test
    public void assertShouldBeSuccessfulWhenMultiLevelPathWithDifferenceIsIgnored() {
        Team input = TestDataGenerator.generateTeam(2L);

        input.getLead().getCurrentAddress().setSince(LocalDate.now());

        assertThat(input, sameJsonAsApproved().ignoring("lead.currentAddress.since"));
    }

    @Test
    public void assertShouldBeSuccessfulWhenMultiLevelPathInCollectionWithDifferenceIsIgnored() {
        Team input = TestDataGenerator.generateTeam(2L);

        input.getMembers().get(0).getCurrentAddress().setSince(LocalDate.now());

        assertThat(input, sameJsonAsApproved().ignoring("members.currentAddress.since"));
    }

    @Test
    public void assertShouldBeSuccessfulWhenMultiLevelPathInCollectionWithNullDifferenceIsIgnored() {
        Team input = TestDataGenerator.generateTeam(2L);

        input.getMembers().get(0).getCurrentAddress().setSince(null);

        assertThat(input, sameJsonAsApproved().ignoring("members.currentAddress.since"));
    }

    @Test
    public void assertShouldBeSuccessfulWhenMultiLevelPathInCollectionWithNullDifferenceInFileIsIgnored() {
        Team input = TestDataGenerator.generateTeam(2L);

        input.getMembers().get(0).getCurrentAddress().setSince(LocalDate.now());

        assertThat(input, sameJsonAsApproved().ignoring("members.currentAddress.since"));
    }

    @Test
    public void assertShouldFailfulWhenMultiLevelPathInCollectionWithNullDifferenceInFileIsNotIgnored() {
        Team input = TestDataGenerator.generateTeam(2L);

        input.getMembers().get(0).getCurrentAddress().setSince(LocalDate.now());

        AssertionFailedError exception = assertThrows(AssertionFailedError.class,
                () -> assertThat(input, sameJsonAsApproved()));

        assertContains("Unexpected: since", exception.getMessage());
    }

    @Test
    public void assertShouldFailWhenMultiLevelPathWithDifferenceIsNotIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.getCurrentAddress().setSince(LocalDate.now());

        AssertionFailedError exception = assertThrows(AssertionFailedError.class,
                () -> assertThat(input, sameJsonAsApproved()));

        assertContains("currentAddress.since", exception.getMessage());
    }

    @Test
    public void assertShouldBeSuccessfulWhenMultipleSimplePathWithDifferenceIsIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.setFirstName("Different first name");
        input.setLastName("Different last name");

        assertThat(input, sameJsonAsApproved().ignoring("firstName").ignoring("lastName"));
    }

    @Test
    public void assertShouldBeSuccessfulWhenMultipleMultiLevelPathWithDifferenceIsIgnored() {
        Person input = TestDataGenerator.generatePerson(1L);

        input.getCurrentAddress().setSince(LocalDate.now());
        input.getCurrentAddress().setCountry(Country.HUNGARY);

        assertThat(input, sameJsonAsApproved().ignoring("currentAddress.since").ignoring("currentAddress.country"));
    }
}
