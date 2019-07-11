package com.github.jsqltool.sql.create;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.exception.SqlParseException;
import com.github.jsqltool.param.CreateParam;
import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.utils.StringUtil;

/**
 * 该类支持MySQL和Oracle数据库中的表create操作
 * 
 * @author yzh
 * @date 2019年6月26日
 */
public class DefaultCreateStatement implements ICreateStatement {

	private Connection connection;
	private String driverName;
	private String tableInfo;
	private String tableComment;
	private List<String> baseColumnInfo = new ArrayList<>();
	private List<PrimaryKey> pks = new ArrayList<>();
	private List<CommentInfo> comments = new ArrayList<>();

	@Override
	public List<String> createTable(Connection connection, TableColumnsParam tableParam, List<CreateParam> param) {
		this.connection = connection;
		List<String> result = new ArrayList<>();
		try {
			driverName = connection.getMetaData().getDriverName();
			tableComment = tableParam.getComment();
			// 设置create 头部信息，如：test.tableName
			tableInfo = JdbcUtil.getTableNameInfo(connection, tableParam.getCatalog(), tableParam.getSchema(),
					tableParam.getTableName());
			if (param == null || param.isEmpty()) {
				throw new SqlParseException("不能解析create语句，因为没有字段参数！");
			}
			// 设置主键信息和comment信息
			setPksAndComment(param);
			// 最终的解析
			result = parse();
		} catch (Exception e) {
			throw new SqlParseException("create sql时出错", e);
		}
		return result;
	}

	private List<String> parse() {
		List<String> sql = new ArrayList<>();
		// 解析主体
		String main = parseSqlMain();
		sql.add(main);
		// 解析comment[针对Oracle数据库]
		if (isOracle()) {
			// 表comment
			if (StringUtils.isNotBlank(tableComment)) {
				sql.add("comment on table " + tableInfo + " is " + "'" + tableComment + "'");
			}
			// 列comment
			if (!comments.isEmpty()) {
				for (CommentInfo info : comments)
					sql.add("comment on column " + tableInfo + "." + info.getColumnName() + "  is " + "'"
							+ info.getComment() + "'");
			}
		}
		return sql;
	}

	/**
	 * 解析主体sql
	 * 
	 * @author yzh
	 *
	 * @date 2019年6月20日
	 */
	private String parseSqlMain() {
		StringBuilder sb = new StringBuilder();
		sb.append("create table ");
		sb.append(tableInfo);
		sb.append(" ( ");
		for (String str : baseColumnInfo) {
			sb.append(str);
			sb.append(",");
		}
		// 解析pks
		if (!pks.isEmpty()) {
			sb.append("primary key (");
			for (PrimaryKey key : pks) {
				sb.append(key.columnName);
				sb.append(",");
			}
			sb.setLength(sb.length() - 1);
			sb.append(")");
		} else {
			sb.setLength(sb.length() - 1);
		}
		sb.append(" )");
		if (isMySql() && StringUtils.isNotBlank(tableComment)) {
			sb.append(" comment '");
			sb.append(tableComment);
			sb.append("'");
		}

		return sb.toString();
	}

	private void setPksAndComment(List<CreateParam> param) throws SQLException {
		for (CreateParam p : param) {
			if (!CreateParam.validate(p)) {
				throw new SqlParseException("不能解析create语句，字段参数名或者类型为空！");
			}
			// 主键信息
			Boolean primaryKey = p.getPrimaryKey();
			if (primaryKey != null && primaryKey) {
				pks.add(new PrimaryKey(p.getColumnName()));
			}
			// comment信息
			if (StringUtils.isNotBlank(p.getComment())) {
				comments.add(new CommentInfo(p.getColumnName(), p.getComment()));
			}
			// 基本语句
			addBaseCoumnInfo(p);

		}

	}

	private void addBaseCoumnInfo(CreateParam p) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append(p.getColumnName());
		appendType(sb, p);
		appendNotNull(sb, p);
		appendAutoIncrement(sb, p);
		appendMySqlComment(sb, p);
		baseColumnInfo.add(sb.toString());
	}

	private void appendMySqlComment(StringBuilder sb, CreateParam p) {
		if (isMySql() && StringUtils.isNotBlank(p.getComment())) {
			sb.append(" comment ");
			sb.append("'" + p.getComment() + "'");
		}
	}

	private void appendNotNull(StringBuilder sb, CreateParam p) {
		if (p.getNotNull() != null && p.getNotNull()) {
			sb.append(" not null");
		}

	}

	private void appendAutoIncrement(StringBuilder sb, CreateParam p) throws SQLException {
		if (p.getAutoIncrement() != null && p.getAutoIncrement()
				&& StringUtils.containsIgnoreCase(connection.getMetaData().getDriverName(), "mysql")) {
			sb.append(" auto_increment");
		}
	}

	/**
	 * 判断是否是MySQL数据库
	 * 
	 * @author yzh
	 *
	 * @date 2019年6月20日
	 */
	private boolean isMySql() {
		if (StringUtils.containsIgnoreCase(driverName, "mysql")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是Oracle数据库
	 * 
	 * @author yzh
	 *
	 * @date 2019年6月20日
	 */
	private boolean isOracle() {
		if (StringUtils.containsIgnoreCase(driverName, "oracle")) {
			return true;
		}
		return false;
	}

	private void appendType(StringBuilder sb, CreateParam p) {
		if (StringUtil.isFind(p.getType(), "\\(.*\\)")) {
			throw new SqlParseException("不能解析create语句，字段类型！" + p.getType() + "非法");
		}
		sb.append(" ");
		sb.append(p.getType());
		if (p.getLength() != null && p.getLength() > 0) {
			sb.append("(" + p.getLength() + ")");
		}
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

	// comment信息
	class CommentInfo {
		private String columnName;
		private String comment;

		public CommentInfo(String columnName, String comment) {
			super();
			this.columnName = columnName;
			this.comment = comment;
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

	}

	// 主键信息
	class PrimaryKey {
		private String columnName;

		public PrimaryKey(String columnName) {
			this.columnName = columnName;
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
	}

}
