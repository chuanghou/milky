package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.demo.infrastructure.database.InvocationStoreDO;
import com.stellariver.milky.demo.infrastructure.database.InvocationStoreMapper;
import com.stellariver.milky.demo.infrastructure.database.MessageStoreDO;
import com.stellariver.milky.demo.infrastructure.database.MessageStoreMapper;
import com.stellariver.milky.domain.support.base.Message;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TraceRepositoryImpl implements TraceRepository {

    InvocationStoreMapper invocationStoreMapper;

    MessageStoreMapper messageStoreMapper;

    @Override
    public void insert(Long invocationId, Context context) {
        Employee operator = (Employee) context.getMetaData("operator");
        InvocationStoreDO invocationStoreDO = InvocationStoreDO.builder().id(invocationId)
                .operatorId(operator.getId())
                .operatorName(operator.getName())
                .operatorSource((String) context.getMetaData("operatorSource"))
                .build();
        invocationStoreMapper.insert(invocationStoreDO);
    }

    @Override
    public void batchInsert(List<Message> messages, Context context) {
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
