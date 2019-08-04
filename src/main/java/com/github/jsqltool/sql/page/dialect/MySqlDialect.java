package com.github.jsqltool.sql.page.dialect;

public class MySqlDialect implements Dialect {

	@Override
	public String getPageSql(String sql, Integer page, Integer pageSize) {
		if (page != null && pageSize != null && page.compareTo(1) >= 0 && pageSize.compareTo(1) >= 0) {
			StringBuilder sqlBuilder = new StringBuilder();
			sqlBuilder.append("select * from ( ");
			sqlBuilder.append(sql);
			sqlBuilder.append("  ) temp_page ");
			String endPage = getEndPage(page, pageSize);
			sqlBuilder.append(endPage);
			return sqlBuilder.toString();
		}
		return sql;
	}

	private String getEndPage(Integer page, Integer pageSize) {
		int startRow = getStartRow(page, pageSize);
		StringBuilder sqlBuilder = new StringBuilder();
		if (startRow == 0) {
			sqlBuilder.append(" LIMIT ");
			sqlBuilder.append(pageSize);
		} else {
			sqlBuilder.append(" LIMIT ");
			sqlBuilder.append(startRow);
			sqlBuilder.append(",");
			sqlBuilder.append(pageSize);
		}
		return sqlBuilder.toString();
	}

	private int getStartRow(Integer page, Integer pageSize) {
		return page > 0 ? (page - 1) * pageSize : 0;
	}

}
