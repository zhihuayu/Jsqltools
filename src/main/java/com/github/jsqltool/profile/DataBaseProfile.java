package com.github.jsqltool.profile;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.SqlPlus.Column;
import com.github.jsqltool.sql.SqlPlus.Record;
import com.github.jsqltool.sql.SqlPlus.SqlResult;
import com.github.jsqltool.utils.JdbcUtil;

public class DataBaseProfile {

	private final static String tableName = "t_jsqltool_connection_info";
	private String createTableSql = "create table " + tableName + " (id int primary key ,\r\n"
			+ "  f_user varchar(30) not null ,\r\n" + "  f_name varchar(30) not null,\r\n"
			+ "  driver_class_name varchar(60) not null ,\r\n" + "  f_url varchar(128) not null,\r\n"
			+ "  user_name varchar(30),\r\n" + "  pwd varchar(60),\r\n"
			+ " CONSTRAINT index_unique unique(f_user,f_name)\r\n" + "  )";

	private final ConnectionInfo connectionInfo;

	private final Object lock = new Object();

	public DataBaseProfile(String name, String driverClassName, String url, String userName, String password) {
		this.connectionInfo = new ConnectionInfo();
		this.connectionInfo.setName(StringUtils.trim(name));
		this.connectionInfo.setDriverClassName(StringUtils.trim(driverClassName));
		this.connectionInfo.setUrl(StringUtils.trim(url));
		this.connectionInfo.setUserName(StringUtils.trim(userName));
		this.connectionInfo.setPassword(StringUtils.trim(password));
		init();
	}

	public DataBaseProfile(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
		init();
	}

	public List<String> listConnection(String user) throws SQLException {
		try (Connection connect = JsqltoolBuilder.builder().connect(connectionInfo);) {
			String sql = "select f_name from " + tableName + " where ";
			if (StringUtils.isBlank(user)) {
				sql += "f_user = '' or f_user = ' ' ";
			} else {
				sql += "f_user =" + user;
			}
			SqlResult execute = SqlPlus.execute(connect, sql);
			List<Record> records = execute.getRecords();
			List<String> result = new ArrayList<>();
			if (records != null && !records.isEmpty()) {
				for (Record r : records) {
					List<Object> values = r.getValues();
					if (values != null && values.size() == 1) {
						Object v = values.get(0);
						if (v != null && v instanceof String && StringUtils.isNotBlank((String) v)) {
							result.add(StringUtils.trim((String) v));
						}
					}
				}
			}
			return result;
		}
	}

	public boolean save(String user, ConnectionInfo info) throws SQLException {
		try (Connection connect = JsqltoolBuilder.builder().connect(connectionInfo);) {
			synchronized (lock) {
				// 1.检查id是否已经存在
				Integer id = getIdByConnectionInfo(connect, user, info);
				if (id != null && id.compareTo(0) > 0) {
					// 2.1 如果id存在，则更新
					String updateSql = getUpdateSql(id, user, info);
					SqlPlus.execute(connect, updateSql);
					return true;
				} else {
					// 2.2 如果不存在，则新增
					id = getMaxId(connect);
					String insertSql = getInsertSql(id + 1, user, info);
					SqlPlus.execute(connect, insertSql);
					return true;
				}
			}
		}
	}

	private String getInsertSql(Integer id, String user, ConnectionInfo info) {
		if (id == null || id.compareTo(0) <= 0) {
			throw new JsqltoolParamException("获取insert语句失败！");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("insert into  " + tableName);
		sb.append(" ( id,f_user,f_name,driver_class_name,f_url,user_name,pwd )");
		sb.append(" values (");
		sb.append(id + ",");
		sb.append(getStringValue(user) + ",");
		sb.append(getStringValue(info.getName()) + ",");
		if (StringUtils.isBlank(info.getDriverClassName())) {
			throw new JsqltoolParamException("driverClassName不能为空！");
		}
		sb.append(getStringValue(info.getDriverClassName()) + ",");

		if (StringUtils.isBlank(info.getUrl())) {
			throw new JsqltoolParamException("url不能为空！");
		}
		sb.append(getStringValue(info.getUrl()) + ",");

		if (StringUtils.isBlank(info.getUserName())) {
			throw new JsqltoolParamException("userName不能为空！");
		}
		sb.append(getStringValue(info.getUserName()) + ",");

		if (StringUtils.isBlank(info.getPassword())) {
			throw new JsqltoolParamException("password不能为空！");
		}
		sb.append(getStringValue(info.getPassword()));
		sb.append(" )");
		return sb.toString();
	}

	private synchronized Integer getMaxId(Connection connect) {
		String sql = "select max(id) from " + tableName;
		SqlResult execute = SqlPlus.execute(connect, sql);
		List<Record> records = execute.getRecords();
		if (records == null || records.isEmpty()) {
			return 1;
		}
		if (records.size() != 1) {
			throw new JsqltoolParamException("获取最大的id值出错！");
		}
		Record record = records.get(0);
		if (record.getValues() == null || record.getValues().size() != 1) {
			throw new JsqltoolParamException("获取最大的id值出错！");
		}
		Object object = record.getValues().get(0);
		if (object == null)
			return 1;
		if (object instanceof Number) {
			return ((Number) object).intValue();
		}
		return 1;
	}

	private String getUpdateSql(Integer id, String user, ConnectionInfo info) {
		if (id == null || id.compareTo(0) <= 0) {
			throw new JsqltoolParamException("id不能空");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("update " + tableName);
		sb.append(" set ");
		sb.append("f_user = ");
		sb.append(getStringValue(user));
		sb.append(",");
		sb.append(" f_name = ");
		sb.append(getStringValue(info.getName()));
		sb.append(",");
		//
		sb.append(" driver_class_name = ");
		if (StringUtils.isBlank(info.getDriverClassName())) {
			throw new JsqltoolParamException("driverClassName不能为空！");
		}
		sb.append(getStringValue(info.getDriverClassName()));
		sb.append(",");
		//
		sb.append(" f_url = ");
		if (StringUtils.isBlank(info.getUrl())) {
			throw new JsqltoolParamException("url不能为空！");
		}
		sb.append(getStringValue(info.getUrl()));
		sb.append(",");
		//
		sb.append(" user_name = ");
		if (StringUtils.isBlank(info.getUserName())) {
			throw new JsqltoolParamException("userName不能为空！");
		}
		sb.append(getStringValue(info.getUserName()));
		sb.append(",");
		//
		sb.append(" pwd = ");
		if (StringUtils.isBlank(info.getPassword())) {
			throw new JsqltoolParamException("password不能为空！");
		}
		sb.append(getStringValue(info.getPassword()));
		sb.append(" where id = ");
		sb.append(id);
		return sb.toString();
	}

	private String getStringValue(String str) {
		if (StringUtils.isBlank(str)) {
			return "' '";
		}
		return "'" + StringUtils.trim(str) + "'";
	}

	/**
	 * 
	* @author yzh
	* @date 2019年7月7日
	* @Description:  根据用户user和info获取连接的id
	 */
	private Integer getIdByConnectionInfo(Connection connect, String user, ConnectionInfo info) {
		Integer id = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select id from " + tableName);
		sb.append(" where f_name= ");
		if (StringUtils.isBlank(info.getName())) {
			throw new JsqltoolParamException("连接名不能为空");
		}
		sb.append("'" + StringUtils.trim(info.getName()) + "'");
		sb.append(" and ");
		if (StringUtils.isBlank(user)) {
			sb.append("( f_user='' or f_user = ' ')");
		} else {
			sb.append(" f_user ='" + StringUtils.trim(user) + "'");
		}
		SqlResult execute = SqlPlus.execute(connect, sb.toString());
		if (execute.getRecords() != null && execute.getRecords().size() == 1) {
			Record records = execute.getRecords().get(0);
			if (records != null && records.getValues() != null && records.getValues().size() == 1) {
				Object object = records.getValues().get(0);
				if (object instanceof Number) {
					id = ((Number) object).intValue();
				}
			}

		}
		return id;
	}

	public ConnectionInfo getConnectionInfo(String user, String connectionName) {
		StringBuilder sb = getSelectSql(user, connectionName);
		try (Connection connect = JsqltoolBuilder.builder().connect(connectionInfo);) {
			SqlResult execute = SqlPlus.execute(connect, sb.toString());
			List<Record> records = execute.getRecords();
			if (records != null && records.size() == 1) {
				ConnectionInfo info = new ConnectionInfo();
				Record record = records.get(0);
				List<Column> columns = execute.getColumns();
				List<Object> values = record.getValues();
				for (int i = 0; i < columns.size(); i++) {
					Column c = columns.get(i);
					// driver_class_name
					if (StringUtils.equalsIgnoreCase("driver_class_name", c.getColumnName())) {
						Object v = values.get(i);
						if (v == null) {
							throw new JsqltoolParamException("获取的driver_class_name不能为空");
						}
						info.setDriverClassName(v.toString());
					}
					// url
					if (StringUtils.equalsIgnoreCase("f_url", c.getColumnName())) {
						Object v = values.get(i);
						if (v == null) {
							throw new JsqltoolParamException("获取的f_url不能为空");
						}
						info.setUrl(v.toString());
					}
					// user_name
					if (StringUtils.equalsIgnoreCase("user_name", c.getColumnName())) {
						Object v = values.get(i);
						if (v == null) {
							throw new JsqltoolParamException("获取的user_name不能为空");
						}
						info.setUserName(v.toString());
					}
					// pwd
					if (StringUtils.equalsIgnoreCase("pwd", c.getColumnName())) {
						Object v = values.get(i);
						if (v == null) {
							throw new JsqltoolParamException("获取的pwd不能为空");
						}
						info.setPassword(v.toString());
					}
				}
				return info;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsqltoolParamException(e);
		}
		return null;
	}

	private StringBuilder getSelectSql(String user, String connectionName) {
		StringBuilder sb = new StringBuilder("select * from " + tableName);
		sb.append(" where  ");
		if (StringUtils.isBlank(user)) {
			sb.append("( f_user='' or f_user = ' ')");
		} else {
			sb.append("f_user ='" + StringUtils.trim(user) + "'");
		}
		sb.append(" and f_name = ");
		if (StringUtils.isBlank(connectionName)) {
			throw new JsqltoolParamException("连接名不能为空！");
		}
		sb.append("'" + StringUtils.trim(connectionName) + "'");
		return sb;
	}

	public boolean delete(String user, String connectionName) {
		try (Connection connect = JsqltoolBuilder.builder().connect(connectionInfo);) {
			StringBuilder deleteSql = getDeleteSql(user, connectionName);
			SqlPlus.execute(connect, deleteSql.toString());
			JdbcUtil.commit(connect);
			return true;
		} catch (Exception e) {
			throw new JsqltoolParamException(e);
		}
	}

	private StringBuilder getDeleteSql(String user, String connectionName) {
		StringBuilder sb = new StringBuilder("delete from " + tableName);
		sb.append(" where ");
		if (StringUtils.isBlank(user)) {
			sb.append("( f_user='' or f_user = ' ')");
		} else {
			sb.append("f_user='" + StringUtils.trim(user) + "'");
		}
		sb.append(" and f_name = ");
		if (StringUtils.isBlank(connectionName)) {
			throw new JsqltoolParamException("连接名不能为空！");
		}
		sb.append("'" + StringUtils.trim(connectionName) + "'");
		return sb;
	}

	private void init() {
		String sql = "SELECT COUNT(*) FROM " + tableName;
		try (Connection conn = connect(); Statement statment = conn.createStatement();) {
			boolean existed = false;
			for (int i = 0; i <= 1; i++) {
				try {
					statment.executeQuery(sql);
					existed = true;
					break;
				} catch (Exception e) {
					createTable(conn);
				}
			}
			if (!existed) {
				throw new JsqltoolParamException("DataBaseProfile初始化失败！");
			}
		} catch (Exception e) {
			throw new JsqltoolParamException("DataBaseProfile初始化失败！", e);
		}

	}

	private void createTable(Connection conn) {
		try (Statement statement = conn.createStatement();) {
			statement.execute(createTableSql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.commit(conn);
		}
	}

	private Connection connect() {
		return JdbcUtil.connect(connectionInfo);
	}

}