package com.stellariver.milky.common.tool.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.stellariver.milky.common.tool.Translate;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.reflect.Method;

public class Translator extends JsonSerializer<Enum<?>> implements ContextualSerializer {

    private Method method;

    @Override
    public void serialize(Enum<?> enumValue, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Object value = Reflect.invoke(method, enumValue);
        if (value instanceof String) {
            gen.writeString((String) value);
            return;
        }
        throw new IllegalStateException("Getter method return type not String!");
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Translate translate = property.getAnnotation(Translate.class);
        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) property.getType().getRawClass();
        String fieldName = translate.field();
        String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        this.method = enumClass.getMethod(getMethodName);
        return this;
    }

}
