package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.demo.adapter.ajc.Param;
import com.stellariver.milky.demo.adapter.ajc.anno.AjcAnnoDemo;
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
    public void testReturnValue() {
        AjcCustomDemo ajcCustomDemo = new AjcCustomDemo();
        Throwable throwable = null;
        try {
            ajcCustomDemo.testAjc(new Param());
        } catch (BizEx bizEx) {
            throwable = bizEx;
        }
        Assertions.assertNotNull(throwable);
    }

    @Test
    public void testAjcLog() {
        AjcCustomDemo ajcCustomDemo = new AjcCustomDemo();
        ajcCustomDemo.testAjcLog("Tom", 11L);
    }

    @Test
    public void testAjcLogOfAnno() {
        AjcAnnoDemo ajcAnnoDemo = new AjcAnnoDemo();
        ajcAnnoDemo.testAjcLogOfAnno("Tom", 11L);
    }

}
