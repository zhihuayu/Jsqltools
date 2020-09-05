package com.github.jsqltool.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.PooledConnection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.CountSqlException;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.model.IModel;
import com.github.jsqltool.param.ProcedureParam;
import com.github.jsqltool.param.ProcedureParam.P_Param;
import com.github.jsqltool.param.SqlParam;
import com.github.jsqltool.result.SqlResult;
import com.github.jsqltool.result.SqlResult.Column;
import com.github.jsqltool.result.SqlResult.Record;
import com.github.jsqltool.sql.page.PageHelper;
import com.github.jsqltool.sql.type.TypeHandler;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.vo.UpdateResult;

public class SqlPlus {

	private static final Logger logger = LoggerFactory.getLogger(SqlPlus.class);
	private static final ThreadLocal<PageHelper> pageHelper = new ThreadLocal<>();
	private static final ExecutorService executorCountSqlService = Executors
			.newCachedThreadPool(new CountTaskThreadFactory());

	public static void removePage() {
		pageHelper.remove();
	}

	public static void setPage(Integer page, Integer pageSize, DBType type) {
		setPage(page, pageSize, null, true, type);
	}

	public static void setPage(Integer page, Integer pageSize, Long count, Boolean isCount, DBType type) {
		setPage(page, pageSize, count, isCount, type, null);
	}

	public static void setPage(Integer page, Integer pageSize, Long count, Boolean isCount, DBType type,
			String countSql) {
		PageHelper helper = new PageHelper();
		helper.setCount(count);
		if (isCount != null)
			helper.setIsCount(isCount);
		else
			helper.setIsCount(true);
		if (page == null || pageSize == null || page.compareTo(0) <= 0 || pageSize.compareTo(0) <= 0) {
			helper.setPage(1);
			helper.setPageSize(100);
		} else {
			helper.setPage(page);
			helper.setPageSize(pageSize);
		}
		helper.setDbType(type);
		helper.setCountSql(countSql);
		pageHelper.set(helper);
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月6日
	* @Description:  用以执行预编译的语句(非查询类)
	*  @param connect
	*  @param sqls
	*  @return
	*  @throws SQLException    参数
	* @return UpdateResult   
	* @throws
	 */
	public static UpdateResult excutePrepareStatement(Connection connect, Map<String, List<SqlParam>> sqls)
			throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connect.getMetaData().getDriverName());
		int effectRowsResult = 0;
		long startTime = System.currentTimeMillis();
		Set<Entry<String, List<SqlParam>>> entrySet = sqls.entrySet();
		for (Entry<String, List<SqlParam>> entry : entrySet) {
			List<SqlParam> value = entry.getValue();
			for (SqlParam sql : value) {
				String rsql = sql.getSql();
				SqlType sqlType = getSqlType(rsql);
				rsql = sqlFilter(rsql, sqlType);
				try (PreparedStatement prepareStatement = connect.prepareStatement(rsql);) {
					Object[] param = sql.getParam();
					for (int i = 0; i < param.length; i++) {
						try {
							Object obj = processObj(connect, dbType, param[i]);
							prepareStatement.setObject(i + 1, obj);
						} catch (Exception e) {
							throw new JsqltoolParamException(param[i] + ":" + e.getMessage(), e);
						}
					}
					int effectRows = prepareStatement.executeUpdate();
					effectRowsResult += effectRows;
					logger.info("{}影响的函数：{}", rsql, effectRows);
				}
			}
			JdbcUtil.commit(connect);
		}
		UpdateResult updateResult = new UpdateResult();
		updateResult.setEffectRows(effectRowsResult);
		long endTime = System.currentTimeMillis();
		updateResult.setCode(UpdateResult.OK);
		updateResult.setMsg("执行成功");
		updateResult.setTime(endTime - startTime);
		return updateResult;
	}

	/**
	* @author yzh
	* @date 2019年7月7日
	* @Description: 处理预编译的SQL的入参
	*  @param dbType
	*  @param object
	*  @return    参数
	* @return Object    返回类型
	* @throws
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object processObj(Connection connect, DBType dbType, Object object)
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		if (object == null) {
			return null;
		}
		if (object instanceof Date && DBType.ORACLE_TYPE == dbType) {
			if (connect instanceof PooledConnection) {
				Connection connection = ((PooledConnection) connect).getConnection();
				if (connection.getClass().getName().equals("oracle.jdbc.driver.T4CConnection")) {
					Class clazz = Class.forName("oracle.jdbc.OracleConnectionWrapper");
					Constructor constructor = clazz.getConstructor(Class.forName("oracle.jdbc.OracleConnection"));
					Object oracleConnectionWrapper = constructor.newInstance(connection);
					Method method = clazz.getMethod("createDATE", object.getClass());
					Object invoke = method.invoke(oracleConnectionWrapper, object);
					return invoke;
				}
			}
		}
		return object;
	}

	@SuppressWarnings("rawtypes")
	public static String executeCall(Connection connection, ProcedureParam procedure) throws SQLException {
		sqlFilter(null, SqlType.CALLABLE);
		StringBuilder result = new StringBuilder();
		if (procedure != null && StringUtils.isNotBlank(procedure.getProcedureName())) {
			long startTime = System.currentTimeMillis();
			JsqltoolBuilder builder = JsqltoolBuilder.builder();
			TypeHandler typeHandler = builder.getTypeHandler();
			StringBuilder sql = new StringBuilder();
			sql.append("{ ");
			JdbcType returnType = procedure.getReturnType();
			if (returnType != null) {
				sql.append(" ?= ");
			}
			sql.append("call ");
			sql.append(JdbcUtil.getTableNameInfo(connection, procedure.getCatalog(), procedure.getSchema(),
					procedure.getProcedureName().trim()));
			sql.append("(");
			// 参数
			if (procedure.getParams() != null && !procedure.getParams().isEmpty()) {
				for (P_Param p : procedure.getParams()) {
					ProcedureParam.checkParam(p);
					sql.append("?,");
				}
				sql.setLength(sql.length() - 1);
			}
			sql.append(") }");
			try (CallableStatement prepareCall = connection.prepareCall(sql.toString());) {
				// 设置参数
				// 返回值
				if (returnType != null) {
					prepareCall.registerOutParameter(1, returnType.TYPE_CODE);
				}
				// 参数
				if (procedure.getParams() != null && !procedure.getParams().isEmpty()) {
					for (P_Param p : procedure.getParams()) {
						// IN参数
						if (p.getType().toUpperCase().contains("IN")) {
							if (p.getParamIndex() != null && p.getParamIndex().compareTo(0) > 0) {
								Integer ind = p.getParamIndex();
								if (returnType != null)
									ind++;
								prepareCall.setObject(ind, typeHandler.getParam(p.getValue(), p.getDataType()));
							} else {
								prepareCall.setObject(p.getParamName().trim(),
										typeHandler.getParam(p.getValue(), p.getDataType()));
							}
						}
						// OUT参数
						if (p.getType().toUpperCase().contains("OUT")) {
							if (p.getParamIndex() != null && p.getParamIndex().compareTo(0) > 0) {
								Integer ind = p.getParamIndex();
								if (returnType != null)
									ind++;
								prepareCall.registerOutParameter(ind, p.getDataType().TYPE_CODE);
							} else {
								prepareCall.registerOutParameter(p.getParamName().trim(), p.getDataType().TYPE_CODE);
							}
						}
					}
				}
				// 执行
				prepareCall.execute();
				result.append("存储过程 " + procedure.getProcedureName() + " 执行成功！");
				// 获取out类型的结果
				if (procedure.getParams() != null && !procedure.getParams().isEmpty()) {
					result.append("\n");
					for (P_Param p : procedure.getParams()) {
						// OUT参数
						if (p.getType().toUpperCase().contains("OUT")) {
							result.append(p.getType().trim().toUpperCase() + "参数 ");
							if (p.getParamIndex() != null && p.getParamIndex().compareTo(0) > 0) {
								Integer ind = p.getParamIndex();
								if (returnType != null)
									ind++;
								result.append(p.getParamIndex() + ":" + prepareCall.getObject(ind));
							} else {
								result.append(p.getParamName() + ":" + prepareCall.getObject(p.getParamName()));
							}
						}
					}
				}
				// 获取结果
				if (returnType != null) {
					result.append("\n返回值为：" + prepareCall.getObject(1));
				}
				result.append("\n耗时：" + (System.currentTimeMillis() - startTime) + "ms");
			}
		} else {
			throw new JsqltoolParamException("存储过程不能为空");
		}
		return result.toString();
	}

	public static String executeCall(Connection connection, String blockSql) throws SQLException {
		StringBuilder result = new StringBuilder();
		if (StringUtils.isNotBlank(blockSql)) {
			long startTime = System.currentTimeMillis();
			try (CallableStatement prepareCall = connection.prepareCall(blockSql);) {
				prepareCall.execute();
				result.append("程序块执行成功：");
				result.append("\n耗时：" + (System.currentTimeMillis() - startTime) + "ms");
			}
		} else {
			throw new JsqltoolParamException("存储过程不能为空");
		}
		return result.toString();
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
			SqlParser sqlParser = null;
			statement = connection.createStatement();
			while ((sqlParser = getSqlParser(bufferedReader)) != null) {
				SqlType type = sqlParser.getSqlType();
				String sql = sqlParser.getSql();
				logger.info("type: {}, sql: {}", type, sql);
				try {
					sql = sqlFilter(sql, type);// 使用sql过滤器
					if (type == SqlType.SELECT || type == SqlType.SHOW) {
						return makeSqlResult(selectAndPage(connection, sql, type), result);
					}
					if (type == SqlType.BLOCK) {
						JsqltoolBuilder builder = JsqltoolBuilder.builder();
						String executeCall = builder.executeCall(connection, sql);
						result.append("\n");
						result.append(executeCall);
						continue;
					}
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
					status = 500;
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

	private static SqlResult makeSqlResult(SqlResult sqlResult, StringBuilder strBuilder) {
		if (sqlResult != null && strBuilder != null && strBuilder.length() > 0) {
			strBuilder.append("\n\r" + sqlResult.getMessage());
			sqlResult.setMessage(strBuilder.toString());
		}
		return sqlResult;
	}

	private static class SqlParser {
		private String sql;
		private SqlType sqlType;

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public SqlType getSqlType() {
			return sqlType;
		}

		public void setSqlType(SqlType sqlType) {
			this.sqlType = sqlType;
		}

	}

	public static SqlParser getSqlParser(BufferedReader bufferedReader) throws IOException {
		String line = null;
		StringBuilder buffer = new StringBuilder();
		boolean isBlock = false;
		SqlParser sql = new SqlParser();
		while ((line = bufferedReader.readLine()) != null) {
			line = line.trim();
			if (line.length() < 1 || line.startsWith("--")) {
				continue;
			}
			// 查看： $begin_block; 和 $end_block;包起来的程序块
			if (!isBlock && buffer.length() == 0 && line.contains("$begin_block") && line.endsWith(";")) {
				isBlock = true;
				continue;
			}
			if (isBlock) {
				if (line.contains("$end_block") && line.endsWith(";")) {
					sql.setSqlType(SqlType.BLOCK);
					break;
				}
				buffer.append(line);
				buffer.append(" ");
			} else {
				if (line.endsWith(";")) {
					buffer.append(line.substring(0, line.length() - 1));
					buffer.append(" ");
					break;
				} else {
					buffer.append(line);
					buffer.append(" ");
				}
			}
		}
		if (buffer.length() > 0) {
			sql.setSql(buffer.toString());
			if (sql.getSqlType() == null)
				sql.setSqlType(getSqlType(sql.getSql()));
		}
		return (buffer.length() > 0 ? sql : null);
	}

	/**
	 * 
	* @author yzh
	 * @throws CloneNotSupportedException 
	* @date 2019年7月27日
	* @Description: 执行select语句并进行分页
	 */
	public static SqlResult selectAndPage(Connection connection, String selectSql, SqlType sqlType)
			throws SQLException, CloneNotSupportedException {
		SqlResult sqlResult = null;
		String sourceSql = selectSql;
		// 分页
		PageHelper page = null;
		long startTime = System.currentTimeMillis();
		Future<Long> count = null;
		if (sqlType == SqlType.SELECT) {
			page = pageHelper.get();
			if (page != null) {
				count = executorCountSqlService.submit(new CountTask(page, selectSql, connection));
				if (page.getPageSize() != null && page.getPageSize().compareTo(0) >= 0) {
					selectSql = page.getPageSql(selectSql);
				}
			}
		}
		sqlResult = select(connection, selectSql);
		try {
			if (count != null) {
				Long c = count.get();
				sqlResult.setCount(c);
			}
		} catch (Exception e) {
			throw new CountSqlException(e);
		}
		if (page != null) {
			sqlResult.setPage(page.getPage());
			sqlResult.setPageSize(page.getPageSize());
		}
		long endTime = System.currentTimeMillis();
		String message = String.format("SQL[%s],%n", sourceSql) + sqlResult.getMessage();
		message += "，用时：" + (endTime - startTime) + "ms";
		sqlResult.setMessage(message);
		return sqlResult;
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月27日
	* @Description: 仅仅执行简单的select或者show语句
	 */
	public static SqlResult select(Connection connection, String selectSql) throws SQLException {
		long startTime = System.currentTimeMillis();
		try (Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(selectSql);) {
			ResultSetMetaData medaData = resultSet.getMetaData();
			List<Column> columns = getColumns(medaData);
			List<Record> records = getRecords(resultSet);
			SqlResult sqlResult = SqlResult.success("status:success");
			sqlResult.setColumns(columns);
			sqlResult.setRecords(records);
			sqlResult.setCount(records != null ? records.size() : 0);
			long endTime = System.currentTimeMillis();
			logger.debug("execute sql {} times:{} ms", selectSql, endTime - startTime);
			return sqlResult;
		}
	}

	/**
	 *  可以执行多个update语句
	* @author yzh
	* @date 2019年7月9日
	* @Description sql an SQL Data Manipulation Language (DML) statement, such as INSERT, UPDATE or DELETE; or an SQL statement that returns nothing, such as a DDL statement.
	* @return int    返回类型
	 */
	public static int executeUpdate(Connection connection, List<String> sqls) throws SQLException {
		if (sqls == null || sqls.isEmpty()) {
			throw new JsqltoolParamException("没有可以执行的excuteUpdate的SQL语句！");
		}
		int rows = 0;
		for (String sql : sqls) {
			long startTime = System.currentTimeMillis();
			try (Statement statement = connection.createStatement();) {
				int effective = statement.executeUpdate(sql);
				rows += effective;
				logger.debug("execute sql {} 影响的行数为：{}，时间为:{} ms",
						new Object[] { sql, effective, System.currentTimeMillis() - startTime });
			}
		}
		JdbcUtil.commit(connection);
		return rows;
	}

	/**
	 * 获取列的元数据信息
	 * 
	 * @author yzh
	 * @date 2019年6月29日
	 */
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

	private static String sqlFilter(String sql, SqlType sqlType) {
		return JsqltoolBuilder.builder().getSqlFilter().sqlFilter(sql, sqlType);
	}

	@SuppressWarnings("rawtypes")
	private static List<Record> getRecords(ResultSet resultSet) throws SQLException {
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
		}
		return records;
	}

	public static SqlType getSqlType(String sql) {
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
			return SqlType.UNKNOWN;
		}

		String word = sql.substring(i, j).toLowerCase();

		if (word.equals("create")) {
			return SqlType.CREATE;
		} else if (word.equals("insert")) {
			return SqlType.INSERT;
		} else if (word.equals("update")) {
			return SqlType.UPDATE;
		} else if (word.equals("delete")) {
			return SqlType.DELETE;
		} else if (word.equals("select")) {
			return SqlType.SELECT;
		} else if (word.equals("alter")) {
			return SqlType.ALTER;
		} else if (word.equals("drop")) {
			return SqlType.DROP;
		} else if (word.equals("show")) {
			return SqlType.SHOW;
		} else {
			return SqlType.UNKNOWN;
		}
	}

	// 专门用于执行count表的任务
	private static class CountTask implements Callable<Long> {

		private final PageHelper page;
		private final String selectSql;
		private final Connection connection;

		public CountTask(PageHelper page, String selectSql, Connection connection)
				throws SQLException, CloneNotSupportedException {
			this.page = page;
			this.selectSql = selectSql;
			this.connection = connection;
		}

		@Override
		public Long call() throws SQLException {
			return page.getCountFromSql(connection, selectSql);
		}

	}

	private static class CountTaskThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		CountTaskThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "JsqlTool-countPool-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException, SQLException, ExecutionException {
		IModel model = JsqltoolBuilder.builder().getModel();
		ConnectionInfo loadConnectionInfo = model.getConnectionInfo("", "测试MySql");
		Connection connect = JdbcUtil.connect(loadConnectionInfo);

		execute(connect, "use test");
		String sql = "  select" + " * from student;";
		System.out.println(execute(connect, sql));
		JdbcUtil.close(connect);

	}

}
