package com.github.karsaig.approvalcrest.matcher.circular;

import static com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean.Child.Builder.child;
import static com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean.Parent.Builder.parent;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.cyclic.CircularReferenceBean;

/**
 * Where a circular-reference wrapper can appear in the written form.
 *
 * <p>This exists because "a wrapper is never an array element" looked like a way to tell a generated
 * wrapper key from a user's own map key that happens to spell one — the two are otherwise identical in
 * the text, since both are single-key objects. The invariant is false, which is why that collision is
 * documented as a limitation rather than fixed. See docs/ignoring-fields.md.
 */
public class JsonMatcherEnvelopePositionTest extends AbstractFileMatcherTest {

    @Test
    public void aWrapperIsWrittenAtEveryArrayPositionForACollectionOfCyclicObjects() {
        CircularReferenceBean.Parent p1 = parent().parentAttribute("p1").build();
        p1.addChild(child().withChildAttribute("c1").withParent(p1).build());
        CircularReferenceBean.Parent p2 = parent().parentAttribute("p2").build();
        p2.addChild(child().withChildAttribute("c2").withParent(p2).build());

        // Each element is a wrapper object, and the ids restart per element because the writer clears
        // its state per top-level graph.
        String approvedFileContent = "[\n" +
                "  {\n" +
                "    \"0x1\": {\n" +
                "      \"children\": [\n" +
                "        {\n" +
                "          \"childAttribute\": \"c1\",\n" +
                "          \"parent\": \"0x1\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"parentAttribute\": \"p1\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"0x1\": {\n" +
                "      \"children\": [\n" +
                "        {\n" +
                "          \"childAttribute\": \"c2\",\n" +
                "          \"parent\": \"0x1\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"parentAttribute\": \"p2\"\n" +
                "    }\n" +
                "  }\n" +
                "]";

        assertJsonMatcherWithDummyTestInfo(Arrays.asList(p1, p2), approvedFileContent, null);
    }
}
