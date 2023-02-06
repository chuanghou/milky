package com.stellariver.milky.domain.support.dependency;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.stellariver.milky.common.tool.exception.ErrorEnumsBase;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.ErrorEnums;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */
public interface DAOWrapper<DataObject extends BaseDataObject<?>, PrimaryId> {

    class CheckNull {

        private static final Map<Class<?>, List<Pair<MethodAccess, Integer>>> gettersMap = new ConcurrentHashMap<>();

        public static boolean isGetter(String name) {
            return (name.startsWith("get") && name.length() > 3) || (name.startsWith("is") && name.length() > 2);
        }

        public static void checkNullField(List<Object> objects) throws SysException {
            Class<?> clazz = objects.get(0).getClass();
            List<Pair<MethodAccess, Integer>> getters = gettersMap.get(clazz);
            if (getters == null) {
                getters = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.getParameterTypes().length == 0 && isGetter(m.getName()))
                        .map(method -> {
                            MethodAccess methodAccess = MethodAccess.get(clazz);
                            int methodIndex = methodAccess.getIndex(method.getName(), method.getParameterTypes());
                            return Pair.of(methodAccess, methodIndex);
                        }).collect(Collectors.toList());
                gettersMap.put(clazz, getters);
            }
            for (Object object : objects) {
                getters.forEach(getter -> {
                    MethodAccess methodAccess = getter.getLeft();
                    Integer index = getter.getRight();
                    Object value = methodAccess.invoke(object, index);
                    SysException.nullThrowGet(value, () -> ErrorEnums.FIELD_IS_NULL.message(object));
                });
            }
        }

    }

    @SuppressWarnings("unchecked")
    default void batchSaveWrapper(List<Object> dataObjects) {
        if (Collect.isEmpty(dataObjects)) { return; }
        CheckNull.checkNullField(dataObjects);
        int count = batchSave(Collect.transfer(dataObjects, doj -> (DataObject) doj));
        SysException.trueThrow(Kit.notEq(count, dataObjects.size()), ErrorEnums.PERSISTENCE_ERROR);
    }

    int batchSave(@NonNull List<DataObject> dataObjects);

    @SuppressWarnings("unchecked")
    default void batchUpdateWrapper(List<Object> dataObjects) {
        if (Collect.isEmpty(dataObjects)) { return; }
        CheckNull.checkNullField(dataObjects);
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
