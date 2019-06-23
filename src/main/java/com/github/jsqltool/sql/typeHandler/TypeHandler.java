package com.github.jsqltool.sql.typeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.jsqltool.enums.JdbcType;

public interface TypeHandler<T> {
	T handler(ResultSet resultSet, int index, JdbcType type) throws SQLException;

	boolean support(JdbcType type);
}
