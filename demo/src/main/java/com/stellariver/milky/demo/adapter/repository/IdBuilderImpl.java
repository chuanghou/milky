package com.stellariver.milky.demo.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.demo.infrastructure.database.entity.IdBuilderDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.IdBuilderMapper;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import java.util.*;
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

    static final long NULL_HOLDER_OF_LONG = -1L;

    static final int NULL_PLACE_HOLDER_OF_INTEGER = -1;

    static final Integer MONTH = 1;

    static final Integer WEEK = 2;

    static final Integer DAY = 3;

    static final Set<Integer> SUPPORTABLE_DUTIES = new HashSet<>(Arrays.asList(MONTH, WEEK, DAY));

    final IdBuilderMapper idBuilderMapper;

    Pair<AtomicLong, Long> section;

    @Override
    public Long build(String nameSpace) {
        if (section == null || section.getLeft().get() >= section.getRight()) {
            section = buildSection(nameSpace);
        }
        return section.getLeft().getAndIncrement();
    }

    @Override
    public void reset(String nameSpace) {
        LambdaQueryWrapper<IdBuilderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IdBuilderDO::getNameSpace, nameSpace);
        IdBuilderDO idBuilderDO = idBuilderMapper.selectOne(wrapper);
        idBuilderDO.setUniqueId(NULL_HOLDER_OF_LONG);
    }

    private Pair<AtomicLong, Long> buildSection(String namespace) {
        int count;
        Pair<AtomicLong, Long> section;
        int times = 0;
        do {
            LambdaQueryWrapper<IdBuilderDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IdBuilderDO::getNameSpace, namespace);
            IdBuilderDO idBuilderDO = idBuilderMapper.selectOne(wrapper);

            if (reset(idBuilderDO)) {
                idBuilderDO.setUniqueId(NULL_HOLDER_OF_LONG);
            }
            Long start = idBuilderDO.getUniqueId();
            if (Objects.equals(idBuilderDO.getUniqueId(), NULL_HOLDER_OF_LONG)) {
                idBuilderDO.setUniqueId(start);
            }
            AtomicLong atomicStart = new AtomicLong(idBuilderDO.getUniqueId());
            long end = idBuilderDO.getUniqueId() + idBuilderDO.getStep();
            section = Pair.of(atomicStart, end);
            idBuilderDO.setUniqueId(idBuilderDO.getUniqueId() + idBuilderDO.getStep());
            count = idBuilderMapper.updateById(idBuilderDO);
            SysException.trueThrow(times++ > maxTimes, OPTIMISTIC_COMPETITION);
        } while (count < 1);
        return section;
    }

    private boolean reset(IdBuilderDO idBuilderDO) {
        if (!SUPPORTABLE_DUTIES.contains(idBuilderDO.getDuty())) {
            return false;
        }
        Date gmtModified = idBuilderDO.getGmtModified();
        Calendar modified = Calendar.getInstance();
        modified.setTime(gmtModified);

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        if (Objects.equals(idBuilderDO.getDuty(), MONTH)) {
            return modified.get(Calendar.MONTH) != now.get(Calendar.MONTH);
        } else if (Objects.equals(idBuilderDO.getDuty(), WEEK)) {
            return modified.get(Calendar.WEEK_OF_YEAR) != now.get(Calendar.WEEK_OF_YEAR);
        } else if (Objects.equals(idBuilderDO.getDuty(), DAY)) {
            return modified.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR);
        } else {
            return false;
        }

    }

}
