package com.github.jsqltool.sql.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.sql.SqlType;

/**
 * 不过滤任何语句直接执行
 * 
 * @description
 * @author yzh
 * @date 2020年9月7日
 */
public class NopSqlFilter implements SqlFilter {
	Logger logger = LoggerFactory.getLogger(NopSqlFilter.class);

	@Override
	public String sqlFilter(String sql, SqlType sqlTypel) {
		logger.info("sqlFilter...");
		return sql;
	}

}
