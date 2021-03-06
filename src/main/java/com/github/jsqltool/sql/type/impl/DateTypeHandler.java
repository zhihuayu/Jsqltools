package com.github.jsqltool.sql.type.impl;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.sql.type.TypeHandler;

public class DateTypeHandler implements TypeHandler<String, java.util.Date> {

	private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

	@Override
	public String handler(ResultSet resultSet, int index, JdbcType type) throws SQLException {

		if (type == JdbcType.TIMESTAMP) {
			Timestamp time = resultSet.getTimestamp(index);
			if (time == null) {
				return null;
			}
			java.util.Date d = new java.util.Date(time.getTime());
			SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATETIME_PATTERN);
			return format.format(d);
		}

		if (type == JdbcType.DATE) {
			Date time = resultSet.getDate(index);
			if (time == null) {
				return null;
			}
			java.util.Date d = new java.util.Date(time.getTime());
			SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
			return format.format(d);
		}

		if (type == JdbcType.TIME) {
			Time time = resultSet.getTime(index);
			if (time == null) {
				return null;
			}
			java.util.Date d = new java.util.Date(time.getTime());
			SimpleDateFormat format = new SimpleDateFormat(DEFAULT_TIME_PATTERN);
			return format.format(d);
		}
		return null;
	}

	@Override
	public boolean support(JdbcType type) {
		return type == JdbcType.TIME || type == JdbcType.TIMESTAMP || type == JdbcType.DATE;
	}

	@Override
	public java.util.Date getParam(Object obj, JdbcType type) {
		try {
			if (obj == null) {
				return null;
			}
			if (obj instanceof java.util.Date)
				return (java.util.Date) obj;
			if (obj instanceof String) {
				String str = StringUtils.trim((String) obj);
				// 年月日模式
				if (str.length() == 10) {
					SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
					java.util.Date parse = format.parse(str);
					return new Date(parse.getTime());
				}
				// 时分秒模式
				if (str.length() == 8) {
					SimpleDateFormat format = new SimpleDateFormat(DEFAULT_TIME_PATTERN);
					java.util.Date parse = format.parse(str);
					return new Time(parse.getTime());
				}
				// 年月日时分秒模式
				if (str.length() == 19) {
					SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATETIME_PATTERN);
					java.util.Date parse = format.parse(str);
					return new Timestamp(parse.getTime());
				}
				return null;
			}
		} catch (Exception e) {
			throw new JsqltoolParamException(e);
		}
		throw new JsqltoolParamException("日期类型解析出错！");
	}

}
