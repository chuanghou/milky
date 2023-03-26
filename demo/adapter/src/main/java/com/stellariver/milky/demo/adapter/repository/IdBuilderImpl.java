package com.stellariver.milky.demo.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.exception.SysEx;
import com.stellariver.milky.demo.infrastructure.database.entity.IdBuilderDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.IdBuilderMapper;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.dependency.Sequence;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.stellariver.milky.common.tool.common.Kit.eq;
import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.*;
import static com.stellariver.milky.common.tool.exception.SysEx.*;

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

    static private final Executor executor = Executors.newSingleThreadExecutor();

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
            throw new BizEx(DUPLICATE_NAME_SPACE);
        } catch (Throwable throwable) {
            throw new SysEx(ErrorEnumsBase.SYSTEM_EXCEPTION, throwable);
        }
        trueThrow(insert != 1, ErrorEnumsBase.SYSTEM_EXCEPTION);
    }

    static Lock lock = new ReentrantLock();

    private final BlockingQueue<Pair<AtomicLong, Long>> queue = new ArrayBlockingQueue<>(1);

    @Override
    @SneakyThrows
    public Long get(String nameSpace) {

        // init
        if (null == section) {
            synchronized (this) {
                if (null == section) {
                    section = doLoadSectionFromDB(nameSpace);
                    CompletableFuture.runAsync(() -> {
                        try {
                            queue.put(doLoadSectionFromDB(nameSpace));
                        } catch (InterruptedException ignored) {}
                    }, executor);
                }
            }
        }

        // get
        int times = 0;
        do {
            long value = section.getLeft().getAndIncrement();
            if (value < section.getRight()) {
                return value;
            }

            if (section.getLeft().get() >= section.getRight()) {
                synchronized (this) {
                    if (section.getLeft().get() >= section.getRight()) {
                        section = queue.take();
                        CompletableFuture.runAsync(() -> {
                            try {
                                queue.put(doLoadSectionFromDB(nameSpace));
                            } catch (InterruptedException ignore) {}
                        }, executor);
                    }
                }
            }

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
            if (eq(idBuilderDO.getUniqueId(), NULL_HOLDER_OF_LONG)) {
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

        if (eq(idBuilderDO.getDuty(), Duty.MONTH.name())) {
            return modified.get(Calendar.MONTH) != now.get(Calendar.MONTH);
        } else if (eq(idBuilderDO.getDuty(), Duty.WEEK.name())) {
            return modified.get(Calendar.WEEK_OF_YEAR) != now.get(Calendar.WEEK_OF_YEAR);
        } else if (eq(idBuilderDO.getDuty(), Duty.DAY.name())) {
            return modified.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR);
        } else {
            return false;
        }

    }

}