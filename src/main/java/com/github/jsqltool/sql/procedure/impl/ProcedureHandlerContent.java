package com.github.jsqltool.sql.procedure.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.NoSelectTableHandlerException;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.result.SqlResult;
import com.github.jsqltool.sql.procedure.ProcedureHandler;

public class ProcedureHandlerContent implements ProcedureHandler {

	LinkedList<ProcedureHandler> handlers = new LinkedList<>();

	private ProcedureHandlerContent() {
	}

	public static ProcedureHandlerContent builder() {
		ProcedureHandlerContent procedureHandlerContent = new ProcedureHandlerContent();
		procedureHandlerContent.addFirst(new JDBCProcedureHandler());
		procedureHandlerContent.addFirst(new MySqlProcedureHandler());
		return procedureHandlerContent;
	}

	@Override
	public List<String> lisProcedure(Connection connection, DBObjectParam param) throws SQLException {
		for (ProcedureHandler schema : handlers) {
			if (schema.support(DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName()))) {
				return schema.lisProcedure(connection, param);
			}
		}
		throw new NoSelectTableHandlerException("没有找到对应的ProcedureHandler实例来处理该查询！");
	}

	@Override
	public SqlResult listProcedureInfo(Connection connection, DBObjectParam param) throws SQLException {
		for (ProcedureHandler schema : handlers) {
			if (schema.support(DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName()))) {
				return schema.listProcedureInfo(connection, param);
			}
		}
		throw new NoSelectTableHandlerException("没有找到对应的ProcedureHandler实例来处理该查询！");
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	public void addLast(ProcedureHandler handler) {
		handlers.addLast(handler);
	}

	public void addFirst(ProcedureHandler handler) {
		handlers.addFirst(handler);
	}

}
