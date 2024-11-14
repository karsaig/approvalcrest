package com.github.karsaig.approvalcrest.matcher.custom;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.startsWith;

public class BeanMatcherCustomMatchOnInheritedFields extends AbstractBeanMatcherTest {

    @Test
    public void customMatchersShouldWorkForInheritedFieldsWhenNoDifference() {
        Child actual = new Child();
        Child expected = new Child();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .with("privateFieldGP", startsWith("privateGrandPar"))
                .with("protectedFieldGP", startsWith("inherited-grandParentPr"))
                .with("publicFieldGP", startsWith("inherited-grandParentPu"))
                .with("defaultFieldGP", startsWith("inherited-grandParentD"))
                .with("protectedFieldP", startsWith("inherited-parentPr"))
                .with("publicFieldP", startsWith("inherited-parentPu"))
                .with("defaultFieldP", startsWith("inherited-parentD"))
                .with("privateFieldP", startsWith("privatePar")));
    }

    @Test
    public void customMatchersShouldWorkForNotInheritedFieldsWhenNoDifference() {
        Child actual = new Child();
        Child expected = new Child();

        assertDiagnosingMatcher(actual, expected, beanMatcher -> beanMatcher
                .with("protectedField", startsWith("prot"))
                .with("publicField", startsWith("pub"))
                .with("defaultField", startsWith("def"))
                .with("privateField", startsWith("pri")));
    }

    @Test
    public void customMatchersShouldThrowExceptionWhenInheritedFieldDiffers() {
        Child actual = new Child();
        Child expected = new Child();

        assertDiagnosingMatcher(actual, expected, sameBeanAs -> sameBeanAs.with("protectedFieldGP", startsWith("newProtectedValue")), AssertionError.class, thrown -> {
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"defaultField\": \"default\",\n" +
                    "  \"defaultFieldGP\": \"inherited-grandParentDefault\",\n" +
                    "  \"defaultFieldP\": \"inherited-parentDefault\",\n" +
                    "  \"privateField\": \"private\",\n" +
                    "  \"privateFieldGP\": \"privateGrandParent\",\n" +
                    "  \"privateFieldP\": \"privateParent\",\n" +
                    "  \"protectedField\": \"protected\",\n" +
                    "  \"protectedFieldP\": \"inherited-parentProtected\",\n" +
                    "  \"publicField\": \"public\",\n" +
                    "  \"publicFieldGP\": \"inherited-grandParentPublic\",\n" +
                    "  \"publicFieldP\": \"inherited-parentPublic\"\n" +
                    "}\n" +
                    "and protectedFieldGP a string starting with \"newProtectedValue\"\n" +
                    "     but: protectedFieldGP was \"inherited-grandParentProtected\"", thrown.getMessage());
        });
    }

    @Test
    public void customMatchersShouldThrowExceptionWhenInheritedPrivateFieldDiffers() {
        Child actual = new Child();
        Child expected = new Child();

        assertDiagnosingMatcher(actual, expected, sameBeanAs -> sameBeanAs.with("privateFieldGP", startsWith("newPrivateValue")), AssertionError.class, thrown -> {
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"defaultField\": \"default\",\n" +
                    "  \"defaultFieldGP\": \"inherited-grandParentDefault\",\n" +
                    "  \"defaultFieldP\": \"inherited-parentDefault\",\n" +
                    "  \"privateField\": \"private\",\n" +
                    "  \"privateFieldP\": \"privateParent\",\n" +
                    "  \"protectedField\": \"protected\",\n" +
                    "  \"protectedFieldGP\": \"inherited-grandParentProtected\",\n" +
                    "  \"protectedFieldP\": \"inherited-parentProtected\",\n" +
                    "  \"publicField\": \"public\",\n" +
                    "  \"publicFieldGP\": \"inherited-grandParentPublic\",\n" +
                    "  \"publicFieldP\": \"inherited-parentPublic\"\n" +
                    "}\n" +
                    "and privateFieldGP a string starting with \"newPrivateValue\"\n" +
                    "     but: privateFieldGP was \"privateGrandParent\"", thrown.getMessage());
        });
    }

    @Test
    public void customMatchersShouldThrowExceptionWhenPrivateFieldDiffers() {
        Child actual = new Child();
        Child expected = new Child();

        assertDiagnosingMatcher(actual, expected, sameBeanAs -> sameBeanAs.with("privateField", startsWith("newPrivateValue")), AssertionError.class, thrown -> {
            Assertions.assertEquals("\n" +
                    "Expected: {\n" +
                    "  \"defaultField\": \"default\",\n" +
                    "  \"defaultFieldGP\": \"inherited-grandParentDefault\",\n" +
                    "  \"defaultFieldP\": \"inherited-parentDefault\",\n" +
                    "  \"privateFieldGP\": \"privateGrandParent\",\n" +
                    "  \"privateFieldP\": \"privateParent\",\n" +
                    "  \"protectedField\": \"protected\",\n" +
                    "  \"protectedFieldGP\": \"inherited-grandParentProtected\",\n" +
                    "  \"protectedFieldP\": \"inherited-parentProtected\",\n" +
                    "  \"publicField\": \"public\",\n" +
                    "  \"publicFieldGP\": \"inherited-grandParentPublic\",\n" +
                    "  \"publicFieldP\": \"inherited-parentPublic\"\n" +
                    "}\n" +
                    "and privateField a string starting with \"newPrivateValue\"\n" +
                    "     but: privateField was \"private\"", thrown.getMessage());
        });
    }

    private class GrandParent {
        private String privateFieldGP = "privateGrandParent";
        protected String protectedFieldGP = "inherited-grandParentProtected";
        public String publicFieldGP = "inherited-grandParentPublic";
        String defaultFieldGP = "inherited-grandParentDefault";

        public void setPrivateFieldGP(String privateFieldGP) {
            this.privateFieldGP = privateFieldGP;
        }

        public void setProtectedFieldGP(String protectedFieldGP) {
            this.protectedFieldGP = protectedFieldGP;
        }
    }

    private class Parent extends GrandParent {
        private String privateFieldP = "privateParent";
        protected String protectedFieldP = "inherited-parentProtected";
        public String publicFieldP = "inherited-parentPublic";
        String defaultFieldP = "inherited-parentDefault";
    }

    private class Child extends Parent {
        private String privateField = "private";
        protected String protectedField = "protected";
        public String publicField = "public";
        String defaultField = "default";
    }
}
