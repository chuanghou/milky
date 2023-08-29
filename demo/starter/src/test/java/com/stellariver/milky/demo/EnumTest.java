package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.Translate;
import com.stellariver.milky.common.tool.util.Json;
import lombok.*;
import org.junit.jupiter.api.Test;

public class EnumTest {

    @Test
    @SuppressWarnings("all")
    public void enumOfTest() {
        MyItem myItem = new MyItem(Season.SPRING);
        System.out.println(Json.toJson(myItem));
    }

    @Data
    @AllArgsConstructor
    static public class MyItem {

        @Translate
        Season season;
    }


    @Getter
    @AllArgsConstructor
    enum Season {
        SPRING(0, "春天"),
        SUMMER(1, "夏天");
        @Getter
        final private Integer order;
        @Getter
        final private String desc;
    }

}
