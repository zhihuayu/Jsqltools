package com.github.jsqltool.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.SqlExecuteException;
import com.github.jsqltool.model.DatabaseModel;
import com.github.jsqltool.model.IModel;
import com.github.jsqltool.model.ProfileModel;
import com.github.jsqltool.param.ExecutorSqlParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.param.TablesParam;
import com.github.jsqltool.sql.SimpleTableInfo;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.SqlPlus.SqlResult;
import com.github.jsqltool.sql.TableColumnInfo;
import com.github.jsqltool.sql.catelog.CatelogHandlerContent;
import com.github.jsqltool.sql.catelog.DefaultCatelogHandler;
import com.github.jsqltool.sql.catelog.ICatelogHandler;
import com.github.jsqltool.sql.schema.DefaultSchemaHandler;
import com.github.jsqltool.sql.schema.IScheamHandler;
import com.github.jsqltool.sql.schema.SchemaHandlerContent;
import com.github.jsqltool.sql.table.DefaultTableHandler;
import com.github.jsqltool.sql.table.ITableHandler;
import com.github.jsqltool.sql.table.TableHandlerContent;
import com.github.jsqltool.sql.tableColumn.DefaultTableColumnHandler;
import com.github.jsqltool.sql.tableColumn.ITableColumnHandler;
import com.github.jsqltool.sql.tableColumn.TableColumnHandlerContent;
import com.github.jsqltool.sql.typeHandler.ClobTypeHandler;
import com.github.jsqltool.sql.typeHandler.DateTypeHandler;
import com.github.jsqltool.sql.typeHandler.TypeHandler;
import com.github.jsqltool.sql.typeHandler.TypeHandlerContent;
import com.github.jsqltool.utils.JdbcUtil;

/**
 * 获取IModel实例，用于配置jsqltool的模式，支持数据库和配置文件的模式
 * 
 * @author yzh
 * @date 2019年6月16日
 */
public class JsqltoolBuilder {

	Logger logger = LoggerFactory.getLogger(JsqltoolBuilder.class);

	private final IModel model;
	private final CatelogHandlerContent catelog;
	private final SchemaHandlerContent schema;
	private final TableHandlerContent table;
	private final TableColumnHandlerContent tableColumn;
	private final TypeHandlerContent typeHandlerContent;

	private JsqltoolBuilder() {
		try {
			InputStream config = JsqltoolBuilder.class.getResourceAsStream("config.properties");
			Properties prop = new Properties();
			prop.load(config);
			String m = prop.getProperty("jsqltool.model");
			if (StringUtils.equalsIgnoreCase(m, "databaseProfile")) {
				model = new DatabaseModel();
			} else {
				model = new ProfileModel();
			}
			// 初始化CatelogHandlerContent实例
			catelog = new CatelogHandlerContent();
			catelog.addLast(new DefaultCatelogHandler());
			// 初始化SchemaHandlerContent实例
			schema = new SchemaHandlerContent();
			schema.addLast(new DefaultSchemaHandler());
			// 初始化TableHandlerContent实例
			table = new TableHandlerContent();
			table.addLast(new DefaultTableHandler());
			// 初始化TableHandlerContent实例
			tableColumn = new TableColumnHandlerContent();
			tableColumn.addLast(new DefaultTableColumnHandler());
			// 初始化类型处理器
			typeHandlerContent = new TypeHandlerContent();
			typeHandlerContent.addFirst(new DateTypeHandler());
			typeHandlerContent.addFirst(new ClobTypeHandler());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 该方法在单机模式中运行，执行完SQL后会关闭连接
	 * 
	 * @author yzh
	 * @date 2019年6月22日
	 */
	public SqlResult executorSql(String connectionName, ExecutorSqlParam param) throws SQLException {
		return executorSql("", connectionName, param);
	}

	/**
	 * 执行完SQL后会关闭连接
	 * 
	 * @author yzh
	 * @date 2019年6月22日
	 */
	public SqlResult executorSql(String user, String connectionName, ExecutorSqlParam param) throws SQLException {
		try (Connection connect = connect(user, connectionName);) {
			return executorSql(connect, param);
		}
	}

	/**
	 * 执行完SQL后并没有关闭连接，需要自己手动关闭连接
	 * 
	 * @author yzh
	 * @date 2019年6月22日
	 */
	public SqlResult executorSql(Connection connect, ExecutorSqlParam param) throws SQLException {
		DatabaseMetaData metaData = connect.getMetaData();
		if (StringUtils.containsIgnoreCase(metaData.getDriverName(), "mysql")) {
			if (StringUtils.isBlank(param.getCatelog())) {
				logger.warn("mysql数据库必须选择一个databse来执行");
				throw new SqlExecuteException("mysql数据库必须选择一个databse来执行");
			}
			SqlPlus.execute(connect, "use " + param.getCatelog());
		}
		SqlPlus.setPage(param.getPage(), param.getPageSize(),
				DBType.getDBTypeByDriverClassName(metaData.getDriverName()));
		return SqlPlus.execute(connect, param.getSql());
	}

	/**
	 * 在单机模式时调用该方法来获取{@link Connection}，其会忽略user信息
	 * 
	 * @author yzh
	 * @date 2019年6月22日
	 */
	public Connection connect(String connectionName) {
		return JdbcUtil.connect("", connectionName);
	}

	public Connection connect(String user, String connectionName) {
		return JdbcUtil.connect(user, connectionName);
	}

	public void close(Connection connection) {
		JdbcUtil.close(connection);
	}

	public List<String> listAllConnectionName(String user) {
		return model.listConnection(user);
	}

	public List<SimpleTableInfo> listTable(Connection connection, TablesParam param) {
		return table.list(connection, param);
	}

	public void addFirstTableHandler(ITableHandler handler) {
		table.addFirst(handler);
	}

	public void addLastTableHandler(ITableHandler handler) {
		table.addLast(handler);
	}

	public List<TableColumnInfo> getTableColumnInfo(Connection connection, TableColumnsParam param) {
		return tableColumn.list(connection, param);
	}

	/**
	 * TODO 该方法需要以后再做处理
	 * 
	 * @author yzh
	 * @date 2019年6月22日
	 */
	@SuppressWarnings("rawtypes")
	public TypeHandler getTypeHandler() {
		return typeHandlerContent;
	}

	@SuppressWarnings("rawtypes")
	public void addFirstTypeHandler(TypeHandler handler) {
		typeHandlerContent.addFirst(handler);
	}

	@SuppressWarnings("rawtypes")
	public void addLastTypeHandler(TypeHandler handler) {
		typeHandlerContent.addLast(handler);
	}

	public void addFirstTableColumnHandler(ITableColumnHandler handler) {
		tableColumn.addFirst(handler);
	}

	public void addLastTableColumnHandler(ITableColumnHandler handler) {
		tableColumn.addLast(handler);
	}

	public List<String> listSchema(Connection connection, String catelog) {
		return schema.list(connection, catelog);
	}

	public void addFirstSchemaHandler(IScheamHandler handler) {
		schema.addFirst(handler);
	}

	public void addLastSchemaHandler(IScheamHandler handler) {
		schema.addLast(handler);
	}

	public List<String> listCatelog(Connection connection) {
		return catelog.list(connection);
	}

	public void addFirstCatelogHandler(ICatelogHandler handler) {
		catelog.addFirst(handler);
	}

	public void addLastCatelogHandler(ICatelogHandler handler) {
		catelog.addLast(handler);
	}

	public ConnectionInfo getConnectionInfo(String user, String connectionName) {
		return model.getConnectionInfo(user, connectionName);
	}

	static class Builder {
		private static final JsqltoolBuilder instance = new JsqltoolBuilder();
	}

	public static JsqltoolBuilder builder() {
		return Builder.instance;
	}

}