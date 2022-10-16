package com.stellariver.milky.demo.infrastructure.database;

import com.stellariver.milky.domain.support.dependency.TransactionSupport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionSupportImpl implements TransactionSupport {

    final PlatformTransactionManager platformTransactionManager;

    @Override
    public void begin() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }
}
