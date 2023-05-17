package com.stellariver.milky.infrastructure.base.tablestore;

import com.alicloud.openservices.tablestore.model.ColumnValue;
import com.alicloud.openservices.tablestore.model.search.query.*;
import com.alicloud.openservices.tablestore.model.search.sort.FieldSort;
import com.alicloud.openservices.tablestore.model.search.sort.Sort;
import com.alicloud.openservices.tablestore.model.search.sort.SortOrder;
import com.google.common.base.Strings;
import com.stellariver.milky.common.base.TSSearch;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.base.SysEx;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */
public class TableStoreTool {

    public static BoolQuery buildQuery(Object query) {
        Field[] fields = query.getClass().getDeclaredFields();
        List<Query> queries = Arrays.stream(fields)
                .filter(field -> !Objects.equals(field.getName(), "orderConditions"))
                .map(field -> build(field, query))
                .filter(Objects::nonNull).collect(Collectors.toList());
        BoolQuery boolQuery = new BoolQuery();
        boolQuery.setMustQueries(queries);
        return boolQuery;
    }

    public static Sort buildSort(List<OrderCondition> orderConditions) {
        List<Sort.Sorter> fieldSorters = orderConditions.stream()
                .map(oC -> new FieldSort(oC.getField(),
                        Kit.eq(oC.getOrder(), Order.DESC) ? SortOrder.DESC : SortOrder.ASC))
                .collect(Collectors.toList());
        return new Sort(fieldSorters);
    }

    @Nullable
    @SneakyThrows
    @SuppressWarnings("all")
    private static Query build(Field field, Object queryVO) {
        TSSearch annotation = field.getAnnotation(TSSearch.class);
        if (annotation == null || annotation.index() == null) {
            return null;
        }
        String index = Strings.isNullOrEmpty(annotation.index()) ? field.getName() : annotation.index();
        field.setAccessible(true);
        Object value = field.get(queryVO);

        if (value == null) {
            return null;
        }

        if (annotation.like()) {
            SysEx.falseThrow(value instanceof String, ErrorEnumsBase.PARAM_FORMAT_WRONG.message("like only support String field!"));
            Query query = QueryBuilders.matchPhrase(index, (String) value).build();
            return doBuildQuery(index, query);
        } else {
            if (value instanceof List) {
                List<?> valueList = (List<?>) value;
                return buildQuery(index, valueList.toArray(new Object[0]));
            }
            return buildQuery(index, value);
        }
    }

    private static Query buildQuery(String index, Object... values) {
        List<ColumnValue> columnValues = Arrays.stream(values).map(TableStoreTool::buildColumnValue).collect(Collectors.toList());
        TermsQuery termsQuery = new TermsQuery();
        termsQuery.setFieldName(index);
        columnValues.forEach(termsQuery::addTerm);
        return doBuildQuery(index, termsQuery);
    }

    static final private String SEPARATOR = ".";

    private static Query doBuildQuery(String index, Query query) {
        if (index.contains(SEPARATOR)) {
            String[] paths = index.split("\\.");
            BizEx.trueThrow(paths.length != 2, ErrorEnumsBase.SYS_EX.message("search param size is not 2!"));
            String path = paths[0];
            NestedQuery nestedQuery = new NestedQuery();
            nestedQuery.setPath(path);
            nestedQuery.setQuery(query);
            nestedQuery.setScoreMode(ScoreMode.None);
            return nestedQuery;
        }
        return query;
    }

    public static ColumnValue buildColumnValue(Object fieldValue) {
        ColumnValue columnValue;
        if (fieldValue instanceof Double) {
            columnValue = ColumnValue.fromDouble((Double) fieldValue);
        } else if (fieldValue instanceof Boolean) {
            columnValue = ColumnValue.fromBoolean((Boolean) fieldValue);
        } else if ((fieldValue instanceof Integer) || (fieldValue instanceof Long)) {
            columnValue = ColumnValue.fromLong(((Number) fieldValue).longValue());
        } else if (fieldValue instanceof String) {
            columnValue = ColumnValue.fromString((String) fieldValue);
        } else {
            throw new SysEx(fieldValue);
        }
        return columnValue;
    }

}
