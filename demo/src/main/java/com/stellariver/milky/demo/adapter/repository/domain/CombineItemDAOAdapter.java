package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.demo.domain.item.CombineItem;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.domain.support.dependency.AggregateDaoAdapter;
import com.stellariver.milky.domain.support.dependency.DataObjectInfo;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author houchuang
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CombineItemDAOAdapter implements AggregateDaoAdapter<CombineItem> {

    @Override
    public CombineItem toAggregate(@NonNull Object dataObject) {
        ItemDO itemDO = (ItemDO) dataObject;
        return Convertor.INST.to(itemDO);
    }

    @Override
    public Object toDataObject(CombineItem item, DataObjectInfo dataObjectInfo) {
        return Convertor.INST.to(item);
    }

    @Override
    public DataObjectInfo dataObjectInfo(String aggregateId) {
        Long primaryId = Long.parseLong(aggregateId);
        return DataObjectInfo.builder().clazz(ItemDO.class).primaryId(primaryId).build();
    }


    @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public interface Convertor {

        Convertor INST = Mappers.getMapper(Convertor.class);

        @BeanMapping(builder = @Builder(disableBuilder = true))
        CombineItem to(ItemDO itemDO);


        @BeanMapping(builder = @Builder(disableBuilder = true))
        ItemDO to(CombineItem item);


    }
}
