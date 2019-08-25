package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.karsaig.approvalcrest.testdata.Person;
import com.github.karsaig.approvalcrest.testdata.TestDataGenerator;

public class IgnoreMatcherTest {

	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
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

		thrown.expect(org.junit.ComparisonFailure.class);
		thrown.expectMessage("previousAddresses[0].since");
		
		assertThat(input, sameJsonAsApproved());
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
