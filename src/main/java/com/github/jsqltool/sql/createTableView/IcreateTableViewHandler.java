package com.github.jsqltool.sql.createTableView;

import java.sql.Connection;
import java.sql.SQLException;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.TablesParam;

/**
 * 获取数据表创建语句的基础接口
 * @author yzh
 * @date 2019年7月12日
 */
public interface IcreateTableViewHandler {

	/**
	 * 
	* @author yzh
	* @date 2019年7月12日
	* @Description: 
	* @return 返回值诸如：
	*   create table xxx ( id number,name varchar2(30) );等等
	 */
	String getCreateTableView(Connection connect, TablesParam param) throws SQLException;

	boolean support(DBType dbType);

}
