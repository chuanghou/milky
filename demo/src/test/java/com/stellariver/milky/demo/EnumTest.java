package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.common.Kit;
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class EnumTest {

    @Test
    public void enumOfTest() {
        myTest();
        myTest();
    }

    private void myTest() {
        Season ss0 = Kit.enumOf(Season::getName, "sp");
        System.out.println(ss0);
    }

    enum Season {
        SPRING("sp", 0),
        SUMMER("su", 1);
        @Getter
        private final String name;
        @Getter
        private final Integer order;
        Season(String name, Integer order) {
            this.name = name;
            this.order = order;
        }
    }
}
