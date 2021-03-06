package com.github.jsqltool.sql.page.dialect;

public class OracleDialect implements Dialect {

	@Override
	public String getPageSql(String sql, Integer page, Integer pageSize) {
		int startRow = getStartRow(page, pageSize);
		int endRow = getEndRow(page, pageSize);
		StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
		if (startRow > 0) {
			sqlBuilder.append("SELECT * FROM ( ");
		}
		if (endRow > 0) {
			sqlBuilder.append(" SELECT TMP_PAGE.*, ROWNUM ROW_ID FROM ( ");
		}
		sqlBuilder.append(sql);
		if (endRow > 0) {
			sqlBuilder.append(" ) TMP_PAGE WHERE ROWNUM <= ");
			sqlBuilder.append(endRow);
		}
		if (startRow > 0) {
			sqlBuilder.append(" ) WHERE ROW_ID > ");
			sqlBuilder.append(startRow);
		}
		return sqlBuilder.toString();
	}

	private int getStartRow(Integer page, Integer pageSize) {
		return page > 0 ? (page - 1) * pageSize : 0;
	}

	private int getEndRow(Integer page, Integer pageSize) {
		return getStartRow(page, pageSize) + pageSize * (page > 0 ? 1 : 0);
	}

}
