package com.stellariver.milky.domain.support.base;

/**
 * @author houchuang
 */
public interface BaseDataObject<PrimaryId> {

    /**
     * get data object primaryId
     * @return PrimaryId
     */
    PrimaryId getPrimaryId();

}
