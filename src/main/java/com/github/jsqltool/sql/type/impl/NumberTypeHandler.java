package com.github.jsqltool.sql.type.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.sql.type.TypeHandler;

public class NumberTypeHandler implements TypeHandler<String, Number> {

	/**
	 * 由于 number类型可能为long，如果为long则显示的前端如浏览器的位数为16为，超过16位会强制转换成0，
	 * 也可能为float或者double类型，所以这里统一转换成字符串进行输出
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
		return type == JdbcType.NUMERIC;
	}

	@Override
	public Number getParam(Object obj, JdbcType type) {
		try {
			if (obj == null) {
				return null;
			}
			if (obj instanceof CharSequence && StringUtils.isBlank((CharSequence) obj)) {
				return null;
			}
			if (obj instanceof Number) {
				return (Number) obj;
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
