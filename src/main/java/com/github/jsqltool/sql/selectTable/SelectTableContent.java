package com.github.jsqltool.sql.selectTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.NoSelectTableHandlerException;
import com.github.jsqltool.param.SelectTableParam;
import com.github.jsqltool.result.SqlResult;

public class SelectTableContent implements SelectTableHandler {

	LinkedList<SelectTableHandler> schemas = new LinkedList<>();

	private SelectTableContent() {
	}

	public static SelectTableContent builder() {
		SelectTableContent selectTableContent = new SelectTableContent();
		selectTableContent.addFirst(new DefaultSelectTableHandler());
		selectTableContent.addFirst(new MySqlSelectTableHandler());
		selectTableContent.addFirst(new OracleSelectTableHandler());
		return selectTableContent;
	}

	@Override
	public SqlResult selectTable(Connection connection, SelectTableParam param) throws SQLException {
		for (SelectTableHandler schema : schemas) {
			if (schema.support(DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName()))) {
				return schema.selectTable(connection, param);
			}
		}
		throw new NoSelectTableHandlerException("没有找到对应的SelectTableHandler实例来处理该查询！");
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	public void addLast(SelectTableHandler handler) {
		schemas.addLast(handler);
	}

	public void addFirst(SelectTableHandler handler) {
		schemas.addFirst(handler);
	}

}
