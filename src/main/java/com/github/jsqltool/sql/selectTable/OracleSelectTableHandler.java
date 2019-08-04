package com.github.jsqltool.sql.selectTable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.SelectTableParam;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.SqlPlus.SqlResult;
import com.github.jsqltool.utils.JdbcUtil;

public class OracleSelectTableHandler implements SelectTableHandler {

	@Override
	public SqlResult selectTable(Connection connection, SelectTableParam param) throws SQLException {
		if (param == null || !StringUtils.isNoneBlank(param.getTableName())) {
			throw new JsqltoolParamException("表名不能为空！");
		}
		long startTime = System.currentTimeMillis();
		String tableInfo = JdbcUtil.getTableNameInfo(connection, param.getCatalog(), param.getSchema(),
				param.getTableName());
		DatabaseMetaData metaData = connection.getMetaData();
		// 获取行
		SqlPlus.setPage(param.getPage(), param.getPageSize(), null, true,
				DBType.getDBTypeByDriverClassName(metaData.getDriverName()), "select count(*) from " + tableInfo);
		SqlResult result = SqlPlus.execute(connection, "select t.*,rowid from " + tableInfo + " t");
		long endTime = System.currentTimeMillis();
		result.setMessage("执行成功，耗时：" + (endTime - startTime) + "ms");
		return result;
	}

	@Override
	public boolean support(DBType dbType) {
		return dbType == DBType.ODBC_TYPE ? true : false;
	}

}
