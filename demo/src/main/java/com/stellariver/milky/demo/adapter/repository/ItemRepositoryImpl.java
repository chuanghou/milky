package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.demo.infrastructure.database.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.ItemDOMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRepositoryImpl extends ItemRepository {

    final ItemDOMapper itemDOMapper;

    @Override
    public Map<Long, Item> queryMapByIdsFilterEmptyIdsAfterCache(Set<Long> itemIds) {
        List<ItemDO> itemDOs = itemDOMapper.selectBatchIds(itemIds);
        return Collect.toMap(itemDOs, ItemDO::getItemId,
                itemDO -> Item.builder().itemId(itemDO.getItemId()).title(itemDO.getTitle()).amount(itemDO.getAmount()).build());
    }
}
