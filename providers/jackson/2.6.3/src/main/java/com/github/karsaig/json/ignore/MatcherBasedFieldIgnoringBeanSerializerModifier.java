package com.github.karsaig.json.ignore;

import java.util.Iterator;
import java.util.List;

import org.hamcrest.Matcher;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class MatcherBasedFieldIgnoringBeanSerializerModifier extends BeanSerializerModifier {
    private final List<Matcher<String>> matchers;

    public MatcherBasedFieldIgnoringBeanSerializerModifier(List<Matcher<String>> matchers) {
        this.matchers = matchers;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (int i = 0; i < beanProperties.size(); i++) {
            BeanPropertyWriter writer = beanProperties.get(i);

            Iterator<Matcher<String>> propertyNameMatchers = matchers.iterator();
            boolean found = false;
            while (propertyNameMatchers.hasNext() && !found) {
                Matcher<String> propertyNameMatcher = propertyNameMatchers.next();
                found = propertyNameMatcher.matches(writer.getName());
            }

            if (found) {
                beanProperties.set(i, new FieldIgnoringBeanPropertyWriter(writer));
            }
        }

        return super.changeProperties(config, beanDesc, beanProperties);
    }
}
