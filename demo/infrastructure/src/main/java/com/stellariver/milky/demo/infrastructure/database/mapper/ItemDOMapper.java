package com.stellariver.milky.demo.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;

/**
 * @author houchuang
 */
@Mapper
public interface ItemDOMapper extends BaseMapper<ItemDO> {

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
