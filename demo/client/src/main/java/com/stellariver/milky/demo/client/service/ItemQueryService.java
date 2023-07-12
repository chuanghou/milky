package com.stellariver.milky.demo.client.service;

import com.stellariver.milky.common.base.IteratableResult;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.client.entity.ItemDTOIterableQuery;

/**
 * @author houchuang
 */
public interface ItemQueryService {

    Result<ItemDTO> queryItemDTO(Long itemId);

    IteratableResult<ItemDTO> pageQueryItemDTO(ItemDTOIterableQuery query);
}
