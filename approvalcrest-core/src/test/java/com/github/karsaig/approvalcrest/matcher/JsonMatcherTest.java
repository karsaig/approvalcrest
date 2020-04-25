package com.github.karsaig.approvalcrest.matcher;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.github.karsaig.approvalcrest.util.InMemoryFiles;

/**
 * Unit test for the {@link JsonMatcher}.
 * Verifies creation of not approved files.
 *
 * @author Andras_Gyuro
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class JsonMatcherTest extends AbstractFileMatcherTest {

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExists() throws IOException {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "JsonMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExists");
            JsonMatcher<BeanWithPrimitives> underTest = new JsonMatcher<>(dummyTestInfo);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "183d71-not-approved.json", "183d71-approved.json"), actualError.getMessage());

            List<InMemoryFiles> files = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("8c5498/183d71-not-approved.json", "/*JsonMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExists*/\n" +
                    "{\n" +
                    "  \"beanInteger\": 4,\n" +
                    "  \"beanByte\": 2,\n" +
                    "  \"beanChar\": \"c\",\n" +
                    "  \"beanShort\": 1,\n" +
                    "  \"beanLong\": 6,\n" +
                    "  \"beanFloat\": 3.0,\n" +
                    "  \"beanDouble\": 5.0,\n" +
                    "  \"beanBoolean\": true\n" +
                    "}");

            assertIterableEquals(files, singletonList(expected));
        });
    }

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString() throws IOException {
        String actual = getBeanAsJsonString();
        inMemoryUnixFs((fs, path) -> {
            DummyInformation dummyTestInfo = new DummyInformation(path, "JsonMatcherTest", "testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString");
            JsonMatcher<String> underTest = new JsonMatcher<>(dummyTestInfo);

            AssertionError actualError = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            Assertions.assertEquals(getNotApprovedCreationMessage("8c5498", "675159-not-approved.json", "675159-approved.json"), actualError.getMessage());

            List<InMemoryFiles> files = getFiles(fs);
            InMemoryFiles expected = new InMemoryFiles("8c5498/675159-not-approved.json", "/*JsonMatcherTest.testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString*/\n" +
                    "{\n" +
                    "  \"beanLong\": 5,\n" +
                    "  \"beanString\": \"dummyString\",\n" +
                    "  \"beanInt\": 10\n" +
                    "}");

            assertIterableEquals(files, singletonList(expected));
        });
    }


}
