package com.github.jsqltool.sql.typeHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.github.jsqltool.enums.JdbcType;

public class DateTypeHandler implements TypeHandler<String> {

	private final static String defaultDateTimePattern = "yyyy-MM-dd HH:mm:ss";
	private final static String defaultDatePattern = "yyyy-MM-dd";
	private final static String defaultTimePattern = "HH:mm:ss";

	@Override
	public String handler(ResultSet resultSet, int index, JdbcType type) throws SQLException {

		if (type == JdbcType.TIMESTAMP) {
			Timestamp time = resultSet.getTimestamp(index);
			if (time == null) {
				return null;
			}
			java.util.Date d = new java.util.Date(time.getTime());
			SimpleDateFormat format = new SimpleDateFormat(defaultDateTimePattern);
			return format.format(d);
		}

		if (type == JdbcType.DATE) {
			Date time = resultSet.getDate(index);
			if (time == null) {
				return null;
			}
			java.util.Date d = new java.util.Date(time.getTime());
			SimpleDateFormat format = new SimpleDateFormat(defaultDatePattern);
			return format.format(d);
		}

		if (type == JdbcType.TIME) {
			Time time = resultSet.getTime(index);
			if (time == null) {
				return null;
			}
			java.util.Date d = new java.util.Date(time.getTime());
			SimpleDateFormat format = new SimpleDateFormat(defaultTimePattern);
			return format.format(d);
		}
		return null;
	}

	@Override
	public boolean support(JdbcType type) {
		if (type == null) {
			return false;
		}
		if (type == JdbcType.TIME || type == JdbcType.TIMESTAMP || type == JdbcType.DATE)
			return true;
		return false;
	}

}
