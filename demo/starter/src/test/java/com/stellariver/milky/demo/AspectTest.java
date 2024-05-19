package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.demo.adapter.controller.ItemController;
import com.stellariver.milky.demo.domain.item.Item;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@CustomLog
@Transactional
@SpringBootTest
@DirtiesContext
public class AspectTest {

    @Autowired
    ItemController  itemController;

    @Test
    public void testFullTextHandler() {
        Result<Item> hellQ = itemController.publish("HellQ");
        Assertions.assertFalse(hellQ.getSuccess());
        Assertions.assertEquals(hellQ.getMessage(), "时间区间不在可接收范围");
    }

}
