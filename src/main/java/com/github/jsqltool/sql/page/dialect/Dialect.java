package com.github.jsqltool.sql.page.dialect;

public interface Dialect {

	String getPageSql(String sql, Integer page, Integer pageSize,long count);

}
