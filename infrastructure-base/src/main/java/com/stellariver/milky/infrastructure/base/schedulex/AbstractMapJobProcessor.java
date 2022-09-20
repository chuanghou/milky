package com.stellariver.milky.infrastructure.base.schedulex;

import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.MapJobProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.LogChoice;
import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.common.tool.common.SystemClock;
import com.stellariver.milky.common.tool.util.Json;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

@CustomLog
abstract public class AbstractMapJobProcessor<T> extends MapJobProcessor {

    @Override
    public ProcessResult process(JobContext jobContext) throws InterruptedException {
        ScheduleConfig scheduleConfig;
        String jobParameters = jobContext.getJobParameters();
        if (StringUtils.isNotBlank(jobParameters)) {
            scheduleConfig = Json.parse(jobParameters, ScheduleConfig.class);
        } else {
            scheduleConfig = getScheduleConfig();
        }
        if (scheduleConfig == null || Boolean.FALSE.equals(scheduleConfig.getEnable())) {
            return new ProcessResult(true);
        }

        if (isRootTask(jobContext)) {
            Long start = scheduleConfig.getStartIndex();
            while (start < scheduleConfig.getStartIndex() + scheduleConfig.getTotal()) {
                Long end = start + scheduleConfig.getPageSize();
                ScheduleParam scheduleParam = ScheduleParam.builder().start(start).end(end)
                        .ds(scheduleConfig.getDs()).metadata(scheduleConfig.getMetadata()).build();
                map(Collections.singletonList(scheduleParam), this.getClass().getName());
                start = start + scheduleConfig.getPageSize();
            }
        } else {
            ScheduleParam param = (ScheduleParam) jobContext.getTask();
            List<T> ts = selectByParam(param);
            for (T t: ts) {
                Throwable throwable = null;
                long now = SystemClock.now();
                try {
                    interceptBeforeTask(t, param);
                    processTask(t, param);
                    interceptAfterTask(t, param);
                } catch (InterruptedException interruptedException) {
                    throw interruptedException;
                } catch (Throwable th) {
                    whenFail(t, param, th);
                    throwable = th;
                } finally {
                    interceptAfterTaskFinally(t, param);
                    log.arg0(t).arg1(param).cost(SystemClock.now() - now);
                    if (Kit.eq(logChoice(), LogChoice.ALWAYS)) {
                        log.log(this.getClass().getSimpleName(), throwable);
                    } else if (Kit.eq(logChoice(), LogChoice.EXCEPTION)) {
                        log.logWhenException(this.getClass().getSimpleName(), throwable);
                    } else {
                        log.error("UNREACHED_PART", throwable);
                    }
                }
            }
        }
        return new ProcessResult(true);
    }

    abstract public List<T> selectByParam (ScheduleParam param);

    abstract public void processTask (T t, ScheduleParam param) throws InterruptedException;

    public void whenFail (T t, ScheduleParam param, Throwable throwable) {}

    public ScheduleConfig getScheduleConfig() {
        throw new SysException("It should be implemented by its sub class!");
    }

    public LogChoice logChoice() {
        return LogChoice.ALWAYS;
    }

    public void interceptBeforeTask(T t, ScheduleParam param) {

    }

    public void interceptAfterTask(T t, ScheduleParam param) {

    }

    public void interceptAfterTaskFinally(T t, ScheduleParam param) {

    }

}
