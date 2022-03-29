package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.domain.support.depend.BeanLoader;
import com.stellariver.milky.domain.support.depend.ConcurrentOperate;
import com.stellariver.milky.domain.support.event.AsyncExecutorService;
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