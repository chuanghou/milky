package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.demo.infrastructure.database.InvocationStoreDO;
import com.stellariver.milky.demo.infrastructure.database.InvocationStoreMapper;
import com.stellariver.milky.demo.infrastructure.database.MessageStoreMapper;
import com.stellariver.milky.domain.support.base.MessageRecord;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.dependency.TraceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TraceRepositoryImpl implements TraceRepository {

    InvocationStoreMapper invocationStoreMapper;

    MessageStoreMapper messageStoreMapper;

    @Override
    public void insert(Long invocationId, Context context, boolean success) {
    }

    @Override
    public void batchInsert(List<MessageRecord> messageRecords, Context context) {

    }

}
