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
@JsonSerialize(using = DoubleSerializer.Serializer.class)
public @interface DoubleSerializer {

    int bits();

    RoundingMode roundingMode() default RoundingMode.HALF_UP;

    boolean paddingZero() default true;

    @NoArgsConstructor
    class Serializer extends JsonSerializer<Double> implements ContextualSerializer {

        private int bits;

        private RoundingMode roundingMode;

        private boolean paddingZero;

        @Override
        public void serialize(Double value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            BigDecimal bigDecimal = BigDecimal.valueOf(value).setScale(bits, roundingMode);
            if (paddingZero) {
                jsonGenerator.writeRawValue(bigDecimal.toString());
            } else {
                jsonGenerator.writeRawValue(String.valueOf(bigDecimal.doubleValue()));
            }
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
            DoubleSerializer doubleSerializer = beanProperty.getAnnotation(DoubleSerializer.class);
            this.bits = doubleSerializer.bits();
            this.roundingMode = doubleSerializer.roundingMode();
            this.paddingZero = doubleSerializer.paddingZero();
            return this;
        }


    }


}
