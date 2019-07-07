package com.github.jsqltool.sql.typeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.JsqltoolParamException;

public class LongTypeHandler implements TypeHandler<Long, Long> {

	@Override
	public Long handler(ResultSet resultSet, int index, JdbcType type) throws SQLException {
		Object object = resultSet.getObject(index);
		if (object == null)
			return null;
		return resultSet.getLong(index);
	}

	@Override
	public boolean support(JdbcType type) {
		if (type == null) {
			return false;
		}
		if (type == JdbcType.BIGINT) {
			return true;
		}
		return false;
	}

	@Override
	public Long getParam(Object obj, JdbcType type) {
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
			return Long.valueOf(obj.toString());
		} catch (Exception e) {
			throw new JsqltoolParamException(obj + "转换成Long类型解析出错！", e);
		}
	}

}
