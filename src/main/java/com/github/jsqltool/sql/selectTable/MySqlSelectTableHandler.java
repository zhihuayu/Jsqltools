package com.github.jsqltool.sql.selectTable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.SelectTableParam;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.SqlPlus.Column;
import com.github.jsqltool.sql.SqlPlus.SqlResult;

public class MySqlSelectTableHandler implements SelectTableHandler {

	@Override
	public SqlResult selectTable(Connection connection, SelectTableParam param) throws SQLException {
		if (param == null || !StringUtils.isNoneBlank(param.getCatalog(), param.getTableName())) {
			throw new JsqltoolParamException("catalog和表名不能为空！");
		}
		long startTime = System.currentTimeMillis();
		DatabaseMetaData metaData = connection.getMetaData();
		SqlPlus.execute(connection, "use " + param.getCatalog());
		// 获取行的条数
		long count = 0;
		SqlResult select = SqlPlus.select(connection, "SHOW  TABLE STATUS  LIKE '" + param.getTableName().trim() + "'");
		if (select.getCount() == 1L) {
			List<Column> columns = select.getColumns();
			for (int i = 0; i < columns.size(); i++) {
				if (columns.get(i).getColumnName().equalsIgnoreCase("table_rows")) {
					Object object = select.getRecords().get(0).getValues().get(i);
					count = Long.valueOf(object.toString());
					break;
				}
			}
		}
		// 获取行
		SqlPlus.setPage(param.getPage(), param.getPageSize(), count, false,
				DBType.getDBTypeByDriverClassName(metaData.getDriverName()));
		SqlResult result = SqlPlus.execute(connection, "select * from " + param.getTableName());
		long endTime = System.currentTimeMillis();
		result.setMessage("执行成功，耗时：" + (endTime - startTime) + "ms");
		return result;
	}

	@Override
	public boolean support(DBType dbType) {
		return dbType == DBType.MYSQL_TYPE ? true : false;
	}
}