package com.stellariver.milky.demo;


import com.stellariver.milky.demo.adapter.controller.ItemController;
import lombok.CustomLog;
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
        itemController.publish("HellQ");
    }

}
