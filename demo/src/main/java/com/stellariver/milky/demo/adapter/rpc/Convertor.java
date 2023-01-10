package com.stellariver.milky.demo.adapter.rpc;

import com.stellariver.milky.demo.client.entity.ItemDTO;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import org.mapstruct.factory.Mappers;

/**
 * @author houchuang
 */
public interface Convertor {

    Convertor INST = Mappers.getMapper(Convertor.class);

    ItemDTO to(ItemDO itemDO);

}
