package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import com.stellariver.milky.domain.support.util.AsyncExecutor;
import com.stellariver.milky.domain.support.event.EventBus;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MilkySupport {

    ConcurrentOperate concurrentOperate;

    EventBus eventBus;

    TraceRepository traceRepository;

    AsyncExecutor asyncExecutor;

}