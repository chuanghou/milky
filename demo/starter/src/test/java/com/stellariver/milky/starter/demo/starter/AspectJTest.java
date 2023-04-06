package com.stellariver.milky.starter.demo.starter;

import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.demo.adapter.ajc.custom.AjcCustomDemo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AspectJTest {

    @Test
    public void test() {
        AjcCustomDemo ajcCustomDemo = new AjcCustomDemo();
        Throwable throwable = null;
        try {
            ajcCustomDemo.testAjc(null);
        } catch (BizEx bizEx) {
            throwable = bizEx;
        }
        Assertions.assertNotNull(throwable);
    }
}
