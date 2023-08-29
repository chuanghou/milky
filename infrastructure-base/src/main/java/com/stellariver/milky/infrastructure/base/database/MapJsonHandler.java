package com.stellariver.milky.infrastructure.base.database;

import com.stellariver.milky.common.tool.util.Json;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Hou Chuana
 */
public abstract class MapJsonHandler<K, V> extends BaseTypeHandler<Map<K, V>> {

    private final Class<K> kClazz;
    private final Class<V> vClazz;

    @SuppressWarnings("unchecked")
    public MapJsonHandler() {

        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type type0 = parameterizedType.getActualTypeArguments()[0];
        Type type1 = parameterizedType.getActualTypeArguments()[1];

        if (type0 instanceof Class) {
            kClazz = (Class<K>) type0;
        } else {
            throw new RuntimeException("unexpected status!");
        }
        if (type1 instanceof Class) {
            vClazz = (Class<V>) type1;
        } else {
            throw new RuntimeException("unexpected status!");
        }

    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<K, V> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, Json.toJson(parameter));
    }

    @Override
    public Map<K, V> getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        String result = rs.getString(columnName);
        return rs.wasNull() ? null : Json.parseMap(result, kClazz, vClazz);
    }

    @Override
    public Map<K, V> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        String result = rs.getString(columnIndex);
        return rs.wasNull() ? null : Json.parseMap(result, kClazz, vClazz);

    }

    @Override
    public Map<K, V> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String result = cs.getString(columnIndex);
        return cs.wasNull() ? null : Json.parseMap(result, kClazz, vClazz);
    }

}
