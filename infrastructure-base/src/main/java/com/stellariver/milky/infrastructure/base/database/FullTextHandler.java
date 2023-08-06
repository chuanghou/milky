package com.stellariver.milky.infrastructure.base.database;

import com.stellariver.milky.common.base.ErrorEnum;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.infrastructure.base.ErrorEnums;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hou Chuana
 */
public class FullTextHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType)
            throws SQLException {
        parameter.forEach(p -> SysEx.trueThrow(StringUtils.length(p) < 3, ErrorEnums.CONFIG_ERROR));
        String value = String.join(",", parameter);
        ps.setString(i, value);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        String result = rs.getString(columnName);
        return rs.wasNull() ? null : Arrays.stream(StringUtils.split(result, ",")).collect(Collectors.toList());
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        String result = rs.getString(columnIndex);
        return rs.wasNull() ? null : Arrays.stream(StringUtils.split(result, ",")).collect(Collectors.toList());

    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String result = cs.getString(columnIndex);
        return cs.wasNull() ? null : Arrays.stream(StringUtils.split(result, ",")).collect(Collectors.toList());
    }

}
