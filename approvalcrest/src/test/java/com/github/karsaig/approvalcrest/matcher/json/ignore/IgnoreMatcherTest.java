package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.karsaig.approvalcrest.testdata.Person;
import com.github.karsaig.approvalcrest.util.TestDataGenerator;

public class IgnoreMatcherTest {

	@SuppressWarnings("deprecation")
	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
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

		thrown.expect(AssertionError.class);
		thrown.expectMessage("since");

		assertThat(input, sameJsonAsApproved().ignoring(Matchers.comparesEqualTo("since")));
	}

}
