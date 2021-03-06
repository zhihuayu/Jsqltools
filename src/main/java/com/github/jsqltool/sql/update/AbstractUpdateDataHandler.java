package com.github.jsqltool.sql.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.exception.UpdateDataException;
import com.github.jsqltool.param.ChangeValue;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.SqlParam;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.sql.SqlPlus;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.vo.Primary;
import com.github.jsqltool.vo.UpdateResult;

public abstract class AbstractUpdateDataHandler implements IUpdateDataHandler {
	private static final Logger logger = LoggerFactory.getLogger(AbstractUpdateDataHandler.class);

	@Override
	final public UpdateResult update(Connection connect, List<UpdateParam> updates, Boolean force) throws SQLException {
		UpdateResult updateResult = new UpdateResult();
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		if (updates == null || updates.isEmpty()) {
			throw new UpdateDataException("更新的数据为空");
		}
		Set<String> tables = new HashSet<>();
		for (UpdateParam u : updates) {
			tables.add(u.getTableName());
		}
		Map<String, List<SqlParam>> sqls = new HashMap<>();
		// 遍历表名
		for (String tableName : tables) {
			UpdateParam update = getByTableName(updates, tableName);
			IndexParam indexParam = new IndexParam();
			indexParam.setCatalog(update.getCatalog());
			indexParam.setSchema(update.getSchema());
			indexParam.setTable(update.getTableName());
			indexParam.setUnique(true);
			Primary primayInfo = builder.getPrimayInfo(connect, indexParam);
			boolean isFirst = true;
			for (UpdateParam param : updates) {
				if (StringUtils.equals(tableName, param.getTableName())) {
					if (isFirst) {
						updateResult = beforeGeneratorSql(primayInfo, param, updateResult);
						isFirst = false;
					}
					SqlParam sqlparam = getSqlParam(connect, param, primayInfo);
					List<SqlParam> list = sqls.get(tableName);
					if (list == null)
						list = new ArrayList<>();
					list.add(sqlparam);
					sqls.put(tableName, list);
				}
			}
		}
		// 如果没有强制执行，则遇到警告就直接返回不执行
		if (updateResult.getCode() != null && updateResult.getCode().equals(UpdateResult.WARN) && !force) {
			logger.warn("更新不会被执行，因为：" + updateResult.getMsg() + "请设置好force参数为true时强制执行！");
			return updateResult;
		}
		return SqlPlus.excutePrepareStatement(connect, sqls);
	}

	/**
	 * 
	* @author yzh
	* @date 2019年6月30日
	* @Description:  在生成SQL语句之前该方法会被执行，用以监测参数的设置情况，
	*     并提前设置UpdateResult，对于每一个table该方法只会执行一次
	*  @param dbType  数据类型
	*  @param primayInfo
	*  @param param
	*  @param updateResult
	*  @return    参数
	* @return UpdateResult    返回类型
	* @throws
	 */
	protected abstract UpdateResult beforeGeneratorSql(Primary primayInfo, UpdateParam param,
			UpdateResult updateResult);

	private SqlParam getSqlParam(Connection connect, UpdateParam param, Primary primayInfo) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		String tableInfo = JdbcUtil.getTableNameInfo(connect, param.getCatalog(), param.getSchema(),
				param.getTableName());
		sb.append(tableInfo);
		List<ChangeValue> values = param.getValues();
		List<Object> zwf = new ArrayList<>();
		// 拼装set值
		String set = getSqlSet(zwf, values);
		sb.append(set);
		// 拼装where条件
		String where = getSqlWhere(zwf, values, primayInfo);
		sb.append(where);
		SqlParam result = new SqlParam();
		result.setCatalog(param.getCatalog());
		result.setSchema(param.getSchema());
		afterSqlGenerator(sb);
		result.setSql(sb.toString());
		result.setParam(zwf.toArray());
		return result;
	}

	/**
	 * 
	* @author yzh
	* @date 2019年6月30日
	* @Description:  当生成预编译的sql语句之后，会调用该方法，可以在该方法中通过修改参数来修改最终的sql（慎重）
	* ，也可以输出日志，默认实现为空，子类可以实现该方法
	*  @param sb    参数
	* @return void    返回类型
	* @throws
	 */
	protected void afterSqlGenerator(StringBuilder sb) {
	}

	protected abstract String getSqlSet(List<Object> zwf, List<ChangeValue> values);

	/**
	 * 
	* @author yzh
	* @date 2019年6月30日
	* @Description:  获取where条件
	*  @param zwf 对应预定义参数的值
	*  @param values 修改的值
	*  @param primayInfo
	* @return String   返回where的sql语句
	* @throws
	 */
	protected abstract String getSqlWhere(List<Object> zwf, List<ChangeValue> values, Primary primayInfo);

	private UpdateParam getByTableName(List<UpdateParam> updates, String tableName) {
		for (UpdateParam param : updates) {
			if (StringUtils.equals(param.getTableName(), tableName)) {
				return param;
			}
		}
		return null;
	}

}
