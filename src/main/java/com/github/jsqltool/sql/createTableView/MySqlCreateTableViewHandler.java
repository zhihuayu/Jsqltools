package com.github.jsqltool.sql.createTableView;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.CreateTableViewException;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.DBObjectParam;
import com.github.jsqltool.result.SqlResult;
import com.github.jsqltool.result.SqlResult.Record;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.utils.JdbcUtil;

/**
 * 支持MySQL
 * @author yzh
 * @date 2019年7月12日
 */
public class MySqlCreateTableViewHandler implements IcreateTableViewHandler {

	@Override
	public String getCreateTableView(Connection connect, DBObjectParam param) throws SQLException {
		if (param == null || StringUtils.isBlank(param.getCatalog()) || StringUtils.isBlank(param.getName())) {
			throw new JsqltoolParamException("数据库名称和表名不能为空！");
		}
		String type = StringUtils.isBlank(param.getType()) ? "TABLE" : StringUtils.upperCase(param.getType());
		StringBuilder sb = new StringBuilder();
		sb.append("show create ");
		sb.append(type);
		String tableNameInfo = JdbcUtil.getTableNameInfo(connect, param.getCatalog(), param.getSchema(),
				param.getName());
		sb.append(" " + tableNameInfo);

		SqlResult execute = SqlPlus.execute(connect, sb.toString());
		if (execute != null && execute.getRecords() != null && execute.getRecords().size() == 1) {
			Record records = execute.getRecords().get(0);
			List<Object> values = records.getValues();
			if (values != null ) {
				Object object = values.get(1);
				return object.toString();
			}
		}
		throw new CreateTableViewException("MySqlCreateTableViewHandler没有获取到对应的SQL语句");
	}

	@Override
	public boolean support(DBType dbType) {
		if (dbType != null && dbType == DBType.MYSQL_TYPE)
			return true;
		return false;
	}

}
