package com.github.jsqltool.sql.table.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.result.SimpleTableInfo;
import com.github.jsqltool.result.TableColumnInfo;
import com.github.jsqltool.sql.table.TableHandler;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.Primary;

public class DefaultJDBCTableHandler implements TableHandler {

	@Override
	public List<SimpleTableInfo> listTableInfo(Connection connection, DBObjectParam param) throws SQLException {
		return JdbcUtil.listTableInfo(connection, param);
	}

	@Override
	public List<TableColumnInfo> listTableColumnInfo(Connection connection, TableColumnsParam param) {
		return JdbcUtil.listTableColumnInfo(connection, param);
	}

	@Override
	public List<Index> getIndexInfo(Connection connect, IndexParam param) throws SQLException {
		return JdbcUtil.getIndexInfo(connect, param);
	}

	@Override
	public Primary getPrimaryInfo(Connection connect, IndexParam param) throws SQLException {
		return JdbcUtil.getPrimaryInfo(connect, param);
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

}
