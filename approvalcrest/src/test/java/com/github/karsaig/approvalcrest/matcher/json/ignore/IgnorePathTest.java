package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.karsaig.approvalcrest.testdata.Country;
import com.github.karsaig.approvalcrest.testdata.Person;
import com.github.karsaig.approvalcrest.testdata.Team;
import com.github.karsaig.approvalcrest.testdata.TestDataGenerator;

public class IgnorePathTest {
	
	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
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

		thrown.expect(org.junit.ComparisonFailure.class);
		thrown.expectMessage("Expected: FirstName1\n     got: Different first name");
		
		assertThat(input, sameJsonAsApproved());
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

		thrown.expect(org.junit.ComparisonFailure.class);
		thrown.expectMessage("Expected: firstName\n     but none found");
		
		assertThat(input, sameJsonAsApproved());
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

		thrown.expect(org.junit.ComparisonFailure.class);
		thrown.expectMessage("Unexpected: firstName");
		
		assertThat(input, sameJsonAsApproved());
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

		thrown.expect(org.junit.ComparisonFailure.class);
		thrown.expectMessage("Unexpected: since");
		
		assertThat(input, sameJsonAsApproved());
	}
	
	@Test
	public void assertShouldFailWhenMultiLevelPathWithDifferenceIsNotIgnored() {
		Person input = TestDataGenerator.generatePerson(1L);

		input.getCurrentAddress().setSince(LocalDate.now());

		thrown.expect(org.junit.ComparisonFailure.class);
		thrown.expectMessage("currentAddress.since");
		
		assertThat(input, sameJsonAsApproved());
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
