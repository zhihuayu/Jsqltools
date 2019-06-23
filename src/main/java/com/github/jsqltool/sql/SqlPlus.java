package com.github.jsqltool.sql;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.sql.page.PageHelper;
import com.github.jsqltool.sql.typeHandler.TypeHandler;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.utils.ProfileUtil;

public class SqlPlus {

	private static final int CREATE = 1;
	private static final int INSERT = 2;
	private static final int UPDATE = 3;
	private static final int DELETE = 4;
	private static final int SELECT = 5;
	private static final int ALTER = 6;
	private static final int DROP = 7;
	private static final int SHOW = 8;
	private static final int UNKNOWN = 99;
	private static final Logger logger = LoggerFactory.getLogger(SqlPlus.class);
	private static final ThreadLocal<PageHelper> pageHelper = new ThreadLocal<>();

	public static void removePage() {
		pageHelper.remove();
	}

	public static void setPage(Integer page, Integer pageSize, DBType type) {
		PageHelper helper = new PageHelper();
		if (page == null || pageSize == null || page.compareTo(0) <= 0 || pageSize.compareTo(0) <= 0) {
			helper.setPage(1);
			helper.setPageSize(100);
		} else {
			helper.setPage(page);
			helper.setPageSize(pageSize);
		}
		helper.setDbType(type);
		pageHelper.set(helper);
	}

	public static SqlResult execute(Connection connection, String sql) {
		return execute(connection, new StringReader(sql));
	}

	public static SqlResult execute(Connection connection, Reader reader) {
		int status = 200;
		StringBuilder result = new StringBuilder();
		Statement statement = null;

		try {
			BufferedReader bufferedReader = null;

			if (reader instanceof BufferedReader) {
				bufferedReader = (BufferedReader) reader;
			} else {
				bufferedReader = new BufferedReader(reader);
			}

			String sql = null;
			statement = connection.createStatement();

			while ((sql = getSql(bufferedReader)) != null) {
				int type = getSqlType(sql);
				logger.info("type: {}, sql: {}", type, sql);

				if (type == SELECT) {
					return select(connection, sql, type);
				}

				if (type == SHOW) {
					return select(connection, sql, type);
				}

				try {
					long t1 = System.currentTimeMillis();
					int count = statement.executeUpdate(sql);
					long t2 = System.currentTimeMillis();

					result.append("[SQL]: ");
					result.append(sql);
					result.append("\r\n");
					result.append("Affected rows: ");
					result.append(count);
					result.append("\r\n");
					result.append("Time: ");
					result.append(t2 - t1);
					result.append("ms\r\n\r\n");
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
					result.append("[SQL]: ");
					result.append(sql);
					result.append("\r\n\r\n");
					result.append(e.getMessage());
					result.append("\r\n\r\n");
					JdbcUtil.rollback(connection);
					break;
				}
			}
			JdbcUtil.commit(connection);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			status = 500;
			result.append("[ERROR]: ");
			result.append(e.getMessage());
			result.append("\r\n\r\n");
			JdbcUtil.rollback(connection);
		} finally {
			removePage();
			JdbcUtil.close(statement);
		}
		return new SqlResult(status, result.toString());
	}

	public static String getSql(BufferedReader bufferedReader) throws IOException {
		String line = null;
		StringBuilder buffer = new StringBuilder();

		while ((line = bufferedReader.readLine()) != null) {
			line = line.trim();

			if (line.length() < 1 || line.startsWith("--")) {
				continue;
			}

			if (line.endsWith(";")) {
				buffer.append(line.substring(0, line.length() - 1));
				buffer.append(" ");
				break;
			} else {
				buffer.append(line);
				buffer.append(" ");
			}
		}
		return (buffer.length() > 0 ? buffer.toString() : null);
	}

	public static SqlResult select(Connection connection, String sql, int sqlType) throws SQLException {
		// 分页
		PageHelper page = null;
		long startTime = System.currentTimeMillis();
		long count = 0;
		if (sqlType == SELECT) {
			page = pageHelper.get();
			if (page != null) {
				count = page.getCountSql(connection, sql);
				if (count > page.getPageSize() && page.getPageSize().compareTo(0) >= 0) {
					sql = page.getPageSql(sql);
				}
			}
		}
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
			ResultSetMetaData medaData = resultSet.getMetaData();
			List<Column> columns = getColumns(medaData);
			List<Record> records = getRecords(resultSet, page.getPageSize());
			SqlResult sqlResult = SqlResult.success("success");
			sqlResult.setColumns(columns);
			sqlResult.setRecords(records);
			sqlResult.setCount(count);
			long endTime = System.currentTimeMillis();
			logger.debug("execute sql {} times:{}ms", sql, endTime - startTime);
			if (page != null) {
				sqlResult.setPage(page.getPage());
				sqlResult.setPageSize(page.getPageSize());
			}
			return sqlResult;
		}
	}

	public static int update(Connection connection, String sql) throws SQLException {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			return statement.executeUpdate(sql);
		} finally {
			JdbcUtil.close(statement);
		}
	}

	private static List<Column> getColumns(ResultSetMetaData medaData) throws SQLException {
		int columnCount = medaData.getColumnCount();
		List<Column> columns = new ArrayList<Column>();

		for (int i = 1; i <= columnCount; i++) {
			Column column = new Column();
			String columnLabel = medaData.getColumnLabel(i);
			String columnName = medaData.getColumnName(i);
			int dataType = medaData.getColumnType(i);
			String typeName = medaData.getColumnTypeName(i);
			boolean autoIncrement = medaData.isAutoIncrement(i);

			column.setAlias(columnLabel);
			column.setColumnName(columnName);
			column.setDataType(dataType);
			column.setTypeName(typeName);
			column.setAutoIncrement(autoIncrement);
			columns.add(column);
		}
		return columns;
	}

	@SuppressWarnings("rawtypes")
	private static List<Record> getRecords(ResultSet resultSet, int size) throws SQLException {
		int rows = 0;
		List<Record> records = new ArrayList<Record>();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		TypeHandler typeHandler = JsqltoolBuilder.builder().getTypeHandler();
		while (resultSet.next()) {
			List<Object> values = new ArrayList<Object>();
			for (int i = 1; i <= columnCount; i++) {
				JdbcType type = JdbcType.forCode(metaData.getColumnType(i));
				if (type != null) {
					if (typeHandler.support(type)) {
						values.add(typeHandler.handler(resultSet, i, type));
					} else
						values.add(resultSet.getObject(i));
				} else {
					values.add(resultSet.getObject(i) == null ? null : resultSet.getObject(i).toString());
				}
			}
			records.add(new Record(values));
			rows++;
			if (rows >= size) {
				break;
			}
		}
		return records;
	}

	public static int getSqlType(String sql) {
		int i = 0;
		int length = sql.length();

		while (i < length && sql.charAt(i) <= ' ') {
			i++;
		}

		int j = i;
		while (j < length && sql.charAt(j) > ' ') {
			j++;
		}

		if (j <= i) {
			return UNKNOWN;
		}

		String word = sql.substring(i, j).toLowerCase();

		if (word.equals("create")) {
			return CREATE;
		} else if (word.equals("insert")) {
			return INSERT;
		} else if (word.equals("update")) {
			return UPDATE;
		} else if (word.equals("delete")) {
			return DELETE;
		} else if (word.equals("select")) {
			return SELECT;
		} else if (word.equals("alter")) {
			return ALTER;
		} else if (word.equals("drop")) {
			return DROP;
		} else if (word.equals("show")) {
			return SHOW;
		} else {
			return UNKNOWN;
		}
	}

	public static class SqlResult {

		private int status;
		private String message;
		private List<Column> columns;
		private List<Record> records;
		private long count; // 总数
		private long page; // 当前页
		private long pageSize; // 每页大小

		public SqlResult() {
		}

		public SqlResult(int status, String message) {
			this.status = status;
			this.message = message;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}

		public long getPage() {
			return page;
		}

		public void setPage(long page) {
			this.page = page;
		}

		public long getPageSize() {
			return pageSize;
		}

		public void setPageSize(long pageSize) {
			this.pageSize = pageSize;
		}

		public boolean success() {
			return (this.status == 200);
		}

		public static SqlResult success(String message) {
			return new SqlResult(200, message);
		}

		public static SqlResult error(String message) {
			return new SqlResult(500, message);
		}

		public int getStatus() {
			return this.status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getMessage() {
			return this.message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public List<Column> getColumns() {
			return this.columns;
		}

		public void setColumns(List<Column> columns) {
			this.columns = columns;
		}

		public List<Record> getRecords() {
			return this.records;
		}

		public void setRecords(List<Record> records) {
			this.records = records;
		}
	}

	static class Column {

		private String alias;
		private String columnName;
		private Integer dataType;
		private String typeName;
		private Boolean autoIncrement;

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public Integer getDataType() {
			return dataType;
		}

		public void setDataType(Integer dataType) {
			this.dataType = dataType;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public Boolean getAutoIncrement() {
			return autoIncrement;
		}

		public void setAutoIncrement(Boolean autoIncrement) {
			this.autoIncrement = autoIncrement;
		}

	}

	static class Record {

		private List<Object> values;

		public Record() {
		}

		public Record(List<Object> values) {
			this.values = values;
		}

		public List<Object> getValues() {
			return this.values;
		}

		public void setValues(List<Object> values) {
			this.values = values;
		}

	}

	public static void main(String[] args) throws IOException {
		ConnectionInfo loadConnectionInfo = ProfileUtil.loadConnectionInfo("", "测试MySql");
		Connection connect = JdbcUtil.connect(loadConnectionInfo);
		SqlResult execute = execute(connect, "use test");
		System.out.println(execute);
		String sql = "  select" + " * from student;";
		SqlResult execute2 = execute(connect, sql);
		System.out.println(execute2);
		JdbcUtil.close(connect);
	}

}
