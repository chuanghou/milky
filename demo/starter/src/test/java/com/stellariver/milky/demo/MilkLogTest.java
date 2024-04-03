package com.stellariver.milky.demo;


import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import lombok.CustomLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@CustomLog
@SpringBootTest
public class MilkLogTest {

    @Autowired
    ItemDOMapper itemDOMapper;

    @Test
    public void testMilkyLog() {
        ItemDO itemDO = ItemDO.builder().userId(9000L)
                .itemId(100000L).title("myTitle")
                .amount(1000L).channelEnum(ChannelEnum.JD).storeCode("jd").price("78").userName("78").build();
        itemDOMapper.insert(itemDO);
        itemDOMapper.selectList(null);
    }

}
