package com.stellariver.milky.demo.adapter.rpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stellariver.milky.common.base.IteratableResult;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.client.entity.ItemDTOPageQuery;
import com.stellariver.milky.demo.client.service.ItemQueryService;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */


@Service
@RequiredArgsConstructor
public class ItemQueryServiceImpl implements ItemQueryService {


    final ItemDOMapper itemDOMapper;

    Function<String, Long> nexKeyService = Long::parseLong;

    @Override
    public Result<ItemDTO> queryItemDTO(Long itemId) {
        ItemDO itemDO = itemDOMapper.selectById(itemId);
        return Result.success(Kit.op(itemDO).map(Convertor.INST::to).orElse(null));
    }

    @Override
    public IteratableResult<ItemDTO> pageQueryItemDTO(ItemDTOPageQuery query) {
        LambdaQueryWrapper<ItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemDO::getUserId, query.getUserId());
        wrapper.orderBy(true, false, ItemDO::getItemId);
//        Long count = itemDOMapper.selectCount(wrapper);
        if (StringUtils.isNotBlank(query.getNexPageKey())) {
            Long lastMinItemId = nexKeyService.apply(query.getNexPageKey());
            wrapper.lt(true, ItemDO::getItemId, lastMinItemId);
        }
        wrapper.last("limit " + query.getPageSize());
        List<ItemDO> itemDOS = itemDOMapper.selectList(wrapper);
        List<ItemDTO> itemDTOs = itemDOS.stream().map(Convertor.INST::to).collect(Collectors.toList());
        if (Collect.isNotEmpty(itemDTOs)) {
            String nextPageKey = itemDOS.get(itemDOS.size() - 1).getItemId().toString();
            return IteratableResult.success(itemDTOs, nextPageKey);
        } else {
            return IteratableResult.empty();
        }
    }


}
