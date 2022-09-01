package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import lombok.NonNull;

import java.util.Optional;

public interface DAOWrapper<DataObject extends BaseDataObject<?>, PrimaryId> {

    @SuppressWarnings("unchecked")
    default void saveWrapper(Object dataObject) {
        SysException.nullThrow(dataObject);
        save((DataObject) dataObject);
    }

    void save(@NonNull DataObject dataObject);

    @SuppressWarnings("unchecked")
    default void updateWrapper(Object dataObject) {
        SysException.nullThrow(dataObject);
        update((DataObject) dataObject);
    }

    void update(@NonNull DataObject dataObject);

    @SuppressWarnings("unchecked")
    default Optional<DataObject> getByPrimaryIdWrapper(@NonNull Object primaryId) {
        return getByPrimaryId((PrimaryId) primaryId);
    }

    Optional<DataObject> getByPrimaryId(@NonNull PrimaryId primaryId);

    @SuppressWarnings("unchecked")
    default BaseDataObject<?> mergeWrapper(@NonNull Object priority, Object general) {
        if (general == null) {
            return (BaseDataObject<?>) priority;
        } else {
            return merge((DataObject) priority, (DataObject) general);
        }
    }

    DataObject merge(@NonNull DataObject priority, @NonNull DataObject general);

}
