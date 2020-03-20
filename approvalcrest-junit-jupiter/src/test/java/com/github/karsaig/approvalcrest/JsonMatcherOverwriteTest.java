package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.assertContains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.karsaig.approvalcrest.matcher.Matchers;
import com.github.karsaig.approvalcrest.model.BeanWithPrimitives;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class JsonMatcherOverwriteTest extends AbstractJsonMatcherTest {

	private static final String OVERWRITE_FLAG_NAME = "jsonMatcherUpdateInPlace";

	@TempDir
	public File testFolder;

	@AfterAll
	public static void tearDown() {
		disableOverwrite();
	}

	@Test
	public void shouldThrowExceptionWhenSystemPropertyIsSetAndApprovedFileDoesNotExist() {
		// GIVEN
		enableOverwrite();
		BeanWithPrimitives input = getBeanWithPrimitives();

		// WHEN
		AssertionError exception = assertThrows(AssertionError.class, () -> {
			MatcherAssert.assertThat(input, Matchers.sameJsonAsApproved()
					.withPathName(testFolder.getAbsolutePath()).withFileName("notExistingApprovedFile"));
		});

		// THEN
		assertEquals("Not approved file created: 'notExistingApprovedFile-not-approved.json'; please verify its contents and rename it to 'notExistingApprovedFile-approved.json'.", exception.getMessage());
	}

	@Test
	public void shouldOverwriteApprovedFileWhenSystemPropertyIsSetAndApprovedFileExists() throws IOException {
		// GIVEN
		File tmp = new File(testFolder.getAbsolutePath() + "/overwriteTestInput-approved.json");
		enableOverwrite();
		BeanWithPrimitives input = getBeanWithPrimitives();
		Files.copy(new File("src/test/overwriteTestInputToCopy.json"), tmp);
		// WHEN
		MatcherAssert.assertThat(input, Matchers.sameJsonAsApproved()
				.withPathName(testFolder.getAbsolutePath()).withFileName("overwriteTestInput"));
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
		File tmp = new File(testFolder.getAbsolutePath() + "/overwriteTestInput2-approved.json");
		disableOverwrite();
		BeanWithPrimitives input = getBeanWithPrimitives();
		Files.copy(new File("src/test/overwriteTestInputToCopy.json"), tmp);

		// WHEN
		AssertionError exception = assertThrows(AssertionError.class, () -> {
			MatcherAssert.assertThat(input, Matchers.sameJsonAsApproved()
					.withPathName(testFolder.getAbsolutePath()).withFileName("overwriteTestInput2"));
		});

		// THEN
		assertContains("overwriteTestInput2\nbeanByte\nExpected: ChangedValue!!\n     got: 2", exception.getMessage());
	}

	private static void enableOverwrite() {
		System.setProperty(OVERWRITE_FLAG_NAME, "true");
	}

	private static void disableOverwrite() {
		System.setProperty(OVERWRITE_FLAG_NAME, "false");
	}
}
