package com.stellariver.milky.common.base;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.stellariver.milky.common.base.ErrorEnumsBase.CONFIG_ERROR;
import static com.stellariver.milky.common.base.ErrorEnumsBase.PARAM_FORMAT_WRONG;


@JacksonAnnotationsInside
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JsonSerialize(using = EnumSerializer.Serializer.class)
public @interface EnumSerializer {

    String codeField() default "";
    String descField() default "desc";


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class Entity {
        String code;
        String desc;
    }


    class Serializer extends JsonSerializer<Enum<?>> implements ContextualSerializer {

        private Method getCodeMethod;
        private Method getDescMethod;

        @Override
        @SneakyThrows
        public void serialize(Enum<?> enumValue, JsonGenerator gen, SerializerProvider serializers) {
            Object code = getCodeMethod.invoke(enumValue);
            Object desc = getDescMethod.invoke(enumValue);
            gen.writeStartObject();
            gen.writeStringField("code", (String) code);
            gen.writeStringField("desc", (String) desc);
            gen.writeEndObject();
        }


        static private final String NO_GETTER_MESSAGE = " need getter for all fields thanks to json serializer requirements";

        @Override
        @SneakyThrows
        @SuppressWarnings("unchecked")
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            EnumSerializer enumSerializer = property.getAnnotation(EnumSerializer.class);
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) property.getType().getRawClass();

            String getCodeMethodName;
            if (!Utils.isBlank(enumSerializer.codeField())) {
                getCodeMethodName = "get" + enumSerializer.codeField();
            } else {
                getCodeMethodName = "name";
            }

            this.getCodeMethod = Arrays.stream(enumClass.getMethods())
                    .filter(m -> m.getName().equalsIgnoreCase(getCodeMethodName)).findFirst()
                    .orElseThrow(() -> new BizEx(CONFIG_ERROR.message(enumClass.getSimpleName() + NO_GETTER_MESSAGE)));
            this.getCodeMethod.setAccessible(true);
            boolean equals = getCodeMethod.getReturnType().equals(String.class);
            BizEx.falseThrow(equals, PARAM_FORMAT_WRONG.message("code field should be String type"));

            String getDescMethodName = "get" + enumSerializer.descField();
            this.getDescMethod = Arrays.stream(enumClass.getMethods())
                    .filter(m -> m.getName().equalsIgnoreCase(getDescMethodName)).findFirst()
                    .orElseThrow(() -> new BizEx(CONFIG_ERROR.message(enumClass.getSimpleName() + NO_GETTER_MESSAGE)));
            this.getDescMethod.setAccessible(true);
            equals = getDescMethod.getReturnType().equals(String.class);
            BizEx.falseThrow(equals, PARAM_FORMAT_WRONG.message("desc field should be String type"));
            return this;
        }

    }


}
