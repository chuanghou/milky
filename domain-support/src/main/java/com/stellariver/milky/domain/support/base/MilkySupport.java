package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.domain.support.dependency.BeanLoader;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.domain.support.util.AsyncExecutorService;
import com.stellariver.milky.domain.support.event.EventBus;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MilkySupport {

    ConcurrentOperate concurrentOperate;

    EventBus eventBus;

    AsyncExecutorService asyncExecutorService;

    BeanLoader beanLoader;

}