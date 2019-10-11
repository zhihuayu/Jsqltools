package com.github.jsqltool.sql.excuteCall;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.param.ProcedureParam;
import com.github.jsqltool.sql.SqlPlus;

public abstract class AbstractExecuteCallLifeCycle implements ExecuteCallHandler {

	protected String beforeExecuteCall(Connection connection, ProcedureParam param) throws SQLException {
		return "";
	};

	protected String afterExecuteCall(Connection connection, ProcedureParam param) throws SQLException {
		return "";
	};

	@Override
	public final String executeCall(Connection connection, ProcedureParam param) throws SQLException {
		StringBuilder result = new StringBuilder();
		String beforeExecuteCall = beforeExecuteCall(connection, param);
		if (StringUtils.isNotBlank(beforeExecuteCall))
			result.append(beforeExecuteCall);
		result.append(SqlPlus.executeCall(connection, param));
		String afterExecuteCall = afterExecuteCall(connection, param);
		if (StringUtils.isNotBlank(afterExecuteCall))
			result.append(afterExecuteCall);
		return result.toString();
	}

	@Override
	public String executeCall(Connection connection, String sqlBlock) throws SQLException {
		StringBuilder result = new StringBuilder();
		String beforeExecuteCall = beforeExecuteCall(connection, null);
		if (StringUtils.isNotBlank(beforeExecuteCall))
			result.append(beforeExecuteCall);
		result.append(SqlPlus.executeCall(connection, sqlBlock));
		String afterExecuteCall = afterExecuteCall(connection, null);
		if (StringUtils.isNotBlank(afterExecuteCall))
			result.append(afterExecuteCall);
		return result.toString();
	}

}
