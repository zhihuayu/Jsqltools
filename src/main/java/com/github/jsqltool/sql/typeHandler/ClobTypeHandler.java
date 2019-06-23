package com.github.jsqltool.sql.typeHandler;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.jsqltool.enums.JdbcType;

public class ClobTypeHandler implements TypeHandler<String> {

	@Override
	public String handler(ResultSet resultSet, int index, JdbcType type) throws SQLException {
		if (type == JdbcType.CLOB) {
			Clob clob = resultSet.getClob(index);
			if (clob == null) {
				return null;
			}
			int size = (int) clob.length();
			String value = clob.getSubString(1, size);
			return value;
		}
		return null;
	}

	@Override
	public boolean support(JdbcType type) {
		if (type == null) {
			return false;
		}
		if (type == JdbcType.CLOB)
			return true;
		return false;
	}

}
