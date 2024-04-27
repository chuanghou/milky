package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.IterableResult;
import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.client.entity.ItemDTOIterableQuery;
import com.stellariver.milky.demo.client.service.ItemQueryService;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.infrastructure.database.DruidConfiguration;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@CustomLog
@SpringBootTest
public class PageQueryTest {


    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemQueryService itemQueryService;

    @Autowired
    private DruidConfiguration.MyDeepPageFilter myDeepPageFilter;

    @Test
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

}
