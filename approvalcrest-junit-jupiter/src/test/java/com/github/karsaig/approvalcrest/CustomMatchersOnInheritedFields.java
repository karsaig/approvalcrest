package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.jupiter.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.jupiter.matcher.Matchers.sameBeanAs;
import static com.github.karsaig.approvalcrest.util.AssertionHelper.assertContains;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
public class CustomMatchersOnInheritedFields {

    @Test
    public void customMatchersShouldWorkForInheritedFieldsWhenNoDifference() {
        Child actual = new Child();
        Child expected = new Child();

        assertThat(actual, sameBeanAs(expected)
                .with("privateFieldGP", startsWith("privateGrandPar"))
                .with("protectedFieldGP", startsWith("inherited-grandParentPr"))
                .with("publicFieldGP", startsWith("inherited-grandParentPu"))
                .with("defaultFieldGP", startsWith("inherited-grandParentD"))
                .with("protectedFieldP", startsWith("inherited-parentPr"))
                .with("publicFieldP", startsWith("inherited-parentPu"))
                .with("defaultFieldP", startsWith("inherited-parentD"))
                .with("privateFieldP", startsWith("privatePar"))
        );
    }

    @Test
    public void customMatchersShouldWorkForNotInheritedFieldsWhenNoDifference() {
        Child actual = new Child();
        Child expected = new Child();

        assertThat(actual, sameBeanAs(expected)
                .with("protectedField", startsWith("prot"))
                .with("publicField", startsWith("pub"))
                .with("defaultField", startsWith("def"))
                .with("privateField", startsWith("pri"))
        );
    }

    @Test
    public void customMatchersShouldThrowExceptionWhenInheritedFieldDiffers() {
        Child actual = new Child();
        Child expected = new Child();

        AssertionError exception = assertThrows(AssertionError.class, () -> {
            assertThat(actual, sameBeanAs(expected).with("protectedFieldGP", startsWith("newProtectedValue")));
        });

        assertContains("protectedFieldGP was \"inherited-grandParentProtected\"", exception.getMessage());
    }

    @Test
    public void customMatchersShouldThrowExceptionWhenInheritedPrivateFieldDiffers() {
        Child actual = new Child();
        Child expected = new Child();

        AssertionError exception = assertThrows(AssertionError.class, () -> {
            assertThat(actual, sameBeanAs(expected).with("privateFieldGP", startsWith("newPrivateValue")));
        });

        assertContains("privateFieldGP was \"privateGrandParent\"", exception.getMessage());
    }

    @Test
    public void customMatchersShouldThrowExceptionWhenPrivateFieldDiffers() {
        Child actual = new Child();
        Child expected = new Child();

        AssertionError exception = assertThrows(AssertionError.class, () -> {
            assertThat(actual, sameBeanAs(expected).with("privateField", startsWith("newPrivateValue")));
        });

        assertContains("privateField was \"private\"", exception.getMessage());
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