package com.github.jsqltool.sql.type;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.jsqltool.enums.JdbcType;

public interface TypeHandler<T, P> {
	/**
	 * 处理结果
	 * 
	 * @author yzh
	 * @date 2019年6月29日
	 */
	T handler(ResultSet resultSet, int index, JdbcType type) throws SQLException;

	/**
	 * 处理参数
	 * 
	 * @author yzh
	 * @date 2019年6月29日
	 */
	P getParam(Object obj, JdbcType type);

	boolean support(JdbcType type);
}
