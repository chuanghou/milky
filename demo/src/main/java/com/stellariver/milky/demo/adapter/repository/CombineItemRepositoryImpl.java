package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.tool.common.CacheConfig;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.domain.item.CombineItem;
import com.stellariver.milky.demo.domain.item.repository.CombineItemRepository;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author houchuang
 */
@Repository
@RequiredArgsConstructor
@CacheConfig
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CombineItemRepositoryImpl extends CombineItemRepository {

    final ItemDOMapper itemDOMapper;

    @Override
    public Map<Long, CombineItem> queryMapByIdsFilterEmptyIdsAfterCache(Set<Long> itemIds) {
        List<ItemDO> itemDOs = itemDOMapper.selectBatchIds(itemIds);
        return Collect.toMap(itemDOs, ItemDO::getItemId,
                itemDO -> CombineItem.builder().itemId(itemDO.getItemId())
                        .ratio(itemDO.getRatio()).title(itemDO.getTitle()).amount(itemDO.getAmount()).build());
    }

}
