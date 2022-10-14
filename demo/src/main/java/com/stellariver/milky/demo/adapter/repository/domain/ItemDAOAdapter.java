package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.infrastructure.database.enitity.ItemDO;
import com.stellariver.milky.domain.support.dependency.DataObjectInfo;
import com.stellariver.milky.domain.support.dependency.AggregateDaoAdapter;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemDAOAdapter implements AggregateDaoAdapter<Item> {

    @Override
    public Item toAggregate(@NonNull Object dataObject) {
        ItemDO itemDO = (ItemDO) dataObject;
        return Convertor.inst.to(itemDO);
    }

    @Override
    public Object toDataObject(Item item, DataObjectInfo dataObjectInfo) {
        return Convertor.inst.to(item);
    }

    @Override
    public DataObjectInfo dataObjectInfo(String aggregateId) {
        Long primaryId = Long.parseLong(aggregateId);
        return DataObjectInfo.builder().clazz(ItemDO.class).primaryId(primaryId).build();
    }

    @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public interface Convertor {

        Convertor inst = Mappers.getMapper(Convertor.class);

        @BeanMapping(builder = @Builder(disableBuilder = true))
        Item to(ItemDO itemDO);

        @BeanMapping(builder = @Builder(disableBuilder = true))
        ItemDO to(Item item);

    }


}
