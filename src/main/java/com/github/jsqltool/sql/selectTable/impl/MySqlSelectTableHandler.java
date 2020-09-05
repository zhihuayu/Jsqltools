package com.github.jsqltool.sql.selectTable.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.SelectTableParam;
import com.github.jsqltool.result.SqlResult;
import com.github.jsqltool.result.SqlResult.Column;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.selectTable.SelectTableHandler;

public class MySqlSelectTableHandler extends DefaultSelectTableHandler implements SelectTableHandler {

	@Override
	public SqlResult selectTable(Connection connection, SelectTableParam param) throws SQLException {
		if (param == null || !StringUtils.isNoneBlank(param.getCatalog(), param.getTableName())) {
			throw new JsqltoolParamException("catalog和表名不能为空！");
		}
		if (StringUtils.equalsIgnoreCase(param.getType(), "VIEW")) {
			return super.selectTable(connection, param);
		}
		long startTime = System.currentTimeMillis();
		DatabaseMetaData metaData = connection.getMetaData();
		SqlPlus.execute(connection, "use " + param.getCatalog());
		SqlPlus.setPage(param.getPage(), param.getPageSize(), param.getCount(), true,
				DBType.getDBTypeByDriverClassName(metaData.getDriverName()));
		SqlResult result = SqlPlus.execute(connection, "select * from " + param.getTableName());
		long endTime = System.currentTimeMillis();
		if (result.getStatus() == SqlResult.success)
			result.setMessage("执行成功，耗时：" + (endTime - startTime) + "ms");
		return result;
	}

	@Override
	public boolean support(DBType dbType) {
		return dbType == DBType.MYSQL_TYPE ? true : false;
	}
}