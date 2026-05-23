package com.stellariver.milky.demo.adapter.rpc;

import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;


/**
 * @author houchuang
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface Convertor {

    Convertor INST = Mappers.getMapper(Convertor.class);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    ItemDTO to(ItemDO itemDO);

}
