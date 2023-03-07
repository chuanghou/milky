package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysEx;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import lombok.NonNull;

import java.util.*;

import static com.stellariver.milky.common.tool.exception.ErrorEnumsBase.PERSISTENCE_ERROR;

/**
 * @author houchuang
 */
public interface DAOWrapper<DataObject extends BaseDataObject<?>, PrimaryId> {

    @SuppressWarnings("unchecked")
    default void batchSaveWrapper(List<Object> dataObjects) {
        int count = batchSave(Collect.transfer(dataObjects, doj -> (DataObject) doj));
        SysEx.trueThrow(Kit.notEq(count, dataObjects.size()), PERSISTENCE_ERROR);
    }

    int batchSave(List<DataObject> dataObjects);

    @SuppressWarnings("unchecked")
    default void batchUpdateWrapper(List<Object> dataObjects) {
        int count = batchUpdate(Collect.transfer(dataObjects, doj -> (DataObject) doj));
        SysEx.trueThrow(Kit.notEq(count, dataObjects.size()), PERSISTENCE_ERROR);
    }

    int batchUpdate(List<DataObject> dataObjects);

    @SuppressWarnings("unchecked")
    default Map<Object, Object> batchGetByPrimaryIdsWrapper(@NonNull Set<Object> primaryIds) {
        Set<PrimaryId> tempPrimaryIds = Collect.transfer(primaryIds, primaryId -> (PrimaryId) primaryId, HashSet::new);
        Map<PrimaryId, DataObject> map = batchGetByPrimaryIds(tempPrimaryIds);
        return new HashMap<>(map);
    }

    Map<PrimaryId, DataObject> batchGetByPrimaryIds(@NonNull Set<PrimaryId> primaryIds);

    default Optional<Object> getByPrimaryIdOptionalWrapper(Object primaryId) {
        Map<Object, Object> map = batchGetByPrimaryIdsWrapper(Collect.asSet(primaryId));
        return Kit.op(map.get(primaryId));
    }

    @SuppressWarnings("unchecked")
    default BaseDataObject<?> mergeWrapper(Object priority, Object original) {
        return original == null ? (BaseDataObject<?>) priority : merge((DataObject) priority, (DataObject) original);
    }

    DataObject merge(DataObject priority, @NonNull DataObject original);

}
