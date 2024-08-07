package com.stellariver.milky.demo;


import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.common.Typed;
import com.stellariver.milky.common.tool.test.ParameterMatcher;
import com.stellariver.milky.demo.basic.TypedEnums;
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
import com.stellariver.milky.demo.infrastructure.database.entity.DemoMetaUnit;
import com.stellariver.milky.demo.infrastructure.database.entity.UnitType;
import com.stellariver.milky.demo.infrastructure.database.mapper.DemoMetaUnitMapper;
import com.stellariver.milky.demo.infrastructure.database.mapper.InventoryDOMapper;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import com.stellariver.milky.domain.support.base.DomainTunnel;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import com.stellariver.milky.infrastructure.base.database.SqlLogFilter;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;

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

    @Autowired
    DataSource dataSource;

    @Autowired
    DomainTunnel domainTunnel;


    @Autowired
    DemoMetaUnitMapper demoMetaUnitMapper;

    @Autowired
    SqlLogFilter sqlLogFilter;

    @Test
    public void testFullTextHandler() {
        DemoMetaUnit build = DemoMetaUnit.builder()
                .name("test")
                .unitType(new UnitType("work"))
                .generatorType(Arrays.asList("111", "2211"))
                .province("test")
                .metaUnitId(1)
                .capacity("1")
                .sourceId(1)
                .build();

        demoMetaUnitMapper.insert(build);

        DemoMetaUnit demoMetaUnit = demoMetaUnitMapper.selectById(1);
        Assertions.assertEquals(build, demoMetaUnit);
        demoMetaUnit = SqlLogFilter.byPass(() -> demoMetaUnitMapper.selectById(1));
        log.info("ignore success");
        demoMetaUnit = demoMetaUnitMapper.selectById(1);
        demoMetaUnitMapper.deleteById(1);
    }

    @Test
    public void publishItemDOTest() {

        ItemCreateCommand itemCreateCommand = ItemCreateCommand.builder().itemId(1L).title("测试商品")
                .userId(10086L).amount(0L).storeCode("")
                .channelEnum(ChannelEnum.ALI)
                .build();
        HashMap<Class<? extends Typed<?>>, Object> parameters = new HashMap<>();
        parameters.put(TypedEnums.EMPLOYEE.class, new Employee("110", "小明"));
        CommandBus.accept(itemCreateCommand, parameters);


        Item item = domainTunnel.getByAggregateId(Item.class, "1");
        Assertions.assertNotNull(item);
        Inventory inventory = domainTunnel.getByAggregateId(Inventory.class, "1");
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
        item = domainTunnel.getByAggregateId(Item.class, "1");
        Assertions.assertNotNull(item);
        Assertions.assertEquals(100L, (long) item.getAmount());
        inventory = domainTunnel.getByAggregateId(Inventory.class, "1");
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


    @Test
    public void idBuilderTest() {
        UniqueIdGetter bean = BeanUtil.getBean(UniqueIdGetter.class);
        long l = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            bean.get();
        }
        System.out.println(System.nanoTime() - l);
    }
}
