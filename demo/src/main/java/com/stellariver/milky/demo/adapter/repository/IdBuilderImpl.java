package com.stellariver.milky.demo.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.demo.infrastructure.database.entity.IdBuilderDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.IdBuilderMapper;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.dependency.Sequence;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.*;
import static com.stellariver.milky.common.tool.exception.SysException.*;

/**
 * @author houchuang
 */
@CustomLog
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdBuilderImpl implements IdBuilder {

    static final int maxTimes = 10;

    static final Set<String> SUPPORTABLE_DUTIES = new HashSet<>(Arrays.asList(Duty.MONTH.name(), Duty.WEEK.name(), Duty.DAY.name()));

    final IdBuilderMapper idBuilderMapper;

    volatile Pair<AtomicLong, Long> section;

    @Override
    public void initNameSpace(Sequence param) {
        long ceiling = param.getCeiling() == null ? Long.MAX_VALUE : param.getCeiling();
        double alarmRatio = Kit.whenNull(param.getAlarmRatio(), 0.8);
        IdBuilderDO builderDO = IdBuilderDO.builder().nameSpace(param.getNameSpace())
                .start(param.getStart())
                .uniqueId(NULL_HOLDER_OF_LONG)
                .step(param.getStep())
                .ceiling(ceiling)
                .alarmThreshold((long) (ceiling * alarmRatio))
                .duty(param.getDuty().name())
                .build();
        int insert;
        try {
            insert = idBuilderMapper.insert(builderDO);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new BizException(DUPLICATE_NAME_SPACE);
        } catch (Throwable throwable) {
            throw new SysException(ErrorEnumsBase.SYSTEM_EXCEPTION, throwable);
        }
        trueThrow(insert != 1, ErrorEnumsBase.SYSTEM_EXCEPTION);
    }

    @Override
    public Long get(String nameSpace) {
        if (section == null) {
            loadSectionFromDB(nameSpace);
        }
        int times = 0;
        do {
            long value = section.getLeft().getAndIncrement();
            if (value < section.getRight()) {
                return value;
            }
            section = null;
            loadSectionFromDB(nameSpace);
            trueThrow(times++ > maxTimes, OPTIMISTIC_COMPETITION);
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
            trueThrow(times++ > maxTimes, OPTIMISTIC_COMPETITION);
        }while (count < 1);
    }

    private void loadSectionFromDB(String namespace) {
        if (null == section) {
            synchronized (this) {
                if (null == section) {
                    section = doLoadSectionFromDB(namespace);
                }
            }
        }
    }

    private Pair<AtomicLong, Long> doLoadSectionFromDB(String namespace) {
        int count;
        Pair<AtomicLong, Long> section;
        int times = 0;
        do {
            LambdaQueryWrapper<IdBuilderDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IdBuilderDO::getNameSpace, namespace);
            IdBuilderDO idBuilderDO = idBuilderMapper.selectOne(wrapper);
            nullThrow(idBuilderDO, CONFIG_ERROR.message("you haven't config you namespace " + namespace));
            if (autoReset(idBuilderDO)) {
                idBuilderDO.setUniqueId(NULL_HOLDER_OF_LONG);
            }
            Long start = idBuilderDO.getUniqueId();
            if (Objects.equals(idBuilderDO.getUniqueId(), NULL_HOLDER_OF_LONG)) {
                idBuilderDO.setUniqueId(start);
            }
            AtomicLong atomicStart = new AtomicLong(idBuilderDO.getUniqueId());
            long tail = idBuilderDO.getUniqueId() + idBuilderDO.getStep();
            section = Pair.of(atomicStart, tail);
            if (tail > idBuilderDO.getAlarmThreshold()) {
                log.arg0(idBuilderDO).error("LOAD_NEXT_SECTION_ALARM");
                trueThrow(tail > idBuilderDO.getCeiling(), LOAD_NEXT_SECTION_LIMIT);
            }
            idBuilderDO.setUniqueId(idBuilderDO.getUniqueId() + idBuilderDO.getStep());
            count = idBuilderMapper.updateById(idBuilderDO);
            trueThrow(times++ > maxTimes, OPTIMISTIC_COMPETITION);
        } while (count < 1);
        return section;
    }

    private boolean autoReset(IdBuilderDO idBuilderDO) {
        if (!SUPPORTABLE_DUTIES.contains(idBuilderDO.getDuty())) {
            return false;
        }
        Date gmtModified = idBuilderDO.getGmtModified();
        Calendar modified = Calendar.getInstance();
        modified.setTime(gmtModified);

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        if (Kit.eq(idBuilderDO.getDuty(), Duty.MONTH.name())) {
            return modified.get(Calendar.MONTH) != now.get(Calendar.MONTH);
        } else if (Kit.eq(idBuilderDO.getDuty(), Duty.WEEK.name())) {
            return modified.get(Calendar.WEEK_OF_YEAR) != now.get(Calendar.WEEK_OF_YEAR);
        } else if (Kit.eq(idBuilderDO.getDuty(), Duty.DAY.name())) {
            return modified.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR);
        } else {
            return false;
        }

    }

}
