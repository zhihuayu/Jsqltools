package com.github.jsqltool.sql.tableColumn;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.sql.TableColumnInfo;
import com.github.jsqltool.utils.DBUtil;

public class DefaultTableColumnHandler implements ITableColumnHandler {

	@Override
	public List<TableColumnInfo> list(Connection connection, TableColumnsParam param) {
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			String catelog = StringUtils.isBlank(param.getCatalog()) ? null : param.getCatalog().toUpperCase();
			String schema = StringUtils.isBlank(param.getSchema()) ? null : param.getSchema().toUpperCase();
			String tableName = StringUtils.upperCase(param.getTableName());
			List<TableColumnInfo> columns = null;
			// 1.基本信息
			try (ResultSet rs = dbmd.getColumns(catelog, schema, tableName, "%");) {
				columns = DBUtil.getTableColumn(rs);
			}
			// 2.PrimayKey信息
			try (ResultSet rs = dbmd.getPrimaryKeys(catelog, schema, StringUtils.upperCase(tableName));) {
				while (rs.next()) {
					for (TableColumnInfo c : columns) {
						if (StringUtils.equalsIgnoreCase(c.getTableName(), rs.getString(3))
								&& StringUtils.equalsIgnoreCase(c.getColumnName(), rs.getString(4))) {
							c.setPkComponent(true);
						}
					}
				}
			}
			return columns;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

}
