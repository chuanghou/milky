package com.stellariver.milky.demo;


import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.demo.basic.ErrorEnum;
import com.stellariver.milky.demo.basic.NameTypes;
import com.stellariver.milky.demo.domain.inventory.Inventory;
import com.stellariver.milky.demo.domain.inventory.command.InventoryUpdateCommand;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.repository.InventoryRepository;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.demo.infrastructure.database.mapper.InventoryDOMapper;
import com.stellariver.milky.domain.support.base.NameType;
import com.stellariver.milky.domain.support.command.CommandBus;
import lombok.CustomLog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;

@CustomLog
@Transactional
@SpringBootTest
public class MilkyDemoTest {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    ItemRepository itemRepository;

    @MockBean
    InventoryDOMapper inventoryDOMapper;

    @Test
    public void publishItemDOTest() {
        ItemCreateCommand itemCreateCommand = ItemCreateCommand.builder().itemId(1L).title("测试商品")
                .userId(10086L).amount(0L).storeCode("")
                .build();
        HashMap<NameType<?>, Object> parameters = new HashMap<>();
        parameters.put(NameTypes.employee, new Employee("110", "小明"));
        CommandBus.accept(itemCreateCommand, parameters);
        Item item = itemRepository.queryById(1L);
        Assertions.assertNotNull(item);
        Inventory inventory = inventoryRepository.queryById(1L);
        Assertions.assertNotNull(inventory);
        Assertions.assertEquals(item.getStoreCode(), inventory.getStoreCode());
        Assertions.assertEquals(item.getAmount(), inventory.getAmount());
        InventoryUpdateCommand command = InventoryUpdateCommand.builder().itemId(1L).updateAmount(100L).build();
        CommandBus.accept(command, parameters);
        item = itemRepository.queryById(1L);
        Assertions.assertNotNull(item);
        Assertions.assertEquals(100L, (long) item.getAmount());
        inventory = inventoryRepository.queryById(1L);
        Assertions.assertNotNull(inventory);
        Assertions.assertEquals(100L, (long) inventory.getAmount());
    }

    @Test
    public void transactionTest() {
        ItemCreateCommand itemCreateCommand = ItemCreateCommand.builder().itemId(1L).title("测试商品")
                .userId(10086L).amount(0L).storeCode("")
                .build();
        HashMap<NameType<?>, Object> parameters = new HashMap<>();
        parameters.put(NameTypes.employee, new Employee("110", "小明"));

        Mockito.when(inventoryDOMapper.insert(Mockito.any())).thenThrow(new BizException(ErrorEnum.MOCK_EXCEPTION));
        Throwable throwable = null;
        try {
            CommandBus.acceptMemoryTransactional(itemCreateCommand, parameters);
        } catch (Throwable t) {
            throwable = t;
        }
        Assertions.assertNotNull(throwable);
        Assertions.assertTrue(throwable instanceof BizException);
        Optional<Item> item = itemRepository.queryByIdOptional(1L);

        Assertions.assertFalse(item.isPresent());
        boolean present = inventoryRepository.queryByIdOptional(1L).isPresent();
        Assertions.assertFalse(present);
    }



}
