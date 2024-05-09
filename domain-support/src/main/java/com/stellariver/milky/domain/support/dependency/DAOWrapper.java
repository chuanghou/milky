package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import lombok.NonNull;

import java.util.*;

import static com.stellariver.milky.common.base.ErrorEnumsBase.PERSISTENCE_ERROR;

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
    default void batchDeleteWrapper(List<Object> dataObjects) {
        int count = batchDelete(Collect.transfer(dataObjects, doj -> (DataObject) doj));
        SysEx.trueThrow(Kit.notEq(count, dataObjects.size()), PERSISTENCE_ERROR);
    }

    int batchDelete(List<DataObject> dataObjects);

}
