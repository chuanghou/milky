package com.demo;


import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.demo.MilkyDemoApplication;
import com.stellariver.milky.demo.basic.NameTypes;
import com.stellariver.milky.demo.domain.inventory.Inventory;
import com.stellariver.milky.demo.domain.inventory.command.InventoryUpdateCommand;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.repository.InventoryRepository;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.domain.support.base.NameType;
import com.stellariver.milky.domain.support.command.CommandBus;
import org.apache.catalina.core.ApplicationContext;
import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MilkyDemoApplication.class)
public class MilkyDemoTest {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void publishItemDOTest() {
        ItemCreateCommand itemCreateCommand = ItemCreateCommand.builder().itemId(1L).title("测试商品").userId(10086L).build();
        HashMap<NameType<?>, Object> parameters = new HashMap<>();
        parameters.put(NameTypes.employee, new Employee("110", "小明"));
        CommandBus.accept(itemCreateCommand, parameters);
        Item item = itemRepository.queryById(1L);
        Assert.assertNotNull(item);
        Inventory inventory = inventoryRepository.queryById(1L);
        Assert.assertNotNull(inventory);
        Assert.assertEquals(item.getStoreCode(), inventory.getStoreCode());
        Assert.assertEquals(item.getAmount(), inventory.getAmount());
        InventoryUpdateCommand command = InventoryUpdateCommand.builder().itemId(1L).updateAmount(100L).build();
        CommandBus.accept(command, parameters);
        item = itemRepository.queryById(1L);
        Assert.assertNotNull(item);
        Assert.assertEquals(100L, (long) item.getAmount());
        inventory = inventoryRepository.queryById(1L);
        Assert.assertNotNull(inventory);
        Assert.assertEquals(100L, (long) inventory.getAmount());
    }

}
