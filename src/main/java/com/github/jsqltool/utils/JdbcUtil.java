package com.github.jsqltool.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.CannotParseUrlException;
import com.github.jsqltool.exception.DatasourceNullException;
import com.github.jsqltool.exception.SqlExecuteException;

public class JdbcUtil {

	private final static Map<String, DataSource> FACTORY_MAP = new ConcurrentHashMap<String, DataSource>();

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
			Connection conn = JdbcUtil.connect(info.getDriverClassName(), info.getUrl(), info.getUserName(),
					info.getPassword());
			// conn.setAutoCommit(true);
			// this.conn.setTransactionIsolation(isolationLivels[dbConnection.getIsolationLevel()]);
			conn.setReadOnly(false);
			return conn;
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Connection connect(String driverClassName, String url, String userName, String pwd)
			throws SQLException {
		if (StringUtils.containsIgnoreCase(driverClassName, "mysql")) {
		}
		DataSource dataSource = getDataSource(driverClassName, url, userName, pwd);
		if (dataSource == null) {
			throw new DatasourceNullException("can not get DataSource instance");
		}
		return dataSource.getConnection();
	}

	public static DataSource getDataSource(String driverClassName, String url, String userName, String pwd) {
		String key = getDataSourceKey(url, userName);
		if (StringUtils.isBlank(key)) {
			throw new CannotParseUrlException("can not parse url param");
		}

		DataSource dataSource = FACTORY_MAP.get(key);
		if (dataSource == null) {
			synchronized (FACTORY_MAP) {
				dataSource = FACTORY_MAP.get(key);
				if (dataSource == null) {
					DruidDataSource createDateSource = new DruidDataSource();
					createDateSource.setDriverClassName(driverClassName);
					createDateSource.setUrl(url);
					createDateSource.setUsername(userName);
					createDateSource.setPassword(pwd);
					if (StringUtils.containsIgnoreCase(driverClassName, "oracle"))
						createDateSource.addConnectionProperty("remarksReporting", "true");
					if (StringUtils.containsIgnoreCase(driverClassName, "mysql"))
						createDateSource.addConnectionProperty("useInformationSchema", "true");
					// 其他基本参数
					createDateSource.setConnectionErrorRetryAttempts(3);
					createDateSource.setAsyncCloseConnectionEnable(true);
					createDateSource.setInitialSize(2);
					createDateSource.setMinIdle(2);
					createDateSource.setMaxActive(20);
					createDateSource.setMaxWait(60000L);
					createDateSource.setTimeBetweenEvictionRunsMillis(60000L);
					createDateSource.setMinEvictableIdleTimeMillis(300000L);
					createDateSource.setPoolPreparedStatements(true);
					createDateSource.setMaxPoolPreparedStatementPerConnectionSize(20);
					FACTORY_MAP.put(key, createDateSource);
					dataSource = createDateSource;
				}
			}
		}
		return dataSource;
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
		}
		return null;
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
	 * @date 2019年6月28日
	 */
	public static String getTableNameInfo(Connection connection, String catalog, String schema, String tableName) {
		String tableInfo = "";
		if (StringUtils.isNotBlank(catalog)) {
			tableInfo += StringUtils.trim(catalog) + ".";
		}
		if (StringUtils.isNotBlank(schema)) {
			tableInfo += StringUtils.trim(schema) + ".";
		}
		tableInfo += tableName;
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

}
