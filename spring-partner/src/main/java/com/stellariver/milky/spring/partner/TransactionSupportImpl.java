package com.stellariver.milky.spring.partner;

import com.stellariver.milky.domain.support.dependency.TransactionSupport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author houchuang
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBean(DataSourceTransactionManager.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionSupportImpl implements TransactionSupport {

    final DataSourceTransactionManager dataSourceTransactionManager;

    static ThreadLocal<TransactionStatus> transStatusTL = new ThreadLocal<>();

    @Override
    public void begin() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(def);
        transStatusTL.set(transactionStatus);
    }

    @Override
    public void commit() {
        TransactionStatus transactionStatus = transStatusTL.get();
        dataSourceTransactionManager.commit(transactionStatus);
        transStatusTL.remove();
    }

    @Override
    public void rollback() {
        TransactionStatus transactionStatus = transStatusTL.get();
        if (transactionStatus != null) {
            dataSourceTransactionManager.rollback(transactionStatus);
            transStatusTL.remove();
        }
    }

}
