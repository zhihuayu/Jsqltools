package com.github.jsqltool.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
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
import com.github.jsqltool.exception.JsqltoolBuildException;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.exception.SqlExecuteException;
import com.github.jsqltool.model.DatabaseModel;
import com.github.jsqltool.model.IModel;
import com.github.jsqltool.model.ProfileModel;
import com.github.jsqltool.param.ExecutorSqlParam;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.param.TablesParam;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.sql.SimpleTableInfo;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.SqlPlus.SqlResult;
import com.github.jsqltool.sql.TableColumnInfo;
import com.github.jsqltool.sql.catelog.CatelogHandlerContent;
import com.github.jsqltool.sql.catelog.DefaultCatelogHandler;
import com.github.jsqltool.sql.catelog.ICatelogHandler;
import com.github.jsqltool.sql.delete.DefaultDeleteHandler;
import com.github.jsqltool.sql.delete.DeleteHandlerContent;
import com.github.jsqltool.sql.delete.IdeleteHandler;
import com.github.jsqltool.sql.delete.MySqlDeleteHandler;
import com.github.jsqltool.sql.delete.OracleDeleteHandler;
import com.github.jsqltool.sql.index.IIndexInfoHandler;
import com.github.jsqltool.sql.index.IndexInfoHandlerContent;
import com.github.jsqltool.sql.index.JDBCIndexInfoHandler;
import com.github.jsqltool.sql.insert.DefaultInsertHandler;
import com.github.jsqltool.sql.insert.IinertHandler;
import com.github.jsqltool.sql.insert.InsertHandlerContent;
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
import com.github.jsqltool.sql.typeHandler.IntegerTypeHandler;
import com.github.jsqltool.sql.typeHandler.LongTypeHandler;
import com.github.jsqltool.sql.typeHandler.NumberTypeHandler;
import com.github.jsqltool.sql.typeHandler.TypeHandler;
import com.github.jsqltool.sql.typeHandler.TypeHandlerContent;
import com.github.jsqltool.sql.update.DefaultUpdateDataHandler;
import com.github.jsqltool.sql.update.IUpdateDataHandler;
import com.github.jsqltool.sql.update.MySqlUpdateDataHandler;
import com.github.jsqltool.sql.update.OracleUpdateDataHandler;
import com.github.jsqltool.sql.update.UpdateDataHandlerContent;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.Primary;
import com.github.jsqltool.vo.UpdateResult;

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
	private final IndexInfoHandlerContent indexInfoHandlerContent;
	// 更新数据处理器
	private final UpdateDataHandlerContent updateDataHandlerContent;
	// 插入数据处理器
	private final InsertHandlerContent insertHandlerContent;
	// 删除数据处理器
	private final DeleteHandlerContent deleteHandlerContent;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JsqltoolBuilder() {
		InputStream config = null;
		try {
			config = JsqltoolBuilder.class.getResourceAsStream("/jsqltool.properties");
			Properties prop = new Properties();
			prop.load(config);
			// 检查jsqltool.model.customeClass属性是否存在，如果存在则使用自定义模式
			String customerModel = prop.getProperty("jsqltool.model.customeClass");
			if (StringUtils.isBlank(customerModel)) {
				// 内置模式
				String m = prop.getProperty("jsqltool.model");
				if (StringUtils.equalsIgnoreCase(m, "databaseProfile")) {
					model = new DatabaseModel(prop);
				} else {
					model = new ProfileModel(prop);
				}
			} else {
				// 自定义模式
				try {
					Class clazz = Class.forName(customerModel);
					if (IModel.class.isAssignableFrom(clazz)) {
						Constructor constructor = null;
						try {
							constructor = clazz.getConstructor(Properties.class);
						} catch (NoSuchMethodException | SecurityException e) {
						}
						try {
							if (constructor == null) {
								model = (IModel) clazz.newInstance();
							} else {
								model = (IModel) constructor.newInstance(prop);
							}
						} catch (Exception e) {
							throw new JsqltoolParamException(customerModel + "实例化失败！");
						}

					} else {
						throw new JsqltoolParamException(customerModel + "必须实现com.github.jsqltool.model.IModel接口！");
					}
				} catch (ClassNotFoundException e) {
					throw new JsqltoolParamException(customerModel + "不存在", e);
				}

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
			typeHandlerContent.addFirst(new NumberTypeHandler());
			typeHandlerContent.addFirst(new LongTypeHandler());
			typeHandlerContent.addFirst(new IntegerTypeHandler());
			typeHandlerContent.addFirst(new DateTypeHandler());
			typeHandlerContent.addFirst(new ClobTypeHandler());
			// 索引信息处理器
			indexInfoHandlerContent = new IndexInfoHandlerContent();
			indexInfoHandlerContent.addFirst(new JDBCIndexInfoHandler());
			// 更新数据处理器
			updateDataHandlerContent = new UpdateDataHandlerContent();
			updateDataHandlerContent.addLast(new DefaultUpdateDataHandler());
			updateDataHandlerContent.addFirst(new MySqlUpdateDataHandler());
			updateDataHandlerContent.addFirst(new OracleUpdateDataHandler());
			// 插入数据处理器
			insertHandlerContent = new InsertHandlerContent();
			insertHandlerContent.addFirst(new DefaultInsertHandler());
			// 删除数据处理器
			deleteHandlerContent = new DeleteHandlerContent();
			deleteHandlerContent.addLast(new DefaultDeleteHandler());
			deleteHandlerContent.addFirst(new MySqlDeleteHandler());
			deleteHandlerContent.addFirst(new OracleDeleteHandler());
		} catch (IOException e) {
			throw new JsqltoolBuildException("JsqltoolBuilder创建失败", e);
		} finally {
			if (config != null) {
				try {
					config.close();
				} catch (IOException e) {
					throw new JsqltoolBuildException("JsqltoolBuilder创建失败", e);
				}
			}

		}

	}

	public DBType getDbType(Connection connection) throws SQLException {
		return DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName());
	}

	/**
	 * 获取主键信息
	 * 
	 * @author yzh
	 * @date 2019年6月27日
	 */
	public Primary getPrimayInfo(Connection connection, IndexParam param) throws SQLException {
		return indexInfoHandlerContent.getPrimaryInfo(connection, param);
	}

	/**
	 * 获取索引信息
	 * 
	 * @author yzh
	 * @date 2019年6月27日
	 */
	public List<Index> listIndexInfo(Connection connection, IndexParam param) throws SQLException {
		return indexInfoHandlerContent.getIndexInfo(connection, param);
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
		SqlPlus.setPage(param.getPage(), param.getPageSize(), param.getCount(), param.getIsCount(),
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

	public Connection connect(ConnectionInfo info) {
		return JdbcUtil.connect(info);
	}

	public void close(Connection connection) {
		JdbcUtil.close(connection);
	}

	public List<String> listAllConnectionName(String user) {
		return model.listConnection(user);
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月7日
	* @Description:  保存连接信息
	 */
	public boolean saveConnectionInfo(String user, String oldConnectionName, ConnectionInfo info) {
		return model.save(user, oldConnectionName, info);
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月7日
	* @Description:  删除
	 */
	public boolean deleteConnectionInfo(String user, String connectionName) {
		return model.delete(user, connectionName);
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月8日
	* @Description: 获取connectionInfo信息
	 */
	public ConnectionInfo getConnectionInfo(String user, String connectionName) {
		return model.getConnectionInfo(user, connectionName);
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

	public UpdateResult delete(Connection connect, List<UpdateParam> updates, Boolean force) throws SQLException {
		return deleteHandlerContent.delete(connect, updates, force);
	}

	public void addFirstDeleteHandler(IdeleteHandler handler) {
		deleteHandlerContent.addFirst(handler);
	}

	public void addLastDeleteHandler(IdeleteHandler handler) {
		deleteHandlerContent.addLast(handler);
	}

	public UpdateResult insert(Connection connect, List<UpdateParam> updates) throws SQLException {
		return insertHandlerContent.insert(connect, updates);
	}

	public void addFirstInsertHandler(IinertHandler handler) {
		insertHandlerContent.addFirst(handler);
	}

	public void addLastInsertHandler(IinertHandler handler) {
		insertHandlerContent.addLast(handler);
	}

	public UpdateResult updateData(Connection connect, List<UpdateParam> updates, Boolean force) throws SQLException {
		return updateDataHandlerContent.update(connect, updates, force);
	}

	public void addFirstUpdateDataHandler(IUpdateDataHandler handler) {
		updateDataHandlerContent.addFirst(handler);
	}

	public void addLastUpdateDataHandler(IUpdateDataHandler handler) {
		updateDataHandlerContent.addLast(handler);
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

	public void addFirstIndexInfoHandler(IIndexInfoHandler handler) {
		indexInfoHandlerContent.addFirst(handler);
	}

	public void addLastIndexInfoHandler(IIndexInfoHandler handler) {
		indexInfoHandlerContent.addLast(handler);
	}

	static class Builder {
		private static final JsqltoolBuilder instance = new JsqltoolBuilder();
	}

	public static JsqltoolBuilder builder() {
		return Builder.instance;
	}

	public IModel getModel() {
		return model;
	}

}
