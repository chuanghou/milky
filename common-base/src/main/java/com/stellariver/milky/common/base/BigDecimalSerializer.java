package com.stellariver.milky.common.base;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.RoundingMode;

@JacksonAnnotationsInside
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JsonSerialize(using = BigDecimalSerializer.Serializer.class)
public @interface BigDecimalSerializer {

    int bits();

    RoundingMode roundingMode() default RoundingMode.HALF_UP;

    boolean paddingZero() default true;

    @NoArgsConstructor
    class Serializer extends JsonSerializer<BigDecimal> implements ContextualSerializer {

        private int bits;

        private RoundingMode roundingMode;

        private boolean paddingZero;

        @Override
        public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            bigDecimal = bigDecimal.setScale(bits, roundingMode);
            if (paddingZero) {
                jsonGenerator.writeString(bigDecimal.toString());
            } else {
                jsonGenerator.writeString(String.valueOf(bigDecimal.doubleValue()));
            }
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
            BigDecimalSerializer doubleSerializer = beanProperty.getAnnotation(BigDecimalSerializer.class);
            this.bits = doubleSerializer.bits();
            this.roundingMode = doubleSerializer.roundingMode();
            this.paddingZero = doubleSerializer.paddingZero();
            return this;
        }

    }


}
