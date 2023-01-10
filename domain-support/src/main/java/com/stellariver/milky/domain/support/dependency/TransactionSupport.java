package com.stellariver.milky.domain.support.dependency;

/**
 * @author houchuang
 */
public interface TransactionSupport {

    void begin();

    void commit();

    void rollback();

}
