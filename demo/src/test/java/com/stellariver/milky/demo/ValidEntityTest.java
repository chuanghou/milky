package com.stellariver.milky.demo;


import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidEntityTest{


    @Test
    public void validEntityTest() {
        ValidEntity validEntity = ValidEntity.builder().build();
        Throwable t = null;
        try {
            ValidateUtil.validate(validEntity);
        } catch (Throwable throwable) {
            t = throwable;
        }
        Assertions.assertNotNull(t);
        Assertions.assertTrue(t instanceof BizEx);
        Assertions.assertEquals(t.getMessage(), "number不能为空");

        t = null;
        try {
            ValidateUtil.validate(validEntity, ExceptionType.BIZ, true, ValidEntity.NameGroup.class);
        } catch (Throwable throwable) {
            t = throwable;
        }
        Assertions.assertNotNull(t);
        Assertions.assertTrue(t instanceof BizEx);
        Assertions.assertEquals(t.getMessage(), "name不能为空");

    }


}
