package com.github.jsqltool.sql.tableColumn;

import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.sql.TableColumnInfo;

public class TableColumnHandlerContent implements ITableColumnHandler {

	LinkedList<ITableColumnHandler> tableHandlers = new LinkedList<>();

	@Override
	public List<TableColumnInfo> list(Connection connection, TableColumnsParam param) {
		for (ITableColumnHandler tableHandler : tableHandlers) {
			if (tableHandler.support(connection)) {
				return tableHandler.list(connection, param);
			}
		}
		return Collections.emptyList();
	}

	public void addLast(ITableColumnHandler table) {
		tableHandlers.addLast(table);
	}

	public void addFirst(ITableColumnHandler table) {
		tableHandlers.addFirst(table);
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

}
