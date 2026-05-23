package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.DecimalSerializer;
import com.stellariver.milky.common.base.EnumSerializer;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;


public class JsonTest {


    @Test
    public void testJson() {
        JsonExample jsonExample = new JsonExample(2.34D, 2.34D, 2.34D, 2.34F, BigDecimal.valueOf(0.23D), Season.SPRING);
        System.out.println(Json.toJson(jsonExample));
    }


    @Getter
    @AllArgsConstructor
    enum Season {

        SPRING("春天");

        final String desc;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static public class JsonExample {

        @DecimalSerializer(bits = 4, paddingZero = false)
        Double number0;

        @DecimalSerializer(bits = 4)
        Double number1;

        @DecimalSerializer(bits = 4, asString = true)
        Double number2;

        @DecimalSerializer(bits = 4)
        Float number3;

        @DecimalSerializer(bits = 4)
        BigDecimal number4;

        @EnumSerializer
        Season season;
    }
}
