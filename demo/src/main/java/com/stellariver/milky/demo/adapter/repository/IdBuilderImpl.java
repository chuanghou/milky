package com.stellariver.milky.demo.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stellariver.milky.demo.infrastructure.database.IdBuilderDO;
import com.stellariver.milky.demo.infrastructure.database.IdBuilderMapper;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdBuilderImpl implements IdBuilder {

    IdBuilderMapper idBuilderMapper;

    @Override
    public Long build(String nameSpace) {
        idBuilderMapper.insert(IdBuilderDO.builder().nameSpace(nameSpace).build());
        QueryWrapper<IdBuilderDO> wrapper = new QueryWrapper<>();
        wrapper.orderBy(true, false, "id").last("limit 1");
        IdBuilderDO idBuilderDO = idBuilderMapper.selectList(wrapper).get(0);
        return idBuilderDO.getId();
    }

}
