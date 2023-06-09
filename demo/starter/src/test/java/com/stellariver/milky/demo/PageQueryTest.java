package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.PageResult;
import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.client.entity.ItemDTOPageQuery;
import com.stellariver.milky.demo.client.service.ItemQueryService;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@CustomLog
@Transactional
@SpringBootTest
public class PageQueryTest {


    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemQueryService itemQueryService;

    @Test
    public void testPageQuery() {

        Long itemId = 1L;
        ItemDO.ItemDOBuilder<?, ?> builder = ItemDO.builder().title("test").userId(10001L).channelEnum(ChannelEnum.JD)
                .price("231").amount(1000L).storeCode("JD").userName("Tom");
        for (int i = 0; i < 100; i++) {
            ItemDO itemDO = builder.itemId(itemId++).build();
            itemDOMapper.insert(itemDO);
            builder = itemDO.toBuilder();
        }

        ItemDTOPageQuery query = ItemDTOPageQuery.builder().userId(10001L).pageSize(10L).build();

        PageResult<ItemDTO> itemDTOPageResult = itemQueryService.pageQueryItemDTO(query);

        query.setNexPageKey(itemDTOPageResult.getNextPageKey());

        itemDTOPageResult = itemQueryService.pageQueryItemDTO(query);

        Optional<Long> reduce = itemDTOPageResult.getData().stream().map(ItemDTO::getItemId).reduce(Long::sum);
        Assertions.assertTrue(reduce.isPresent());
        Assertions.assertEquals(reduce.get(), (81 + 90) * 10 / 2);

    }

}
