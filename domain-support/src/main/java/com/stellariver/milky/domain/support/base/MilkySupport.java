package com.stellariver.milky.domain.support.base;

import com.stellariver.milky.common.tool.common.BeanLoader;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.domain.support.dependency.*;
import com.stellariver.milky.domain.support.event.EventRouters;
import com.stellariver.milky.domain.support.interceptor.Interceptors;
import com.stellariver.milky.domain.support.util.ThreadLocalTransferableExecutor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;

/**
 * @author houchuang
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MilkySupport {

    ConcurrentOperate concurrentOperate;

    TraceRepository traceRepository;

    ThreadLocalTransferableExecutor threadLocalTransferableExecutor;

    List<Interceptors> interceptors;

    List<EventRouters> eventRouters;

    List<DaoAdapter<?>> daoAdapters;

    List<DAOWrapper<? extends BaseDataObject<?>, ?>> daoWrappers;

    Reflections reflections;

    BeanLoader beanLoader;

    TransactionSupport transactionSupport;

    public MilkySupport(ConcurrentOperate concurrentOperate,
                        TraceRepository traceRepository,
                        ThreadLocalTransferableExecutor threadLocalTransferableExecutor,
                        List<Interceptors> interceptors,
                        List<EventRouters> eventRouters,
                        List<DaoAdapter<?>> daoAdapters,
                        List<DAOWrapper<? extends BaseDataObject<?>, ?>> daoWrappers,
                        Reflections reflections,
                        BeanLoader beanLoader,
                        TransactionSupport transactionSupport) {
        this.concurrentOperate = concurrentOperate;
        this.traceRepository = traceRepository;
        this.threadLocalTransferableExecutor = threadLocalTransferableExecutor;
        this.interceptors = Kit.op(interceptors).orElseGet(ArrayList::new);
        this.eventRouters = Kit.op(eventRouters).orElseGet(ArrayList::new);
        this.daoAdapters = Kit.op(daoAdapters).orElseGet(ArrayList::new);
        this.daoWrappers = Kit.op(daoWrappers).orElseGet(ArrayList::new);
        this.reflections = reflections;
        this.beanLoader = beanLoader;
        this.transactionSupport = transactionSupport;
    }
}