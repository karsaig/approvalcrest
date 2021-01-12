package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;

import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class MetaInfoTest {

    @Rule
    public DesciptionWatcher testWatcher = new DesciptionWatcher() {

        @Override
        protected void starting(final Description description) {
            this.description = description;
        }
    };

    @Test
    public void testJunit4MetaWithSameBeanAsMatcher() {
        Junit4TestMeta underTest = new Junit4TestMeta();

        Junit4TestMeta expected = new Junit4TestMeta(Paths.get("src/test/java/com/github/karsaig/approvalcrest"), "com.github.karsaig.approvalcrest.MetaInfoTest", "testJunit4MetaWithSameBeanAsMatcher", Paths.get("src/test/resources/approvalcrest"));
        assertThat(underTest, sameBeanAs(expected));
    }

    @Test
    public void testJunit4DescriptionMetaWithSameBeanAsMatcher() {
        Junit4DescriptionBasedTestMeta underTest = new Junit4DescriptionBasedTestMeta(testWatcher.getDescription());

        Junit4DescriptionBasedTestMeta expected = new Junit4DescriptionBasedTestMeta(Paths.get("src/test/java/com/github/karsaig/approvalcrest"), "com.github.karsaig.approvalcrest.MetaInfoTest", "testJunit4DescriptionMetaWithSameBeanAsMatcher", Paths.get("src/test/resources/approvalcrest"));
        assertThat(underTest, sameBeanAs(expected));
    }

    @Test
    public void testStacktraceAndDescriptionBasedMetaHasSameResult() {
        Junit4TestMeta input1 = new Junit4TestMeta();
        Junit4DescriptionBasedTestMeta input2 = new Junit4DescriptionBasedTestMeta(testWatcher.getDescription());

        assertThat(input1, sameBeanAs(input2));
    }

    private static class DesciptionWatcher extends TestWatcher {
        protected Description description;

        public Description getDescription() {
            return description;
        }
    }
}
