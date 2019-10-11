package com.github.jsqltool.sql.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.result.SimpleTableInfo;
import com.github.jsqltool.result.TableColumnInfo;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.Primary;

/**
 * 用于获取典型的 "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".的列表信息
 * @author yzh
 * @date 2019年8月10日
 */
public interface TableHandler {

	/**
	 * 
	* @author yzh
	* @date 2019年8月10日
	* @Description: 获取简单的列表信息
	 */
	List<SimpleTableInfo> listTableInfo(Connection connection, DBObjectParam param) throws SQLException;

	/**
	 * 
	* @author yzh
	* @date 2019年8月10日
	* @Description: 获取指定表的列信息
	 */
	List<TableColumnInfo> listTableColumnInfo(Connection connection, TableColumnsParam param);

	/**
	 * 
	* @author yzh
	* @date 2019年8月10日
	* @Description: 获取索引信息
	 */
	List<Index> getIndexInfo(Connection connect, IndexParam param) throws SQLException;

	/**
	 * 
	* @author yzh
	* @date 2019年8月10日
	* @Description: 获取主键信息
	 */
	Primary getPrimaryInfo(Connection connect, IndexParam param) throws SQLException;

	boolean support(Connection connection);
}
