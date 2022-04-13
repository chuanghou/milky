package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.base.Message;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.invocation.Invocation;

import java.util.List;
import java.util.Map;

public interface TraceRepository {

    default void insert(Long invocationId, Context context) {
        insert(invocationId, context, true);
    }

    void batchInsert(List<Message> messages,Context context);

    void insert(Long invocationId, Context context, boolean success);

}
