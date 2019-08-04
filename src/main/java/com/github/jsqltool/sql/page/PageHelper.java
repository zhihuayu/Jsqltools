package com.github.jsqltool.sql.page;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.sql.page.dialect.Dialect;
import com.github.jsqltool.sql.page.dialect.MySqlDialect;
import com.github.jsqltool.sql.page.dialect.OracleDialect;

public class PageHelper implements Cloneable {

	private final static ConcurrentHashMap<DBType, Dialect> dialecte = new ConcurrentHashMap<>();
	private static Logger logger = LoggerFactory.getLogger(PageHelper.class);
	private DBType dbType;
	private Integer page;
	private Integer pageSize;
	private Long count;
	private Boolean isCount; // 是否计算总页数，默认是计算总页数的
	private String countSql;

	static {
		dialecte.put(DBType.MYSQL_TYPE, new MySqlDialect());
		dialecte.put(DBType.ORACLE_TYPE, new OracleDialect());
	}

	public Boolean getIsCount() {
		return isCount;
	}

	public void setIsCount(Boolean isCount) {
		this.isCount = isCount;
	}

	public long getCountFromSql(Connection connect, String sql) throws SQLException {
		if (isCount != null && !isCount) {
			return count;
		}
		long startTime = System.currentTimeMillis();
		long result = 0L;
		StringBuilder sb = new StringBuilder();
		sql = StringUtils.trim(sql).toLowerCase();
		sb.append("select count(*) from (");
		sb.append(sql);
		sb.append(" ) temp_count");
		// 有countSql的情况下，就执行该语句来获取行数
		if (StringUtils.isNotBlank(countSql)) {
			sb.setLength(0);
			sb.append(countSql);
		}
		try (Statement statement = connect.createStatement();
				ResultSet resultSet = statement.executeQuery(sb.toString());) {
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			if (columnCount == 1 || JdbcType.forCode(metaData.getColumnType(1)) == JdbcType.INTEGER) {
				while (resultSet.next()) {
					result = resultSet.getLong(columnCount);
					break;
				}
			}
		}
		long endTime = System.currentTimeMillis();
		logger.debug("execute sql:[{}],rowNum:[{}],times:[{}]ms",
				new Object[] { sb.toString(), result, endTime - startTime });
		this.count = result;
		return result;
	}

	public void setCountSql(String countSql) {
		this.countSql = countSql;
	}

	public String getPageSql(String sql) {
		Dialect dialect = dialecte.get(dbType);
		if (dialect != null) {
			return dialect.getPageSql(sql, page, pageSize);
		}
		return sql;
	}

	public DBType getDbType() {
		return dbType;
	}

	public void setDbType(DBType dbType) {
		this.dbType = dbType;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
