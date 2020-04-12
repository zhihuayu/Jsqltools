package com.github.jsqltool.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DataSourceNotAvailableException;
import com.alibaba.druid.pool.DruidDataSource;
import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.CannotParseUrlException;
import com.github.jsqltool.exception.DatasourceNullException;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.exception.SqlExecuteException;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.ProcedureInfoParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.result.ColumnInfo;
import com.github.jsqltool.result.FunctionInfo;
import com.github.jsqltool.result.SimpleTableInfo;
import com.github.jsqltool.result.TableColumnInfo;
import com.github.jsqltool.result.TypeInfo;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.Primary;

public class JdbcUtil {

	private final static Map<String, DataSource> FACTORY_MAP = new ConcurrentHashMap<String, DataSource>();
	private final static Map<DBType, Set<TypeInfo>> TypeInfo_Map = new ConcurrentHashMap<>();

	/**
	 * @param user:登录该系统的用户名，如果为""或null则代表是单机模式，没有用户
	 * @param connectName:连接数据库的名称
	 * @author yzh
	 * @date 2019年6月17日
	 */
	public static Connection connect(String user, String connectName) {
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		ConnectionInfo connectionInfo = builder.getConnectionInfo(user, connectName);
		return connect(connectionInfo);
	}

	public static Connection connect(ConnectionInfo info) {
		try {
			Connection conn = connect(info.getDriverClassName(), info.getUrl(), info.getUserName(), info.getPassword(),
					info.getProp());
			// conn.setAutoCommit(true);
			// this.conn.setTransactionIsolation(isolationLivels[dbConnection.getIsolationLevel()]);
			conn.setReadOnly(false);
			return conn;
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Connection connect(String driverClassName, String url, String userName, String pwd, Properties prop)
			throws SQLException {
		DataSource dataSource = getDataSource(driverClassName, url, userName, pwd, prop);
		if (dataSource == null) {
			throw new DatasourceNullException("can not get DataSource instance");
		}
		DruidDataSource d = null;
		if (dataSource instanceof DruidDataSource) {
			d = (DruidDataSource) dataSource;
			if (d.isClosed()) {
				d.restart();
			}
		}
		Throwable th = null;
		for (int i = 0; i < 3; i++) {
			try {
				return dataSource.getConnection();
			} catch (java.sql.SQLTimeoutException e) {
				th = e;
				break;
			} catch (Exception e) {
				if (d != null) {
					d.close();
					d.restart();
				}
				th = e;
			}
		}
		if (d != null)
			d.close();
		throw new DataSourceNotAvailableException(th);
	}

	public static DataSource getDataSource(String driverClassName, String url, String userName, String pwd,
			Properties prop) {
		String key = getDataSourceKey(url, userName);
		if (StringUtils.isBlank(key)) {
			throw new CannotParseUrlException("can not parse url param");
		}

		DataSource dataSource = FACTORY_MAP.get(key);
		if (dataSource == null) {
			synchronized (FACTORY_MAP) {
				dataSource = FACTORY_MAP.get(key);
				if (dataSource == null) {
					DruidDataSource druidDateSource = new DruidDataSource();
					druidDateSource.setDefaultAutoCommit(true);
					druidDateSource.setDriverClassName(driverClassName);
					druidDateSource.setUrl(url);
					druidDateSource.setUsername(userName);
					druidDateSource.setPassword(pwd);
					// 添加属性
					addConnectionProperty(druidDateSource, prop);
					// 为Oracle和mysql添加属性
					if (StringUtils.containsIgnoreCase(driverClassName, "oracle"))
						druidDateSource.addConnectionProperty("remarksReporting", "true");
					if (StringUtils.containsIgnoreCase(driverClassName, "mysql"))
						druidDateSource.addConnectionProperty("useInformationSchema", "true");
					// 其他基本参数
					druidDateSource.setConnectionErrorRetryAttempts(3);
					druidDateSource.setAsyncCloseConnectionEnable(false);
//					druidDateSource.setKeepAlive(true);
//					druidDateSource.setKeepAliveBetweenTimeMillis(keepAliveBetweenTimeMillis);
					druidDateSource.setAsyncInit(true);
					druidDateSource.setInitialSize(2);
					druidDateSource.setMinIdle(2);
					druidDateSource.setMaxActive(20);
					druidDateSource.setMaxWait(60000L);
					druidDateSource.setTimeBetweenEvictionRunsMillis(60000L);
					druidDateSource.setMinEvictableIdleTimeMillis(300000L);
					druidDateSource.setPoolPreparedStatements(true);
					druidDateSource.setMaxPoolPreparedStatementPerConnectionSize(20);
					try {
						druidDateSource.setFilters("stat");
					} catch (Exception e) {
					}
					/**
					 * 配置removeAbandoned对性能会有一些影响，建议怀疑存在泄漏之后再打开。在下面的配置中，如果连接超过30分钟未关闭，就会被强行回收，并且日志记录连接申请时的调用堆栈。
					 */
					/*
					 * druidDateSource.setRemoveAbandoned(true); // 打开removeAbandoned功能
					 * druidDateSource.setRemoveAbandonedTimeout(1800); // 1800秒，也就是30分钟
					 * druidDateSource.setLogAbandoned(true); // 关闭abanded连接时输出错误日志
					 */
					FACTORY_MAP.put(key, druidDateSource);
					dataSource = druidDateSource;
				}
			}
		}
		return dataSource;
	}

	private static void addConnectionProperty(DruidDataSource createDateSource, Properties prop) {
		if (createDateSource != null && prop != null && !prop.isEmpty()) {
			Set<String> stringPropertyNames = prop.stringPropertyNames();
			if (stringPropertyNames != null) {
				for (String key : stringPropertyNames) {
					String value = prop.getProperty(key);
					if (StringUtils.isNoneBlank(key, value)) {
						createDateSource.addConnectionProperty(key.trim(), value.trim());
					}
				}
			}
		}
	}

	private static String getDataSourceKey(String url, String userName) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		DBType dbType = DBType.getDBTypeByUrl(url);
		if (dbType == DBType.ORACLE_TYPE) {
			String base = url.substring(url.indexOf("@") + 1);
			StringBuilder sb = new StringBuilder();
			sb.append("oracle::");
			sb.append(base.substring(0, base.indexOf(":")));
			sb.append("::");
			base = base.substring(base.indexOf(":") + 1);
			String dbName = StringUtil.findRegStr(base, "(?<=[/:])[^/?]+");
			if (StringUtils.isNotBlank(dbName)) {
				sb.append(dbName);
			}
			if (StringUtils.isNotBlank(userName)) {
				sb.append(":" + userName);
			}
			return sb.toString();
		} else if (dbType == DBType.MYSQL_TYPE) {
			String base = url.substring(url.indexOf("//") + 2);
			StringBuilder sb = new StringBuilder();
			sb.append("mysql::");
			sb.append(base.substring(0, base.indexOf(":")));
			sb.append("::");
			base = base.substring(base.indexOf(":") + 1);
			String dbName = StringUtil.findRegStr(base, "(?<=[/:])[^/?]+");
			if (StringUtils.isNotBlank(dbName)) {
				sb.append(dbName);
			}
			if (StringUtils.isNotBlank(userName)) {
				sb.append(":" + userName);
			}
			return sb.toString();
		} else if (dbType == DBType.SQLSERVER_TYPE) {
			String base = url.substring(url.indexOf("//") + 2);
			StringBuilder sb = new StringBuilder();
			sb.append("sqlserver::");
			sb.append(base.substring(0, base.indexOf(";")));
			sb.append("::");
			base = base.substring(base.indexOf(";") + 1);
			String dbName = StringUtil.findRegStr(base, "(?<=[Dd]atabaseName=)[^/;?]+");
			if (StringUtils.isNotBlank(dbName)) {
				sb.append(dbName);
			}
			if (StringUtils.isNotBlank(userName)) {
				sb.append(":" + userName);
			}
			return sb.toString();
		} else {
			return url + ":" + userName;
		}
	}

	/**
	 * @param connection
	 */
	public static void commit(Connection connection) {
		if (connection != null) {
			try {
				if (!connection.getAutoCommit())
					connection.commit();
			} catch (SQLException e) {
				throw new SqlExecuteException("commit失败：", e);
			}
		}
	}

	public static void rollBack(Connection connection) {
		if (connection != null) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw new SqlExecuteException("rollback失败：", e);
			}
		}
	}

	public static void rollBack(Connection connection, Savepoint savepoint) {
		if (connection != null) {
			try {
				connection.rollback(savepoint);
			} catch (SQLException e) {
				throw new SqlExecuteException("rollback失败：", e);
			}
		}
	}

	/**
	 * @param connection
	 */
	public static void rollback(Connection connection) {
		if (connection != null) {
			try {
				connection.rollback();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * 获取table的名称（全名）：如：databaseName.tableName
	 * 
	 * @author yzh
	 * @throws SQLException 
	 * @date 2019年6月28日
	 */
	public static String getTableNameInfo(Connection connection, String catalog, String schema, String tableName)
			throws SQLException {
		String tableInfo = "";
		if (StringUtils.isNotBlank(catalog) && !catalog.equalsIgnoreCase("undefined")) {
			tableInfo += covertName(connection, catalog) + ".";
		}
		if (StringUtils.isNotBlank(schema) && !schema.equalsIgnoreCase("undefined")) {
			tableInfo += covertName(connection, schema) + ".";
		}
		tableInfo += covertName(connection, tableName);
		return tableInfo;
	}

	/**
	 * @param connection
	 */
	public static void close(Connection connection) {
		if (connection != null) {
			boolean closed = false;
			try {
				closed = connection.isClosed();
			} catch (SQLException e) {
			}
			if (closed == false) {
				try {
					connection.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * @param statement
	 */
	public static void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * @param resultSet
	 */
	public static void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月11日
	* @Description: 获取数据库支持的类型，可能会从缓存中拿
	 */
	public static Set<TypeInfo> getTypeInfo(Connection connection) throws SQLException {
		DBType type = DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName());
		if (type != null && type != DBType.OTHER_TYPE) {
			if (TypeInfo_Map.containsKey(type)) {
				return TypeInfo_Map.get(type);
			} else {
				Set<TypeInfo> result = newTypeInfo(connection);
				TypeInfo_Map.put(type, result);
				return result;
			}
		}
		return newTypeInfo(connection);
	}

	/**
	* @author yzh
	* @date 2019年7月11日
	* @Description: 获取索引信息
	 */
	public static List<Index> getIndexInfo(Connection connect, IndexParam param) throws SQLException {
		DatabaseMetaData metaData = connect.getMetaData();
		String catelog = covertName(connect, param.getCatalog());
		String schema = covertName(connect, param.getSchema());
		String table = covertName(connect, param.getTable());
		Boolean unique = param.getUnique();
		try (ResultSet resultSet = metaData.getIndexInfo(catelog, schema, table, unique, false);) {
			List<Index> result = DBUtil.getIndexInfo(resultSet);
			// 如果有unique索引，再来确定是否其是不是primary索引
			if (!result.isEmpty()) {
				boolean hasUnique = false;
				for (Index ind : result) {
					if (!ind.getNonUnique()) {
						hasUnique = true;
						break;
					}
				}
				if (hasUnique) {
					// 获取主键索引
					Primary primaryInfo = getPrimaryInfo(connect, param);
					if (primaryInfo != null) {
						for (Index ind : result) {
							if (ind.getIndexName().equals(primaryInfo.getPrimaryName())) {
								ind.setIsPrimary(true);
							}
						}
					}
				}
			}
			return result;
		}

	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月12日
	* @Description: 
	*  @param 根据数据类型来获取诸如：catalog,shema和表的名称
	 * @throws SQLException 
	 */
	public static String covertName(Connection connect, String name) throws SQLException {
		if (StringUtils.isBlank(name))
			return name;
		DatabaseMetaData metaData = connect.getMetaData();
		boolean storesUpperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();
		boolean storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();
		if (storesUpperCaseIdentifiers) {
			return StringUtils.upperCase(name).trim();
		} else if (storesLowerCaseIdentifiers) {
			return StringUtils.lowerCase(name).trim();
		} else {
			return name.trim();
		}
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月11日
	* @Description: 获取主键信息
	 */
	public static Primary getPrimaryInfo(Connection connect, IndexParam param) throws SQLException {
		DatabaseMetaData metaData = connect.getMetaData();
		String catalog = covertName(connect, param.getCatalog());
		String schema = covertName(connect, param.getSchema());
		String table = covertName(connect, param.getTable());
		try (ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schema, table);) {
			return DBUtil.getPrimaryInfo(primaryKeys);
		}
	}

	/**
	 * 
	* @author yzh
	* @date 2019年8月10日
	* @Description: 获取table的简要信息
	 */
	public static List<SimpleTableInfo> listTableInfo(Connection connection, DBObjectParam param) throws SQLException {
		if (StringUtils.isBlank(param.getType())) {
			throw new JsqltoolParamException("type参数不能为空");
		}
		String catelog = StringUtils.isBlank(param.getCatalog()) ? null : param.getCatalog().toUpperCase();
		String schema = StringUtils.isBlank(param.getSchema()) ? null : param.getSchema().toUpperCase();
		String tableName = JdbcUtil.covertName(connection, param.getName());
		try (ResultSet rset = connection.getMetaData().getTables(catelog, schema, tableName,
				new String[] { param.getType() });) {
			return DBUtil.getTableInfo(rset);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ArrayList<SimpleTableInfo>();
	}

	public static List<TableColumnInfo> listTableColumnInfo(Connection connection, TableColumnsParam param) {
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			String catelog = StringUtils.isBlank(param.getCatalog()) ? null : param.getCatalog().toUpperCase();
			String schema = StringUtils.isBlank(param.getSchema()) ? null : param.getSchema().toUpperCase();
			String tableName = StringUtils.upperCase(param.getTableName());
			List<TableColumnInfo> columns = null;
			// 1.基本信息
			try (ResultSet rs = dbmd.getColumns(catelog, schema, tableName, "%");) {
				columns = DBUtil.getTableColumn(rs);
			}
			// 2.PrimayKey信息
			try (ResultSet rs = dbmd.getPrimaryKeys(catelog, schema, StringUtils.upperCase(tableName));) {
				while (rs.next()) {
					for (TableColumnInfo c : columns) {
						if (StringUtils.equalsIgnoreCase(c.getTableName(), rs.getString(3))
								&& StringUtils.equalsIgnoreCase(c.getColumnName(), rs.getString(4))) {
							c.setPkComponent(true);
						}
					}
				}
			}
			return columns;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月11日
	* @Description: 每次都会重新获取新的TypeInfo
	*  @param connection
	 */
	public static Set<TypeInfo> newTypeInfo(Connection connection) throws SQLException {
		try (ResultSet resultSet = connection.getMetaData().getTypeInfo();) {
			Set<TypeInfo> types = DBUtil.getTypeInfo(resultSet);
			types = filterTypeInfo(types);
			return types;
		}
	}

	private static Set<TypeInfo> filterTypeInfo(Set<TypeInfo> types) {
		Set<TypeInfo> result = new LinkedHashSet<>();
		for (TypeInfo type : types) {
			// 过滤掉UNSIGNED类型
			if (type.getTypeName().contains("UNSIGNED")) {
				continue;
			}
			result.add(type);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static List<FunctionInfo> getFunctionsOrProcedure(Connection connection, DBObjectParam param)
			throws SQLException {
		ProcedureInfoParam ps = new ProcedureInfoParam();
		ps.setCatalog(param.getCatalog());
		ps.setSchema(param.getSchema());
		if (StringUtils.isNoneBlank(param.getType()) && param.getType().equalsIgnoreCase("PROCEDURE")) {
			return getProcedure(connection, ps);
		} else if (StringUtils.isNoneBlank(param.getType()) && param.getType().equalsIgnoreCase("FUNCTION")) {
			return getFunctions(connection, ps);
		}
		return Collections.EMPTY_LIST;
	}

	public static List<FunctionInfo> getFunctions(Connection connection, ProcedureInfoParam param) throws SQLException {
		String catalog = covertName(connection, param.getCatalog());
		String schema = covertName(connection, param.getSchema());
		String procedureName = covertName(connection, param.getProcedureName());
		List<FunctionInfo> result = new ArrayList<>();
		try (ResultSet procedures = connection.getMetaData().getFunctions(catalog, schema, procedureName);) {
			if (procedures != null) {
				while (procedures.next()) {
					FunctionInfo info = new FunctionInfo();
					info.setCatalog(procedures.getString("FUNCTION_CAT"));
					info.setSchema(procedures.getString("FUNCTION_SCHEM"));
					info.setName(procedures.getString("FUNCTION_NAME"));
					info.setRemark(procedures.getString("REMARKS"));
					int int1 = procedures.getByte("FUNCTION_TYPE");
					if (int1 == 2) {
						info.setHasReturnValue(true);
					} else if (int1 == 1) {
						info.setHasReturnValue(false);
					}
					info.setSpecificName(procedures.getString("SPECIFIC_NAME"));
					result.add(info);
				}

			}
			return result;
		}

	}

	/**
	 * 
	* @author yzh
	* @date 2019年8月11日
	* @Description: 获取存储过程和存储函数
	 */
	public static List<FunctionInfo> getProcedure(Connection connection, ProcedureInfoParam param) throws SQLException {
		String catalog = covertName(connection, param.getCatalog());
		String schema = covertName(connection, param.getSchema());
		String procedureName = covertName(connection, param.getProcedureName());
		List<FunctionInfo> result = new ArrayList<>();
		try (ResultSet procedures = connection.getMetaData().getProcedures(catalog, schema, procedureName);) {
			if (procedures != null) {
				while (procedures.next()) {
					FunctionInfo info = new FunctionInfo();
					info.setCatalog(procedures.getString("PROCEDURE_CAT"));
					info.setSchema(procedures.getString("PROCEDURE_SCHEM"));
					info.setName(procedures.getString("PROCEDURE_NAME"));
					info.setRemark(procedures.getString("REMARKS"));
					int int1 = procedures.getInt("PROCEDURE_TYPE");
					if (int1 == 2) {
						info.setHasReturnValue(true);
					} else if (int1 == 1) {
						info.setHasReturnValue(false);
					}
					info.setSpecificName(procedures.getString("SPECIFIC_NAME"));
					result.add(info);
				}

			}
			return result;
		}

	}

	public static void main(String[] args) throws SQLException {
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		Connection connect = builder.connect("测试MySql");
		ProcedureInfoParam param = new ProcedureInfoParam();
		param.setCatalog("lxfy");
//		param.setSchema("scott");
		List<FunctionInfo> procedure = getProcedure(connect, param);

//		 List<FunctionInfo> functions = getFunctions(connect, param);
		System.out.println(procedure);

	}

	/**
	* @author yzh
	* @date 2019年7月11日
	* @Description: 获取表格的列信息
	*  @throws SQLException    参数
	 */
	public static List<ColumnInfo> getTableColumnInfo(Connection conn, TableColumnsParam param) throws SQLException {
		try (ResultSet rs = conn.getMetaData().getColumns(param.getCatalog(), param.getSchema(), param.getTableName(),
				null);) {
			return DBUtil.getColumnInfo(rs);
		}
	}

}
