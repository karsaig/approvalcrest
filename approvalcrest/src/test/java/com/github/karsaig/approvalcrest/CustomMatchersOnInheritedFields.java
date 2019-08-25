package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.MatcherAssert.assertThat;
import static com.github.karsaig.approvalcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("unused")
public class CustomMatchersOnInheritedFields {

	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
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
    	thrown.expect(AssertionError.class);
    	thrown.expectMessage("protectedFieldGP was \"inherited-grandParentProtected\"");
    	
    	assertThat(actual, sameBeanAs(expected).with("protectedFieldGP", startsWith("newProtectedValue")));
	}
    
    @Test
	public void customMatchersShouldThrowExceptionWhenInheritedPrivateFieldDiffers() {
    	Child actual = new Child();
    	Child expected = new Child();
    	thrown.expect(AssertionError.class);
    	thrown.expectMessage("privateFieldGP was \"privateGrandParent\"");
    	
    	assertThat(actual, sameBeanAs(expected).with("privateFieldGP", startsWith("newPrivateValue")));
	}
    
    @Test
	public void customMatchersShouldThrowExceptionWhenPrivateFieldDiffers() {
    	Child actual = new Child();
    	Child expected = new Child();
    	thrown.expect(AssertionError.class);
    	thrown.expectMessage("privateField was \"private\"");
    	
    	assertThat(actual, sameBeanAs(expected).with("privateField", startsWith("newPrivateValue")));
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