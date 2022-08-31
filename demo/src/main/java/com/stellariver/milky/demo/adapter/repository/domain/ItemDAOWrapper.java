package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.demo.infrastructure.database.InventoryDO;
import com.stellariver.milky.demo.infrastructure.database.InventoryDOMapper;
import com.stellariver.milky.demo.infrastructure.database.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.ItemDOMapper;
import com.stellariver.milky.domain.support.dependency.DAOWrapper;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDAOWrapper implements DAOWrapper<ItemDO, Long> {

    final ItemDOMapper itemDOMapper;

    @Override
    public void save(@NonNull ItemDO itemDO) {
        itemDOMapper.insert(itemDO);
    }

    @Override
    public void update(@NonNull ItemDO itemDO) {
        itemDOMapper.updateById(itemDO);
    }

    @Override
    public Optional<ItemDO> getByPrimaryId(@NonNull Long primaryId) {
        ItemDO itemDO = itemDOMapper.selectById(primaryId);
        return Kit.op(itemDO);
    }

    @Override
    public ItemDO merge(@NonNull ItemDO priority, @NonNull ItemDO general) {
        return Merger.inst.merge(priority, general);
    }


    public interface Merger {

        Merger inst = Mappers.getMapper(Merger.class);

        @BeanMapping(builder = @Builder(disableBuilder = true))
        ItemDO merge(ItemDO priority, @MappingTarget ItemDO general);

    }
}
