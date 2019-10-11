package com.github.jsqltool.sql.excuteCall.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.ProcedureParam;
import com.github.jsqltool.sql.excuteCall.ExecuteCallHandler;

public class ExecuteCallHandlerContent implements ExecuteCallHandler {

	LinkedList<ExecuteCallHandler> executeCallHandlers = new LinkedList<>();

	private ExecuteCallHandlerContent() {
	}

	public static ExecuteCallHandlerContent builder() {
		ExecuteCallHandlerContent executeCallContent = new ExecuteCallHandlerContent();
		executeCallContent.addFirst(new DefaultExecuteCallHandler());
		executeCallContent.addFirst(new OracleExecuteCallHandler());
		return executeCallContent;
	}

	@Override
	public String executeCall(Connection connection, ProcedureParam param) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName());
		for (ExecuteCallHandler executeCallHandler : executeCallHandlers) {
			if (executeCallHandler.support(dbType)) {
				return executeCallHandler.executeCall(connection, param);
			}
		}
		throw new JsqltoolParamException("没有找到对应的ExecuteCallHandler来处理相应的存储过程！");
	}

	@Override
	public String executeCall(Connection connection, String sqlBlock) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName());
		for (ExecuteCallHandler executerHandler : executeCallHandlers) {
			if (executerHandler.support(dbType)) {
				return executerHandler.executeCall(connection, sqlBlock);
			}
		}
		throw new JsqltoolParamException("没有找到对应的ExecuteCallHandler来处理相应的存储过程！");
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	public void addLast(ExecuteCallHandler handler) {
		executeCallHandlers.addLast(handler);
	}

	public void addFirst(ExecuteCallHandler handler) {
		executeCallHandlers.addFirst(handler);
	}

}
