package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import com.stellariver.milky.domain.support.dependency.DAOWrapper;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author houchuang
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDAOWrapper implements DAOWrapper<ItemDO, Long> {

    final ItemDOMapper itemDOMapper;

    @Override
    public int batchSave(@NonNull List<ItemDO> itemDOs) {
        return itemDOs.stream().map(itemDOMapper::insert).reduce(0, Integer::sum);
    }

    @Override
    public int batchUpdate(@NonNull List<ItemDO> itemDOs) {
        return itemDOs.stream().map(itemDOMapper::updateById).reduce(0, Integer::sum);
    }

    @Override
    public Map<Long, ItemDO> batchGetByPrimaryIds(@NonNull Set<Long> itemIds) {
        List<ItemDO> itemDOs = itemDOMapper.selectBatchIds(itemIds);
        return Collect.toMap(itemDOs, ItemDO::getItemId);
    }

    @Override
    public ItemDO merge(@NonNull ItemDO priority, @NonNull ItemDO original) {
        return Merger.INST.merge(priority, original);
    }

    @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public interface Merger {

        Merger INST = Mappers.getMapper(Merger.class);

        @BeanMapping(builder = @Builder(disableBuilder = true))
        ItemDO merge(ItemDO priority, @MappingTarget ItemDO original);

    }
}
