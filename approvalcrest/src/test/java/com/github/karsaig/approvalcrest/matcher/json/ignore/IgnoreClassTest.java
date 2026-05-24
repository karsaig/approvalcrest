package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import java.time.LocalDate;

import org.junit.Test;

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
}
