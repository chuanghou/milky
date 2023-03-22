package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.common.tool.common.CacheConfig;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.domain.inventory.Inventory;
import com.stellariver.milky.demo.domain.item.repository.InventoryRepository;
import com.stellariver.milky.demo.infrastructure.database.entity.InventoryDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.InventoryDOMapper;
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
@CacheConfig
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryRepositoryImpl extends InventoryRepository {

    final InventoryDOMapper inventoryDOMapper;


    @Override
    public Map<Long, Inventory> queryMapByIdsFilterEmptyIdsAfterCache(Set<Long> itemIds) {
        List<InventoryDO> inventoryDOs = inventoryDOMapper.selectBatchIds(itemIds);
        return Collect.toMap(inventoryDOs, InventoryDO::getItemId,
                inventoryDO -> Inventory.builder().itemId(inventoryDO.getItemId()).amount(inventoryDO.getAmount()).build());
    }
}
