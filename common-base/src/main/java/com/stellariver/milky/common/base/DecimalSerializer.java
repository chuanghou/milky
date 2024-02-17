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
@JsonSerialize(using = DecimalSerializer.Serializer.class)
public @interface DecimalSerializer {

    int bits();

    RoundingMode roundingMode() default RoundingMode.HALF_UP;

    boolean paddingZero() default true;

    boolean asString() default false;

    @NoArgsConstructor
    class Serializer extends JsonSerializer<Object> implements ContextualSerializer {

        private int bits;
        private RoundingMode roundingMode;
        private boolean paddingZero;
        private boolean asString;

        @Override
        public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            BigDecimal bigDecimal;
            if (value instanceof Double) {
                bigDecimal = BigDecimal.valueOf((Double) value);
            } else if (value instanceof Float) {
                bigDecimal = BigDecimal.valueOf((Float) value);
            } else if (value instanceof BigDecimal) {
                bigDecimal = (BigDecimal) value;
            } else {
                throw new IllegalArgumentException("@DecimalSerializer could only be annotated to Double/float/BigDecimal");
            }
            bigDecimal = bigDecimal.setScale(bits, roundingMode);
            String jsonValue;
            if (paddingZero) {
                jsonValue = bigDecimal.toString();
            } else {
                jsonValue = String.valueOf(bigDecimal.doubleValue());
            }
            if (asString) {
                jsonValue = "\"" + jsonValue + "\"";
            }
            jsonGenerator.writeRawValue(jsonValue);
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
            DecimalSerializer decimalSerializer = beanProperty.getAnnotation(DecimalSerializer.class);
            this.bits = decimalSerializer.bits();
            this.roundingMode = decimalSerializer.roundingMode();
            this.paddingZero = decimalSerializer.paddingZero();
            this.asString = decimalSerializer.asString();
            return this;
        }


    }


}
