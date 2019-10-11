package com.github.jsqltool.sql.procedure;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.result.SqlResult;

public interface ProcedureHandler {

	/**
	 * 
	* @author yzh
	* @date 2019年9月2日
	* @Description:获取存储函数或者存储过程列表的名称
	 */
	List<String> lisProcedure(Connection connection, DBObjectParam param) throws SQLException;

	SqlResult listProcedureInfo(Connection connection, DBObjectParam param) throws SQLException;

	boolean support(DBType dbType);

}
