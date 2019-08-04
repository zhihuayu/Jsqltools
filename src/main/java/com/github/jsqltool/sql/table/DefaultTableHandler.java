package com.github.jsqltool.sql.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.TablesParam;
import com.github.jsqltool.sql.SimpleTableInfo;
import com.github.jsqltool.utils.JdbcUtil;

public class DefaultTableHandler implements ITableHandler {

	@Override
	public List<SimpleTableInfo> list(Connection connection, TablesParam param) throws SQLException {
		List<SimpleTableInfo> list = new ArrayList<>();
		if (StringUtils.isBlank(param.getType())) {
			throw new JsqltoolParamException("type参数不能为空");
		}
		String catelog = StringUtils.isBlank(param.getCatalog()) ? null : param.getCatalog().toUpperCase();
		String schema = StringUtils.isBlank(param.getSchema()) ? null : param.getSchema().toUpperCase();
		String tableName=JdbcUtil.covertName(connection, param.getTable());
		try (ResultSet rset = connection.getMetaData().getTables(catelog, schema, tableName,
				new String[] { param.getType() });) {
			while (rset.next()) {
				SimpleTableInfo info = new SimpleTableInfo();
				info.setTableName(rset.getString(3));
				info.setComment(rset.getString(5));
				list.add(info);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

}
