package com.stellariver.milky.infrastructure.base.database;

import com.stellariver.milky.common.tool.util.Json;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Hou Chuana
 */

public abstract class JsonHandler<T> extends BaseTypeHandler<T> {

    private final Class<T> clazz;

    @SuppressWarnings("unchecked")
    public JsonHandler() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class) {
            clazz = (Class<T>) type;
        } else {
            throw new RuntimeException("unexpected status!");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, Json.toJson(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        String result = rs.getString(columnName);
        return rs.wasNull() || StringUtils.isBlank(result) ? null : Json.parse(result, clazz);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        String result = rs.getString(columnIndex);
        return rs.wasNull() || StringUtils.isBlank(result) ? null: Json.parse(result, clazz);

    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String result = cs.getString(columnIndex);
        return cs.wasNull() || StringUtils.isBlank(result) ? null : Json.parse(result, clazz);
    }

}
