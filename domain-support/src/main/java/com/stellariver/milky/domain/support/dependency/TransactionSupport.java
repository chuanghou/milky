package com.stellariver.milky.domain.support.dependency;

public interface TransactionSupport {

    void begin();

    void commit();

    void rollback();

}
