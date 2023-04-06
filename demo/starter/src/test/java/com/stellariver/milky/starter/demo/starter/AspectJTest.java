package com.stellariver.milky.starter.demo.starter;

import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.demo.adapter.ajc.Param;
import com.stellariver.milky.demo.adapter.ajc.custom.AjcCustomDemo;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@CustomLog
public class AspectJTest {

    @Test
    public void testParamValid() {
        AjcCustomDemo ajcCustomDemo = new AjcCustomDemo();
        Throwable throwable = null;
        try {
            ajcCustomDemo.testAjc(null);
        } catch (BizEx bizEx) {
            throwable = bizEx;
        }
        Assertions.assertNotNull(throwable);
    }

    @Test
    public void testReturnValue() throws InterruptedException {
        AjcCustomDemo ajcCustomDemo = new AjcCustomDemo();
        Throwable throwable = null;
        try {
            ajcCustomDemo.testAjc(new Param());
        } catch (BizEx bizEx) {
            throwable = bizEx;
        }
        Assertions.assertNotNull(throwable);
    }
}
