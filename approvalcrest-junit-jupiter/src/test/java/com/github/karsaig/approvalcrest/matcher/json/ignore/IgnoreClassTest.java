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
import com.github.karsaig.approvalcrest.util.TestDataGenerator;

public class IgnoreClassTest {
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

		AssertionFailedError exception = assertThrows(AssertionFailedError.class, () -> {
			assertThat(input, sameJsonAsApproved());
		});

		assertContains("previousAddresses[0].since", exception.getMessage());
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
