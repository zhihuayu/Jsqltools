package com.github.jsqltool.sql.insert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.exception.UpdateDataException;
import com.github.jsqltool.param.ChangeValue;
import com.github.jsqltool.param.SqlParam;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.sql.typeHandler.TypeHandler;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.vo.UpdateResult;

public class DefaultInsertHandler implements IinertHandler {

	public UpdateResult insert(Connection connect, List<UpdateParam> updates) throws SQLException {
		if (updates == null || updates.isEmpty()) {
			throw new UpdateDataException("插入的数据为空");
		}
		Set<String> tables = new HashSet<>();
		for (UpdateParam u : updates) {
			tables.add(u.getTableName());
		}
		// key为表名，value为需要插入的语句的集合
		Map<String, List<SqlParam>> sqls = new HashMap<>();
		// 遍历表名
		for (String tableName : tables) {
			for (UpdateParam param : updates) {
				if (StringUtils.equals(tableName, param.getTableName())) {
					SqlParam sqlparam = getSqlParam(connect, param);
					List<SqlParam> list = sqls.get(tableName);
					if (list == null)
						list = new ArrayList<>();
					list.add(sqlparam);
					sqls.put(tableName, list);
				}
			}
		}
		return SqlPlus.excutePrepareStatement(connect, sqls);
	}

	private SqlParam getSqlParam(Connection connect, UpdateParam param) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		String tableInfo = JdbcUtil.getTableNameInfo(connect, param.getCatalog(), param.getSchema(),
				param.getTableName());
		sb.append(tableInfo);
		List<ChangeValue> values = param.getValues();
		List<Object> zwf = new ArrayList<>();
		// 拼装values值
		String v = getSqlValues(zwf, values);
		sb.append(v);
		SqlParam result = new SqlParam();
		result.setCatalog(param.getCatalog());
		result.setSchema(param.getSchema());
		afterSqlGenerator(sb);
		result.setSql(sb.toString());
		result.setParam(zwf.toArray());
		return result;
	}

	protected void afterSqlGenerator(StringBuilder sb) {
	}

	@SuppressWarnings("rawtypes")
	private String getSqlValues(List<Object> zwf, List<ChangeValue> values) {
		if (values == null || values.isEmpty()) {
			throw new UpdateDataException("插入的字段数不能为空！");
		}
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		TypeHandler typeHandler = builder.getTypeHandler();
		StringBuilder sb = new StringBuilder();
		StringBuilder fields = new StringBuilder(" (");
		StringBuilder variables = new StringBuilder(" (");
		for (ChangeValue v : values) {
			if (StringUtils.equalsIgnoreCase("rowid", v.getColumnName())||StringUtils.equalsIgnoreCase("row_id", v.getColumnName())) {
				continue;
			}
			fields.append(v.getColumnName() + ",");
			variables.append("?,");
			try {
				zwf.add(typeHandler.getParam(v.getNewValue(), JdbcType.forCode(v.getDataType())));
			} catch (Exception e) {
				throw new JsqltoolParamException(v.getColumnName() + ":" + e.getMessage());
			}
		}
		variables.setLength(variables.length() - 1);
		fields.setLength(fields.length() - 1);
		variables.append(" )");
		fields.append(" )");
		// 拼装SQL
		sb.append(fields);
		sb.append(" values ");
		sb.append(variables);
		return sb.toString();
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

}
