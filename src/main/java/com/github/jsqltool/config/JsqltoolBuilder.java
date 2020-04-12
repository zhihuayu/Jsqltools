package com.github.jsqltool.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.SqlExecuteException;
import com.github.jsqltool.model.IModel;
import com.github.jsqltool.model.ModelBuilder;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.param.DropTableParam;
import com.github.jsqltool.param.ExecutorSqlParam;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.ProcedureParam;
import com.github.jsqltool.param.SelectTableParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.result.ColumnInfo;
import com.github.jsqltool.result.SimpleTableInfo;
import com.github.jsqltool.result.SqlResult;
import com.github.jsqltool.result.TableColumnInfo;
import com.github.jsqltool.result.TypeInfo;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.catelog.ICatelogHandler;
import com.github.jsqltool.sql.catelog.impl.CatelogHandlerContent;
import com.github.jsqltool.sql.createTableView.IcreateTableViewHandler;
import com.github.jsqltool.sql.createTableView.impl.CreateTableViewHandlerContent;
import com.github.jsqltool.sql.delete.IdeleteHandler;
import com.github.jsqltool.sql.delete.impl.DeleteHandlerContent;
import com.github.jsqltool.sql.dropTable.IdropTableHandler;
import com.github.jsqltool.sql.dropTable.impl.DropTableHandlerContent;
import com.github.jsqltool.sql.excuteCall.ExecuteCallHandler;
import com.github.jsqltool.sql.excuteCall.impl.ExecuteCallHandlerContent;
import com.github.jsqltool.sql.insert.InsertHandler;
import com.github.jsqltool.sql.insert.impl.InsertHandlerContent;
import com.github.jsqltool.sql.procedure.ProcedureHandler;
import com.github.jsqltool.sql.procedure.impl.ProcedureHandlerContent;
import com.github.jsqltool.sql.schema.IScheamHandler;
import com.github.jsqltool.sql.schema.impl.SchemaHandlerContent;
import com.github.jsqltool.sql.selectTable.SelectTableHandler;
import com.github.jsqltool.sql.selectTable.impl.SelectTableContent;
import com.github.jsqltool.sql.table.TableHandler;
import com.github.jsqltool.sql.table.impl.TableHandlerContent;
import com.github.jsqltool.sql.typeHandler.TypeHandler;
import com.github.jsqltool.sql.typeHandler.impl.TypeHandlerContent;
import com.github.jsqltool.sql.update.IUpdateDataHandler;
import com.github.jsqltool.sql.update.impl.UpdateDataHandlerContent;
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
final public class JsqltoolBuilder {

	Logger logger = LoggerFactory.getLogger(JsqltoolBuilder.class);

	private final IModel model;
	private final CatelogHandlerContent catelog;
	private final SchemaHandlerContent schema;
	private final TableHandlerContent table;
	private final TypeHandlerContent typeHandlerContent;
	// 更新数据处理器
	private final UpdateDataHandlerContent updateDataHandlerContent;
	// 插入数据处理器
	private final InsertHandlerContent insertHandlerContent;
	// 删除数据处理器
	private final DeleteHandlerContent deleteHandlerContent;
	// 删除表/视图处理器
	private final DropTableHandlerContent dropTableHandlerContent;
	// 获取create语句的处理器
	private final CreateTableViewHandlerContent createTableViewHandlerContent;
	// 获取表数据的处理器
	private final SelectTableContent selectTableContent;
	// 存储过程执行器
	ExecuteCallHandlerContent executeCallContent;
	// 储存过程（函数）处理器
	ProcedureHandlerContent procedureHandlerContent;

	private JsqltoolBuilder() {
		logger.info("JsqltoolBuilder start init...");
		long start = System.currentTimeMillis();
		// 读取配置文件
		Properties prop = ConfigPropertiesReader.loadProperties();
		model = ModelBuilder.builder(prop);
		// 初始化CatelogHandlerContent实例
		catelog = CatelogHandlerContent.builder();
		// 初始化SchemaHandlerContent实例
		schema = SchemaHandlerContent.builder();
		// 初始化TableHandlerContent实例
		table = TableHandlerContent.builder();
		// 初始化类型处理器
		typeHandlerContent = TypeHandlerContent.builder();
		// 更新数据处理器
		updateDataHandlerContent = UpdateDataHandlerContent.builder();
		// 插入数据处理器
		insertHandlerContent = InsertHandlerContent.builder();
		// 删除数据处理器
		deleteHandlerContent = DeleteHandlerContent.builder();
		// 删除表格处理器
		dropTableHandlerContent = DropTableHandlerContent.builder();
		// 获取create语句的处理器
		createTableViewHandlerContent = CreateTableViewHandlerContent.builder();
		// 获取表数据的处理器
		selectTableContent = SelectTableContent.builder();
		// 存储过程执行器
		executeCallContent = ExecuteCallHandlerContent.builder();
		// 存储过程（函数）处理器
		procedureHandlerContent = ProcedureHandlerContent.builder();
		logger.info("JsqltoolBuilder inited times:{}ms", System.currentTimeMillis() - start);
	}

	/**
	* @author yzh
	* @date 2019年7月11日
	* @Description: 获取数据库类型
	 */
	public DBType getDbType(Connection connection) throws SQLException {
		return DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName());
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月11日
	* @Description: 获取数据库支持的类型信息
	 */
	public Set<TypeInfo> getDatabaseDataTypeInfo(Connection connection) throws SQLException {
		return JdbcUtil.getTypeInfo(connection);
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月11日
	* @Description: 获取指定表格的列信息
	 */
	public List<ColumnInfo> getColumnInfo(Connection connection, TableColumnsParam param) throws SQLException {
		return JdbcUtil.getTableColumnInfo(connection, param);
	}

	/**
	 * 获取主键信息
	 * 
	 * @author yzh
	 * @date 2019年6月27日
	 */
	public Primary getPrimayInfo(Connection connection, IndexParam param) throws SQLException {
		return table.getPrimaryInfo(connection, param);
	}

	/**
	 * 获取索引信息
	 * 
	 * @author yzh
	 * @date 2019年6月27日
	 */
	public List<Index> listIndexInfo(Connection connection, IndexParam param) throws SQLException {
		return table.getIndexInfo(connection, param);
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
			if (StringUtils.isBlank(param.getCatalog())) {
				logger.warn("mysql数据库必须选择一个databse来执行");
				throw new SqlExecuteException("mysql数据库必须选择一个databse来执行");
			}
			SqlPlus.execute(connect, "use " + param.getCatalog());
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

	public List<SimpleTableInfo> listTable(Connection connection, DBObjectParam param) throws SQLException {
		return table.listTableInfo(connection, param);
	}

	public void addFirstTableHandler(TableHandler handler) {
		table.addFirst(handler);
	}

	public void addLastTableHandler(TableHandler handler) {
		table.addLast(handler);
	}

	public List<TableColumnInfo> getTableColumnInfo(Connection connection, TableColumnsParam param) {
		return table.listTableColumnInfo(connection, param);
	}

	/**
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

	public List<String> listSchema(Connection connection, String catelog) {
		return schema.list(connection, catelog);
	}

	public void addFirstSchemaHandler(IScheamHandler handler) {
		schema.addFirst(handler);
	}

	public void addLastSchemaHandler(IScheamHandler handler) {
		schema.addLast(handler);
	}

	public String getCreateTableView(Connection connect, DBObjectParam param) throws SQLException {
		return createTableViewHandlerContent.getCreateTableView(connect, param);
	}

	public void addFirstCreateTableViewHandler(IcreateTableViewHandler handler) {
		createTableViewHandlerContent.addFirst(handler);
	}

	public void addLastCreateTableViewHandler(IcreateTableViewHandler handler) {
		createTableViewHandlerContent.addLast(handler);
	}

	public UpdateResult dropTable(Connection connect, DropTableParam dropTableParam) throws SQLException {
		return dropTableHandlerContent.drop(connect, dropTableParam);
	}

	public void addFirstDropTableHandler(IdropTableHandler handler) {
		dropTableHandlerContent.addFirst(handler);
	}

	public void addLastDropTableHandler(IdropTableHandler handler) {
		dropTableHandlerContent.addLast(handler);
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

	public void addFirstInsertHandler(InsertHandler handler) {
		insertHandlerContent.addFirst(handler);
	}

	public void addLastInsertHandler(InsertHandler handler) {
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

	public SqlResult selectTable(Connection connection, SelectTableParam param) throws SQLException {
		return selectTableContent.selectTable(connection, param);
	}

	public void addFirsSelectTableHandler(SelectTableHandler handler) {
		selectTableContent.addFirst(handler);
	}

	public void addLastSelectTableHandler(SelectTableHandler handler) {
		selectTableContent.addLast(handler);
	}

	public List<String> listCatalog(Connection connection) {
		return catelog.list(connection);
	}

	public void addFirstCatelogHandler(ICatelogHandler handler) {
		catelog.addFirst(handler);
	}

	public void addLastCatelogHandler(ICatelogHandler handler) {
		catelog.addLast(handler);
	}

	public String executeCall(Connection connection, ProcedureParam param) throws SQLException {
		return executeCallContent.executeCall(connection, param);
	}

	/**
	 * 
	* @author yzh
	* @date 2019年8月17日
	* @Description: 执行程序代码块
	 */
	public String executeCall(Connection connection, String sqlBlock) throws SQLException {
		return executeCallContent.executeCall(connection, sqlBlock);
	}

	public void addFirstExecuteCallHandler(ExecuteCallHandler handler) {
		executeCallContent.addFirst(handler);
	}

	public void addLastExecuteCallHandler(ExecuteCallHandler handler) {
		executeCallContent.addLast(handler);
	}

	public SqlResult listProcedureInfo(Connection connection, DBObjectParam param) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();
		if (StringUtils.containsIgnoreCase(metaData.getDriverName(), "mysql")) {
			if (StringUtils.isBlank(param.getCatalog())) {
				logger.warn("mysql数据库必须选择一个databse来执行");
				throw new SqlExecuteException("mysql数据库必须选择一个databse来执行");
			}
			SqlPlus.execute(connection, "use " + param.getCatalog());
		}
		SqlResult result = procedureHandlerContent.listProcedureInfo(connection, param);
		return result;
	}

	public List<String> listProcedure(Connection connection, DBObjectParam param) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();
		if (StringUtils.containsIgnoreCase(metaData.getDriverName(), "mysql")) {
			if (StringUtils.isBlank(param.getCatalog())) {
				logger.warn("mysql数据库必须选择一个databse来执行");
				throw new SqlExecuteException("mysql数据库必须选择一个databse来执行");
			}
			SqlPlus.execute(connection, "use " + param.getCatalog());
		}
		List<String> result = procedureHandlerContent.lisProcedure(connection, param);
		return result;
	}

	public void addFirstProcedureHandler(ProcedureHandler handler) {
		procedureHandlerContent.addFirst(handler);
	}

	public void addLastProcedureHandler(ProcedureHandler handler) {
		procedureHandlerContent.addLast(handler);
	}

	private static class Builder {
		private static final JsqltoolBuilder instance = new JsqltoolBuilder();
	}

	public static JsqltoolBuilder builder() {
		return Builder.instance;
	}

	public IModel getModel() {
		return model;
	}

}
