package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.common.Kit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnumTest {

    @Test
    @SuppressWarnings("all")
    public void enumOfTest() {
        Season season0 = Kit.enumOf(Season::getOrder, 0).get();
        Assertions.assertEquals(season0, Season.SPRING);
        Season season1 = Kit.enumOf(Season::getName, "夏天").get();
        Assertions.assertEquals(season1, Season.SUMMER);
    }

    @AllArgsConstructor
    enum Season {
        SPRING(0, "春天"),
        SUMMER(1, "夏天");
        @Getter
        final private Integer order;
        @Getter
        final private String name;
    }

}
