package com.github.jsqltool.sql.filter;

import com.github.jsqltool.sql.SqlType;

/**
 * sqlFilter的过滤器类，可以用于控制各个Sql的权限，其在sql解析之后sql执行之前执行，如果抛出运行时异常则会不会执行该语句
 * 
 * @author yzh
 *
 * @date 2020年9月1日
 */
public interface SqlFilter {

	String sqlFilter(String sql, SqlType sqlType);

}
