package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;

public interface DAOWrapper<DataObject extends BaseDataObject<?>, PrimaryId> {

    @SneakyThrows
    default void checkNullField(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object o = field.get(obj);
            SysException.nullThrowGet(o, () -> {
                String message = String.format("class %s, field %s", obj.getClass().getSimpleName(), field.getName());
                return ErrorEnumsBase.FIELD_IS_NULL.message(message);
            });
        }
    }

    @SuppressWarnings("unchecked")
    default void batchSaveWrapper(List<Object> dataObjects) {
        SysException.trueThrow(Collect.isEmpty(dataObjects), ErrorEnums.SYSTEM_EXCEPTION);
        dataObjects.forEach(this::checkNullField);
        int count = batchSave(Collect.transfer(dataObjects, doj -> (DataObject) doj));
        SysException.trueThrow(Kit.notEq(count, dataObjects.size()), ErrorEnums.PERSISTENCE_ERROR);
    }

    int batchSave(@NonNull List<DataObject> dataObjects);

    @SuppressWarnings("unchecked")
    default void batchUpdateWrapper(List<Object> dataObjects) {
        SysException.trueThrow(Collect.isEmpty(dataObjects), ErrorEnums.SYSTEM_EXCEPTION);
        dataObjects.forEach(this::checkNullField);
        int count = batchUpdate(Collect.transfer(dataObjects, doj -> (DataObject) doj));
        SysException.trueThrow(Kit.notEq(count, dataObjects.size()), ErrorEnums.PERSISTENCE_ERROR);
    }

    int batchUpdate(@NonNull List<DataObject> dataObjects);

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
    default BaseDataObject<?> mergeWrapper(@NonNull Object priority, Object general) {
        if (general == null) {
            return (BaseDataObject<?>) priority;
        } else {
            return merge((DataObject) priority, (DataObject) general);
        }
    }

    DataObject merge(@NonNull DataObject priority, @NonNull DataObject general);

}
