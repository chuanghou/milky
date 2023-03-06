package com.stellariver.milky.demo;


import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.validate.tool.ValidateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

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
        Assertions.assertTrue(t instanceof BizException);
        Assertions.assertEquals(t.getMessage(), "number不能为空");

        t = null;
        try {
            ValidateUtil.validate(validEntity, ExceptionType.BIZ, true, ValidEntity.NameGroup.class);
        } catch (Throwable throwable) {
            t = throwable;
        }
        Assertions.assertNotNull(t);
        Assertions.assertTrue(t instanceof BizException);
        Assertions.assertEquals(t.getMessage(), "name不能为空");

    }


}
