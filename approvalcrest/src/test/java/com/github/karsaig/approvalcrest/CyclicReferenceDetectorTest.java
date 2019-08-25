/*
 * Copyright 2013 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.karsaig.approvalcrest;

import static com.github.karsaig.approvalcrest.CyclicReferenceDetector.getClassesWithCircularReferences;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.github.karsaig.approvalcrest.model.cyclic.Element;
import com.github.karsaig.approvalcrest.model.cyclic.Five;
import com.github.karsaig.approvalcrest.model.cyclic.Four;
import com.github.karsaig.approvalcrest.model.cyclic.One;
import com.github.karsaig.approvalcrest.model.cyclic.Three;
import com.github.karsaig.approvalcrest.model.cyclic.Two;
import com.google.common.base.Function;

/**
 * Tests which verify the {@link com.github.karsaig.approvalcrest.CyclicReferenceDetector} returns the classes which participate in a cyclic reference.
 */
public class CyclicReferenceDetectorTest {
	
	private static final Set<String> EMPTY_PATHS_TO_IGNORE = Collections.emptySet();
	private static final List<Class<?>> EMPTY_TYPES_TO_IGNORE = Collections.emptyList();
	private static final List<Matcher<String>> EMPTY_PATTERNS_TO_IGNORE = Collections.emptyList();
	
    @Test
    public void shouldReturnAnEmptySetWhenTheObjectIsNull() {
    	MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
    	
        Set<Class<?>> returnedObjects = getClassesWithCircularReferences(null,matcherConfig);

        assertThat(returnedObjects.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnAnEmptyListWhenThereIsNoCircularReference() {
        One one = new One();
        one.setGenericObject(new Two());
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedObjects = getClassesWithCircularReferences(one,matcherConfig);
        
        assertThat(returnedObjects.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnTheClassWithCyclicReferenceWhenTheObjectHasAFieldWithCyclicReference() {
        One one = new One();
        Two two = new Two();
        two.setGenericObject(one);
        one.setGenericObject(two);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedObjects = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedObjects, hasItem(One.class));
    }

    @Test
    public void shouldReturnTheClassWithCyclicReferenceWhenTheObjectHasAFieldWithCyclicReferenceAndFieldIsNotSkipped() {
        One one = new One();
        Two two = new Two();
        two.setGenericObject(one);
        one.setGenericObject(two);
        Function<Object, Boolean> fieldSkipper = new Function<Object, Boolean>() {
			
			@Override
			public Boolean apply(Object input) {
				return Three.class.isInstance(input);
			}
		};
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE)
        		.addSkipCircularReferenceChecker(fieldSkipper);
        
        Set<Class<?>> returnedObjects = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedObjects, hasItem(One.class));
    }
    
    @Test
    public void shouldReturnEmptyListWhenTheObjectHasAFieldWithCyclicReferenceButTheTypeIsIgnored() {
        One one = new One();
        Two two = new Two();
        two.setGenericObject(one);
        one.setGenericObject(two);
        List<Class<?>> typesToIgnore = new ArrayList<Class<?>>();
        typesToIgnore.add(Two.class);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(typesToIgnore).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedObjects = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedObjects.isEmpty(), is(true));
    }
    
    @Test
    public void shouldReturnEmptyListWhenTheObjectHasAFieldWithCyclicReferenceButTheFieldIsSkipped() {
        One one = new One();
        Two two = new Two();
        two.setGenericObject(one);
        one.setGenericObject(two);
        List<Class<?>> typesToIgnore = new ArrayList<Class<?>>();
        typesToIgnore.add(Two.class);
        Function<Object, Boolean> fieldSkipper = new Function<Object, Boolean>() {
			
			@Override
			public Boolean apply(Object input) {
				return Two.class.isInstance(input);
			}
		};
        
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE)
        		.addSkipCircularReferenceChecker(fieldSkipper);
        
        Set<Class<?>> returnedObjects = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedObjects.isEmpty(), is(true));
    }
   
    
    @Test
    public void shouldReturnEmptyListWhenTheObjectHasAFieldWithCyclicReferenceButTheFieldNameIsIgnored() {
        One one = new One();
        Two two = new Two();
        two.setGenericObject(one);
        one.setGenericObject(two);
        List<Matcher<String>> fieldNamesToIgnore = new ArrayList<Matcher<String>>();
        fieldNamesToIgnore.add(Matchers.is("twoObject"));
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(fieldNamesToIgnore).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedObjects = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedObjects.isEmpty(), is(true));
    }
    
    @Test
    public void shouldReturnTheClassWithCyclicReferenceFieldWhenTheCyclicReferenceIsMoreThanTwoNodesAway() {
        One one = new One();
        Two two = new Two();
        Three three = new Three();

        one.setGenericObject(two);
        two.setGenericObject(three);
        three.setGenericObject(one);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses, hasItem(One.class));
    }

    @Test
    public void shouldReturnEmptyListWhenTheCyclicReferenceIsMoreThanTwoNodesAwayButTheTypeIsIgnored() {
        One one = new One();
        Two two = new Two();
        Three three = new Three();

        one.setGenericObject(two);
        two.setGenericObject(three);
        three.setGenericObject(one);
        List<Class<?>> typesToIgnore = new ArrayList<Class<?>>();
        typesToIgnore.add(Three.class);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(typesToIgnore).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }
    
    @Test
    public void shouldReturnEmptyListWhenTheCyclicReferenceIsMoreThanTwoNodesAwayButTheFieldNameIsIgnored() {
        One one = new One();
        Two two = new Two();
        Three three = new Three();

        one.setGenericObject(two);
        two.setGenericObject(three);
        three.setGenericObject(one);
        List<Matcher<String>> fieldNamesToIgnore = new ArrayList<Matcher<String>>();
        fieldNamesToIgnore.add(Matchers.is("threeObject"));
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(fieldNamesToIgnore).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }
    
    @Test
    public void shouldReturnAnEmptySetWhenTheObjectHasANullField() {
        One one = new One();
        one.setGenericObject(null);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnAnEmptySetWhenTheObjectHasANullFieldAndTheTypeIsIgnored() {
        One one = new One();
        one.setGenericObject(null);
        List<Class<?>> typesToIgnore = new ArrayList<Class<?>>();
        typesToIgnore.add(Object.class);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(typesToIgnore).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }
    
    @Test
    public void shouldReturnAnEmptySetWhenTheObjectHasANullFieldAndTheFieldNameIsIgnored() {
        One one = new One();
        one.setGenericObject(null);
        List<Matcher<String>> fieldNamesToIgnore = new ArrayList<Matcher<String>>();
        fieldNamesToIgnore.add(Matchers.is("oneObject"));
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(fieldNamesToIgnore).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }
    
    @Test
    public void shouldReturnAnEmptySetWhenTheObjectHasAPrimitiveOrWrapperField() {
        One one = new One();
        one.setGenericObject(5);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnAnEmptySetWhenTheObjectHasAStringField() {
        One one = new One();
        one.setGenericObject("irrelevant string");
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnAnEmptySetWhenTheObjectHasAnEnumField() {
        One one = new One();
        one.setGenericObject(Five.FIVE);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnTheClassWithCircularReferenceFieldWhenTheFieldIsAListAndContainsAnObjectThatCausesCircularReference() {
        One one = new One();
        Two two = new Two();
        Three three = new Three();

        two.setGenericObject(three);
        three.setGenericObject(one);
        one.setGenericObject(Arrays.asList(two));
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses, hasItem(One.class));
    }

    @Test
    public void shouldReturnEmptySetWhenTheFieldIsAListAndContainsAnObjectThatCausesCircularReferenceButTheFieldNameIsIgnored() {
        One one = new One();
        Two two = new Two();
        Three three = new Three();

        two.setGenericObject(three);
        three.setGenericObject(one);
        one.setGenericObject(Arrays.asList(two));
        List<Matcher<String>> fieldNamesToIgnore = new ArrayList<Matcher<String>>();
        fieldNamesToIgnore.add(Matchers.is("twoObject"));
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(fieldNamesToIgnore).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void shouldReturnClassesWithCircularReferenceWhenAnObjectHasAListFieldWithMoreThanOneObjectsThatHasCircularReference() {
        One one = new One();
        Two two = new Two();
        Two two2 = new Two();
        Three three = new Three();
        three.setGenericObject(two2);
        two2.setGenericObject(three);

        two.setGenericObject(one);
        one.setGenericObject(Arrays.asList(two, three));
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses, hasItems(One.class, Three.class));
    }

    @Test
    public void shouldReturnTheClassWithCircularReferenceWhenTheObjectHasAFieldThatReferencesItself() {
        One one = new One();
        one.setGenericObject(one);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses, hasItem(One.class));
    }

    @Test
    public void shouldReturnClassWithCircularReferenceWhenTheObjectHasAMapFieldWithAnObjectThatCausesCircularaReference() {
        One one = new One();
        Two two = new Two();

        two.setGenericObject(one);
        Map<Object, Two> twoMap = new HashMap<Object, Two>(1);

        twoMap.put(1, two);
        one.setGenericObject(twoMap);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses, hasItem(One.class));
    }

    @Test
    public void shouldReturnAClassWithCircularReferenceWhenTheObjectHasAFieldFromTheSuperClassThatHasCircularReference() {
        Four four = new Four();
        One one = new One();
        one.setGenericObject(four);
        four.setGenericObject(one);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses, hasItem(One.class));
    }

    @Test
    public void shouldReturnEmptySetWhenTheObjectHasAFieldFromTheSuperClassThatHasCircularReferenceButTheFieldNameIsIgnored() {
        Four four = new Four();
        One one = new One();
        one.setGenericObject(four);
        four.setGenericObject(one);
        List<Matcher<String>> fieldNamesToIgnore = new ArrayList<Matcher<String>>();
        fieldNamesToIgnore.add(Matchers.is("threeObject"));
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(fieldNamesToIgnore).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }
    
    @Test
    public void shouldReturnAClassWithCircularReferenceWhenTheObjectHasAMapFieldThatHasCircularReferenceOnTheKeySey() {
        One one = new One();
        Two two = new Two();

        two.setGenericObject(one);
        Map<Two, Object> twoMap = new HashMap<Two, Object>(1);

        twoMap.put(two, 1);
        one.setGenericObject(twoMap);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(one,matcherConfig);

        assertThat(returnedClasses, hasItem(One.class));
    }

    @Test
    public void shouldNotReturnAClassWhenAnObjectIsInSeparatePathAndCausesNoCircularReference() {
        One one = new One();
        Two two = new Two();

        Four four = new Four();
        four.setGenericObject(one);
        four.setSubClassField(two);
        one.setGenericObject(two);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(four,matcherConfig);

        assertThat(returnedClasses.isEmpty(), is(true));
    }

	@Test
	@SuppressWarnings("unchecked")
    public void shouldReturnClassesWithCircularReferenceEvenWhenTheTwoClassAreInSeparatePath() {
        Four four = new Four();
        Four four1 = new Four();

        Two two = new Two();
        four.setGenericObject(two);
        two.setGenericObject(four);

        four.setSubClassField(four1);

        Two two1 = new Two();
        Three three = new Three();

        four1.setSubClassField(two1);
        two1.setGenericObject(three);
        three.setGenericObject(two1);
        MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
        
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(four,matcherConfig);

        assertThat(returnedClasses, hasItems(Four.class, Two.class));
    }

    @Test
    public void shouldNotAddStaticFieldsToTheSetOfCircularReferences() {
    	MatcherConfiguration matcherConfig = new MatcherConfiguration().addTypeToIgnore(EMPTY_TYPES_TO_IGNORE).addPatternToIgnore(EMPTY_PATTERNS_TO_IGNORE).addPathToIgnore(EMPTY_PATHS_TO_IGNORE);
    	
        Set<Class<?>> returnedClasses = getClassesWithCircularReferences(Element.ONE,matcherConfig);

        assertThat(returnedClasses, is(empty()));
    }
}