package com.github.jsqltool.sql.type.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.sql.type.TypeHandler;

public class IntegerTypeHandler implements TypeHandler<Integer, Integer> {

	@Override
	public Integer handler(ResultSet resultSet, int index, JdbcType type) throws SQLException {
		Object object = resultSet.getObject(index);
		if (object == null)
			return null;
		return resultSet.getInt(index);
	}

	@Override
	public boolean support(JdbcType type) {
		return type == JdbcType.INTEGER || type == JdbcType.BIT || type == JdbcType.TINYINT;
	}

	@Override
	public Integer getParam(Object obj, JdbcType type) {
		try {
			if (obj == null) {
				return null;
			}
			if (obj instanceof Number) {
				return ((Number) obj).intValue();
			}
			if (obj instanceof CharSequence && StringUtils.isBlank((CharSequence) obj)) {
				return null;
			}
			return Integer.valueOf(obj.toString());
		} catch (Exception e) {
			throw new JsqltoolParamException(obj + "转换成Integer类型解析出错！", e);
		}
	}

}
