package com.github.jsqltool.sql.procedure.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.result.FunctionInfo;
import com.github.jsqltool.result.SqlResult;
import com.github.jsqltool.sql.procedure.ProcedureHandler;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.utils.SqlResultUtil;

public class JDBCProcedureHandler implements ProcedureHandler {
	Logger logger = LoggerFactory.getLogger(JDBCProcedureHandler.class);

	@Override
	public List<String> lisProcedure(Connection connection, DBObjectParam param) throws SQLException {
		List<String> result = new ArrayList<>();
		if (param != null && StringUtils.isNotBlank(param.getType())) {
			List<FunctionInfo> functionsOrProcedure = JdbcUtil.getFunctionsOrProcedure(connection, param);
			for (FunctionInfo info : functionsOrProcedure) {
				result.add(info.getName());
			}
		} else {
			logger.error("参数错误！");
			throw new JsqltoolParamException("参数错误！");
		}
		return result;
	}

	@Override
	public SqlResult listProcedureInfo(Connection connection, DBObjectParam param) throws SQLException {
		if (param != null && StringUtils.isNotBlank(param.getType())) {
			List<FunctionInfo> functionsOrProcedure = JdbcUtil.getFunctionsOrProcedure(connection, param);
			try {
				SqlResult re = SqlResultUtil.ObjectToSqlResult(functionsOrProcedure);
				re.setStatus(SqlResult.success);
				re.setMessage("获取成功！");
				return re;
			} catch (Exception e) {
				logger.error("POJO转换成SqlResult出错！");
				throw new JsqltoolParamException(e);
			}
		} else {
			logger.error("参数错误！");
			throw new JsqltoolParamException("参数错误！");
		}
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}
}
