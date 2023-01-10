package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.demo.domain.inventory.Inventory;
import com.stellariver.milky.demo.infrastructure.database.entity.InventoryDO;
import com.stellariver.milky.domain.support.dependency.AggregateDaoAdapter;
import com.stellariver.milky.domain.support.dependency.DataObjectInfo;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * @author houchuang
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryDAOAdapter implements AggregateDaoAdapter<Inventory> {
    @Override
    public Inventory toAggregate(@NonNull Object dataObject) {
        InventoryDO inventoryDO = (InventoryDO) dataObject;
        return Inventory.builder().itemId(inventoryDO.getItemId())
                .amount(inventoryDO.getAmount()).storeCode(inventoryDO.getStoreCode()).build();
    }

    @Override
    public InventoryDO toDataObject(Inventory inventory, DataObjectInfo dataObjectInfo) {
        return InventoryDO.builder().itemId(inventory.getItemId())
                .amount(inventory.getAmount())
                .storeCode(inventory.getStoreCode())
                .build();
    }

    @Override
    public DataObjectInfo dataObjectInfo(String aggregateId) {
        Long primaryId = Long.parseLong(aggregateId);
        return DataObjectInfo.builder().clazz(InventoryDO.class).primaryId(primaryId).build();
    }

}
