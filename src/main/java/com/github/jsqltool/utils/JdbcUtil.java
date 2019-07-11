package com.github.jsqltool.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.IndexColumn;
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
			Connection conn = JdbcUtil.connect(info.getDriverClassName(), info.getUrl(), info.getUserName(),
					info.getPassword(), info.getProp());
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
		return dataSource.getConnection();
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
					DruidDataSource createDateSource = new DruidDataSource();
					createDateSource.setDriverClassName(driverClassName);
					createDateSource.setUrl(url);
					createDateSource.setUsername(userName);
					createDateSource.setPassword(pwd);
					// 添加属性
					addConnectionProperty(createDateSource, prop);
					// 为Oracle和mysql添加属性
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
		boolean storesUpperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();
		boolean storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();
		String catelog = null;
		String schema = null;
		String table = null;
		if (storesUpperCaseIdentifiers) {
			catelog = StringUtils.upperCase(param.getCatalog());
			schema = StringUtils.upperCase(param.getSchema());
			table = StringUtils.upperCase(param.getTable());
		} else if (storesLowerCaseIdentifiers) {
			catelog = StringUtils.lowerCase(param.getCatalog());
			schema = StringUtils.lowerCase(param.getSchema());
			table = StringUtils.lowerCase(param.getTable());
		} else {
			catelog = param.getCatalog();
			schema = param.getSchema();
			table = param.getTable();
		}

		Boolean unique = param.getUnique();
		List<Index> result = new ArrayList<>();
		try (ResultSet resultSet = metaData.getIndexInfo(catelog, schema, table, unique, false);) {
			while (resultSet.next()) {
				addIndex(resultSet, result);
			}
		}
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

	private static void addIndex(ResultSet resultSet, List<Index> result) throws SQLException {
		if (StringUtils.isNotBlank(resultSet.getString("INDEX_NAME"))) {
			String indexName = resultSet.getString("INDEX_NAME");
			Index index = null;
			for (Index id : result) {
				if (id.getIndexName().equals(indexName)) {
					index = id;
					break;
				}
			}
			if (index == null) {
				index = new Index();
				index.setIndexName(indexName);
				index.setNonUnique(resultSet.getBoolean("NON_UNIQUE"));
				result.add(index);
			}
			IndexColumn indexColumn = new IndexColumn();
			indexColumn.setColumnName(resultSet.getString("COLUMN_NAME"));
			indexColumn.setIndexPosition(resultSet.getByte("ORDINAL_POSITION"));
			System.out.println(resultSet.getString("TYPE"));
			index.addColumn(indexColumn);
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
		boolean storesUpperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();
		boolean storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();
		String catalog = null;
		String schema = null;
		String table = null;
		if (storesUpperCaseIdentifiers) {
			catalog = StringUtils.upperCase(param.getCatalog());
			schema = StringUtils.upperCase(param.getSchema());
			table = StringUtils.upperCase(param.getTable());
		} else if (storesLowerCaseIdentifiers) {
			catalog = StringUtils.lowerCase(param.getCatalog());
			schema = StringUtils.lowerCase(param.getSchema());
			table = StringUtils.lowerCase(param.getTable());
		} else {
			catalog = param.getCatalog();
			schema = param.getSchema();
			table = param.getTable();
		}
		Primary primary = null;
		try (ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schema, table);) {
			while (primaryKeys.next()) {
				primary = getPrimaryKey(primaryKeys, primary);
			}
		}
		return primary;
	}

	private static Primary getPrimaryKey(ResultSet primaryKeys, Primary primary) throws SQLException {
		if (StringUtils.isNotBlank(primaryKeys.getString("COLUMN_NAME"))) {
			if (primary == null) {
				primary = new Primary();
				primary.setPrimaryName(primaryKeys.getString("PK_NAME"));
			}
			IndexColumn indexColumn = new IndexColumn();
			indexColumn.setColumnName(primaryKeys.getString("COLUMN_NAME"));
			indexColumn.setIndexPosition(primaryKeys.getByte("KEY_SEQ"));
			primary.addColumn(indexColumn);
		}
		return primary;
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月11日
	* @Description: 每次都会重新获取新的TypeInfo
	*  @param connection
	 */
	public static Set<TypeInfo> newTypeInfo(Connection connection) throws SQLException {
		ResultSet resultSet = connection.getMetaData().getTypeInfo();
		Set<TypeInfo> types = new LinkedHashSet<>();
		while (resultSet.next()) {
			TypeInfo info = new TypeInfo();
			info.setTypeName(resultSet.getString("TYPE_NAME"));
			info.setDataType(resultSet.getInt("DATA_TYPE"));
			info.setPrecision(resultSet.getInt("PRECISION"));
			info.setNullable(resultSet.getShort("NULLABLE"));
			info.setCaseSensitive(resultSet.getBoolean("CASE_SENSITIVE"));
			info.setAutoIncrement(resultSet.getBoolean("AUTO_INCREMENT"));
			info.setUnsignedAttribute(resultSet.getBoolean("UNSIGNED_ATTRIBUTE"));
			info.setCreateParams(resultSet.getString("CREATE_PARAMS"));
			types.add(info);
		}
		types = filterTypeInfo(types);
		return types;
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

	public static void main(String[] args) throws SQLException {
		System.out.println(null == null);
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		Connection connect = builder.connect("测试Oracle");

		IndexParam param = new IndexParam();
		param.setCatalog("");
		param.setSchema("SCOTT");
		param.setTable("T1");
		param.setUnique(false);
		List<Index> indexInfo = getIndexInfo(connect, param);
		System.out.println(indexInfo);

//		TableColumnsParam param = new TableColumnsParam();
//		param.setCatalog("");
//		param.setSchema("SCOTT");
//		param.setTableName("T1");
//		List<ColumnInfo> tableColumnInfo = getTableColumnInfo(connect, param);
//		System.out.println(tableColumnInfo);

//		Set<TypeInfo> typeInfo = getTypeInfo(connect);
//		System.out.println(typeInfo);

	}

	/**
	* @author yzh
	* @date 2019年7月11日
	* @Description: 获取表格的列信息
	*  @throws SQLException    参数
	 */
	public static List<ColumnInfo> getTableColumnInfo(Connection conn, TableColumnsParam param) throws SQLException {
		ResultSet columns = conn.getMetaData().getColumns(param.getCatalog(), param.getSchema(), param.getTableName(),
				null);
		List<ColumnInfo> infos = new ArrayList<>();
		while (columns.next()) {
			ColumnInfo info = new ColumnInfo();
			info.setColumnName(columns.getString("COLUMN_NAME"));
			info.setColumnSize(columns.getInt("COLUMN_SIZE"));
			info.setDecimalDigits(columns.getInt("DECIMAL_DIGITS"));
			info.setTypeName(columns.getString("TYPE_NAME"));
			info.setDataType(columns.getInt("DATA_TYPE"));
			info.setNullable(columns.getInt("NULLABLE"));
			info.setRemarks(columns.getString("REMARKS"));
			info.setIsNullable(columns.getString("IS_NULLABLE"));
			info.setIsAutoincrement(columns.getString("IS_AUTOINCREMENT"));
			infos.add(info);
		}
		return infos;
	}

	public static class ColumnInfo {

		private String columnName;
		private Integer columnSize;
		/**
		 * the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable. 
		 */
		private Integer decimalDigits;

		/**
		 * Data source dependent type name, for a UDT the type name is fully qualified 
		 */
		private String typeName;
		/**
		 * SQL type from java.sql.Types 
		 */
		private Integer dataType;

		/**
		 *  ◦ columnNoNulls(0) - might not allow NULL values 
		 *  ◦ columnNullable(1) - definitely allows NULL values 
		 *  ◦ columnNullableUnknown(2) - nullability unknown 
		 */
		private Integer nullable;
		private String remarks;
		/**
		 *  ISO rules are used to determine the nullability for a column. 
		 *  ◦ YES --- if the column can include NULLs 
		 *  ◦ NO --- if the column cannot include NULLs 
		 *  ◦ empty string --- if the nullability for the column is unknown 
		 */
		private String isNullable;
		/**
		 *  Indicates whether this column is auto incremented
		 *   ◦ YES --- if the column is auto incremented 
		 *   ◦ NO --- if the column is not auto incremented 
		 *   ◦ empty string --- if it cannot be determined whether the column is auto incremented 
		 */
		private String isAutoincrement;

		public Integer getDecimalDigits() {
			return decimalDigits;
		}

		public void setDecimalDigits(Integer decimalDigits) {
			this.decimalDigits = decimalDigits;
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public Integer getColumnSize() {
			return columnSize;
		}

		public void setColumnSize(Integer columnSize) {
			this.columnSize = columnSize;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public Integer getDataType() {
			return dataType;
		}

		public void setDataType(Integer dataType) {
			this.dataType = dataType;
		}

		public Integer getNullable() {
			return nullable;
		}

		public void setNullable(Integer nullable) {
			this.nullable = nullable;
		}

		public String getRemarks() {
			return remarks;
		}

		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}

		public String getIsNullable() {
			return isNullable;
		}

		public void setIsNullable(String isNullable) {
			this.isNullable = isNullable;
		}

		public String getIsAutoincrement() {
			return isAutoincrement;
		}

		public void setIsAutoincrement(String isAutoincrement) {
			this.isAutoincrement = isAutoincrement;
		}

		@Override
		public String toString() {
			return typeName;
		}

	}

	public static class TypeInfo {

		private String typeName;
		private Integer dataType; // 对应java.sql.Types
		private Integer precision; // 精度
		/**
		指示列能否包含 Null 值。 可以为下列值之一：
			typeNoNulls (0)
			typeNullable (1)
			typeNullableUnknown (2)
		 */
		private Short nullable; //
		private Boolean caseSensitive;
		// 是否支持自增
		private Boolean autoIncrement;
		private Boolean unsignedAttribute;
		private String createParams;

		@Override
		public int hashCode() {
			if (typeName == null)
				return 0;
			return typeName.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj instanceof TypeInfo) {
				TypeInfo info = (TypeInfo) obj;
				if (info.getTypeName() == this.typeName) {
					return true;
				} else if (info.getTypeName() != null && info.getTypeName().equals(this.typeName)) {
					return true;
				}
				return false;
			} else {
				return false;
			}

		}

		public String getCreateParams() {
			return createParams;
		}

		public void setCreateParams(String createParams) {
			this.createParams = createParams;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public Integer getDataType() {
			return dataType;
		}

		public void setDataType(Integer dataType) {
			this.dataType = dataType;
		}

		public Integer getPrecision() {
			return precision;
		}

		public void setPrecision(Integer precision) {
			this.precision = precision;
		}

		public Short getNullable() {
			return nullable;
		}

		public void setNullable(Short nullable) {
			this.nullable = nullable;
		}

		public Boolean getCaseSensitive() {
			return caseSensitive;
		}

		public void setCaseSensitive(Boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
		}

		public Boolean getAutoIncrement() {
			return autoIncrement;
		}

		public void setAutoIncrement(Boolean autoIncrement) {
			this.autoIncrement = autoIncrement;
		}

		public Boolean getUnsignedAttribute() {
			return unsignedAttribute;
		}

		public void setUnsignedAttribute(Boolean unsignedAttribute) {
			this.unsignedAttribute = unsignedAttribute;
		}

		@Override
		public String toString() {
			return typeName;
		}

	}

}
