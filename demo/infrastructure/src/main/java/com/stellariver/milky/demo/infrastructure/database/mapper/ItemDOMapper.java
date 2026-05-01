package com.stellariver.milky.demo.infrastructure.database.mapper;

import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.infrastructure.base.database.MilkyBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;

/**
 * @author houchuang
 */
@Mapper
public interface ItemDOMapper extends MilkyBaseMapper<ItemDO> {

    int deleteByIdReally(Serializable id);

    /**
     * Where is there a SuppressWarnings?
     * Answer: the package configuration couldn't be recognized!
     * @param id id
     * @return itemDO
     */
    @SuppressWarnings("all")
    ItemDO selectByIdIncludeDeleted(Serializable id);

}
