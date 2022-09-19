package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.infrastructure.database.InventoryDO;
import com.stellariver.milky.demo.infrastructure.database.InventoryDOMapper;
import com.stellariver.milky.domain.support.dependency.DAOWrapper;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryDAOWrapper implements DAOWrapper<InventoryDO, Long> {

    final InventoryDOMapper inventoryDOMapper;

    @Override
    public void batchSave(@NonNull List<InventoryDO> inventoryDOs) {
        inventoryDOs.forEach(inventoryDOMapper::insert);
    }

    @Override
    public void batchUpdate(@NonNull List<InventoryDO> inventoryDOs) {
        inventoryDOs.forEach(inventoryDOMapper::updateById);
    }

    @Override
    public Map<Long, InventoryDO> batchGetByPrimaryIds(@NonNull Set<Long> itemIds) {
        List<InventoryDO> inventoryDOs = inventoryDOMapper.selectBatchIds(itemIds);
        return Collect.toMap(inventoryDOs, InventoryDO::getItemId);
    }

    @Override
    public InventoryDO merge(@NonNull InventoryDO priority, @NonNull InventoryDO general) {
        return Merger.inst.merge(priority, general);
    }

    @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public interface Merger {

        Merger inst = Mappers.getMapper(Merger.class);

        @BeanMapping(builder = @Builder(disableBuilder = true))
        InventoryDO merge(InventoryDO priority, @MappingTarget InventoryDO general);

    }
}
