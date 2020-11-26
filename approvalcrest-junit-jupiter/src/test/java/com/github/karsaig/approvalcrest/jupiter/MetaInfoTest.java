package com.github.karsaig.approvalcrest.jupiter;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class MetaInfoTest {

    @Test
    public void testJunitJupiterMetaWithSameBeanAsMatcher() {
        JunitJupiterTestMeta underTest = new JunitJupiterTestMeta();

        JunitJupiterTestMeta expected = new JunitJupiterTestMeta(Paths.get("src/test/java/com/github/karsaig/approvalcrest/jupiter"), "com.github.karsaig.approvalcrest.jupiter.MetaInfoTest", "testJunitJupiterMetaWithSameBeanAsMatcher", Paths.get("src/test/resources/approvalcrest"));
        assertThat(underTest, sameBeanAs(expected));
    }

    @Test
    public void testJunit5InfoMetaWithSameBeanAsMatcher(TestInfo testInfo) {
        JunitJupiterTestMeta underTest = new JunitJupiterTestMeta(testInfo);

        JunitJupiterTestMeta expected = new JunitJupiterTestMeta(Paths.get("src/test/java/com/github/karsaig/approvalcrest/jupiter"), "com.github.karsaig.approvalcrest.jupiter.MetaInfoTest", "testJunit5InfoMetaWithSameBeanAsMatcher", Paths.get("src/test/resources/approvalcrest"));
        assertThat(underTest, sameBeanAs(expected));
    }
}
