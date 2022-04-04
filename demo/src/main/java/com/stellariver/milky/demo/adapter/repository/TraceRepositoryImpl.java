package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.demo.infrastructure.database.InvocationStoreDO;
import com.stellariver.milky.demo.infrastructure.database.InvocationStoreMapper;
import com.stellariver.milky.demo.infrastructure.database.MessageStoreDO;
import com.stellariver.milky.demo.infrastructure.database.MessageStoreMapper;
import com.stellariver.milky.domain.support.base.Message;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TraceRepositoryImpl implements TraceRepository {

    InvocationStoreMapper invocationStoreMapper;

    MessageStoreMapper messageStoreMapper;

    @Override
    public void insert(Long invocationId, Map<String, Object> parameters) {
        Employee operator = (Employee) parameters.get("operator");
        InvocationStoreDO invocationStoreDO = InvocationStoreDO.builder().id(invocationId)
                .operatorId(operator.getId())
                .operatorName(operator.getName())
                .build();
        invocationStoreMapper.insert(invocationStoreDO);
    }

    @Override
    public void batchInsert(List<Message> messages, Map<String, Object> metaData) {
         messages.stream().map(m -> MessageStoreDO.builder().id(m.getId())
                     .aggregateId(m.getAggregateId())
                     .className(m.getClass().getName())
                     .invocationId(m.getInvokeTrace().getInvocationId())
                     .triggerId(m.getInvokeTrace().getTriggerId())
                     .build()
                 )
                 .forEach(messageStoreMapper::insert);
    }
}
