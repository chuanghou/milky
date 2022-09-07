package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.common.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import lombok.NonNull;

import java.util.*;

public interface DAOWrapper<DataObject extends BaseDataObject<?>, PrimaryId> {

    @SuppressWarnings("unchecked")
    default void batchSaveWrapper(List<Object> dataObjects) {
        if (Collect.isNotEmpty(dataObjects)) {
            batchSave(Collect.transfer(dataObjects, doj -> (DataObject) doj));
        }
    }

    void batchSave(@NonNull List<DataObject> dataObjects);

    @SuppressWarnings("unchecked")
    default void batchUpdateWrapper(@NonNull List<Object> dataObjects) {
        if (Collect.isNotEmpty(dataObjects)) {
            batchUpdate(Collect.transfer(dataObjects, doj -> (DataObject) doj));
        }
    }

    void batchUpdate(@NonNull List<DataObject> dataObjects);

    @SuppressWarnings("unchecked")
    default HashMap<Object, Object> batchGetByPrimaryIdWrapper(@NonNull Set<Object> primaryIds) {
        HashSet<PrimaryId> tempPrimaryIds= Collect.transfer(primaryIds, primaryId -> (PrimaryId) primaryId, HashSet::new);
        Map<PrimaryId, DataObject> map = batchGetByPrimaryIds(tempPrimaryIds);
        return new HashMap<>(map);
    }

    Map<PrimaryId, DataObject> batchGetByPrimaryIds(@NonNull Set<PrimaryId> primaryIds);


    default Optional<Object> getByPrimaryIdOptionalWrapper(Object primaryId) {
        Map<Object, Object> map = batchGetByPrimaryIdWrapper(Collect.asSet(primaryId));
        return Kit.op(map.get(primaryId));
    }

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
