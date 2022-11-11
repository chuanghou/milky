package com.stellariver.milky.demo.adapter.rpc;

import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import org.mapstruct.factory.Mappers;

public interface Convertor {

    Convertor inst = Mappers.getMapper(Convertor.class);

    ItemDTO to(ItemDO itemDO);

}
