package com.stellariver.milky.demo.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.demo.infrastructure.database.entity.IdBuilderDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.IdBuilderMapper;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicLong;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.OPTIMISTIC_COMPETITION;

/**
 * @author houchuang
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdBuilderImpl implements IdBuilder {

    static final int maxTimes = 10;

    final IdBuilderMapper idBuilderMapper;

    Pair<AtomicLong, Long> section;

    @Override
    public Long build(String nameSpace) {
        if (section == null || section.getLeft().get() >= section.getRight()) {
            section = buildSection(nameSpace);
        }
        return section.getLeft().getAndIncrement();
    }

    private Pair<AtomicLong, Long> buildSection(String namespace) {
        int count;
        Pair<AtomicLong, Long> pair;
        int times = 0;
        do {
            LambdaQueryWrapper<IdBuilderDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IdBuilderDO::getNameSpace, namespace);
            IdBuilderDO idBuilderDO = idBuilderMapper.selectOne(wrapper);
            Long start = idBuilderDO.getUniqueId();
            pair = Pair.of(new AtomicLong(start), start + idBuilderDO.getStep());
            idBuilderDO.setUniqueId(idBuilderDO.getUniqueId() + idBuilderDO.getStep());
            count = idBuilderMapper.updateById(idBuilderDO);
            SysException.trueThrow(times++ > maxTimes, OPTIMISTIC_COMPETITION);
        } while (count < 1);
        return pair;
    }

}
