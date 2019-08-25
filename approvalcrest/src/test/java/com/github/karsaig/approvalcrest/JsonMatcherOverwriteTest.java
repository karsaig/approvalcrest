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
import com.github.karsaig.approvalcrest.model.BeanWithPrimitives;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class JsonMatcherOverwriteTest extends AbstractJsonMatcherTest {

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
		BeanWithPrimitives input = getBeanWithPrimitives();
		expectedException.expect(AssertionError.class);
		expectedException.expectMessage(
				"Not approved file created: 'notExistingApprovedFile-not-approved.json'; please verify its contents and rename it to 'notExistingApprovedFile-approved.json'.");
		// WHEN
		MatcherAssert.assertThat(input, Matchers.sameJsonAsApproved()
				.withPathName(testFolder.getRoot().getAbsolutePath()).withFileName("notExistingApprovedFile"));
		// THEN
		// Exception thrown
	}

	@Test
	public void shouldOverwriteApprovedFileWhenSystemPropertyIsSetAndApprovedFileExists() throws IOException {
		// GIVEN
		File tmp = new File(testFolder.getRoot().getAbsolutePath() + "/overwriteTestInput-approved.json");
		enableOverwrite();
		BeanWithPrimitives input = getBeanWithPrimitives();
		Files.copy(new File("src/test/overwriteTestInputToCopy.json"), tmp);
		// WHEN
		MatcherAssert.assertThat(input, Matchers.sameJsonAsApproved()
				.withPathName(testFolder.getRoot().getAbsolutePath()).withFileName("overwriteTestInput"));
		// THEN
		String expected = "/*com.github.karsaig.approvalcrest.JsonMatcherOverwriteTest.shouldOverwriteApprovedFileWhenSystemPropertyIsSetAndApprovedFileExists*/\n"
				+ "{\n" + "  \"beanInteger\": 4,\n" + "  \"beanByte\": 2,\n" + "  \"beanChar\": \"c\",\n"
				+ "  \"beanShort\": 1,\n" + "  \"beanLong\": 6,\n" + "  \"beanFloat\": 3.0,\n"
				+ "  \"beanDouble\": 5.0,\n" + "  \"beanBoolean\": true\n" + "}";
		String actual = Files.toString(tmp, Charsets.UTF_8);
		assertThat(actual, is(expected));
	}

	@Test
	public void shouldThrowExceptionWhenApprovedFileDiffersAndFlagIsFalse() throws IOException {
		// GIVEN
		File tmp = new File(testFolder.getRoot().getAbsolutePath() + "/overwriteTestInput2-approved.json");
		disableOverwrite();
		BeanWithPrimitives input = getBeanWithPrimitives();
		Files.copy(new File("src/test/overwriteTestInputToCopy.json"), tmp);

		expectedException.expect(AssertionError.class);
		expectedException
				.expectMessage("overwriteTestInput2\n" + "beanByte\n" + "Expected: ChangedValue!!\n" + "     got: 2");

		// WHEN
		MatcherAssert.assertThat(input, Matchers.sameJsonAsApproved()
				.withPathName(testFolder.getRoot().getAbsolutePath()).withFileName("overwriteTestInput2"));
		// THEN
		//Exception thrown
	}

	private static void enableOverwrite() {
		System.setProperty(OVERWRITE_FLAG_NAME, "true");
	}

	private static void disableOverwrite() {
		System.setProperty(OVERWRITE_FLAG_NAME, "false");
	}
}
