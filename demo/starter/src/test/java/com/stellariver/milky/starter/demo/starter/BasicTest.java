package com.stellariver.milky.starter.demo.starter;


import com.stellariver.milky.common.tool.TestReset;
import com.stellariver.milky.demo.basic.TypedEnums;
import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.test.ParameterMatcher;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.domain.inventory.Inventory;
import com.stellariver.milky.demo.domain.inventory.command.InventoryUpdateCommand;
import com.stellariver.milky.demo.domain.item.CombineItem;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.CombineItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemTitleUpdateCommand;
import com.stellariver.milky.demo.domain.item.repository.CombineItemRepository;
import com.stellariver.milky.demo.domain.item.repository.InventoryRepository;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.demo.domain.item.repository.UserInfoRepository;
import com.stellariver.milky.demo.domain.service.ItemCreatedMessage;
import com.stellariver.milky.demo.domain.service.MqService;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.InventoryDOMapper;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import com.stellariver.milky.common.tool.common.Typed;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import lombok.CustomLog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

@CustomLog
@Transactional
@SpringBootTest
@DirtiesContext
public class BasicTest {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    InventoryDOMapper inventoryDOMapper;

    @Autowired
    UserInfoRepository userInfoRepository;

    @SpyBean
    ConcurrentOperate concurrentOperate;

    @SpyBean
    MqService mqService;

    @Autowired
    CombineItemRepository combineItemRepository;

    @Autowired
    ItemDOMapper itemDOMapper;

    @Test
    public void publishItemDOTest() {

        ItemCreateCommand itemCreateCommand = ItemCreateCommand.builder().itemId(1L).title("测试商品")
                .userId(10086L).amount(0L).storeCode("")
                .channelEnum(ChannelEnum.ALI)
                .build();
        HashMap<Class<? extends Typed<?>>, Object> parameters = new HashMap<>();
        parameters.put(TypedEnums.EMPLOYEE.class, new Employee("110", "小明"));
        CommandBus.accept(itemCreateCommand, parameters);

        Item item = itemRepository.queryById(1L);
        Assertions.assertNotNull(item);
        Inventory inventory = inventoryRepository.queryById(1L);
        Assertions.assertNotNull(inventory);
        Assertions.assertEquals(item.getStoreCode(), inventory.getStoreCode());
        Assertions.assertEquals(item.getAmount(), inventory.getAmount());
        Assertions.assertEquals(item.getUserName(), "小明");
        ItemCreatedMessage message = ItemCreatedMessage.builder().itemId(1L).build();

        verify(mqService).sendMessage(argThat(new ParameterMatcher<>(message)));

        // 通过ParameterMeter实现对于null字段的验证过滤
        ItemCreatedMessage message1 = message.toBuilder().title("测试商品").build();
        verify(mqService).sendMessage(argThat(new ParameterMatcher<>(message1)));

        InventoryUpdateCommand command = InventoryUpdateCommand.builder().itemId(1L).updateAmount(100L).build();
        CommandBus.accept(command, parameters);
        item = itemRepository.queryById(1L);
        Assertions.assertNotNull(item);
        Assertions.assertEquals(100L, (long) item.getAmount());
        inventory = inventoryRepository.queryById(1L);
        Assertions.assertNotNull(inventory);
        Assertions.assertEquals(100L, (long) inventory.getAmount());

        ItemTitleUpdateCommand updateCommand = ItemTitleUpdateCommand.builder().itemId(1L)
                .updateTitle("new Title").build();
        Context context = (Context) CommandBus.accept(updateCommand, parameters);
        Long before = context.getMetaData(TypedEnums.MARK_BEFORE.class);
        Long handle = context.getMetaData(TypedEnums.MARK_HANDLE.class);
        Long after = context.getMetaData(TypedEnums.MARK_AFTER.class);
        Assertions.assertNotNull(before);
        Assertions.assertNotNull(handle);
        Assertions.assertNotNull(after);
        Assertions.assertTrue(before < handle);
        Assertions.assertTrue(handle < after);

        List<ItemDO> itemDOS = itemDOMapper.selectList(null);

        log.arg0("1").arg1(233).error("test");

    }

    @Test
    public void combineItemTest() {
        CombineItemCreateCommand combineItemCreateCommand = CombineItemCreateCommand.builder().itemId(1L).title("测试商品")
                .userId(10086L).amount(0L).storeCode("")
                .channelEnum(ChannelEnum.ALI)
                .ratio(1025L)
                .build();
        HashMap<Class<? extends Typed<?>>, Object> parameters = new HashMap<>();
        parameters.put(TypedEnums.EMPLOYEE.class, new Employee("110", "小明"));
        CommandBus.accept(combineItemCreateCommand, parameters);

        CombineItem combineItem = combineItemRepository.queryById(1L);
        Assertions.assertNotNull(combineItem);
        Assertions.assertEquals(combineItem.getRatio(), 1025L);

        ItemTitleUpdateCommand updateCommand = ItemTitleUpdateCommand.builder().itemId(1L)
                .updateTitle("new Title").build();
        CommandBus.accept(updateCommand, parameters);
        combineItem = combineItemRepository.queryById(1L);
        Assertions.assertNotNull(combineItem);
        Assertions.assertEquals(combineItem.getTitle(), "new Title");
    }

    @AfterAll
    static public void resetMilky() {
        CommandBus.reset();
        TestReset.reset();
    }

}
