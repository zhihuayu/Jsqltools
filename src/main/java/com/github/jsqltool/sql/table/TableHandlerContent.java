package com.github.jsqltool.sql.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.jsqltool.param.TablesParam;
import com.github.jsqltool.sql.SimpleTableInfo;

public class TableHandlerContent implements ITableHandler {

	LinkedList<ITableHandler> tables = new LinkedList<>();

	@Override
	public List<SimpleTableInfo> list(Connection connection, TablesParam param) throws SQLException {
		for (ITableHandler table : tables) {
			if (table.support(connection)) {
				return table.list(connection, param);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

	public void addLast(ITableHandler handler) {
		tables.addLast(handler);
	}

	public void addFirst(ITableHandler handler) {
		tables.addFirst(handler);
	}

}
