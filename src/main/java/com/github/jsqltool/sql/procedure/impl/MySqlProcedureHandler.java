package com.github.jsqltool.sql.procedure.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.result.SqlResult;
import com.github.jsqltool.result.SqlResult.Column;
import com.github.jsqltool.result.SqlResult.Record;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.procedure.ProcedureHandler;
import com.github.jsqltool.utils.JdbcUtil;

public class MySqlProcedureHandler implements ProcedureHandler {

	@Override
	public List<String> lisProcedure(Connection connection, DBObjectParam param) throws SQLException {
		List<String> result = new ArrayList<>();
		if (param != null && StringUtils.isNotBlank(param.getType())) {
			String sql = " show " + StringUtils.trim(param.getType()) + " status";
			SqlResult execute = SqlPlus.execute(connection, sql);
			int columnIndex = getColumnIndex("name", execute.getColumns());
			if (execute.getCount() > 0 && columnIndex >= 0) {
				List<Record> records = execute.getRecords();
				for (Record r : records) {
					List<Object> values = r.getValues();
					result.add(values.get(columnIndex).toString());
				}
			}
		} else {
			throw new JsqltoolParamException("参数有误！");
		}
		return result;
	}

	private int getColumnIndex(String columnName, List<Column> columns) {
		if (StringUtils.isNotBlank(columnName) && columns != null && !columns.isEmpty()) {
			for (int i = 0; i < columns.size(); i++) {
				Column c = columns.get(i);
				if (columnName.trim().equalsIgnoreCase(c.getAlias().trim())) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public SqlResult listProcedureInfo(Connection connection, DBObjectParam param) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append(" show ");
		sql.append(StringUtils.trim(param.getType()));
		sql.append(" status ");
		String name = JdbcUtil.covertName(connection, param.getName());
		if (StringUtils.isNotEmpty(name)) {
			sql.append(" where name='");
			sql.append(name + "'");
		}
		SqlResult execute = SqlPlus.execute(connection, sql.toString());
		return execute;
	}

	@Override
	public boolean support(DBType dbType) {
		return dbType == DBType.MYSQL_TYPE;
	}

}
