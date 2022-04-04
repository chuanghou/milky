package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.base.Message;
import com.stellariver.milky.domain.support.invocation.Invocation;

import java.util.List;
import java.util.Map;

public interface TraceRepository {

    void insert(Long invocationId, Map<String, Object> parameters);

    void batchInsert(List<Message> messages, Map<String, Object> metaData);

}
