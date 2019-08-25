package com.github.karsaig.approvalcrest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.github.karsaig.approvalcrest.matcher.Matchers;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ContentMatcherOverwriteTest {
	private static final String OVERWRITE_FLAG_NAME = "jsonMatcherUpdateInPlace";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@AfterClass
	public static void tearDown() {
		disableOverwrite();
	}

	@Test
	public void shouldThrowExceptionWhenSystemPropertyIsSetAndApprovedFileDoesNotExist() {
		// GIVEN
		enableOverwrite();
		String input = "Test input data...";
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage("Not approved file created: 'notExistingApprovedFile-not-approved.content';\n"
				+ " please verify its contents and rename it to 'notExistingApprovedFile-approved.content'.");
		// WHEN
		MatcherAssert.assertThat(input, Matchers.sameContentAsApproved()
				.withPathName(testFolder.getRoot().getAbsolutePath()).withFileName("notExistingApprovedFile"));
		// THEN
		// Exception thrown
	}

	@Test
	public void shouldOverwriteApprovedFileWhenSystemPropertyIsSetAndApprovedFileExists() throws IOException {
		// GIVEN
		File tmp = new File(testFolder.getRoot().getAbsolutePath() + "/overwriteTestInput-approved.content");
		enableOverwrite();

		String input = "Overwritten content...";
		Files.copy(new File("src/test/overwriteTestInputToCopy.content"), tmp);
		// WHEN
		MatcherAssert.assertThat(input, Matchers.sameContentAsApproved()
				.withPathName(testFolder.getRoot().getAbsolutePath()).withFileName("overwriteTestInput"));
		// THEN
		String actual = Files.toString(tmp, Charsets.UTF_8);
		String expected = "/*com.github.karsaig.approvalcrest.ContentMatcherOverwriteTest.shouldOverwriteApprovedFileWhenSystemPropertyIsSetAndApprovedFileExists*/\nOverwritten content...";
		assertThat(actual, is(expected));

	}

	@Test
	public void shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse() throws IOException {
		// GIVEN
		File tmp = new File(testFolder.getRoot().getAbsolutePath() + "/overwriteTestInput2-approved.content");
		disableOverwrite();
		String input = "Overwritten content...";
		Files.copy(new File("src/test/overwriteTestInputToCopy.content"), tmp);

		expectedException.expect(AssertionError.class);
		expectedException
				.expectMessage("Content does not match! expected:<O[riginal content!]> but was:<O[verwritten content...]>");

		// WHEN
		MatcherAssert.assertThat(input, Matchers.sameContentAsApproved()
				.withPathName(testFolder.getRoot().getAbsolutePath()).withFileName("overwriteTestInput2"));
		// THEN
		//Exception thrown
	}
	
	private static void enableOverwrite() {
		System.setProperty(OVERWRITE_FLAG_NAME, "true");
	}

	private static void disableOverwrite() {
		System.clearProperty(OVERWRITE_FLAG_NAME);
	}
}
