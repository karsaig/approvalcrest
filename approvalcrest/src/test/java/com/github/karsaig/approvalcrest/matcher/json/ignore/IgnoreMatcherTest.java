package com.github.karsaig.approvalcrest.matcher.json.ignore;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameJsonAsApproved;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.Assert;
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

	@Test
	public void strictModeFailsForJsonStringInputWhenApprovedFileContainsIgnoredField() {
		// Same scenario but with a pre-serialised JSON string as actual input.
		// Using Gson object format for dates to match the approved file format.
		String input = "{\n" +
				"  \"birthCountry\": \"BELGIUM\",\n" +
				"  \"birthDate\": {\n" +
				"    \"date\": {\n" +
				"      \"year\": 2016,\n" +
				"      \"month\": 4,\n" +
				"      \"day\": 1\n" +
				"    },\n" +
				"    \"time\": {\n" +
				"      \"hour\": 13,\n" +
				"      \"minute\": 42,\n" +
				"      \"second\": 11,\n" +
				"      \"nano\": 0\n" +
				"    }\n" +
				"  },\n" +
				"  \"currentAddress\": {\n" +
				"    \"city\": \"CityName1\",\n" +
				"    \"country\": \"BELGIUM\",\n" +
				"    \"postCode\": \"PostCode64\",\n" +
				"    \"since\": {\n" +
				"      \"year\": 2017,\n" +
				"      \"month\": 4,\n" +
				"      \"day\": 2\n" +
				"    },\n" +
				"    \"streetName\": \"StreetName60\",\n" +
				"    \"streetNumber\": 43\n" +
				"  },\n" +
				"  \"email\": \"e1@e.mail\",\n" +
				"  \"firstName\": \"FirstName1\",\n" +
				"  \"lastName\": \"LastName1\",\n" +
				"  \"previousAddresses\": [\n" +
				"    {\n" +
				"      \"city\": \"CityName11\",\n" +
				"      \"country\": \"EGYPT\",\n" +
				"      \"postCode\": \"PostCode74\",\n" +
				"      \"since\": {\n" +
				"        \"year\": 2017,\n" +
				"        \"month\": 4,\n" +
				"        \"day\": 12\n" +
				"      },\n" +
				"      \"streetName\": \"StreetName70\",\n" +
				"      \"streetNumber\": 53\n" +
				"    }\n" +
				"  ]\n" +
				"}";

		thrown.expect(AssertionError.class);
		thrown.expectMessage("since");

		assertThat(input, sameJsonAsApproved().ignoring(Matchers.comparesEqualTo("since")));
	}
}
