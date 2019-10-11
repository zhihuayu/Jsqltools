package com.github.jsqltool.sql.table.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.result.SimpleTableInfo;
import com.github.jsqltool.result.TableColumnInfo;
import com.github.jsqltool.sql.table.TableHandler;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.Primary;

public class TableHandlerContent implements TableHandler {

	LinkedList<TableHandler> tables = new LinkedList<>();

	private TableHandlerContent() {
	}

	public static TableHandlerContent builder() {
		TableHandlerContent table = new TableHandlerContent();
		table.addLast(new DefaultJDBCTableHandler());
		return table;
	}

	@Override
	public List<SimpleTableInfo> listTableInfo(Connection connection, DBObjectParam param) throws SQLException {
		for (TableHandler table : tables) {
			if (table.support(connection)) {
				return table.listTableInfo(connection, param);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public List<TableColumnInfo> listTableColumnInfo(Connection connection, TableColumnsParam param) {
		for (TableHandler tableHandler : tables) {
			if (tableHandler.support(connection)) {
				return tableHandler.listTableColumnInfo(connection, param);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public List<Index> getIndexInfo(Connection connect, IndexParam param) throws SQLException {
		for (TableHandler info : tables) {
			if (info.support(connect)) {
				return info.getIndexInfo(connect, param);
			}
		}
		return null;
	}

	@Override
	public Primary getPrimaryInfo(Connection connect, IndexParam param) throws SQLException {
		for (TableHandler info : tables) {
			if (info.support(connect)) {
				return info.getPrimaryInfo(connect, param);
			}
		}
		return null;
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

	public void addLast(TableHandler handler) {
		tables.addLast(handler);
	}

	public void addFirst(TableHandler handler) {
		tables.addFirst(handler);
	}

}
