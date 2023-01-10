package com.stellariver.milky.demo.adapter.rpc;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.client.service.ItemQueryService;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;

/**
 * @author houchuang
 */


public class ItemQueryServiceImpl implements ItemQueryService {

    ItemDOMapper itemDOMapper;

    @Override
    public Result<ItemDTO> queryItemDTO(Long itemId) {
        ItemDO itemDO = itemDOMapper.selectById(itemId);
        return Result.success(Kit.op(itemDO).map(Convertor.INST::to).orElse(null));
    }

}
