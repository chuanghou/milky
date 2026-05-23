package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.EnumSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class EnumTest {

    @Test
    @SuppressWarnings("all")
    public void enumOfTest() {
        MyItem myItem = new MyItem(Season.SPRING);
    }

    @Data
    @AllArgsConstructor
    static public class MyItem {

        @EnumSerializer
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
