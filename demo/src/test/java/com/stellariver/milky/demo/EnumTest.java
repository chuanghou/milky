package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.EnumSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.Test;

/**
 * 枚举相关用例。
 * <p>
 * 两个 enum 互相把对方常量作为构造参数时，编译期会被
 * {@link com.stellariver.milky.common.compiler.CircularEnumConstructorProcessor} 拒绝
 * （见 {@code common-compiler} 模块测试）。
 */
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
        final private Integer order;
        final private String desc;
    }

}
