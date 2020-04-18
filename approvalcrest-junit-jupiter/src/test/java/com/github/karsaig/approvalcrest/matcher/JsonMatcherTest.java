package com.github.karsaig.approvalcrest.matcher;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.karsaig.approvalcrest.AbstractJsonMatcherTest;
import com.github.karsaig.approvalcrest.model.BeanWithPrimitives;

/**
 * Unit test for the {@link JsonMatcher}.
 * Verifies creation of not approved files.
 *
 * @author Andras_Gyuro
 */
@ExtendWith(MockitoExtension.class)
public class JsonMatcherTest extends AbstractJsonMatcherTest {

    private static final String DUMMY_FILE_NAME = "fileName";
    private static final String DUMMY_APPROVED_FILE_NAME = DUMMY_FILE_NAME + "-approved";
    private static final String DUMMY_TEST_PATH = "DummyTestPath";
    private static final String DUMMY_CLASS_NAME = "DummyClassName";
    private static final String DUMMY_METHOD_NAME = "DummyMethodName";
    private static final String CLASS_HASH = "31a03e";
    private static final String METHOD_HASH = "f8e392";
    private static final Path DUMMY_FILE_NAME_WITH_PATH = Paths.get(DUMMY_TEST_PATH + File.separator + CLASS_HASH + File.separator + METHOD_HASH);
    private static final String DUMMY_COMMENT = DUMMY_CLASS_NAME + "." + DUMMY_METHOD_NAME;

    @Mock
    private FileStoreMatcherUtils utils;

    @Mock
    private TestMetaInformation testMetaInformation;

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExists() throws IOException {
        Path dummyFile = Paths.get(DUMMY_APPROVED_FILE_NAME);
        BeanWithPrimitives actual = getBeanWithPrimitives();


        when(testMetaInformation.testMethodName()).thenReturn(DUMMY_METHOD_NAME);
        when(testMetaInformation.testClassName()).thenReturn(DUMMY_CLASS_NAME);
        when(utils.getApproved(DUMMY_FILE_NAME_WITH_PATH)).thenReturn(dummyFile);
        when(testMetaInformation.getTestClassPath()).thenReturn(Paths.get(DUMMY_TEST_PATH));
        when(utils.createNotApproved(eq(DUMMY_FILE_NAME_WITH_PATH), anyString(), eq(DUMMY_COMMENT))).thenReturn(DUMMY_FILE_NAME);
        JsonMatcher<BeanWithPrimitives> matcher = new JsonMatcher<>(testMetaInformation);
        matcher.setJsonMatcherUtils(utils);

        try {
            assertThat(actual, matcher);
        } catch (AssertionError er) {
            assertThat(er.getMessage(), is(getNotApprovedCreationMessage()));
        }
    }

    @Test
    public void testRunShouldCreateNotApprovedFileWhenNotExistsAndModelAsString() throws IOException {
        Path dummyFile = Paths.get(DUMMY_APPROVED_FILE_NAME);
        String actual = getBeanAsJsonString();

        when(testMetaInformation.testMethodName()).thenReturn(DUMMY_METHOD_NAME);
        when(testMetaInformation.testClassName()).thenReturn(DUMMY_CLASS_NAME);
        when(utils.getApproved(DUMMY_FILE_NAME_WITH_PATH)).thenReturn(dummyFile);
        when(testMetaInformation.getTestClassPath()).thenReturn(Paths.get(DUMMY_TEST_PATH));
        when(utils.createNotApproved(eq(DUMMY_FILE_NAME_WITH_PATH), anyString(), eq(DUMMY_COMMENT))).thenReturn(DUMMY_FILE_NAME);
        JsonMatcher<String> matcher = new JsonMatcher<>(testMetaInformation);
        matcher.setJsonMatcherUtils(utils);

        try {
            assertThat(actual, matcher);
        } catch (AssertionError er) {
            assertThat(er.getMessage(), is(getNotApprovedCreationMessage()));
        }
    }

    private String getNotApprovedCreationMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Not approved file created: '");
        builder.append(CLASS_HASH);
        builder.append(File.separator);
        builder.append(DUMMY_FILE_NAME);
        builder.append("'; please verify its contents and rename it to '");
        builder.append(DUMMY_APPROVED_FILE_NAME);
        builder.append("'.");
        return builder.toString();
    }

}
