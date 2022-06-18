package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.base.MessageRecord;
import com.stellariver.milky.domain.support.context.Context;

import java.util.List;

public interface TraceRepository {

    default void batchInsert(List<MessageRecord> messages, Context context) {
        batchInsert(messages, context, true);
    }

    void batchInsert(List<MessageRecord> messages, Context context, boolean success);

    default void insert(Long invocationId, Context context) {
        insert(invocationId, context, true);
    }

    void insert(Long invocationId, Context context, boolean success);

}
