package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class MetaInfoTest {

    @Test
    public void testJunitJupiterMetaWithSameBeanAsMatcher() {
        JunitJupiterTestMeta underTest = new JunitJupiterTestMeta();

        JunitJupiterTestMeta expected = new JunitJupiterTestMeta(Paths.get("src/test/java/com/github/karsaig/approvalcrest"), "com.github.karsaig.approvalcrest.MetaInfoTest", "testJunitJupiterMetaWithSameBeanAsMatcher", Paths.get("src/test/resources/approvalcrest"));
        assertThat(underTest, sameBeanAs(expected));
    }

    @Test
    public void testJunit5InfoMetaWithSameBeanAsMatcher(TestInfo testInfo) {
        Junit5InfoBasedTestMeta underTest = new Junit5InfoBasedTestMeta(testInfo);

        Junit5InfoBasedTestMeta expected = new Junit5InfoBasedTestMeta(Paths.get("src/test/java/com/github/karsaig/approvalcrest"), "com.github.karsaig.approvalcrest.MetaInfoTest", "testJunit5InfoMetaWithSameBeanAsMatcher", Paths.get("src/test/resources/approvalcrest"));
        assertThat(underTest, sameBeanAs(expected));
    }
}
