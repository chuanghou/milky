package com.stellariver.milky.demo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stellariver.milky.common.base.IterableResult;
import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.client.entity.ItemDTOIterableQuery;
import com.stellariver.milky.demo.client.service.ItemQueryService;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.infrastructure.database.DruidConfiguration;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import com.stellariver.milky.infrastructure.base.database.CursorOptions;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@CustomLog
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class PageQueryTest {


    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemQueryService itemQueryService;

    @Autowired
    private DruidConfiguration.MyDeepPageFilter myDeepPageFilter;

    @Test
    @Order(1)
    public void testPageQuery() {

        long itemId = 1L;
        ItemDO.ItemDOBuilder<?, ?> builder = ItemDO.builder().title("test").userId(10001L).channelEnum(ChannelEnum.JD)
                .price("231").amount(1000L).storeCode("JD").userName("Tom");
        for (int i = 0; i < 100; i++) {
            ItemDO itemDO = builder.itemId(itemId++).build();
            itemDOMapper.insert(itemDO);
            builder = itemDO.toBuilder();
        }

        ItemDTOIterableQuery query = ItemDTOIterableQuery.builder().userId(10001L).pageSize(10).build();

        IterableResult<ItemDTO> itemDTOIterableResult = itemQueryService.pageQueryItemDTO(query);

        query.setNextPageKey(itemDTOIterableResult.getNextPageKey());

        itemDTOIterableResult = itemQueryService.pageQueryItemDTO(query);

        Optional<Long> reduce = itemDTOIterableResult.getData().stream().map(ItemDTO::getItemId).reduce(Long::sum);
        Assertions.assertTrue(reduce.isPresent());
        Assertions.assertEquals(reduce.get(), (81 + 90) * 10 / 2);


        query = ItemDTOIterableQuery.builder().userId(10001L).pageSize(110000).build();

        itemQueryService.pageQueryItemDTO(query);
        Assertions.assertEquals(myDeepPageFilter.getTriggerTimes(), 1);
    }


    @Test
    @Order(2)
    public void testCursorConsume() {

        long itemId = 1L;
        ItemDO.ItemDOBuilder<?, ?> builder = ItemDO.builder().title("test").userId(10001L).channelEnum(ChannelEnum.JD)
                .price("231").amount(1000L).storeCode("JD").userName("Tom");
        for (int i = 0; i < 100; i++) {
            ItemDO itemDO = builder.itemId(itemId++).build();
            itemDOMapper.insert(itemDO);
            builder = itemDO.toBuilder();
        }

        AtomicInteger count = new AtomicInteger();
        itemDOMapper.cursorConsumer(
                itemDO -> count.incrementAndGet(),
                CursorOptions.of(LambdaQueryWrapper::new, ItemDO::getItemId)
                        .batchSize(2)
                        .cursorColumn("item_id"));
        Assertions.assertEquals(100, count.get());

    }

}
