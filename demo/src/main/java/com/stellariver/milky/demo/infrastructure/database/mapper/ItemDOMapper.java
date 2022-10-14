package com.stellariver.milky.demo.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stellariver.milky.demo.infrastructure.database.enitity.ItemDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemDOMapper extends BaseMapper<ItemDO> {
}
