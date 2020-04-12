package com.github.jsqltool.sql.create;

import java.sql.Connection;
import java.util.List;

import com.github.jsqltool.param.CreateParam;
import com.github.jsqltool.param.TableColumnsParam;

/**
 * 
 * @author yzh
 * @date 2019年10月12日
 * 建表语句的基础类
 */
public interface ICreateStatement {

	/**
	 * 
	* @author yzh
	* @date 2019年10月12日
	* @Description: 生成建表的SQL语句
	 */
	List<String> createTable(Connection connection, TableColumnsParam tableParam, List<CreateParam> param);

	boolean support(Connection connection);

}
