package com.github.jsqltool.sql.typeHandler;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.JsqltoolParamException;

public class NumberTypeHandler implements TypeHandler<Integer, Number> {

	@Override
	public Integer handler(ResultSet resultSet, int index, JdbcType type) throws SQLException {
		Object object = resultSet.getObject(index);
		if (object == null)
			return null;
		return resultSet.getInt(index);
	}

	@Override
	public boolean support(JdbcType type) {
		if (type == null) {
			return false;
		}
		if (type == JdbcType.NUMERIC) {
			return true;
		}
		return false;
	}

	@Override
	public Number getParam(Object obj, JdbcType type) {
		try {
			if (obj == null) {
				return null;
			}
			if (obj instanceof CharSequence) {
				if (StringUtils.isBlank((CharSequence) obj)) {
					return null;
				}
			}
			if (obj instanceof Number) {
				return ((Number) obj).longValue();
			}
			if (obj instanceof String) {
				String str = (String) obj;
				int indexOf = str.indexOf(".");
				if (indexOf == -1) {
					return Long.valueOf(str);
				}
				return new BigDecimal(str);
			}
		} catch (Exception e) {
			throw new JsqltoolParamException(obj + "转换成Number类型解析出错！", e);
		}
		throw new JsqltoolParamException("不能解析Class:" + obj.getClass().getName());
	}

}
