package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.DoubleSerializer;
import com.stellariver.milky.common.base.EnumSerializer;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;
import lombok.experimental.FieldDefaults;



public class JsonTest {


    public static void main(String[] args) {
        JsonExample jsonExample = new JsonExample(2.34, 2.34, Season.SPRING);
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

        @DoubleSerializer(bits = 3, paddingZero = false)
        Double number0;

        @DoubleSerializer(bits = 3)
        Double number1;

        @EnumSerializer
        Season season;
    }
}
