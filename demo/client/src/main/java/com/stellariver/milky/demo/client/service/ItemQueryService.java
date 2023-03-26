package com.stellariver.milky.demo.client.service;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.demo.client.entity.ItemDTO;

/**
 * @author houchuang
 */
public interface ItemQueryService {

    Result<ItemDTO> queryItemDTO(Long itemId);

}
