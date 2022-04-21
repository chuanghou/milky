package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.domain.support.context.DependencyPrepares;
import com.stellariver.milky.domain.support.dependency.BeanLoader;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.domain.support.dependency.DomainRepository;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.interceptor.Interceptors;
import com.stellariver.milky.domain.support.util.AsyncExecutor;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MilkySupport {

    ConcurrentOperate concurrentOperate;

    TraceRepository traceRepository;

    AsyncExecutor asyncExecutor;

    List<DependencyPrepares> dependencyPrepares;

    List<Interceptors> interceptors;

    List<EventRouters> eventRouters;

    List<DomainRepository<?>> domainRepositories;

    BeanLoader beanLoader;

}