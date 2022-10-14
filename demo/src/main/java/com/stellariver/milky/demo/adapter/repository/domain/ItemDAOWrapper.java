package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.demo.basic.ErrorEnum;
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

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDAOWrapper implements DAOWrapper<ItemDO, Long> {

    final ItemDOMapper itemDOMapper;

    @Override
    public void batchSave(@NonNull List<ItemDO> itemDOs) {
        itemDOs.forEach(itemDO -> {
            int count = itemDOMapper.insert(itemDO);
            SysException.falseThrowGet(Kit.eq(count, 1), () -> ErrorEnum.SYSTEM_EXCEPTION.message(itemDO));
        });
    }

    @Override
    public void batchUpdate(@NonNull List<ItemDO> itemDOs) {
        itemDOs.forEach(itemDO -> {
            int count = itemDOMapper.updateById(itemDO);
            SysException.falseThrowGet(Kit.eq(count, 1), () -> ErrorEnum.SYSTEM_EXCEPTION.message(itemDO));
        });
    }

    @Override
    public Map<Long, ItemDO> batchGetByPrimaryIds(@NonNull Set<Long> itemIds) {
        List<ItemDO> itemDOs = itemDOMapper.selectBatchIds(itemIds);
        return Collect.toMap(itemDOs, ItemDO::getItemId);
    }

    @Override
    public ItemDO merge(@NonNull ItemDO priority, @NonNull ItemDO general) {
        return Merger.inst.merge(priority, general);
    }

    @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public interface Merger {

        Merger inst = Mappers.getMapper(Merger.class);

        @BeanMapping(builder = @Builder(disableBuilder = true))
        ItemDO merge(ItemDO priority, @MappingTarget ItemDO general);

    }
}
