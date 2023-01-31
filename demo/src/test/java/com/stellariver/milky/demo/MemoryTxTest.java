package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.basic.TypedEnums;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.repository.InventoryRepository;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.demo.infrastructure.database.entity.InventoryDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.InventoryDOMapper;
import com.stellariver.milky.domain.support.base.Typed;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.TransactionSupport;
import lombok.CustomLog;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

import java.util.HashMap;
import java.util.Optional;

@CustomLog
@SpringBootTest
public class MemoryTxTest {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    InventoryDOMapper inventoryDOMapper;

    @Autowired
    TransactionSupport transactionSupport;

    @Test
    public void transactionTest() {
        LogUtil.setLogLevel(MemoryTxTest.class, Level.OFF);
        ItemCreateCommand itemCreateCommand = ItemCreateCommand.builder().itemId(1L).title("测试商品")
                .userId(10086L).amount(0L).storeCode("")
                .channelEnum(ChannelEnum.JD)
                .build();
        HashMap<Typed<?>, Object> parameters = new HashMap<>();
        parameters.put(TypedEnums.employee, new Employee("110", "小明"));
        Throwable throwable = null;
        InventoryDO test = InventoryDO.builder().itemId(1L).amount(30L).storeCode("test").build();
        inventoryDOMapper.insert(test);
        try {
            CommandBus.acceptMemoryTransactional(itemCreateCommand, parameters);
        } catch (Throwable t) {
            throwable = t;
            log.error("TEST_THROWABLE", throwable);
        }
        Assertions.assertNotNull(throwable);
        Assertions.assertTrue(throwable instanceof DuplicateKeyException);
        Optional<Item> item = itemRepository.queryByIdOptional(1L);

        Assertions.assertFalse(item.isPresent());
        LogUtil.setLogLevel(MemoryTxTest.class, Level.WARN);
    }

}
