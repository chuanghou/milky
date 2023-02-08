package com.stellariver.milky.demo.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.demo.infrastructure.database.entity.IdBuilderDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.IdBuilderMapper;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.dependency.NameSpaceParam;
import com.stellariver.milky.validate.tool.Validate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.CONFIG_ERROR;
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

    static final Set<Integer> SUPPORTABLE_DUTIES =
            new HashSet<>(Arrays.asList(Duty.MONTH.getCode(), Duty.WEEK.getCode(), Duty.DAY.getCode()));

    final IdBuilderMapper idBuilderMapper;

    Pair<AtomicLong, Long> section;

    @Override
    @Validate
    public void initNameSpace(NameSpaceParam param) {
        IdBuilderDO builderDO = IdBuilderDO.builder().nameSpace(param.getNameSpace())
                .start(param.getStart())
                .step(param.getStep())
                .duty(param.getDuty().getCode())
                .build();
        idBuilderMapper.insert(builderDO);
    }

    @Override
    public Long get(String nameSpace) {
        if (section == null) {
            section = loadSectionFromDB(nameSpace);
        }
        int times = 0;
        do {
            long value = section.getLeft().getAndIncrement();
            if (value < section.getRight()) {
                return value;
            }
            section = loadSectionFromDB(nameSpace);
            SysException.trueThrow(times++ > maxTimes, OPTIMISTIC_COMPETITION);
        }while (true);
    }

    @Override
    public void reset(String nameSpace) {
        int count;
        int times = 0;
        do {
            LambdaQueryWrapper<IdBuilderDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IdBuilderDO::getNameSpace, nameSpace);
            IdBuilderDO idBuilderDO = idBuilderMapper.selectOne(wrapper);
            idBuilderDO.setUniqueId(NULL_HOLDER_OF_LONG);
            count = idBuilderMapper.updateById(idBuilderDO);
            SysException.trueThrow(times++ > maxTimes, OPTIMISTIC_COMPETITION);
        }while (count < 1);
    }

    private Pair<AtomicLong, Long> loadSectionFromDB(String namespace) {
        int count;
        Pair<AtomicLong, Long> section;
        int times = 0;
        do {
            LambdaQueryWrapper<IdBuilderDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IdBuilderDO::getNameSpace, namespace);
            IdBuilderDO idBuilderDO = idBuilderMapper.selectOne(wrapper);
            SysException.nullThrow(idBuilderDO, CONFIG_ERROR.message("you haven't config you namespace " + namespace));
            if (autoResetQuestion(idBuilderDO)) {
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

    private boolean autoResetQuestion(IdBuilderDO idBuilderDO) {
        if (!SUPPORTABLE_DUTIES.contains(idBuilderDO.getDuty())) {
            return false;
        }
        Date gmtModified = idBuilderDO.getGmtModified();
        Calendar modified = Calendar.getInstance();
        modified.setTime(gmtModified);

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        if (Objects.equals(idBuilderDO.getDuty(), Duty.MONTH.getCode())) {
            return modified.get(Calendar.MONTH) != now.get(Calendar.MONTH);
        } else if (Objects.equals(idBuilderDO.getDuty(), Duty.WEEK.getCode())) {
            return modified.get(Calendar.WEEK_OF_YEAR) != now.get(Calendar.WEEK_OF_YEAR);
        } else if (Objects.equals(idBuilderDO.getDuty(), Duty.DAY.getCode())) {
            return modified.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR);
        } else {
            return false;
        }

    }

}
