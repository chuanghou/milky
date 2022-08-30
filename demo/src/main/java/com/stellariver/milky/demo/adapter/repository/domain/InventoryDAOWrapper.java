package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.demo.infrastructure.database.InventoryDO;
import com.stellariver.milky.demo.infrastructure.database.InventoryDOMapper;
import com.stellariver.milky.domain.support.dependency.DAOWrapper;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryDAOWrapper implements DAOWrapper<InventoryDO, Long> {

    final InventoryDOMapper inventoryDOMapper;

    @Override
    public void save(@NonNull InventoryDO inventoryDO) {
        inventoryDOMapper.insert(inventoryDO);
    }

    @Override
    public void update(@NonNull InventoryDO inventoryDO) {
        inventoryDOMapper.insert(inventoryDO);
    }

    @Override
    public Optional<InventoryDO> getByPrimaryId(@NonNull Long primaryId) {
        InventoryDO inventoryDO = inventoryDOMapper.selectById(primaryId);
        return Kit.op(inventoryDO);
    }

    @Override
    public InventoryDO merge(@NonNull InventoryDO priority, @NonNull InventoryDO general) {
        return null;
    }


}
