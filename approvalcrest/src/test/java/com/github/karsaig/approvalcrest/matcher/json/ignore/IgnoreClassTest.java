package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.karsaig.approvalcrest.testdata.Country;
import com.github.karsaig.approvalcrest.testdata.Person;
import com.github.karsaig.approvalcrest.testdata.TestDataGenerator;

public class IgnoreClassTest {
	
    @Rule
    public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void assertShouldBeSuccessfulWhenClassWithDifferenceIsIgnored() {
		Person input = TestDataGenerator.generatePerson(1L);

		input.getCurrentAddress().setSince(LocalDate.now());
		input.getPreviousAddresses().get(0).setSince(LocalDate.now());

		assertThat(input, sameJsonAsApproved().ignoring(LocalDate.class));
	}
	
	@Test
	public void assertShouldFailWhenClassWithDifferenceIsNotIgnored() {
		Person input = TestDataGenerator.generatePerson(1L);

		input.getCurrentAddress().setSince(LocalDate.now());
		input.getPreviousAddresses().get(0).setSince(LocalDate.now());

		thrown.expect(org.junit.ComparisonFailure.class);
		thrown.expectMessage("previousAddresses[0].since");
		
		assertThat(input, sameJsonAsApproved());
	}
	
	@Test
	public void assertShouldBeSuccessfulWhenMultipleClassWithDifferenceIsIgnored() {
		Person input = TestDataGenerator.generatePerson(1L);

		input.getCurrentAddress().setSince(LocalDate.now());
		input.getPreviousAddresses().get(0).setSince(LocalDate.now());
		input.setBirthCountry(Country.AUSTRIA);

		assertThat(input, sameJsonAsApproved().ignoring(LocalDate.class).ignoring(Country.class));
	}
}
