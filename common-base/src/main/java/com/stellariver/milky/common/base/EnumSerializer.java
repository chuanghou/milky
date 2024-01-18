package com.stellariver.milky.common.base;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.SneakyThrows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;


@JacksonAnnotationsInside
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JsonSerialize(using = EnumSerializer.Serializer.class)
public @interface EnumSerializer {

    String field() default "desc";


    class Serializer extends JsonSerializer<Enum<?>> implements ContextualSerializer {

        private Method method;

        @Override
        @SneakyThrows
        public void serialize(Enum<?> enumValue, JsonGenerator gen, SerializerProvider serializers) {
            method.setAccessible(true);
            Object value = method.invoke(enumValue);
            if (value instanceof String) {
                gen.writeString((String) value);
                return;
            }
            throw new IllegalStateException("Getter method return type not String!");
        }

        @Override
        @SneakyThrows
        @SuppressWarnings("unchecked")
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            EnumSerializer enumSerializer = property.getAnnotation(EnumSerializer.class);
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) property.getType().getRawClass();
            String fieldName = enumSerializer.field();
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            this.method = enumClass.getMethod(getMethodName);
            return this;
        }

    }


}
