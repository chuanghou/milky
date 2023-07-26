package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.infrastructure.database.entity.InventoryDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.InventoryDOMapper;
import com.stellariver.milky.domain.support.dependency.DAOWrapper;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author houchuang
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryDAOWrapper implements DAOWrapper<InventoryDO, Long> {

    final InventoryDOMapper inventoryDOMapper;

    @Override
    public int batchSave(List<InventoryDO> inventoryDOs) {
        return inventoryDOs.stream().map(inventoryDOMapper::insert).reduce(0, Integer::sum);
    }

    @Override
    public int batchUpdate(List<InventoryDO> inventoryDOs) {
        return inventoryDOs.stream().map(inventoryDOMapper::updateById).reduce(0, Integer::sum);
    }

    @Override
    public Map<Long, InventoryDO> batchGetByPrimaryIds(@NonNull Set<Long> itemIds) {
        List<InventoryDO> inventoryDOs = inventoryDOMapper.selectBatchIds(itemIds);
        return Collect.toMap(inventoryDOs, InventoryDO::getItemId);
    }

}
