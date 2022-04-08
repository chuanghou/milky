package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.base.Message;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.invocation.Invocation;

import java.util.List;
import java.util.Map;

public interface TraceRepository {

    void insert(Long invocationId, Context context);

    void batchInsert(List<Message> messages,Context context);

}
