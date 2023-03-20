package com.stellariver.milky.demo;

import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.tool.validate.OfEnum;
import com.stellariver.milky.common.tool.validate.Validate;
import com.stellariver.milky.common.tool.validate.ValidateUtil;
import com.stellariver.milky.financial.base.ExactDivision;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.Valid;
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
        Assertions.assertTrue(ex instanceof BizEx);
        Assertions.assertEquals(ex.getMessage(), "id 不能为空");

        ex = null;
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, new FoolParam()}, true, Default.class);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizEx);
        Assertions.assertEquals(ex.getMessage(), "id 不能为空");


        ex = null;
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, null}, false);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizEx);
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
        Validate annotation = method.getAnnotation(Validate.class);
        Assertions.assertEquals(1, annotation.groups().length);

        method = Fool.class.getMethod("myTestDefaultGroup", Long.class, FoolParam.class);
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, null}, false, Default.class);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizEx);
        Assertions.assertEquals(ex.getMessage(), "default");

        ex = null;
        try {
            ValidateUtil.bizValidate(fool, method, new Object[]{null, null}, false, MyGroup.class);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizEx);
        Assertions.assertEquals(ex.getMessage(), "myGroup");

        ValidParam validParam = new ValidParam();
        validParam.setParam(new NestValidParam());
        ex = null;
        try {
            ValidateUtil.validate(validParam, ExceptionType.BIZ, true);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex instanceof BizEx);
        Assertions.assertEquals(ex.getMessage(), "NESTED");

        NotValidParam notValidParam = new NotValidParam();
        notValidParam.setParam(new NestValidParam());
        ex = null;
        try {
            ValidateUtil.validate(notValidParam, ExceptionType.BIZ, true);
        } catch (Throwable throwable) {
            ex = throwable;
        }
        Assertions.assertNull(ex);
    }

    static private class Fool {

        public void myTest(@NotNull(message = "id 不能为空") Long id, @NotNull(message = "foolParam 不能为空") FoolParam foolParam) {

        }

        @Validate(groups = MyGroup.class)
        public void myTestWithGroup(@NotNull Long id, FoolParam foolParam) {

        }

        @Validate
        public void myTestWithEmptyGroup(@NotNull Long id, FoolParam foolParam) {

        }


        @Validate
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

    @Data
    static private class ValidParam {

        @Valid
        private NestValidParam param;

    }

    @Data
    static private class NotValidParam {

        private NestValidParam param;

    }

    @Data
    static private class NestValidParam {

        @NotNull(message = "NESTED")
        private String string;


    }



    @Test
    public void enumNameTest() {

        MyParamTestEnumName myParamTestEnumName;
        Throwable throwable = null;
        try {
            myParamTestEnumName = MyParamTestEnumName.builder().code("PDD").build();
            ValidateUtil.validate(myParamTestEnumName);
        } catch (Throwable t) {
            throwable = t;
        }
        Assertions.assertNotNull(throwable);
        Assertions.assertTrue(throwable instanceof BizEx);

        myParamTestEnumName = MyParamTestEnumName.builder().code("ALI").build();
        ValidateUtil.validate(myParamTestEnumName);

    }

    @Data
    @Builder
    static class MyParamTestEnumName {

        @OfEnum(enumType = ChannelEnum.class)
        String code;

    }

    @Test
    public void enumOtherFieldTest() {

        MyParamTestEnumOtherField myParamTestEnumName;
        Throwable throwable = null;
        try {
            myParamTestEnumName = MyParamTestEnumOtherField.builder().code("拼多多").build();
            ValidateUtil.validate(myParamTestEnumName, ExceptionType.BIZ, true);
        } catch (Throwable t) {
            throwable = t;
        }
        Assertions.assertNotNull(throwable);
        Assertions.assertTrue(throwable instanceof BizEx);

        myParamTestEnumName = MyParamTestEnumOtherField.builder().code("京东").build();
        ValidateUtil.validate(myParamTestEnumName);

    }

    @Data
    @Builder
    static class MyParamTestEnumOtherField {

        @OfEnum(enumType = ChannelEnum.class, field = "display")
        String code;

    }


    @Test
    public void enumSelectKeysTest() {

        MyParamTestEnumSelectedKeys myParamTestEnumName;
        Throwable throwable = null;
        try {
            myParamTestEnumName = MyParamTestEnumSelectedKeys.builder().code("拼多多").build();
            ValidateUtil.validate(myParamTestEnumName, ExceptionType.BIZ, true);
        } catch (Throwable t) {
            throwable = t;
        }
        Assertions.assertNotNull(throwable);
        Assertions.assertTrue(throwable instanceof BizEx);

        throwable = null;
        try {
            myParamTestEnumName = MyParamTestEnumSelectedKeys.builder().code("京东").build();
            ValidateUtil.validate(myParamTestEnumName);
        } catch (Throwable t) {
            throwable = t;
        }
        Assertions.assertNotNull(throwable);

        myParamTestEnumName = MyParamTestEnumSelectedKeys.builder().code("阿里").build();
        ValidateUtil.validate(myParamTestEnumName);

    }

    @Data
    @Builder
    static class MyParamTestEnumSelectedKeys {

        @OfEnum(enumType = ChannelEnum.class, field = "display", selectedEnums = "ALI")
        String code;


    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class StockEntity {

        @ExactDivision
        long quantity;


        @ExactDivision("0.01")
        String value;

    }


    @Test
    public void shareTest() {
        StockEntity stockEntity = new StockEntity();
        ValidateUtil.validate(stockEntity);

        stockEntity.setQuantity(1050L);

        Throwable backup = null;
        try {
            ValidateUtil.validate(stockEntity);
        } catch (Throwable t) {
            backup = t;
        }
        Assertions.assertNotNull(backup);
        Assertions.assertTrue(backup instanceof BizEx);

        stockEntity.setQuantity(1000L);
        ValidateUtil.validate(stockEntity);


        StockEntity stockEntity1 = new StockEntity();
        stockEntity1.setValue("0.45");
        ValidateUtil.validate(stockEntity1);

        backup = null;
        stockEntity1.setValue("0.451");
        try {
            ValidateUtil.validate(stockEntity1);
        } catch (Throwable throwable) {
            backup = throwable;
        }
        Assertions.assertNotNull(backup);
        Assertions.assertTrue(backup instanceof BizEx);
    }


}
