package com.github.jsqltool.sql.excuteCall;

import java.sql.Connection;
import java.sql.SQLException;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.ProcedureParam;

/**
 * 处理存储过程、存储函数以及诸如Oracle包的基类
 * @author yzh
 * @date 2019年8月16日
 */
public interface ExecuteCallHandler {

	/**
	 * 
	* @author yzh
	* @date 2019年8月16日
	* @Description: 执行存储过程
	 */
	String executeCall(Connection connection, ProcedureParam param) throws SQLException;

	/**
	* @author yzh
	* @date 2019年8月17日
	* @Description: 执行SQL语句块
	*/
	String executeCall(Connection connection, String sqlBlock) throws SQLException;

	boolean support(DBType dbType);
}
