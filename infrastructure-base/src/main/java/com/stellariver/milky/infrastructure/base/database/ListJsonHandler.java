package com.stellariver.milky.infrastructure.base.database;

import com.stellariver.milky.common.tool.common.Kit;
import com.stellariver.milky.common.tool.util.Json;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hou Chuana
 */
public abstract class ListJsonHandler<T> extends BaseTypeHandler<List<T>> {

    private final Class<T> clazz;

    @SuppressWarnings("unchecked")
    public ListJsonHandler() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class) {
            clazz = (Class<T>) type;
        } else {
            throw new RuntimeException("unexpected status!");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, Json.toJson(parameter));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        String result = rs.getString(columnName);
        return (rs.wasNull() || Kit.isBlank(result))  ? new ArrayList<>() : Json.parseList(result, clazz);
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        String result = rs.getString(columnIndex);
        return (rs.wasNull() || Kit.isBlank(result))  ? new ArrayList<>(): Json.parseList(result, clazz);

    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String result = cs.getString(columnIndex);
        return (cs.wasNull() || Kit.isBlank(result)) ? new ArrayList<>() : Json.parseList(result, clazz);
    }

}
