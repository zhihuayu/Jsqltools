package com.github.jsqltool.sql.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.sql.SqlType;

public class NopSqlFilter implements SqlFilter {
	Logger logger = LoggerFactory.getLogger(NopSqlFilter.class);

	@Override
	public String sqlFilter(String sql, SqlType sqlTypel) {
		logger.info("sqlFilter...");
		return sql;
	}

}
