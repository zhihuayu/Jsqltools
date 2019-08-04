package com.github.jsqltool.sql.selectTable;

import java.sql.Connection;
import java.sql.SQLException;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.SelectTableParam;
import com.github.jsqltool.sql.SqlPlus.SqlResult;

/**
 * 
 * 查询指定表数据的处理器基类
 * @author yzh
 * @date 2019年7月27日
 */
public interface SelectTableHandler {

	SqlResult selectTable(Connection connection, SelectTableParam param) throws SQLException;

	boolean support(DBType dbType);
}
