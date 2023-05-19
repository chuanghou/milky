package com.stellariver.milky.demo.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.demo.infrastructure.database.entity.IdBuilderDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.IdBuilderMapper;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.DuplicateKeyException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.stellariver.milky.common.tool.common.Kit.eq;
import static com.stellariver.milky.common.base.ErrorEnumsBase.*;
import static com.stellariver.milky.common.base.SysEx.*;

/**
 * @author houchuang
 */
@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdBuilder implements UniqueIdGetter {

    enum Duty {NOT_WORK, MONTH, WEEK, DAY}
    static final int maxTimes = 10;
    static long NULL_HOLDER_OF_LONG = -1L;

    static final Set<String> SUPPORTABLE_DUTIES = new HashSet<>(Arrays.asList(Duty.MONTH.name(), Duty.WEEK.name(), Duty.DAY.name()));

    final Sequence sequence;

    final IdBuilderMapper mapper;

    volatile Pair<AtomicLong, Long> section;

    static private final Executor executor = Executors.newSingleThreadExecutor();

    public IdBuilder(Sequence sequence, IdBuilderMapper mapper) {
        this.sequence = sequence;
        this.mapper = mapper;
    }

    public void initDB() {
        long ceiling = sequence.getCeiling() == null ? Long.MAX_VALUE : sequence.getCeiling();
        double alarmRatio = Kit.whenNull(sequence.getAlarmRatio(), 0.8);
        IdBuilderDO builderDO = IdBuilderDO.builder().nameSpace(sequence.getNameSpace())
                .start(sequence.getStart())
                .uniqueId(NULL_HOLDER_OF_LONG)
                .step(sequence.getStep())
                .ceiling(ceiling)
                .alarmThreshold((long) (ceiling * alarmRatio))
                .duty(sequence.getDuty().name())
                .build();
        int insert;
        try {
            insert = mapper.insert(builderDO);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new BizEx(DUPLICATE_NAME_SPACE);
        } catch (Throwable throwable) {
            throw new SysEx(ErrorEnumsBase.SYS_EX, throwable);
        }
        trueThrow(insert != 1, ErrorEnumsBase.SYS_EX);
    }

    private final BlockingQueue<Pair<AtomicLong, Long>> queue = new ArrayBlockingQueue<>(1);

    @SneakyThrows
    public Long get() {

        // init
        if (null == section) {
            synchronized (this) {
                if (null == section) {
                    loadSection();
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

            if (value >= section.getRight()) {
                synchronized (this) {
                    if (value >= section.getRight()) {
                        loadSection();
                    }
                }
            }

            trueThrow(times++ > maxTimes, OPTIMISTIC_COMPETITION);
        }while (true);
    }


    @SneakyThrows
    private void loadSection() {
        section = queue.take();
        CompletableFuture.runAsync(() -> {
            try {
                queue.put(doLoadSectionFromDB(sequence.getNameSpace()));
            } catch (Throwable throwable) {
                log.arg0(sequence.getNameSpace()).error("NEXT_LOAD_SECTION", throwable);
            }
        }, executor);
    }

    public void reset() {
        int count;
        int times = 0;
        do {
            LambdaQueryWrapper<IdBuilderDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IdBuilderDO::getNameSpace, sequence.getNameSpace());
            IdBuilderDO idBuilderDO = mapper.selectOne(wrapper);
            idBuilderDO.setUniqueId(NULL_HOLDER_OF_LONG);
            count = mapper.updateById(idBuilderDO);
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
            IdBuilderDO idBuilderDO = mapper.selectOne(wrapper);
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
            count = mapper.updateById(idBuilderDO);
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
