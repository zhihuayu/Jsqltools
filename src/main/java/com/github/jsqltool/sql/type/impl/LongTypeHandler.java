package com.github.jsqltool.sql.type.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.sql.type.TypeHandler;

public class LongTypeHandler implements TypeHandler<String, Long> {

	/**
	 * 为了兼容浏览器只能显示16为数字而大于16位数字会自动将后面的位数填0，所以这里统一输出为string类型
	 */
	@Override
	public String handler(ResultSet resultSet, int index, JdbcType type) throws SQLException {
		Object object = resultSet.getObject(index);
		if (object == null)
			return null;
		return resultSet.getString(index);
	}

	@Override
	public boolean support(JdbcType type) {
		return type == JdbcType.BIGINT;
	}

	@Override
	public Long getParam(Object obj, JdbcType type) {
		try {
			if (obj == null) {
				return null;
			}
			if (obj instanceof CharSequence && StringUtils.isBlank((CharSequence) obj)) {
				return null;
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
