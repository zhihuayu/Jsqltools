package com.github.jsqltool.sql.dropTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.DropTableParam;
import com.github.jsqltool.param.DropTableParam.SimpleInfo;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.vo.UpdateResult;

/**
 * 默认的数据表删除器
 * @author yzh
 * @date 2019年7月9日
 */
public class DefaultDropTable implements IdropTableHandler {

	@Override
	public UpdateResult drop(Connection connect, DropTableParam dropTableParam) throws SQLException {
		if (dropTableParam == null || dropTableParam.getInfos() == null || dropTableParam.getInfos().isEmpty()) {
			throw new JsqltoolParamException("删除的对象不能为空！");
		}
		UpdateResult result = new UpdateResult();
		List<String> sqls = new ArrayList<>();
		for (SimpleInfo info : dropTableParam.getInfos()) {
			String sql = getDropTableSql(connect, dropTableParam.getCatalog(), dropTableParam.getSchema(), info);
			sqls.add(sql);
		}
		int effectRows = SqlPlus.executeUpdate(connect, sqls);
		result.setEffectRows(effectRows);
		result.setCode(UpdateResult.OK);
		return result;
	}

	private String getDropTableSql(Connection connect, String catalog, String schema, SimpleInfo info) throws SQLException {
		String tableNameInfo = JdbcUtil.getTableNameInfo(connect, catalog, schema, info.getTableName());
		if (StringUtils.isBlank(tableNameInfo)) {
			throw new JsqltoolParamException("不能获取table信息！");
		}
		String type = StringUtils.isBlank(info.getType()) ? "TABLE" : info.getType().toUpperCase();
		return String.format("drop %s %s", type, tableNameInfo);
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

}
