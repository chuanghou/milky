package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.demo.infrastructure.database.mapper.IdBuilderMapper;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.dependency.Sequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IdBuilderTest{

    @Autowired
    IdBuilder idBuilder;

    @Autowired
    @SuppressWarnings("all")
    IdBuilderMapper idBuilderMapper;


    @Test
    public void testIdBuilder() {
        Sequence sequence = Sequence.builder().nameSpace("my_test")
                .duty(IdBuilder.Duty.NOT_WORK.name()).step(100).start(1L).build();
        idBuilder.initNameSpace(sequence);


        sequence = Sequence.builder().nameSpace("my_test")
                .duty(IdBuilder.Duty.NOT_WORK.name()).step(100).start(1L).build();
        Throwable backUp = null;
        try {
            idBuilder.initNameSpace(sequence);
        } catch (Throwable throwable) {
            backUp = throwable;
        }
        Assertions.assertTrue(backUp instanceof BizEx);
        String code = ((BizEx) backUp).getFirstError().getCode();
        Assertions.assertEquals(code, ErrorEnumsBase.DUPLICATE_NAME_SPACE.getCode());

    }


}
