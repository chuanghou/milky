package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.common.ValidateConfig;
import com.stellariver.milky.common.tool.common.ValidateUtil;
import com.stellariver.milky.common.tool.exception.BizException;
import lombok.Data;
import lombok.SneakyThrows;
import org.hibernate.validator.constraints.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.lang.reflect.Method;

public class ValidateUtilTest {


    @Test
    @SneakyThrows
    public void testValid() {
        Fool fool = new Fool();
        Method method = Fool.class.getMethod("myTest", Long.class, FoolParam.class);
        Throwable ex = null;
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, new FoolParam()}, true);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizException);
        Assertions.assertEquals(ex.getMessage(), "id 不能为空");

        ex = null;
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, new FoolParam()}, true, Default.class);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizException);
        Assertions.assertEquals(ex.getMessage(), "id 不能为空");


        ex = null;
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, null}, false);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizException);
        Assertions.assertTrue(ex.getMessage().contains(";"));


        method = Fool.class.getMethod("myTestWithGroup", Long.class, FoolParam.class);
        ex = null;
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, new FoolParam()}, true, MyGroup.class);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNull(ex);

        method = Fool.class.getMethod("myTestWithEmptyGroup", Long.class, FoolParam.class);
        ValidateConfig annotation = method.getAnnotation(ValidateConfig.class);
        Assertions.assertEquals(0, annotation.groups().length);

        method = Fool.class.getMethod("myTestDefaultGroup", Long.class, FoolParam.class);
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, null}, false, Default.class);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizException);
        Assertions.assertEquals(ex.getMessage(), "default");

        ex = null;
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, null}, false, MyGroup.class);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizException);
        Assertions.assertEquals(ex.getMessage(), "myGroup");
    }

    static private class Fool {

        public void myTest(@NotNull(message = "id 不能为空") Long id, @NotNull(message = "foolParam 不能为空") FoolParam foolParam) {

        }

        @ValidateConfig(groups = MyGroup.class)
        public void myTestWithGroup(@NotNull Long id, FoolParam foolParam) {

        }

        @ValidateConfig
        public void myTestWithEmptyGroup(@NotNull Long id, FoolParam foolParam) {

        }


        @ValidateConfig
        public void myTestDefaultGroup(@NotNull(message = "myGroup", groups = MyGroup.class) Long id,
                                       @NotNull(message = "default", groups = Default.class) FoolParam foolParam) {

        }
    }

    interface MyGroup{}

    @Data
    static private class FoolParam {

        @NotNull
        private String string;

        @Range(min = 1)
        private Integer integer;

    }


}
