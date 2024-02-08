package com.stellariver.milky.demo;

import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;

import java.util.List;
import java.util.concurrent.Callable;

public class DemoScript implements Callable<String> {
    @Override
    public String call() throws Exception {
        ItemDOMapper itemDOMapper = BeanUtil.getBean(ItemDOMapper.class);
        List<ItemDO> itemDOS = itemDOMapper.selectList(null);
        return Json.toJson(itemDOS);
    }
}
