package com.github.karsaig.json.ignore;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

public class FieldIgnoringBeanPropertyWriter extends BeanPropertyWriter {

    public FieldIgnoringBeanPropertyWriter(BeanPropertyWriter writer) {
        super(writer);
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        // We Skip field serialization.
    }
}
